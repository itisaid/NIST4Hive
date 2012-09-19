set hive.ql.mode=hql;
drop database IF EXISTS SUN CASCADE;
create database SUN;
use SUN;
CREATE TABLE PROJ_P(PNUM STRING,PNAME STRING,PTYPE STRING,BUDGET DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0020/PROJ_P.csv' OVERWRITE INTO TABLE PROJ_P;
CREATE TABLE PROJ_M(PNUM STRING,PNAME STRING,PTYPE STRING,BUDGET DOUBLE,CITY STRING,MGR STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0020/PROJ_M.csv' OVERWRITE INTO TABLE PROJ_M;
CREATE TABLE PROJ3(PNUM STRING,PNAME STRING,PTYPE STRING,BUDGET DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0020/PROJ3.csv' OVERWRITE INTO TABLE PROJ3;
CREATE TABLE PROJ(PNUM STRING,PNAME STRING,PTYPE STRING,BUDGET DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0020/PROJ.csv' OVERWRITE INTO TABLE PROJ;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM PROJ;
