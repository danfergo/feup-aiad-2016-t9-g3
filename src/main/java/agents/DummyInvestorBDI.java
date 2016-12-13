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
	
	private int minOffer = 5;
	private int maxOffer = 35;
	private float pClose = 0.5f;
	
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

		IExecutionFeature exe = ia.getComponentFeature(IExecutionFeature.class);
		exe.waitForDelay(600).get();
		
		Company company = manager.getCompanies().get(companyIndex);
		if(self.equals(company.currentInvestor) || company.closed == true){
			return;
		}
		
		
		IManagerService managerService = (IManagerService) SServiceProvider
				.getService(ia, manager.getComponentIdentifier(), IManagerService.class).get();
		Company offer = company.clone();
		offer.currentInvestor = (Investor)self;
		offer.currentOffer = minOffer + randomGenerator.nextInt(maxOffer - minOffer + 1);
		offer.closed = randomGenerator.nextFloat() <= pClose;

		Boolean sucessfullInvestment = managerService.investOn(offer).get();
		if (sucessfullInvestment.equals(Boolean.TRUE)) {
			manager.companies.set(companyIndex, offer);
			console.log("SUCCESS");
		}else{
			console.log("FAILED");
		}
		

	}
}
