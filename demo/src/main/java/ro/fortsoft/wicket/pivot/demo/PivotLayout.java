/*
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

import ro.fortsoft.wicket.pivot.PivotField;

import java.io.Serializable;
import java.util.List;

/**
 * @author Decebal Suiu
 */
public class PivotLayout implements Serializable {

    private List<PivotField> fields;

    private boolean showGrandTotalForColumn;
    private boolean showGrandTotalForRow;
    private boolean autoCalculate;

    public List<PivotField> getFields() {
        return fields;
    }

    public void setFields(List<PivotField> fields) {
        this.fields = fields;
    }

    public boolean isShowGrandTotalForColumn() {
        return showGrandTotalForColumn;
    }

    public void setShowGrandTotalForColumn(boolean showGrandTotalForColumn) {
        this.showGrandTotalForColumn = showGrandTotalForColumn;
    }

    public boolean isShowGrandTotalForRow() {
        return showGrandTotalForRow;
    }

    public void setShowGrandTotalForRow(boolean showGrandTotalForRow) {
        this.showGrandTotalForRow = showGrandTotalForRow;
    }

    public boolean isAutoCalculate() {
        return autoCalculate;
    }

    public void setAutoCalculate(boolean autoCalculate) {
        this.autoCalculate = autoCalculate;
    }

}
