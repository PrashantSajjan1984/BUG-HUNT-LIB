package com.bughunt.core;

import java.util.Map.Entry;

import com.bughunt.config.BugHuntConfig;
import com.bughunt.constants.BugHuntConstants;
import com.bughunt.domain.MethodVO;
import com.bughunt.domain.Test;
import com.bughunt.testmanager.ExcelTestManager;
import com.bughunt.testmanager.TestManager;
import com.bughunt.util.PersistMethods;

public class ExecutionManager {

	private static ExecutionManager execManager;
	private boolean execInProgress = false;
	private ExecutionManager() {
		
	}
	
	public static ExecutionManager instance() {
		if(execManager == null) {
			execManager = new ExecutionManager();
		}
		return execManager;
	}
	
	public void triggerExecution() {
		if(execInProgress) {
			return;
		}
		execInProgress = true;
		BugHuntConfig.instance().setConfigPaths();
		persistMethods();
		setTestsToExecute();
			
		for(Test test : TestSession.testCases) {
			System.out.println(test);
		}
			
		for(Entry<String, MethodVO> entry : TestSession.keywordMap.entrySet()) {
			System.out.printf("Name %s , ClassName %s \n", entry.getValue().getName(), entry.getValue().getClassName());
		}
		System.out.println(BugHuntConfig.instance().getBugHuntProperty("Environment"));
	}

	private void persistMethods() {
		PersistMethods persistMethods = new PersistMethods();
		try {
			persistMethods.setKeywordMethodMapping();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setTestsToExecute() {
		TestManager testManager = null;
		if(BugHuntConstants.EXCEL.toLowerCase().
				equals(BugHuntConfig.instance().getBugHuntProperty(BugHuntConstants.TEST_MANAGER_FORMAT).toLowerCase())) {
			testManager = new ExcelTestManager();
		}
		testManager.setTestsToExecute();
	}
}
