package ro.fortsoft.wicket.pivot.web;

import java.util.ArrayList;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import ro.fortsoft.wicket.pivot.PivotModel;
import ro.fortsoft.wicket.pivot.config.IPivotConfigStorage;
import ro.fortsoft.wicket.pivot.config.PivotConfig;

public class PivotConfigStoragePanel extends Panel {
	private static final long serialVersionUID = 1L;
	private IPivotConfigStorage pivotConfigStorage;
	private Model<ArrayList<String>> configNameModel;
	private ListView<String> configListView;

	public PivotConfigStoragePanel(String id, final PivotModel pivotModel, final IPivotConfigStorage pivotConfigStorage) {
		super(id);
		this.pivotConfigStorage = pivotConfigStorage;

		Form<Void> form = new Form<Void>("form");
		final TextField<String> configStoreName = new TextField<String>("configStoreName", new Model<String>(""));
		form.add(configStoreName);

		AjaxButton saveButton = new AjaxButton("save") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				String value = configStoreName.getValue();
				if (value == null || value.isEmpty())
					return;
				PivotConfig config = new PivotConfig();
				config.setName(value);
				config.storeModelState(pivotModel);
				pivotConfigStorage.saveConfig(config);
				ModalWindow.closeCurrent(target);
			}
		};
		form.add(saveButton);
		add(form);

		configNameModel = new Model<ArrayList<String>>();
		configListView = new ListView<String>("configs", configNameModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<String> item) {
				item.add(new Label("name", new Model<String>(item.getModelObject())));
				item.add(new AjaxLink<Void>("load") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						PivotConfig config = pivotConfigStorage.loadConfig(item.getModelObject());
						if (config == null) {
							refreshList(target);
							return;
						}
						config.restoreModelState(pivotModel);
						ModalWindow.closeCurrent(target);
					}
				});
				item.add(new AjaxLink<Void>("delete") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						pivotConfigStorage.deleteConfig(item.getModelObject());
						refreshList(target);
					}
				});
			}
		};
		setOutputMarkupId(true);
		add(configListView);
		refreshList(null);
	}

	private void refreshList(AjaxRequestTarget target) {
		configNameModel.setObject(new ArrayList<String>(pivotConfigStorage.listConfigNames()));
		if (target != null)
			target.add(this);
	}
}
