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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.asf.wicket.pivot.PivotDataSource;
import com.asf.wicket.pivot.ResultSetPivotDataSource;

/**
 * @author Decebal Suiu
 */
public class PivotDataSourceHandler {

	public static PivotDataSource getPivotDataSource() {
		Connection connection = getConnection();
		if (connection == null) {
			return null;
		}
		
		String sql = "select * from STATISTIC";
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			return new ResultSetPivotDataSource(resultSet);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} finally {
			try { 
				resultSet.close();
				statement.close();
				connection.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
	
	private static Connection getConnection() {
		String url = "jdbc:derby:./demo/db/";
		String dbName = "pivot";
		String driver = "org.apache.derby.jdbc.EmbeddedDriver";
		String userName = "sa"; 
		String password = "";
		  
		try {
			Class.forName(driver).newInstance();
			return DriverManager.getConnection(url + dbName, userName, password);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
}
