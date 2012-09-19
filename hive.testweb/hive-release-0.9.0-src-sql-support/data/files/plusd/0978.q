set hive.ql.mode=hql;
drop database IF EXISTS SUN CASCADE;
create database SUN;
use SUN;
CREATE TABLE SIZ3_P6(F1 STRING,F2 STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0978/SIZ3_P6.csv' OVERWRITE INTO TABLE SIZ3_P6;
CREATE TABLE SIZ3_P5(F1 DOUBLE,F2 STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0978/SIZ3_P5.csv' OVERWRITE INTO TABLE SIZ3_P5;
CREATE TABLE SIZ3_P4(F1 STRING,F2 STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0978/SIZ3_P4.csv' OVERWRITE INTO TABLE SIZ3_P4;
CREATE TABLE SIZ3_P3(F1 DOUBLE,F2 STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0978/SIZ3_P3.csv' OVERWRITE INTO TABLE SIZ3_P3;
CREATE TABLE SIZ3_P2(F1 STRING,F2 STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0978/SIZ3_P2.csv' OVERWRITE INTO TABLE SIZ3_P2;
CREATE TABLE SIZ3_P10(F1 DOUBLE,F2 STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0978/SIZ3_P10.csv' OVERWRITE INTO TABLE SIZ3_P10;
CREATE TABLE SIZ3_P1(F1 STRING,F2 STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0978/SIZ3_P1.csv' OVERWRITE INTO TABLE SIZ3_P1;
CREATE TABLE SIZ3_F(P1 STRING,P2 STRING,P3 DOUBLE,P4 STRING,P5 DOUBLE,P6 STRING,P7 STRING,P8 DOUBLE,P9 DOUBLE,P10 DOUBLE,P11 STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0978/SIZ3_F.csv' OVERWRITE INTO TABLE SIZ3_F;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM SIZ3_F,SIZ3_P1,SIZ3_P2,SIZ3_P3,SIZ3_P4, SIZ3_P5,SIZ3_P6 WHERE P1 = SIZ3_P1.F1 AND P2 = SIZ3_P2.F1 AND P3 = SIZ3_P3.F1 AND P4 = SIZ3_P4.F1 AND P5 = SIZ3_P5.F1 AND P6 = SIZ3_P6.F1 AND SIZ3_P3.F1 BETWEEN 1 AND 2;
