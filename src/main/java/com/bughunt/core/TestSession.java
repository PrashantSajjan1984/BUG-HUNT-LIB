package com.bughunt.core;

import java.time.LocalDateTime;
import java.util.EnumSet;
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
	private static Set<String> summaryReportProps;
	private static Map<String, MethodVO> annotationMap;
	private static EnumSet<StepResult> screenShotStepResults;
	private static Map<Integer, Object> masterTestData;
	private static Map<String, List<Test>> multiConfigTestMap;
	private static Map<String, Object> multiDeviceGroupMap;
	private static ExecutionMode executionMode;
	private static LocalDateTime startExecutionTime;
	private static Map<String, String> commonData;
	private static int reRunCount;
	private static List<Map<String,String>> parallelConfigs;
	
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
	
	public static Set<String> getSummaryReportProps() {
		return summaryReportProps;
	}

	public static void setSummaryReportProps(Set<String> summaryReportProps) {
		TestSession.summaryReportProps = summaryReportProps;
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

	public static LocalDateTime getStartExecutionTime() {
		return startExecutionTime;
	}

	public static void setStartExecutionTime(LocalDateTime startExecutionTime) {
		TestSession.startExecutionTime = startExecutionTime;
	}

	public static Map<String, String> getCommonData() {
		return commonData;
	}

	public static void setCommonData(Map<String, String> commonData) {
		TestSession.commonData = commonData;
	}

	public static int getReRunCount() {
		return reRunCount;
	}

	public static void setReRunCount(int reRunCount) {
		TestSession.reRunCount = reRunCount;
	}

	public static Map<String, Object> getMultiDeviceGroupMap() {
		return multiDeviceGroupMap;
	}

	public static void setMultiDeviceGroupMap(Map<String, Object> multiDeviceGroupMap) {
		TestSession.multiDeviceGroupMap = multiDeviceGroupMap;
	}

	public static List<Map<String, String>> getParallelConfigs() {
		return parallelConfigs;
	}

	public static void setParallelConfigs(List<Map<String, String>> parallelConfigs) {
		TestSession.parallelConfigs = parallelConfigs;
	}
}


