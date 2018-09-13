package com.bughunt.core;

import java.util.List;
import java.util.Map;

import com.bughunt.domain.MethodVO;
import com.bughunt.domain.Test;

public class TestSession {
	public static List<Test> testCases;
	public static Map<String, MethodVO> keywordMap;
	private TestSession() {
		
	}
}
