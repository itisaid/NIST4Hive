package com.intel.ssg.ssd.sotc.clound.hive.nistsql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Statement;

public class MysqlTest {

	public static String[] schema = {};

	public static void run() throws Exception {
		Connection conn = Base.getCon();
		Statement st = conn.createStatement();

		File scd = new File(Base.sqlDir);
		File[] scf = scd.listFiles();
		for (File f : scf) {
			BufferedReader br = new BufferedReader(new FileReader(f));

			String line;
			String outLine = "";
			while ((line = br.readLine()) != null) {
				if (line.length()>16&&"-- AUTHORIZATION".equals(line.substring(0,16))) {
					String u = line.replace("-- AUTHORIZATION ", "use ");
					System.out.println(u);
					st.execute(u);
					continue;
				}
				if (line.startsWith("-- ")) {
					continue;
				}
				outLine += line.trim();
				outLine += " ";
				if (line.trim().endsWith(";")) {
					try {
						st.execute(outLine);
						if (outLine.startsWith("SELECT")||!"SELECT USER".equals(outLine.substring(0,11))) {
							
						}
						System.out.println("success:"+outLine);
					} catch (Exception e) {
						System.out.println("failed:"+outLine);
//						e.printStackTrace();
//						throw new Exception();
					}
					outLine="";
				}
			}
			br.close();

		}
		st.close();
		conn.close();
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		run();

	}

}
