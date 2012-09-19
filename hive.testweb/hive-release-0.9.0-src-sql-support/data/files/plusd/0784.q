set hive.ql.mode=hql;
drop database IF EXISTS FLATER CASCADE;
create database FLATER;
use FLATER;
CREATE TABLE NO_DUCK(GOOSE DOUBLE,ALBATROSS FLOAT,SEAGULL DOUBLE,OSPREY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0784/NO_DUCK.csv' OVERWRITE INTO TABLE NO_DUCK;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM NO_DUCK WHERE GOOSE IS NULL;
