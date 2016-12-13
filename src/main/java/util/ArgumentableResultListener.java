package util;

import jadex.commons.future.DefaultResultListener;

public abstract class ArgumentableResultListener<T> extends DefaultResultListener<T> {
	
	protected Object [] args;
	protected Object arg;
	
	public ArgumentableResultListener(Object [] args){
		this.args = args;
	}
	
	public ArgumentableResultListener(Object arg){
		this.arg = arg;
	}
}
