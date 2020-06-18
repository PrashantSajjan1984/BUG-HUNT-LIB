package com.bughunt.core;

import com.bughunt.domain.Test;
import com.bughunt.reports.SummaryReport;

public class RunParallel implements Runnable {

	private Test test;
	private SummaryReport summaryReport;
	
	public RunParallel(Test test, SummaryReport summaryReport) {
		this.test = test;
		this.summaryReport = summaryReport;
	}
	
	@Override
	public void run() {
		Executor executor = new KeywordTestExecutor();
		executor.executeTest(test);
		summaryReport.generateReport();
	}
}
