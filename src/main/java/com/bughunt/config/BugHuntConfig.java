package com.bughunt.config;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Properties;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.bughunt.constants.BugHuntConstants;
import com.bughunt.core.TestSession;
import com.bughunt.domain.StepResult;
import com.bughunt.util.CommonUtil;

public class BugHuntConfig {

	public static BugHuntConfig bugHuntConfig;
	private Properties bugHuntProps;
	private String baseFWPath;
	private String fwConfigPath;
	private String keywordPackagePath;
	private String reportsTemplatePath;
	private String reportsPath;
	private String dataPath;
	private String executionReportPath;
	private boolean configSet = false;
	private String envURL;
	private JSONObject urlJSONObj;
	
	private BugHuntConfig() {
		
	}
	
	public static BugHuntConfig instance() {
		if(bugHuntConfig == null) {
			bugHuntConfig = new BugHuntConfig();
		}
		return bugHuntConfig;
	}
	
	public String getBaseFWPath() {
		return baseFWPath;
	}
	
	public String getFWConfigPath() {
		return fwConfigPath;
	}

	public String getKeywordPackagePath() {
		return keywordPackagePath;
	}
	
	public String getReportsTemplatePath() {
		return reportsTemplatePath;
	}
	
	public String getReportsPath() {
		return reportsPath;
	}
	
	public String getDataPath() {
		return dataPath;
	}

	private void setBaseFWPath(String baseFWPath) {
		this.baseFWPath = baseFWPath;
	}

	private void setFWConfigPath(String fwConfigPath) {
		this.fwConfigPath = fwConfigPath;
	}

	private void setKeywordPackagePath(String keywordPackagePath) {
		this.keywordPackagePath = keywordPackagePath;
	}

	private void setReportsTemplatePath(String reportsTemplatePath) {
		this.reportsTemplatePath = reportsTemplatePath;
	}

	private void setReportsPath(String reportsPath) {
		this.reportsPath = reportsPath;
	}

	private void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}
	
	public String getExecutionReportPath() {
		return executionReportPath;
	}

	public void setExecutionReportPath(String executionReportPath) {
		this.executionReportPath = executionReportPath + "/"; 
	}

	public void setConfigPaths() {
		if(configSet) {
			return;
		}
		configSet = true;
		String basePath = CommonUtil.getFWRootPath();
		setBaseFWPath(basePath);
		setFWConfigPath(basePath + "/" + BugHuntConstants.FW_CONFIG_NAME);
		setFWProps();
		String codePath = CommonUtil.getClassLocation(getBugHuntProperty(BugHuntConstants.RUNNER_CLASS));
		String relKeywordPackage = getBugHuntProperty(BugHuntConstants.KEYWORDS_PACKAGE).replaceAll("\\.", "\\/");
		setKeywordPackagePath(codePath + relKeywordPackage);
		dataPath = basePath + BugHuntConstants.SRC_MAIN_RESOURCES_PATH + BugHuntConstants.DATA_PATH;
		reportsTemplatePath = basePath + BugHuntConstants.SRC_MAIN_RESOURCES_PATH + BugHuntConstants.REPORT_TEMPLATE_PATH;
		reportsPath = basePath + BugHuntConstants.SRC_MAIN_RESOURCES_PATH + BugHuntConstants.REPORT_PATH;
		setReportProps();
		setScreenShotEnum();
		setURLJsonObject();
	}
	
	

	private void setFWProps() {
		bugHuntProps = new Properties();
		try {
			FileInputStream configFile=new FileInputStream(fwConfigPath);
			bugHuntProps.load(configFile);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public String getBugHuntProperty(String propertyName) {
		String propertyVal ="";
		try {
			propertyVal = bugHuntProps.getProperty(propertyName).trim(); 
		} catch(Exception ex) {
			ex.printStackTrace();
		}
        return propertyVal;
	}
	
	private void setReportProps() {
		String reportProps = getBugHuntProperty(BugHuntConstants.REPORT_PROPERTIES);
		String[] propsSplit = reportProps.split(",");
		TestSession.setReportProps(new LinkedHashSet<>(Arrays.asList(propsSplit)));
	}
	
	private void setScreenShotEnum() {
		EnumSet<StepResult> screenShotList = EnumSet.noneOf(StepResult.class);
		if("true".equals(getBugHuntProperty(BugHuntConstants.PASS_SCREENSHOT).toLowerCase())) {
			screenShotList.add(StepResult.PASS);
		}
		if("true".equals(getBugHuntProperty(BugHuntConstants.FAIL_SCREENSHOT).toLowerCase())) {
			screenShotList.add(StepResult.FAIL);
		}
		if("true".equals(getBugHuntProperty(BugHuntConstants.WARNING_SCREENSHOT).toLowerCase())) {
			screenShotList.add(StepResult.WARNING);
		}
		TestSession.setScreenShotStepResults(screenShotList);
	}

	public String getEnvironmentURL() {
		if(null==envURL) {
			setEnvURL();
		}
		return envURL;
	}
	
	private void setURLJsonObject() {
		JSONParser parser = new JSONParser();
		try {
			urlJSONObj  =  (JSONObject) parser.parse(new FileReader(baseFWPath + BugHuntConstants.SRC_MAIN_RESOURCES_PATH + "Environment.json"));
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	private void setEnvURL() {
		String env = getBugHuntProperty("Environment");
		getEnvironmentURL(env);
		if(null!=urlJSONObj && urlJSONObj.containsKey(env)) {
			envURL = (String) urlJSONObj.get(env);
		} else {
			envURL = "";
		}
	}
	
	public String getEnvironmentURL(String environment) {
		String url = "";
		if(null!=urlJSONObj && urlJSONObj.containsKey(environment)) {
			url = (String) urlJSONObj.get(environment);
		} else {
			url = "";
		}
		return url;
	}
}
