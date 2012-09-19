package com.intel.ssg.ssd.sotc.cloud.hive.nistsql.plus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.Statement;

import com.intel.ssg.ssd.sotc.cloud.hive.nistsql.Base;

public class TestProcessor {
	static String TEST_DIR = Base.baseDir + "/hive.nistsql/testresultplus";
	static String TEST_FAILED = TEST_DIR + "/t.t";

	public static void printFailedSql() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(new File(
				TEST_FAILED)));
		String line = "";
		while ((line = br.readLine()) != null) {
			String[] s = line.split(":");
			String f = s[1].trim();
			BufferedReader sf = new BufferedReader(new FileReader(new File(
					Base.outDir + "/plus", f)));
//			sf.readLine();//skip sql set line.
			String t1="",t2="";
			while((t1=sf.readLine())!=null){
				t2=t1;
			}
			
			System.out.println("\""+f.split("\\.")[0]+"\""+","+"\""+t2+"\"");
		}
	}

	public static void checkResult() throws Exception {
		File dir = new File(TEST_DIR);
		int failedNum = 0;
		File[] fs = dir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				String filename = pathname.getName().toLowerCase();
				if (filename.contains(".q.out")) {
					return true;
				} else {
					return false;
				}
			}
		});
		Statement st = Base.getCon().createStatement();
		for (File f : fs) {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String hiveResult = "";
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("PREHOOK:") || line.startsWith("POSTHOOK:")
						|| line.startsWith("####")) {
					continue;
				}
				String[] hiveRow = line.split("\t");

				String tmp = "| ";
				for (String r : hiveRow) {
					try {
						Float.valueOf(r);
						String[] ar = r.split("\\.");
						if(ar.length==2&&ar[1].equals("0")){
							r = ar[0];
						}
					} catch (Exception e) {

					}
					tmp += r;
					tmp += " | ";
				}

				// for(int i=0;i<line.length();i++){
				// line.charAt(i)==0x0a;
				// }
				hiveResult += tmp;
				hiveResult += "\r\n";
			}

			String mysqlResult = "";
			String id = f.getName().substring(0, 4);
			String sqlFileName = Base.outDir + "/p/" + id + ".q";
			BufferedReader sqlbr = new BufferedReader(new FileReader(
					sqlFileName));
			sqlbr.readLine();
			String sql = sqlbr.readLine();
			ResultSet rs = st.executeQuery(sql);
			while (rs.next()) {
				String tmp = "| ";
				for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
					tmp += rs.getObject((i + 1))==null?"NULL":rs.getObject((i + 1)).toString();
					tmp += " | ";
				}
				mysqlResult += tmp;
				mysqlResult += "\r\n";
			}

			if (mysqlResult.equals(hiveResult)) {
				// System.out.println(id+" PASS.");
			} else {
				failedNum++;
				System.out.println(id + " NG. sql: " + sql);
				System.out.println("expected(mysql):\r\n" + mysqlResult);
				System.out.println("fact(hive):\r\n" + hiveResult);
			}
		}
		System.out.println("TOTAL NUMBER OF DIFFERENT RESULT BETWEEN HIVE AND MYSQL:"+failedNum);
	}

	public static void main(String[] args) throws Exception {
		 printFailedSql();
//		checkResult();
	}
}
