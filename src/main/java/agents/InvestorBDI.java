package agents;

import java.util.ArrayList;
import java.util.List;

import agents.WallStreetAgent.PlayingMode;
import classes.Company;
import classes.Investor;
import classes.Manager;
import classes.Player;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.GoalCreationCondition;
import jadex.bdiv3.annotation.GoalMaintainCondition;
import jadex.bdiv3.annotation.GoalParameter;
import jadex.bdiv3.annotation.GoalRecurCondition;
import jadex.bdiv3.annotation.GoalTargetCondition;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.model.MProcessableElement.ExcludeMode;
import jadex.bdiv3.annotation.Trigger;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.IServiceIdentifier;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.annotation.ServiceIdentifier;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import services.IInvestorService;
import services.IManagerService;
import services.IWallStreetService;

@Agent
@Service
@ProvidedServices({ @ProvidedService(type = IInvestorService.class) })
public class InvestorBDI extends PlayerBDI implements IInvestorService {

	public InvestorBDI() {
		super(PlayingMode.INVESTOR);
	}

	@Belief(dynamic=true)
	WallStreetAgent.GameState anotherGameState = gameState;
	
	@Belief
	int companiesInvestOn = 0;
	
	
	private static List<Manager> filterManagers(List<Player> players){
		List<Manager> investors = new ArrayList<>();
		for(Player p : players){
			if(p instanceof Manager){
				investors.add((Manager)p);
			}
		}
		return investors;
	}
	
	@Belief(dynamic=true)
	List<Manager> managers = filterManagers(otherPlayers);

	@Goal(excludemode=ExcludeMode.Never)
	public static class InvestOnCompanies {

		public InvestorBDI investor;

		
		InvestOnCompanies(InvestorBDI investor){
			this.investor = investor;
		}

		@GoalMaintainCondition(beliefs = "gameState")
		protected boolean mantain(){
			return !investor.gameState.equals(WallStreetAgent.GameState.NEGOTIATION);
		}
		
		@GoalTargetCondition(beliefs = "companiesInvestOn")
		protected boolean target() {
			return investor.companiesInvestOn == 2;
		}

		@GoalCreationCondition(beliefs={"gameState"})
		public static InvestOnCompanies launch(InvestorBDI  investor)
		{
			return investor.gameState.equals(WallStreetAgent.GameState.NEGOTIATION) ? new InvestOnCompanies(investor) : null;	
		}
		
	}

	

	
	
	@Plan(trigger=@Trigger(goals=InvestOnCompanies.class)) 
	protected void invest(){
		System.out.println("++;" + gameState.equals(WallStreetAgent.GameState.NEGOTIATION));
		
		Manager manager = managers.get(randomGenerator.nextInt(managers.size()));
		Company company = manager.getCompanies().get(randomGenerator.nextInt(manager.getCompanies().size()));
		IManagerService managerService = (IManagerService)SServiceProvider.getService(agent, manager.getComponentIdentifier(), IManagerService.class).get();
		Boolean sucessfullInvestment = managerService.investOn((Investor)self, company, 100, false).get();
		if(sucessfullInvestment.equals(Boolean.TRUE)){
			companiesInvestOn++;
		}
		System.out.println("companies: " + companiesInvestOn);
	}
	
	
	
	@AgentBody
	public void body() {
		super.body();
		//bdiFeature.dispatchTopLevelGoal(new InvestOnCompanies());

	}
}
