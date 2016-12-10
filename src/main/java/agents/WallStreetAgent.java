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
import services.IPlayerService;
import services.IWallStreetService;
import util.ArgumentableResultListener;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
		ESTABLISHING_GAME, NEGOTIATION, EXCHANGING_INCOMES, AUCTIONING_NEW_COMPANIES, GAME_ENDED
	}

	private static int numberOfPlayers = 11;

	@Agent
	protected IInternalAccess ia;

	@Belief
	protected List<Player> players = new ArrayList<>();

	@Belief
	WallStreetAgent.GameState gameState = WallStreetAgent.GameState.ESTABLISHING_GAME;

	protected Market market = new Market();
	protected Deck companiesDeck = new Deck();

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
				System.out.println("(" + ia.getComponentIdentifier().getLocalName() + ") ready to "
						+ ((WallStreetAgent) args[0]).gameState);
			}
		}));
		return ret;
	}

	@Override
	public IFuture<Player> join(IComponentIdentifier componentIdentifier, PlayingMode playingAs) {
		Future<Player> ret = new Future<>();

		if (players.size() < numberOfPlayers) {

			Player player = playingAs.equals(PlayingMode.INVESTOR) ? new Investor(componentIdentifier)
					: new Manager(componentIdentifier, companiesDeck.fetchCompanies(2));
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

	void manageGame() {
		IExecutionFeature exe = ia.getComponentFeature(IExecutionFeature.class);

		syncGameInformation().get();
		announceGameStateChange(GameState.NEGOTIATION).get();

		exe.waitForDelay(5000).get();
		
		announceGameStateChange(GameState.EXCHANGING_INCOMES).get();
		
		updateMarket();
		applyInvestorsIncome();
		applyManagersIncome();
		applyManagmentCosts();
		
		syncGameInformation().get();
		
		//solveInvestorsDebts();
		//solveManagersDebts();
		
		//syncGameInformation().get();
		
		//announceGameStateChange(GameState.AUCTIONING_NEW_COMPANIES).get();

		
		
		//announceGameStateChange(GameState.NEGOTIATION).get();

		return;/*
				 * exe.repeatStep(5000-System.currentTimeMillis()%5000, 5000,
				 * new IComponentStep<Void>() {
				 * 
				 * @Override public IFuture<Void> execute(IInternalAccess ia) {
				 * 
				 * return Future.DONE;
				 * 
				 * } });
				 */
	}

	private ArrayList<Company> getManagersCompanies() {
		ArrayList<Company> companies = new ArrayList<>();
		for (Player player : players) {
			if (player instanceof Manager) {
				companies.addAll(((Manager)player).getCompanies());
			}
		}
		return companies;
	}

	private void applyInvestorsIncome() {
		for(Company company : getManagersCompanies()){
			if(company.currentInvestor != null){
				company.currentInvestor.balance += market.calcCompanyValue(company);
			}
		}

	}
	
	private void applyManagersIncome(){
		for(Player player : players){
			if(player instanceof Investor){
				Investor investor = (Investor)player;
				ArrayList<Company> companies = fetchInvestorCompanies(investor);
				int totalOffers = calcTotalOffers(companies);
				if(investor.balance < totalOffers){
					int payment = totalOffers/companies.size();

					for(Company company : companies){
						company.owner.balance = 
						company.currentOffer = 0;
						company.currentInvestor = null;
					}
					players.remove(player);
				}else{
					
				}
			}
		}
	}

	private int calcTotalOffers(ArrayList<Company> companies) {
		int totalOffers = 0;
		for(Company company : companies){
			totalOffers += company.currentOffer;
		}
		return totalOffers;
	}

	private ArrayList<Company> fetchInvestorCompanies(Investor investor) {
		ArrayList<Company> allCompanies= getManagersCompanies();
		ArrayList<Company> investorCompanies = new ArrayList<>();
		for(Company company : allCompanies){
			if(investor.equals(company.currentInvestor)){
				investorCompanies.add(company);
			}
		}
		return investorCompanies;
	}

	private void applyManagmentCosts(){
		for(Company company : getManagersCompanies()){
			if(company.currentInvestor != null){

			}
		}
	}
	
	
	
	private void solveInvestorsDebts(){
		for(int i = 0; i < players.size(); i++){
			Player player = players.get(i);
			if(player instanceof Investor && player.balance < 0){
				players.remove(i);
			}
		}
	}
	
	private void solveManagersDebts(){ //TODO fix this!
		for(int i = 0; i < players.size(); i++){
			Player player = players.get(i);
			if(player instanceof Manager && player.balance < 0){
				players.remove(i);
			}
		}
	}
	
	
	private void updateMarket() {
		market.rollTheDices();
	}

}
