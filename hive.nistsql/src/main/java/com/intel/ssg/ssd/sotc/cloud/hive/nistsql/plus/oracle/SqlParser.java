package com.intel.ssg.ssd.sotc.cloud.hive.nistsql.plus.oracle;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;

import com.intel.ssg.ssd.sotc.cloud.hive.nistsql.Base;

import br.com.porcelli.parser.plsql.PLSQLLexer;
import br.com.porcelli.parser.plsql.PLSQLParser;

public class SqlParser {

	public static CommonTree buildAST(String sql) throws Exception {

		// Create an input character stream from standard in
		// System.out.println("query string-> "+args[0]);
		ANTLRInputStream input = new ANTLRInputStream(new ByteArrayInputStream(
				sql.getBytes()));
		// Create an ExprLexer that feeds from that stream
		PLSQLLexer lexer = new PLSQLLexer(input);
		// Create a stream of tokens fed by the lexer
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		// Create a parser that feeds off the token stream
		PLSQLParser parser = new PLSQLParser(tokens);
		// Begin parsing at rule prog
		PLSQLParser.seq_of_statements_return r = parser.seq_of_statements();
		CommonTree t = (CommonTree) r.getTree();
		return t;

	}

	public static List<String> getSchemas(String sql) throws Exception {
		CommonTree root = buildAST(sql);
		List<String> schemas = new ArrayList<String>();
		getTableSchema(root, schemas);
		return schemas;
	}

	protected static void getTableSchema(CommonTree node, List<String> schemas) {
		if (node.getType() == PLSQLParser.TABLEVIEW_NAME) {
			if (node.getChildCount() == 2) {
				schemas.add(node.getChild(0).getText());
			}
		}
		for (int i = 0; i < node.getChildCount(); i++) {
			getTableSchema((CommonTree) node.getChild(i), schemas);
		}
	}

	protected static void getTables(CommonTree node, List<String> tables) {
		if (node.getType() == PLSQLParser.TABLEVIEW_NAME) {
			if (node.getChildCount() == 1) {
				tables.add(node.getChild(0).getText());
			}
			if (node.getChildCount() == 2) {
				tables.add(node.getChild(1).getText());
			}
		}
		for (int i = 0; i < node.getChildCount(); i++) {
			getTables((CommonTree) node.getChild(i), tables);
		}
	}

	public static void scanMultiTable(CommonTree node,List<String> l) {
		if (node.getType() == PLSQLParser.SQL92_RESERVED_FROM) {
			if (node.getChildCount() > 1) {
				l.add("");
			}
		}

		for (int i = 0; i < node.getChildCount(); i++) {
			 scanMultiTable((CommonTree) node.getChild(i),l);
		}

	}
	
	public static boolean isMultiTable(String sql){
		List<String> l=new ArrayList<String>();
		CommonTree root;
		try {
			root = buildAST(sql.toLowerCase());
			scanMultiTable(root,l);
			return l.size()>0?true:false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
//			System.err.println("===="+sql);
		}
		return false;
		
	}

	public static List<String> getTables(String sql) {

		try {
			CommonTree root = buildAST(sql.toLowerCase());
			List<String> tables = new ArrayList<String>();
			getTables(root, tables);
			return tables;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}

	public static void test() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(new File(
				"C:/work/shengsheng/src/hive.nistsql/q/0so.sql")));
		String line;
		int count = 1;
		while ((line = br.readLine()) != null) {
			try {
				count++;
				if(isMultiTable(line) ){
				System.out.println(count+"               " + line);}
			} catch (Exception e) {
				System.out.println("===           " + line);
			}
		}
		br.close();
	}

	/**
	 * post process q file for delete useless sentence.
	 * 
	 * @throws Exception
	 */
	public static void postProcessQ() throws Exception {

		File[] fa = new File(Base.outDir + "/oracle").listFiles();
		for (File f : fa) {
			if (f.isFile() && f.getName().contains(".q")) {
				BufferedReader br = new BufferedReader(new FileReader(f));
				FileOutputStream fos = new FileOutputStream(new File(
						Base.outDir + "/oracle/thin/" + f.getName()));
				List<String> lineList = new ArrayList<String>();
				String line;
				while ((line = br.readLine()) != null) {
					lineList.add(line);
				}
				List<String> tables = getTables(lineList
						.get(lineList.size() - 1));
				if (tables == null || tables.size() == 0) {
					System.out.println("unprocess:" + f.getName());
					for (String tmp : lineList) {
						fos.write((tmp + "\r\n").getBytes());
					}
					br.close();
					fos.close();
					continue;
				}
				if (tables.size() > 1) {
					System.out.println("mutil tables:" + f.getName());
				}
				List<String> tmpList = new ArrayList<String>();
				for (String sen : lineList) {
					if (sen.length() < 20) {
						tmpList.add(sen);
						continue;
					}
					if (sen.substring(0, 12).equals("CREATE TABLE")
							|| sen.substring(0, 9).equals("LOAD DATA")) {
						for (String table : tables) {
							if (sen.toLowerCase().contains(table)) {
								tmpList.add(sen);
								break;
							}
						}

					} else {
						tmpList.add(sen);
					}
				}
				for (String tmp : tmpList) {
					fos.write((tmp + "\r\n").getBytes());
				}
				br.close();
				fos.close();
			}

		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
//		postProcessQ();
		 test();
	}
}
