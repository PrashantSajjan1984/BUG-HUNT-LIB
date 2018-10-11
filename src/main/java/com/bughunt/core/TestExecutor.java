package com.bughunt.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;

import com.bughunt.config.BugHuntConfig;
import com.bughunt.constants.BugHuntConstants;
import com.bughunt.domain.Test;
import com.bughunt.reports.SummaryReport;
import com.bughunt.util.CommonUtil;

public class TestExecutor {

	Executor executor;
	
	public void executeTests() {
		SummaryReport summaryRpt = new SummaryReport();
		executor = new KeywordTestExecutor();
		for(Test test: TestSession.getTestCases()) {
			executor.executeTest(test);
			summaryRpt.generateReport();
		} 
	}
	
	public void executeTestsInParallel() {
		 String threadCount = BugHuntConfig.instance().getBugHuntProperty(BugHuntConstants.THREAD_COUNT);
		 int count = CommonUtil.getIntegerValue(threadCount) != 0 ? Integer.valueOf(threadCount) : Runtime.getRuntime().availableProcessors();
		 ExecutorService es = Executors.newFixedThreadPool(count);
	     List<Runnable> tasks = getTasks();
	     CompletableFuture<?>[] futures = tasks.stream().map(task -> CompletableFuture.runAsync(task, es)).toArray(CompletableFuture[]::new);
	     CompletableFuture.allOf(futures).join();
	     es.shutdown();
	}
	
	private List<Runnable> getTasks() {
        List<Runnable> tasks = new ArrayList<Runnable>();
        RunParallel runParallel;
        SummaryReport summaryReport = new SummaryReport();
        for(Test test:TestSession.getTestCases()) {
        		runParallel = new RunParallel(test, summaryReport);
        		tasks.add(runParallel);
        }
        return tasks;
    }
	
	public void executeTestsForParallelConfig() {
		 String threadCount = BugHuntConfig.instance().getBugHuntProperty(BugHuntConstants.THREAD_COUNT);
		 int count = CommonUtil.getIntegerValue(threadCount) != 0 ? Integer.valueOf(threadCount) : Runtime.getRuntime().availableProcessors();
		 ExecutorService es = Executors.newFixedThreadPool(count);
	     List<Runnable> tasks = getTasksParallelConfig();
	     CompletableFuture<?>[] futures = tasks.stream().map(task -> CompletableFuture.runAsync(task, es)).toArray(CompletableFuture[]::new);
	     CompletableFuture.allOf(futures).join();
	     es.shutdown();
	}
	
	private List<Runnable> getTasksParallelConfig() {
       List<Runnable> tasks = new ArrayList<Runnable>();
       RunParallelMultiConfig runParallel;
       SummaryReport summaryReport = new SummaryReport();
       for(Test test:TestSession.getTestCases()) {
       		runParallel = new RunParallelMultiConfig(test, summaryReport);
       		tasks.add(runParallel);
       }
       return tasks;
   }
	
	
}
