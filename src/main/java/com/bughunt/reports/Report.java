package com.bughunt.reports;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;

import com.bughunt.config.BugHuntConfig;
import com.bughunt.constants.BugHuntConstants;
import com.bughunt.core.TestSession;
import com.bughunt.domain.ExecutionMode;
import com.bughunt.domain.ScreenShot;
import com.bughunt.domain.StepResult;
import com.bughunt.domain.Test;
import com.bughunt.domain.VideoCapture;
import com.bughunt.util.CommonUtil;
import com.samskivert.mustache.Mustache;

public class Report {
	private Test test;
	ScreenShot screenShot = null;
	int screenShotNum = 0;
	String screenShotPath;
	public Report(Test test) {
		this.test = test;
		setScreenShotInstanceAndFolder();
	}
	
	public void addReportStep(String description, String actualDesc, StepResult stepResult) {
		if(screenShot!=null) {
			String screenShotFile = screenShotPath + String.format("ScreenShot%d.jpg", ++screenShotNum);
			if(ExecutionMode.PARALLELMULTICONFIG == TestSession.getExecutionMode()) {
				String prefix = CommonUtil.getParallelConfigPrefix(test);
				screenShotFile = screenShotPath + prefix + String.format("ScreenShot%d.jpg", ++screenShotNum);
			}
			if(TestSession.getScreenShotStepResults().contains(stepResult)) {
				screenShot.takeScreenShot(screenShotFile);
				test.addTestStep(description, actualDesc, stepResult, Optional.of(screenShotFile));
			} else {
				test.addTestStep(description, actualDesc, stepResult, Optional.ofNullable(null));
			}
		} else {
			test.addTestStep(description, actualDesc, stepResult, Optional.ofNullable(null));
		}
	}
	
	private void setScreenShotInstanceAndFolder() {
		String screenShotClass = BugHuntConfig.instance().getBugHuntProperty(BugHuntConstants.SCREEN_SHOT_CLASS);
		try {
			screenShot = (ScreenShot) Class.forName(screenShotClass).newInstance();
			screenShotPath = test.getDirPath() + BugHuntConstants.SCREEN_SHOT_PATH;
			Path dir = Paths.get(screenShotPath);
			try {
				if(!Files.exists(dir)) {
					Files.createDirectories(dir);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	private String getVideoURL() {
		String videoCaptureClass = BugHuntConfig.instance().getBugHuntProperty(BugHuntConstants.VIDEO_CAPTURE_CLASS);
		VideoCapture videoCapture = null;
		String videoURL = null;
		try {
			if(ExecutionMode.SEQUENTIAL != TestSession.getExecutionMode()) {
				videoCapture = (VideoCapture) Class.forName(videoCaptureClass).newInstance();
				videoURL = videoCapture.getSauceVideoUrl();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return videoURL;
	}
	
	public void saveReport() {
		final File templateDir = new File(BugHuntConfig.instance().getReportsTemplatePath());
		Mustache.Compiler c = Mustache.compiler().withLoader(new Mustache.TemplateLoader() {
    	    		public Reader getTemplate (String name) throws Exception {
    	    			return new FileReader(new File(templateDir, name));
    	    		}
		});
		LocalDateTime endTime = LocalDateTime.now();
	    long diffInSeconds = Duration.between(test.getStartTime(), endTime).getSeconds();
		String executionTime = LocalTime.MIN.plusSeconds(diffInSeconds).toString();
		test.setEndTime(endTime);
		test.setExecutionTime(executionTime);
		Optional<Map<String, String>> optionalExecTimeMap = test.getProps().stream().
				filter(t->t.get(BugHuntConstants.LABEL).equals(BugHuntConstants.EXECUTION_TIME)).findFirst();
		if(optionalExecTimeMap.isPresent()) {
			Map<String, String> execTimeMap = optionalExecTimeMap.get();
			execTimeMap.put(BugHuntConstants.VALUE, executionTime);
		}
		
		Map<String, Object> reportObject = new HashMap<>();
		// Map<String, Test> testObject = new HashMap<>();
		reportObject.put("testObject", test);
		
		if("true".equals(BugHuntConfig.instance().getBugHuntProperty(BugHuntConstants.INTEGRATE_VIDEO))) {
			String videoURL = getVideoURL();
			if(null != videoURL) {
				reportObject.put("integrateVideo", true);
				reportObject.put("videoURL", videoURL);
			}
		}
		String tmplName = String.format("{{>%s}}", BugHuntConstants.REPORT_TEMPLATE_NAME);
		String compiledHTML =  c.compile(tmplName).execute(reportObject);
		String testRptShortName = CommonUtil.getShortFileName(test.getName()) + ".html";
		String reportName = test.getDirPath() + testRptShortName;
		String summaryRptLink = test.getFolderName() + "/" + testRptShortName;
		if(ExecutionMode.PARALLELMULTICONFIG == TestSession.getExecutionMode()) {
			String prefix = CommonUtil.getParallelConfigPrefix(test);
			reportName = test.getDirPath() + prefix + testRptShortName;
			summaryRptLink = prefix + testRptShortName;
		}
		
		test.setSummaryRptLink(summaryRptLink);
		
		File file = new File(reportName);
		try {
			FileUtils.writeStringToFile(file, compiledHTML.toString());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
