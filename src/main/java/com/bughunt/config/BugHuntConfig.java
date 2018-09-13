package com.bughunt.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.bughunt.constants.BugHuntConstants;
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
	private boolean configSet = false;
	
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
		System.out.println(keywordPackagePath);
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
}
