package game;

import jadex.base.PlatformConfiguration;
import jadex.base.Starter;
import agents.InvestorBDI;
import agents.ManagerBDI;
import agents.WallStreetAgent;

public class Game {
	public static void main(String[] args) {
        PlatformConfiguration   config  = PlatformConfiguration.getDefaultNoGui();
		//PlatformConfiguration config  = PlatformConfiguration.getDefault();
        //config.setClock(true);
        
        // Add WallStreet
        config.addComponent("agents.WallStreetAgent.class");
        
        // Add Investors and Managers
        for(int i = 0; i < 4; i++){
            config.addComponent("agents.InvestorBDI.class");
            config.addComponent("agents.ManagerBDI.class");
        }
        
        
        // Launch Jadex
        Starter.createPlatform(config).get();
	}
}
