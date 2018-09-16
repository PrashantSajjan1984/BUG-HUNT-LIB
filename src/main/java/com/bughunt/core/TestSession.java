package com.bughunt.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bughunt.domain.MethodVO;
import com.bughunt.domain.Test;

public class TestSession {
	private static List<Test> testCases;
	private static Map<String, MethodVO> keywordMap;
	private static Set<String> reportProps;
	private static Map<String, MethodVO> annotationMap;
	
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
	
}


