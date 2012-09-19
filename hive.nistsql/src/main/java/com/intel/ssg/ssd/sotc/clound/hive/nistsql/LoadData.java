package com.intel.ssg.ssd.sotc.clound.hive.nistsql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * make create table file,make hive data csv file
 * @author zhihuili
 *
 */
public class LoadData {

	public static String outDir = Base.baseDir+"hive.nistsql/sql/";
	public static String vmIn = Base.baseDir+"hive.nistsql/src/main/resources/SQLDefinition.vm";
	public static String vmOut = outDir;

	public static void load() {

		Connection conn = null;
		Statement statement1 = null;
		Statement statement2 = null;
		Statement statement3 = null;
		FileOutputStream nf = null;
		try {

			Class.forName(Base.driver);
			conn = DriverManager.getConnection(Base.url, Base.user, Base.password);

			statement1 = conn.createStatement();
			statement2 = conn.createStatement();
			statement3 = conn.createStatement();
			String show = "show tables";
			ResultSet rs = statement1.executeQuery(show);

			StringBuilder sb = new StringBuilder();
			while (rs.next()) {
				String tableName = rs.getString("Tables_in_nist");
				String loadSql = "select * from "
						+ tableName
						+ " into outfile '"
						+ outDir
						+ tableName
						+ ".csv'    fields terminated by ',' optionally enclosed by '' escaped by ''    lines terminated by '\n';";
				System.out.println(loadSql);

				// create csv file
				statement2.execute(loadSql);

				ResultSet der = statement3.executeQuery("describe " + tableName
						+ ";");
				String createStr = "		new Pair(\"" + tableName
						+ "\",\"CREATE TABLE " + tableName + "(";
				while (der.next()) {
					String field = der.getString("Field");
					createStr += field + " ";
					String type = der.getString("Type");
					String hit = type;
					if (type.startsWith("decimal") || type.startsWith("double")) {
						hit = "DOUBLE";
					}
					if (type.startsWith("float")) {
						hit = "FLOAT";
					}
					if (type.startsWith("smallint")) {
						hit = "SMALLINT";
					}
					if (type.startsWith("char") || type.startsWith("varchar")) {
						hit = "STRING";
					}
					if (type.startsWith("int")) {
						hit = "INT";
					}
					if (type.startsWith("time")) {
						hit = "TIMESTAMP";
					}
					createStr += hit;
					if (!der.isLast()) {
						createStr += ",";
					}
				}
				createStr += ") ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE\")";
				if (!rs.isLast()) {
					createStr += ",";
				}
				createStr += "\r\n";
				System.out.print(createStr);
				sb.append(createStr);
			}
			nf = new FileOutputStream(new File(vmOut, "SQLDefinition.java"));
			BufferedReader br = new BufferedReader(new FileReader(vmIn));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().equals("$new_pair")) {
					nf.write(sb.toString().getBytes());
				} else {
					nf.write(line.getBytes());
				}
				nf.write("\r\n".getBytes());
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				nf.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				statement1.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				statement2.cancel();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public static void main(String[] args) throws Exception {
		load();
	}
}