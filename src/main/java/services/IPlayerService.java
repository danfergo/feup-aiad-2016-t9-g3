package services;

import java.util.List;
import java.util.Map;

import agents.WallStreetAgent;
import classes.Company;
import classes.Market;
import classes.Player;
import jadex.commons.future.IFuture;

public interface IPlayerService {
	IFuture<Void> syncGameInformation(Player self, List<Player> otherPlayers, Market market);
	IFuture<Void> updateGameState(WallStreetAgent.GameState gameState);
    IFuture<Void> informConfirmedOffer(Company company);

}
