package com.bughunt.reports;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.impl.xb.ltgfmt.TestsDocument.Tests;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.bughunt.config.BugHuntConfig;
import com.bughunt.constants.BugHuntConstants;
import com.bughunt.core.TestSession;
import com.bughunt.domain.Test;
import com.bughunt.domain.Test.OverALLStatus;
import com.bughunt.util.CommonUtil;
import com.samskivert.mustache.Mustache;

public class SummaryReport {

	public synchronized void generateReport() {
		Map<String, Object> testObject = new HashMap<>();
		Map<String, List<Test>> testMap = new HashMap<>();
		List<Test> testsCompleted = CommonUtil.getTestsCompleted();
		AtomicInteger index = new AtomicInteger();
		testsCompleted.forEach(t->t.setSlNo(index.incrementAndGet()));
		testMap.put("tests", testsCompleted);
		testObject.put("testObject", testMap);
		testObject.put("headerLabels", TestSession.getSummaryReportProps());

		List<Map<String, String>> summaryMap = getSummaryHeader(testsCompleted);
		testObject.put("summaryHeaders",summaryMap);
		String reportName = BugHuntConfig.instance().getExecutionReportPath() + "SummaryReport.html";
		createReport(BugHuntConstants.SUMMARY_REPORT_TEMPLATE_NAME, testObject, reportName);
		failedTestsJSONReport(testsCompleted);
	}

	private List<Map<String, String>> getSummaryHeader(List<Test> testsCompleted) {
		List<Map<String,String>> summaryMap = new ArrayList<>();
		addSummaryReportProps(BugHuntConstants.ENVIRONMENT, 
				BugHuntConfig.instance().getBugHuntProperty(BugHuntConstants.ENVIRONMENT), summaryMap);
		addSummaryReportProps(BugHuntConstants.DATE_TIME, 
				CommonUtil.getExecutionDateTime(), summaryMap);
		int noOfStepsPassed = (int) testsCompleted.stream().filter(t->t.getOverAllStatus()==(OverALLStatus.PASSED)).count();
		int noOfStepsFailed = (int) testsCompleted.stream().filter(t->t.getOverAllStatus()==(OverALLStatus.FAILED)).count();
		
		addSummaryReportProps(BugHuntConstants.NO_OF_TESTS_PASSED, 
				String.valueOf(noOfStepsPassed), summaryMap);
		addSummaryReportProps(BugHuntConstants.NO_OF_TESTS_FAILED, 
				String.valueOf(noOfStepsFailed), summaryMap);
		
		LocalDateTime endTime = LocalDateTime.now();
	    long diffInSeconds = Duration.between(TestSession.getStartExecutionTime(), endTime).getSeconds();
		String executionTime = LocalTime.MIN.plusSeconds(diffInSeconds).toString();
		addSummaryReportProps(BugHuntConstants.EXECUTION_TIME, 
				executionTime, summaryMap);
		return summaryMap;
	}
	
	private void addSummaryReportProps(String label, String value,List<Map<String,String>> summaryMap) {
		Map<String, String> propMap = new HashMap<>();
		propMap.put(BugHuntConstants.LABEL, label);
		propMap.put(BugHuntConstants.VALUE,value);
		summaryMap.add(propMap);
	}
	
	
	public synchronized void generateParallelConfigSummaryReport() {
		Map<String, Object> testObject = new HashMap<>();
		Map<String, Object> testMap = new HashMap<>();
		List<MultiConfigResult> reportObject = getMultiConfigSummaryReportObject();
		testMap.put("tests", reportObject);
		testObject.put("testObject", testMap);
		testObject.put("headerLabel", TestSession.getSummaryReportProps());
		List<Map<String, String>> summaryMap = getMultiConfigSummaryHeader(reportObject);
		testObject.put("summaryHeaders",summaryMap);
		String reportName = BugHuntConfig.instance().getExecutionReportPath() + "MultiConfigSummaryReport.html";
		createReport(BugHuntConstants.MULTI_CONFIG_SUMMARY_REPORT_TEMPLATE_NAME, testObject, reportName);
	}

	private List<MultiConfigResult> getMultiConfigSummaryReportObject() {
		List<MultiConfigResult> testResults = new ArrayList<>();
		MultiConfigResult result = null;
		int slNo = 0;
		for(Entry<String, List<Test>> entry:TestSession.getMultiConfigTestMap().entrySet()) {
			List<Test> tests = entry.getValue();
			boolean executionNotCompleted = tests.stream().anyMatch(t->OverALLStatus.NOT_STARTED ==t.getOverAllStatus() 
					|| OverALLStatus.INPROGRESS ==t.getOverAllStatus());
			if(executionNotCompleted) {
				continue;
			}
			List<String> reportVals = tests.stream().map(t->t.getParallelConfig().get(BugHuntConstants.REPORT_VALUE)).collect(Collectors.toList());
			String reportVal = StringUtils.join(reportVals, ",");
			boolean testFailed = tests.stream().anyMatch(t->OverALLStatus.FAILED ==t.getOverAllStatus());
			result = new MultiConfigResult(++slNo, entry.getKey(), reportVal, !testFailed );
			testResults.add(result);
		}
		return testResults;
	}
	
	private List<Map<String, String>> getMultiConfigSummaryHeader(List<MultiConfigResult> reportObject) {
		List<Map<String,String>> summaryMap = new ArrayList<>();
		addSummaryReportProps(BugHuntConstants.ENVIRONMENT, 
				BugHuntConfig.instance().getBugHuntProperty(BugHuntConstants.ENVIRONMENT), summaryMap);
		addSummaryReportProps(BugHuntConstants.DATE_TIME, 
				CommonUtil.getExecutionDateTime(), summaryMap);
		int noOfStepsPassed = (int) reportObject.stream().filter(t->t.isTestPassed()).count();
		int noOfStepsFailed = (int) reportObject.stream().filter(t->!t.isTestPassed()).count();
		
		addSummaryReportProps(BugHuntConstants.NO_OF_TESTS_PASSED, 
				String.valueOf(noOfStepsPassed), summaryMap);
		addSummaryReportProps(BugHuntConstants.NO_OF_TESTS_FAILED, 
				String.valueOf(noOfStepsFailed), summaryMap);
		
		LocalDateTime endTime = LocalDateTime.now();
	    long diffInSeconds = Duration.between(TestSession.getStartExecutionTime(), endTime).getSeconds();
		String executionTime = LocalTime.MIN.plusSeconds(diffInSeconds).toString();
		addSummaryReportProps(BugHuntConstants.EXECUTION_TIME, 
				executionTime, summaryMap);
		return summaryMap;
	}
	
	public synchronized void generateMultiConfigReport(Test test) {
		Object testObject = getMultiConfigReportObject(test);
		List<Test> tests = TestSession.getMultiConfigTestMap().get(test.getName());
		String reportName = tests.get(0).getDirPath() +"MultiConfig_" +CommonUtil.getShortFileName(test.getName()) + ".html";
		createReport(BugHuntConstants.MULTI_CONFIG_REPORT_TEMPLATE_NAME, testObject, reportName);
	}
	
	private Object getMultiConfigReportObject(Test test) {
		Map<String, Object> testObject = new HashMap<>();
		Map<String, Object> testMap = new HashMap<>();
		List<Test> tests = TestSession.getMultiConfigTestMap().get(test.getName());
		List<Map<String, Object>> multiConfigTests = new ArrayList<>();
		Map<String, Object> map = null;
		for(Test configTest: tests) {
			map = new HashMap<>();
			Map<String, String> subHeadingMap = new HashMap<>();
			subHeadingMap.put(BugHuntConstants.REPORT_LABEL, configTest.getParallelConfig().get(BugHuntConstants.REPORT_LABEL));
			subHeadingMap.put(BugHuntConstants.REPORT_VALUE, configTest.getParallelConfig().get(BugHuntConstants.REPORT_VALUE));
			map.put("subHeading", subHeadingMap);
			map.put("test", configTest);
			multiConfigTests.add(map);
		}
		List<Map<String, String>> props = new ArrayList<>();
		props.add(getMultiConfigProps(BugHuntConstants.TEST_NAME, test.getName()));
		props.add(getMultiConfigProps(BugHuntConstants.ENVIRONMENT, 
				BugHuntConfig.instance().getBugHuntProperty(BugHuntConstants.ENVIRONMENT)));
		
		List<String> reportVals = tests.stream().map(t->t.getParallelConfig().get("ReportValue")).collect(Collectors.toList());
		String reportVal = StringUtils.join(reportVals, ",");
		
		props.add(getMultiConfigProps(tests.get(0).getParallelConfig().get(BugHuntConstants.REPORT_LABEL),reportVal));
		
		int noOfStepsPassed = (int) tests.stream().mapToInt(t->t.getStepsPassed()).sum();
		int noOfStepsFailed = (int) tests.stream().mapToInt(t->t.getStepsPassed()).sum();
		
		props.add(getMultiConfigProps(BugHuntConstants.NO_OF_STEPS_PASSED,String.valueOf(noOfStepsPassed)));
		props.add(getMultiConfigProps(BugHuntConstants.NO_OF_STEPS_FAILED,String.valueOf(noOfStepsFailed)));
		boolean testFailed = tests.stream().anyMatch(t->"FAILED".equals(t.getOverAllStatus().toString()));
		String result = testFailed ? BugHuntConstants.FAIL : BugHuntConstants.PASS;
		props.add(getMultiConfigProps(BugHuntConstants.RESULT,result));
		testMap.put("configTests", multiConfigTests);
		testObject.put("reportHeadings", props);
		testObject.put("testObject", testMap);
		testObject.put("headerLabel", TestSession.getSummaryReportProps());
		return testObject;
	}
	
	private void failedTestsJSONReport(List<Test> testsCompleted) {
		boolean anyFailures = testsCompleted.stream().anyMatch(t->t.getOverAllStatus()==(OverALLStatus.FAILED));
		if(anyFailures) {
			JSONObject failedTestsObj = new JSONObject();
			JSONObject failedTestObj = null;
			JSONArray failedTestsArray = new JSONArray();
			List<Test> failedTests = testsCompleted.stream().filter(t->t.getOverAllStatus()==OverALLStatus.FAILED).collect(Collectors.toList());
			for (Test test : failedTests) {
				failedTestObj = new JSONObject();
				Map<String,String> props = test.getPropMap();
				for(Entry<String, String> entry:props.entrySet()) {
					failedTestObj.put(entry.getKey(), entry.getValue());
				}
				failedTestsArray.add(failedTestObj);
				// obj.put("name", "mkyong.com");
			}
			failedTestsObj.put("tests", failedTestsArray);
			String filePath = BugHuntConfig.instance().getBaseFWPath() + BugHuntConstants.SRC_MAIN_RESOURCES_PATH + 
					BugHuntConstants.FAILED_TESTS_JSON;
	        try (FileWriter file = new FileWriter(filePath)) {
	            file.write(failedTestsObj.toJSONString());
	            file.flush();

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
	}
	
	
	private void createReport(String templateName, Object testObject, String reportName) {
		final File templateDir = new File(BugHuntConfig.instance().getReportsTemplatePath());
		Mustache.Compiler c = Mustache.compiler().withLoader(new Mustache.TemplateLoader() {
    	    		public Reader getTemplate (String name) throws Exception {
    	    			return new FileReader(new File(templateDir, name));
    	    		}
		});
		String tmplName = String.format("{{>%s}}", templateName);
		String compiledHTML =  c.compile(tmplName).execute(testObject);
		File file = new File(reportName);
		try {
			FileUtils.writeStringToFile(file, compiledHTML.toString());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private Map<String, String> getMultiConfigProps(String label, String value) {
		Map<String, String> propMap = new HashMap<>();
		propMap.put("label", label);
		propMap.put("value",value);
		return propMap;
	}
	
	private class MultiConfigResult {
		int slNo;
		String name;
		String customColumnVal;
		boolean testPassed;
		public MultiConfigResult(int slNo, String name, String customColumnVal, boolean testPassed) {
			this.slNo = slNo;
			this.name = name;
			this.customColumnVal = customColumnVal;
			this.testPassed = testPassed;
		}
		public int getSlNo() {
			return slNo;
		}
		public String getName() {
			return name;
		}
		public String getCustomColumnVal() {
			return customColumnVal;
		}
		public boolean isTestPassed() {
			return testPassed;
		}
	}
}
