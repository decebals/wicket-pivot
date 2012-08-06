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
package com.asf.wicket.pivot;

import java.io.Serializable;

/**
 * @author Decebal Suiu
 */
public interface PivotDataSource extends Serializable {

	public String getFieldName(int fieldIndex);
	
	public int getFieldIndex(String fieldName);
	
	public Class<?> getFieldType(int fieldIndex);
	
	/**
	 * Gets number of fields in this data source.
	 */
	public int getFieldCount();
	
	/**
	 * Gets the row count.
	 */
	public int getRowCount();
	
    /**
     * Gets the value at the specified field index and the row index. 
     */
    public Object getValueAt(int rowIndex, int fieldIndex);

	public Object getValueAt(int rowIndex, PivotField field);	
	
}
