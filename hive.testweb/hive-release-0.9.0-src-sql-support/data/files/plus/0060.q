set hive.ql.mode=hql;
drop database IF EXISTS CANWEPARSELENGTH18 CASCADE;
create database CANWEPARSELENGTH18;
use CANWEPARSELENGTH18;
CREATE TABLE CHARACTERS18VIEW18(LONGNAME18LONGNAME STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0060/CHARACTERS18VIEW18.csv' OVERWRITE INTO TABLE CHARACTERS18VIEW18;
set hive.ql.mode=sql;
SELECT * FROM CANWEPARSELENGTH18.CHARACTERS18VIEW18;
