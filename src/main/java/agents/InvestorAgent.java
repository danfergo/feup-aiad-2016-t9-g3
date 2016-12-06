package agents;

import agents.services.IWallStreetService;
import agents.WallStreetBDI.Player;
//import agents.WallStreetAgent.Player;
import agents.services.IInvestorService;
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

@Agent
@Service
@ProvidedServices({ @ProvidedService(type = IInvestorService.class) })
public class InvestorAgent implements IInvestorService {

	@Agent
	protected IInternalAccess agent;

	@ServiceIdentifier
	protected IServiceIdentifier serviceIdentifier;

	@AgentBody
	public void body() {
		System.out.println("here.");

		SServiceProvider
				.getServices(agent.getExternalAccess(), IWallStreetService.class, RequiredServiceInfo.SCOPE_PLATFORM)
				.addResultListener(new IntermediateDefaultResultListener<IWallStreetService>() {
					public void intermediateResultAvailable(IWallStreetService wallStreet) {

						wallStreet.join(agent.getComponentIdentifier(), Player.INVESTOR)
						.addResultListener(v -> {
								System.out.println("xxx");
						});
					}
				});

	}

}
