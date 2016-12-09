package classes;

import java.util.ArrayList;

import jadex.bridge.IComponentIdentifier;
import jadex.commons.transformation.annotations.IncludeFields;

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
	
	
}
