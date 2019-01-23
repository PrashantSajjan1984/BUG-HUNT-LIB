
package com.bughunt.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.bughunt.config.BugHuntConfig;
import com.bughunt.constants.BugHuntConstants;
import com.bughunt.core.TestSession;
import com.bughunt.domain.Test;
import com.bughunt.domain.Test.OverALLStatus;
import com.bughunt.exception.InCompleteSettingsException;
import com.bughunt.testmanager.ExcelTestManager;

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
		String testSet = BugHuntConfig.getBugHuntProperty(BugHuntConstants.TEST_SET);
		String dataFile = BugHuntConfig.getDataPath() + BugHuntConstants.EXCEL + "/" +testSet;
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
	
	public static void addFailedTCToExcel() {

		HashMap<String, Integer> failedTCMAp = new HashMap<String, Integer>();
		List<Test> failedTests = TestSession.getTestCases().stream().filter(t->t.getOverAllStatus()==(OverALLStatus.FAILED)).collect(Collectors.toList());
		if(failedTests.size() == 0) {
			return;
		}
		String fileName = BugHuntConfig.getBaseFWPath() + BugHuntConstants.FAILED_TESTS_EXCEL;
		Workbook workbook = null;
		try (FileOutputStream fos = new FileOutputStream(fileName)) {
			File file = new File(fileName);
			workbook = new XSSFWorkbook();
			Sheet failedTestSheet = workbook.createSheet(BugHuntConfig.getBugHuntProperty(BugHuntConstants.TEST_SET));
			XSSFCellStyle style = getCellStyle(workbook);
			int cellNum = 0;
			Cell cell;
			Row dataRow;
			int rowNum = 0;
			dataRow = failedTestSheet.createRow(rowNum++);
			for(String columnName : ExcelTestManager.columnNameMap.keySet()) {
				cell = dataRow.createCell(cellNum);
				failedTestSheet.setColumnWidth(cellNum, ExcelTestManager.columnWidth.get(cellNum));
				cell.setCellStyle(style);	
				cell.setCellValue(columnName);
				cellNum++;
			}

			for(Test test: failedTests) {
				dataRow = failedTestSheet.createRow(rowNum);
				for(Entry<String, Integer> columnEntry : ExcelTestManager.columnNameMap.entrySet()) {
					cell = dataRow.createCell(columnEntry.getValue());
					if(columnEntry.getValue() == 0) {
						cell.setCellValue(rowNum);
					} else if(null != test.getPropMap().get(columnEntry.getKey())){
						cell.setCellValue(test.getPropMap().get(columnEntry.getKey()));
					}
				}
				rowNum++;
			}
			workbook.write(fos);
		} catch (IOException ioEx) {
			System.out.println("Exception occured while adding failed tests" + ioEx.getMessage());
		} 
	}

	private static XSSFCellStyle getCellStyle(Workbook workbook) {
		XSSFCellStyle style;
		XSSFFont font;
		style = (XSSFCellStyle) workbook.createCellStyle();
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setFillForegroundColor(IndexedColors.BLACK.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setAlignment(HorizontalAlignment.CENTER);
		font = (XSSFFont) workbook.createFont();
		font.setFontName("Calibri");
		font.setColor(IndexedColors.WHITE.getIndex()); 
		font.setBold(true);
		font.setFontHeightInPoints((short) 11);
		style.setFont(font);
		style.setWrapText(true);
		return style;
	}
	
	public static void deleteFailedTestsExcel() {
		Path fileToDeletePath = Paths.get(BugHuntConfig.getBaseFWPath() + BugHuntConstants.FAILED_TESTS_EXCEL);
		try {
			if(Files.exists(fileToDeletePath)) {
				 Files.delete(fileToDeletePath);
			}
		} catch (NoSuchFileException x) {
		    System.err.format("%s: no such" + " file or directory%n", fileToDeletePath);
		} catch (DirectoryNotEmptyException x) {
		    System.err.format("%s not empty%n", fileToDeletePath);
		} catch (IOException x) {
		    System.err.println(x);
		}
	}
	
	public static void setCommonData() {
		Map<String, String> commonDataMap = new HashMap<>();
		try {
			String fileName = BugHuntConfig.getDataPath() + BugHuntConstants.EXCEL + "/" + BugHuntConstants.COMMON_EXCEL_DATA;
			Workbook workbook = getWorkBook(fileName);
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIt = sheet.rowIterator();
			rowIt.next();
			while(rowIt.hasNext()) {
				Row row = rowIt.next();
				if(null!=row.getCell(0) && null!=row.getCell(1)) {
					if(StringUtils.isBlank(row.getCell(0).getStringCellValue())) {
						break;
					}
					String key = row.getCell(0).getStringCellValue();
					String value = row.getCell(1).getStringCellValue();
					commonDataMap.put(key, value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			TestSession.setCommonData(commonDataMap);
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
