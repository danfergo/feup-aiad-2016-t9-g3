package game;

import jadex.base.PlatformConfiguration;
import jadex.base.Starter;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.future.IFuture;
import jadex.commons.future.ITuple2Future;
import ui.StartScreen;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Game {
	public static void main(String[] args) throws InterruptedException {
		
		StartScreen s = new StartScreen();
		s.draw();
		while(!s.isDone()){
			Thread.sleep(200);
		}
		
		HashMap<String,Integer> managers = s.getManagers();
		HashMap<String,Integer> investors = s.getInvestors();
		
		int numplayers = s.getNump();
		
		
		
		
        PlatformConfiguration   config  = PlatformConfiguration.getDefaultNoGui();
		//PlatformConfiguration config  = PlatformConfiguration.getDefault();
        //config.setClock(true);
        
        
        IExternalAccess platform = Starter.createPlatform(config).get();
		
		IFuture<IComponentManagementService> fut = SServiceProvider.getService(platform, IComponentManagementService.class);
		IComponentManagementService cms = fut.get();
		
		
		// Add WallStreet
		Map<String, Object> agentArgs = new HashMap<String, Object>();
	    agentArgs.put("numP", numplayers);
	    CreationInfo cInfo = new CreationInfo(agentArgs);
	    
		cms.createComponent("WallStreet", "agents.WallStreetAgent.class", cInfo).getFirstResult();
		
		// Add Managers
		for (Entry<String, Integer> manager : managers.entrySet()) {
			Map<String, Object> agentArgs1 = new HashMap<String, Object>();
			agentArgs1.put("type", manager.getValue());
		    CreationInfo cInfo1 = new CreationInfo(agentArgs1);
			cms.createComponent(manager.getKey(), "agents.ManagerBDI.class", cInfo1).getFirstResult();
			
		}
		// Add Investors
		for (Entry<String, Integer> investor : investors.entrySet()) {
			Map<String, Object> agentArgs1 = new HashMap<String, Object>();
			agentArgs1.put("type", investor.getValue());
		    CreationInfo cInfo1 = new CreationInfo(agentArgs1);
			cms.createComponent(investor.getKey(), "agents.InvestorBDI.class", cInfo1).getFirstResult();
			
		}
		
		
		
		
        
        
	}
}
