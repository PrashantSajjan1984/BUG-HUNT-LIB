package com.bughunt.testmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import com.bughunt.domain.Test;
import com.bughunt.util.ExcelUtil;

public class ExcelTestManager extends TestManager {

	private static Map<String, Integer> headerMap;
	
	@Override
	public void setTestsToExecute() {
		BugHuntConfig bugHuntConfig = BugHuntConfig.instance();
		String fileName = bugHuntConfig.getBaseFWPath() + "/TestManager";
		String testSet = bugHuntConfig.getBugHuntProperty(BugHuntConstants.TEST_SET);
		File file = null;
		Workbook workbook = null;
		TestSession.testCases = new ArrayList<>();
		try {
			file = new File(fileName);
			if ("xlsx".equals(ExcelUtil.getExcelFileExtension(fileName))) {
				fileName = fileName + ".xlsx";
				file = new File(fileName);
				workbook = new XSSFWorkbook(file);
			} else {
				fileName = fileName + ".xls";
				file = new File(fileName);
				workbook = new HSSFWorkbook(new FileInputStream(file));
			}
			Sheet sheet = workbook.getSheet(testSet);
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
			int testManagerRowNo = -1;
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
		} catch (Exception ex) {

			System.out.println(ex.getMessage());
		} finally {
			closeWorkBook(workbook);
		}
	}
	
	@Override
	public Map<String, String> getTestManagerRow(int rowNo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getTestManagerRow(String testCaseName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTestManagerColumnVal(int rowNo, String columnName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTestManagerColumnVal(String testCaseName, String columnName) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	private void addTestsToTestSession(Row row, String testName, int testManagerRowNo) {
		Map<String, String> testProps = new LinkedHashMap<>();
		Iterator<Cell> cellIt = row.cellIterator();
		String cellVal;
		for(Entry<String, Integer> entry : headerMap.entrySet()) {
			cellVal = ExcelUtil.getCellVal(row, entry.getValue());
			testProps.put(entry.getKey(), cellVal);
		}
		
		Test test = new Test(testName, testManagerRowNo, testProps);
		TestSession.testCases.add(test);
	}
	
	private void closeWorkBook(Workbook workbook) {
		try {
			workbook.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
