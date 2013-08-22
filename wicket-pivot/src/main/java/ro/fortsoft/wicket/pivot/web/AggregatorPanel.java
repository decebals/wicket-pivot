/*
 * Copyright 2013 Decebal Suiu
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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import ro.fortsoft.wicket.pivot.Aggregator;
import ro.fortsoft.wicket.pivot.PivotField;

/**
 * @author Decebal Suiu
 */
public class AggregatorPanel extends GenericPanel<PivotField> {

	private static final long serialVersionUID = 1L;

	private Aggregator aggregator;
	private boolean okPressed;
	
	public AggregatorPanel(String id, IModel<PivotField> model) {
		super(id, model);
		
		okPressed = false;
		
		aggregator = getModelObject().getAggregator();
		
		List<Aggregator> aggregators = new ArrayList<Aggregator>();
		aggregators.add(Aggregator.get(Aggregator.SUM));
		aggregators.add(Aggregator.get(Aggregator.AVG));
		aggregators.add(Aggregator.get(Aggregator.MIN));
		aggregators.add(Aggregator.get(Aggregator.MAX));
		aggregators.add(Aggregator.get(Aggregator.COUNT));
		final DropDownChoice<Aggregator> aggregatorDownChoice = new DropDownChoice<Aggregator>("aggregator", 
				new PropertyModel<Aggregator>(this, "aggregator"), aggregators,
				new ChoiceRenderer<Aggregator>("function") {

					private static final long serialVersionUID = 1L;
			 		
					@Override
					public Object getDisplayValue(Aggregator object) {
						return ((String) super.getDisplayValue(object)).toUpperCase();
					}
			
		});
		aggregatorDownChoice.add(new OnChangeAjaxBehavior() {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		aggregatorDownChoice.setOutputMarkupId(true);
		add(aggregatorDownChoice);
		
		add(new AjaxLink<Void>("ok") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				okPressed = true;	
				ModalWindow.closeCurrent(target);

			}
			
		});
	}
	
	public boolean isOkPressed() {
		return okPressed;
	}

	public Aggregator getAggregator() {
		return aggregator;
	}
	
}
