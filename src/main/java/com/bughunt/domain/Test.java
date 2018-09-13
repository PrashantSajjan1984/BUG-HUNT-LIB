package com.bughunt.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

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
	
	public Test(String name, int id, Map<String, String> propMap) {
		this.name = name;
		this.id = id;
		steps = new ArrayList<>();
		props = new ArrayList<>();
		
		addTCProps("TestCase name", name);
		
		for(Entry<String, String> mapProps : propMap.entrySet()) {
			addTCProps(mapProps.getKey(), mapProps.getValue());
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
	
	public void setExecutionStatus() {
		stepsPassed = (int) steps.stream().filter(s->s.isStepPassed()).count();
		stepsFailed = (int) steps.stream().filter(s->s.isStepFailed()).count();
		stepsWithWarning = (int) steps.stream().filter(s->s.isStepWithWarning()).count();
		
		if(stepsFailed == 0) {
			setOverAllStatus(OverALLStatus.PASSED);
			addTCProps("Result", "PASS");
		} else {
			setOverAllStatus(OverALLStatus.FAILED);
			addTCProps("Result", "FAIL");
		}
		
		addTCProps("No of steps passed", String.valueOf(stepsPassed));
		addTCProps("No of steps failed", String.valueOf(stepsFailed));
		addTCProps("Execution Time", String.valueOf(stepsWithWarning));
	}
	
	public OverALLStatus getOverAllStatus() {
		return overAllStatus;
	}

	private void setOverAllStatus(OverALLStatus overAllStatus) {
		this.overAllStatus = overAllStatus;
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
				+ props + ", overAllStatus=" + overAllStatus + "]";
	}
	
	
}
