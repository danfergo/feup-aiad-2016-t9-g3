package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import agents.DummyInvestorBDI.InvestOnCompanies;
import classes.Company;
import classes.Investor;
import classes.Manager;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.GoalContextCondition;
import jadex.bdiv3.annotation.GoalCreationCondition;
import jadex.bdiv3.annotation.GoalMaintainCondition;
import jadex.bdiv3.annotation.GoalTargetCondition;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.model.MProcessableElement.ExcludeMode;
import jadex.bridge.component.IExecutionFeature;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import services.IManagerService;

@Agent
public class DummyManagerBDI extends ManagerBDI {

	private int companyBidDelta = 20;
	private float pBid = 0.6f;
	private float pAcceptClosed = 0.5f;

	@Override
	public IFuture<Company> informNewCompanyAuction(Company company) {
		Future<Company> future = new Future<Company>();
		if (randomGenerator.nextFloat() < pBid) {
			company.owner = (Manager) self;
			company.currentOffer += 1 + randomGenerator.nextInt(companyBidDelta);
			future.setResult(company);
		} else {
			future.setResult(null);
		}
		return future;
	}

	@Belief
	private Map<Future<Boolean>, Company> offersQueue = new ConcurrentHashMap<>();

	@Override
	public IFuture<Boolean> investOn(Company offer) {
		Future<Boolean> future = new Future<>();
		offersQueue.put(future, offer);
		return future;
	}

	@Goal(excludemode = ExcludeMode.Never)
	public class ProcessOffers {

		@GoalMaintainCondition(beliefs = "offersQueue")
		protected boolean mantain() {
			return false;
			// return offersQueue.size() == 0;
		}

		@GoalContextCondition(beliefs = "gameState")
		protected boolean context() {
			boolean inContext = gameState.equals(WallStreetAgent.GameState.NEGOTIATION);
			if (!inContext) {
				for (Future<Boolean> f : offersQueue.keySet()) {
					f.setResult(false);
					offersQueue.remove(f);
				}
			}

			return inContext;
		}

		@GoalTargetCondition(beliefs = "offersQueue")
		protected boolean target() {
			return offersQueue.size() == 0;
		}
	}

	@Plan(trigger = @Trigger(goals = ProcessOffers.class))
	protected void invest() {
		Map.Entry<Future<Boolean>, Company> entry = offersQueue.entrySet().iterator().next();
		Future<Boolean> future = entry.getKey();
		Company offer = entry.getValue();
		boolean accept = false;

		int index = myCompanies().indexOf(offer);

		if (index < 0 || myCompanies().get(index).closed
				|| offer.currentOffer <= myCompanies().get(index).currentOffer) {
			accept = false;
		} else if (offer.closed) {
			Company company = myCompanies().get(index);

			double E = market.companyNextRoundExpectedRevenue(company);
			double decisionValue = E <= 0 ? pAcceptClosed : (E - offer.currentOffer) / (E);

			
			if (randomGenerator.nextFloat() < decisionValue) {
				accept = false;
			} else {
				accept = true;
			}

		} else {
			accept = true;
		}

		if (accept) {
			Boolean success = wallStreet.informOffer(offer).get();
			if (success) {
				myCompanies().set(index, offer);
			}
			future.setResult(success);
		} else {
			future.setResult(false);
		}

		offersQueue.remove(future);

	}

	@AgentBody
	public void body() {
		super.body();
		bdiFeature.dispatchTopLevelGoal(new ProcessOffers());

	}

}
