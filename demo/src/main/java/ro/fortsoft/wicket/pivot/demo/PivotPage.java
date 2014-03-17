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
package ro.fortsoft.wicket.pivot.demo;

import org.apache.wicket.markup.html.WebPage;

import ro.fortsoft.wicket.pivot.PivotDataSource;
import ro.fortsoft.wicket.pivot.PivotField;
import ro.fortsoft.wicket.pivot.PivotModel;
import ro.fortsoft.wicket.pivot.web.PivotPanel;

/**
 * @author Decebal Suiu
 */
public class PivotPage extends WebPage {

	private static final long serialVersionUID = 1L;

	public PivotPage() {
		super();

		// create a pivot data source
		PivotDataSource pivotDataSource = PivotDataSourceHandler.getPivotDataSource();
		System.out.println("pivotDataSource = " + pivotDataSource);
//		System.out.println("fieldCount = " + pivotDataSource.getFieldCount());
//		System.out.println("rowCount = " + pivotDataSource.getRowCount());

		PivotPanel pivotPanel = new PivotPanel("pivot", pivotDataSource) {

			private static final long serialVersionUID = 1L;

			@Override
			protected PivotModel createPivotModel(PivotDataSource pivotDataSource) {
				PivotModel pivotModel = super.createPivotModel(pivotDataSource);

				// add some fields on some area
				pivotModel.getField("REGION").setArea(PivotField.Area.ROW);
				pivotModel.getField("SALESMAN").setArea(PivotField.Area.ROW).setAreaIndex(1);
				pivotModel.getField("YEAR").setArea(PivotField.Area.COLUMN);
				pivotModel.getField("MONTH").setArea(PivotField.Area.COLUMN).setAreaIndex(1);
				pivotModel.getField("SALES").setArea(PivotField.Area.DATA);

				// set an aggregator for a data pivot field
//				pivotModel.getField("SALES").setAggregator(new Aggregator.Count());
				
				// set a custom converter for a pivot field
				/*
				pivotModel.getField("SALES").setConverter(new DoubleConverter() {

					private static final long serialVersionUID = 1L;

					@Override
					public NumberFormat getNumberFormat(Locale locale) {
						NumberFormat format = super.getNumberFormat(locale);
						format.setMinimumFractionDigits(2);
						
						return format;
					}
					
				});
				*/
				
				// show grand totals
				pivotModel.setShowGrandTotalForColumn(true);
				pivotModel.setShowGrandTotalForRow(true);

				return pivotModel;
			}

		};
		pivotPanel.setPivotConfigStorage(new PivotConfigStorageSession());
		add(pivotPanel);
	}

}
