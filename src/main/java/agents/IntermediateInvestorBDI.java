package agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import agents.DummyInvestorBDI.InvestOnCompanies;
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
public class IntermediateInvestorBDI extends InvestorBDI {
	
	List<Manager> getInGameManagers() {
		return otherPlayers.stream().filter(p -> p instanceof Manager && p.inGame).map(p -> (Manager) p)
				.collect(Collectors.toList());
	}
	
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
	
	private ArrayList<Company> getManagersCompanies() {
		ArrayList<Company> companies = new ArrayList<>();
		for (Manager manager : getInGameManagers()) {
			companies.addAll(((Manager) manager).getCompanies());
		}
		return companies;
	}

	@Plan(trigger = @Trigger(goals = InvestOnCompanies.class))
	protected void invest() {

		List<Company> companies = new ArrayList<Company>(getManagersCompanies());
		Collections.sort(companies, new Comparator<Company>() {
		    @Override
		    public int compare(Company o1, Company o2) {
		        return (int) (market.companyNextRoundOpportunity(o1) - market.companyNextRoundOpportunity(o2));
		    }
		});

		for(Company company : companies){
			if(randomGenerator.nextInt(10) < 8){
				int offerValue = (int)market.companyNextRoundExpectedRevenue(company);
				if(company.currentOffer > offerValue){
					continue;
				}
				
				IManagerService managerService = (IManagerService) SServiceProvider
						.getService(ia, company.owner.getComponentIdentifier(), IManagerService.class).get();
				Company offer = company.clone();
				offer.currentInvestor = (Investor)self;
				offer.currentOffer = offerValue;	
				Boolean sucessfullInvestment = managerService.investOn(offer, false).get();
				
				if (sucessfullInvestment.equals(Boolean.TRUE)) {
					Manager m = (Manager)otherPlayers.get(otherPlayers.indexOf(company.owner));
					m.companies.set(m.companies.indexOf(company), offer);
					console.log("SUCCESS");
				}else{
					console.log("FAILED");
				}
				
				IExecutionFeature exe = ia.getComponentFeature(IExecutionFeature.class);
				exe.waitForDelay(1000).get();
			}
			
		}
		
		//if(companiesInvestOn >= )
		//console.log("Number of companies: " + companiesInvestOn);

	}
}
