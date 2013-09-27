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
package ro.fortsoft.wicket.pivot.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.util.collections.MultiMap;

import ro.fortsoft.wicket.pivot.PivotField;
import ro.fortsoft.wicket.pivot.PivotModel;
import ro.fortsoft.wicket.pivot.PivotUtils;
import ro.fortsoft.wicket.pivot.tree.Node;
import ro.fortsoft.wicket.pivot.tree.TreeHelper;

/**
 * Render Model of the PivotTable. This is independant of the resulting output
 * format.
 */
public class PivotTableRenderModel implements Serializable {
	private static final long serialVersionUID = 1L;
	private Map<List<Object>, Integer> spanCache;

	public static abstract class RenderCell implements Serializable {
		private static final long serialVersionUID = 1L;
		PivotField pivotField;
		Object value;
		int colspan = 1;
		int rowspan = 1;

		protected RenderCell() {
		}

		public Object getRawValue() {
			return value;
		}
	}

	public static class RowValueRenderCell extends RenderCell {
		private static final long serialVersionUID = 1L;

		public RowValueRenderCell(Number cellValue, PivotField dataField) {
			value = cellValue;
			this.pivotField = dataField;
		}

	}

	public static class GrandTotalValueRenderCell extends RenderCell {
		private static final long serialVersionUID = 1L;
		boolean forRow;

		public GrandTotalValueRenderCell(double grandTotalForRow, boolean forRow) {
			value = grandTotalForRow;
			this.forRow = forRow;
		}
	}

	public static class RowHeaderRenderCell extends RenderCell {
		private static final long serialVersionUID = 1L;

		public RowHeaderRenderCell(Object value, PivotField rowField) {
			this.value = value;
			this.pivotField = rowField;
		}
	}

	public static class HeaderRenderCell extends RenderCell {
		private static final long serialVersionUID = 1L;

		public HeaderRenderCell(PivotField pivotField) {
			this.pivotField = pivotField;
			if (pivotField != null)
				value = pivotField.getTitle();
		}
	}

	public static class HeaderValueRenderCell extends HeaderRenderCell {
		private static final long serialVersionUID = 1L;

		public HeaderValueRenderCell(Object value, PivotField pivotField) {
			super(pivotField);
			this.value = value;
		}
	}

	public static class GrandTotalHeaderRenderCell extends RenderCell {
		public GrandTotalHeaderRenderCell(Object value) {
			this.value = value;
		}

		private static final long serialVersionUID = 1L;
	}

	public static class GrandTotalRowHeaderRenderCell extends GrandTotalHeaderRenderCell {
		private static final long serialVersionUID = 1L;

		public GrandTotalRowHeaderRenderCell(String value) {
			super(value);
		}

	}

	public static abstract class RenderRow implements Serializable {
		private static final long serialVersionUID = 1L;

		public abstract List<RenderCell> getRenderCells();
	}

	public static class HeaderRenderRow extends RenderRow {
		private static final long serialVersionUID = 1L;
		private List<HeaderRenderCell> rowHeader = new ArrayList<HeaderRenderCell>();
		private List<RenderCell> value = new ArrayList<RenderCell>();
		private List<RenderCell> grandTotalColumn = new ArrayList<RenderCell>();

		@Override
		public List<RenderCell> getRenderCells() {
			List<RenderCell> ret = new ArrayList<RenderCell>();
			ret.addAll(rowHeader);
			ret.addAll(value);
			ret.addAll(grandTotalColumn);
			return ret;
		}

		public List<HeaderRenderCell> getRowHeader() {
			return rowHeader;
		}

		public List<RenderCell> getValueCells() {
			return value;
		}

		public List<RenderCell> getGrandTotalColumn() {
			return grandTotalColumn;
		}
	}

	public static class DataRenderRow extends RenderRow {
		private static final long serialVersionUID = 1L;
		List<RowHeaderRenderCell> rowHeader = new ArrayList<RowHeaderRenderCell>();
		List<RenderCell> value = new ArrayList<RenderCell>();

		@Override
		public List<RenderCell> getRenderCells() {
			List<RenderCell> ret = new ArrayList<RenderCell>();
			ret.addAll(rowHeader);
			ret.addAll(value);
			return ret;
		}
	}

	public static class GrandTotalRenderRow extends RenderRow {
		private static final long serialVersionUID = 1L;
		List<GrandTotalRowHeaderRenderCell> rowHeader = new ArrayList<GrandTotalRowHeaderRenderCell>();
		List<GrandTotalValueRenderCell> value = new ArrayList<GrandTotalValueRenderCell>();

		@Override
		public List<RenderCell> getRenderCells() {
			List<RenderCell> ret = new ArrayList<RenderCell>();
			ret.addAll(rowHeader);
			ret.addAll(value);
			return ret;
		}
	}

	private List<HeaderRenderRow> column;
	private List<DataRenderRow> row;
	private List<GrandTotalRenderRow> grandTotalRow;

	public List<HeaderRenderRow> getHeaderRows() {
		return column;
	}

	public List<DataRenderRow> getValueRows() {
		return row;
	}

	public List<GrandTotalRenderRow> getGrandTotalRows() {
		return grandTotalRow;
	}

	public List<RenderRow> getAllRenderRows() {
		List<RenderRow> ret = new ArrayList<RenderRow>();
		ret.addAll(column);
		ret.addAll(row);
		ret.addAll(grandTotalRow);
		return ret;
	}

	public void calculate(PivotModel pivotModel) {
		spanCache = new HashMap<List<Object>, Integer>();
		column = new ArrayList<HeaderRenderRow>();
		row = new ArrayList<DataRenderRow>();
		grandTotalRow = new ArrayList<GrandTotalRenderRow>();

		List<PivotField> columnFields = pivotModel.getFields(PivotField.Area.COLUMN);
		List<PivotField> rowFields = pivotModel.getFields(PivotField.Area.ROW);
		List<PivotField> dataFields = pivotModel.getFields(PivotField.Area.DATA);

		int columnFieldsSize = columnFields.size();
		int rowFieldsSize = rowFields.size();
		int dataFieldsSize = dataFields.size();

		List<List<Object>> rowKeys = pivotModel.getRowKeys();
		List<List<Object>> columnKeys = pivotModel.getColumnKeys();

		// rendering header
		int headerRowCount = columnFieldsSize;
		if (headerRowCount == 0) {
			headerRowCount = 1;
		}
		if ((dataFieldsSize > 1) && (columnFieldsSize > 0)) {
			// add an extra row (the row with data field titles)
			headerRowCount++;
		}

		for (int i = 0; i < headerRowCount; i++) {
			// rendering row header (first columns)
			HeaderRenderRow tr = new HeaderRenderRow();
			column.add(tr);

			for (int j = 0; j < rowFieldsSize; j++) {
				if (i < headerRowCount - 1) {
					// rendering empty cell
					tr.rowHeader.add(new HeaderRenderCell(null));
				} else {
					// rendering row field
					tr.rowHeader.add(new HeaderRenderCell(rowFields.get(j)));
				}
			}

			// rendering column keys
			Node columnsRoot = pivotModel.getColumnsHeaderTree().getRoot();
			List<List<Object>> pathRenderedCache = new ArrayList<List<Object>>();
			for (List<Object> columnKey : columnKeys) {
				if (i < columnFieldsSize) {
					PivotField columnField = columnFields.get(i);
					List<Object> path = new ArrayList<Object>(columnKey.subList(0, i + 1));
					int colspan = getSpan(columnsRoot, path);
					colspan = colspan * dataFieldsSize;

					HeaderValueRenderCell valueRenderCell = new HeaderValueRenderCell(columnKey.get(i), columnField);
					valueRenderCell.colspan = colspan;

					if (pathRenderedCache.contains(path)) {
						/*
						 * Was: setVisible(false). We just dont add the field
						 * here.
						 */
					} else {
						tr.value.add(valueRenderCell);
						pathRenderedCache.add(path);
					}
				} else {
					for (PivotField dataField : dataFields) {
						tr.value.add(new HeaderRenderCell(dataField));
					}
				}
			}

			if (!columnFields.isEmpty() && pivotModel.isShowGrandTotalForRow()) {
				// rendering grand total column
				if (i == 0) {
					GrandTotalHeaderRenderCell cell = new GrandTotalHeaderRenderCell("Grand Total");
					cell.colspan = dataFieldsSize;
					tr.grandTotalColumn.add(cell);
				} else if (i < columnFieldsSize) {
					GrandTotalHeaderRenderCell cell = new GrandTotalHeaderRenderCell(null);
					cell.colspan = dataFieldsSize;
					tr.grandTotalColumn.add(cell);
				} else {
					for (PivotField dataField : dataFields) {
						HeaderRenderCell cell = new HeaderRenderCell(dataField);
						tr.grandTotalColumn.add(cell);
					}
				}
			}
		}

		// rendering rows
		Node rowsRoot = pivotModel.getRowsHeaderTree().getRoot();
		List<List<Object>> pathRenderedCache = new ArrayList<List<Object>>();
		for (List<Object> rowKey : rowKeys) {
			DataRenderRow tr = new DataRenderRow();
			row.add(tr);

			for (int k = 0; k < rowKey.size(); k++) {
				List<Object> path = new ArrayList<Object>(rowKey.subList(0, k + 1));
				int rowspan = getSpan(rowsRoot, path);

				PivotField rowField = rowFields.get(k);
				RowHeaderRenderCell cell = new RowHeaderRenderCell(rowKey.get(k), rowField);
				cell.rowspan = rowspan;

				// TODO optimization (create an emptyPanel is more optimal)
				if (pathRenderedCache.contains(path)) {
					/* tmp.setVisible(false); */
				} else {
					pathRenderedCache.add(path);
					tr.rowHeader.add(cell);
				}
			}

			for (List<Object> columnKey : columnKeys) {
				for (PivotField dataField : dataFields) {
					Number cellValue = (Number) pivotModel.getValueAt(dataField, rowKey, columnKey);
					tr.value.add(new RowValueRenderCell(cellValue, dataField));
				}
			}

			if (!columnFields.isEmpty() && pivotModel.isShowGrandTotalForRow()) {
				MultiMap<PivotField, Object> values = new MultiMap<PivotField, Object>();
				for (List<Object> columnKey : columnKeys) {
					for (PivotField dataField : dataFields) {
						values.addValue(dataField, pivotModel.getValueAt(dataField, rowKey, columnKey));
					}
				}
				for (PivotField dataField : dataFields) {
					double grandTotalForRow = PivotUtils.getSummary(dataField, values.get(dataField)).doubleValue();
					GrandTotalValueRenderCell cell = new GrandTotalValueRenderCell(grandTotalForRow, true);
					tr.value.add(cell);
				}
			}
		}

		if (!rowFields.isEmpty() && pivotModel.isShowGrandTotalForColumn()) {
			GrandTotalRenderRow tr = new GrandTotalRenderRow();
			grandTotalRow.add(tr);

			GrandTotalRowHeaderRenderCell grandTotalCell = new GrandTotalRowHeaderRenderCell("Grand Total");
			tr.rowHeader.add(grandTotalCell);
			grandTotalCell.colspan = rowFieldsSize;

			Map<PivotField, Double> grandTotal = new HashMap<PivotField, Double>();
			for (List<Object> columnKey : columnKeys) {
				MultiMap<PivotField, Object> values = new MultiMap<PivotField, Object>();
				for (List<Object> rowKey : rowKeys) {
					for (PivotField dataField : dataFields) {
						values.addValue(dataField, pivotModel.getValueAt(dataField, rowKey, columnKey));
					}
				}
				for (PivotField dataField : dataFields) {
					double grandTotalForColumn = PivotUtils.getSummary(dataField, values.get(dataField)).doubleValue();
					if (!grandTotal.containsKey(dataField)) {
						grandTotal.put(dataField, grandTotalForColumn);
					} else {
						grandTotal.put(dataField, grandTotal.get(dataField) + grandTotalForColumn);
					}

					tr.value.add(new GrandTotalValueRenderCell(grandTotalForColumn, false));
				}
			}
			if (!columnFields.isEmpty() && pivotModel.isShowGrandTotalForRow()) {
				for (PivotField dataField : dataFields) {
					tr.value.add(new GrandTotalValueRenderCell(grandTotal.get(dataField), true));
				}
			}
		}
	}

	private int getSpan(Node root, List<Object> path) {
		if (spanCache.containsKey(path)) {
			return spanCache.get(path);
		}

		// quick and dirty
		Node node = TreeHelper.getNode(root, path);

		int span = 1;
		if (!node.isLeaf()) {
			span = TreeHelper.getLeafs(node).size();
		}
		return span;
	}
}
