/*
 * Copyright 2012, 2013 Decebal Suiu, Emmeran Seehuber
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.wicket.pivot.exporter;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import ro.fortsoft.wicket.pivot.PivotModel;
import ro.fortsoft.wicket.pivot.PivotTableRenderModel;
import ro.fortsoft.wicket.pivot.PivotTableRenderModel.*;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic XLS exporter
 */
public class PivotXlsExporter implements PivotExporter {
	private static final long serialVersionUID = 1L;

	private static class StyleContext {
		private CellStyle headerStyle;
		private CellStyle dataHeaderStyle;
		private HSSFCellStyle grandTotalStyle;

		StyleContext(HSSFWorkbook wb) {
			headerStyle = wb.createCellStyle();

			headerStyle.setBorderTop(BorderStyle.MEDIUM);
			headerStyle.setBorderLeft(BorderStyle.MEDIUM);
			headerStyle.setBorderRight(BorderStyle.MEDIUM);
			headerStyle.setBorderBottom(BorderStyle.MEDIUM);
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerStyle.setFillForegroundColor(HSSFColorPredefined.GREY_25_PERCENT.getIndex());

			dataHeaderStyle = wb.createCellStyle();
			dataHeaderStyle.setBorderTop(BorderStyle.MEDIUM);
			dataHeaderStyle.setBorderLeft(BorderStyle.MEDIUM);
			dataHeaderStyle.setBorderRight(BorderStyle.MEDIUM);
			dataHeaderStyle.setBorderBottom(BorderStyle.MEDIUM);
			dataHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			dataHeaderStyle.setFillForegroundColor(HSSFColorPredefined.GREY_25_PERCENT.getIndex());

			grandTotalStyle = wb.createCellStyle();
			grandTotalStyle.setBorderTop(BorderStyle.MEDIUM);
			grandTotalStyle.setBorderLeft(BorderStyle.MEDIUM);
			grandTotalStyle.setBorderRight(BorderStyle.MEDIUM);
			grandTotalStyle.setBorderBottom(BorderStyle.MEDIUM);
			grandTotalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			grandTotalStyle.setFillForegroundColor(HSSFColorPredefined.LIGHT_TURQUOISE.getIndex());
		}
	}

	@Override
	public void exportPivot(PivotModel pivotModel, OutputStream outputStream) throws IOException {
		PivotTableRenderModel renderModel = PivotTableRenderModel.create(pivotModel);

		HSSFWorkbook wb = new HSSFWorkbook();
		Sheet sheetData = wb.createSheet("Pivot");

		Map<Integer, Integer> rowSpanMap = new HashMap<>();
		StyleContext styleContext = new StyleContext(wb);

		int rowNumber = 0;
		int maxColNum = 0;
		for (RenderRow row : renderModel.getAllRenderRows()) {
			int col = 0;
			Row poiRow = sheetData.createRow(rowNumber);
			for (RenderCell cell : row.getRenderCells()) {
				maxColNum = Math.max(maxColNum, col);

				/*
				 * Check if we currently have a rowspan at this column from the
				 * parent row. We only support a colspan of 1 at the moment
				 * here, if rowspan > 1
				 */
				Integer rowSpan = rowSpanMap.get(col);
				if (rowSpan != null) {
					rowSpan--;
					if (rowSpan == 0)
						rowSpanMap.remove(col);
					else
						rowSpanMap.put(col, rowSpan);
					col++;
				}

				Cell poiCell = poiRow.createCell(col);

				/*
				 * Output the Value
				 */
				Object rawValue = cell.getRawValue();
				if (rawValue != null) {
					if (rawValue instanceof Double) {
						poiCell.setCellValue((Double) rawValue);
					} else {
						poiCell.setCellValue(String.valueOf(rawValue));
					}
				}

				styleCell(poiCell, cell, styleContext);

				if (cell.getRowspan() > 1) {
					rowSpanMap.put(col, cell.getRowspan() - 1);
					sheetData.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber + cell.getRowspan() - 1, col,
							col));
				}

				/*
				 * We only support colspan _OR_ rowspan. The current PivotTable
				 * also doesnt have rowspan and colspan at the same time.
				 */
				if (cell.getColspan() > 1) {
					sheetData.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, col, col + cell.getColspan()
							- 1));
				}

				col++;
				for (int i = 1; i < cell.getColspan(); i++) {
					col++;
				}
			}
			rowNumber++;
		}

		autoSizeColumns(sheetData, maxColNum);

		wb.write(outputStream);
		outputStream.flush();
	}

	private void styleCell(Cell poiCell, RenderCell cell, StyleContext styleContext) {
		if (cell instanceof HeaderRenderCell)
			poiCell.setCellStyle(styleContext.headerStyle);
		if (cell instanceof GrandTotalHeaderRenderCell)
			poiCell.setCellStyle(styleContext.grandTotalStyle);
		if( cell instanceof GrandTotalValueRenderCell)
			poiCell.setCellStyle(styleContext.grandTotalStyle);
		if (cell instanceof DataHeaderRenderCell)
			poiCell.setCellStyle(styleContext.dataHeaderStyle);
	}

	private void autoSizeColumns(Sheet sheetData, int maxColNum) {
		try {
			// Autosize columns
			int width = 0;
			for (int col = 0; col < maxColNum; col++) {
				sheetData.autoSizeColumn(col);
				int cwidth = sheetData.getColumnWidth(col);
				cwidth += 500;
				sheetData.setColumnWidth(col, cwidth);
				width += cwidth;
			}

			// calculate zoom factor
			int nominator = 45000 * 100 / width;
			if (nominator < 100)
				sheetData.setZoom(nominator);

		} catch (HeadlessException he) {
			// No UI, no autosize :(
		}
	}

	@Override
	public String getFormatName() {
		return "XLS";
	}

	@Override
	public String getFormatMimetype() {
		return "application/vnd.ms-excel";
	}

	@Override
	public String getFilenameExtension() {
		return "xls";
	}
}
