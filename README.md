Simple wicket pivot table
=====================

A pivot table is a data summarization tool found in data visualization programs such as spreadsheets or business intelligence software.
For more information what it's a pivot table see http://en.wikipedia.org/wiki/Pivot_table

Components
-------------------

- PivotDataSource is the data source for pivot table (pivot's fields). The data can be featched from a sql ResultSet (see ResultSetPivotDataSource) 
or other non sql sources.
- PivotModel is the place where I put the pivot configuration, here I can specify what fields are on each area (ROW, COLUMN, DATA)
and the ordering of these fields on each area. Also, here I can specify if I want a grand total on rows and/or columns.
- PivotTable is the component that displays the pivot and it takes a PivotModel object as parameter.
- PivotField is the object that can be put on a pivot's area. This object has a name and an index. As constrain, on each pivot's area must be minimum one field.
Also on aria DATA you can put only fields with Number type. 
  
Use
-------------------

It's very simple to add a pivot table in your wicket application.

    PivotDataSource pivotDataSource = ...;      
    add(new PivotPanel("pivot", pivotDataSource));
  
First you must create a PivotDataSource and second add the pivot panel in your page.
   
Demo
-------------------

I have a tiny demo application with a demo apache derby embeded database. The demo application is in demo package.
To run de demo application use:  
 
    mvn jetty:run

In the internet browser type http://localhost:8081/.

In demo pivot page put some fields on the areas (for example "REGION", "SALESMAN" on ROW area, "YEAR", "MONTH" 
on COLUMN area and "SALES" on DATA area) and press the "Show pivot" button.

You can see a screenshot from demo application in [wiki page] (https://github.com/decebals/wicket-pivot/wiki)

License
--------------
  
Copyright 2012 Decebal Suiu
 
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
the License. You may obtain a copy of the License in the LICENSE file, or at:
 
http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.
