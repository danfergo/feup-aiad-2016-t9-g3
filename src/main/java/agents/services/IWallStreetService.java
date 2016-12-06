package agents.services;

import agents.WallStreetBDI.Player;
//import agents.WallStreetAgent.Player;
import jadex.bridge.IComponentIdentifier;
import jadex.commons.future.IFuture;
import jadex.commons.future.ISubscriptionIntermediateFuture;

public interface IWallStreetService {
    public IFuture<Boolean> join(IComponentIdentifier agent, Player playingAs);    
    
}
