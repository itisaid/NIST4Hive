set hive.ql.mode=hql;
drop database IF EXISTS CTS1 CASCADE;
create database CTS1;
use CTS1;
CREATE TABLE TESTREPORT(TESTNO STRING,RESULT STRING,TESTTYPE STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
CREATE TABLE TT2(TTA DOUBLE,TTB INT,TTC DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0891/TT2.csv' OVERWRITE INTO TABLE TT2;
CREATE TABLE TT(TTA DOUBLE,TTB DOUBLE,TTC DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0891/TT.csv' OVERWRITE INTO TABLE TT;
set hive.ql.mode=sql;
SELECT COUNT (*) FROM CTS1.TT WHERE TTB IS NULL OR TTC IS NULL;
