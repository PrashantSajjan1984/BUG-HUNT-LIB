package com.bughunt.domain;

import com.bughunt.reports.Report;
import com.bughunt.util.DataUtil;

public class ParameterVO {

	private Report report;
	private DataUtil dataUtil;
	private String testName;
	
	public ParameterVO(Report report, DataUtil dataUtil, String testName) {
		this.report = report;
		this.dataUtil = dataUtil;
		this.testName = testName;
	}
	
	public Report getReport() {
		return report;
	}
	
	public DataUtil getDataUtil() {
		return dataUtil;
	}
	
	public String getTestName() {
		return testName;
	}
}
