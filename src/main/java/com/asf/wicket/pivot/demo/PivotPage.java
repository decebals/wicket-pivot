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
package com.asf.wicket.pivot.demo;

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;

import wicketdnd.IEBackgroundImageCacheFix;
import wicketdnd.IECursorFix;
import wicketdnd.theme.WebTheme;

import com.asf.wicket.pivot.PivotDataSource;
import com.asf.wicket.pivot.web.PivotPanel;

/**
 * @author Decebal Suiu
 */
public class PivotPage extends WebPage {

	private static final long serialVersionUID = 1L;
	
	public PivotPage() {
		super();		
		
		add(new IECursorFix());
        add(new IEBackgroundImageCacheFix());

		// create a pivot data source
		PivotDataSource pivotDataSource = PivotDataSourceHandler.getPivotDataSource();
//		System.out.println("fieldCount = " + pivotDataSource.getFieldCount());
//		System.out.println("rowCount = " + pivotDataSource.getRowCount());
		
		add(new PivotPanel("pivot", pivotDataSource));
	}

	@Override
    public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
        response.renderCSSReference(new WebTheme());
    }
			
}
