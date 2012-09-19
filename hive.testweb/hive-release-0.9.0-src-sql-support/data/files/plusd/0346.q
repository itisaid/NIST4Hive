set hive.ql.mode=hql;
drop database IF EXISTS HU CASCADE;
create database HU;
use HU;
CREATE TABLE V_WORKS3(EMPNUM STRING,PNUM STRING,HOURS DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0346/V_WORKS3.csv' OVERWRITE INTO TABLE V_WORKS3;
CREATE TABLE V_WORKS2(EMPNUM STRING,PNUM STRING,HOURS DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0346/V_WORKS2.csv' OVERWRITE INTO TABLE V_WORKS2;
CREATE TABLE V_WORKS1(EMPNUM STRING,PNUM STRING,HOURS DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0346/V_WORKS1.csv' OVERWRITE INTO TABLE V_WORKS1;
CREATE TABLE STAFF_WORKS_DESIGN(NAME STRING,COST DOUBLE,PROJECT STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0346/STAFF_WORKS_DESIGN.csv' OVERWRITE INTO TABLE STAFF_WORKS_DESIGN;
CREATE TABLE WORKS1(EMPNUM STRING,PNUM STRING,HOURS DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0346/WORKS1.csv' OVERWRITE INTO TABLE WORKS1;
CREATE TABLE WORKS(EMPNUM STRING,PNUM STRING,HOURS DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0346/WORKS.csv' OVERWRITE INTO TABLE WORKS;
CREATE TABLE PROJ1(PNUM STRING,PNAME STRING,PTYPE STRING,BUDGET DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0346/PROJ1.csv' OVERWRITE INTO TABLE PROJ1;
CREATE TABLE PROJ(PNUM STRING,PNAME STRING,PTYPE STRING,BUDGET DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0346/PROJ.csv' OVERWRITE INTO TABLE PROJ;
set hive.ql.mode=sql;
SELECT PNUM, SUM(HOURS) FROM WORKS GROUP BY PNUM HAVING EXISTS (SELECT PNAME FROM PROJ WHERE PROJ.PNUM = WORKS.PNUM AND SUM(WORKS.HOURS) > PROJ.BUDGET / 200);
