package agents;


import jadex.bridge.service.types.clock.IClockService;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.SubscriptionIntermediateFuture;
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
import agents.services.IWallStreetService;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bdiv3.runtime.ChangeEvent;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Agent
@Service
@ProvidedServices({
    @ProvidedService(type=IWallStreetService.class)
})
@RequiredServices(@RequiredService(name="clockservice", type=IClockService.class, binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM)))

public class WallStreetBDI implements IWallStreetService{
	public static enum Player{
		INVESTOR, MANAGER
	}
	
	
    @Agent
    protected IInternalAccess agentAccess;
	
	@AgentFeature 
	protected IBDIAgentFeature bdiFeature;

	@AgentCreated
	public void init(){
	}
	
	@AgentBody
	public void executeBody(){
		bdiFeature.adoptPlan("introducePlayersEachOther");
	}
	
	@Belief
    protected List<IComponentIdentifier> players = new ArrayList<>();
	
	@Belief(dynamic=true)
	protected int playersSize = players.size();
	
	
	@Plan(trigger=@Trigger(factchangeds="playersSize"))
	public void introducePlayersEachOther(ChangeEvent<Object> event)
	{
//		ChangeEvent<Integer> change = ((ChangeEvent<Integer>)event.getValue());

		System.out.println("> " + event);
	    System.out.println(">" + players.size());
	}
	
	protected Map<IComponentIdentifier, SubscriptionIntermediateFuture<String>> investors;
	protected Map<IComponentIdentifier, SubscriptionIntermediateFuture<String>> managers;

    

	@Override
	
	public IFuture<Boolean> join(IComponentIdentifier investor, Player playingAs) {
		Future <Boolean> ret = new Future<>(true);
		players.add(investor);
		

		
		return ret;
	}



}
