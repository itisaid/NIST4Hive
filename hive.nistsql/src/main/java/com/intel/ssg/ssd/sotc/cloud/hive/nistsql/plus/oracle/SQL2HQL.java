package com.intel.ssg.ssd.sotc.cloud.hive.nistsql.plus.oracle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

import com.intel.ssg.ssd.sotc.cloud.hive.nistsql.Base;

public class SQL2HQL {

	public static void run() throws Exception {

		File[] fa = new File(Base.outDir + "/oracle/thin").listFiles();
		for (File f : fa) {
			if (f.isFile() && f.getName().contains(".q")) {
				BufferedReader br = new BufferedReader(new FileReader(f));
				FileOutputStream fos = new FileOutputStream(new File(
						Base.outDir + "/oracle/hql/" + f.getName()));
				String line;
				while ((line = br.readLine()) != null) {
					if (line.contains("hive.ql.mode=sql")) {
						continue;
					}
					fos.write((line + "\r\n").getBytes());
				}
				br.close();
				fos.close();
			}
			
		}

	}

	public static void main(String[] args) throws Exception {
		run();

	}

}
