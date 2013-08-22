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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.MultiKeyMap;

import ro.fortsoft.wicket.pivot.tree.Node;
import ro.fortsoft.wicket.pivot.tree.Tree;
import ro.fortsoft.wicket.pivot.tree.TreeHelper;

/**
 * @author Decebal Suiu
 */
public class DefaultPivotModel implements PivotModel {

	private static final long serialVersionUID = 1L;

	private PivotDataSource dataSource;
	private List<PivotField> fields;
	private Tree columnsHeaderTree;
	private Tree rowsHeaderTree;
	private List<MultiKeyMap> calculatedData; // or use a MultiValueMap from apache commons

	private boolean showGrandTotalForColumn;
	private boolean showGrandTotalForRow;
	private boolean autoCalculate;

	public DefaultPivotModel(PivotDataSource dataSource) {
		this.dataSource = dataSource;

		// init fields
		int count = dataSource.getFieldCount();
		fields = new ArrayList<PivotField>(count);
		for (int i = 0; i < count; i++) {
			PivotField field = new PivotField(dataSource.getFieldName(i), i);
			field.setTitle(field.getName());
			field.setArea(PivotField.Area.UNUSED);
			field.setType(dataSource.getFieldType(i));
			fields.add(field);
		}
	}

	@Override
	public List<PivotField> getFields() {
		return fields;
	}

	@Override
	public PivotField getField(String name) {
		for (PivotField field : fields) {
			if (field.getName().equals(name)) {
				return field;
			}
		}

		return null;
	}

	@Override
	public PivotField getField(int index) {
		for (PivotField field : fields) {
			if (field.getIndex() == index) {
				return field;
			}
		}

		return null;
	}
	
	@Override
	public List<PivotField> getFields(PivotField.Area area) {
		List<PivotField> areaFields = new ArrayList<PivotField>();
		List<PivotField> fields = getFields();
		for (PivotField field : fields) {
			if (field.getArea().equals(area)) {
				areaFields.add(field);
			}
		}
		Collections.sort(areaFields);
		
		return areaFields;
	}

	@Override
	public PivotDataSource getDataSource() {
		return dataSource;
	}

	@Override
	public void calculate() {
		long start = System.currentTimeMillis();
		rowsHeaderTree = null;
		columnsHeaderTree = null;
		getRowsHeaderTree();
		long t1 = System.currentTimeMillis();
		System.out.println("created rowsHeaderTree in " + (t1 - start));
		getColumnsHeaderTree();
		long t2 = System.currentTimeMillis();
		System.out.println("created columnsHeaderTree in " + (t2 - t1));

		t1 = System.currentTimeMillis();
		List<PivotField> dataFields = getFields(PivotField.Area.DATA);
		calculatedData = new ArrayList<MultiKeyMap>();
		for (PivotField field : dataFields) {
			field.getAggregator().init();
			calculatedData.add(getData(field));
		}
		t2 = System.currentTimeMillis();
		System.out.println("filled calculatedData in " + (t2 - t1));
		long stop = System.currentTimeMillis();
		System.out.println("calculated in " + (stop- start));
		System.out.println("calculatedData = " + calculatedData);
		// getValues(field, filter)
	}

	/*
	 * TODO: trebuie imbunatatita metoda asta. Am facut un test pe un tabel
	 * cu 4500 inregistrari si 7 coloane (nextreports downloads). Am observat ca
	 * la 86 chei pe row si 212 chei pe column am 18.232 (86 x 212) combinatii.
	 * Daca in getValues se sta 3,25 ms (cum am obtinut) rezulta un total de 
	 * 5576 ms. Cred ca ar trebuii sa parcurg o singura data inregistrarile din baza.
	 */
	private MultiKeyMap getData(PivotField dataField) {
		MultiKeyMap data = new MultiKeyMap();
		List<List<Object>> rowKeys = getRowKeys();
		System.out.println("rowKeys.size() = " + rowKeys.size());
		List<List<Object>> columnKeys = getColumnKeys();
		System.out.println("columnKeys.size() = " + columnKeys.size());
		
		List<PivotField> rowFields = getFields(PivotField.Area.ROW);
		List<PivotField> columnFields = getFields(PivotField.Area.COLUMN);
		for (List<Object> rowKey : rowKeys) {
			for (List<Object> columnKey : columnKeys) {
				Map<Integer, Object> rowFilter = getFilter(rowFields, rowKey);
				Map<Integer, Object> columnFilter = getFilter(columnFields, columnKey);
				Map<Integer, Object> filter = new HashMap<Integer, Object>(rowFilter);
				filter.putAll(columnFilter);				
				List<Object> values = getValues(dataField, filter);
				if (!CollectionUtils.isEmpty(values)) {
					/*
					System.out.println("filter = " + filter);
					System.out.println("values = " + values);
					System.out.println(values.size());
					*/
					Object summary = PivotUtils.getSummary(dataField, values);
//					System.out.println("summary = " + summary);
					data.put(rowKey, columnKey, summary);
				}
			}
		}
		
		return data;
	}
		
	@Override
	public Tree getColumnsHeaderTree() {
		if (columnsHeaderTree == null) {
			Node root = new Node();
			insertChildren(root, getFields(PivotField.Area.COLUMN));
			columnsHeaderTree = new Tree(root);
		}

		return columnsHeaderTree;
	}

	@Override
	public Tree getRowsHeaderTree() {
		if (rowsHeaderTree == null) {
			Node root = new Node();
			insertChildren(root, getFields(PivotField.Area.ROW));
			rowsHeaderTree = new Tree(root);
		}

		return rowsHeaderTree;
	}

	@Override
	public List<List<Object>> getRowKeys() {
		return TreeHelper.getLeafValues(getRowsHeaderTree().getRoot());
	}

	@Override
	public List<List<Object>> getColumnKeys() {
		return TreeHelper.getLeafValues(getColumnsHeaderTree().getRoot());
	}

	public Object getValueAt(PivotField dataField, List<Object> rowKey, List<Object> columnKey) {
		int index = getFields(PivotField.Area.DATA).indexOf(dataField);
		return calculatedData.get(index).get(rowKey, columnKey);
	}

	@Override
	public boolean isShowGrandTotalForColumn() {
		return showGrandTotalForColumn;
	}

	@Override
	public void setShowGrandTotalForColumn(boolean showGrandTotalForColumn) {
		this.showGrandTotalForColumn = showGrandTotalForColumn;
	}

	@Override
	public boolean isShowGrandTotalForRow() {
		return showGrandTotalForRow;
	}

	@Override
	public void setShowGrandTotalForRow(boolean showGrandTotalForRow) {
		this.showGrandTotalForRow = showGrandTotalForRow;
	}

	@Override
	public boolean isAutoCalculate() {
		return autoCalculate;
	}

	@Override
	public void setAutoCalculate(boolean autoCalculate) {
		this.autoCalculate = autoCalculate;
	}

	@Override
	public String toString() {
		return "DefaultPivotModel [fields=" + fields + "]";
	}

	private void insertChildren(Node node, List<PivotField> fields) {
		// System.out.println("DefaultPivotModel.insertChildren()");
		Set<Object> values = getPossibleChildrenValues(node, fields);
		if (CollectionUtils.isEmpty(values)) {
			return;
		}

		Iterator<Object> it = values.iterator();
		while (it.hasNext()) {
			node.insert(it.next());
		}

		for (Node child : node.getChildren()) {
			insertChildren(child, fields);
		}
	}

	private Set<Object> getPossibleChildrenValues(Node node, List<PivotField> fields) {
		int level = node.getLevel();
		// System.out.println("level = " + level);
		// System.out.println("fields.size = " + fields.size());
		if (fields.size() <= level) {
			return null;
		}

		PivotField nextField = fields.get(level);
		// System.out.println("nextField = " + nextField);
		Map<Integer, Object> filter = getFilter(fields, node.getPathValues());
		// System.out.println("filter = " + filter);
		Set<Object> values = getUniqueValues(nextField, filter);
		// System.out.println("values = " + values);

		return values;
	}

	/*
	 * Retrieves the values for a data field using a filter.
	 */
	private List<Object> getValues(PivotField field, Map<Integer, Object> filter) {
//		long start = System.currentTimeMillis();
		List<Object> values = new ArrayList<Object>();
		int fieldIndex = field.getIndex();
		for (int i = 0; i < dataSource.getRowCount(); i++) {
			if (filter.isEmpty()) {
				values.add(dataSource.getValueAt(i, fieldIndex));
			} else {
				if (acceptValue(i, filter)) {
					values.add(dataSource.getValueAt(i, fieldIndex));
				}
			}
		}
//		long stop = System.currentTimeMillis();
//		System.out.println("getValues in " + (stop - start));
		
		return values;
	}

	/*
	 * Retrieves a filter for filtering data source (raw data). The size of fields must be equals with
	 * the size of values. The key in map is the field index.  
	 */
	private Map<Integer, Object> getFilter(List<PivotField> fields, List<Object> values) {
//		long start = System.currentTimeMillis();
		Map<Integer, Object> filter = new HashMap<Integer, Object>();
		for (int i = 0; i < values.size(); i++) {
			int fieldIndex = fields.get(i).getIndex();
			// System.out.println(fieldIndex);
			filter.put(fieldIndex, values.get(i));
		}
//		long stop = System.currentTimeMillis();
//		System.out.println("getFilter in " + (stop - start));
	
		return filter;
	}
	
	private Set<Object> getUniqueValues(PivotField field, Map<Integer, Object> filter) {
		List<Object> values = getValues(field, filter);
		
		int sortOrder = field.getSortOrder();
		if (sortOrder == PivotField.SORT_ORDER_ASCENDING) {
			return new TreeSet<Object>(values); 
		} else if (sortOrder == PivotField.SORT_ORDER_DESCENDING) {
			return new TreeSet<Object>(values).descendingSet();
		}

		return new LinkedHashSet<Object>(values);
	}

	private boolean acceptValue(int row, Map<Integer, Object> filter) {
		boolean accept = true;
		Set<Integer> keys = filter.keySet();
		Object value = null;
		for (int index : keys) {
			value = dataSource.getValueAt(row, fields.get(index));
			if (!filter.get(index).equals(value)) {
				return false;
			}
		}

		return accept;
	}

}
