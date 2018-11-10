package com.bughunt.testmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.bughunt.config.BugHuntConfig;
import com.bughunt.constants.BugHuntConstants;
import com.bughunt.core.TestSession;
import com.bughunt.domain.Test;
import com.bughunt.exception.InCompleteSettingsException;

public abstract class TestManager {
	public abstract boolean setTestsToExecute();
	public abstract String getTestManagerColumnVal(String columnName, int rowNum);
	
	public void setParallelMultiConfigTests() {
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
	
	public void setParallelDeviceTests() {
		List<Map<String,String>> parallelConfigs = BugHuntConfig.instance().getParallelConfigMap();
		if(parallelConfigs==null) {
			throw new InCompleteSettingsException("Please check ParallelConfig.json settings");
		}
		BugHuntConfig.instance().setParallelDeviceGroupID(parallelConfigs);
		TestSession.setParallelConfigs(parallelConfigs);
		Set<String> distinctGroupIDs = parallelConfigs.stream().map(t->t.get(BugHuntConstants.GROUP_ID)).collect(Collectors.toSet());
		System.out.println(distinctGroupIDs);
		Map<String, List<Test>> multiConfigTestMap = new HashMap<>();
		List<Test> multiConfigTests = null;
		Test configTest = null;
		Map<String, String> parallelConfig = null;
		for(String groupID:distinctGroupIDs) {
			multiConfigTests = new ArrayList();
			parallelConfig = parallelConfigs.stream().filter(t->groupID.equals(t.get(BugHuntConstants.GROUP_ID))).findFirst().get();
			for(Test test: TestSession.getTestCases()) {
				// This parallel config will be replaced before invoking method for group ids provided in config.
				// Else all tests will run with same config for given group id
				configTest = new Test(test,parallelConfig);
				multiConfigTests.add(configTest);
			}
			multiConfigTestMap.put(groupID, multiConfigTests);
		}
		TestSession.setMultiConfigTestMap(multiConfigTestMap);
	}
}
 