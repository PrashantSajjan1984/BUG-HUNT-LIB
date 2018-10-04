package com.bughunt.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.bughunt.constants.BugHuntConstants;
import com.bughunt.domain.ExecutionMode;
import com.bughunt.domain.MethodVO;
import com.bughunt.domain.ParameterVO;
import com.bughunt.domain.Test;
import com.bughunt.reports.Report;
import com.bughunt.util.DataUtil;

public class KeywordTestExecutor extends Executor {

	@Override
	protected void callTestMethods(Test test) {
		Map<String, Object> instanceMap = null;
		Object keywordObj = null;
		test.createReportFolder();
		Report report = new Report(test);
		DataUtil dataUtil = new DataUtil(test);
		int totalIteration = test.isRunMultiIteration() ? dataUtil.getTotalIteration() : 1;
		test.setTotalIteration(totalIteration);
		callBeforeAfterMethods(BugHuntConstants.BEFORE_ANNOTATION, test, report, dataUtil);
		
		for(int i=1; i<=totalIteration;i++) {
			instanceMap = new HashMap<>();
			dataUtil.setIteration(i);
			test.setCurrentIteration(i);
			for(MethodVO methodVO: test.getKeywords()) {
				dataUtil.setKeyword(methodVO.getName());
				try {
					if(!instanceMap.containsKey(methodVO.getClassName())) {
						keywordObj = getKeywordClassInstance(test, report, dataUtil, methodVO);
						instanceMap.put(methodVO.getClassName(), keywordObj);
					} else {
						keywordObj = instanceMap.get(methodVO.getClassName());
					}
					keywordObj.getClass().getMethod(methodVO.getName()).invoke(keywordObj);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			test.addStepsAfterIteration();
		}
		callBeforeAfterMethods(BugHuntConstants.AFTER_ANNOTATION, test, report, dataUtil);
		test.setExecutionStatus();
		report.saveReport();
	}

	private Object getKeywordClassInstance(Test test, Report report, DataUtil dataUtil, MethodVO methodVO)
			throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException,
			InvocationTargetException {
		Object keywordObj;
		Class<?> keywordClass;
		Constructor<?> constructor;
		keywordClass = Class.forName(methodVO.getClassName());
		constructor = keywordClass
				.getConstructor(new Class[] { ParameterVO.class });
		ParameterVO parameterVO = null;
		if(ExecutionMode.PARALLELMULTICONFIG != TestSession.getExecutionMode()) {
			parameterVO = new ParameterVO(report, dataUtil, test.getName(), test.getDirPath());
		} else {
			parameterVO = new ParameterVO(report, dataUtil, test.getName(), test.getDirPath(), getMultiConfigProps());
		}
		keywordObj = constructor.newInstance(parameterVO);
		return keywordObj;
	}
	
	private void callBeforeAfterMethods(String annotation, Test test, Report report, DataUtil dataUtil) {
		MethodVO methodVO = TestSession.getAnnotationMap().get(annotation);
		Object instance = null;
		Method method = null;
		try {
			instance = getKeywordClassInstance(test, report, dataUtil, methodVO);
			if(methodVO.isSuperClass()) {
				method = instance.getClass().getSuperclass().getDeclaredMethod(methodVO.getName());
			} else {
				method = instance.getClass().getDeclaredMethod(methodVO.getName());
			}
			method.invoke(instance);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
