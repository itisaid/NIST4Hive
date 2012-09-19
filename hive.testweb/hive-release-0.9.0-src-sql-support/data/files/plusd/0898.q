set hive.ql.mode=hql;
drop database IF EXISTS CTS1 CASCADE;
create database CTS1;
use CTS1;
CREATE TABLE V_DATA_TYPE(VT1 DOUBLE,VT2 DOUBLE,VT3 DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0898/V_DATA_TYPE.csv' OVERWRITE INTO TABLE V_DATA_TYPE;
set hive.ql.mode=sql;
SELECT VT1, VT2, VT3 FROM V_DATA_TYPE WHERE VT2 = 1;
