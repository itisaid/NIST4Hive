set hive.ql.mode=hql;
drop database IF EXISTS SUN CASCADE;
create database SUN;
use SUN;
CREATE TABLE STAFF6_WITH_GRADES(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0021/STAFF6_WITH_GRADES.csv' OVERWRITE INTO TABLE STAFF6_WITH_GRADES;
CREATE TABLE STAFF_P(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0021/STAFF_P.csv' OVERWRITE INTO TABLE STAFF_P;
CREATE TABLE STAFF_M(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING,PRI_WK STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0021/STAFF_M.csv' OVERWRITE INTO TABLE STAFF_M;
CREATE TABLE STAFF_C(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING,MGR STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0021/STAFF_C.csv' OVERWRITE INTO TABLE STAFF_C;
CREATE TABLE STAFF9(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0021/STAFF9.csv' OVERWRITE INTO TABLE STAFF9;
CREATE TABLE STAFF8(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0021/STAFF8.csv' OVERWRITE INTO TABLE STAFF8;
CREATE TABLE STAFF7(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0021/STAFF7.csv' OVERWRITE INTO TABLE STAFF7;
CREATE TABLE STAFF6(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0021/STAFF6.csv' OVERWRITE INTO TABLE STAFF6;
CREATE TABLE STAFF5(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0021/STAFF5.csv' OVERWRITE INTO TABLE STAFF5;
CREATE TABLE STAFF4(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0021/STAFF4.csv' OVERWRITE INTO TABLE STAFF4;
CREATE TABLE STAFF3(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0021/STAFF3.csv' OVERWRITE INTO TABLE STAFF3;
CREATE TABLE STAFF16(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0021/STAFF16.csv' OVERWRITE INTO TABLE STAFF16;
CREATE TABLE STAFF15(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0021/STAFF15.csv' OVERWRITE INTO TABLE STAFF15;
CREATE TABLE STAFF14(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0021/STAFF14.csv' OVERWRITE INTO TABLE STAFF14;
CREATE TABLE STAFF13(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0021/STAFF13.csv' OVERWRITE INTO TABLE STAFF13;
CREATE TABLE STAFF12(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0021/STAFF12.csv' OVERWRITE INTO TABLE STAFF12;
CREATE TABLE STAFF11(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0021/STAFF11.csv' OVERWRITE INTO TABLE STAFF11;
CREATE TABLE STAFF10(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0021/STAFF10.csv' OVERWRITE INTO TABLE STAFF10;
CREATE TABLE STAFF(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0021/STAFF.csv' OVERWRITE INTO TABLE STAFF;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM STAFF;
