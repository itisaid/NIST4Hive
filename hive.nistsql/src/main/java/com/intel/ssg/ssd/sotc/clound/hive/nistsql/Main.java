package com.intel.ssg.ssd.sotc.clound.hive.nistsql;

public class Main {

	public static void main(String[] args) throws Exception {
		DMLConvertor.convertDML();
		DMLConvertor.detail();
		LoadData.load();
	}
}
