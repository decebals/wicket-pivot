package ro.fortsoft.wicket.pivot.web;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import ro.fortsoft.wicket.pivot.PivotModel;
import ro.fortsoft.wicket.pivot.web.PivotTableRenderModel.RenderCell;
import ro.fortsoft.wicket.pivot.web.PivotTableRenderModel.RenderRow;

public class PivotCsvExporter {
	private String seperator = ";";

	public void exportPivot(PivotModel pivotModel, OutputStream outputStream) throws IOException {
		PivotTableRenderModel renderModel = new PivotTableRenderModel();
		renderModel.calculate(pivotModel);

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
				if (cell.rowspan > 1)
					rowSpanMap.put(col, cell.rowspan - 1);
				col++;

				/*
				 * We only support colspan _OR_ rowspan. The current PivotTable
				 * also doesnt have rowspan and colspan at the same time.
				 */
				for (int i = 1; i < cell.colspan; i++) {
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
}
