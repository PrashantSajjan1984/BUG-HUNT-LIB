package com.bughunt.core;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.bughunt.constants.BugHuntConstants;
import com.bughunt.domain.Test;
import com.bughunt.reports.SummaryReport;
import com.bughunt.util.SynchronizeUtil;

public class RunParallelDeviceConfig implements Runnable {
	
	private String groupID;
	private SummaryReport summaryReport;
	private SynchronizeUtil syncUtil;
	public RunParallelDeviceConfig(String groupID, SummaryReport summaryReport, SynchronizeUtil syncUtil) {
		this.groupID = groupID;
		this.summaryReport = summaryReport;
		this.syncUtil = syncUtil;
	}
	
	@Override
	public void run() {
		List<Map<String, String>>  configs = TestSession.getParallelConfigs();
		List<Map<String, String>> groups = configs.stream().filter(t->groupID.equals(t.get(BugHuntConstants.GROUP_ID))).collect(Collectors.toList());
		if(groups.size()==1) {
			runTestsForNoGroup();
		} else {
			syncUtil.setGroupsMap(groupID, groups);
			runTestForAGroup();
		}
	}
	
	private void runTestsForNoGroup() {
		List<Test> tests = TestSession.getMultiConfigTestMap().get(groupID);
		Executor executor = new KeywordTestExecutor();
		List<Map<String, String>>  configs = TestSession.getParallelConfigs();
		List<Map<String, String>> groups = configs.stream().filter(t->groupID.equals(t.get(BugHuntConstants.GROUP_ID))).collect(Collectors.toList());
		String reportName = groups.stream().map(t->t.get("ReportValue")).findFirst().get();
		for(Test test: tests) {
			executor.executeTest(test);
			
			System.out.printf("Execution completed for test %s on thred %s for groupID %s for report %s\n", test.getName(), Thread.currentThread().getId(), groupID, reportName);
			//summaryRpt.generateReport();
		} 
	}
	
	private void runTestForAGroup() {
		boolean execute = true;
		Executor executor = new KeywordTestExecutor();
		do{
			Test test = syncUtil.getNextTestForDevice(groupID);
			if(test!=null) {
				executor.executeTest(test);
			} else {
				execute = false;
			}
		}while(execute==true);
	}
}
