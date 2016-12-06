package agents;

import java.util.ArrayList;

import agents.PlayerBDI.Connect;
import agents.WallStreetAgent.Player;
import agents.services.IManagerService;
import agents.services.IPlayerService;
import agents.services.IWallStreetService;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Capability;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.GoalParameter;
import jadex.bdiv3.annotation.GoalResult;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.features.IBDIAgentFeature;
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
import jadex.bdiv3.annotation.Trigger;


@Agent
@Service
@ProvidedServices({ @ProvidedService(type = IPlayerService.class) })
public class PlayerBDI implements IPlayerService {
	
	@Agent
	protected IInternalAccess agent;
	
	@AgentFeature
	protected IBDIAgentFeature bdiFeature;

	
	//@Belief
	//public native IWallStreetService getWallStreet();

	//@Belief
	//public native void setWallStreet(IWallStreetService wallStreet);
	
	
	@Goal
	public class Connect {
		
		@GoalResult
		IWallStreetService wallStreet;
		
	}
	
	private Player playingAs;

	public Player getPlayingAs() {
		return playingAs;
	}


	
	public PlayerBDI(Player playingAs){
		this.playingAs = playingAs;
	}

	
	
	@Plan(trigger=@Trigger(goals=Connect.class))
	public IWallStreetService connectToWallStreet(){
		ArrayList<IWallStreetService> wallStreets = (ArrayList<IWallStreetService>)SServiceProvider.getServices(agent.getExternalAccess(), IWallStreetService.class, RequiredServiceInfo.SCOPE_PLATFORM).get();
		for(IWallStreetService wallStreet : wallStreets){
			Boolean joinedSuccessfully = (Boolean)wallStreet.join(agent.getComponentIdentifier(), Player.MANAGER).get();
			if(joinedSuccessfully){
				return wallStreet;
			}
		}
		 
		//TODO throw exception
		return null;
	}

	@AgentBody
	public void body() {
		bdiFeature.dispatchTopLevelGoal(new Connect());
	}
	
	
	
	
	
	
	
	/**
	 * IPlayerService implementation  BEGIN
	 */
	
	
	@Override
	public void introduceToOtherPlayers() {
		System.out.println(" hello ");
	}




}
