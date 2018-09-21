package com.bughunt.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bughunt.core.TestSession;
import com.bughunt.domain.Test;
import com.bughunt.testmanager.ExcelTestManager;

public class DataUtil {
	private Test test;
	private Map<Integer, Object> testData = null;
	protected Map<Integer, Map<String, String>> iterationMaps = null;
	protected Map<String, Integer> keywordMap = null;
	protected Map<String, String> dataMap = null;
	
	public DataUtil(Test test) {
		this.test = test;
		testData = (Map<Integer, Object>) TestSession.getMasterTestData().get(test.getId());
		setIterationMaps();
		keywordMap = new HashMap<>();
	}
	
	private void setIterationMaps() {
		if(testData.containsKey(1)) {
			iterationMaps = (Map<Integer, Map<String, String>>) testData.get(1);
		}
	}
	
	public void setIteration(int iteration) {
		if(testData.containsKey(iteration)) {
			iterationMaps = (Map<Integer, Map<String, String>>) testData.get(iteration);
		}
	}
	
	public int getTotalIteration() {
        return null!=testData ? (int) testData.keySet().toArray()[testData.size() - 1] : 1;
	}
	
	public void setKeyword(String keyword) {
		int subIteration = 1;
		if(keywordMap.containsKey(keyword)) {
			int lastSubIteration = keywordMap.get(keyword);
			keywordMap.put(keyword, lastSubIteration + 1);
			subIteration = lastSubIteration + 1;
		} else {
			keywordMap.put(keyword, 1);
		}
		
		if(null!=iterationMaps && iterationMaps.containsKey(subIteration)) {
			dataMap = iterationMaps.get(subIteration);
		}
	}
	
	public String getData(String columnName) {
		String data = "";
		if(null!=dataMap && dataMap.containsKey(columnName)) {
			data = dataMap.get(columnName);
		}
		return data;
	}
	
	public String getData(String columnName, int subIteration) {
		String data = "";
		Map<String, String> tempDataMap = iterationMaps.get(subIteration);
		if(null!=tempDataMap && tempDataMap.containsKey(columnName)) {
			data = tempDataMap.get(columnName);
		}
		return data;
	}
	
	public String getTestManagerColumnVal(String columnName) {
		ExcelTestManager excelTestManager = new ExcelTestManager();
		return excelTestManager.getTestManagerColumnVal(columnName, test.getId());
	}
	
}
