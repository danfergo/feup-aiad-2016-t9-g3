package util;

import jadex.commons.future.DefaultResultListener;

public abstract class ArgumentableResultListener<T> extends DefaultResultListener<T> {
	
	protected Object [] args;
	
	public ArgumentableResultListener(Object [] args){
		this.args = args;
	}
}
