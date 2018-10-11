package com.bughunt.testmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bughunt.config.BugHuntConfig;
import com.bughunt.core.TestSession;
import com.bughunt.domain.Test;
import com.bughunt.exception.InCompleteSettingsException;

public abstract class TestManager {
	public abstract boolean setTestsToExecute();
	public abstract String getTestManagerColumnVal(String columnName, int rowNum);
	
	public void setParallelConfigTests() {
		List<Map<String,String>> parallelConfigs = BugHuntConfig.instance().getParallelConfigMap();
		if(parallelConfigs==null) {
			throw new InCompleteSettingsException("Please check ParallelConfig.json settings");
		}
		Map<String, List<Test>> multiConfigTestMap = new HashMap<>();
		List<Test> multiConfigTests = null;
		Test configTest = null;
		for(Test test: TestSession.getTestCases()) {
			multiConfigTests = new ArrayList();
			for(Map<String, String> parallelConfig:parallelConfigs) {
				configTest = new Test(test, parallelConfig);
				multiConfigTests.add(configTest);
			}
			multiConfigTestMap.put(test.getName(), multiConfigTests);
		}
		TestSession.setMultiConfigTestMap(multiConfigTestMap);
	}
}
 