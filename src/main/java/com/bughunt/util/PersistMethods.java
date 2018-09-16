package com.bughunt.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.impl.xb.ltgfmt.TestsDocument.Tests;

import com.bughunt.config.BugHuntConfig;
import com.bughunt.constants.BugHuntConstants;
import com.bughunt.core.TestSession;
import com.bughunt.domain.After;
import com.bughunt.domain.Before;
import com.bughunt.domain.MethodVO;
import com.bughunt.exception.InCompleteSettingsException;

public class PersistMethods {
	public HashMap<String, String> allKeywordsMap;
	public HashMap<String, List<String>> sameKeywordMap;
	Map<String, MethodVO> keywordMap;
	Map<String, MethodVO> annotationMap;
	Map<String, List<MethodVO>> sameAnnotationMap;
	
	public void setKeywordMethodMapping() throws Exception {
		keywordMap = new HashMap<>();
		String keywordPackage = BugHuntConfig.instance().getBugHuntProperty(BugHuntConstants.KEYWORDS_PACKAGE);
		List<Class<?>> classes = ClassFinderUtil.getAllClasses(keywordPackage);
    		Class<?> keywordClass = null;
		Object keywordInstance = null;
		allKeywordsMap = new HashMap<>();
		sameKeywordMap = new HashMap<>();
		annotationMap = new HashMap<>();
		sameAnnotationMap = new HashMap<>();
		for (Class<?> packageClass : classes) {   
			if(Modifier.isAbstract( packageClass.getModifiers())) {
				continue;
			}
			try {
				keywordClass = Class.forName(packageClass.getName());
				keywordInstance = keywordClass.newInstance();
				addKeywords(keywordInstance); 
			} catch (Exception ex) {
				System.out.println("Error in constructor for class here: " +packageClass.getName());
			}    		   	
		}
		if(sameKeywordMap.size() > 0) {
			StringBuilder sb = new StringBuilder("Below keywords are repeated.\n");
			List<String> lstClasses;
			int i = 0;
			String message;
			for(Entry<String,List<String>> entry : sameKeywordMap.entrySet()) {
				lstClasses = entry.getValue();
				lstClasses.add(TestSession.getKeywordMap().get(entry.getKey()).getClassName().replace("com.bughunt.keywords.", "").trim());
				message = String.format("%d. Keyword method : %s in keyword class %s \n", ++i, entry.getKey(), StringUtils.join(lstClasses, ", "));
				sb.append(message);
			}	
			throw new InCompleteSettingsException(sb.substring(0, sb.length()-1));
		}	
		
		
		Class<?> superClass = classes.get(0).newInstance().getClass().getSuperclass();
		setBaseClassBeforeAfter(superClass, classes.get(0));
		
		if(sameAnnotationMap.containsKey(BugHuntConstants.BEFORE_ANNOTATION) || sameAnnotationMap.containsKey(BugHuntConstants.AFTER_ANNOTATION)) {
			handleDuplicateBeforeAfterMethods();
		}
		
		TestSession.setKeywordMap(keywordMap);
		TestSession.setAnnotationMap(annotationMap);
	}
	
	public void addKeywords(Object obj) {
		MethodVO methodVO = null;
		List<String> lstSameKeywords;
		String methodName = "";
		String className = obj.getClass().getSimpleName();	
        for (Method m : obj.getClass().getDeclaredMethods()) {
	        setBeforeAfterMethods(m);	   
	        	methodName = m.getName();
	        	if(!allKeywordsMap.containsKey(methodName)) {
	        		allKeywordsMap.put(methodName,m.getName()); 
	        		methodVO = new MethodVO();
	        		methodVO.setName(m.getName()); 
	        		methodVO.setClassName(obj.getClass().getName());  
	        		keywordMap.put(methodName, methodVO);	   
	        		
	    		} else {
	    			if(!sameKeywordMap.containsKey(methodName)) {
	    				lstSameKeywords = new ArrayList<String>();
	    				lstSameKeywords.add(className);
	    				sameKeywordMap.put(methodName, lstSameKeywords);
	    			} else {
	    				lstSameKeywords = sameKeywordMap.get(methodName);
	    				lstSameKeywords.add(className);
	    			}
	    		}      	        	   
        }
	}

	private void setBeforeAfterMethods(Method m) {
		MethodVO methodVO;
		Before beforeAnnot = (Before)m.getAnnotation(Before.class);
        After afterAnnot = (After)m.getAnnotation(After.class);
        List<MethodVO> lstMethods = null;
        if(beforeAnnot != null) {
			methodVO = setMethodVO(m);
			if(!annotationMap.containsKey(BugHuntConstants.BEFORE_ANNOTATION)) {
    				annotationMap.put(BugHuntConstants.BEFORE_ANNOTATION, methodVO);
    			} else {
    				lstMethods = sameAnnotationMap.containsKey(BugHuntConstants.BEFORE_ANNOTATION) ? 
    				sameAnnotationMap.get(BugHuntConstants.BEFORE_ANNOTATION) : new ArrayList<>();
    				if(!sameAnnotationMap.containsKey(BugHuntConstants.BEFORE_ANNOTATION)) {
    					lstMethods.add(annotationMap.get(BugHuntConstants.BEFORE_ANNOTATION));
    				}
    				lstMethods.add(methodVO);
    				sameAnnotationMap.put(BugHuntConstants.BEFORE_ANNOTATION, lstMethods);
    			}
		} else if(afterAnnot != null) {
	    		methodVO = setMethodVO(m);
	    		if(!annotationMap.containsKey(BugHuntConstants.AFTER_ANNOTATION)) {
	    			annotationMap.put(BugHuntConstants.AFTER_ANNOTATION, methodVO);
	    		} else {
	    			lstMethods = sameAnnotationMap.containsKey(BugHuntConstants.AFTER_ANNOTATION) ? 
	    					sameAnnotationMap.get(BugHuntConstants.AFTER_ANNOTATION) : new ArrayList<>();
				if(!sameAnnotationMap.containsKey(BugHuntConstants.AFTER_ANNOTATION)) {
					lstMethods.add(annotationMap.get(BugHuntConstants.AFTER_ANNOTATION));
				}
	    			lstMethods.add(methodVO);
	    			sameAnnotationMap.put(BugHuntConstants.AFTER_ANNOTATION, lstMethods);
	    		}
		} 
	}

	private void setBaseClassBeforeAfter(Class<?> superClass, Class<?> keywordClass)
			throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Method[] methods = superClass.getDeclaredMethods();
		MethodVO methodVO = null;
		List<MethodVO> lstMethods = null;
		for (Method m : methods) {
			Before beforeAnnot = (Before)m.getAnnotation(Before.class);
	        After afterAnnot = (After)m.getAnnotation(After.class);
	        if(beforeAnnot != null) {
		        	methodVO = new MethodVO();
		    		methodVO.setName(m.getName());
		    		methodVO.setSuperClass(true);
				if(!annotationMap.containsKey(BugHuntConstants.BEFORE_ANNOTATION)) {
					methodVO.setClassName(keywordClass.getName());
					annotationMap.put(BugHuntConstants.BEFORE_ANNOTATION, methodVO);
	    			} else {
	    				methodVO.setClassName(m.getDeclaringClass().getName());
	    				lstMethods = sameAnnotationMap.get(BugHuntConstants.BEFORE_ANNOTATION);
	    				lstMethods.add(methodVO);
	    				sameAnnotationMap.put(BugHuntConstants.BEFORE_ANNOTATION, lstMethods);
	    			}
	        } else if (afterAnnot!=null) {
	        		methodVO = new MethodVO();
	        		methodVO.setName(m.getName());
	        		methodVO.setSuperClass(true);
	        		if(!annotationMap.containsKey(BugHuntConstants.AFTER_ANNOTATION)) {
	        			methodVO.setClassName(keywordClass.getName());
	        			annotationMap.put(BugHuntConstants.AFTER_ANNOTATION, methodVO);
	        		} else {
	        			methodVO.setClassName(m.getDeclaringClass().getName());
	        			lstMethods = sameAnnotationMap.get(BugHuntConstants.AFTER_ANNOTATION);
	        			lstMethods.add(methodVO);
	    				sameAnnotationMap.put(BugHuntConstants.AFTER_ANNOTATION, lstMethods);
	        		}
	        }
		}
	}
	
	private void handleDuplicateBeforeAfterMethods() {
		StringBuilder sb = new StringBuilder();
		List<MethodVO> lstMethods;
		if(sameAnnotationMap.containsKey(BugHuntConstants.BEFORE_ANNOTATION)) {
			lstMethods = sameAnnotationMap.get(BugHuntConstants.BEFORE_ANNOTATION);
			sb.append("Before method is present in below classes: \n");
			int i = 0;
			for (MethodVO methodVO : lstMethods) {
				sb.append(String.valueOf(++i) + ". " + methodVO.getClassName() + "\n");
			}
		}
		
		if(sameAnnotationMap.containsKey(BugHuntConstants.AFTER_ANNOTATION)) {
			lstMethods = sameAnnotationMap.get(BugHuntConstants.AFTER_ANNOTATION);
			sb.append("After method is present in below classes: \n");
			int i = 0;
			for (MethodVO methodVO : lstMethods) {
				sb.append(String.valueOf(++i) + ". " + methodVO.getClassName() + "\n");
			}
		}
		
		sb.append("Framework allows only one method for Before and After methods");
		
		throw new InCompleteSettingsException(sb.toString());
	}
	
	private MethodVO setMethodVO(Method m) {
		MethodVO methodVO;
		String methodName = m.getName();
		methodVO = new MethodVO();
		methodVO.setName(methodName);
		methodVO.setClassName(m.getDeclaringClass().getName());
		return methodVO;
	}
}
