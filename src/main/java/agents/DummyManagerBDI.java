package agents;

import classes.Company;
import classes.Manager;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.Agent;

@Agent
public class DummyManagerBDI extends ManagerBDI{
	@Override
	public IFuture<Company> informNewCompanyAuction(Company company) {
		Future<Company> future = new Future<Company>();
		float rnd = randomGenerator.nextInt(10);
		if (rnd < 6) {
			company.owner = (Manager) self;
			company.currentOffer += 20 + randomGenerator.nextInt(5);
			future.setResult(company);
		} else {
			future.setResult(null);
		}
		return future;
	}
}
