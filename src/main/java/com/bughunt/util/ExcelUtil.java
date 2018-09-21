
package com.bughunt.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.event.CellEditorListener;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.bughunt.config.BugHuntConfig;
import com.bughunt.constants.BugHuntConstants;
import com.bughunt.core.TestSession;
import com.bughunt.domain.Test;
import com.bughunt.exception.InCompleteSettingsException;

public class ExcelUtil {

	private static boolean setMasterData;
	
	private static final String ITERATION = "Iteration";
	private static final String SUB_ITERATION = "SubIteration";
	
	public static String getExcelFileExtension(String file) {
		String extension = null;
		extension = file.substring(file.indexOf(".") + 1, file.length());
		return extension;
	}
	
	public static String getCellVal(Row row, int columnIndex) {
		String cellVal = "";
		Cell cell = row.getCell(columnIndex);
		if(cell!=null) {
			cellVal = cell.toString().replaceAll("[^\\x00-\\x7f]", " ");
		}	
		return cellVal;
	}
	
	public static void setMasterTestData() {
		if(setMasterData) {
			return;
		}
		setMasterData = true;
		String testSet = BugHuntConfig.instance().getBugHuntProperty(BugHuntConstants.TEST_SET);
		String dataFile = BugHuntConfig.instance().getDataPath() + BugHuntConstants.EXCEL + "/" +testSet;
		Workbook workBook = null;
		Map<Integer, Object> tempMasterData = new HashMap<>();
		
		try {
			workBook = getWorkBook(dataFile);
			Sheet sheet = workBook.getSheetAt(1);
			Iterator<Row> rows = sheet.rowIterator();
			String test;
			Row row = (Row) rows.next();
			Cell cell;
			Iterator<?> cells = row.cellIterator();
			Map<Integer, String> headerMap = new LinkedHashMap<>();
			int iterationCellIndex = 0;
			while (cells.hasNext()) {
				cell = (Cell) cells.next();
				if(ITERATION.equals(cell.getStringCellValue().toString())) {
					iterationCellIndex = cell.getColumnIndex();
				}
				headerMap.put(cell.getColumnIndex(), cell.getStringCellValue());				
			}
			while (rows.hasNext()) {
				row = (Row) rows.next();
				test = "";
				test = ExcelUtil.getCellVal(row, 0).trim();
				if(StringUtils.isNotEmpty(test)) {
					addDataToMasterSet(test, row, tempMasterData, headerMap, iterationCellIndex);
				} else if(test.isEmpty()) {
					break;
				}			
			}
			TestSession.setMasterTestData(tempMasterData);
		} catch (InCompleteSettingsException iex) {
			throw iex;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new InCompleteSettingsException("Exception occured while reading Keywords");
		} finally {
			closeWorkBook(workBook);
		}
			
	}

	private static void addDataToMasterSet(String testName, Row row, 
			Map<Integer, Object> tempMasterData, Map<Integer, String> headerMap, int iterationCellIndex) {
		Optional<Test> optTest = TestSession.getTestCases().stream().filter(t->testName.equalsIgnoreCase(t.getName())).findFirst();
		if(optTest.isPresent()) {
			Test test = optTest.get();
			int testId = test.getId();
			Map<Integer, Object> itearationMap = null;
			Map<String, String> dataRow = null;
			Map<String, String> subIterationMap = new HashMap<>();
			Map<Integer,Map<String, String>> subIterationMaps = null;
			int iteration;
			int subIteration = 0;
			Iterator<Cell> cellIt = row.cellIterator();
			iteration = (int) row.getCell(iterationCellIndex).getNumericCellValue();
			if(!tempMasterData.containsKey(testId)) {
				itearationMap = new LinkedHashMap();
				subIterationMaps = new LinkedHashMap<>();
				while(cellIt.hasNext()) {
					Cell cell = cellIt.next();
					if(ITERATION.equals(headerMap.get(cell.getColumnIndex())) || 
							SUB_ITERATION.equals(headerMap.get(cell.getColumnIndex()))) {
						if(SUB_ITERATION.equals(headerMap.get(cell.getColumnIndex()))) {
							subIteration = (int)cell.getNumericCellValue();
						}
						subIterationMap.put(headerMap.get(cell.getColumnIndex()), String.valueOf((int)cell.getNumericCellValue()));
					} else {
						subIterationMap.put(headerMap.get(cell.getColumnIndex()), cell.toString());
					}
				}
				
				subIterationMaps.put(subIteration,subIterationMap);
				itearationMap.put(iteration, subIterationMaps);
				tempMasterData.put(testId, itearationMap);
			} else {
				itearationMap = (Map<Integer, Object>) tempMasterData.get(testId);
				if(itearationMap.containsKey(iteration)) {
					subIterationMaps = (Map<Integer, Map<String, String>>) itearationMap.get(iteration);
					while(cellIt.hasNext()) {
						Cell cell = cellIt.next();
						if(ITERATION.equals(headerMap.get(cell.getColumnIndex())) || 
								SUB_ITERATION.equals(headerMap.get(cell.getColumnIndex()))) {
							if(SUB_ITERATION.equals(headerMap.get(cell.getColumnIndex()))) {
								subIteration = (int)cell.getNumericCellValue();
							}
							subIterationMap.put(headerMap.get(cell.getColumnIndex()), String.valueOf((int)cell.getNumericCellValue()));
						} else {
							subIterationMap.put(headerMap.get(cell.getColumnIndex()), cell.toString());
						}
					}
					subIterationMaps.put(subIteration,subIterationMap);
				} else {
					subIterationMaps = new HashMap<>();
					while(cellIt.hasNext()) {
						Cell cell = cellIt.next();
						if(ITERATION.equals(headerMap.get(cell.getColumnIndex())) || 
								SUB_ITERATION.equals(headerMap.get(cell.getColumnIndex()))) {
							if(SUB_ITERATION.equals(headerMap.get(cell.getColumnIndex()))) {
								subIteration = (int)cell.getNumericCellValue();
							}
							subIterationMap.put(headerMap.get(cell.getColumnIndex()), String.valueOf((int)cell.getNumericCellValue()));
						} else {
							subIterationMap.put(headerMap.get(cell.getColumnIndex()), cell.toString());
						}
					}
					subIterationMaps.put(subIteration,subIterationMap);
					itearationMap.put(iteration, subIterationMaps);
				}
			}
		}
	}
	
	
	private static Workbook getWorkBook(String dataFile)
			throws IOException, InvalidFormatException, FileNotFoundException {
		File file;
		Workbook workbook;
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
		return workbook;
	}
	
	private static void closeWorkBook(Workbook workbook) {
		try {
			workbook.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
