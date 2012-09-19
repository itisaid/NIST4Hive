package com.intel.ssg.ssd.sotc.clound.hive.nistsql.plus.oracle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SQLHQLSetAnylizer {

	static String path = "C:/work/shengsheng/src/hive.nistsql/temp/oracle";
	static Map<String, String> hmap = new HashMap<String, String>();
	static Map<String, String> smap = new HashMap<String, String>();
	static Set<String> intersectSet = new HashSet<String>();
	static Set<String> hSet = new HashSet<String>();
	static Set<String> sSet = new HashSet<String>();

	public static void run() throws Exception {
		BufferedReader hbr = new BufferedReader(new FileReader(new File(path
				+ "/hql.rpt")));
		BufferedReader sbr = new BufferedReader(new FileReader(new File(path
				+ "/sql.rpt")));

		String line;
		while ((line = hbr.readLine()) != null) {
			String[] s = line.split("\",\"");
			hmap.put(s[0].substring(1), line);
		}
		while ((line = sbr.readLine()) != null) {
			String[] s = line.split("\",\"");
			smap.put(s[0].substring(1), line);
		}
		for (Entry<String, String> eh : hmap.entrySet()) {
			if (smap.containsKey(eh.getKey())) {
				intersectSet.add(eh.getKey());
			} else {
				hSet.add(eh.getKey());
			}
		}
		for (Entry<String, String> es : smap.entrySet()) {
			if (hmap.containsKey(es.getKey())) {
				if (!intersectSet.contains(es.getKey())) {
					System.err.println(es.getKey());
				}
			} else {
				sSet.add(es.getKey());
			}
		}
	}

	static void print(){
		System.out.println("----------intersect set in sql & hql----------");
		int counter=0;
		for(String s:intersectSet){
			System.out.println(counter+++" : "+smap.get(s));
		}
		counter=0;
		System.out.println("---------set only in hql-----------");
		for(String s:hSet){
			System.out.println(counter+++":"+hmap.get(s));
		}
		counter=0;
		System.out.println("---------set only in sql-----------");
		for(String s:sSet){
			System.out.println(counter+++":"+smap.get(s));
		}
	}

	public static void main(String[] args) throws Exception {
		run();
		print();
	}

}
