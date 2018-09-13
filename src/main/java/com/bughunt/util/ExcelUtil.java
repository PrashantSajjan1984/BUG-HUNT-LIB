package com.bughunt.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class ExcelUtil {

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
}
