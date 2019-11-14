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
package ro.fortsoft.wicket.pivot;

import org.apache.wicket.util.collections.MultiMap;

import ro.fortsoft.wicket.pivot.FieldCalculation.FieldValueProvider;
import ro.fortsoft.wicket.pivot.tree.Node;
import ro.fortsoft.wicket.pivot.tree.TreeHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Render Model of the PivotTable. This is independant of the resulting output
 * format.
 */
public class PivotTableRenderModel implements Serializable {
	private static final long serialVersionUID = 1L;

	private PivotTableRenderModel() {
	}

	private Map<List<Object>, Integer> spanCache;

	/**
	 * Get values from pre summed column / row sums. 
	 */
	private static final class FieldValueProviderFromSummedValues implements FieldValueProvider {
		private final MultiMap<PivotField, Object> values;

		private FieldValueProviderFromSummedValues(MultiMap<PivotField, Object> values) {
			this.values = values;
		}

		@Override
		public Object getFieldValue(PivotField field) {
			// If the field is not in the regular
			// data, we can not return a sum of it
			if (!values.containsKey(field))
				return 0;
			double sum = 0;
			List<Object> items = values.get(field);
			for (Object item : items) {
				if (item != null) {
					sum += ((Number) item).doubleValue();
				}
			}
			return sum;
		}
	}

	public static abstract class RenderCell implements Serializable {
		private static final long serialVersionUID = 1L;
		PivotField pivotField;
		Object value;
		int colspan = 1;
		int rowspan = 1;

		protected RenderCell() {
		}

		public PivotField getPivotField() {
			return pivotField;
		}

		public Object getRawValue() {
			return value;
		}

		public int getColspan() {
			return colspan;
		}

		public int getRowspan() {
			return rowspan;
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

	public static class DataHeaderRenderCell extends RenderCell {
		private static final long serialVersionUID = 1L;

		public DataHeaderRenderCell(Object value, PivotField rowField) {
			this.value = value;
			this.pivotField = rowField;
		}
	}

	public static class DataValueRenderCell extends RenderCell {
		private static final long serialVersionUID = 1L;

		public DataValueRenderCell(Number cellValue, PivotField dataField) {
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

		public boolean isForRow() {
			return forRow;
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
		private List<HeaderRenderCell> rowHeader = new ArrayList<>();
		private List<RenderCell> value = new ArrayList<>();
		private List<RenderCell> grandTotalColumn = new ArrayList<>();

		@Override
		public List<RenderCell> getRenderCells() {
			List<RenderCell> ret = new ArrayList<>();
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
		List<DataHeaderRenderCell> rowHeader = new ArrayList<>();
		List<RenderCell> value = new ArrayList<>();

		@Override
		public List<RenderCell> getRenderCells() {
			List<RenderCell> ret = new ArrayList<>();
			ret.addAll(rowHeader);
			ret.addAll(value);
			return ret;
		}

		public List<DataHeaderRenderCell> getRowHeader() {
			return rowHeader;
		}

		public List<RenderCell> getValue() {
			return value;
		}
	}

	public static class GrandTotalRenderRow extends RenderRow {
		private static final long serialVersionUID = 1L;
		List<GrandTotalRowHeaderRenderCell> rowHeader = new ArrayList<>();
		List<GrandTotalValueRenderCell> value = new ArrayList<>();

		@Override
		public List<RenderCell> getRenderCells() {
			List<RenderCell> ret = new ArrayList<>();
			ret.addAll(rowHeader);
			ret.addAll(value);
			return ret;
		}

		public List<GrandTotalRowHeaderRenderCell> getRowHeader() {
			return rowHeader;
		}

		public List<GrandTotalValueRenderCell> getValue() {
			return value;
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
		List<RenderRow> ret = new ArrayList<>();
		ret.addAll(column);
		ret.addAll(row);
		ret.addAll(grandTotalRow);
		return ret;
	}

	public static PivotTableRenderModel create(PivotModel pivotModel) {
		PivotTableRenderModel renderModel = new PivotTableRenderModel();
		renderModel.calculate(pivotModel);
		return renderModel;
	}

	private void calculate(PivotModel pivotModel) {
		spanCache = new HashMap<>();
		column = new ArrayList<>();
		row = new ArrayList<>();
		grandTotalRow = new ArrayList<>();

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

			for (PivotField rowField : rowFields) {
				if (i < headerRowCount - 1) {
					// rendering empty cell
					tr.rowHeader.add(new HeaderRenderCell(null));
				} else {
					// rendering row field
					tr.rowHeader.add(new HeaderRenderCell(rowField));
				}
			}

			// rendering column keys
			Node columnsRoot = pivotModel.getColumnsHeaderTree().getRoot();
			List<List<Object>> pathRenderedCache = new ArrayList<>();
			for (List<Object> columnKey : columnKeys) {
				if (i < columnFieldsSize) {
					PivotField columnField = columnFields.get(i);
					List<Object> path = new ArrayList<>(columnKey.subList(0, i + 1));
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
		List<List<Object>> pathRenderedCache = new ArrayList<>();
		for (List<Object> rowKey : rowKeys) {
			DataRenderRow tr = new DataRenderRow();
			row.add(tr);

			for (int k = 0; k < rowKey.size(); k++) {
				List<Object> path = new ArrayList<>(rowKey.subList(0, k + 1));
				int rowspan = getSpan(rowsRoot, path);

				PivotField rowField = rowFields.get(k);
				DataHeaderRenderCell cell = new DataHeaderRenderCell(rowKey.get(k), rowField);
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
					tr.value.add(new DataValueRenderCell(cellValue, dataField));
				}
			}

			if (!columnFields.isEmpty() && pivotModel.isShowGrandTotalForRow()) {
				final MultiMap<PivotField, Object> values = new MultiMap<>();
				for (List<Object> columnKey : columnKeys) {
					for (PivotField dataField : dataFields) {
						values.addValue(dataField, pivotModel.getValueAt(dataField, rowKey, columnKey));
					}
				}
				for (PivotField dataField : dataFields) {
					double grandTotalForRow = 0.0d;
					if (dataField.getFieldCalculation() != null) {
						grandTotalForRow = PivotUtils.getSummary(dataField, Collections.emptyList(),
								new FieldValueProviderFromSummedValues(values)).doubleValue();
					} 
					else {
                        List<Object> items = values.get(dataField);
                        for (Object item : items) {
                            if (item != null) {
                                grandTotalForRow += ((Number) item).doubleValue();
                            }
                        }
					}
                    tr.value.add(new GrandTotalValueRenderCell(grandTotalForRow, true));
				}
			}
		}

		if (!rowFields.isEmpty() && pivotModel.isShowGrandTotalForColumn()) {
			GrandTotalRenderRow tr = new GrandTotalRenderRow();
			grandTotalRow.add(tr);

			GrandTotalRowHeaderRenderCell grandTotalCell = new GrandTotalRowHeaderRenderCell("Grand Total");
			tr.rowHeader.add(grandTotalCell);
			grandTotalCell.colspan = rowFieldsSize;

			final Map<PivotField, Double> grandTotal = new HashMap<>();
			for (List<Object> columnKey : columnKeys) {
				MultiMap<PivotField, Object> values = new MultiMap<>();
				for (List<Object> rowKey : rowKeys) {
					for (PivotField dataField : dataFields) {
						if( dataField.getFieldCalculation() == null)
							values.addValue(dataField, pivotModel.getValueAt(dataField, rowKey, columnKey));
					}
				}
				for (PivotField dataField : dataFields) {
					double grandTotalForColumn = 0.0d;
					if (dataField.getFieldCalculation() != null) {
						grandTotalForColumn = PivotUtils.getSummary(dataField, Collections.emptyList(),
								new FieldValueProviderFromSummedValues(values)).doubleValue();
					}
					else {
						// We can sum all row values
                        List<Object> items = values.get(dataField);
                        for (Object item : items) {
                            if (item != null) {
                                grandTotalForColumn += ((Number) item).doubleValue();
                            }
                        }

                        if (!grandTotal.containsKey(dataField)) {
                            grandTotal.put(dataField, grandTotalForColumn);
                        } else {
                            grandTotal.put(dataField, grandTotal.get(dataField) + grandTotalForColumn);
                        }

					}
                    tr.value.add(new GrandTotalValueRenderCell(grandTotalForColumn, false));
				}
			}
			if (!columnFields.isEmpty() && pivotModel.isShowGrandTotalForRow()) {
				for (PivotField dataField : dataFields) {
					if (dataField.getFieldCalculation() != null) {
						double grandTotalValue = PivotUtils.getSummary(dataField, Collections.emptyList(),
								field -> {
									// If the field is not in the regular
									// data, we can not return a sum of it
									if (!grandTotal.containsKey(field))
										return 0;
									return grandTotal.get(field);
								}).doubleValue();

						tr.value.add(new GrandTotalValueRenderCell(grandTotalValue, true));
					}
					else {
						tr.value.add(new GrandTotalValueRenderCell(grandTotal.get(dataField), true));
					}
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
