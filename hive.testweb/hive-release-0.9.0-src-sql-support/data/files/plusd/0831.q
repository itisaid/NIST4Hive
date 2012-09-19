set hive.ql.mode=hql;
drop database IF EXISTS CTS1 CASCADE;
create database CTS1;
use CTS1;
CREATE TABLE TEST6840C(NUM_C1 DOUBLE,CH_C1 STRING,NUM_C2 DOUBLE,CH_C2 STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0831/TEST6840C.csv' OVERWRITE INTO TABLE TEST6840C;
set hive.ql.mode=sql;
SELECT NUM_C1,CH_C1,NUM_C2,CH_C2 FROM TEST6840C WHERE NUM_C1 = 1004;
