set hive.ql.mode=hql;
drop database IF EXISTS CTS1 CASCADE;
create database CTS1;
use CTS1;
CREATE TABLE TESTA6439(COLUMNOFCHARACTERSA STRING,COLUMNOFCHARACTERSB STRING,COLUMNOFNUMERICSS_0 DOUBLE,COLUMNOFNUMERICSS_1 DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0834/TESTA6439.csv' OVERWRITE INTO TABLE TESTA6439;
set hive.ql.mode=sql;
SELECT COLUMNOFCHARACTERSA, columnofcharactersb, cOlUmNoFNUMERICss_0, cOlUmNoFNUMERICss_1 FROM CTS1.TESTA6439;
