package com.bughunt.reports;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.bughunt.config.BugHuntConfig;
import com.bughunt.constants.BugHuntConstants;
import com.bughunt.core.TestSession;
import com.bughunt.domain.Test;
import com.bughunt.util.CommonUtil;
import com.samskivert.mustache.Mustache;

public class SummaryReport {

	public synchronized void generateReport(Test test) {
		final File templateDir = new File(BugHuntConfig.instance().getReportsTemplatePath());
		Mustache.Compiler c = Mustache.compiler().withLoader(new Mustache.TemplateLoader() {
    	    		public Reader getTemplate (String name) throws Exception {
    	    			return new FileReader(new File(templateDir, name));
    	    		}
		});
		String tmplName = String.format("{{>%s}}", BugHuntConstants.SUMMARY_REPORT_TEMPLATE_NAME);
		Map<String, Object> testObject = new HashMap<>();
		Map<String, List<Test>> testMap = new HashMap<>();
		testMap.put("tests", TestSession.getTestCases());
		testObject.put("testObject", testMap);
		testObject.put("headerLabels", TestSession.getSummaryReportProps());
		String compiledHTML =  c.compile(tmplName).execute(testObject);
		String reportName = BugHuntConfig.instance().getExecutionReportPath() + "SummaryReport.html";
		File file = new File(reportName);
		try {
			FileUtils.writeStringToFile(file, compiledHTML.toString());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}
	
	public synchronized void generateParallelConfigSummaryReport(Test test) {
		final File templateDir = new File(BugHuntConfig.instance().getReportsTemplatePath());
		Mustache.Compiler c = Mustache.compiler().withLoader(new Mustache.TemplateLoader() {
    	    		public Reader getTemplate (String name) throws Exception {
    	    			return new FileReader(new File(templateDir, name));
    	    		}
		});
		String tmplName = String.format("{{>%s}}", BugHuntConstants.MULTI_CONFIG_SUMMARY_REPORT_TEMPLATE_NAME);
		Map<String, Object> testObject = new HashMap<>();
		Map<String, Object> testMap = new HashMap<>();
		List<MultiConfigResult> reportObject = getMultiConfigReportObject(test);
		testMap.put("tests", reportObject);
		testObject.put("testObject", testMap);
		testObject.put("headerLabel", TestSession.getSummaryReportProps());
		String compiledHTML =  c.compile(tmplName).execute(testObject);
		String reportName = BugHuntConfig.instance().getExecutionReportPath() + "MultiConfigSummaryReport.html";
		File file = new File(reportName);
		try {
			FileUtils.writeStringToFile(file, compiledHTML.toString());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}

	private List<MultiConfigResult> getMultiConfigReportObject(Test test) {
		List<MultiConfigResult> testResults = new ArrayList<>();
		MultiConfigResult result = null;
		int slNo = 0;
		for(Entry<String, List<Test>> entry:TestSession.getMultiConfigTestMap().entrySet()) {
			List<Test> tests = entry.getValue();
			System.out.println(tests.get(0).getParallelConfig());
			List<String> reportVals = tests.stream().map(t->t.getParallelConfig().get(BugHuntConstants.REPORT_VALUE)).collect(Collectors.toList());
			String reportVal = StringUtils.join(reportVals, ",");
			boolean testFailed = tests.stream().anyMatch(t->"FAILED".equals(t.getOverAllStatus().toString()));
			result = new MultiConfigResult(++slNo, test.getName(), reportVal, !testFailed );
			testResults.add(result);
		}
		return testResults;
	}
	
	public synchronized void generateMultiConfigReport(Test test) {
		final File templateDir = new File(BugHuntConfig.instance().getReportsTemplatePath());
		Mustache.Compiler c = Mustache.compiler().withLoader(new Mustache.TemplateLoader() {
    	    		public Reader getTemplate (String name) throws Exception {
    	    			return new FileReader(new File(templateDir, name));
    	    		}
		});
		String tmplName = String.format("{{>%s}}", BugHuntConstants.MULTI_CONFIG_REPORT_TEMPLATE_NAME);
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
		String compiledHTML =  c.compile(tmplName).execute(testObject);
		String reportName = tests.get(0).getDirPath() +"MultiConfig_" +CommonUtil.getShortFileName(test.getName()) + ".html";
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
