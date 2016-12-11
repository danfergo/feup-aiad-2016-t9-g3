package util;

import jadex.bridge.IComponentIdentifier;

public class Console {

	IComponentIdentifier componentIdentifier;
	
	public Console(IComponentIdentifier componentIdentifier){
		this.componentIdentifier = componentIdentifier;
	}
	
	
	public void log(String message){
		System.out.println("(" + componentIdentifier.getLocalName() +  ") " + message);
	}
	
	public void log(int message){
		log(Integer.toString(message));
	}
	
}
