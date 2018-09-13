package com.bughunt.util;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public class CommonUtil {

	private CommonUtil() {
		
	}
	
	public static String getFWRootPath() {
		return System.getProperty("user.dir").replace("\\", "/");
	}
	
	public static String getClassLocation(String runnerClass) {
		URL url;
		String classLocation = "";
		try {
			url = Class.forName(runnerClass).getProtectionDomain().getCodeSource().getLocation();
			String rootPath = getFWRootPath();
			String basePath = Paths.get(url.toURI()).toFile().toString();
			
			if(basePath.contains("target\\classes") || basePath.contains("/target/classes")) {
				classLocation = rootPath + "/target/classes/";
		     } else if(basePath.contains("test-classes")) {
		    	 	classLocation = rootPath + "/target/test-classes/";
		     } else {
		    	 	classLocation = rootPath + "/src/";
		     }
		} catch (ClassNotFoundException | URISyntaxException ex) {
			ex.printStackTrace();
		}
		return classLocation;
	}
	
}
