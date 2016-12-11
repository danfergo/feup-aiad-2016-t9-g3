package game;

import jadex.base.PlatformConfiguration;
import jadex.base.Starter;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.search.SServiceProvider;
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
		cms.createComponent("WallStreet", "agents.WallStreetAgent.class", null).getFirstResult();
		// Add Managers
		for (Entry<String, Integer> manager : managers.entrySet()) {		
			cms.createComponent(manager.getKey(), "agents.ManagerBDI.class", null).getFirstResult();
			
		}
		// Add Investors
		for (Entry<String, Integer> investor : investors.entrySet()) {
			
			cms.createComponent(investor.getKey(), "agents.InvestorBDI.class", null).getFirstResult();
			
		}
		
		
		
		
        
        
	}
}
