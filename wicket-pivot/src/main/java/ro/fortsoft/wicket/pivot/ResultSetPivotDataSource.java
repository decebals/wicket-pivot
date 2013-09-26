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
package ro.fortsoft.wicket.pivot;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Decebal Suiu
 */
public class ResultSetPivotDataSource implements PivotDataSource {

	private static final long serialVersionUID = 1L;
	
	private List<List<Object>> data;
	private List<String> columnNames;
	private List<Class<?>> columnTypes;
	private int rowCount;
	private int columnCount;

	public ResultSetPivotDataSource(ResultSet resultSet) throws SQLException {
		data = new ArrayList<List<Object>>();
		columnNames = new ArrayList<String>();
		columnTypes = new ArrayList<Class<?>>();
		
		populate(resultSet);
	}

	@Override
	public String getFieldName(int fieldIndex) {
		return columnNames.get(fieldIndex);
	}

	@Override
	public int getFieldIndex(String fieldName) {
		for (int i = 0; i < columnNames.size(); i++) {
			if (columnNames.get(i).equals(fieldName)) {
				return i;
			}
		}
		
		return -1;
	}

	@Override
	public Class<?> getFieldType(int fieldIndex) {
		return columnTypes.get(fieldIndex);
	}

	@Override
	public int getFieldCount() {
		return columnCount;
	}

	@Override
	public int getRowCount() {
		return rowCount;
	}

	@Override
	public Object getValueAt(int rowIndex, int fieldIndex) {
		return data.get(rowIndex).get(fieldIndex);
	}

	@Override
	public Object getValueAt(int rowIndex, PivotField field) {
		int fieldIndex = getFieldIndex(field.getName());
		return data.get(rowIndex).get(fieldIndex);
	}

	private void populate(ResultSet resultSet) throws SQLException {
		boolean firstRow = true;
		while (resultSet.next()) {
			if (firstRow) {
				firstRow = false;
				ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
				columnCount = resultSetMetaData.getColumnCount();
				for (int i = 0; i < columnCount; i++) {
					columnNames.add(resultSetMetaData.getColumnLabel(i + 1));
					try {
						columnTypes.add(Class.forName(resultSetMetaData.getColumnClassName(i + 1)));
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
//				System.out.println(columnNames);
//				System.out.println(columnTypes);
			}
	
			List<Object> row = new ArrayList<Object>(columnCount);
			for (int i = 0; i < columnCount; i++) {
				row.add(resultSet.getObject(i + 1));
			}
			data.add(row);
	
			rowCount++;
		}
	}

}
