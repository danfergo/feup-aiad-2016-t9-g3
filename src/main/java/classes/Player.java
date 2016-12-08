package classes;

import jadex.bridge.IComponentIdentifier;
import jadex.commons.transformation.annotations.IncludeFields;

@IncludeFields
public abstract class Player {

	private static final int startBalance = 120;
	
	public int balance = startBalance;
	public IComponentIdentifier componentIdentifier = null;
	
	
	Player(IComponentIdentifier componentIdentifier){
		this.componentIdentifier = componentIdentifier;
	}
	
	
	void setBalance(){
		this.balance = balance;
	}
	
	int getBalance(){
		return this.balance;
	}
	
	
	@Override
	public boolean equals(Object other) {
		if (other == null || !Player.class.isAssignableFrom(other.getClass())) {
	        return false;
	    }
		
		return ((Player)other).componentIdentifier.equals(this.componentIdentifier);
	}


	public IComponentIdentifier getComponentIdentifier() {
		return this.componentIdentifier;
	}
	
}
