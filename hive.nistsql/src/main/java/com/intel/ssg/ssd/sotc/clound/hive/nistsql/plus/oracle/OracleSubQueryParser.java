package com.intel.ssg.ssd.sotc.clound.hive.nistsql.plus.oracle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OracleSubQueryParser {

	static List<String> ds = new ArrayList<String>();
	static int total = 0;
	static int hql = 0;
	static int sql = 0;
	static int hqld = 0;
	static int sqld = 0;
	static String all = "C:/work/shengsheng/src/hive.nistsql/q/0so.sql";
	static String rptPath = "C:/work/shengsheng/src/hive.nistsql/temp/oracle";
	static Set<String> hqlDSet = new HashSet<String>();
	static Set<String> sqlDSet = new HashSet<String>();

	static {
		BufferedReader br;
		try {
			String line;

			br = new BufferedReader(new FileReader(new File(rptPath, "hql.d")));
			while ((line = br.readLine()) != null) {
				String s = line.substring(41, 45);
				// System.out.println(s);
				hqlDSet.add(s);
			}
			br.close();

			br = new BufferedReader(new FileReader(new File(rptPath, "sql.d")));
			while ((line = br.readLine()) != null) {
				String s = line.substring(41, 45);
				// System.out.println(s);
				sqlDSet.add(s);
			}
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static boolean process(String sql) {
		String s = sql.substring(7);
		if (s.contains("SELECT")) {
			System.out.println(sql);
			return true;
		}
		return false;
	}

	static String getRPTSql(String line) {
		String[] s = line.split("\",\"");
		// System.out.println(s[1]);
		return s[1];
	}

	static String getRPTSeq(String line) {
		String[] s = line.split("\",\"");
		return s[0].substring(1);
	}

	static void parse() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(new File(all)));
		String line;
		while ((line = br.readLine()) != null) {
			if (process(line)) {
				total++;
			}
		}
		br.close();
		br = new BufferedReader(new FileReader(new File(rptPath + "/hql.rpt")));
		while ((line = br.readLine()) != null) {
			if (process(getRPTSql(line))) {
				hql++;
				if (hqlDSet.contains(getRPTSeq(line))) {
					hqld++;
				}
			}
		}
		br.close();
		br = new BufferedReader(new FileReader(new File(rptPath + "/sql.rpt")));
		while ((line = br.readLine()) != null) {
			if (process(getRPTSql(line))) {
				sql++;
				if (sqlDSet.contains(getRPTSeq(line))) {
					sqld++;
				}
			}
		}
		br.close();
	}

	public static void main(String[] args) throws Exception {
		parse();
		System.out.println("total sub query:" + total);
		System.out.println("failed hql sub query:" + hql
				+ ", include incorrect result:" + hqld);
		System.out.println("failed sql sub query:" + sql
				+ ", include incorrect result:" + sqld);

	}
}
