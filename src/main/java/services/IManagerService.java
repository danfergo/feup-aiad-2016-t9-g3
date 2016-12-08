package services;

import classes.Company;
import classes.Investor;
import jadex.bridge.IComponentIdentifier;
import jadex.commons.future.IFuture;

public interface IManagerService {
	IFuture <Boolean> investOn(Investor investor, Company company, int offer, boolean close);
	IFuture <Boolean> closeDeal(Company company);

}
