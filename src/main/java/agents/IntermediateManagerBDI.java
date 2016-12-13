package agents;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import agents.DummyManagerBDI.ProcessOffers;
import classes.Company;
import classes.Manager;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.GoalContextCondition;
import jadex.bdiv3.annotation.GoalMaintainCondition;
import jadex.bdiv3.annotation.GoalTargetCondition;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.model.MProcessableElement.ExcludeMode;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;

@Agent
public class IntermediateManagerBDI extends ManagerBDI {

	private int companyBidDelta = 30	;
	private float pRejectClosed = 0.7f;
	
	@Override
	public IFuture<Company> informNewCompanyAuction(Company company) {
		Future<Company> future = new Future<Company>();

		double E = market.companyNextRoundExpectedRevenue(company) * (5 - market.getRound());
		if (company.currentOffer < E) {
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

		if (offer.closed && randomGenerator.nextFloat() < pRejectClosed) {
			future.setResult(false);
			offersQueue.remove(future);
			return;
		}

		// return offersQueue.size() == 0;
		int index = myCompanies().indexOf(offer);

		if (index != -1 && !myCompanies().get(index).closed
				&& offer.currentOffer > myCompanies().get(index).currentOffer) {

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
