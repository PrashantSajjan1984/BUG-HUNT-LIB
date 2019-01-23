package com.bughunt.testmanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.bughunt.config.BugHuntConfig;
import com.bughunt.constants.BugHuntConstants;
import com.bughunt.core.TestSession;
import com.bughunt.domain.Test;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonTestManager extends TestManager {

	@Override
	public boolean setTestsToExecute() {
		boolean setTestSucessful = true;
		List<Test> testsToExecute = new ArrayList<>();
		List<Map<String, String>> tests;
		try {
			tests = (List<Map<String, String>>) getFailedTestsList().get(0).get("tests");
			Test test = null;
			int rowCnt = 0;
			String testCaseName = null;
			for (Map<String, String> testMap : tests) {
				if(null != testMap.get(BugHuntConstants.TEST_CASE_NAME)) {
					testCaseName = testMap.get("Test Case Name");
				}
				if("Yes".equals(testMap.get(BugHuntConstants.EXECUTE))) {
					test = new Test(testCaseName, ++rowCnt, testMap);
					testsToExecute.add(test);
				}
			}
			TestSession.setTestCases(testsToExecute);
		} catch (IOException e) {
			e.printStackTrace();
			setTestSucessful = false;
		}
		return setTestSucessful;
	}

	@Override
	public String getTestManagerColumnVal(String columnName, int rowNum) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<Map<String, Object>> getFailedTestsList() throws IOException {
		List<Map<String, Object>> tests = null;
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
	    File file = new File(BugHuntConfig.getBaseFWPath() + BugHuntConstants.SRC_MAIN_RESOURCES_PATH + BugHuntConstants.FAILED_TESTS_JSON);
	    String json = FileUtils.readFileToString(file);
	    tests = mapper.readValue(json, new TypeReference<List<Map<String, Object>>>(){});
		return tests;
	}
	
}
