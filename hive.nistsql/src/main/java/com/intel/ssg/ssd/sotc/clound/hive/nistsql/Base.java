package com.intel.ssg.ssd.sotc.clound.hive.nistsql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Base {

	public static String baseDir = "/home/frank/work/hive-0.9.0-plsql-support/src/";
	public static String outDir = Base.baseDir + "hive.nistsql/q";
	public static String driver = "com.mysql.jdbc.Driver";
	public static String url = "jdbc:mysql://127.0.0.1:3306/";
	public static String user = "root";
	public static String password = "111111";
	public static String[] schema = {"HU","SUN","CTS1","CTS2","FLATER","CANWEPARSELENGTH18","CUGINI",};
	public static String scmDir = Base.baseDir + "hive.nistsql/pcisql/schema";
	public static String sqlDir = Base.baseDir + "hive.nistsql/pcisql/sql";

	public static Connection getCon() throws Exception {
		Class.forName(driver);
		return DriverManager.getConnection(Base.url, Base.user, Base.password);
	}
	
	public static String schemaFilter(String sql){
		String r = sql.trim();
		for(String s:schema){
			r = r.replaceAll(s+"\\.", "");
		}
		return r;
	}
	
	public static void main(String[] args){
		System.out.println(schemaFilter("SELECT EMPNAME FROM HU.STAFF14 WHERE EMPNAME = 'SUN'"));
	}
}
