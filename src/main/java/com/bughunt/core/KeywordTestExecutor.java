package com.bughunt.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.bughunt.constants.BugHuntConstants;
import com.bughunt.domain.MethodVO;
import com.bughunt.domain.ParameterVO;
import com.bughunt.domain.Test;
import com.bughunt.reports.Report;
import com.bughunt.util.DataUtil;

public class KeywordTestExecutor extends TestExecutor {

	@Override
	protected void callTestMethods(Test test) {
		Map<String, Object> instanceMap = new HashMap<>();
		Object keywordObj = null;
		Class<?> keywordClass = null;
		Constructor<?> constructor =null;
		test.createReportFolder();
		Report report = new Report(test);
		DataUtil dataUtil = new DataUtil();
		callBeforeAfterMethods(BugHuntConstants.BEFORE_ANNOTATION);
		for(MethodVO methodVO: test.getKeywords()) {
			try {
				if(!instanceMap.containsKey(methodVO.getClassName())) {
					keywordClass = Class.forName(methodVO.getClassName());
					constructor = keywordClass
							.getConstructor(new Class[] { ParameterVO.class });
					keywordObj = constructor.newInstance(new ParameterVO(report, dataUtil, test.getName()));
					instanceMap.put(methodVO.getClassName(), keywordObj);
				} else {
					keywordObj = instanceMap.get(methodVO.getClassName());
				}
				keywordObj.getClass().getMethod(methodVO.getName()).invoke(keywordObj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		callBeforeAfterMethods(BugHuntConstants.AFTER_ANNOTATION);
		test.setExecutionStatus();
		report.saveReport();
	}
	
	private void callBeforeAfterMethods(String annotation) {
		MethodVO methodVO = TestSession.getAnnotationMap().get(annotation);
		Object instance = null;
		Method method = null;
		try {
			instance = Class.forName(methodVO.getClassName()).newInstance();
			if(methodVO.isSuperClass()) {
				method = instance.getClass().getSuperclass().getDeclaredMethod(methodVO.getName());
				method.invoke(instance);
			} else {
				method = instance.getClass().getDeclaredMethod(methodVO.getName());
				method.invoke(instance);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
