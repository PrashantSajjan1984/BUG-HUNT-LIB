package com.bughunt.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.bughunt.config.BugHuntConfig;
import com.bughunt.constants.BugHuntConstants;
import com.bughunt.core.TestSession;
import com.bughunt.domain.ExecutionMode;
import com.bughunt.domain.Test;
import com.bughunt.domain.Test.OverALLStatus;

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
	
	public static int getIntegerValue(String intVal) {
		int retVal = 0;
		try {
			retVal = Integer.parseInt(intVal);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		return retVal;
	}
	
	public static String getParallelConfigPrefix(Test test) {
		String prefix = null;
		if(ExecutionMode.PARALLELMULTICONFIG == TestSession.getExecutionMode()) {
			 prefix = test.getParallelConfig().get(BugHuntConstants.REPORT_VALUE) + "_" + test.getParallelConfig().get(BugHuntConstants.ID) + "_";
		}
		return prefix;
	}
	
	public static String getExecutionDateTime() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM, yyyy hh:mm:ss a");
        return LocalDateTime.now().format(formatter);
	}
	
	public static List<Test> getTestsCompleted() {
		return TestSession.getTestCases().stream().filter(t->t.getOverAllStatus()==(OverALLStatus.PASSED)
				|| t.getOverAllStatus()==(OverALLStatus.FAILED)).collect(Collectors.toList());
	}
}
