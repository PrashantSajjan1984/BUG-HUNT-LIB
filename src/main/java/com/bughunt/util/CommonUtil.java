package com.bughunt.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
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
	
	public static void createFolder(String dir) {
		Path path = Paths.get(dir);
		try {
			if(!Files.exists(path)) {
				Files.createDirectory(path);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public static String getShortFileName(String fileName) {
		fileName = fileName.replaceAll(" ", "_");
		return fileName.length() < 80 ? fileName : fileName.substring(0, 80);
	}
	
}
