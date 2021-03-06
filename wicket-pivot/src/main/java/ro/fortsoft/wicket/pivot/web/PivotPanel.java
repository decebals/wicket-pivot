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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.WindowClosedCallback;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceRequestHandler;
import org.apache.wicket.request.resource.ByteArrayResource;

import ro.fortsoft.wicket.pivot.DefaultPivotFieldActionsFactory;
import ro.fortsoft.wicket.pivot.DefaultPivotModel;
import ro.fortsoft.wicket.pivot.PivotDataSource;
import ro.fortsoft.wicket.pivot.PivotField;
import ro.fortsoft.wicket.pivot.PivotFieldActionsFactory;
import ro.fortsoft.wicket.pivot.PivotModel;
import ro.fortsoft.wicket.pivot.config.IPivotConfigStorage;
import ro.fortsoft.wicket.pivot.exporter.PivotCsvExporter;
import ro.fortsoft.wicket.pivot.exporter.PivotExporter;

/**
 * @author Decebal Suiu
 */
public class PivotPanel extends GenericPanel<PivotDataSource> {

	private final class ButtonCssClassModel extends AbstractReadOnlyModel<String> {
		private static final long serialVersionUID = 1L;

		@Override
		public String getObject() {
			return verify() ? "btn-success" : "btn-success disabled";
		}
	}

	private static final long serialVersionUID = 1L;

	private WebMarkupContainer areasContainer;
	private PivotModel pivotModel;
	private PivotTable pivotTable;
	private AjaxLink<Void> computeLink;
	private WebMarkupContainer downloadContainer;
	private PivotExporter[] pivotExporters = new PivotExporter[] { new PivotCsvExporter() };
	// TODO: requires Serializable?!
	private PivotFieldActionsFactory pivotFieldActionsFactory;
	private String pivotExportFilename = "pivottable";
	private IPivotConfigStorage pivotConfigStorage;

	private ModalWindow modal;

	public PivotPanel(String id, PivotDataSource pivotDataSource) {
		super(id, Model.of(pivotDataSource));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		// create a pivot model
		pivotModel = createPivotModel(getModelObject());

		// create pivot field action factory
		pivotFieldActionsFactory = createPivotFieldActionsFactory();

		pivotModel.calculate();

		areasContainer = new WebMarkupContainer("areas");
		areasContainer.setOutputMarkupId(true);
		add(areasContainer);

		RepeatingView areaRepeater = new RepeatingView("area");
		areasContainer.add(areaRepeater);
		List<PivotField.Area> areas = PivotField.Area.getValues();
		for (PivotField.Area area : areas) {
			areaRepeater.add(new PivotAreaPanel(areaRepeater.newChildId(), area));
		}

		pivotTable = createPivotTabel("pivotTable", pivotModel);
		add(pivotTable);

		modal = new ModalWindow("modal");
		modal.setAutoSize(true);
		add(modal);

		AjaxLink<Void> configStoreButton = new AjaxLink<Void>("configStoreButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				modal.setTitle("Configuration");
				modal.setContent(new PivotConfigStoragePanel(ModalWindow.CONTENT_ID, pivotModel, pivotConfigStorage));
				modal.setAutoSize(true);
				modal.show(target);
				modal.setWindowClosedCallback(new WindowClosedCallback() {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClose(AjaxRequestTarget target) {
						target.add(PivotPanel.this);
						if (pivotModel.isAutoCalculate())
							compute(target);
						computeLink.setVisible(!pivotModel.isAutoCalculate());
					}
				});
			}

			@Override
			public boolean isVisible() {
				return super.isVisible() && pivotConfigStorage != null;
			}
		};
		add(configStoreButton);

		AjaxCheckBox showGrandTotalForColumnCheckBox = new AjaxCheckBox("showGrandTotalForColumn",
				new PropertyModel<>(this, "pivotModel.showGrandTotalForColumn")) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				if (pivotModel.isAutoCalculate()) {
					compute(target);
				}
			}

		};
		add(showGrandTotalForColumnCheckBox);

		AjaxCheckBox showGrandTotalForRowCheckBox = new AjaxCheckBox("showGrandTotalForRow",
				new PropertyModel<>(this, "pivotModel.showGrandTotalForRow")) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				if (pivotModel.isAutoCalculate()) {
					compute(target);
				}
			}

		};
		add(showGrandTotalForRowCheckBox);

		AjaxCheckBox autoCalculateCheckBox = new AjaxCheckBox("autoCalculate", new PropertyModel<>(this,
				"pivotModel.autoCalculate")) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				computeLink.setVisible(!pivotModel.isAutoCalculate());
				target.add(computeLink);

				if (pivotModel.isAutoCalculate() && !pivotTable.isVisible()) {
					compute(target);
				}
			}

		};
		add(autoCalculateCheckBox);

		computeLink = new IndicatingAjaxLink<Void>("compute") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				compute(target);
			}

			/*
			 * @Override public boolean isEnabled() { return verify(); }
			 */

		};
		computeLink.setOutputMarkupPlaceholderTag(true);
		computeLink.add(AttributeModifier.append("class", new ButtonCssClassModel()));
		computeLink.setVisible(!pivotModel.isAutoCalculate());
		add(computeLink);

		downloadContainer = new WebMarkupContainer("downloadContainer");
		downloadContainer.setOutputMarkupPlaceholderTag(true);
		downloadContainer.setVisible(pivotTable.isVisible() && (pivotExporters.length > 0));
		add(downloadContainer);

		RepeatingView downloadExports = new RepeatingView("downloadExport");
		downloadContainer.add(downloadExports);
		for (final PivotExporter exporter : pivotExporters) {
			Link<Void> downloadLink = new Link<Void>(downloadExports.newChildId()) {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick() {
					pivotModel.calculate();
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					try {
						exporter.exportPivot(getPivotModel(), out);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					ResourceRequestHandler downloadHandler = new ResourceRequestHandler(new ByteArrayResource(
							exporter.getFormatMimetype(), out.toByteArray(), pivotExportFilename + "."
									+ exporter.getFilenameExtension()), null);
					RequestCycle.get().scheduleRequestHandlerAfterCurrent(downloadHandler);
				}
			};
			downloadExports.add(downloadLink);
			IModel<String> resourceModel = new StringResourceModel("downloadAs", downloadLink, Model.of(exporter
					.getFormatName()));
			downloadLink.add(new Label("label", resourceModel));
			downloadLink.setOutputMarkupPlaceholderTag(true);
			downloadLink.add(AttributeModifier.append("class", new ButtonCssClassModel()));
		}

		add(new PivotResourcesBehavior());
		if (pivotModel.isAutoCalculate()) {
			compute(null);
		}
		
		setOutputMarkupId(true);
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);

		if (event.getPayload() instanceof AreaChangedEvent) {
			AjaxRequestTarget target = ((AreaChangedEvent) event.getPayload()).getAjaxRequestTarget();
			target.add(areasContainer);
			target.add(computeLink);

			if (pivotModel.isAutoCalculate()) {
				compute(target);
			}
		}
	}

	public PivotModel getPivotModel() {
		return pivotModel;
	}

	public PivotFieldActionsFactory getPivotFieldActionsFactory() {
		return pivotFieldActionsFactory;
	}

	public void compute(AjaxRequestTarget target) {
		if (!verify()) {
			return;
		}

		pivotModel.calculate();
		PivotTable newPivotTable = new PivotTable("pivotTable", pivotModel);
		pivotTable.replaceWith(newPivotTable);
		pivotTable = newPivotTable;
		if (target != null) {
			// update pivot table
			target.add(pivotTable);

			// update download container visibility
			downloadContainer.setVisible(pivotTable.isVisible() && (pivotExporters.length > 0));
			target.add(downloadContainer);
		}
	}

	protected PivotModel createPivotModel(PivotDataSource pivotDataSource) {
		PivotModel pivotModel = new DefaultPivotModel(pivotDataSource);

		// debug
		/*
		 * Tree columnsHeaderTree = pivotModel.getColumnsHeaderTree();
		 * System.out.println("### Columns Header Tree ###");
		 * TreeHelper.printTree(columnsHeaderTree.getRoot());
		 * TreeHelper.printLeafValues(columnsHeaderTree.getRoot());
		 * 
		 * Tree rowsHeaderTree = pivotModel.getRowsHeaderTree();
		 * System.out.println("### Rows Header Tree ### ");
		 * TreeHelper.printTree(rowsHeaderTree.getRoot());
		 * TreeHelper.printLeafValues(rowsHeaderTree.getRoot());
		 */

		return pivotModel;
	}

	protected PivotTable createPivotTabel(String id, PivotModel pivotModel) {
		PivotTable pivotTable = new PivotTable(id, pivotModel);
		pivotTable.setOutputMarkupPlaceholderTag(true);
		pivotTable.setVisible(false);

		return pivotTable;
	}

	protected PivotFieldActionsFactory createPivotFieldActionsFactory() {
		return new DefaultPivotFieldActionsFactory();
	}

	private boolean verify() {
		return !pivotModel.getFields(PivotField.Area.DATA).isEmpty()
				&& (!pivotModel.getFields(PivotField.Area.COLUMN).isEmpty() || !pivotModel.getFields(
						PivotField.Area.ROW).isEmpty());
	}

	/**
	 * Set the pivot exporter to use
	 */
	public void setPivotExporters(PivotExporter[] pivotExporter) {
		this.pivotExporters = pivotExporter;
	}

	/**
	 * Set the basename of the download files
	 * 
	 * @param pivotExportFilename
	 */
	public void setPivotExportFilename(String pivotExportFilename) {
		this.pivotExportFilename = pivotExportFilename;
	}

	public IPivotConfigStorage getPivotConfigStorage() {
		return pivotConfigStorage;
	}

	/**
	 * Set the Pivot Configuration Storage to use. By default no storage is set.
	 * 
	 * @param pivotConfigStorage
	 */
	public void setPivotConfigStorage(IPivotConfigStorage pivotConfigStorage) {
		this.pivotConfigStorage = pivotConfigStorage;
	}
}
