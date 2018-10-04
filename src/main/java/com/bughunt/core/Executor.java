package com.bughunt.core;

import java.util.Map;

import com.bughunt.domain.Test;

public abstract class Executor {
	
	private Map<String, String> multiConfigProps;
	
	public void setProps(Map<String, String> multiConfigProps) {
		this.multiConfigProps = multiConfigProps;
	}
	
	public Map<String, String> getMultiConfigProps() {
		return multiConfigProps;
	}

	protected abstract void callTestMethods(Test test);
	
}
