package com.intel.ssg.ssd.sotc.clound.hive.nistsql.plus.oracle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.intel.ssg.ssd.sotc.clound.hive.nistsql.Base;

public class OracleProcessor {

	static String url = "jdbc:oracle:thin:@localhost:1521:frank1";
	static String[] users = { "HU", "CUGINI", "MCGINN", "SULLIVAN1", "FLATER",
			"CANWEPARSELENGTH18", "SUN", "SULLIVAN", "SCHANZLE", "CTS2",
			"CTS1", "CTS1b", "CTS4", "CTS3", "XOPEN1", "XOPEN2" };
	static Map<String, Statement> stmtMap = new HashMap<String, Statement>();
	static {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			for (String user : users) {
				Connection conn = DriverManager.getConnection(url, user, user);
				Statement stmt = conn.createStatement(
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				stmtMap.put(user, stmt);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<String[]> scan() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(Base.sqlDir
				+ "/oracle_runsql.all"));
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
				"0so.sql"));
		FileOutputStream ff = new FileOutputStream(new File(Base.outDir,
				"0fo.sql"));
		FileOutputStream nc = new FileOutputStream(new File(Base.outDir,
				"0co.sql"));
		for (String[] s : scan()) {
			String schema = s[2].trim();
			String sqlFile = s[1].trim();
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(Base.sqlDir + "/"
						+ sqlFile + ".sql"));
			} catch (Exception e) {
				e.printStackTrace();
				ff.write(e.getMessage().getBytes());
				// continue;
				return;
			}
			String dropStr = "drop database IF EXISTS " + schema
					+ " CASCADE;\r\n";
			String createStr = "create database " + schema + ";\r\n";
			String line;
			String useStr = "use " + schema;

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
						stmtMap.get(schema)
								.execute(outLine.replaceAll(";", ""));
					} catch (Exception e) {
						System.out.println(outLine);
						System.err.println(e.getMessage());
						ff.write(("++++++++" + outLine + "::::" + e
								.getMessage()).getBytes());
						if (outLine.startsWith("SELECT")) {
							nc.write((outLine + "\r\n").getBytes());
							nc.write(("==" + sqlFile + "==\r\n" + schema)
									.getBytes());
							nc.write(("\r\n--" + e.getMessage() + "\r\n")
									.getBytes());
						}
						outLine = "";

						continue;
					}
					if (outLine.startsWith("SELECT")) {
						String seqStr = String.format("%04d", seq++);
						String outDir = Base.outDir + "/oracle/" + seqStr + "/";
						String loadStr = load(stmtMap.get(schema), outDir, ff,
								seqStr);

						FileOutputStream qf = new FileOutputStream(new File(
								Base.outDir + "/oracle", seqStr + ".q"));
						qf.write("set hive.ql.mode=hql;\r\n".getBytes());
						
						//for a patch
//						String tmpScm;
//						if((tmpScm=TestResultPatch1.neePatch(seqStr))!=null){
//							//there is a logic bug in the line, same name csv file should be covered, but no effect in fact.
//							String ld = load(stmtMap.get(tmpScm),outDir,ff,seqStr);
//							String dp = "drop database IF EXISTS " + tmpScm
//									+ " CASCADE;\r\n";
//							qf.write(dp.getBytes());
//							String ct = "create database " + tmpScm + ";\r\n";
//							qf.write(ct.getBytes());
//							String us = "use " + tmpScm+";\r\n";
//							qf.write(us.getBytes());
//							qf.write(ld.getBytes());							
//						}
											
						qf.write(dropStr.getBytes());
						qf.write(createStr.getBytes());
						qf.write((useStr + ";\r\n").getBytes());
						qf.write(loadStr.getBytes());
						qf.write("set hive.ql.mode=sql;\r\n".getBytes());
						qf.write(outLine.getBytes());
						qf.close();
						System.out.println("===============" + seqStr);
						nf.write((outLine + "\n").getBytes());

						ResultSet rs = stmtMap.get(schema).executeQuery(
								outLine.replaceAll(";", ""));
						String t= "";
						while (rs.next()) {
							String tmp = "| ";
							for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
								tmp += rs.getObject((i + 1))==null?"NULL":rs.getObject((i + 1)).toString();
								tmp += " | ";
							}
							t += tmp;
							t += "\r\n";
						}
						FileOutputStream tf = new FileOutputStream(new File(
								Base.outDir + "/oracle", seqStr + ".t"));
						tf.write(t.getBytes());
						tf.close();
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
		String[] shows = { "select * from tab where tabtype = 'VIEW'",
				"select * from tab where tabtype = 'TABLE'" };
		StringBuilder sb = new StringBuilder();
		Set<String> conflictSet = new HashSet<String>();//ignore table(view) name of uppercase or lowercase 
		for (String show : shows) {
			ResultSet rs = st.executeQuery(show);
			List<String> loadList = new ArrayList<String>();
			List<String> tableList = new ArrayList<String>();
			while (rs.next()) {
				String tableName = rs.getString(1);
				if(conflictSet.contains(tableName.toLowerCase())){
					continue;
				}else{
					conflictSet.add(tableName.toLowerCase());
				}
				String loadSql = "select * from " + tableName;
				loadList.add(loadSql);
				tableList.add(tableName);
			}
			for (int i = 0; i < loadList.size(); i++) {
				FileOutputStream nf = new FileOutputStream(new File(outDir,
						tableList.get(i) + ".csv"));
				try {
					ResultSet loadRs = st.executeQuery(loadList.get(i));
					while (loadRs.next()) {
						for (int j = 1; j <= loadRs.getMetaData()
								.getColumnCount(); j++) {
							Object o = loadRs.getObject(j);
							nf.write(o == null ? "".getBytes() : o.toString()
									.getBytes());
							nf.write(",".getBytes());
						}
						nf.write("\n".getBytes());
					}
				} catch (Exception e) {
					System.out.println(loadList.get(i) + "::::"
							+ e.getMessage());
					e.printStackTrace();
					ff.write(("***********" + loadList.get(i) + "::::" + e
							.getMessage()).getBytes());
					continue;
					// throw new Exception(e);
				}
				String tableName = tableList.get(i);
				ResultSet der;
				try {
					// String t = "describe " + tableName ;
					String t = "select COLUMN_NAME, DATA_TYPE from USER_TAB_COLUMNS where TABLE_NAME='"
							+ tableName + "' order by column_id";
					der = st.executeQuery(t);
				} catch (Exception e) {
					System.out.println(tableName + " can't create!");
					ff.write((tableName + " can't create! " + e.getMessage())
							.getBytes());
					continue;
				}
				String createStr = "CREATE TABLE " + tableName + "(";
				while (der.next()) {
					String field = der.getString(1);
					createStr += field + " ";
					String type = der.getString(2);
					String hit = type;
					if (type.startsWith("DECIMAL") || type.startsWith("DOUBLE")
							|| type.startsWith("NUMBER")) {
						hit = "DOUBLE";
					}
					if (type.startsWith("FLOAT")) {
						hit = "FLOAT";
					}
					if (type.startsWith("SMALLINT")) {
						hit = "SMALLINT";
					}
					if (type.startsWith("CHAR") || type.startsWith("VARCHAR")) {
						hit = "STRING";
					}
					if (type.startsWith("INT")) {
						hit = "INT";
					}
					if (type.startsWith("TIME")||type.startsWith("DATE")) {
						hit = "TIMESTAMP";
					}
					createStr += hit;
					if (!der.isLast()) {
						createStr += ",";
					}
				}
				createStr += ") ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;";
				createStr += "\r\n";
				String loadHive = "LOAD DATA LOCAL INPATH '"
						+ "_datapath_/plusd/" + seqStr + "/" + tableName
						+ ".csv' OVERWRITE INTO TABLE " + tableName + ";\r\n";
				// System.out.print(createStr);
				sb.append(createStr);
				sb.append(loadHive);
			}
		}
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		run();
	}
	
}
