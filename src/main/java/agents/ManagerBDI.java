package agents;

import java.util.ArrayList;
import java.util.List;

import agents.PlayerBDI.Connect;
import agents.WallStreetAgent.PlayingMode;
import classes.Company;
import classes.Investor;
import classes.Manager;
import classes.Market;
import classes.Player;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Capability;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.GoalMaintainCondition;
import jadex.bdiv3.annotation.GoalTargetCondition;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bdiv3.model.MProcessableElement.ExcludeMode;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.IServiceIdentifier;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.annotation.ServiceIdentifier;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.IntermediateDefaultResultListener;
import jadex.commons.future.SubscriptionIntermediateFuture;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentFeature;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import services.IInvestorService;
import services.IManagerService;
import services.IWallStreetService;
import jadex.bdiv3.annotation.Mapping;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Trigger;

@Agent
@Service
@ProvidedServices({ @ProvidedService(type = IManagerService.class) })
public class ManagerBDI extends PlayerBDI implements IManagerService {
	public ManagerBDI() {
		super(PlayingMode.MANAGER);
	}

	@Belief(dynamic = true)
	List<Company> myCompanies = self == null ? new ArrayList<>() : ((Manager) self).getCompanies();

	/*
	 * @Belief int announcedCompanies = 0;
	 * 
	 * 
	 * @Goal(excludemode=ExcludeMode.Never) public class AnnounceCompanies {
	 * 
	 * @GoalMaintainCondition(beliefs = "gameState") protected boolean
	 * maintain() { return gameState.equals(WallStreetAgent.GameState.TRADING);
	 * }
	 * 
	 * @GoalTargetCondition(beliefs = {"announcedCompanies", "myCompanies"})
	 * protected boolean target() { System.out.println( myCompanies == null ?
	 * ". " : myCompanies);
	 * 
	 * return myCompanies.size() == announcedCompanies; }
	 * 
	 * }
	 * 
	 * @Plan(trigger=@Trigger(goals=AnnounceCompanies.class)) protected void
	 * announceCompany(){ System.out.println("announce company! @ " +
	 * self.getComponentIdentifier().getName()); for(Player player:
	 * otherPlayers){ if(player instanceof Investor){ IInvestorService
	 * investorService = SServiceProvider.getService(agent,
	 * player.getComponentIdentifier(), IInvestorService.class).get(); } }
	 * announcedCompanies++; }
	 */

	@AgentBody
	public void body() {
		super.body();
		// bdiFeature.dispatchTopLevelGoal(new AnnounceCompanies());

	}

	@Override
	public IFuture<Boolean> investOn(Investor investor, Company company, int offer, boolean close) {
		Future<Boolean> future = new Future<>();
		int index = myCompanies.indexOf(company);

		if (index != -1 && offer > myCompanies.get(index).currentOffer) {
			myCompanies.get(index).currentInvestor = investor;
			myCompanies.get(index).currentOffer = offer;
			console.log("invests on company " + company.id + " of " + self.getComponentIdentifier().getLocalName() + " "
					+ offer);
			wallStreet.informOffer((Manager) self, myCompanies.get(index));
			future.setResult(true);
		} else {
			future.setResult(false);
		}
		return future;
	}

	@Override
	public IFuture<Boolean> closeDeal(Company company) {
		Future<Boolean> future = new Future<>();
		return future;
	}

	@Override
	public IFuture<List<Company>> consultWhatCompaniesToSell() {
		Future<List<Company>> future = new Future<List<Company>>();
		List<Company> companiesToSell = new ArrayList<>();
		int nCompaniesToSell = Math.min(((Manager) self).companies.size(), Market.numberOfCompaniesRequiredToSell(self.balance));
		System.out.println("N companies:" + ((Manager) self).companies.size());
		for (int i = 0; i < nCompaniesToSell; i++) {
			companiesToSell.add(((Manager) self).companies.get(i));
		}
		future.setResult(companiesToSell);
		return future;
	}

	@Override
	public IFuture<Company> informNewCompanyAuction(Company company) {
		Future<Company> future = new Future<Company>();
		float rnd = randomGenerator.nextInt(10);
		if (rnd < 8) {
			company.owner = (Manager) self;
			company.currentOffer += 30;
			future.setResult(company);
		} else {
			future.setResult(null);
		}
		return future;
	}

}
