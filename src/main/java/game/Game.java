package game;

import jadex.base.PlatformConfiguration;
import jadex.base.Starter;
import agents.InvestorAgent;
import agents.ManagerAgent;
import agents.WallStreetBDI;

public class Game {
	public static void main(String[] args) {
        PlatformConfiguration   config  = PlatformConfiguration.getDefaultNoGui();
		//PlatformConfiguration config  = PlatformConfiguration.getDefault();
        //config.setClock(true);
        
        // Add WallStreet
        config.addComponent("agents.WallStreetBDI.class");
        
        // Add Investors and Managers
        for(int i = 0; i < 4; i++){
            config.addComponent(InvestorAgent.class);
            config.addComponent(ManagerAgent.class);
        }
        
        
        // Launch Jadex
        Starter.createPlatform(config).get();
	}
}
