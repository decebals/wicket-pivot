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
package ro.fortsoft.wicket.pivot.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.template.PackageTextTemplate;

import ro.fortsoft.wicket.pivot.PivotField;
import ro.fortsoft.wicket.pivot.PivotModel;

/**
 * @author Decebal Suiu
 */
public class PivotAreaPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private SortableAjaxBehavior sortableAjaxBehavior;
	private ListView<PivotField> fieldsView;
	private PivotField.Area area;
	private ModalWindow modal;
	
	public PivotAreaPanel(String id, PivotField.Area area) {
		super(id);

		this.area = area;
		
		modal = new ModalWindow("modal");
		modal.setAutoSize(true);
		add(modal);

		add(new Label("name", new ResourceModel(area.getName())));
		
		WebMarkupContainer fieldsContainer = new WebMarkupContainer("fieldsContainer");
		fieldsContainer.setOutputMarkupId(true);
		fieldsContainer.setMarkupId("area-" + area.getName() + "-" + getSession().nextSequenceValue());
		add(fieldsContainer);
		
		fieldsView = new ListView<PivotField>("fields") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<PivotField> item) {
				final IModel<PivotField> itemModel = item.getModel();
				final PivotField pivotField = itemModel.getObject();
				final PivotField.Area area = PivotAreaPanel.this.area;
				Label fieldLabel = new Label("field", new AbstractReadOnlyModel<String>() {

					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						String title = pivotField.getTitle();
						if (area.equals(PivotField.Area.DATA)) {
							title += " (" + pivotField.getCalculationDescription() + ")"; 
						}
						
						return title;
					}
					
				});
				if (pivotField.isNumber()) {
					item.add(AttributeModifier.append("class", "field-number"));
				}
												
				// add field actions panel
				if (!area.equals(PivotField.Area.UNUSED)) {
					PivotFieldActionsPanel pivotFieldActionsPanel = new PivotFieldActionsPanel("dropDownPanel", Model.of(pivotField));
					pivotFieldActionsPanel.setRenderBodyOnly(true);
					item.add(pivotFieldActionsPanel);
					String markupId = "dropdown-" + pivotField.getIndex();
					pivotFieldActionsPanel.get("dropdown").setMarkupId(markupId);
					fieldLabel.add(AttributeModifier.append("data-dropdown", "#" + markupId));
				} else {
					item.add(new EmptyPanel("dropDownPanel").setVisible(false));
				}
				
				item.add(fieldLabel);				
				item.setOutputMarkupId(true);
				item.setMarkupId("field-" + pivotField.getIndex());
			}
		};
		fieldsView.setOutputMarkupPlaceholderTag(true);
		fieldsContainer.add(fieldsView);
		
		setOutputMarkupId(true);
	}

	@Override
	protected void onBeforeRender() {
		IModel<List<PivotField>> model = new LoadableDetachableModel<List<PivotField>>() {

			private static final long serialVersionUID = 1L;

			@Override
			protected List<PivotField> load() {
				return getPivotModel().getFields(area);
			}
			
		};

		fieldsView.setModel(model);
		
		super.onBeforeRender();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		addSortableBehavior(get("fieldsContainer"));
	}
	
	public PivotField.Area getArea() {
		return area;
	}
	
	public ModalWindow getModal() {
		return modal;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        CharSequence script = sortableAjaxBehavior.getCallbackFunctionBody();

        Map<String, String> vars = new HashMap<String, String>();
        vars.put("component", get("fieldsContainer").getMarkupId());
        vars.put("stopBehavior", script.toString());

        PackageTextTemplate template = new PackageTextTemplate(PivotAreaPanel.class, "res/sort-behavior.template.js");
        template.interpolate(vars);

        response.render(OnDomReadyHeaderItem.forScript(template.getString()));
        try {
        	template.close();
        } catch(IOException e) {
        	throw new RuntimeException(e);
        }
    }
	
	public ListView<PivotField> getFieldsView() {
		return fieldsView;
	}

	private void addSortableBehavior(Component component) {
		sortableAjaxBehavior = new SortableAjaxBehavior() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSort(AjaxRequestTarget target, Item[] items) {
				PivotModel pivotModel = getPivotModel();
				for (Item item : items) {
					PivotField pivotField = pivotModel.getField(item.fieldIndex);
					pivotField.setArea(PivotField.Area.getValue(item.areaName));
					pivotField.setAreaIndex(item.sortIndex);
				}
				send(getPage(), Broadcast.BREADTH, new AreaChangedEvent(target));
			}
			
		};
		component.add(sortableAjaxBehavior);
	}

	private PivotModel getPivotModel() {
		return findParent(PivotPanel.class).getPivotModel();
	}
		
}
