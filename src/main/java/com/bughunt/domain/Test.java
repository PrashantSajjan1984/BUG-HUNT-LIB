package com.bughunt.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.bughunt.config.BugHuntConfig;
import com.bughunt.constants.BugHuntConstants;
import com.bughunt.core.TestSession;
import com.bughunt.util.CommonUtil;

public class Test {
	private int id;
	private String name;
	private int stepsPassed;
	private int stepsFailed;
	private int stepsWithWarning;
	private int stepNo;
	private List<Step> steps = null;
	private List<Map<String, String>> props = null;
	private OverALLStatus overAllStatus = OverALLStatus.NOT_STARTED;
	private List<MethodVO> keywords;
	private String dirPath;
	
	public Test(String name, int id, Map<String, String> propMap) {
		this.name = name;
		this.id = id;
		steps = new ArrayList<>();
		props = new ArrayList<>();
		
		addTCProps("Test Case Name", name);
		addTCProps(BugHuntConstants.ENVIRONMENT, 
				BugHuntConfig.instance().getBugHuntProperty(BugHuntConstants.ENVIRONMENT));
		setReportProps(propMap);
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public int getStepsPassed() {
		return stepsPassed;
	}

	public int getStepsFailed() {
		return stepsFailed;
	}
	
	public int getStepsWithWarning() {
		return stepsWithWarning;
	}
	
	public OverALLStatus getOverAllStatus() {
		return overAllStatus;
	}

	private void setOverAllStatus(OverALLStatus overAllStatus) {
		this.overAllStatus = overAllStatus;
	}
	
	public List<MethodVO> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<MethodVO> keywords) {
		this.keywords = keywords;
	}
	
	private void addTCProps(String label, String value) {
		Map<String, String> propMap = new HashMap<>();
		propMap.put("label", label);
		propMap.put("value",value);
		props.add(propMap);
	}
	
	public void addTestStep(String desc, String actualResult, StepResult result, Optional<String> screenShotPath) {
		Step step = new Step();
		step.setStepNo(++stepNo);
		step.setDesc(desc);
		step.setActualResult(actualResult);
		
		switch(result) {
		case FAIL: step.setStepFailed(true);
			break;
		case PASS: step.setStepPassed(true);
			break;
		case WARNING: step.setStepWithWarning(true);
			break;
		}
		
		if(screenShotPath.isPresent()) {
			step.setScreenShotPath(screenShotPath.get());
		}
		
		steps.add(step);
	}
	
	private void setReportProps(Map<String, String> propMap) {
		for(String prop: TestSession.getReportProps()) {
			if(propMap.containsKey(prop) && StringUtils.isNotBlank(propMap.get(prop))) {
				addTCProps(prop, propMap.get(prop));
			}
		}
	}
	
	public String getDirPath() {
		return dirPath + "/";
	}

	public void setExecutionStatus() {
		stepsPassed = (int) steps.stream().filter(s->s.isStepPassed()).count();
		stepsFailed = (int) steps.stream().filter(s->s.isStepFailed()).count();
		stepsWithWarning = (int) steps.stream().filter(s->s.isStepWithWarning()).count();
		addTCProps("No of Steps Passed", String.valueOf(stepsPassed));
		addTCProps("No of Steps Failed", String.valueOf(stepsFailed));
		if(stepsWithWarning > 0) {
			addTCProps("No of Steps with Warnings", String.valueOf(stepsWithWarning));
		}
		addTCProps("Execution Time", String.valueOf(stepsWithWarning));
		if(stepsFailed == 0) {
			setOverAllStatus(OverALLStatus.PASSED);
			addTCProps("Result", "PASS");
		} else {
			setOverAllStatus(OverALLStatus.FAILED);
			addTCProps("Result", "FAIL");
		}
	}
	
	public void createReportFolder() {
		String reportPath = BugHuntConfig.instance().getExecutionReportPath();
		dirPath = reportPath + id + "_" + CommonUtil.getShortFileName(name);
		Path dir = Paths.get(dirPath);
		try {
			if(!Files.exists(dir)) {
				Files.createDirectory(dir);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private class Step {
		private int stepNo;
		private String desc;
		private String actualResult;
		private boolean stepPassed;
		private boolean stepFailed;
		private boolean stepWithWarning;
		private String screenShotPath;
		
		public boolean isStepPassed() {
			return stepPassed;
		}
		
		public void setStepPassed(boolean stepPassed) {
			this.stepPassed = stepPassed;
		}
		
		public boolean isStepFailed() {
			return stepFailed;
		}
		
		public void setStepFailed(boolean stepFailed) {
			this.stepFailed = stepFailed;
		}
		
		public boolean isStepWithWarning() {
			return stepWithWarning;
		}
		
		public void setStepWithWarning(boolean stepWithWarning) {
			this.stepWithWarning = stepWithWarning;
		}
		
		public int getStepNo() {
			return stepNo;
		}
		
		public void setStepNo(int stepNo) {
			this.stepNo = stepNo;
		}
		
		public String getDesc() {
			return desc;
		}
		
		public void setDesc(String desc) {
			this.desc = desc;
		}
		
		public String getActualResult() {
			return actualResult;
		}
		
		public void setActualResult(String actualResult) {
			this.actualResult = actualResult;
		}

		public String getScreenShotPath() {
			return screenShotPath;
		}

		public void setScreenShotPath(String screenShotPath) {
			this.screenShotPath = screenShotPath;
		}
	}
	
	private enum OverALLStatus {
		NOT_STARTED, INPROGRESS, PASSED, FAILED
	}

	@Override
	public String toString() {
		return "Test [id=" + id + ", name=" + name + ", stepsPassed=" + stepsPassed + ", stepsFailed=" + stepsFailed
				+ ", stepsWithWarning=" + stepsWithWarning + ", stepNo=" + stepNo + ", steps=" + steps + ", props="
				+ props + ", overAllStatus=" + overAllStatus + ", keywords=" + keywords + "]";
	}
	
}
