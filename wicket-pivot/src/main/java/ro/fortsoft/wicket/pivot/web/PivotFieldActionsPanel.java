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

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.resource.JQueryPluginResourceReference;

import ro.fortsoft.wicket.pivot.PivotField;
import ro.fortsoft.wicket.pivot.PivotFieldAction;

/**
 * @author Decebal Suiu
 */
public class PivotFieldActionsPanel extends GenericPanel<PivotField> {

	private static final long serialVersionUID = 1L;

	private IModel<List<PivotFieldAction>> actionsModel;
	
	public PivotFieldActionsPanel(String id, IModel<PivotField> model) {
		super(id, model);
		
		WebMarkupContainer dropDownContainer = new WebMarkupContainer("dropdown");
		dropDownContainer.setOutputMarkupId(true);
		add(dropDownContainer);
		
		actionsModel = new LoadableDetachableModel<List<PivotFieldAction>>() {

			private static final long serialVersionUID = 1L;

			@Override
			protected List<PivotFieldAction> load() {
				PivotPanel pivotPanel = findParent(PivotPanel.class);
				return pivotPanel.getPivotFieldActionsFactory().createPivotFieldActions(getPivotField());
			}
			
		};
		ListView<PivotFieldAction> actionsView = new ListView<PivotFieldAction>("action", actionsModel) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<PivotFieldAction> item) {
				PivotFieldAction action = item.getModelObject();
				AbstractLink link = action.getLink("link");
//				link.add(new Image("image", action.getImage()));
//				link.add(AttributeModifier.replace("title", action.getTooltip()));
				link.add(new Label("name", action.getName()));
				item.add(link);
			}

		};
		dropDownContainer.add(actionsView);		
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setVisible(!actionsModel.getObject().isEmpty());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(JavaScriptHeaderItem.forReference(new JQueryPluginResourceReference(PivotAreaPanel.class, "res/jquery.dropdown.js")));
        response.render(CssHeaderItem.forReference(new PackageResourceReference(PivotAreaPanel.class, "res/jquery.dropdown.css")));
	}
	
	private PivotField getPivotField() {
		return getModelObject();
	}
	
}
