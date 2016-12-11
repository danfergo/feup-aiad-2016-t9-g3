package classes;

import jadex.commons.transformation.annotations.IncludeFields;

@IncludeFields
public class Company {
	 
	public int id;
	public boolean x2;
	public Market.Color color;
	

	public int currentOffer ;
	public Investor currentInvestor;
	public boolean closed;
	public Manager owner;
	public String name;
	
	Company(Market.Color color, boolean x2, int id,String name){
		this.color = color;
		this.x2 = x2;
		this.id = id;
		this.name = name;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null || !Company.class.isAssignableFrom(other.getClass())) {
	        return false;
	    }
		return ((Company)other).id == this.id;
	}
		
}
