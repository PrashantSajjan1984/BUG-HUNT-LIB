package com.bughunt.core;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bughunt.domain.ExecutionMode;
import com.bughunt.domain.MethodVO;
import com.bughunt.domain.StepResult;
import com.bughunt.domain.Test;

public class TestSession {
	private static List<Test> testCases;
	private static Map<String, MethodVO> keywordMap;
	private static Set<String> reportProps;
	private static Map<String, MethodVO> annotationMap;
	private static EnumSet<StepResult> screenShotStepResults;
	private static Map<Integer, Object> masterTestData;
	private static Map<String, List<Test>> multiConfigTestMap;
	private static ExecutionMode executionMode;
	
	private TestSession() {
		
	}
	
	public static List<Test> getTestCases() {
		return testCases;
	}
	
	public static void setTestCases(List<Test> testCases) {
		TestSession.testCases = testCases;
	}
	
	public static Map<String, MethodVO> getKeywordMap() {
		return keywordMap;
	}
	
	public static void setKeywordMap(Map<String, MethodVO> keywordMap) {
		TestSession.keywordMap = keywordMap;
	}
	
	public static Set<String> getReportProps() {
		return reportProps;
	}
	
	public static void setReportProps(Set<String> reportProps) {
		TestSession.reportProps = reportProps;
	}

	public static Map<String, MethodVO> getAnnotationMap() {
		return annotationMap;
	}

	public static void setAnnotationMap(Map<String, MethodVO> annotationMap) {
		TestSession.annotationMap = annotationMap;
	}

	public static EnumSet<StepResult> getScreenShotStepResults() {
		return screenShotStepResults;
	}

	public static void setScreenShotStepResults(EnumSet<StepResult> screenShotStepResults) {
		TestSession.screenShotStepResults = screenShotStepResults;
	}

	public static Map<Integer, Object> getMasterTestData() {
		return masterTestData;
	}

	public static void setMasterTestData(Map<Integer, Object> masterTestData) {
		TestSession.masterTestData = masterTestData;
	}

	public static Map<String, List<Test>> getMultiConfigTestMap() {
		return multiConfigTestMap;
	}

	public static void setMultiConfigTestMap(Map<String, List<Test>> multiConfigTestMap) {
		TestSession.multiConfigTestMap = multiConfigTestMap;
	}

	public static ExecutionMode getExecutionMode() {
		return executionMode;
	}

	public static void setExecutionMode(ExecutionMode executionMode) {
		TestSession.executionMode = executionMode;
	}
}


