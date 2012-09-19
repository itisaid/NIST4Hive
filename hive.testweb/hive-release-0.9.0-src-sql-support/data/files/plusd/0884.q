set hive.ql.mode=hql;
drop database IF EXISTS CTS1 CASCADE;
create database CTS1;
use CTS1;
CREATE TABLE TV(A DOUBLE,B STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0884/TV.csv' OVERWRITE INTO TABLE TV;
set hive.ql.mode=sql;
SELECT B FROM CTS1.TV WHERE A = 3;
