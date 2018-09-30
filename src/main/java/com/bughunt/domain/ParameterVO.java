package com.bughunt.domain;

import java.util.Map;

import com.bughunt.reports.Report;
import com.bughunt.util.DataUtil;

public class ParameterVO {

	private Report report;
	private DataUtil dataUtil;
	private String testName;
	private String reportPath;
	private Map<String, String> jsonConfigProps;
	
	public ParameterVO(Report report, DataUtil dataUtil, String testName, String reportPath) {
		this.report = report;
		this.dataUtil = dataUtil;
		this.testName = testName;
		this.reportPath = reportPath;
	}
	
	public ParameterVO(Report report, DataUtil dataUtil, String testName, String reportPath, Map<String, String> jsonConfigProps) {
		this.report = report;
		this.dataUtil = dataUtil;
		this.testName = testName;
		this.reportPath = reportPath;
		this.jsonConfigProps = jsonConfigProps;
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

	public String getReportPath() {
		return reportPath;
	}

	public Map<String, String> getJsonConfigProps() {
		return jsonConfigProps;
	}
}
