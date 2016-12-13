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

	float offerInc = 5;
	float pSkip = 0.2f;
	float pLock = 0.5f;

	List<Manager> getInGameManagers() {
		return otherPlayers.stream().filter(p -> p instanceof Manager && p.inGame).map(p -> (Manager) p)
				.collect(Collectors.toList());
	}

	private ArrayList<Company> getManagersCompanies() {
		ArrayList<Company> companies = new ArrayList<>();
		for (Manager manager : getInGameManagers()) {
			companies.addAll(((Manager) manager).getCompanies());
		}
		return companies;
	}

	private ArrayList<Company> getMyCompanies(boolean includeClosed) {
		ArrayList<Company> companies = new ArrayList<>();
		for (Manager manager : getInGameManagers()) {
			for (Company company : manager.getCompanies()) {
				if (self.equals(company.currentInvestor)) {
					if (includeClosed || !company.closed) {
						companies.add(company);

					}
				}
			}
		}
		return companies;
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

	@Plan(trigger = @Trigger(goals = InvestOnCompanies.class))
	protected void invest() {

		List<Company> companies = new ArrayList<Company>(getManagersCompanies());
		Collections.sort(companies, new Comparator<Company>() {
			@Override
			public int compare(Company o1, Company o2) {
				return (int) -1 * Double.compare(market.companyNextRoundOpportunity(o1), market.companyNextRoundOpportunity(o2));
			}
		});

		IExecutionFeature exe = ia.getComponentFeature(IExecutionFeature.class);
		exe.waitForDelay(500).get();

		
		for (Company company : companies) {
			if (pSkip < randomGenerator.nextFloat()) {
				continue;
			}

			int expectedRevenue = (int) market.companyNextRoundExpectedRevenue(company);

			if (company.currentOffer > expectedRevenue) {
				continue;
			}

			IManagerService managerService = (IManagerService) SServiceProvider
					.getService(ia, company.owner.getComponentIdentifier(), IManagerService.class).get();
			Company offer = company.clone();
			offer.currentInvestor = (Investor) self;
			offer.currentOffer = (int) Math.min(expectedRevenue, company.currentOffer + offerInc);
			offer.closed = randomGenerator.nextFloat() < pLock ? true : false;
			Boolean sucessfullInvestment = managerService.investOn(offer).get();

			if (sucessfullInvestment.equals(Boolean.TRUE)) {
				Manager m = (Manager) otherPlayers.get(otherPlayers.indexOf(company.owner));
				m.companies.set(m.companies.indexOf(company), offer);
				console.log("SUCCESS");
			} else {
				console.log("FAILED");
			}

			return;
		}

	}

	@Goal(excludemode = ExcludeMode.Never)
	public static class DropBadOffers {

		public InvestorBDI investor;

		DropBadOffers(InvestorBDI investor) {
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
		public static DropBadOffers launch(InvestorBDI investor) {
			return investor.gameState.equals(WallStreetAgent.GameState.NEGOTIATION) ? new DropBadOffers(investor)
					: null;
		}

	}

	@Plan(trigger = @Trigger(goals = DropBadOffers.class))
	protected void cancel() {

		IExecutionFeature exe = ia.getComponentFeature(IExecutionFeature.class);
		exe.waitForDelay(600).get();
		
		
		List<Company> badOffers = new ArrayList<Company>(getMyCompanies(false));
		Collections.sort(badOffers, new Comparator<Company>() {
			@Override
			public int compare(Company o1, Company o2) {
				return (int) Double.compare(market.companyNextRoundOpportunity(o1), market.companyNextRoundOpportunity(o2));
			}
		});

		if (badOffers.size() > 0
				&& market.companyNextRoundOpportunity(badOffers.get(0)) < badOffers.get(0).currentOffer) {
			Company offer = badOffers.get(0).clone();
			offer.currentOffer = 0;
			offer.currentInvestor = null;

			Boolean sucessfullInvestment = wallStreet.informOfferCancellation(offer).get();
			if (sucessfullInvestment.equals(Boolean.TRUE)) {
				Manager m = (Manager) otherPlayers.get(otherPlayers.indexOf(badOffers.get(0).owner));
				m.companies.set(m.companies.indexOf(badOffers.get(0)), offer);
				console.log("CANCEL SUCCESS");
			} else {
				console.log("CANCEL FAILED");
			}
		}
	}

}
