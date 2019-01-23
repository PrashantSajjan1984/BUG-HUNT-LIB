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

	public static Map<String, Integer> columnNameMap;
	public static Map<Integer, Integer> columnWidth;
	List<Test> tests = null;
	@Override
	public boolean setTestsToExecute() {
		boolean setTestSucessful = true;
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
			columnNameMap = new LinkedHashMap<>();
			columnWidth = new LinkedHashMap<>();
			while (cells.hasNext()) {
				cell = (Cell) cells.next();
				columnNameMap.put(cell.getStringCellValue(), cell.getColumnIndex());	
				columnWidth.put(cell.getColumnIndex(), sheet.getColumnWidth(cell.getColumnIndex()));
			}
			// verifyRunManagerColumns(ExecutionSession.runManagerColumnMap);
			int testManagerRowNo = 0;
			while (rows.hasNext()) {
				row = (Row) rows.next();
				testManagerRowNo++;
				tcName = "";
				if(!ExcelUtil.getCellVal(row, columnNameMap.get(TestManagerColumns.EXECUTE.getName())).equalsIgnoreCase("Yes")) {
					tcName = ExcelUtil.getCellVal(row, columnNameMap.get(TestManagerColumns.TEST_CASE_NAME.getName()));
					if(!tcName.isEmpty()) {						
						continue;						
					} else {
						break;
					}				
				}
				tcName = ExcelUtil.getCellVal(row, columnNameMap.get(TestManagerColumns.TEST_CASE_NAME.getName()));
				if(StringUtils.isNotEmpty(tcName)) {
					addTestsToTestSession(row, tcName, testManagerRowNo);
				} else if(tcName.isEmpty()) {
					break;
				}			
			}	
			TestSession.setTestCases(tests);
		} catch (Exception ex) {
			ex.printStackTrace();
			setTestSucessful = false;
			ExcelUtil.deleteFailedTestsExcel();
		} finally {
			if(null != workBook) {
				closeWorkBook(workBook);
			}
		}
		return setTestSucessful;
	}
	
	private Workbook getWorkBook() throws InvalidFormatException, IOException {
		String fileName = BugHuntConfig.getBaseFWPath() + "/TestManager";
		if("true".equals(BugHuntConfig.getBugHuntProperty("ExecuteFailedTests"))) {
			fileName = BugHuntConfig.getBaseFWPath() + BugHuntConstants.FAILED_TESTS_EXCEL;
		}
		File file = null;
		Workbook workBook = null;
		tests = new ArrayList<>();
		file = new File(fileName);
		if ("xlsx".equals(ExcelUtil.getExcelFileExtension(fileName))) {
			if(!"true".equals(BugHuntConfig.getBugHuntProperty("ExecuteFailedTests"))) {
				fileName = fileName + ".xlsx";
			}
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
		String testSet = BugHuntConfig.getBugHuntProperty(BugHuntConstants.TEST_SET);
		return workBook.getSheet(testSet);
	}
	
	public void setTestHeaderColumnAndWidth() {
		Workbook workBook = null;
		String fileName = BugHuntConfig.getBaseFWPath() + "/TestManager";
		File file = null;
		try {
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
			Sheet sheet = getTestManagerSheet(workBook);
			Iterator<Row> rows = sheet.rowIterator();
			Row row = (Row) rows.next();
			Cell cell;
			Iterator<?> cells = row.cellIterator();
			columnNameMap = new LinkedHashMap<>();
			columnWidth = new LinkedHashMap<>();
			while (cells.hasNext()) {
				cell = (Cell) cells.next();
				columnNameMap.put(cell.getStringCellValue(), cell.getColumnIndex());	
				columnWidth.put(cell.getColumnIndex(), sheet.getColumnWidth(cell.getColumnIndex()));
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
			closeWorkBook(workBook);
		}
		
	}
	
	@Override
	public String getTestManagerColumnVal(String columnName, int rowNum) {
		String columnVal = "";
		if(!columnNameMap.containsKey(columnName)) {
			return columnVal;
		}
		Workbook workBook = null;
		try {
			 workBook = getWorkBook();
			 Sheet sheet = getTestManagerSheet(workBook);
			 Row row = sheet.getRow(rowNum);
			 columnVal = ExcelUtil.getCellVal(row, columnNameMap.get(columnName));
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
		for(Entry<String, Integer> entry : columnNameMap.entrySet()) {
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
