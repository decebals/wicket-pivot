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
package ro.fortsoft.wicket.pivot;

import java.io.Serializable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.WindowClosedCallback;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;

import ro.fortsoft.wicket.pivot.web.AggregatorPanel;
import ro.fortsoft.wicket.pivot.web.PivotAreaPanel;
import ro.fortsoft.wicket.pivot.web.PivotPanel;

/**
 * @author Decebal Suiu
 */
public abstract class PivotFieldAction implements Serializable {

	private static final long serialVersionUID = 1L;

	protected PivotField field;	
	protected String name;
//	protected IResource image;
//	protected String tooltip;

	public PivotFieldAction(PivotField field) {
		this.field = field;
	}
	
	public abstract AbstractLink getLink(String id);
 
	public String getName() {
		return name;
	}

	/*
	public IResource getImage() {
		return image;
	}

	public String getTooltip() {
		return tooltip;
	}
	*/

	public static class Delete extends PivotFieldAction {

		private static final long serialVersionUID = 1L;

		public Delete(PivotField field) {
			super(field);
			
			name = "Delete";
//			image = new ContextRelativeResource("images/delete.gif");
//			tooltip = "Delete";
		}

		@Override
		public AbstractLink getLink(String id) {
			return new AjaxLink<Void>(id) {

				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {			
					// TODO: implement
				}
				
			};			
		}
		
	}

	public static class AggregatorAction extends PivotFieldAction {

		private static final long serialVersionUID = 1L;

		public AggregatorAction(PivotField field) {
			super(field);
			
			name = "Aggregator...";
//			image = new ContextRelativeResource("images/agregator.gif");
//			tooltip = "Delete";
		}

		@Override
		public AbstractLink getLink(String id) {
			return new AjaxLink<Void>(id) {

				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {		
					ModalWindow modal = findParent(PivotAreaPanel.class).getModal();
					modal.setTitle("Aggregator");
					final AggregatorPanel panel = new AggregatorPanel(modal.getContentId(), Model.of(field));
					panel.add(AttributeModifier.append("style", "padding: 10px;"));
					modal.setContent(panel);
					modal.setAutoSize(true);
					modal.setResizable(false);
					modal.show(target);
					modal.setWindowClosedCallback(new WindowClosedCallback() {
						
						private static final long serialVersionUID = 1L;

						@Override
						public void onClose(AjaxRequestTarget target) {
							if (!panel.isOkPressed()) {
								return;
							}
							
							// TODO: performance
//							target.add(findParent(PivotAreaPanel.class));
							int index = field.getAreaIndex();
							target.add(((ListView<PivotField>) findParent(PivotAreaPanel.class).getFieldsView()).get(index));
							
							PivotModel pivotModel = getPivotModel();
							pivotModel.getField(field.getName()).setAggregator(panel.getAggregator());
							if (pivotModel.isAutoCalculate()) {
								findParent(PivotPanel.class).compute(target);
							}
							
						}
					});
				}
				
				private PivotModel getPivotModel() {
					return findParent(PivotPanel.class).getPivotModel();
				}
				
			};			
		}
		
	}

}
