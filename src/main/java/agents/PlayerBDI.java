package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import agents.PlayerBDI.Connect;
import agents.WallStreetAgent.PlayingMode;
import classes.Player;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Capability;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.GoalParameter;
import jadex.bdiv3.annotation.GoalResult;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentFeature;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import services.IManagerService;
import services.IPlayerService;
import services.IWallStreetService;
import jadex.bdiv3.annotation.Trigger;


@Agent
@Service
@ProvidedServices({ @ProvidedService(type = IPlayerService.class) })
public class PlayerBDI implements IPlayerService {
	
	@Agent
	protected IInternalAccess agent;
	
	@AgentFeature
	protected IBDIAgentFeature bdiFeature;

	
	@Goal
	public class Connect {
		
		IWallStreetService wallStreet;
		Player player;

	}
	
	private PlayingMode playingAs;

	public PlayingMode getPlayingAs() {
		return playingAs;
	}


	protected static Random randomGenerator = new Random();
	
	public PlayerBDI(PlayingMode playingAs){
		this.playingAs = playingAs;
	}

	
	
	@Plan(trigger=@Trigger(goals=Connect.class))
	public void connectToWallStreet(Connect goal){
		ArrayList<IWallStreetService> wallStreets = (ArrayList<IWallStreetService>)SServiceProvider.getServices(agent.getExternalAccess(), IWallStreetService.class, RequiredServiceInfo.SCOPE_PLATFORM).get();
		for(IWallStreetService wallStreet : wallStreets){
			Player player = (Player)wallStreet.join(agent.getComponentIdentifier(), playingAs).get();
			if(player != null){
				goal.wallStreet = wallStreet;
				goal.player = player;
			}
		}
		 
		//TODO throw exception
	}

	
	@Belief
	IWallStreetService wallStreet;
	
	@Belief
	protected Player self;
	
	@Belief
	protected WallStreetAgent.GameState gameState = WallStreetAgent.GameState.ESTABLISHING_GAME;
	
	@Belief
    protected List<Player> otherPlayers = new ArrayList<>();
	
	
	
	
	@AgentBody
	public void body() {
		Connect connect = (Connect)bdiFeature.dispatchTopLevelGoal(new Connect()).get();
		this.wallStreet = connect.wallStreet;
		this.self = connect.player;
	}
	
	
	
	
	
	
	
	/**
	 * IPlayerService implementation  BEGIN
	 */
	@Override
	public void introduceToOtherPlayers(List<Player> otherPlayers) {
		this.otherPlayers = otherPlayers;
		System.out.println("Other (" + otherPlayers.size()+ ") players known.");
	
	}



	@Override
	public void updateGameState(WallStreetAgent.GameState gameState) {
		this.gameState = gameState;
	}




}
