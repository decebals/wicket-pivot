package ro.fortsoft.wicket.pivot.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ro.fortsoft.wicket.pivot.Aggregator;
import ro.fortsoft.wicket.pivot.FieldCalculation;
import ro.fortsoft.wicket.pivot.PivotField;
import ro.fortsoft.wicket.pivot.PivotField.Area;
import ro.fortsoft.wicket.pivot.PivotModel;

/**
 * A stored pivot table configuration. The structure of this class and its child
 * PivotConfigField is guaranteed to be stable across releases. Serialize this
 * class using the Java serializers or whatever you like (e.g. GSON)
 * 
 */
public class PivotConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Name of the configuration, to show in the UI.
	 */
	private String name;
	private boolean showGrandTotalForColumn;
	private boolean showGrandTotalForRow;
	private boolean autoCalculate;

	/**
	 * The fields are stored as an array - so that this can be correctly be
	 * stored/restored with GSON
	 */
	private PivotConfigField[] pivotConfigFields = new PivotConfigField[0];

	/**
	 * Represents the configuration state of a PivotField
	 */
	public static class PivotConfigField implements Serializable {
		private static final long serialVersionUID = 1L;
		private String name;
		private String title;
		private Area area;
		private int areaIndex;
		private String aggreatorFunction;
		private String fieldCalculationFunction;
		private String[] fieldCalculationFields;
		private int sortOrder;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Area getArea() {
			return area;
		}

		public void setArea(Area area) {
			this.area = area;
		}

		public int getAreaIndex() {
			return areaIndex;
		}

		public void setAreaIndex(int areaIndex) {
			this.areaIndex = areaIndex;
		}

		public String getAggreatorFunction() {
			return aggreatorFunction;
		}

		public void setAggreatorFunction(String aggreatorFunction) {
			this.aggreatorFunction = aggreatorFunction;
		}

		public String getFieldCalculationFunction() {
			return fieldCalculationFunction;
		}

		public void setFieldCalculationFunction(String fieldCalculationFunction) {
			this.fieldCalculationFunction = fieldCalculationFunction;
		}

		public String[] getFieldCalculationFields() {
			return fieldCalculationFields;
		}

		public void setFieldCalculationFields(String[] fieldCalculationFields) {
			this.fieldCalculationFields = fieldCalculationFields;
		}

		public int getSortOrder() {
			return sortOrder;
		}

		public void setSortOrder(int sortOrder) {
			this.sortOrder = sortOrder;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		/**
		 * Store the state of the of the pivot field in this instance
		 * 
		 * @param field
		 */
		public void storeFieldState(PivotField field) {
			/* Plain Fields */
			this.name = field.getName();
			this.title = field.getTitle();
			this.area = field.getArea();
			this.areaIndex = field.getAreaIndex();
			this.aggreatorFunction = null;
			this.fieldCalculationFunction = null;
			this.fieldCalculationFields = null;
			this.sortOrder = field.getSortOrder();

			/* Functions */
			if (field.getAggregator() != null)
				this.aggreatorFunction = field.getAggregator().getFunction();
			if (field.getFieldCalculation() != null) {
				this.fieldCalculationFunction = field.getFieldCalculation().getFunction();
				this.fieldCalculationFields = new String[] { getFieldName(field.getFieldCalculation().getFieldA()),
						getFieldName(field.getFieldCalculation().getFieldB()) };
			}
		}

		private static String getFieldName(PivotField pivotField) {
			if (pivotField == null)
				return null;
			return pivotField.getName();
		}

		/**
		 * Restore the state of the pivot field from this instance.
		 * 
		 * The name *MUST* be equal!
		 */
		public void restoreFieldState(PivotField field, PivotModel pivotModel) {
			if (!field.getName().equals(name))
				throw new IllegalArgumentException(field.getName() + " != " + name);
			/* Plain Properties */
			field.setTitle(this.title);
			field.setArea(this.getArea());
			field.setAreaIndex(this.getAreaIndex());
			field.setAggregator(null);
			field.setFieldCalculation(null);
			field.setSortOrder(this.getSortOrder());

			/* Functions */
			if (this.aggreatorFunction != null)
				field.setAggregator(Aggregator.get(this.aggreatorFunction));
			if (this.fieldCalculationFunction != null) {
				FieldCalculation fieldCalculation = FieldCalculation.get(this.fieldCalculationFunction);
				field.setFieldCalculation(fieldCalculation);
				fieldCalculation.setFieldA(findField(this.fieldCalculationFields, 0, pivotModel));
				fieldCalculation.setFieldB(findField(this.fieldCalculationFields, 1, pivotModel));
			}
		}

		private static PivotField findField(String[] fieldCalculationFields, int index, PivotModel pivotModel) {
			if (fieldCalculationFields == null)
				return null;
			if (index >= fieldCalculationFields.length)
				return null;
			String fieldName = fieldCalculationFields[index];
			if (fieldName == null)
				return null;
			return pivotModel.getField(fieldName);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @internal Only for the serializers
	 */
	public PivotConfigField[] getPivotConfigFields() {
		return pivotConfigFields;
	}

	/**
	 * @internal Only for the serializers
	 */
	public void setPivotConfigFields(PivotConfigField[] pivotConfigFields) {
		this.pivotConfigFields = pivotConfigFields;
	}

	public boolean isShowGrandTotalForColumn() {
		return showGrandTotalForColumn;
	}

	public void setShowGrandTotalForColumn(boolean showGrandTotalForColumn) {
		this.showGrandTotalForColumn = showGrandTotalForColumn;
	}

	public boolean isShowGrandTotalForRow() {
		return showGrandTotalForRow;
	}

	public void setShowGrandTotalForRow(boolean showGrandTotalForRow) {
		this.showGrandTotalForRow = showGrandTotalForRow;
	}

	public boolean isAutoCalculate() {
		return autoCalculate;
	}

	public void setAutoCalculate(boolean autoCalculate) {
		this.autoCalculate = autoCalculate;
	}

	/**
	 * Store the model state in this configuration instance
	 * 
	 * @param model
	 */
	public void storeModelState(PivotModel model) {
		this.showGrandTotalForColumn = model.isShowGrandTotalForColumn();
		this.showGrandTotalForRow = model.isShowGrandTotalForRow();
		this.autoCalculate = model.isAutoCalculate();

		List<PivotConfigField> fields = new ArrayList<PivotConfigField>();
		for (PivotField field : model.getFields()) {
			/* We don't store unused fields */
			if (field.getArea() == Area.UNUSED)
				continue;

			PivotConfigField configField = new PivotConfigField();
			configField.storeFieldState(field);
			fields.add(configField);
		}
		pivotConfigFields = fields.toArray(new PivotConfigField[fields.size()]);
	}

	public void restoreModelState(PivotModel model) {
		/*
		 * Global model state
		 */
		model.setShowGrandTotalForColumn(this.showGrandTotalForColumn);
		model.setShowGrandTotalForRow(this.showGrandTotalForRow);
		model.setAutoCalculate(this.autoCalculate);

		/*
		 * We set all fields to unused, as we don't save unused fields, so thats
		 * the default value
		 */
		List<PivotField> fieldsToDelete = new ArrayList<PivotField>();
		for (PivotField field : model.getFields()) {
			/*
			 * We must delete all field calculations, as this are additional
			 * fields, which the user can add. Otherwise they will grow when we
			 * load the same configuration with field calculations again and
			 * again
			 */
			if (field.getFieldCalculation() != null)
				fieldsToDelete.add(field);
			field.setArea(Area.UNUSED);
		}

		/*
		 * Now delete all field calculation fields
		 */
		for (PivotField field : fieldsToDelete)
			model.getFields().remove(field);

		/*
		 * And restore the fields
		 */
		for (PivotConfigField configField : this.pivotConfigFields) {
			PivotField field = null;
			if (configField.getFieldCalculationFunction() != null) {
				/*
				 * Special field for field calculation
				 */
				field = new PivotField("", model.getFields().size());
				field.setType(Double.class);
				model.getFields().add(field);
			} else
				field = model.getField(configField.getName());

			/*
			 * Has the field been delete from the model?
			 */
			if (field == null)
				/*
				 * Yes, ignore it
				 */
				continue;
			configField.restoreFieldState(field, model);
		}
	}
}
