/*
 * Copyright 2012, 2013, 2014 Decebal Suiu, Emmeran Seehuber
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
package ro.fortsoft.wicket.pivot.config;

import java.util.List;

/**
 * Implementations of this interface can store and restore the current
 * configuration of the pivot table.
 * 
 * This interface should be implemented by clients.
 */
public interface IPivotConfigStorage {
	/**
	 * 
	 * @return the list of all stored configurations
	 */
	List<String> listConfigNames();

	/**
	 * Load a configuration
	 * 
	 * @param name
	 * @return null or the stored configuration
	 */
	PivotConfig loadConfig(String name);

	/**
	 * Save a configuration. If a configuration with the same name already
	 * exists, it will be overwritten
	 */
	void saveConfig(PivotConfig config);

	/**
	 * Delete the configuration with this name
	 * 
	 * @param name
	 */
	void deleteConfig(String name);
}
