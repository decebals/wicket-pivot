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
import java.util.List;

import com.asf.wicket.pivot.PivotField.Area;
import com.asf.wicket.pivot.tree.Tree;

/**
 * @author Decebal Suiu
 */
public interface PivotModel extends Serializable {
	
	/**
	 * 	Gets all the PivotFields.
	 */
	public List<PivotField> getFields();
	
	public List<PivotField> getFields(Area area);
	
	public PivotField getField(String name);
	
	public PivotDataSource getDataSource();
	
   /**
    * Calculates the pivot data.
    */
	public void calculate();
	
	public List<List<Object>> getRowKeys();
	
	public List<List<Object>> getColumnKeys();
	
	public Object getValueAt(PivotField dataField, List<Object> rowKey, List<Object> columnKey);

	public boolean isShowGrandTotalForColumn();
	
	public void setShowGrandTotalForColumn(boolean showGrandTotalForColumn);
	
	public boolean isShowGrandTotalForRow();
	
	public void setShowGrandTotalForRow(boolean showGrandTotalForRow);
	
	public Tree getColumnsHeaderTree(); // ?!
	
	public Tree getRowsHeaderTree(); // ?!

}
