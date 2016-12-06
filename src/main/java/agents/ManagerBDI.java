package agents;

import agents.PlayerBDI.Connect;
import agents.WallStreetAgent.Player;
import agents.services.IInvestorService;
import agents.services.IManagerService;
import agents.services.IWallStreetService;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Capability;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.IServiceIdentifier;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.annotation.ServiceIdentifier;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentFeature;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import jadex.bdiv3.annotation.Mapping;

@Agent
@Service
@ProvidedServices({ @ProvidedService(type = IManagerService.class) })
public class ManagerBDI extends PlayerBDI implements IManagerService {
	public ManagerBDI() {
		super(Player.INVESTOR);
	}

//	@ServiceIdentifier
//	protected IServiceIdentifier serviceIdentifier;

//	@AgentFeature
//	protected IBDIAgentFeature bdiFeature;

//	@Belief
//	IWallStreetService wallStreet;
	
//	@Capability(beliefmapping=@Mapping(value="wallStreet"))
//	protected PlayerBDI capability = new PlayerBDI(Player.MANAGER);
	

}
