package com.bughunt.core;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.bughunt.config.BugHuntConfig;
import com.bughunt.constants.BugHuntConstants;
import com.bughunt.exception.InCompleteSettingsException;
import com.bughunt.keywordmanager.ExcelKeywordManager;
import com.bughunt.keywordmanager.KeywordManager;
import com.bughunt.testmanager.ExcelTestManager;
import com.bughunt.testmanager.JsonTestManager;
import com.bughunt.testmanager.TestManager;
import com.bughunt.util.CommonUtil;
import com.bughunt.util.ExcelUtil;
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
		TestSession.setStartExecutionTime(LocalDateTime.now());
		execInProgress = true;
		try {
			configureAndTriggerExecution();	
		} catch(InCompleteSettingsException iex) {
			iex.printStackTrace();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		execInProgress = false;
	}

	private void configureAndTriggerExecution() {
		BugHuntConfig.instance().setConfigPaths();
		persistMethods();
		ExcelUtil.setCommonData();
		setTestsToExecute();
		ExcelUtil.deleteFailedTestsExcel();
		setTestKeywords();
		setMasterTestData();
		createExecutionReportFolder();
		setParallelConfig();
		executeTests();
		ExcelUtil.addFailedTCToExcel();
	}

	private void persistMethods() {
		PersistMethods persistMethods = new PersistMethods();
		try {
			persistMethods.setKeywordMethodMapping();
		} catch(InCompleteSettingsException iex) {
			throw iex;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setTestsToExecute() {
		TestManager testManager = null;
		boolean testSetSuccessful = false;
		BugHuntConfig bugHuntConfig = BugHuntConfig.instance();
		String fileName = bugHuntConfig.getBaseFWPath() + BugHuntConstants.FAILED_TESTS_EXCEL;
		if("true".equals(bugHuntConfig.getBugHuntProperty("ExecuteFailedTests")) &&
				!Files.exists(Paths.get(fileName))) {
			ExcelTestManager excelManager = new ExcelTestManager();
			excelManager.setTestHeaderColumnAndWidth();	
			testManager = new JsonTestManager();
			testSetSuccessful = testManager.setTestsToExecute();
		} else {
			testManager = new ExcelTestManager();
			testSetSuccessful = testManager.setTestsToExecute();
			if(!testSetSuccessful && 
					"true".equals(bugHuntConfig.getBugHuntProperty("ExecuteFailedTests"))) {
				testManager = new JsonTestManager();
				testManager.setTestsToExecute();
			}
		}
	}

	private void setParallelConfig() {
		TestManager testManager = null;
		if(BugHuntConstants.EXCEL.toLowerCase().
				equals(BugHuntConfig.instance().getBugHuntProperty(BugHuntConstants.TEST_MANAGER_FORMAT).toLowerCase())) {
			testManager = new ExcelTestManager();
		}
		if(BugHuntConstants.PARALLEL_MULTI_CONFIG.toLowerCase().equals(
				BugHuntConfig.instance().getBugHuntProperty(BugHuntConstants.EXECUTION_MODE).toLowerCase())) {
			testManager.setParallelConfigTests();
		}
	}
	
	private void setTestKeywords() {
		KeywordManager keywordManager = null;
		if(BugHuntConstants.EXCEL.toLowerCase().
				equals(BugHuntConfig.instance().getBugHuntProperty(BugHuntConstants.TEST_DATA_FORMAT).toLowerCase())) {
			keywordManager = new ExcelKeywordManager();
		}
		keywordManager.setKeywords();
	}
	
	private void createExecutionReportFolder() {
		String reportFolder = getExecutionReportFolderName();
        CommonUtil.createFolder(reportFolder);
        BugHuntConfig.instance().setExecutionReportPath(reportFolder);
	}

	private String getExecutionReportFolderName() {
		LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String formatDateTime = now.format(formatter);
        String reportFolder = BugHuntConfig.instance().getBaseFWPath() + BugHuntConstants.SRC_MAIN_RESOURCES_PATH 
        		+ BugHuntConstants.REPORT_PATH + BugHuntConstants.BUG_HUNT_REPORT + "_" + formatDateTime;
		return reportFolder;
	}
	
	private void setMasterTestData() {
		ExcelUtil.setMasterTestData();
	}
	
	private void executeTests() {
		TestExecutor testExecutor = new TestExecutor();
		switch(TestSession.getExecutionMode()) {
		case PARALLEL:
				testExecutor.executeTestsInParallel();
			break;
		case PARALLELMULTICONFIG:
				testExecutor.executeTestsForParallelConfig();
			break;
		case SEQUENTIAL:
				testExecutor.executeTests();
			break;
		default:
				testExecutor.executeTests();
			break;
		}
	}
}
