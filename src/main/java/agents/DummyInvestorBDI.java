package agents;

import classes.Company;
import classes.Investor;
import classes.Manager;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.GoalContextCondition;
import jadex.bdiv3.annotation.GoalCreationCondition;
import jadex.bdiv3.annotation.GoalTargetCondition;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.model.MProcessableElement.ExcludeMode;
import jadex.bridge.component.IExecutionFeature;
import jadex.bridge.service.search.SServiceProvider;
import jadex.micro.annotation.Agent;
import services.IManagerService;


@Agent
public class DummyInvestorBDI extends InvestorBDI{
	@Goal(excludemode = ExcludeMode.Never)
	public static class InvestOnCompanies {

		public InvestorBDI investor;

		InvestOnCompanies(InvestorBDI investor) {
			this.investor = investor;
		}

		@GoalContextCondition(beliefs = "gameState")
		protected boolean mantain() {
			return investor.gameState.equals(WallStreetAgent.GameState.NEGOTIATION);
		}

		@GoalTargetCondition(beliefs = "selfBalance")
		protected boolean target() {
			return false;
		}

		@GoalCreationCondition(beliefs = { "gameState" })
		public static InvestOnCompanies launch(InvestorBDI investor) {
			return investor.gameState.equals(WallStreetAgent.GameState.NEGOTIATION) ? new InvestOnCompanies(investor)
					: null;
		}

	}
	

	@Plan(trigger = @Trigger(goals = InvestOnCompanies.class))
	protected void invest() {

		Manager manager = managers.get(randomGenerator.nextInt(managers.size()));
		int companyIndex = randomGenerator.nextInt(manager.getCompanies().size());

		Company company = manager.getCompanies().get(companyIndex);

		IManagerService managerService = (IManagerService) SServiceProvider
				.getService(ia, manager.getComponentIdentifier(), IManagerService.class).get();
		Company offer = company.clone();
		offer.currentInvestor = (Investor)self;
		offer.currentOffer = 20 + randomGenerator.nextInt(21);
		
		Boolean sucessfullInvestment = managerService.investOn(offer, false).get();
		
		if (sucessfullInvestment.equals(Boolean.TRUE)) {
			manager.companies.set(companyIndex, offer);
			companiesInvestOn++;
			console.log("SUCCESS");
		}else{
			console.log("FAILED");
		}
		//if(companiesInvestOn >= )
		//console.log("Number of companies: " + companiesInvestOn);
		IExecutionFeature exe = ia.getComponentFeature(IExecutionFeature.class);
		exe.waitForDelay(1000).get();
	}
}
