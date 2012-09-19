package com.intel.ssg.ssd.sotc.clound.hive.nistsql.plus.oracle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.intel.ssg.ssd.sotc.clound.hive.nistsql.Base;

public class OracleCaseParser {

	static Map<String,List<String[]>> ora = new HashMap<String,List<String[]>>();
	static List<String[]> ds = new ArrayList<String[]>();
	public static void parse() throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(Base.outDir
				+ "/0co.sql"));
		String line;
		List<String[]> totalList = new ArrayList<String[]>();
		String[] grp = new String[4];
		int idx=0;
		while((line=br.readLine())!=null){
			if(!line.trim().equals("")){
				if(idx>3){
					int sdfds=0;
				}
				grp[idx++]=line;
				continue;
			}
			totalList.add(grp);
			grp = new String[4];
			idx=0;
		}
		
		for(String[] ls:totalList){
			process1(ls);
			process2(ls);
		}
	}
	
	static void process1(String[] ls){
		String s = ls[0].substring(7);
		if(s.contains("SELECT")||s.contains("JOIN")){
			if(!s.contains("INFO_SCHEM")&&!s.contains("INFORMATION_SCHEMA")){
			ds.add(ls);
			}
		}
	}
	
	static void process2(String[] ls){
		String[] s = ls[3].substring(2).split(":");
		List<String[]> t = ora.get(s[0]);
		if(t==null){
			t = new ArrayList<String[]>();
			ora.put(s[0], t);
		}
		t.add(ls);
	}
	
	static void printOra(){
		int count=0;
		int item =0;
		for(Entry<String,List<String[]>> e:ora.entrySet()){
			count++;
			int subCount=0;
			System.out.println(e.getKey()+":");
			for(String[] sa:e.getValue()){
				subCount++;
				for(String s:sa){
					System.out.println(s);
				}
				System.out.println();
			}
			System.out.println("\n---------------"+e.getKey()+" has "+subCount+" items."+"--------------\n");
		item+=subCount;
		}
		System.out.println("Total:"+count+" groups, "+item+" items");
	}

	static void printDs(){
		int count=0;
		for(String[] ss:ds){
			count++;
			for(String s:ss){
				System.out.println(s);
			}
			System.out.println();
		}
		System.out.println("Total:"+count);
	}
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		parse();
		printDs();
//		printOra();
	}

}
