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
		if(optTest.isPresent()) {
			test = optTest.get();
			Map<String, String> config = getDeviceConfig(groupID);
			test.setParallelConfig(config);
			System.out.println("Running test - "+test.getName());
			System.out.println("Thread  - "+Thread.currentThread().getId());
			System.out.println("-----------------------------------------");
		}
		return test;
	}
	
	private Map<String, String> getDeviceConfig(String groupID) {
		List<Map<String, String>> configs = groupsMap.get(groupID);
		int index = 0;
		if(null!=groupsLastIndexMap.get(groupID)) {
			int indexCalc = groupsLastIndexMap.get(groupID) +1;
			index = indexCalc == configs.size() ? 0 :indexCalc;
		}
		groupsLastIndexMap.put(groupID, index);
		return configs.get(index);
	}
}
