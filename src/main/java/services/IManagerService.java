package services;

import java.util.List;

import classes.Company;
import classes.Investor;
import jadex.bridge.IComponentIdentifier;
import jadex.commons.future.IFuture;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.SubscriptionIntermediateFuture;

public interface IManagerService {
	IFuture <Boolean> investOn(Company company, boolean close);
	IFuture <Boolean> closeDeal(Company company);
	IFuture <List<Company>> consultWhatCompaniesToSell();
	IFuture<Company> informNewCompanyAuction(Company company);

}
