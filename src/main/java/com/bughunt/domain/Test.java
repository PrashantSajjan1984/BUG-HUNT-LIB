package com.bughunt.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
	private List<Map<String, Object>> multiIterationMaps = null;
	private List<List<Step>> multiIterationSteps = null;
	private List<Map<String, String>> props = null;
	private List<Map<String, String>> summaryProps = null;
	private OverALLStatus overAllStatus = OverALLStatus.NOT_STARTED;
	private List<MethodVO> keywords;
	private String dirPath;
	private boolean isMultiIteration;
	private int totalIteration;
	private int currentIteration;
	private boolean runMultiIteration = true;
	private Map<String, String> propMap = null;
	private Map<String, String> parallelConfig = null;
	private int slNo = 0;
	private LocalDateTime startTime;
	private LocalDateTime endTime; 
	private String executionTime;
	private String folderName;
	private String summaryRptLink;
	private int reRunCount = 0;
	
	
	public Test(String name, int id, Map<String, String> propMap) {
		this.name = name;
		this.id = id;
		this.propMap = propMap;
		props = new ArrayList<>();
		summaryProps = new ArrayList<>();
		addTCProps(BugHuntConstants.TEST_NAME, name);
		addTCProps(BugHuntConstants.ENVIRONMENT, 
				BugHuntConfig.getBugHuntProperty(BugHuntConstants.ENVIRONMENT));
		setReportProps(propMap);
		setSummaryReportProps(propMap);
		if(propMap.containsKey("RunIterations") && "No".equals(propMap.get("propMap"))) {
			runMultiIteration = false;
		}
	}
	
	public Test(Test test, Map<String, String> parallelConfig) {
		this.name = test.getName();
		this.id = test.getId();
		this.propMap = test.getPropMap();
		this.parallelConfig = parallelConfig;
		this.keywords = test.getKeywords();
		this.dirPath = test.getDirPath();
		this.isMultiIteration = test.isMultiIteration;
		this.totalIteration = test.getTotalIteration();
		this.runMultiIteration = test.isRunMultiIteration();
		props = new ArrayList<>();
		summaryProps = new ArrayList<>();
		addTCProps("Test Case Name", name);
		addTCProps(BugHuntConstants.ENVIRONMENT, 
				BugHuntConfig.getBugHuntProperty(BugHuntConstants.ENVIRONMENT));
		setReportProps(propMap);
		setSummaryReportProps(propMap);
		if(propMap.containsKey("RunIterations") && "No".equals(propMap.get("propMap"))) {
			runMultiIteration = false;
		}
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
	
	public int getTotalIteration() {
		return totalIteration;
	}

	public int getCurrentIteration() {
		return currentIteration;
	}

	public void setCurrentIteration(int currentIteration) {
		this.currentIteration = currentIteration;
		steps = new ArrayList<>();
		stepNo = 0;
	}

	public boolean isRunMultiIteration() {
		return runMultiIteration;
	}

	public void setTotalIteration(int totalIteration) {
		this.totalIteration = totalIteration;
		if(totalIteration > 1) {
			multiIterationMaps = new ArrayList<>();
			multiIterationSteps = new ArrayList<>();
			isMultiIteration = true;
		}
	}
	
	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime() {
		this.startTime = LocalDateTime.now();
	}

	public Map<String, String> getPropMap() {
		return propMap;
	}
	
	public void setParallelConfig(Map<String, String> parallelConfig) {
		this.parallelConfig = parallelConfig;
	}
	
	public Map<String, String> getParallelConfig() {
		return parallelConfig;
	}

	public List<Map<String, String>> getProps() {
		return props;
	}
	
	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	
	public String getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(String executionTime) {
		this.executionTime = executionTime;
	}
	
	public String getFolderName() {
		return folderName;
	}

	public String getSummaryRptLink() {
		return summaryRptLink;
	}

	public void setSummaryRptLink(String summaryRptLink) {
		this.summaryRptLink = summaryRptLink;
	}

	public int getSlNo() {
		return slNo;
	}

	public void setSlNo(int slNo) {
		this.slNo = slNo;
	}
	
	public int getReRunCount() {
		return reRunCount;
	}

	public void setReRunCount(int reRunCount) {
		this.reRunCount = reRunCount;
	}

	private void addTCProps(String label, String value) {
		Map<String, String> propMap = new HashMap<>();
		propMap.put(BugHuntConstants.LABEL, label);
		propMap.put(BugHuntConstants.VALUE,value);
		props.add(propMap);
	}
	
	private void addSummaryReportProps(String label, String value) {
		Map<String, String> propMap = new HashMap<>();
		propMap.put(BugHuntConstants.LABEL, label);
		propMap.put(BugHuntConstants.VALUE,value);
		summaryProps.add(propMap);
	}
	
	public void addStepsAfterIteration() {
		if(isMultiIteration) {
			Map<String, Object> stepsMap = new LinkedHashMap<>();
			stepsMap.put("CurrentIteration", currentIteration);
			stepsMap.put("steps", steps);
			multiIterationMaps.add(stepsMap);
			multiIterationSteps.add(steps);
		}
	}
	
	public void setInProgressStatus() {
		this.overAllStatus = OverALLStatus.INPROGRESS;
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
			if((ExecutionMode.PARALLELMULTICONFIG != TestSession.getExecutionMode() && ExecutionMode.PARALLELDEVICECONFIG != TestSession.getExecutionMode()) || 
					null == parallelConfig) {
				if(propMap.containsKey(prop) && StringUtils.isNotBlank(propMap.get(prop))
						) {
					addTCProps(prop, propMap.get(prop));
				} 
			} else {
				if(parallelConfig.containsKey(prop) && StringUtils.isNotBlank(parallelConfig.get(prop))) {
					addTCProps(prop, parallelConfig.get(prop));
				} else if(propMap.containsKey(prop) && StringUtils.isNotBlank(propMap.get(prop))) {
					addTCProps(prop, propMap.get(prop));
				} 
			}
		}
	}
	
	private void setSummaryReportProps(Map<String, String> propMap) {
		for(String prop: TestSession.getSummaryReportProps()) {
			if((ExecutionMode.PARALLELMULTICONFIG != TestSession.getExecutionMode() &&
					ExecutionMode.PARALLELDEVICECONFIG != TestSession.getExecutionMode()) && null == parallelConfig) {
				if(propMap.containsKey(prop) && StringUtils.isNotBlank(propMap.get(prop))
						) {
					addSummaryReportProps(prop, propMap.get(prop));
				} 
			} else {
				if(parallelConfig!=null && parallelConfig.containsKey(prop) && StringUtils.isNotBlank(parallelConfig.get(prop))) {
					addSummaryReportProps(prop, parallelConfig.get(prop));
				} else if(propMap.containsKey(prop) && StringUtils.isNotBlank(propMap.get(prop))) {
					addSummaryReportProps(prop, propMap.get(prop));
				} 
			}
		}
	}
	
	public String getDirPath() {
		return dirPath + "/";
	}

	public void setExecutionStatus() {
		if(!isMultiIteration) {
			setIterationStatus();
		} else {
			setMultiIterationStatus();
		}
		addTCProps(BugHuntConstants.NO_OF_STEPS_PASSED, String.valueOf(stepsPassed));
		addTCProps(BugHuntConstants.NO_OF_STEPS_FAILED, String.valueOf(stepsFailed));
		if(stepsWithWarning > 0) {
			addTCProps(BugHuntConstants.NO_OF_STEPS_WITH_WARNINGS, String.valueOf(stepsWithWarning));
		}
		addTCProps(BugHuntConstants.EXECUTION_TIME, String.valueOf(stepsWithWarning));
		if(reRunCount > 0) {
			addTCProps(BugHuntConstants.RE_RUN_COUNT_LABEL, String.valueOf(reRunCount));
		}
		
		if(stepsFailed == 0) {
			setOverAllStatus(OverALLStatus.PASSED);
			addTCProps(BugHuntConstants.RESULT, BugHuntConstants.PASS);
		} else {
			setOverAllStatus(OverALLStatus.FAILED);
			addTCProps(BugHuntConstants.RESULT, BugHuntConstants.FAIL);
		}
	}

	private void setIterationStatus() {
		stepsPassed = (int) steps.stream().filter(s->s.isStepPassed()).count();
		stepsFailed = (int) steps.stream().filter(s->s.isStepFailed()).count();
		stepsWithWarning = (int) steps.stream().filter(s->s.isStepWithWarning()).count();
	}
	
	private void setMultiIterationStatus() {
		stepsPassed = multiIterationSteps.stream().map(l->l.stream().filter(s->s.isStepPassed()).count()).mapToInt(Long::intValue).sum();
		stepsFailed = multiIterationSteps.stream().map(l->l.stream().filter(s->s.isStepFailed()).count()).mapToInt(Long::intValue).sum();
		stepsWithWarning = multiIterationSteps.stream().map(l->l.stream().filter(s->s.isStepWithWarning()).count()).mapToInt(Long::intValue).sum();
		addTCProps("No of Iterations", String.valueOf(totalIteration));
	}
	
	public void createReportFolder() {
		String reportPath = BugHuntConfig.getExecutionReportPath();
		if(BugHuntConstants.PARALLEL_DEVICE_CONFIG.toLowerCase().equals(
				BugHuntConfig.getBugHuntProperty(BugHuntConstants.EXECUTION_MODE).toLowerCase())) {
			reportPath = reportPath + parallelConfig.get(BugHuntConstants.REPORT_VALUE) + "/";
		}
		folderName = id + "_" + CommonUtil.getShortFileName(name); 
		dirPath = reportPath + folderName;
		Path dir = Paths.get(dirPath);
		try {
			if(!Files.exists(dir)) {
				Files.createDirectories(dir);
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

		@Override
		public String toString() {
			return "Step [stepNo=" + stepNo + ", desc=" + desc + ", actualResult=" + actualResult + ", stepPassed="
					+ stepPassed + ", stepFailed=" + stepFailed + ", stepWithWarning=" + stepWithWarning
					+ ", screenShotPath=" + screenShotPath + "]";
		}
	}
	
	public enum OverALLStatus {
		NOT_STARTED, INPROGRESS, PASSED, FAILED
	}

	@Override
	public String toString() {
		return "Test [id=" + id + ", name=" + name + ", stepsPassed=" + stepsPassed + ", stepsFailed=" + stepsFailed
				+ ", stepsWithWarning=" + stepsWithWarning + ", stepNo=" + stepNo + ", steps=" + steps
				+ ", multiIterationMaps=" + multiIterationMaps + ", multiIterationSteps=" + multiIterationSteps
				+ ", props=" + props + ", summaryProps=" + summaryProps + ", overAllStatus=" + overAllStatus
				+ ", keywords=" + keywords + ", dirPath=" + dirPath + ", isMultiIteration=" + isMultiIteration
				+ ", totalIteration=" + totalIteration + ", currentIteration=" + currentIteration
				+ ", runMultiIteration=" + runMultiIteration + ", propMap=" + propMap + ", parallelConfig="
				+ parallelConfig + ", slNo=" + slNo + ", startTime=" + startTime + ", endTime=" + endTime
				+ ", executionTime=" + executionTime + ", folderName=" + folderName + ", summaryRptLink="
				+ summaryRptLink + "]";
	}
	
}
