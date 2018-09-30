package com.bughunt.core;

import java.util.Map;

import com.bughunt.domain.Test;

public abstract class Executor {
	
	private Map<String, String> props;
	
	public void setProps(Map<String, String> props) {
		this.props = props;
	}
	
	public Map<String, String> getProps() {
		return props;
	}
	
	protected abstract void callTestMethods(Test test);
	
}
