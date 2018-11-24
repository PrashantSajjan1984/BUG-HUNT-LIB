package com.bughunt.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.bughunt.core.TestSession;
import com.bughunt.domain.Test;
import com.bughunt.domain.Test.OverALLStatus;

public class SynchronizeUtil {

	Map<String, List<Map<String, String>>> groupsMap = new HashMap<>();
	Map<String, Integer> groupsLastIndexMap = new HashMap<>();
	
	public void setGroupsMap(String groupID, List<Map<String, String>> groups) {
		if(!groupsMap.containsKey(groupID)) {
			groupsMap.put(groupID, groups);
		}
	}

	public synchronized Test getNextTestForDevice(String groupID) {
		List<Test> tests = TestSession.getMultiConfigTestMap().get(groupID);
		Optional<Test> optTest = tests.stream().filter(t->t.getOverAllStatus()==OverALLStatus.NOT_STARTED).findFirst();
		Test test = null;
		Map<String, String> config = getDeviceConfig(groupID);
		if(optTest.isPresent() && null!=config) {
			test = optTest.get();
			test.setParallelConfig(config);
			System.out.println("------------------------------------------------------");
			System.out.println();
			System.out.printf("Execution started for test %s on thred %s for groupID %s for report %s\n", test.getName(), Thread.currentThread().getId(), groupID, config.get("ReportValue"));
			System.out.println();
			System.out.println("------------------------------------------------------");
		}
		return test;
	}
	
	private Map<String, String> getDeviceConfig(String groupID) {
		List<Map<String, String>> configs = groupsMap.get(groupID);
		// int index = 0;
		if(configs!=null && !configs.isEmpty()) {
			int indexCalc = groupsLastIndexMap.containsKey(groupID) ? groupsLastIndexMap.get(groupID) + 1 : 0;
			// index = indexCalc == configs.size() ? 0 :indexCalc;
			if(indexCalc < configs.size()) {
				groupsLastIndexMap.put(groupID, indexCalc);
				return configs.get(indexCalc);
			} else {
				groupsLastIndexMap.put(groupID, 0);
				return configs.get(0);
			}
		}
		return null;
	}
}
