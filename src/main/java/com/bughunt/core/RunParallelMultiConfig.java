package com.bughunt.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;

import com.bughunt.config.BugHuntConfig;
import com.bughunt.domain.Test;
import com.bughunt.reports.SummaryReport;

public class RunParallelMultiConfig implements Runnable {

	private List<Thread> mWorkerThreads;
	
	private Test test;
	private SummaryReport summaryReport;
	
	public RunParallelMultiConfig(Test test, SummaryReport summaryReport) {
		this.test = test;
		this.summaryReport = summaryReport;
	}
	
	@Override
	public void run() {
		mWorkerThreads = new LinkedList<Thread>();
		List<Test> configTests = TestSession.getMultiConfigTestMap().get(test.getName());
		for(Test configTest:configTests) {
			Thread t = new Thread(new Runnable() {
                public void run() {
                		Executor executor = new KeywordTestExecutor();
                		executor.setProps(configTest.getParallelConfig());
                		executor.callTestMethods(configTest);
                }
            });
			mWorkerThreads.add (t);
	        t.start();
		}
		awaitTasksDone();
		summaryReport.generateParallelConfigReport(test);
	}

	protected void awaitTasksDone() {
        for (Thread thread : mWorkerThreads)
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println("awaitTasksDone interrupted");
            }
    }
}
