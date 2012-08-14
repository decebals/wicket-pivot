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
package com.asf.wicket.pivot.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.util.collections.MultiMap;

import com.asf.wicket.pivot.PivotField;
import com.asf.wicket.pivot.PivotModel;
import com.asf.wicket.pivot.PivotUtils;

/**
 * @author Decebal Suiu
 */
public class PivotTable extends Panel {

	private static final long serialVersionUID = 1L;

	public PivotTable(String id, PivotModel pivotModel) {
		super(id);
		
		List<PivotField> columnFields = pivotModel.getFields(PivotField.Area.COLUMN);
		List<PivotField> rowFields = pivotModel.getFields(PivotField.Area.ROW);
		List<PivotField> dataFields = pivotModel.getFields(PivotField.Area.DATA);
		
		List<List<Object>> rowKeys = pivotModel.getRowKeys();
		List<List<Object>> columnKeys = pivotModel.getColumnKeys();
		
		// rendering header
		RepeatingView column = new RepeatingView("header");
		add(column);
		int headerRowCount = columnFields.size();
		if (dataFields.size() > 1) {
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
			
			for (int j = 0; j < rowFields.size(); j++) {
				if (i < headerRowCount - 1) {
					// rendering an empty cell
					tmp = new Label(rowHeader.newChildId(), "");
					tmp.add(AttributeModifier.append("class", "empty"));
					rowHeader.add(tmp);
				} else {
					// rendering row field title
					tmp = new Label(rowHeader.newChildId(), rowFields.get(j).getTitle());
					rowHeader.add(tmp);
				}
			}
			
			// rendering column keys
			RepeatingView value = new RepeatingView("value");
			tr.add(value);
			for (List<Object> columnKey : columnKeys) {
				if (i < columnFields.size()) {
					tmp = new Label(value.newChildId(), columnKey.get(i).toString());
					tmp.add(AttributeModifier.append("colspan", dataFields.size()));
					value.add(tmp);
				} else {
					for (PivotField dataField : dataFields) {
						tmp = new Label(value.newChildId(), dataField.getTitle());
						value.add(tmp);
					}
				}
			}
			
			// rendering grand total column
			RepeatingView grandTotalColumn = new RepeatingView("grandTotalColumn");
			if (i == 0) {
				tmp = new Label(grandTotalColumn.newChildId(), "Grand Total");
				tmp.add(AttributeModifier.append("colspan", dataFields.size()));
				grandTotalColumn.add(tmp);
			} else if (i < columnFields.size()) {
				tmp = new WebMarkupContainer(grandTotalColumn.newChildId());
				tmp.add(AttributeModifier.append("colspan", dataFields.size()));
				tmp.add(AttributeModifier.append("class", "empty"));
				grandTotalColumn.add(tmp);
			} else {
				for (PivotField dataField : dataFields) {
					tmp = new Label(value.newChildId(), dataField.getTitle());
					grandTotalColumn.add(tmp);
				}				
			}
			grandTotalColumn.setVisible(pivotModel.isShowGrandTotalForRow());
			tr.add(grandTotalColumn);
		}
		
		// rendering rows
		RepeatingView row = new RepeatingView("row");
		add(row);
		for (List<Object> rowKey : rowKeys) {
			WebMarkupContainer tr = new WebMarkupContainer(row.newChildId());
			row.add(tr);
			RepeatingView rowHeader = new RepeatingView("rowHeader");
			tr.add(rowHeader);

			for (Object value : rowKey) {
				tmp = new Label(rowHeader.newChildId(), value.toString());
				rowHeader.add(tmp);
			}
			
			RepeatingView value = new RepeatingView("value");
			tr.add(value);
			
			for (List<Object> columnKey : columnKeys) {
				for (PivotField dataField : dataFields) {
					Number cellValue = (Number) pivotModel.getValueAt(dataField, rowKey, columnKey);
					String valueAsString = convertValue(cellValue);
					tmp = new Label(value.newChildId(), valueAsString);
					value.add(tmp);					
				}
			}
				
			if (pivotModel.isShowGrandTotalForRow()) {
				MultiMap<PivotField, Object> values = new MultiMap<PivotField, Object>();
				for (List<Object> columnKey: columnKeys) {
					for (PivotField dataField : dataFields) {
						values.addValue(dataField, pivotModel.getValueAt(dataField, rowKey, columnKey));
					}
				}
				for (PivotField dataField : dataFields) {
					double grandTotalForRow = PivotUtils.getSummary(dataField, values.get(dataField)).doubleValue();
					String valueAsString = String.valueOf(grandTotalForRow);
					tmp = new Label(value.newChildId(), valueAsString);
					tmp.add(AttributeModifier.append("class", "grand-total"));
					value.add(tmp);
				}
			}
		}
		
		WebMarkupContainer grandTotalRow = new WebMarkupContainer("grandTotalRow");
		grandTotalRow.setVisible(pivotModel.isShowGrandTotalForColumn());
		add(grandTotalRow);
		
		Label grandTotalRowHeader = new Label("rowHeader", "Grand Total");
		grandTotalRowHeader.add(AttributeModifier.append("colspan", rowFields.size()));
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
				String valueAsString = String.valueOf(grandTotalForColumn);
				tmp = new Label(value.newChildId(), valueAsString);
				value.add(tmp);
			}
		}
		if (pivotModel.isShowGrandTotalForRow()) {
			for (PivotField dataField : dataFields) {
				tmp = new Label(value.newChildId(), String.valueOf(grandTotal.get(dataField)));
				value.add(tmp);
			}
		}
	}

	// TODO maybe converters for each data type?
	private String convertValue(Object value) {
		return (value == null) ? "" : value.toString();
	}
	
}
