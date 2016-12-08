package agents;

import jadex.bridge.service.types.clock.IClockService;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.SubscriptionIntermediateFuture;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentCreated;
import jadex.micro.annotation.AgentFeature;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;
import jadex.rules.eca.ChangeInfo;
import services.IPlayerService;
import services.IWallStreetService;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bdiv3.runtime.ChangeEvent;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.search.SServiceProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import classes.Deck;
import classes.Investor;
import classes.Manager;
import classes.Market;
import classes.Player;

@Agent
@Service
@ProvidedServices({ @ProvidedService(type = IWallStreetService.class) })
@RequiredServices(@RequiredService(name = "clockservice", type = IClockService.class, binding = @Binding(scope = RequiredServiceInfo.SCOPE_PLATFORM)))

public class WallStreetAgent implements IWallStreetService {
	public static enum PlayingMode {
		INVESTOR, MANAGER
	}

	public static enum GameState {
		ESTABLISHING_GAME, TRADING, 
	}

	private static int numberOfPlayers = 11;

	@Agent
	protected IInternalAccess agentAccess;

	@Belief
	protected List<Player> players = new ArrayList<>();

	@Belief
	WallStreetAgent.GameState gameState = WallStreetAgent.GameState.ESTABLISHING_GAME;

	protected Market market = new Market();
	protected Deck companiesDeck = new Deck(market);


	public void introducePlayersToEachOther() {
		for (Player player : players) {
			ArrayList<Player> otherPlayers = new ArrayList<>(players);
			otherPlayers.remove(otherPlayers.indexOf(player));
			
			IPlayerService playerService = SServiceProvider
					.getService(agentAccess, player.getComponentIdentifier(), IPlayerService.class).get();
			playerService.introduceToOtherPlayers(otherPlayers);

		}
	}

	public void announceRoundBegin() {
		gameState = GameState.TRADING;
		for (Player player : players) {
			IPlayerService playerService = SServiceProvider
					.getService(agentAccess, player.getComponentIdentifier(), IPlayerService.class).get();
			playerService.updateGameState(GameState.TRADING);
		}
	}

	@Override
	public IFuture<Player> join(IComponentIdentifier componentIdentifier, PlayingMode playingAs) {
		Future<Player> ret = new Future<>();

		if (players.size() < numberOfPlayers) {
			Player player = playingAs.equals(PlayingMode.INVESTOR) 
					? new Investor(componentIdentifier)
					: new Manager(componentIdentifier, companiesDeck.fetchCompanies(2));
			players.add(player);
			ret.setResult(player);

			if (players.size() == numberOfPlayers) {
				introducePlayersToEachOther();
				announceRoundBegin();
			}
		} else {
			ret.setResult(null);
		}

		return ret;
	}

}
