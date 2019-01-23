package com.bughunt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.bughunt.constants.BugHuntConstants;
import com.bughunt.core.TestSession;
import com.bughunt.domain.ExecutionMode;
import com.bughunt.domain.StepResult;
import com.bughunt.util.CommonUtil;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BugHuntConfig {

	public  static BugHuntConfig bugHuntConfig;
	private static Properties bugHuntProps;
	private static String baseFWPath;
	private static String fwConfigPath;
	private static String keywordPackagePath;
	private static String reportsTemplatePath;
	private static String reportsPath;
	private static String dataPath;
	private static String executionReportPath;
	private static boolean configSet = false;
	private static String envURL;
	private static JSONObject urlJSONObj;
	private static List<Map<String, String>> parallelConfig;
	
	
	
	public static  String getBaseFWPath() {
		return baseFWPath;
	}
	
	public static  String getFWConfigPath() {
		return fwConfigPath;
	}

	public static  String getKeywordPackagePath() {
		return keywordPackagePath;
	}
	
	public static  String getReportsTemplatePath() {
		return reportsTemplatePath;
	}
	
	public static  String getReportsPath() {
		return reportsPath;
	}
	
	public static  String getDataPath() {
		return dataPath;
	}

	private static void setBaseFWPath(String baseFWPath) {
		BugHuntConfig.baseFWPath = baseFWPath;
	}

	private static void setFWConfigPath(String fwConfigPath) {
		BugHuntConfig.fwConfigPath = fwConfigPath;
	}

	private static void setKeywordPackagePath(String keywordPackagePath) {
		BugHuntConfig.keywordPackagePath = keywordPackagePath;
	}

	private static void setReportsTemplatePath(String reportsTemplatePath) {
		BugHuntConfig.reportsTemplatePath = reportsTemplatePath;
	}

	private static void setReportsPath(String reportsPath) {
		BugHuntConfig.reportsPath = reportsPath;
	}

	private static void setDataPath(String dataPath) {
		BugHuntConfig.dataPath = dataPath;
	}
	
	public static  String getExecutionReportPath() {
		return BugHuntConfig.executionReportPath;
	}

	public static  void setExecutionReportPath(String executionReportPath) {
		BugHuntConfig.executionReportPath = executionReportPath + "/"; 
	}

	public static  void setConfigPaths() {
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
		setCurrentExecutionMode();
		setReportProps();
		setSummaryReportProps();
		setScreenShotEnum();
		setURLJsonObject();
		setReRunCount();
	}

	private static void setFWProps() {
		bugHuntProps = new Properties();
		try {
			FileInputStream configFile=new FileInputStream(fwConfigPath);
			bugHuntProps.load(configFile);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static  String getBugHuntProperty(String propertyName) {
		String propertyVal ="";
		try {
			propertyVal = bugHuntProps.getProperty(propertyName).trim(); 
		} catch(Exception ex) {
			ex.printStackTrace();
		}
        return propertyVal;
	}
	
	private static void setReportProps() {
		String reportProps = null;
		switch(TestSession.getExecutionMode()) {
		case PARALLEL:
		case SEQUENTIAL:
			reportProps = getBugHuntProperty(BugHuntConstants.REPORT_PROPERTIES);
			break;
		case PARALLELDEVICECONFIG:
		case PARALLELMULTICONFIG:
			reportProps = getBugHuntProperty(BugHuntConstants.PARALLEL_MULTI_CONFIG_PROPS);
			break;
		default:
			reportProps = getBugHuntProperty(BugHuntConstants.REPORT_PROPERTIES);
			break;
		}
		String[] propsSplit = reportProps.split(",");
		TestSession.setReportProps(new LinkedHashSet<>(Arrays.asList(propsSplit)));
	}
	
	private static void setSummaryReportProps() {
		String reportProps = null;
		reportProps = getBugHuntProperty(BugHuntConstants.SUMMARY_REPORT_PROPERTIES);
		String[] propsSplit = reportProps.split(",");
		TestSession.setSummaryReportProps(new LinkedHashSet<>(Arrays.asList(propsSplit)));
	}
	
	private static void setScreenShotEnum() {
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

	public static  String getEnvironmentURL() {
		if(null==envURL) {
			setEnvURL();
		}
		return envURL;
	}
	
	private static void setURLJsonObject() {
		JSONParser parser = new JSONParser();
		try {
			urlJSONObj  =  (JSONObject) parser.parse(new FileReader(baseFWPath + BugHuntConstants.SRC_MAIN_RESOURCES_PATH + "Environment.json"));
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	private static void setEnvURL() {
		String env = getBugHuntProperty("Environment");
		getEnvironmentURL(env);
		if(null!=urlJSONObj && urlJSONObj.containsKey(env)) {
			envURL = (String) urlJSONObj.get(env);
		} else {
			envURL = "";
		}
	}
	
	public static  String getEnvironmentURL(String environment) {
		String url = "";
		if(null!=urlJSONObj && urlJSONObj.containsKey(environment)) {
			url = (String) urlJSONObj.get(environment);
		} else {
			url = "";
		}
		return url;
	}
	
	public static  void setCurrentExecutionMode() {
		String executionMode = getBugHuntProperty(BugHuntConstants.EXECUTION_MODE);
		ExecutionMode mode = ExecutionMode.valueOf(executionMode.toUpperCase());
		TestSession.setExecutionMode(mode);
	}
	
	private static void setReRunCount() {
		String reRunCountVal = getBugHuntProperty(BugHuntConstants.RE_RUN_COUNT);
		int reRunCount = 0;
		try {
			reRunCount = Integer.parseInt(reRunCountVal);
		} catch(NumberFormatException ex) {
			ex.printStackTrace();
		}
		TestSession.setReRunCount(reRunCount);
	}
	
	public static  List<Map<String,String>> getParallelConfigMap() {
		if(parallelConfig == null) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				String configFileName = "ParallelConfig.json";
				if(ExecutionMode.PARALLELDEVICECONFIG == TestSession.getExecutionMode()) {
					configFileName = "ParallelConfig_Device.json";
				}
			    File file = new File(baseFWPath + BugHuntConstants.SRC_MAIN_RESOURCES_PATH + configFileName);
			    String json = FileUtils.readFileToString(file);
			    parallelConfig = mapper.readValue(json, new TypeReference<List<Map<String, String>>>(){});
			    int id = 0;
			    for(Map<String,String> map:parallelConfig) {
			    		map.put(BugHuntConstants.ID, String.valueOf(++id));
			    }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return parallelConfig;
	}
	
	public static  List<Map<String,String>> setParallelDeviceGroupID(List<Map<String,String>> parallelConfigs) {
		int min = 8;
		int max =100;
		int index = 0;
		int randomNum;
		String groupID = "";
		for (Map<String, String> map : parallelConfigs) {
			index++;
			if(!map.containsKey(BugHuntConstants.GROUP_ID)) {
				randomNum = getRandomNumberInRange(min, max);
				groupID =BugHuntConstants.GROUP_LABEL + String.format("_%d_%d",randomNum, index);
				map.put(BugHuntConstants.GROUP_ID, groupID);
			}
		}
		
		//Remove additional devices if no of tests is less than grouped devices
		Set<String> distinctGroupIDs = parallelConfigs.stream().map(t->t.get(BugHuntConstants.GROUP_ID)).collect(Collectors.toSet());
		int noOfTestCases = TestSession.getTestCases().size();
		for(String group: distinctGroupIDs) {
			List<Map<String,String>> groupConfigs = parallelConfigs.stream().filter(t->group.equals(t.get(BugHuntConstants.GROUP_ID))).collect(Collectors.toList());
			long count  = groupConfigs.size();
			if(count > noOfTestCases) {
				for(int i = noOfTestCases;i<count;i++) {
					parallelConfigs.remove(groupConfigs.get(i));
				}
			}
		}
		
		return parallelConfigs;
	}
	
	private static int getRandomNumberInRange(int min, int max) {
		
		Random r = new Random();
		return r.ints(min, (max + 1)).findFirst().getAsInt();
	}
}
