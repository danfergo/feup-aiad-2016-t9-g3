package agents;

import jadex.bridge.service.types.clock.IClockService;
import jadex.commons.future.CounterResultListener;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IIntermediateResultListener;
import jadex.commons.future.IResultListener;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.IntermediateExceptionDelegationResultListener;
import jadex.commons.future.SubscriptionIntermediateFuture;
import jadex.commons.future.TerminationCommand;
import jadex.commons.transformation.annotations.IncludeFields;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentCreated;
import jadex.micro.annotation.AgentFeature;
import jadex.micro.annotation.Argument;
import jadex.micro.annotation.Arguments;
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
import jadex.bridge.IExternalAccess;
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
@Arguments({ @Argument(name = "numP", clazz = Integer.class, defaultvalue = "11") })
public class WallStreetAgent implements IWallStreetService {
	@IncludeFields
	public static enum PlayingMode {
		INVESTOR, MANAGER
	}

	@IncludeFields
	public static enum GameState {
		ESTABLISHING_GAME, NEGOTIATION, EXCHANGING_INCOMES, SOLVING_MANAGERS_DEBTS, AUCTIONING_NEW_COMPANIES, GAME_ENDED
	}

	private int numberOfPlayers;
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
	protected boolean gameEnded;

	@AgentBody
	void init() {
		this.numberOfPlayers = (Integer) ia.getArgument("numP");
		this.console = new Console(ia.getComponentIdentifier());
		this.marketUI = new MarketWindow(market, getManagers());
		gameEnded = false;
		refreshUI();
	}
	
	void refreshUI(){
		marketUI.draw();
		IExecutionFeature exe = ia.getComponentFeature(IExecutionFeature.class);
		exe.waitForDelay(500, new IComponentStep<Void>(){

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				if(!gameEnded){
					refreshUI();
				}
				return null;
			}
			
		});
	}

	List<Manager> getManagers() {
		return players.stream().filter(p -> p instanceof Manager).map(p -> (Manager) p).collect(Collectors.toList());
	}

	List<Investor> getInvestors() {
		return players.stream().filter(p -> p instanceof Investor).map(p -> (Investor) p).collect(Collectors.toList());
	}

	List<Manager> getInGameManagers() {
		return players.stream().filter(p -> p instanceof Manager && p.inGame).map(p -> (Manager) p)
				.collect(Collectors.toList());
	}

	List<Investor> getInGameInvestors() {
		return players.stream().filter(p -> p instanceof Investor && p.inGame).map(p -> (Investor) p)
				.collect(Collectors.toList());
	}
	/*
	 * void refreshUI() { marketUI.setManagers(getManagers()); }
	 */

	public Future<Void> syncGameInformation() {
		Future<Void> all = new Future<Void>();
		CounterResultListener<Void> allCounter = new CounterResultListener<>(players.size(),
				new DelegationResultListener<Void>(all));

		List<Player> players = new ArrayList<>(this.players);
		Collections.shuffle(players);

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

		List<Player> players = new ArrayList<>(this.players);
		Collections.shuffle(players);

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

	private void plotCompanyValue(Company company) {
		List<Company> tmp = new ArrayList<>();
		tmp.add(company);
		this.marketUI.storeCompanies(tmp);
		marketUI.setManagers(getManagers());
	}

	private Company auctionNewCompany(Company company) {
		Future<Boolean> ret = new Future<>();
		List<Manager> managersInAuction = getInGameManagers();
		List<Company> validOffers;
		plotCompanyValue(company);

		// ask each manager for an offer, if more than one valid
		Company bestOffer = null;
		do {
			validOffers = new ArrayList<>();
			for (int i = 0; i < managersInAuction.size(); i++) {
				Collections.shuffle(managersInAuction);

				IManagerService managerService = managersInAuction.get(i).getManagerService(ia);
				Company tBestOffer = bestOffer == null ? company : bestOffer;
				Company offer = managerService.informNewCompanyAuction(tBestOffer).get();
				if (offer == null || offer.currentOffer <= tBestOffer.currentOffer
						|| managersInAuction.get(i).balance <= offer.currentOffer) {
					managersInAuction.remove(i);
				} else {
					bestOffer = offer;

					IExecutionFeature exe = ia.getComponentFeature(IExecutionFeature.class);
					exe.waitForDelay(100).get();

					validOffers.add(bestOffer);
					plotCompanyValue(bestOffer);
				}
			}
		} while (validOffers.size() > 1);

		return bestOffer != null ? bestOffer : null;

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

		marketUI.storeState(gameState);

		
		if (gameState == GameState.NEGOTIATION || gameState == GameState.AUCTIONING_NEW_COMPANIES) {
			marketUI.setManagers(getManagers());
		}
		if (sync) {
			syncGameInformation().get();
			exe.waitForDelay(100).get();
		}
		announceGameStateChange(gameState).get();
	}

	void manageGame() {
		IExecutionFeature exe = ia.getComponentFeature(IExecutionFeature.class);

		this.marketUI.storePlayersBalance(getManagers(), getInvestors());
		this.marketUI.storeCompanies(getManagersCompanies());
		marketUI.setManagers(getManagers());

		exe.waitForDelay(500).get();

		for (int i = 0; i < 5; i++) {
			market.incRound();

			this.marketUI.storeCompanies(getManagersCompanies());
			this.marketUI.storePlayersBalance(getManagers(), getInvestors());

			changeTo(GameState.NEGOTIATION, true);
			exe.waitForDelay(6000).get();

			this.marketUI.storePlayersBalance(getManagers(), getInvestors());

			changeTo(GameState.EXCHANGING_INCOMES, false);
			
			updateMarket();
			applyInvestorsIncome();
			
			exe.waitForDelay(150).get();
			this.marketUI.storePlayersBalance(getManagers(), getInvestors());
			
			applyManagersIncome();
			
			exe.waitForDelay(150).get();
			this.marketUI.storePlayersBalance(getManagers(), getInvestors());
			
			applyManagmentCosts();
			
			exe.waitForDelay(150).get();
			marketUI.storePlayersBalance(getManagers(), getInvestors());

			changeTo(GameState.SOLVING_MANAGERS_DEBTS, true);

			solveManagersDebts();
			
			exe.waitForDelay(150).get();
			marketUI.storePlayersBalance(getManagers(), getInvestors());

			if (market.getRound() < 5) {
				changeTo(GameState.AUCTIONING_NEW_COMPANIES, true);
				auctionNewCompanies();
				exe.waitForDelay(500).get();
			}
			
			marketUI.storePlayersBalance(getManagers(), getInvestors());

		}
		
		gameEnded = true;

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
				marketUI.setManagers(getManagers());
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
				Player registeredPlayer = players.get(players.indexOf(company.currentInvestor));
				registeredPlayer.balance += market.calcCompanyRevenue(company);
			}
		}

	}

	private void applyManagersIncome() {
		List<Investor> investors = getInGameInvestors();
		for (int i = 0; i < investors.size(); i++) {
			Investor investor = investors.get(i);
			ArrayList<Company> companies = fetchInvestorCompanies(investor);
			int totalOffers = calcTotalOffers(companies);

			if (investor.balance < totalOffers) {
				int payment = totalOffers / companies.size();
				investor.inGame = false;

				for (Company company : companies) {
					Player registeredOwner = players.get(players.indexOf(company.owner));

					registeredOwner.balance += payment;
					company.currentOffer = 0;
					company.currentInvestor = null;
					company.closed = false;
				}

			} else {
				for (Company company : companies) {
					Player registeredOwner = players.get(players.indexOf(company.owner));

					registeredOwner.balance += company.currentOffer;
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
		for (Manager manager : getInGameManagers()) {
			manager.balance -= manager.companies.size() * WallStreetAgent.companyFee;
		}
	}

	private void solveManagersDebts() {
		for (Manager manager : getInGameManagers()) {
			if (manager.balance < 0) {
				IManagerService managerService = (IManagerService) SServiceProvider
						.getService(ia, manager.getComponentIdentifier(), IManagerService.class).get();
				List<Company> companies = managerService.consultWhatCompaniesToSell().get();
				manager.companies.removeAll(companies);
				deck.addCompanies(companies);
				manager.balance += valueOfCompany * companies.size();
				if (manager.balance < 0) {
					manager.inGame = false;
				}
			}
		}
	}

	private void updateMarket() {
		market.rollTheDices();
	}

	private void informOtherPlayersAboutOffer(Company company, boolean includeOwner) {

		List<Player> players = new ArrayList<>(this.players);
		Collections.shuffle(players);

		for (Player player : players) {
			if ( (!player.equals(company.owner) && !player.equals(company.currentInvestor))
					|| (player.equals(company.owner) && includeOwner)) {

				IPlayerService playerService = SServiceProvider
						.getService(ia, player.getComponentIdentifier(), IPlayerService.class).get();
				playerService.informOfferUpdate(company);
			}
		}
	}

	@Override
	public IFuture<Boolean> informOffer(Company offer) {
		Future<Boolean> future = new Future<>();
		Player investor = players.get(players.indexOf(offer.currentInvestor));
		Manager manager = (Manager) players.get(players.indexOf(offer.owner));
		Company company = manager.companies.get(manager.getCompanies().indexOf(offer));

		if (manager.inGame && investor.inGame && gameState.equals(GameState.NEGOTIATION) && !company.closed) {

			manager.getCompanies().set(manager.getCompanies().indexOf(offer), offer);
			marketUI.storeCompanies(getManagersCompanies());

			informOtherPlayersAboutOffer(company, false);
			future.setResult(true);

		} else {
			future.setResult(false);
		}

		return future;
	}

	@Override
	public IFuture<Boolean> informOfferCancellation(Company offerCancellation) {
		console.log("x1");
		
		Future<Boolean> future = new Future<>();
		Manager manager = (Manager) players.get(players.indexOf(offerCancellation.owner));
		Company company = manager.companies.get(manager.getCompanies().indexOf(offerCancellation));
		
		if (manager.inGame && offerCancellation.currentOffer == 0 && offerCancellation.currentInvestor == null && gameState.equals(GameState.NEGOTIATION) && !company.closed) {

			manager.getCompanies().set(manager.getCompanies().indexOf(offerCancellation), offerCancellation);
			marketUI.storeCompanies(getManagersCompanies());

			informOtherPlayersAboutOffer(company, true);
			future.setResult(true);

		} else {
			future.setResult(false);
		}

		return future;
	}

}
