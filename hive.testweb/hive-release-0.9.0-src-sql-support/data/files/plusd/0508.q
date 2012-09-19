set hive.ql.mode=hql;
drop database IF EXISTS CUGINI CASCADE;
create database CUGINI;
use CUGINI;
CREATE TABLE VVTABLE(COL1 DOUBLE,COL2 DOUBLE,COL3 DOUBLE,COL4 DOUBLE,COL5 DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0508/VVTABLE.csv' OVERWRITE INTO TABLE VVTABLE;
drop database IF EXISTS SCHANZLE CASCADE;
create database SCHANZLE;
use SCHANZLE;
set hive.ql.mode=sql;
SELECT COL2 FROM CUGINI.VVTABLE WHERE COL1 = 0;
