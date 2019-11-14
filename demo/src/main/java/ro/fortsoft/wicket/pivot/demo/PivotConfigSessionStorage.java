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
package ro.fortsoft.wicket.pivot.demo;

import com.google.gson.Gson;
import org.apache.wicket.Session;
import ro.fortsoft.wicket.pivot.config.IPivotConfigStorage;
import ro.fortsoft.wicket.pivot.config.PivotConfig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores the configurations in the session. You usually want to store the
 * configuration somewhere else (e.g. in a database)
 */
public class PivotConfigSessionStorage implements IPivotConfigStorage, Serializable {
	private static final long serialVersionUID = 1L;

	@Override
	public List<String> listConfigNames() {
		@SuppressWarnings("unchecked")
		Map<String, String> map = getStorageMap();
		return new ArrayList<>(map.keySet());
	}

	private Map<String, String> getStorageMap() {
		@SuppressWarnings("unchecked")
		HashMap<String, String> map = (HashMap<String, String>) Session.get().getAttribute("PivotConfigStorageSession");
		if (map == null) {
			map = new HashMap<>();
			Session.get().setAttribute("PivotConfigStorageSession", map);
		}
		return map;
	}

	@Override
	public PivotConfig loadConfig(String name) {
		String str = getStorageMap().get(name);
		if (str == null)
			return null;
		return new Gson().fromJson(str, PivotConfig.class);
	}

	@Override
	public void saveConfig(PivotConfig config) {
		getStorageMap().put(config.getName(), new Gson().toJson(config));
	}

	@Override
	public void deleteConfig(String name) {
		getStorageMap().remove(name);
	}

}
