package services;

import agents.WallStreetAgent.PlayingMode;
import classes.Company;
import classes.Manager;
import classes.Player;
//import agents.WallStreetAgent.Player;
import jadex.bridge.IComponentIdentifier;
import jadex.commons.future.IFuture;
import jadex.commons.future.ISubscriptionIntermediateFuture;

public interface IWallStreetService {
    public IFuture<Player> join(IComponentIdentifier agent, PlayingMode playingAs);    
    public IFuture<Boolean> informOffer(Company company);
    public IFuture<Boolean> informOfferCancellation(Company company);
    
}
