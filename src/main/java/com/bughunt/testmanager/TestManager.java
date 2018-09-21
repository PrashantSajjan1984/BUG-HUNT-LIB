package com.bughunt.testmanager;

public abstract class TestManager {
	public abstract void setTestsToExecute();
	public abstract String getTestManagerColumnVal(String columnName, int rowNum);
}
 