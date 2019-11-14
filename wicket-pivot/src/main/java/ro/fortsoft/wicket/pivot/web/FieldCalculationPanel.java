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
package ro.fortsoft.wicket.pivot.web;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import ro.fortsoft.wicket.pivot.FieldCalculation;
import ro.fortsoft.wicket.pivot.PivotField;
import ro.fortsoft.wicket.pivot.PivotModel;

import java.util.ArrayList;
import java.util.List;

public class FieldCalculationPanel extends GenericPanel<PivotField> {
	private final static class OnChangeAjaxBehaviorExtension extends OnChangeAjaxBehavior {
		private static final long serialVersionUID = 1L;

		@Override
		protected void onUpdate(AjaxRequestTarget target) {
			// We just want the field content to be pushed to the server
		}
	}

	private static final class PivotFieldChoiceRenderer extends ChoiceRenderer<PivotField> {
		private static final long serialVersionUID = 1L;

		@Override
		public Object getDisplayValue(PivotField object) {
			return object.getTitle() + " (" + object.getCalculationDescription() + ")";
		}
	}

	private static final long serialVersionUID = 1L;

	private FieldCalculation fieldCalculation;
	private PivotField fieldA;
	private PivotField fieldB;
	private String title = "Calculation";
	private boolean okPressed;

	public FieldCalculationPanel(String id, IModel<PivotField> model, IModel<PivotModel> pivotModel) {
		super(id, model);

		okPressed = false;

		setFieldCalculation(getModelObject().getFieldCalculation());
		if (fieldCalculation != null) {
			fieldA = fieldCalculation.getFieldA();
			fieldB = fieldCalculation.getFieldB();
		}
		List<FieldCalculation> fieldCalculations = new ArrayList<>();
		for (String name : FieldCalculation.FUNCTIONS)
			fieldCalculations.add(FieldCalculation.get(name));

		final DropDownChoice<FieldCalculation> fieldCalculationDropDown = new DropDownChoice<>(
				"fieldCalcluation", new PropertyModel<>(this, "fieldCalculation"), fieldCalculations,
				new ChoiceRenderer<FieldCalculation>("function") {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(FieldCalculation object) {
						return object == null ? "" : object.getDescription();
					}
				});
		fieldCalculationDropDown.setOutputMarkupId(true);
		fieldCalculationDropDown.add(new OnChangeAjaxBehaviorExtension());
		add(fieldCalculationDropDown);

		List<PivotField> fields = new ArrayList<>();
		for (PivotField field : pivotModel.getObject().getFields()) {
			if (field.getAggregator() != null && field.getFieldCalculation() == null && field.isNumber())
				fields.add(field);
		}
		final DropDownChoice<PivotField> fieldADropDown = new DropDownChoice<>("fieldA",
				new PropertyModel<>(this, "fieldA"), fields, new PivotFieldChoiceRenderer());
		fieldADropDown.setOutputMarkupId(true);
		fieldADropDown.add(new OnChangeAjaxBehaviorExtension());
		add(fieldADropDown);

		final DropDownChoice<PivotField> fieldBDropDown = new DropDownChoice<>("fieldB",
				new PropertyModel<>(this, "fieldB"), fields, new PivotFieldChoiceRenderer());
		fieldBDropDown.setOutputMarkupId(true);
		fieldBDropDown.add(new OnChangeAjaxBehaviorExtension());
		add(fieldBDropDown);

		add(new TextField<>("title", new PropertyModel<>(this, "title"))
				.add(new OnChangeAjaxBehaviorExtension()));

		add(new AjaxLink<Void>("ok") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (fieldCalculation != null) {
					okPressed = true;
					fieldCalculation.setFieldA(fieldA);
					fieldCalculation.setFieldB(fieldB);
				}
				ModalWindow.closeCurrent(target);
			}
		});
	}

	public boolean isOkPressed() {
		return okPressed;
	}

	public FieldCalculation getFieldCalculation() {
		return fieldCalculation;
	}

	public void setFieldCalculation(FieldCalculation fieldCalculation) {
		this.fieldCalculation = fieldCalculation;
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
