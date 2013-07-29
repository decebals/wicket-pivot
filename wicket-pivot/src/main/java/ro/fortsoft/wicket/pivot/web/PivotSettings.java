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

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * @author Decebal Suiu
 */
public class PivotSettings {

	@SuppressWarnings("serial")
	private static final MetaDataKey<PivotSettings> KEY = new MetaDataKey<PivotSettings>() {};

	private ResourceReference javaScriptReference = new PackageResourceReference(
			PivotSettings.class, "res/pivot.js");
	private ResourceReference cssReference = new PackageResourceReference(
			PivotSettings.class, "res/pivot.css");
	private ResourceReference jqueryReference = new PackageResourceReference(
			PivotSettings.class, "res/jquery-1.8.3.min.js");
	private ResourceReference jqueryUIReference = new PackageResourceReference(
			PivotSettings.class, "res/jquery-ui-1.9.2.min.js");
	private ResourceReference jqueryJsonReference = new PackageResourceReference(
			PivotSettings.class, "res/jquery.json-2.2.min.js");
	private ResourceReference bootstrapJavaScriptReference = new PackageResourceReference(
			PivotSettings.class, "res/bootstrap.js");
	private ResourceReference bootstrapCssReference = new PackageResourceReference(
			PivotSettings.class, "res/bootstrap.css");

	private boolean includeJQuery = false;
	private boolean includeJQueryUI = true;
	private boolean includeJQueryJson = true;
	private boolean includeJavaScript = true;
	private boolean includeCss = true;
	private boolean includeBootstrap = true;

	/**
	 * Private constructor, use {@link #get()} instead.
	 */
	private PivotSettings() {
	}

	public boolean isIncludeJQuery() {
		return includeJQuery;
	}

	public PivotSettings setIncludeJQuery(boolean includeJQuery) {
		this.includeJQuery = includeJQuery;
		return this;
	}

	public boolean isIncludeJQueryUI() {
		return includeJQueryUI;
	}

	public PivotSettings setIncludeJQueryUI(boolean includeJQueryUI) {
		this.includeJQueryUI = includeJQueryUI;
		return this;
	}

	public boolean isIncludeJQueryJson() {
		return includeJQueryJson;
	}

	public void setIncludeJQueryJson(boolean includeJQueryJson) {
		this.includeJQueryJson = includeJQueryJson;
	}

	public boolean isIncludeJavaScript() {
		return includeJavaScript;
	}

	public PivotSettings setIncludeJavascript(boolean includeJavaScript) {
		this.includeJavaScript = includeJavaScript;
		return this;
	}

	public boolean isIncludeCss() {
		return includeCss;
	}

	public PivotSettings setIncludeCss(boolean includeCss) {
		this.includeCss = includeCss;
		return this;
	}

	public boolean isIncludeBootstrap() {
		return includeBootstrap;
	}

	public PivotSettings setIncludeBootstrap(boolean includeBootstrap) {
		this.includeBootstrap = includeBootstrap;
		return this;
	}

	public ResourceReference getJQueryReference() {
		return jqueryReference;
	}

	public PivotSettings setJQueryReference(ResourceReference jqueryReference) {
		this.jqueryReference = jqueryReference;
		return this;
	}

	public ResourceReference getJQueryUIReference() {
		return jqueryUIReference;
	}

	public PivotSettings setJQueryUIReference(ResourceReference jqueryUIReference) {
		this.jqueryUIReference = jqueryUIReference;
		return this;
	}

	public ResourceReference getJQueryJsonReference() {
		return jqueryJsonReference;
	}

	public PivotSettings setJQueryJsonReference(ResourceReference jqueryJsonReference) {
		this.jqueryJsonReference = jqueryJsonReference;
		return this;
	}

	public ResourceReference getJavaScriptReference() {
		return javaScriptReference;
	}

	public PivotSettings setJavaScriptReference(ResourceReference javaScriptReference) {
		this.javaScriptReference = javaScriptReference;
		return this;
	}

	public ResourceReference getCssReference() {
		return cssReference;
	}

	public PivotSettings setCssReference(ResourceReference cssReference) {
		this.cssReference = cssReference;
		return this;
	}

	public ResourceReference getBootstrapJavaScriptReference() {
		return bootstrapJavaScriptReference;
	}

	public PivotSettings setBootstrapJavaScriptReference(ResourceReference bootstrapJavaScriptReference) {
		this.bootstrapJavaScriptReference = bootstrapJavaScriptReference;
		return this;
	}

	public ResourceReference getBootstrapCssReference() {
		return bootstrapCssReference;
	}

	public PivotSettings setBootstrapCssReference(ResourceReference bootstrapCssReference) {
		this.bootstrapCssReference = bootstrapCssReference;
		return this;
	}

	/**
	 * Retrieves the instance of settings object.
	 * 
	 * @return settings instance
	 */
	public static PivotSettings get() {
		Application application = Application.get();
		PivotSettings settings = application.getMetaData(KEY);
		if (settings == null) {
			synchronized (application) {
				settings = application.getMetaData(KEY);
				if (settings == null) {
					settings = new PivotSettings();
					application.setMetaData(KEY, settings);
				}
			}
		}
		
		return application.getMetaData(KEY);
	}

}
