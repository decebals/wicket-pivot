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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import ro.fortsoft.wicket.pivot.PivotModel;
import ro.fortsoft.wicket.pivot.web.PivotTableRenderModel;
import ro.fortsoft.wicket.pivot.web.PivotTableRenderModel.RenderCell;
import ro.fortsoft.wicket.pivot.web.PivotTableRenderModel.RenderRow;

/**
 * Basic CSV exporter
 */
public class PivotCsvExporter implements IPivotExporter {	
	private static final long serialVersionUID = 1L;
	private String seperator = ";";

	@Override	
	public void exportPivot(PivotModel pivotModel, OutputStream outputStream) throws IOException {
		PivotTableRenderModel renderModel = PivotTableRenderModel.create(pivotModel);

		OutputStreamWriter out = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"));
		Map<Integer, Integer> rowSpanMap = new HashMap<Integer, Integer>();

		for (RenderRow row : renderModel.getAllRenderRows()) {
			int col = 0;
			for (RenderCell cell : row.getRenderCells()) {
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
					out.append(seperator);
				}

				/*
				 * Output the Value
				 */
				Object rawValue = cell.getRawValue();
				if (rawValue != null)
					out.append(String.valueOf(rawValue));
				out.append(seperator);
				if (cell.getRowspan() > 1)
					rowSpanMap.put(col, cell.getRowspan() - 1);
				col++;

				/*
				 * We only support colspan _OR_ rowspan. The current PivotTable
				 * also doesnt have rowspan and colspan at the same time.
				 */
				for (int i = 1; i < cell.getColspan(); i++) {
					out.append(seperator);
					col++;
				}
			}
			out.append("\n");
		}
		out.flush();
	}

	public void setSeperator(String seperator) {
		this.seperator = seperator;
	}

	@Override
	public String getFormatName() {
		return "CSV";
	}

	@Override
	public String getFormatMimetype() {
		return "text/csv";
	}

	@Override
	public String getFilenameExtension() {
		return null;
	}
}
