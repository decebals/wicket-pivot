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
import java.util.ArrayList;
import java.util.List;

/**
 * @author Decebal Suiu
 */
public class DefaultPivotFieldActionsFactory implements PivotFieldActionsFactory,
		Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public List<PivotFieldAction> createPivotFieldActions(PivotField field) {
		List<PivotFieldAction> fieldActions = new ArrayList<PivotFieldAction>();
//		fieldActions.add(new PivotFieldAction.Delete(field));
		if (field.getArea().equals(PivotField.Area.DATA)) {
			fieldActions.add(new PivotFieldAction.AggregatorAction(field));
		}

		
		return fieldActions;
	}

}
