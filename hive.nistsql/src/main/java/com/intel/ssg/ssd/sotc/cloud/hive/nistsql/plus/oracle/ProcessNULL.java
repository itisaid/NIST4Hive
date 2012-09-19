package com.intel.ssg.ssd.sotc.cloud.hive.nistsql.plus.oracle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.intel.ssg.ssd.sotc.cloud.hive.nistsql.Base;

public class ProcessNULL {

	static void exe() throws Exception {
		for (int seq = 0; seq < 1040; seq++) {
			String seqStr = String.format("%04d", seq);
			String inDir = Base.outDir + "/oracle/" + seqStr + "/";
			String outDir = Base.outDir + "/null/" + seqStr + "/";
			File dir = new File(outDir);
			dir.mkdir();
			File[] fl = new File(inDir).listFiles();
			for (File f : fl) {
				FileInputStream in = new FileInputStream(f);
				FileOutputStream out = new FileOutputStream(new File(outDir
						+ f.getName()));
				byte buffer[] = new byte[102400];
				boolean t = false;
				for (int n = 0; -1 != (n = in.read(buffer));) {
					byte[] b = new byte[n*2];
					int count=0;
					for(int i=0;i<n;i++){
						if(buffer[i]!='\0'){
							b[count++]=buffer[i];
						}else{
							b[count++]='\\';
							b[count++]='N';
							t = true;
							
						}
					}
					out.write(b,0,count);
				}
				if(t){
					System.out.println(seqStr+"/"+f.getName());
				}
				in.close();
				out.close();
			}
		}
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		exe();

	}

}
