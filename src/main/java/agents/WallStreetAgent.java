package agents;

import jadex.bridge.service.types.clock.IClockService;
import jadex.commons.future.CounterResultListener;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.commons.future.SubscriptionIntermediateFuture;
import jadex.commons.transformation.annotations.IncludeFields;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentCreated;
import jadex.micro.annotation.AgentFeature;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;
import jadex.rules.eca.ChangeInfo;
import services.IManagerService;
import services.IPlayerService;
import services.IWallStreetService;
import ui.MarketWindow;
import util.ArgumentableResultListener;
import util.Console;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bdiv3.runtime.ChangeEvent;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.component.IExecutionFeature;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.search.SServiceProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import classes.Company;
import classes.Deck;
import classes.Investor;
import classes.Manager;
import classes.Market;
import classes.Player;

@Agent
@Service
@ProvidedServices({ @ProvidedService(type = IWallStreetService.class) })
@RequiredServices(@RequiredService(name = "clockservice", type = IClockService.class, binding = @Binding(scope = RequiredServiceInfo.SCOPE_PLATFORM)))

public class WallStreetAgent implements IWallStreetService {
	@IncludeFields
	public static enum PlayingMode {
		INVESTOR, MANAGER
	}

	@IncludeFields
	public static enum GameState {
		ESTABLISHING_GAME, NEGOTIATION, EXCHANGING_INCOMES, SOLVING_MANAGERS_DEBTS, AUCTIONING_NEW_COMPANIES, GAME_ENDED
	}

	private static int numberOfPlayers = 11;
	public static int companyFee = 10;
	public static int valueOfCompany = 5;

	@Agent
	protected IInternalAccess ia;

	@Belief
	protected List<Player> players = new ArrayList<>();

	@Belief(dynamic = true)
	protected List<Investor> investors = players.stream().filter(p -> p instanceof Investor).map(p -> (Investor) p)
			.collect(Collectors.toList());

	@Belief
	WallStreetAgent.GameState gameState = WallStreetAgent.GameState.ESTABLISHING_GAME;

	protected Market market = new Market();
	protected Deck deck = new Deck();
	protected Console console;
	protected MarketWindow marketUI;

	@AgentBody
	void init() {
		this.console = new Console(ia.getComponentIdentifier());
		this.marketUI = new MarketWindow(market, getManagers());
		marketUI.draw();

	}

	List<Manager> getManagers() {
		return players.stream().filter(p -> p instanceof Manager).map(p -> (Manager) p).collect(Collectors.toList());
	}

	List<Manager> getInGameManagers() {
		return players.stream().filter(p -> p instanceof Manager && p.balance >= 0).map(p -> (Manager) p)
				.collect(Collectors.toList());
	}

	List<Investor> getValidInvestors() {
		return players.stream().filter(p -> p instanceof Investor && p.balance >= 0).map(p -> (Investor) p)
				.collect(Collectors.toList());
	}

	void refreshUI() {
		marketUI.setManagers(getManagers());
		marketUI.draw();
	}

	public Future<Void> syncGameInformation() {
		Future<Void> all = new Future<Void>();
		CounterResultListener<Void> allCounter = new CounterResultListener<>(players.size(),
				new DelegationResultListener<Void>(all));

		for (Player player : players) {
			ArrayList<Player> otherPlayers = new ArrayList<>(players);
			otherPlayers.remove(otherPlayers.indexOf(player));

			IPlayerService playerService = SServiceProvider
					.getService(ia, player.getComponentIdentifier(), IPlayerService.class).get();
			playerService.syncGameInformation(player, otherPlayers, market).addResultListener(allCounter);

		}

		return all;
	}

	public IFuture<Void> announceGameStateChange(GameState gameState) {
		Future<Void> ret = new Future<>();
		Future<Void> all = new Future<Void>();
		CounterResultListener<Void> allCounter = new CounterResultListener<>(players.size(),
				new DelegationResultListener<Void>(all));

		for (Player player : players) {
			IPlayerService playerService = SServiceProvider
					.getService(ia, player.getComponentIdentifier(), IPlayerService.class).get();
			playerService.updateGameState(gameState).addResultListener(allCounter);
		}

		all.addResultListener((new ArgumentableResultListener<Void>(new Object[] { this, gameState }) {
			@Override
			public void resultAvailable(Void result) {
				((WallStreetAgent) args[0]).gameState = ((GameState) args[1]);
				ret.setResult((Void) null);
				console.log(") ready to " + ((WallStreetAgent) args[0]).gameState);
			}
		}));
		return ret;
	}

	private Company auctionNewCompany(Company company) {
		Future<Boolean> ret = new Future<>();
		List<Manager> managersInAuction = getInGameManagers();
		List<Company> validOffers;

		// ask each manager for an offer, if more than one valid
		do {
			Collections.shuffle(managersInAuction);
			validOffers = new ArrayList<>();
			for (int i = 0; i < managersInAuction.size(); i++) {
				IManagerService managerService = managersInAuction.get(i).getManagerService(ia);
				Company offer = managerService.informNewCompanyAuction(company).get();
				if (offer == null || offer.currentOffer <= company.currentOffer
						|| managersInAuction.get(i).balance <= company.currentOffer) {
					managersInAuction.remove(i);

				} else {
					validOffers.add(offer);
				}
			}
		} while (validOffers.size() > 1);

		return validOffers.size() > 0 ? validOffers.get(0) : null;

	}

	@Override
	public IFuture<Player> join(IComponentIdentifier componentIdentifier, PlayingMode playingAs) {
		Future<Player> ret = new Future<>();

		if (players.size() < numberOfPlayers) {

			Player player = playingAs.equals(PlayingMode.INVESTOR) ? new Investor(componentIdentifier)
					: new Manager(componentIdentifier, deck.fetchCompanies(2));
			players.add(player);
			ret.setResult(player);

			if (players.size() == numberOfPlayers) {
				manageGame();

			}
		} else {
			ret.setResult(null);
		}

		return ret;
	}

	void changeTo(WallStreetAgent.GameState gameState, boolean sync) {
		IExecutionFeature exe = ia.getComponentFeature(IExecutionFeature.class);

		if (sync) {
			syncGameInformation().get();
			exe.waitForDelay(100).get();
		}
		announceGameStateChange(gameState).get();
		if(gameState == GameState.NEGOTIATION)
			this.refreshUI();
	}

	void manageGame() {
		IExecutionFeature exe = ia.getComponentFeature(IExecutionFeature.class);

		for (int i = 0; i < 5; i++) {

			changeTo(GameState.NEGOTIATION, true);
			exe.waitForDelay(10000).get();

			changeTo(GameState.EXCHANGING_INCOMES, false);

			updateMarket();
			applyInvestorsIncome();
			applyManagersIncome();
			applyManagmentCosts();

			changeTo(GameState.SOLVING_MANAGERS_DEBTS, true);

			solveManagersDebts();

			changeTo(GameState.AUCTIONING_NEW_COMPANIES, true);

			auctionNewCompanies();
		}
		return;
	}

	private int numberOfManagersInGame() {
		int counter = 0;
		for (Player player : players) {
			if (player instanceof Manager && player.balance >= 0) {
				counter++;
			}
		}
		return counter;
	}

	private void auctionNewCompanies() {
		int numberOfNewCompanies = Math.max(0, 2 * numberOfManagersInGame() - 1);
		List<Company> companies = deck.fetchCompanies(numberOfNewCompanies);

		for (Company newCompany : companies) {
			Company offer = auctionNewCompany(newCompany);

			if (offer != null) {
				// perform company sale
				int index = players.indexOf(offer.owner);
				players.get(index).balance -= offer.currentOffer;
				((Manager) players.get(index)).addCompany(newCompany);
				syncGameInformation().get();
			} else {
				// add back to deck
				deck.addCompany(newCompany);
			}
		}

	}

	private ArrayList<Company> getManagersCompanies() {
		ArrayList<Company> companies = new ArrayList<>();
		for (Manager manager : getInGameManagers()) {
			companies.addAll(((Manager) manager).getCompanies());
		}
		return companies;
	}

	private void applyInvestorsIncome() {
		for (Company company : getManagersCompanies()) {
			if (company.currentInvestor != null) {
				company.currentInvestor.balance += market.calcCompanyRevenue(company);
			}
		}

	}

	private void applyManagersIncome() {
		List<Investor> investors = getValidInvestors();
		for (int i = 0; i < investors.size(); i++) {
			Investor investor = investors.get(i);
			ArrayList<Company> companies = fetchInvestorCompanies(investor);
			int totalOffers = calcTotalOffers(companies);
			if (investor.balance < totalOffers) {
				int payment = totalOffers / companies.size();

				for (Company company : companies) {
					company.owner.balance += payment;
					company.currentOffer = 0;
					company.currentInvestor = null;
					company.closed = false;
				}

			} else {
				for (Company company : companies) {
					company.owner.balance += company.currentOffer;
					company.closed = false;
				}
			}
			investor.balance -= totalOffers;
		}
	}

	private int calcTotalOffers(ArrayList<Company> companies) {
		int totalOffers = 0;
		for (Company company : companies) {
			totalOffers += company.currentOffer;
		}
		return totalOffers;
	}

	private ArrayList<Company> fetchInvestorCompanies(Investor investor) {
		ArrayList<Company> allCompanies = getManagersCompanies();
		ArrayList<Company> investorCompanies = new ArrayList<>();
		for (Company company : allCompanies) {
			if (investor.equals(company.currentInvestor)) {
				investorCompanies.add(company);
			}
		}
		return investorCompanies;
	}

	private void applyManagmentCosts() {
		for (Player player : players) {
			if (player instanceof Manager) {
				Manager manager = (Manager) player;
				manager.balance -= manager.companies.size() * WallStreetAgent.companyFee;
			}
		}
	}

	private void solveManagersDebts() {
		for (Player player : players) {
			if (player instanceof Manager) {
				Manager manager = (Manager) player;
				if (manager.balance < 0 && manager.companies.size() < 0) {
					IManagerService managerService = (IManagerService) SServiceProvider
							.getService(ia, manager.getComponentIdentifier(), IManagerService.class).get();
					List<Company> companies = managerService.consultWhatCompaniesToSell().get();
					manager.companies.removeAll(companies);
					deck.addCompanies(companies);
				}
			}
		}
	}

	private void updateMarket() {
		market.rollTheDices();
	}

	@Override
	public IFuture<Void> informOffer(Manager owner, Company company) {
		Manager m = ((Manager) players.get(players.indexOf(owner)));
		m.getCompanies().set(m.getCompanies().indexOf(company), company);
		return Future.DONE;
	}

}
