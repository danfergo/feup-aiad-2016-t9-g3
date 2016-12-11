package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import agents.PlayerBDI.Connect;
import agents.WallStreetAgent.PlayingMode;
import classes.Market;
import classes.Player;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Capability;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.GoalParameter;
import jadex.bdiv3.annotation.GoalResult;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentFeature;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import services.IManagerService;
import services.IPlayerService;
import services.IWallStreetService;
import util.Console;
import jadex.bdiv3.annotation.Trigger;

@Agent
@Service
@ProvidedServices({ @ProvidedService(type = IPlayerService.class) })
public abstract class PlayerBDI implements IPlayerService {

	@Agent
	protected IInternalAccess ia;

	@AgentFeature
	protected IBDIAgentFeature bdiFeature;

	protected Console console;

	private PlayingMode playingAs;

	public PlayingMode getPlayingAs() {
		return playingAs;
	}

	protected static Random randomGenerator = new Random();

	public PlayerBDI(PlayingMode playingAs) {
		this.playingAs = playingAs;
		this.console = new Console(ia.getComponentIdentifier());
	}

	@Goal
	public class Connect {

		IWallStreetService wallStreet;
		Player player;

	}

	@Plan(trigger = @Trigger(goals = Connect.class))
	public void connectToWallStreet(Connect goal) {

	}

	@Belief
	IWallStreetService wallStreet;

	@Belief
	protected Player self;

	@Belief
	protected WallStreetAgent.GameState gameState = WallStreetAgent.GameState.ESTABLISHING_GAME;

	@Belief
	protected List<Player> otherPlayers = new ArrayList<>();

	private Market market;

	@AgentBody
	public void body() {
		//Connect connect = (Connect) bdiFeature.dispatchTopLevelGoal(new Connect()).get();
		// this.wallStreet = connect.wallStreet;
		// this.self = connect.player;
		
		
		IWallStreetService wallStreet = (IWallStreetService) SServiceProvider
				.getService(ia.getExternalAccess(), IWallStreetService.class, RequiredServiceInfo.SCOPE_PLATFORM).get();
		Player player = (Player) wallStreet.join(ia.getComponentIdentifier(), playingAs).get();
		if (player != null) {
			this.wallStreet = wallStreet;
			this.self = player;
			return;
		}

		// TODO throw exception

	}

	/**
	 * IPlayerService implementation BEGIN
	 */
	@Override
	public IFuture<Void> syncGameInformation(Player self, List<Player> otherPlayers, Market market) {
		this.self = self;
		this.otherPlayers = otherPlayers;
		this.market = market;
		//console.log("knows (" + otherPlayers.size() + ") other players.");
		return Future.DONE;
	}

	@Override
	public IFuture<Void> updateGameState(WallStreetAgent.GameState gameState) {
		this.gameState = gameState;
		//console.log("ready to " + gameState);
		return Future.DONE;
	}

}
