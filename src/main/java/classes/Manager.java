package classes;

import java.util.ArrayList;

import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.transformation.annotations.IncludeFields;
import services.IManagerService;
import services.IPlayerService;

@IncludeFields
public class Manager extends Player{

	public ArrayList<Company> companies = new ArrayList<>();

	public Manager(IComponentIdentifier componentIdentifier, ArrayList<Company> companies) {
		super(componentIdentifier);
		
		if(companies != null){
			for(Company company : companies){
				addCompany(company);
			}
		}
	}

	
	public ArrayList<Company> getCompanies(){
		return this.companies;
	} 
	
	public void addCompany(Company company){
		company.owner = this;
		companies.add(company);
	}
	
	public IManagerService getManagerService(IInternalAccess ia){
		return SServiceProvider.getService(ia, this.componentIdentifier, IManagerService.class).get();
	}
	
}
