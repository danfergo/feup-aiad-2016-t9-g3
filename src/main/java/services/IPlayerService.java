package services;

import java.util.List;
import java.util.Map;

import agents.WallStreetAgent;
import classes.Player;

public interface IPlayerService {
	void introduceToOtherPlayers(List<Player> otherPlayers);
	void updateGameState(WallStreetAgent.GameState gameState);
}
