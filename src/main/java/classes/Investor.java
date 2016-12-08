package classes;

import jadex.bridge.IComponentIdentifier;
import jadex.commons.transformation.annotations.IncludeFields;

@IncludeFields
public class Investor extends Player {

	public Investor(IComponentIdentifier componentIdentifier) {
		super(componentIdentifier);
	}
	
}
