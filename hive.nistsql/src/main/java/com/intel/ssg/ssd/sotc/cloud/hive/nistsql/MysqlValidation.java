package com.intel.ssg.ssd.sotc.cloud.hive.nistsql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MysqlValidation {

	public static void checkSelect() throws Exception {

		String line;
		int count = 0;
		int total = 0;
		Connection conn = null;
		Statement statement1 = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(Base.outDir,
					"all.sql")));
			Class.forName(Base.driver);
			conn = Base.getCon();
			statement1 = conn.createStatement();
			br.readLine();
			Pattern pattern = Pattern.compile("INFORMATION_SCHEMA");
			while ((line = br.readLine()) != null) {
				try {
					total++;
					statement1.executeQuery(line);
					Matcher matcher = pattern.matcher(line);
					if (matcher.find()) {
						throw new Exception();
					}
//					System.out.println(line);
				} catch (Exception e) {
					filterSQL(line);
					count++;
					File del = new File(Base.outDir + "/p", String.format(
							"%04d", total - 1) + ".q");
					if (del.exists()) {
//						System.out.println("delete------------"+del.getName());
						del.delete();
					}
				}
			}
		} finally {
			br.close();
			statement1.close();
			conn.close();
		}
		System.out.println("total:" + total + "! failed:" + count);

	}
	
	public static void filterSQL(String sql){

		if(sql.startsWith("SELECT USER")){
			return;
		}
		Pattern p =  Pattern.compile("INFORMATION_SCHEMA");
		Matcher m = p.matcher(sql);
		if(m.find()){
			return;
			
		}
		
		 p =  Pattern.compile("FIPS_DOCUMENTATION");
		m = p.matcher(sql);
		if(m.find()){
			return;
			
		}

		if(sql.length()<40){
			return;
		}
		System.out.println(sql);
	}

	public static void main(String[] args) throws Exception {
		checkSelect();
	}
}
