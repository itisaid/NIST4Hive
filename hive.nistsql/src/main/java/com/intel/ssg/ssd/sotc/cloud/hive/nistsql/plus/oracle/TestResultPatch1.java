package com.intel.ssg.ssd.sotc.cloud.hive.nistsql.plus.oracle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * patch for cross database access.
 * 
 * @author zhihuili
 * 
 */
public class TestResultPatch1 {

	static String[] list = { "0063,HU", "0082,HU", "0083,HU", "0084,HU",
			"0085,HU", "0088,HU", "0089,HU", "0792,FLATER", "0795,FLATER",
			"0796,FLATER" };
	static Map<String, String> map = new HashMap<String, String>();
	static {
		for (String s : list) {
			String[] kv = s.split(",");
			map.put(kv[0], kv[1]);
		}
	}

	public static String needPatch(String seq) {
		return map.get(seq);
	}

	public static String needPatch(String sql, String schema) {
		try {
			List<String> schemas = SqlParser.getSchemas(sql.toLowerCase());
			for (String s : schemas) {
				if (!s.equals(schema.toLowerCase())) {
					return s.toUpperCase();
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	static void printIncorrectSql() throws Exception {
		String rptPath = "C:/work/shengsheng/src/hive.nistsql/temp/oracle";
		Set<String> hqlDSet = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(new File(rptPath,
				"hql.d")));
		String line;
		while ((line = br.readLine()) != null) {
			String s = line.substring(41, 45);
			hqlDSet.add(s);
		}
		br.close();

		br = new BufferedReader(new FileReader(new File(rptPath + "/hql.rpt")));
		int c = 0;
		while ((line = br.readLine()) != null) {

			if (hqlDSet.contains(OracleSubQueryParser.getRPTSeq(line))) {
				System.out.println(OracleSubQueryParser.getRPTSeq(line) + ":"
						+ OracleSubQueryParser.getRPTSql(line));
				c++;
			}

		}
		System.out.println(c);
		br.close();
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		printIncorrectSql();
	}

}
