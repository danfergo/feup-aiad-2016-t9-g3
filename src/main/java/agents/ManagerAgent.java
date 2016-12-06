package agents;

import agents.WallStreetBDI.Player;
//import agents.WallStreetAgent.Player;
import agents.services.IInvestorService;
import agents.services.IManagerService;
import agents.services.IWallStreetService;
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
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;

@Agent
@Service
@ProvidedServices({ @ProvidedService(type = IManagerService.class) })
public class ManagerAgent implements IManagerService {
	@Agent
	protected IInternalAccess agent;

	@ServiceIdentifier
	protected IServiceIdentifier serviceIdentifier;

	@AgentBody
	public void body() {
/*		SServiceProvider
				.getServices(agent.getExternalAccess(), IWallStreetService.class, RequiredServiceInfo.SCOPE_PLATFORM)
				.addResultListener(new IntermediateDefaultResultListener<IWallStreetService>() {
					public void intermediateResultAvailable(IWallStreetService wallStreet) {

					ISubscriptionIntermediateFuture<String> subscription = wallStreet
								.join(agent.getComponentIdentifier(), Player.MANAGER);
				
				});*/

	}
}
