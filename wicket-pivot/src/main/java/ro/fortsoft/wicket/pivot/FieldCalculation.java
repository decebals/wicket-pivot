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
package ro.fortsoft.wicket.pivot;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class FieldCalculation implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String PERCENT_OF = "percentOf";
	public static final String SUBTRACT = "subtract";
	public static final String ADDITION = "addition";

	public static final List<String> FUNCTIONS = Collections.unmodifiableList(Arrays.asList(PERCENT_OF, SUBTRACT,
			ADDITION));
	private PivotField fieldA;
	private PivotField fieldB;

	public static List<String> getFunctions() {
		return FUNCTIONS;
	}

	public static FieldCalculation get(String function) {
		if (function.equalsIgnoreCase(PERCENT_OF)) {
			return new PercentOf();
		} else if (function.equalsIgnoreCase(SUBTRACT)) {
			return new Substract();
		} else if (function.equalsIgnoreCase(ADDITION)) {
			return new Addition();
		}
		return null;
	}

	/**
	 * Provide
	 */
	public static interface FieldValueProvider {
		/**
		 * @param field
		 *            can be null
		 * 
		 * @return the current value of the pivot field
		 */
		public abstract Object getFieldValue(PivotField field);
	}

	public abstract FieldCalculation init();

	public abstract String getFunction();

	public abstract String getDescription();

	public abstract Object calculate(FieldValueProvider fieldValueProvider);

	protected String getFieldDescription(PivotField field) {
		if (field == null)
			return "";
		return field.getTitle() + " (" + field.getCalculationDescription() + ")";
	}

	public static class PercentOf extends FieldCalculation {
		private static final long serialVersionUID = 1L;

		@Override
		public FieldCalculation init() {
			return this;
		}

		@Override
		public String getFunction() {
			return PERCENT_OF;
		}

		@Override
		public Object calculate(FieldValueProvider fieldValueProvider) {
			Object valueA = fieldValueProvider.getFieldValue(getFieldA());
			Object valueB = fieldValueProvider.getFieldValue(getFieldB());
			if (valueA instanceof Number && valueB instanceof Number) {
				Number numberA = (Number) valueA;
				Number numberB = (Number) valueB;
				if (Math.abs(numberB.doubleValue()) > 0.000001)
					return 100.0 / numberB.doubleValue() * numberA.doubleValue();
			}
			return 0;
		}

		@Override
		public String getDescription() {
			return "% " + getFieldDescription(getFieldA()) + " of " + getFieldDescription(getFieldB());
		}
	}

	public static class Substract extends FieldCalculation {
		private static final long serialVersionUID = 1L;

		@Override
		public FieldCalculation init() {
			return this;
		}

		@Override
		public String getFunction() {
			return SUBTRACT;
		}

		@Override
		public Object calculate(FieldValueProvider fieldValueProvider) {
			Object valueA = fieldValueProvider.getFieldValue(getFieldA());
			Object valueB = fieldValueProvider.getFieldValue(getFieldB());
			if (valueA instanceof Number && valueB instanceof Number) {
				Number numberA = (Number) valueA;
				Number numberB = (Number) valueB;
				return numberA.doubleValue() - numberB.doubleValue();
			}
			return 0;
		}

		@Override
		public String getDescription() {
			return getFieldDescription(getFieldA()) + " - " + getFieldDescription(getFieldB());
		}
	}

	public static class Addition extends FieldCalculation {
		private static final long serialVersionUID = 1L;

		@Override
		public FieldCalculation init() {
			return this;
		}

		@Override
		public String getFunction() {
			return ADDITION;
		}

		@Override
		public Object calculate(FieldValueProvider fieldValueProvider) {
			Object valueA = fieldValueProvider.getFieldValue(getFieldA());
			Object valueB = fieldValueProvider.getFieldValue(getFieldB());
			if (valueA instanceof Number && valueB instanceof Number) {
				Number numberA = (Number) valueA;
				Number numberB = (Number) valueB;
				return numberA.doubleValue() + numberB.doubleValue();
			}
			return 0;
		}

		@Override
		public String getDescription() {
			return getFieldDescription(getFieldA()) + " + " + getFieldDescription(getFieldB());
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FieldCalculation) {
			String tmp = ((FieldCalculation) obj).getFunction();
			return getFunction().equals(tmp);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return getFunction().hashCode();
	}

	public PivotField getFieldA() {
		return fieldA;
	}

	public void setFieldA(PivotField fieldA) {
		this.fieldA = fieldA;
	}

	public PivotField getFieldB() {
		return fieldB;
	}

	public void setFieldB(PivotField fieldB) {
		this.fieldB = fieldB;
	}
}
