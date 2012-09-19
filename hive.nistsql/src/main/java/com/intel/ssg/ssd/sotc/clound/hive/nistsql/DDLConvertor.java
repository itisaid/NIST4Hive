package com.intel.ssg.ssd.sotc.clound.hive.nistsql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DDLConvertor {



	private static String derbyDir = Base.baseDir + "hive.nistsql/nist/derby";

	public static void scan() throws Exception {
		File scmd = new File(Base.scmDir);
		File scmf[] = scmd.listFiles();
		File sqld = new File(Base.sqlDir);
		File sqlf[] = sqld.listFiles();
		File derbyd = new File(derbyDir);
		File derbyf[] = derbyd.listFiles();
		List<File> fl = new ArrayList<File>();
		fl.addAll(Arrays.asList(scmf));
		fl.addAll(Arrays.asList(sqlf));
		StringBuilder createSb = new StringBuilder();
		StringBuilder insertSb = new StringBuilder();
		StringBuilder viewSb = new StringBuilder();
		for (File f : fl) {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line;
			String outLine = "";
			while ((line = br.readLine()) != null) {
				if (line.startsWith("--")) {
					continue;
				}
				outLine += line.trim();
				outLine += " ";
				if (line.trim().endsWith(";")) {
					outLine = Base.schemaFilter(outLine);
					if (outLine.length() > 13) {
						outLine += "\n";
						if ("CREATE TABLE ".equals(outLine.substring(0, 13))
								|| "ALTER TABLE ".equals(outLine.substring(0,
										12))) {
							createSb.append(outLine);
						}
						if ("INSERT INTO ".equals(outLine.substring(0, 12))) {
							insertSb.append(outLine);
						}

						if ("CREATE VIEW ".equals(outLine.substring(0, 12))) {
							viewSb.append(outLine);
						}
					}
					outLine = "";
				}
			}
		}

		System.out.println(insertSb.toString());
		FileOutputStream nf = new FileOutputStream(new File(Base.outDir,
				"create.sql"));
		nf.write(createSb.toString().getBytes());
		nf.write(viewSb.toString().getBytes());
		nf.close();

		nf = new FileOutputStream(new File(Base.outDir, "insert.sql"));
		nf.write(insertSb.toString().getBytes());
		nf.close();

		nf = new FileOutputStream(new File(Base.outDir, "view.sql"));
		nf.write(viewSb.toString().getBytes());
		nf.close();
	}

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws Exception {

		scan();
	}

}
