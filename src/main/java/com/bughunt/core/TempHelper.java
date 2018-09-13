package com.bughunt.core;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;

import com.bughunt.domain.StepResult;
import com.bughunt.domain.Test;
import com.samskivert.mustache.Mustache;

public class TempHelper {
	public static void readMustacheTemplate() throws Exception {
    	final File templateDir = new File("./");
    	Mustache.Compiler c = Mustache.compiler().withLoader(new Mustache.TemplateLoader() {
    	    public Reader getTemplate (String name) throws Exception {
    	        return new FileReader(new File(templateDir, name));
    	    }
    	});
    	String tmpl = "{{>Test.mustache}}";
    	
    	Map<String, Test> testObject = new HashMap<>();
    	
    	Map<String, String> testProps = new LinkedHashMap<>();
    	testProps.put("Environment", "QA");
    	testProps.put("URL", "https://www.homedepot.com/");
    	testProps.put("Browser", "Chrome");
    	testProps.put("Version", "45");
    	testProps.put("Platform", "Windows");
    	testProps.put("Priority", "P1");
    	testProps.put("Module", "Search");
    	testProps.put("Channel", "Desktop");
    	
    	Test test = new Test("Search Keyword" , 1, testProps);
    	
    	test.addTestStep("Open Home Page", "Home Page displayed successfully", StepResult.PASS, Optional.empty());
    	test.addTestStep("Search keyword hammer", "PLP page for hammer is displayed", StepResult.PASS, Optional.empty()) ;
    	test.addTestStep("Verify Certona is displayed", "Certona is not displayed", StepResult.FAIL, Optional.of("href"));
    	test.addTestStep("Verify BOPIS product is displyed", "BOPIS product is not displayed", StepResult.WARNING, Optional.of("href"));
    	
    	test.setExecutionStatus();
    	
    	testObject.put("testObject", test);
    	
  /* String compiledHTML =  c.compile(tmpl).execute(new Object() {
    	    Object persons = Arrays.asList(new Person("Elvis", 75), new Person("Madonna", 52));
    	});*/
   String compiledHTML =  c.compile(tmpl).execute(testObject);
   System.out.println(compiledHTML);
   
   File file = new File("./Reort.html");
	
   FileUtils.writeStringToFile(file, compiledHTML.toString());
}

public static void readJavaCode() throws Exception {
		try {
			URL url = Class.forName("com.example.demo.DemoApplication").getProtectionDomain().getCodeSource().getLocation();
		     
		    String basePath = Paths.get(url.toURI()).toFile().toString();
		    String path1 = System.getProperty("user.dir").replace("\\", "/");
		    //String str =Class.forName("com.example.demo.keywords.HomePageKeyword").getProtectionDomain().getCodeSource().toString(); 
			//System.out.println(str);
		    
		    String keywordFolderPath = path1 + "/src/main/java/com/example/demo/keywords/HomePageKeyword.java";
		    
		    File scannedDir = new File(keywordFolderPath);
		    String data = FileUtils.readFileToString(scannedDir);
		    
			Object obj = Class.forName("com.example.demo.keywords.HomePageKeyword").newInstance();
			URI path = obj.getClass().getResource("Hello.txt").toURI();
			//URI path = Class.forName("com.example.demo.keywords.HomePageKeyword").getResource("HomePageKeyword.java").toURI();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
          
}
}
