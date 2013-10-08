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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;

import ro.fortsoft.wicket.pivot.PivotField;
import ro.fortsoft.wicket.pivot.PivotModel;
import ro.fortsoft.wicket.pivot.PivotTableRenderModel;
import ro.fortsoft.wicket.pivot.PivotTableRenderModel.DataHeaderRenderCell;
import ro.fortsoft.wicket.pivot.PivotTableRenderModel.DataRenderRow;
import ro.fortsoft.wicket.pivot.PivotTableRenderModel.DataValueRenderCell;
import ro.fortsoft.wicket.pivot.PivotTableRenderModel.GrandTotalHeaderRenderCell;
import ro.fortsoft.wicket.pivot.PivotTableRenderModel.GrandTotalRenderRow;
import ro.fortsoft.wicket.pivot.PivotTableRenderModel.GrandTotalRowHeaderRenderCell;
import ro.fortsoft.wicket.pivot.PivotTableRenderModel.GrandTotalValueRenderCell;
import ro.fortsoft.wicket.pivot.PivotTableRenderModel.HeaderRenderCell;
import ro.fortsoft.wicket.pivot.PivotTableRenderModel.HeaderRenderRow;
import ro.fortsoft.wicket.pivot.PivotTableRenderModel.HeaderValueRenderCell;
import ro.fortsoft.wicket.pivot.PivotTableRenderModel.RenderCell;

/**
 * @author Decebal Suiu
 */
public class PivotTable extends GenericPanel<PivotModel> {

	private static final long serialVersionUID = 1L;

	public PivotTable(String id, PivotModel pivotModel) {
		super(id, Model.of(pivotModel));
	}

	private Component applyRowColSpan(RenderCell cell, Component tmp) {
		if (cell.getColspan() > 1)
			tmp.add(AttributeModifier.append("colspan", cell.getColspan()));
		if (cell.getRowspan() > 1)
			tmp.add(AttributeModifier.append("rowspan", cell.getRowspan()));
		return tmp;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		PivotModel pivotModel = getModelObject();
		PivotTableRenderModel renderModel =	PivotTableRenderModel.create(pivotModel);

		// rendering header
		RepeatingView column = new RepeatingView("header");
		add(column);

		Component tmp = null;
		for (HeaderRenderRow row : renderModel.getHeaderRows()) {
			// rendering row header (first columns)
			WebMarkupContainer tr = new WebMarkupContainer(column.newChildId());
			column.add(tr);
			RepeatingView rowHeader = new RepeatingView("rowHeader");
			tr.add(rowHeader);

			for (HeaderRenderCell cell : row.getRowHeader()) {
				if (cell.getPivotField() == null) {
					// rendering an empty cell
					tmp = new Label(rowHeader.newChildId(), "");
					tmp.add(AttributeModifier.append("class", "empty"));
					applyRowColSpan(cell, tmp);
					rowHeader.add(tmp);
				} else {
					// rendering row field
					tmp = createTitleLabel(rowHeader.newChildId(), cell.getPivotField());
					applyRowColSpan(cell, tmp);
					rowHeader.add(tmp);
				}
			}

			// rendering column keys
			RepeatingView value = new RepeatingView("value");
			tr.add(value);
			for (RenderCell cell : row.getValueCells()) {
				if (cell instanceof HeaderValueRenderCell) {
					HeaderValueRenderCell headerValueRenderCell = (HeaderValueRenderCell) cell;
					tmp = createValueLabel(value.newChildId(), headerValueRenderCell.getRawValue(),
							headerValueRenderCell.getPivotField());
					applyRowColSpan(cell, tmp);
					value.add(tmp);
				} else {
					HeaderRenderCell headerRenderCell = (HeaderRenderCell) cell;
					tmp = createTitleLabel(value.newChildId(), headerRenderCell.getPivotField());
					applyRowColSpan(cell, tmp);
					value.add(tmp);
				}
			}

			// rendering grand total column
			RepeatingView grandTotalColumn = new RepeatingView("grandTotalColumn");
			for (RenderCell cell : row.getGrandTotalColumn()) {
				if (cell instanceof GrandTotalHeaderRenderCell) {
					GrandTotalHeaderRenderCell grandTotalHeaderRenderCell = (GrandTotalHeaderRenderCell) cell;
					if (grandTotalHeaderRenderCell.getRawValue() != null) {
						tmp = new Label(grandTotalColumn.newChildId(), grandTotalHeaderRenderCell.getRawValue()
								.toString());
						applyRowColSpan(cell, tmp);
						grandTotalColumn.add(tmp);
					} else {
						tmp = new WebMarkupContainer(grandTotalColumn.newChildId());
						applyRowColSpan(cell, tmp);
						tmp.add(AttributeModifier.append("class", "empty"));
						grandTotalColumn.add(tmp);
					}
				} else {
					HeaderRenderCell headerCell = (HeaderRenderCell) cell;
					tmp = createTitleLabel(value.newChildId(), headerCell.getPivotField());
					applyRowColSpan(cell, tmp);
					grandTotalColumn.add(tmp);
				}
			}
			grandTotalColumn.setVisible(row.getGrandTotalColumn().size() > 0);
			tr.add(grandTotalColumn);
		}

		// rendering rows
		RepeatingView row = new RepeatingView("row");
		add(row);
		for (DataRenderRow renderRow : renderModel.getValueRows()) {
			WebMarkupContainer tr = new WebMarkupContainer(row.newChildId());
			row.add(tr);
			RepeatingView rowHeader = new RepeatingView("rowHeader");
			tr.add(rowHeader);

			for (DataHeaderRenderCell cell : renderRow.getRowHeader()) {
				tmp = createValueLabel(rowHeader.newChildId(), cell.getRawValue(), cell.getPivotField());
				applyRowColSpan(cell, tmp);
				rowHeader.add(tmp);
			}

			RepeatingView value = new RepeatingView("value");
			tr.add(value);

			for (RenderCell cell : renderRow.getValue()) {
				if (cell instanceof DataValueRenderCell) {
					tmp = createValueLabel(value.newChildId(), cell.getRawValue(), cell.getPivotField());
					applyRowColSpan(cell, tmp);
					value.add(tmp);
				} else {
					GrandTotalValueRenderCell grandTotalCell = (GrandTotalValueRenderCell) cell;
					tmp = createGrandTotalLabel(value.newChildId(), grandTotalCell.getRawValue(), grandTotalCell.isForRow());
					applyRowColSpan(cell, tmp);
					tmp.add(AttributeModifier.append("class", "grand-total"));
					value.add(tmp);
				}
			}
		}

		WebMarkupContainer grandTotalRow = new WebMarkupContainer("grandTotalRow");
		grandTotalRow.setVisible(renderModel.getGrandTotalRows().size() > 0);
		add(grandTotalRow);
		/*
		 * We currently expect exactly one GrantTotalRenderRow, therefor we dont
		 * need a repeating viewer
		 */
		for (GrandTotalRenderRow grantTotalRenderRow : renderModel.getGrandTotalRows()) {
			for (GrandTotalRowHeaderRenderCell cell : grantTotalRenderRow.getRowHeader()) {
				Label grandTotalRowHeader = new Label("rowHeader", "Grand Total");
				applyRowColSpan(cell, grandTotalRowHeader);
				grandTotalRow.add(grandTotalRowHeader);
			}

			RepeatingView value = new RepeatingView("value");
			grandTotalRow.add(value);
			for (GrandTotalValueRenderCell cell : grantTotalRenderRow.getValue()) {
				tmp = createGrandTotalLabel(value.newChildId(), cell.getRawValue(), cell.isForRow());
				value.add(tmp);
			}
		}
	}

	/**
	 * Retrieves a name that display the pivot table title (for fields on ROW
	 * and DATA areas)
	 */
	protected Label createTitleLabel(String id, PivotField pivotField) {
		String title = pivotField.getTitle();
		if (pivotField.getArea().equals(PivotField.Area.DATA)) {
			title += " (" + pivotField.getCalculationDescription() + ")";
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
}
