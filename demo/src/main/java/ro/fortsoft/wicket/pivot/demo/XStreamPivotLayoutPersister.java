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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * @author Decebal Suiu
 */
public class XStreamPivotLayoutPersister implements PivotLayouPersister {

    private File file;
    private XStream xstream;

    public XStreamPivotLayoutPersister(File file) {
        this.file = file;

        xstream = new XStream(new DomDriver("UTF-8"));
        xstream.setMode(XStream.NO_REFERENCES);
    }

    @Override
    public PivotLayout load() {
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        try {
            return (PivotLayout) xstream.fromXML(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void save(PivotLayout layout) {
        try {
            xstream.toXML(layout, new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
