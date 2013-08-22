/*
 * Copyright 2012 Decebal Suiu
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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.collections.MultiMap;
import org.apache.wicket.util.convert.IConverter;

import ro.fortsoft.wicket.pivot.PivotField;
import ro.fortsoft.wicket.pivot.PivotModel;
import ro.fortsoft.wicket.pivot.PivotUtils;
import ro.fortsoft.wicket.pivot.tree.Node;
import ro.fortsoft.wicket.pivot.tree.TreeHelper;

/**
 * @author Decebal Suiu
 */
public class PivotTable extends GenericPanel<PivotModel> {

	private static final long serialVersionUID = 1L;

	private Map<List<Object>, Integer> spanCache;
	
	public PivotTable(String id, PivotModel pivotModel) {
		super(id, Model.of(pivotModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		spanCache = new HashMap<List<Object>, Integer>();

		PivotModel pivotModel = getModelObject();
		
		List<PivotField> columnFields = pivotModel.getFields(PivotField.Area.COLUMN);
		List<PivotField> rowFields = pivotModel.getFields(PivotField.Area.ROW);
		List<PivotField> dataFields = pivotModel.getFields(PivotField.Area.DATA);
		
		int columnFieldsSize = columnFields.size();
		int rowFieldsSize = rowFields.size();
		int dataFieldsSize = dataFields.size();
		
		List<List<Object>> rowKeys = pivotModel.getRowKeys();
//		System.out.println("rowKeys = " + rowKeys);
		List<List<Object>> columnKeys = pivotModel.getColumnKeys();
//		System.out.println("columnKeys = " + columnKeys);
		
		// rendering header
		RepeatingView column = new RepeatingView("header");
		add(column);
		int headerRowCount = columnFieldsSize;
		if (headerRowCount == 0) {
			headerRowCount = 1;
		}
		if ((dataFieldsSize > 1) && (columnFieldsSize > 0)) {
			// add an extra row (the row with data field titles)
			headerRowCount++;
		}
		
		Component tmp = null;
		for (int i = 0; i < headerRowCount; i++) {
			// rendering row header (first columns)
			WebMarkupContainer tr = new WebMarkupContainer(column.newChildId());
			column.add(tr);
			RepeatingView rowHeader = new RepeatingView("rowHeader");
			tr.add(rowHeader);
			
			for (int j = 0; j < rowFieldsSize; j++) {
				if (i < headerRowCount - 1) {
					// rendering an empty cell
					tmp = new Label(rowHeader.newChildId(), "");
					tmp.add(AttributeModifier.append("class", "empty"));
					rowHeader.add(tmp);
				} else {
					// rendering row field
					tmp = createTitleLabel(rowHeader.newChildId(), rowFields.get(j));
					rowHeader.add(tmp);
				}
			}
			
			// rendering column keys
			RepeatingView value = new RepeatingView("value");
			tr.add(value);
			Node columnsRoot = pivotModel.getColumnsHeaderTree().getRoot();
			List<List<Object>> pathRenderedCache = new ArrayList<List<Object>>();
			for (List<Object> columnKey : columnKeys) {
//				System.out.println(">>> " + columnKey);
				if (i < columnFieldsSize) {
					PivotField columnField = columnFields.get(i);
//					System.out.println("+++ " + columnKey.get(i) + " <<< " + i);
					List<Object> path = new ArrayList<Object>(columnKey.subList(0, i + 1));
//					System.out.println("columnPath = " + path);
					int colspan = getSpan(columnsRoot, path);
//					System.out.println("### colspan = " + colspan);
					tmp = createValueLabel(value.newChildId(), columnKey.get(i), columnField);
					colspan = colspan * dataFieldsSize;
					tmp.add(AttributeModifier.append("colspan", colspan));
					value.add(tmp);
					// TODO optimization (create an emptyPanel is more optimal)
					if (pathRenderedCache.contains(path)) {
						tmp.setVisible(false);
					} else {
						pathRenderedCache.add(path);
					}
				} else {
					for (PivotField dataField : dataFields) {
						tmp = createTitleLabel(value.newChildId(), dataField);
						value.add(tmp);
					}
				}
			}
			
			// rendering grand total column
			RepeatingView grandTotalColumn = new RepeatingView("grandTotalColumn");
			if (i == 0) {
				tmp = new Label(grandTotalColumn.newChildId(), "Grand Total");
				tmp.add(AttributeModifier.append("colspan", dataFieldsSize));
				grandTotalColumn.add(tmp);
			} else if (i < columnFieldsSize) {
				tmp = new WebMarkupContainer(grandTotalColumn.newChildId());
				tmp.add(AttributeModifier.append("colspan", dataFieldsSize));
				tmp.add(AttributeModifier.append("class", "empty"));
				grandTotalColumn.add(tmp);
			} else {
				for (PivotField dataField : dataFields) {
					tmp = createTitleLabel(value.newChildId(), dataField);
					grandTotalColumn.add(tmp);
				}				
			}
			grandTotalColumn.setVisible(!columnFields.isEmpty() && pivotModel.isShowGrandTotalForRow());
			tr.add(grandTotalColumn);
		}
		
		// rendering rows
		RepeatingView row = new RepeatingView("row");
		add(row);
		Node rowsRoot = pivotModel.getRowsHeaderTree().getRoot();
		List<List<Object>> pathRenderedCache = new ArrayList<List<Object>>();
		for (List<Object> rowKey : rowKeys) {
			WebMarkupContainer tr = new WebMarkupContainer(row.newChildId());
			row.add(tr);
			RepeatingView rowHeader = new RepeatingView("rowHeader");
			tr.add(rowHeader);

			for (int k = 0; k < rowKey.size(); k++) {
				List<Object> path = new ArrayList<Object>(rowKey.subList(0, k + 1));
//				System.out.println("rowPath = " + path);
				int rowspan = getSpan(rowsRoot, path);
//				System.out.println("### rowspan = " + rowspan);

				PivotField rowField = rowFields.get(k);
				tmp = createValueLabel(rowHeader.newChildId(), rowKey.get(k), rowField);
				tmp.add(AttributeModifier.append("rowspan", rowspan));
				rowHeader.add(tmp);
				
				// TODO optimization (create an emptyPanel is more optimal)
				if (pathRenderedCache.contains(path)) {
					tmp.setVisible(false);
				} else {
					pathRenderedCache.add(path);
				}
			}
			
			RepeatingView value = new RepeatingView("value");
			tr.add(value);
			
			for (List<Object> columnKey : columnKeys) {
				for (PivotField dataField : dataFields) {
					Number cellValue = (Number) pivotModel.getValueAt(dataField, rowKey, columnKey);
					tmp = createValueLabel(value.newChildId(), cellValue, dataField);				
					value.add(tmp);					
				}
			}
				
			if (!columnFields.isEmpty() && pivotModel.isShowGrandTotalForRow()) {
				MultiMap<PivotField, Object> values = new MultiMap<PivotField, Object>();
				for (List<Object> columnKey: columnKeys) {
					for (PivotField dataField : dataFields) {
						values.addValue(dataField, pivotModel.getValueAt(dataField, rowKey, columnKey));
					}
				}
				for (PivotField dataField : dataFields) {
					double grandTotalForRow = PivotUtils.getSummary(dataField, values.get(dataField)).doubleValue();
					tmp = createGrandTotalLabel(value.newChildId(), grandTotalForRow, true);
					tmp.add(AttributeModifier.append("class", "grand-total"));
					value.add(tmp);
				}
			}
		}
		
		WebMarkupContainer grandTotalRow = new WebMarkupContainer("grandTotalRow");
		grandTotalRow.setVisible(!rowFields.isEmpty() && pivotModel.isShowGrandTotalForColumn());
		add(grandTotalRow);
		
		Label grandTotalRowHeader = new Label("rowHeader", "Grand Total");
		grandTotalRowHeader.add(AttributeModifier.append("colspan", rowFieldsSize));
		grandTotalRow.add(grandTotalRowHeader);
		
		RepeatingView value = new RepeatingView("value");
		grandTotalRow.add(value);
		Map<PivotField, Double> grandTotal = new HashMap<PivotField, Double>();
		for (List<Object> columnKey : columnKeys) {
			MultiMap<PivotField, Object> values = new MultiMap<PivotField, Object>();
			for (List<Object> rowKey: rowKeys) {
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
				tmp = createGrandTotalLabel(value.newChildId(), grandTotalForColumn, false);
				value.add(tmp);
			}
		}
		if (!columnFields.isEmpty() && pivotModel.isShowGrandTotalForRow()) {
			for (PivotField dataField : dataFields) {
				tmp = createGrandTotalLabel(value.newChildId(), grandTotal.get(dataField), true);
				value.add(tmp);
			}
		}
	}

	/**
	 * Retrieves a name that display the pivot table title (for fields on ROW and DATA areas) 
	 */
	protected Label createTitleLabel(String id, PivotField pivotField) {
		String title = pivotField.getTitle();
		if (pivotField.getArea().equals(PivotField.Area.DATA)) {
			title += " (" + pivotField.getAggregator().getFunction().toUpperCase() + ")"; 
		}

		return new Label(id, title);
	}

	protected Label createValueLabel(String id, Object value, final PivotField pivotField) {
		return new Label(id, Model.of((Serializable) value)) {
			
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public <C> IConverter<C> getConverter(Class<C> type) {
				IConverter<C> converter = (IConverter<C>) pivotField.getConverter();
				if (converter != null) {
					return converter;
				}
				
				return super.getConverter(type);
			}

		};
	}
	
	protected Label createGrandTotalLabel(String id, Object value, boolean forRow) {
		return new Label(id, Model.of((Serializable) value));
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
