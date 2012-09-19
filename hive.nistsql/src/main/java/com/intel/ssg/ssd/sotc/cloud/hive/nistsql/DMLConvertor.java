package com.intel.ssg.ssd.sotc.cloud.hive.nistsql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
/**
 * make test select file (.q file). every file has only one select statement in ./q/p directory 
 * @author zhihuili
 *
 */
public class DMLConvertor {

	private static String inDir = Base.baseDir + "hive.nistsql/nist/sql";


	public static void convertDML() throws Exception {
		File directory = new File(inDir);
		File f[] = directory.listFiles();
		StringBuilder all = new StringBuilder();
		all.append("set hive.ql.mode=sql;\n");
		for (int i = 0; i < f.length; i++) {
			BufferedReader br = new BufferedReader(new FileReader(f[i]));
			StringBuilder qsb = new StringBuilder();
			qsb.append("set hive.ql.mode=sql;\n");
			
			
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
					if (!outLine.startsWith("SELECT")||"SELECT USER".equals(outLine.substring(0,11))) {
						outLine = "";
						continue;
					}
					outLine += "\n";
					qsb.append(outLine);
					all.append(outLine);
					outLine = "";
				}
			}
			FileOutputStream nf = new FileOutputStream(new File(Base.outDir, f[i]
					.getName().replace(".sql", ".q")));
			nf.write(qsb.toString().getBytes());
			nf.close();
			br.close();
		}
		FileOutputStream nf = new FileOutputStream(new File(Base.outDir, "all.sql"));
		nf.write(all.toString().getBytes());
		nf.close();
	}
	
	public static void detail() throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(new File(Base.outDir, "all.sql")));
		String line;
		String outStr;
		int count=0;
		br.readLine();
		while((line=br.readLine())!=null){
			outStr="";
			outStr+="set hive.ql.mode=sql;\n";
			outStr+=line;
			FileOutputStream nf = new FileOutputStream(new File(Base.outDir+"/p", String.format("%04d", count++)+".q"));
			nf.write(outStr.getBytes());
			nf.close();
			
		}
	}

	public static void main(String[] args) throws Exception {
		convertDML();
		detail();
	}

}
