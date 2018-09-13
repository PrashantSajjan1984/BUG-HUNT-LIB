package com.bughunt.testmanager;

import java.util.List;
import java.util.Map;

import com.bughunt.domain.Test;

public abstract class TestManager {
	public abstract void setTestsToExecute();
	public abstract Map<String, String> getTestManagerRow(int rowNo);
	public abstract Map<String, String> getTestManagerRow(String testCaseName);
	public abstract String getTestManagerColumnVal(int rowNo, String columnName);
	public abstract String getTestManagerColumnVal(String testCaseName, String columnName);
	
}
