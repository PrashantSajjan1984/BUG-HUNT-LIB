package com.bughunt.core;

import com.bughunt.domain.Test;

public abstract class TestExecutor {

	public void executeTests() {
		for(Test test: TestSession.getTestCases()) {
			callTestMethods(test);
		} 
	}
	
	protected abstract void callTestMethods(Test test);
}
