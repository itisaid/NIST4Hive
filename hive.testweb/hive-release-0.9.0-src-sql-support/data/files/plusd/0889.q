set hive.ql.mode=hql;
drop database IF EXISTS CTS1 CASCADE;
create database CTS1;
use CTS1;
CREATE TABLE TESTREPORT(TESTNO STRING,RESULT STRING,TESTTYPE STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
CREATE TABLE TU(TUD STRING,TUE DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0889/TU.csv' OVERWRITE INTO TABLE TU;
CREATE TABLE TT2(TTA DOUBLE,TTB INT,TTC DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0889/TT2.csv' OVERWRITE INTO TABLE TT2;
CREATE TABLE TT(TTA DOUBLE,TTB DOUBLE,TTC DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0889/TT.csv' OVERWRITE INTO TABLE TT;
CREATE TABLE PROJ_STATUS(MGR STRING,P_REF STRING,ONTIME STRING,BUDGET DOUBLE,COST DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0889/PROJ_STATUS.csv' OVERWRITE INTO TABLE PROJ_STATUS;
set hive.ql.mode=sql;
SELECT TTA, TTB, TTC FROM CTS1.TT WHERE (SELECT TUD FROM TU WHERE TU.TUE = TT.TTA) IS NULL ORDER BY TTA DESC;
