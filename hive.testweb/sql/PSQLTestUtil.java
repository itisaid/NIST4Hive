package org.apache.hadoop.hive.sql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class PSQLTestUtil extends QTestUtil {

  public PSQLTestUtil(String outDir, String logDir) throws Exception {
    super(outDir, logDir);
  }

  public PSQLTestUtil(String outDir, String logDir, boolean miniMr, String hadoopVer)
      throws Exception {
    super(outDir, logDir, miniMr, hadoopVer);
  }

  /**
   * replace template words in q file
   */
  @Override
  public void preProcedureFile() throws RuntimeException {
    String dataPath = super.getTestFiles();
    File[] qFiles = new File(dataPath + "/plus/").listFiles();
    for (File qf : qFiles) {
      if (qf.isDirectory()) {
        continue;
      }

      BufferedReader br;
      try {
        br = new BufferedReader(new FileReader(qf));
        FileOutputStream nb = new FileOutputStream(new File(dataPath + "/plus/p" + qf.getName()));
        String line;
        while ((line = br.readLine()) != null) {
          line = line.replaceAll("_datapath_", dataPath);
          nb.write((line + "\n").getBytes());
        }
        nb.close();
        br.close();
        qf.delete();
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }

    qFiles = new File(dataPath + "/plus/").listFiles();
    for (File qf : qFiles) {
      if (qf.isDirectory()) {
        continue;
      }
      qf.renameTo(new File(dataPath + "/plus/" + qf.getName().substring(1)));
    }
  }


  /**
   * check test output by comparing with database output
   *
   * @param tname
   * @return
   * @throws Exception
   */
  public int chechResultbyDB(String tname) throws Exception {
    BufferedReader br = new BufferedReader(new FileReader(new File(outDir, tname + ".out")));
    String hiveResult = "";
    String line;
    List<String> hiveList = new ArrayList<String>();
    List<String> dbList = new ArrayList<String>();
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
          if (ar.length == 2 && ar[1].equals("0")) {
            r = ar[0];
          }
        } catch (Exception e) {

        }
        tmp += r;
        tmp += " | ";
      }

      hiveResult += tmp;
      hiveList.add(tmp);
      hiveResult += "\r\n";
    }

    BufferedReader tr = new BufferedReader(new FileReader(new File(super.getTestFiles() + "/plusd/"
        + tname.replace("q", "t"))));
    String dbResult = "";
    String l;
    while ((l = tr.readLine()) != null) {
      dbResult += l;
      dbList.add(l);
      dbResult += "\r\n";
    }
    tr.close();
    br.close();
    if (!dbResult.equals(hiveResult) && !check4Number(hiveList, dbList)) {
      FileOutputStream nf = new FileOutputStream(new File(super.getTestFiles() + "/plusd/"
          + tname.replace("q", "d")));
      nf.write("Expect:\r\n".getBytes());
      nf.write(dbResult.getBytes());
      nf.write("Fact:\r\n".getBytes());
      nf.write(hiveResult.getBytes());
      nf.close();
      return 9999;
    }
    return 0;
  }

  private boolean check4Number(List<String> hiveList, List<String> dbList) {

    double delt = 0.01f;
    if (hiveList.size() != dbList.size()) {
      return false;
    }
    for (int i = 0; i < hiveList.size(); i++) {
      String[] hs = hiveList.get(i).split("\\|");
      String[] ds = dbList.get(i).split("\\|");
      if (hs.length != ds.length) {
        return false;
      }

      for (int j = 0; j < hs.length; j++) {
        String hss = hs[j].trim();
        String dss = ds[j].trim();

        // adapter db's "NULL" to ""
        if (dss.toUpperCase().equals("NULL")) {
          dss = "";
        }
        if (hss.toUpperCase().equals("NULL")) {
          hss = "";
        }
        if(hss.getBytes()!=null&&hss.getBytes().length==1&&hss.getBytes()[0]=='\0'){
          hss="";
        }

        if (hss.equals(dss)) {
          continue;
        }

        try {
          Double hf = Double.valueOf(hss);
          Double df = Double.valueOf(dss);
          if (Math.abs(hf - df) < delt) {
            continue;
          } else {
            return false;
          }
        } catch (Exception e) {
          return false;
        }
      }
    }
    return true;

  }


  /**
   * just for main() test
   *
   * @throws RuntimeException
   */
  public static void preProcedureFiles() throws RuntimeException {
    // String dataPath = super.getTestFiles();
    String dataPath = "/home/frank/work/hive-0.9.0-plsql-support/src/data/files";
    File[] qFiles = new File(dataPath + "/plus/").listFiles();
    for (File qf : qFiles) {
      BufferedReader br;
      try {
        br = new BufferedReader(new FileReader(qf));
        FileOutputStream nb = new FileOutputStream(new File(dataPath + "/plus/p" + qf.getName()));
        String line;
        while ((line = br.readLine()) != null) {
          line = line.replaceAll("_datapath_", dataPath);
          nb.write((line + "\n").getBytes());
        }
        nb.close();
        br.close();
        qf.delete();
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }


    }
  }

  public static void main(String[] args) throws Exception {
    // for (String[] s : scan()) {
    // System.out.println(s[1]);
    // }

    preProcedureFiles();

  }
}
