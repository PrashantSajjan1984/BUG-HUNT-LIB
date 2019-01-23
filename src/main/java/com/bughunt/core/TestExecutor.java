package com.bughunt.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;

import com.bughunt.config.BugHuntConfig;
import com.bughunt.constants.BugHuntConstants;
import com.bughunt.domain.Test;
import com.bughunt.reports.SummaryReport;
import com.bughunt.util.CommonUtil;
import com.bughunt.util.SynchronizeUtil;

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
		 String threadCount = BugHuntConfig.getBugHuntProperty(BugHuntConstants.THREAD_COUNT);
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
		 String threadCount = BugHuntConfig.getBugHuntProperty(BugHuntConstants.THREAD_COUNT);
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
	
	public void executeTestsForParallelDeviceConfig() {
		 int threadCount = TestSession.getParallelConfigs().size();
		 System.out.println("No of thread counts : " +threadCount);
		 ExecutorService es = Executors.newFixedThreadPool(threadCount);
	     List<Runnable> tasks = getTasksParallelDeviceConfig();
	     CompletableFuture<?>[] futures = tasks.stream().map(task -> CompletableFuture.runAsync(task, es)).toArray(CompletableFuture[]::new);
	     CompletableFuture.allOf(futures).join();
	     es.shutdown();
	}
	
	private List<Runnable> getTasksParallelDeviceConfig() {
       List<Runnable> tasks = new ArrayList<Runnable>();
       RunParallelDeviceConfig runParallel;
       List<Map<String,String>> parallelConfigs = BugHuntConfig.getParallelConfigMap();
       List<String> groupIDs = parallelConfigs.stream().map(t->t.get(BugHuntConstants.GROUP_ID)).collect(Collectors.toList());
       System.out.println(groupIDs);
       SummaryReport summaryReport = new SummaryReport();
       SynchronizeUtil syncUtil = new SynchronizeUtil();
       for(String groupID:groupIDs) {
    	   runParallel = new RunParallelDeviceConfig(groupID, summaryReport, syncUtil);
    	   tasks.add(runParallel);
       }
       return tasks;
	}
}
