package agents;

import classes.Company;
import classes.Manager;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.Agent;

@Agent
public class IntermediateManagerBDI extends ManagerBDI {

	@Override
	public IFuture<Company> informNewCompanyAuction(Company company) {
		Future<Company> future = new Future<Company>();

		double E = market.companyNextRoundExpectedRevenue(company) * (5 - market.getRound());

		if (company.currentOffer < E) {
			company.owner = (Manager) self;
			company.currentOffer += 20 + randomGenerator.nextInt(5);
			future.setResult(company);
		} else {
			future.setResult(null);
		}
		return future;
	}

}
