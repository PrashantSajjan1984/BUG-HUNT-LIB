package com.bughunt.reports;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	
	public void saveReport() {
		final File templateDir = new File(BugHuntConfig.instance().getReportsTemplatePath());
		Mustache.Compiler c = Mustache.compiler().withLoader(new Mustache.TemplateLoader() {
    	    		public Reader getTemplate (String name) throws Exception {
    	    			return new FileReader(new File(templateDir, name));
    	    		}
		});
		String tmplName = String.format("{{>%s}}", BugHuntConstants.REPORT_TEMPLATE_NAME);
		Map<String, Test> testObject = new HashMap<>();
		testObject.put("testObject", test);
		String compiledHTML =  c.compile(tmplName).execute(testObject);
		String reportName = test.getDirPath() + CommonUtil.getShortFileName(test.getName()) + ".html";
		if(ExecutionMode.PARALLELMULTICONFIG == TestSession.getExecutionMode()) {
			String prefix = CommonUtil.getParallelConfigPrefix(test);
			reportName = test.getDirPath() + prefix +CommonUtil.getShortFileName(test.getName()) + ".html";
		}
		File file = new File(reportName);
		try {
			FileUtils.writeStringToFile(file, compiledHTML.toString());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
