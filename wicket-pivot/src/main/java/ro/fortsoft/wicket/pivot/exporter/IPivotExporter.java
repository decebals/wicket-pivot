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
import java.io.Serializable;

import ro.fortsoft.wicket.pivot.PivotModel;

/**
 * Interface for the pivot exporter plugins
 */
public interface IPivotExporter extends Serializable {
	/**
	 * @return how is this export named? E.g. CSV
	 */
	String getFormatName();

	/**
	 * @return the filename extension for the download without the ".", e.g. csv
	 *         or xls
	 */
	String getFilenameExtension();

	/**
	 * 
	 * @return the mimetype
	 */
	String getFormatMimetype();

	/**
	 * Export the given PivotModel into the outputStream.
	 */
	void exportPivot(PivotModel pivotModel, OutputStream outputStream) throws IOException;
}
