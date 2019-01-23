package com.bughunt.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.bughunt.config.BugHuntConfig;

public class ClassFinderUtil {
	private static final char DOT = '.';
    private static final String CLASS_SUFFIX = ".class";
    
    private ClassFinderUtil() {
    	
    }
    
    public static List<Class<?>> getAllClasses(String scannedPackage) {
    		String keywordsPackagePath = BugHuntConfig.getKeywordPackagePath();
    		File scannedDir = new File(keywordsPackagePath);
        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (File file : scannedDir.listFiles()) {
        		classes.addAll(getClasses(file, scannedPackage));
        }
        return classes;
    }
    
    private static List<Class<?>> getClasses(File file, String scannedPackage) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        String resource = scannedPackage + DOT + file.getName();
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                classes.addAll(getClasses(child, resource));
            }
        } else if (resource.endsWith(CLASS_SUFFIX)) {
            int endIndex = resource.length() - CLASS_SUFFIX.length();
            String className = resource.substring(0, endIndex);
            try {
                classes.add(Class.forName(className));
            	} catch (ClassNotFoundException ex) {
            		ex.printStackTrace();
            	}
        }
        return classes;
    }
}
