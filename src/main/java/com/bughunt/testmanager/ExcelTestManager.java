package com.bughunt.testmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.bughunt.config.BugHuntConfig;
import com.bughunt.constants.BugHuntConstants;
import com.bughunt.core.TestSession;
import com.bughunt.domain.Test;
import com.bughunt.util.ExcelUtil;

public class ExcelTestManager extends TestManager {

	private static Map<String, Integer> headerMap;
	List<Test> tests = null;
	@Override
	public void setTestsToExecute() {
		Workbook workBook = null;
		tests = new ArrayList<>();
		try {
			workBook = getWorkBook();
			Sheet sheet = getTestManagerSheet(workBook);
			Iterator<Row> rows = sheet.rowIterator();
			String tcName;
			Row row = (Row) rows.next();
			Cell cell;
			Iterator<?> cells = row.cellIterator();
			headerMap = new LinkedHashMap<>();
			while (cells.hasNext()) {
				cell = (Cell) cells.next();
				headerMap.put(cell.getStringCellValue(), cell.getColumnIndex());				
			}
			// verifyRunManagerColumns(ExecutionSession.runManagerColumnMap);
			int testManagerRowNo = 0;
			while (rows.hasNext()) {
				row = (Row) rows.next();
				testManagerRowNo++;
				tcName = "";
				if(!ExcelUtil.getCellVal(row, headerMap.get(TestManagerColumns.EXECUTE.getName())).equalsIgnoreCase("Yes")) {
					tcName = ExcelUtil.getCellVal(row, headerMap.get(TestManagerColumns.TEST_CASE_NAME.getName()));
					if(!tcName.isEmpty()) {						
						continue;						
					} else {
						break;
					}				
				}
				tcName = ExcelUtil.getCellVal(row, headerMap.get(TestManagerColumns.TEST_CASE_NAME.getName()));
				if(StringUtils.isNotEmpty(tcName)) {
					addTestsToTestSession(row, tcName, testManagerRowNo);
				} else if(tcName.isEmpty()) {
					break;
				}			
			}	
			TestSession.setTestCases(tests);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeWorkBook(workBook);
		}
	}
	
	private Workbook getWorkBook() throws InvalidFormatException, IOException {
		BugHuntConfig bugHuntConfig = BugHuntConfig.instance();
		String fileName = bugHuntConfig.getBaseFWPath() + "/TestManager";
		File file = null;
		Workbook workBook = null;
		tests = new ArrayList<>();
		file = new File(fileName);
		if ("xlsx".equals(ExcelUtil.getExcelFileExtension(fileName))) {
			fileName = fileName + ".xlsx";
			file = new File(fileName);
			workBook = new XSSFWorkbook(file);
		} else {
			fileName = fileName + ".xls";
			file = new File(fileName);
			workBook = new HSSFWorkbook(new FileInputStream(file));
		}
		return workBook;
	}
	
	private Sheet getTestManagerSheet(Workbook workBook) throws InvalidFormatException, IOException {
		String testSet = BugHuntConfig.instance().getBugHuntProperty(BugHuntConstants.TEST_SET);
		return workBook.getSheet(testSet);
	}
	
	@Override
	public String getTestManagerColumnVal(String columnName, int rowNum) {
		String columnVal = "";
		if(!headerMap.containsKey(columnName)) {
			return columnVal;
		}
		Workbook workBook = null;
		try {
			 workBook = getWorkBook();
			 Sheet sheet = getTestManagerSheet(workBook);
			 Row row = sheet.getRow(rowNum);
			 columnVal = ExcelUtil.getCellVal(row, headerMap.get(columnName));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeWorkBook(workBook);
		}
		return columnVal;
	}
	
	private void addTestsToTestSession(Row row, String testName, int testManagerRowNo) {
		Map<String, String> testProps = new LinkedHashMap<>();
		String cellVal;
		for(Entry<String, Integer> entry : headerMap.entrySet()) {
			cellVal = ExcelUtil.getCellVal(row, entry.getValue());
			testProps.put(entry.getKey(), cellVal);
		}
		
		Test test = new Test(testName, testManagerRowNo, testProps);
		tests.add(test);
	}
	
	private void closeWorkBook(Workbook workBook) {
		try {
			workBook.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
