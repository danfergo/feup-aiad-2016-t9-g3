package classes;

import java.util.ArrayList;

import jadex.bridge.IComponentIdentifier;
import jadex.commons.transformation.annotations.IncludeFields;

@IncludeFields
public class Manager extends Player{

	public ArrayList<Company> companies;
	
	public Manager(IComponentIdentifier componentIdentifier, ArrayList<Company> companies) {
		super(componentIdentifier);
		this.companies = companies;
	}

	
	public ArrayList<Company> getCompanies(){
		return this.companies;
	} 
	
	
}
