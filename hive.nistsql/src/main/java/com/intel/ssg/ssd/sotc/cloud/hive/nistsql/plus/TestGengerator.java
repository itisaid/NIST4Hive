package com.intel.ssg.ssd.sotc.cloud.hive.nistsql.plus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.intel.ssg.ssd.sotc.cloud.hive.nistsql.Base;

public class TestGengerator {

	public static List<String[]> scan() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(Base.sqlDir
				+ "/runsql.all"));
		List<String[]> l = new ArrayList<String[]>();
		String line;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("RUNSQL")) {
				String[] s = line.split("   ");
				l.add(s);
			}
		}
		return l;
	}

	public static void run() throws Exception {
		int seq = 0;
		FileOutputStream nf = new FileOutputStream(new File(Base.outDir,
				"0s.sql"));
		FileOutputStream ff = new FileOutputStream(new File(Base.outDir,
				"0f.sql"));
		FileOutputStream nc = new FileOutputStream(new File(Base.outDir,
				"0c.sql"));
		Connection conn = Base.getCon();
		Statement st = conn.createStatement();
		for (String[] s : scan()) {
			String schema = s[2];
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(Base.sqlDir + "/" + s[1]
						+ ".sql"));
			} catch (Exception e) {
				e.printStackTrace();
				ff.write(e.getMessage().getBytes());
				continue;
			}
			String dropStr = "drop database IF EXISTS " + schema + " CASCADE;\r\n";
			String createStr = "create database " + schema + ";\r\n";
			String line;
			String useStr = "use " + s[2];
			try {
				st.execute(useStr);

			} catch (Exception e) {
				System.err.println(e.getMessage());
				ff.write(e.getMessage().getBytes());
				continue;
			}
			String outLine = "";
			while ((line = br.readLine()) != null) {
				if (line.startsWith("--")) {
					continue;
				}
				outLine += line.trim();
				outLine += " ";
				if (line.trim().endsWith(";")) {
					outLine = outLine.trim();
					if (outLine.length() > 11
							&& "SELECT USER".equals(outLine.substring(0, 11))) {
						outLine = "";
						continue;
					}
					try {
						System.out.println("########" + outLine);
						st.execute(outLine);
					} catch (Exception e) {
						System.out.println(outLine);
						System.err.println(e.getMessage());
						ff.write(("++++++++" + outLine + "::::" + e
								.getMessage()).getBytes());
						if (outLine.startsWith("SELECT")) {
							nc.write((outLine + "\r\n").getBytes());
							nc.write(("==" + s[1] + "==\r\n" + s[2]).getBytes());
							nc.write(("\r\n--" + e.getMessage() + "\r\n")
									.getBytes());
						}
						outLine = "";

						continue;
					}
					if (outLine.startsWith("SELECT")) {
						String seqStr = String.format("%04d", seq++);
						String outDir = Base.outDir + "/plus/" + seqStr + "/";
						String loadStr = load(st, outDir, ff, seqStr);

						FileOutputStream qf = new FileOutputStream(new File(
								Base.outDir + "/plus", seqStr + ".q"));
						qf.write("set hive.ql.mode=hql;\r\n".getBytes());
						qf.write(dropStr.getBytes());
						qf.write(createStr.getBytes());
						qf.write((useStr + ";\r\n").getBytes());
						qf.write(loadStr.getBytes());
//						qf.write("set hive.ql.mode=sql;\r\n".getBytes());
						qf.write(outLine.getBytes());
						qf.close();
						System.out.println("===============" + seqStr);
						nf.write((outLine + "\n").getBytes());
					}
					outLine = "";
				}
			}
		}
		nf.close();
		ff.close();
		nc.close();
	}

	public static String load(Statement st, String outDir, FileOutputStream ff,
			String seqStr) throws Exception {

		File dir = new File(outDir);
		dir.mkdir();
		String show = "show tables";
		ResultSet rs = st.executeQuery(show);

		StringBuilder sb = new StringBuilder();
		List<String> loadList = new ArrayList<String>();
		List<String> tableList = new ArrayList<String>();
		while (rs.next()) {
			String tableName = rs.getString(1);
			String loadSql = "select * from "
					+ tableName
					+ " into outfile '"
					+ outDir
					+ tableName
					+ ".csv'    fields terminated by ',' optionally enclosed by '' escaped by ''    lines terminated by '\n';";
			loadList.add(loadSql);
			tableList.add(tableName);
		}
		for (int i = 0; i < loadList.size(); i++) {
			try {
				st.execute(loadList.get(i));
			} catch (Exception e) {
				System.out.println(loadList.get(i) + "::::" + e.getMessage());
				ff.write(("***********" + loadList.get(i) + "::::" + e
						.getMessage()).getBytes());
				continue;
				// throw new Exception(e);
			}
			String tableName = tableList.get(i);
			ResultSet der;
			try {
				der = st.executeQuery("describe " + tableName + ";");
			} catch (Exception e) {
				System.out.println(tableName + "can't create!");
				ff.write((tableName + "can't create!" + e.getMessage())
						.getBytes());
				continue;
			}
			String createStr = "CREATE TABLE " + tableName + "(";
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
			createStr += ") ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;";
			createStr += "\r\n";
			String loadHive = "LOAD DATA LOCAL INPATH '" + "_datapath_/plusd/"
					+ seqStr + "/" + tableName + ".csv' OVERWRITE INTO TABLE "
					+ tableName + ";\r\n";
			// System.out.print(createStr);
			sb.append(createStr);
			sb.append(loadHive);
		}
		return sb.toString();
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// for (String[] s : scan()) {
		// System.out.println(s[1]);
		// }

		run();

	}

}
