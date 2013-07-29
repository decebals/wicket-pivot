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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.template.PackageTextTemplate;

import ro.fortsoft.wicket.pivot.Aggregator;
import ro.fortsoft.wicket.pivot.PivotField;
import ro.fortsoft.wicket.pivot.PivotModel;

/**
 * @author Decebal Suiu
 */
public class PivotAreaPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private SortableAjaxBehavior sortableAjaxBehavior;
	private ListView<PivotField> values;
	private PivotField.Area area;
	
	public PivotAreaPanel(String id, PivotField.Area area) {
		super(id);

		this.area = area;
		
		add(new Label("name", area.getName().toUpperCase()));

		final ModalWindow modal = new ModalWindow("modal");
		modal.setTitle("Aggregator");
		modal.setAutoSize(true);
		add(modal);
		
		WebMarkupContainer fieldsContainer = new WebMarkupContainer("fieldsContainer");
		fieldsContainer.setOutputMarkupId(true);
		fieldsContainer.setMarkupId("area-" + area.getName() + "-" + getSession().nextSequenceValue());
		add(fieldsContainer);
		
		values = new ListView<PivotField>("values") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<PivotField> item) {
				final IModel<PivotField> itemModel = item.getModel();
				PivotField pivotField = itemModel.getObject();
				String title = pivotField.getTitle();
				if (pivotField.getArea().equals(PivotField.Area.DATA)) {
					title += " (" + pivotField.getAggregator().getFunction().toUpperCase() + ")"; 
				}
				Label valueLabel = new Label("value", title);
				if (pivotField.isNumber()) {
					valueLabel.add(AttributeModifier.append("class", "label-info"));
//				} else {
//					valueLabel.add(AttributeModifier.append("class", "label-important"));
				}
				if (item.getModelObject().getArea().equals(PivotField.Area.DATA)) {
					valueLabel.add(new AjaxEventBehavior("onclick") {
	
						private static final long serialVersionUID = 1L;
	
						protected void onEvent(AjaxRequestTarget target) {
							final AggregatorPanel panel = new AggregatorPanel(modal.getContentId(), itemModel);
							panel.add(AttributeModifier.append("style", "padding: 10px;"));
							modal.setContent(panel);
							modal.setAutoSize(true);
							modal.setResizable(false);
							/*
							modal.setWindowClosedCallback(new WindowClosedCallback() {
								
								private static final long serialVersionUID = 1L;

								public void onClose(AjaxRequestTarget target) {
									if (panel.isOkPressed()) {
										System.out.println(">>> " + itemModel.getObject().getAggregator());
									}
								}
								
							});
							*/
							modal.show(target);
						}
						
					});
					valueLabel.add(AttributeModifier.append("style", "cursor: pointer;"));
				}
				item.add(valueLabel);				
				item.setOutputMarkupId(true);
				item.setMarkupId("field-" + pivotField.getIndex());
			}
		};
		values.setOutputMarkupPlaceholderTag(true);
		fieldsContainer.add(values);
		
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

		values.setModel(model);
		
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

	/*
	private void addDragBehavior() {
		DragSource dragSource = new DragSource(Operation.MOVE) {
			
			private static final long serialVersionUID = 1L;
            
			@Override 
			public void onAfterDrop(AjaxRequestTarget target, Transfer transfer) {
//				System.out.println("<<< drag");
//				System.out.println("transferedField = " + transfer.getData());
			}
			
		}.drag("div.value-container").initiate("span.initiate");
//		}.drag("div.values").initiate("i");
		add(dragSource);
	}
	
	private void addDropBehavior() {
		DropTarget dropTarget = new DropTarget(Operation.MOVE) {
			
			private static final long serialVersionUID = 1L;

			public void onDrop(AjaxRequestTarget target, Transfer transfer, Location location) {
				PivotField transferedField = transfer.getData();
//				System.out.println("transferedField = " + transferedField);				
				if (getArea().equals(PivotField.Area.DATA) && !transferedField.isNumber()) {
					transfer.reject();
					return;
				}
				
				PivotField locationField = location.getModelObject();
//				System.out.println("locationField = " + locationField);
//				System.out.println("location.anchor = " + location.getAnchor());
				
				// update field area indexes
				List<PivotField> fields = getPivotModel().getFields(getArea());
				int index = fields.indexOf(locationField);
//				System.out.println("index = " + index);
//				System.out.println(fields.size());
				if (location.getAnchor().equals(Anchor.LEFT)) {
//					System.out.println("L");
					if (getArea().equals(transferedField.getArea())) {
						// swap
						int oldIndex = transferedField.getAreaIndex();
//						System.out.println("oldIndex = " + oldIndex);
						transferedField.setAreaIndex(index);
						locationField.setAreaIndex(oldIndex);
					} else {
						// insert before
						transferedField.setAreaIndex(index);
						for (int i = index; i < fields.size(); i++) {
							fields.get(i).setAreaIndex(i + 1);
						}
						transferedField.setArea(getArea());
					}
					send(getPage(), Broadcast.BREADTH, new AreaChangedEvent(target));
				} else if (location.getAnchor().equals(Anchor.RIGHT)) {
//					System.out.println("R");
					if (getArea().equals(transferedField.getArea())) {
						// swap
						int oldIndex = transferedField.getAreaIndex();
//						System.out.println("oldIndex = " + oldIndex);
						transferedField.setAreaIndex(index);
						locationField.setAreaIndex(oldIndex);
					} else {
						// insert after
						transferedField.setAreaIndex(index + 1);
						for (int i = index + 1; i < fields.size(); i++) {
							fields.get(i).setAreaIndex(i);
						}
						transferedField.setArea(getArea());
					}
					send(getPage(), Broadcast.BREADTH, new AreaChangedEvent(target));
				}
//				System.out.println(pivotPage.getPivotModel());
			}

			@Override
			public void onRejected(AjaxRequestTarget target) {
				System.out.println("rejected");
			}
			
		}.dropLeftAndRight("div.value-container");
//		}.dropLeftAndRight("div.values");
		add(dropTarget);
	}
	*/
	
	/*
	private void addSortableBehavior() {
		SortableAjaxBehavior sortableAjaxBehavior = new SortableAjaxBehavior() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onStop(Item[] items, AjaxRequestTarget target) {
				PivotModel pivotModel = getPivotModel();
				for (Item item : items) {
					PivotField pivotField = pivotModel.getField(item.fieldIndex);
					pivotField.setArea(PivotField.Area.getValue(item.areaName));
					pivotField.setAreaIndex(item.sortIndex);
				}
				send(getPage(), Broadcast.BREADTH, new AreaChangedEvent(target));
			}
			
		};
		SortableBehavior sortableBehavior = sortableAjaxBehavior.getSortableBehavior();
		sortableBehavior.setConnectWith(".fields-container");
		sortableBehavior.setHandle(".initiate");
		sortableBehavior.setCursor("move");
		sortableBehavior.setForcePlaceholderSize(true);
		sortableBehavior.setPlaceholder("pivot-placeholder");
		sortableBehavior.setOpacity(0.4f);
		
		get("fieldsContainer").add(sortableAjaxBehavior);
	}
	*/
	
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
	
	private class AggregatorPanel extends GenericPanel<PivotField> {

		private static final long serialVersionUID = 1L;

		private Aggregator aggregator;
//		private boolean okPressed;
		
		public AggregatorPanel(String id, final IModel<PivotField> model) {
			super(id, model);
			
//			okPressed = false;
			
			aggregator = model.getObject().getAggregator();
			
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

				public void onClick(AjaxRequestTarget target) {
//					okPressed = true;
					getPivotModel().getField(model.getObject().getName()).setAggregator(aggregator);
					target.add(PivotAreaPanel.this);
					ModalWindow.closeCurrent(target);
				}
				
			});
		}
		
		/*
		public boolean isOkPressed() {
			return okPressed;
		}
		*/

	}
	
}
