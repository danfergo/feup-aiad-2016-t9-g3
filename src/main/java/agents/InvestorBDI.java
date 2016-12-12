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
import jadex.bdiv3.annotation.GoalContextCondition;
import jadex.bdiv3.annotation.GoalCreationCondition;
import jadex.bdiv3.annotation.GoalMaintainCondition;
import jadex.bdiv3.annotation.GoalParameter;
import jadex.bdiv3.annotation.GoalRecurCondition;
import jadex.bdiv3.annotation.GoalTargetCondition;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.model.MProcessableElement.ExcludeMode;
import jadex.bdiv3.annotation.Trigger;
import jadex.bridge.IInternalAccess;
import jadex.bridge.component.IExecutionFeature;
import jadex.bridge.service.IServiceIdentifier;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.annotation.ServiceIdentifier;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.IFuture;
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
public abstract class InvestorBDI extends PlayerBDI implements IInvestorService {

	public InvestorBDI() {
		super(PlayingMode.INVESTOR);
	}

	@Belief(dynamic = true)
	WallStreetAgent.GameState anotherGameState = gameState;

	@Belief
	int companiesInvestOn = 0;

	private static List<Manager> filterManagers(List<Player> players) {
		List<Manager> investors = new ArrayList<>();
		for (Player p : players) {
			if (p instanceof Manager) {
				investors.add((Manager) p);
			}
		}
		return investors;
	}

	@Belief(dynamic = true)
	List<Manager> managers = filterManagers(otherPlayers);
}
