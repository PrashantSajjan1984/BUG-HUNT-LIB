package com.bughunt.keywordmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.bughunt.config.BugHuntConfig;
import com.bughunt.constants.BugHuntConstants;
import com.bughunt.core.TestSession;
import com.bughunt.domain.MethodVO;
import com.bughunt.domain.Test;
import com.bughunt.exception.InCompleteSettingsException;
import com.bughunt.util.ExcelUtil;

public class ExcelKeywordManager extends KeywordManager {

	private String dataFile;
	
	public ExcelKeywordManager() {
		String testSet = BugHuntConfig.getBugHuntProperty(BugHuntConstants.TEST_SET);
		dataFile = BugHuntConfig.getDataPath() + BugHuntConstants.EXCEL + "/" +testSet;
	}
	
	@Override
	public void setKeywords() {
		File file = null;
		Workbook workbook = null;
		try {
			file = new File(dataFile);
			if ("xlsx".equals(ExcelUtil.getExcelFileExtension(dataFile))) {
				dataFile = dataFile + ".xlsx";
				file = new File(dataFile);
				workbook = new XSSFWorkbook(file);
			} else {
				dataFile = dataFile + ".xls";
				file = new File(dataFile);
				workbook = new HSSFWorkbook(new FileInputStream(file));
			}
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rows = sheet.rowIterator();
			String tcName;
			Row row = (Row) rows.next();
			while (rows.hasNext()) {
				row = (Row) rows.next();
				tcName = "";
				tcName = ExcelUtil.getCellVal(row, 0).trim();
				if(StringUtils.isNotEmpty(tcName)) {
					setKeywordsForSelectedTests(tcName, row);
				} else if(tcName.isEmpty()) {
					break;
				}			
			}
			verifyAllTestKeywordsSet();
		} catch (InCompleteSettingsException iex) {
			throw iex;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new InCompleteSettingsException("Exception occured while reading Keywords");
		} finally {
			closeWorkBook(workbook);
		}
	}

	private void setKeywordsForSelectedTests(String tcName, Row row) {
		Optional<Test> optTest = TestSession.getTestCases().stream().filter(t->t.getName().toLowerCase().equals(tcName.toLowerCase())).findFirst();
		if(optTest.isPresent()) {
			Test test = optTest.get();
			List<MethodVO> keywords = new ArrayList<>();
			MethodVO methodVO = null;
			String methodName = null;
			Iterator<Cell> cellIt = row.cellIterator();
			cellIt.next();
			List<String> unImplementedKeywords = null;
			while(cellIt.hasNext()) {
				methodName = cellIt.next().toString().trim();
				if(StringUtils.isNotEmpty(methodName)) {
					if(TestSession.getKeywordMap().containsKey(methodName)) {
						methodVO = TestSession.getKeywordMap().get(methodName);
						keywords.add(methodVO);
					} else {
						if(null == unImplementedKeywords) {
							unImplementedKeywords = new ArrayList<>();
						}
						unImplementedKeywords.add(methodName);
					}
				} else {
					break;
				}
			}
			if(null!=unImplementedKeywords) {
				String prefixText = unImplementedKeywords.size() == 1 ? "Below keyword is " : "Below keywords are ";
				String appendText = unImplementedKeywords.size() == 1 ? "keyword" : "keywords";
				String message = String.format(prefixText + "not implemented which is required for test '%s'.\nPlease implement below %s.\n", tcName,appendText);
				StringBuilder sb = new StringBuilder(message);
				int cnt = 0;
				for(String unImplementedMethod :unImplementedKeywords) {
					sb.append("\n");
					if(cnt != 0) {
						sb.append("\n");
					}
					sb.append(getUnImplementedMethodSignature(unImplementedMethod));
					cnt++;
				}
				sb.append("\n");
				throw new InCompleteSettingsException(sb.toString());
			}
			test.setKeywords(keywords);
		}
	}
	
	public String getUnImplementedMethodSignature(String methodName) {
		String methodSignature =	String.format("	public void %s { \n"+
					"\n	}", methodName);
		return methodSignature;
	}
	
	
	private void verifyAllTestKeywordsSet() {
		List<Test> noKeywordRowTests = TestSession.getTestCases().stream().filter(t->t.getKeywords()==null).collect(Collectors.toList());
		if(!noKeywordRowTests.isEmpty()) {
			StringBuilder sb = new StringBuilder("For below tests no keyword row is present \n");
			int i = 0;
			for(Test test: noKeywordRowTests) {
				sb.append(String.valueOf(++i) + ". " + test.getName() + "\n");
			}
			throw new InCompleteSettingsException(sb.substring(0, sb.length()-1));
		}
	}
		
	private void closeWorkBook(Workbook workbook) {
		try {
			workbook.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
