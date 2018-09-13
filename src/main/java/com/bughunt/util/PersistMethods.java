package com.bughunt.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.bughunt.config.BugHuntConfig;
import com.bughunt.constants.BugHuntConstants;
import com.bughunt.core.TestSession;
import com.bughunt.domain.MethodVO;
import com.sun.beans.finder.ClassFinder;

public class PersistMethods {
	public HashMap<String, String> allKeywordsMap;
	public HashMap<String, List<String>> sameKeywordMap;
	
	public void setKeywordMethodMapping() throws Exception {
		
		TestSession.keywordMap = new HashMap<>();
		String keywordPackage = BugHuntConfig.instance().getBugHuntProperty(BugHuntConstants.KEYWORDS_PACKAGE);
		List<Class<?>> classes = ClassFinderUtil.getAllClasses(keywordPackage);
    	
    		Class<?> keywordClass = null;
		Object keywordInstance = null;
		allKeywordsMap = new HashMap<String, String>();
		sameKeywordMap = new HashMap<String, List<String>>();
		
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
			
			System.out.println("Below keywords are repeated. Please remove these duplicate methods :\n");
			
			List<String> lstClasses;
			int i = 1;
			
			for(Entry<String,List<String>> entry : sameKeywordMap.entrySet()) {
				
				lstClasses = entry.getValue();
				//lstClasses.add(ExecutionSession.keywordMap.get(entry.getKey()).className.replace("com.homer.keywords.", "").trim());
				
				//System.out.println(i+". Method - " + entry.getKey() + " in Class - "+  org.apache.commons.lang3.StringUtils.join(lstClasses, ", "));
				i++;
			}
			
			System.exit(0);
		}		
	}
	
	
	
	public void addKeywords(Object obj) {
		
		MethodVO methodVO = null;
		List<String> lstSameKeywords;
		//AnnotationMap annotationMap = null;
		List<String> lstSameRegexMethods;
		String regexVal = "";
		String methodName = "";
		int noOfArgs;
		String className = obj.getClass().getSimpleName();	
		
        for (Method m : obj.getClass().getDeclaredMethods()) {
        	
        //	Before beforeAnnot = (Before)m.getAnnotation(Before.class);
       // 	After afterAnnot = (After)m.getAnnotation(After.class);
        	
        	noOfArgs = m.getParameterTypes().length;
        	
        //	if(afterAnnot != null) {
        		
        	//	regexVal = afterAnnot.value();
        		
        		methodName = m.getName();
   		      		
			// annotationMap = new AnnotationMap();
			// annotationMap.className =obj.getClass().getName();
			// annotationMap.methodName = m.getName();
			// annotationMap.noOfArgs = noOfArgs;
			// annotationMap.method = m;
			
			/*if (afterAnnot.value().trim().equals("Iteration") || afterAnnot.value().trim().equals("")) {
				
				if (ExecutionSession.afterMap.get(afterAnnot.value().trim()) != null) {
					System.out.println("The @After tag (" + afterAnnot.value().trim() + ") exists in more than one step below:" );
					System.out.println(annotationMap.className + " : "+ annotationMap.methodName);
					AnnotationMap afterAnnotMap = ExecutionSession.afterMap.get(afterAnnot.value().trim());
					System.out.println(afterAnnotMap.className + " : "+ afterAnnotMap.methodName);
					System.out.println("Please correct these issues");
					System.exit(0);
				}
				
				ExecutionSession.afterMap.put(afterAnnot.value().trim(), annotationMap);
			} else {
				System.out.println("The @After tag (" + afterAnnot.value().trim() + ") is not a valid value:" );
				System.exit(0);
			}
		}	else if(beforeAnnot != null) {
        		
    		regexVal = beforeAnnot.value();
    		
    			methodName = m.getName();
	      		
			annotationMap = new AnnotationMap();
			annotationMap.className =obj.getClass().getName();
			annotationMap.methodName = m.getName();
			annotationMap.noOfArgs = noOfArgs;
			annotationMap.method = m;
			
			if (beforeAnnot.value().trim().equals("Iteration") || beforeAnnot.value().trim().equals("")) {

				if (ExecutionSession.beforeMap.get(beforeAnnot.value().trim()) != null) {
					System.out.println("The @Before tag (" + beforeAnnot.value().trim() + ") exists in more than one step below:" );
					System.out.println(annotationMap.className + " : "+ annotationMap.methodName);
					AnnotationMap bforeMap = ExecutionSession.beforeMap.get(beforeAnnot.value().trim());
					System.out.println(bforeMap.className + " : "+ bforeMap.methodName);
					System.out.println("Please correct these issues");
					System.exit(0);
				}
				
				ExecutionSession.beforeMap.put(beforeAnnot.value().trim(), annotationMap);
			} else {
				System.out.println("The @Before tag (" + beforeAnnot.value().trim() + ") is not a valid value:" );
				System.exit(0);
			}
		}	
             */  
        	methodName = m.getName();
        	noOfArgs = m.getParameterTypes().length;
        	
        	if(!allKeywordsMap.containsKey(methodName)) {
    			
        		allKeywordsMap.put(methodName,m.getName());  
    		
        		methodVO = new MethodVO();
        		methodVO.setName(m.getName()); 
        		methodVO.setClassName(obj.getClass().getName());  		
        		
        		TestSession.keywordMap.put(methodName, methodVO);	   
        		
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
}
