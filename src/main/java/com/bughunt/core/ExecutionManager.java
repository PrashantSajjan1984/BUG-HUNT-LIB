package com.bughunt.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map.Entry;

import com.bughunt.config.BugHuntConfig;
import com.bughunt.constants.BugHuntConstants;
import com.bughunt.domain.MethodVO;
import com.bughunt.domain.Test;
import com.bughunt.exception.InCompleteSettingsException;
import com.bughunt.keywordmanager.ExcelKeywordManager;
import com.bughunt.keywordmanager.KeywordManager;
import com.bughunt.testmanager.ExcelTestManager;
import com.bughunt.testmanager.TestManager;
import com.bughunt.util.CommonUtil;
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
		setTestsToExecute();
		setTestKeywords();
		createExecutionReportFolder();
		executeTests();
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
		if(BugHuntConstants.EXCEL.toLowerCase().
				equals(BugHuntConfig.instance().getBugHuntProperty(BugHuntConstants.TEST_MANAGER_FORMAT).toLowerCase())) {
			testManager = new ExcelTestManager();
		}
		testManager.setTestsToExecute();
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
	
	private void executeTests() {
		TestExecutor testExecutor = new KeywordTestExecutor();
		testExecutor.executeTests();
	}
}
