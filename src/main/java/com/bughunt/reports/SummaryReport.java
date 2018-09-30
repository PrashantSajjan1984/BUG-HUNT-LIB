package com.bughunt.reports;

import com.bughunt.domain.Test;

public class SummaryReport {

	public synchronized void generateReport(Test test) {
		System.out.println("Test execution completed : "+test.getName());
	}
	
	public synchronized void generateParallelConfigReport(Test test) {
		System.out.println("Test execution completed Parallel Config : "+test.getName());
	}
}
