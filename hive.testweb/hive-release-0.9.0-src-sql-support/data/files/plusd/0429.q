set hive.ql.mode=hql;
drop database IF EXISTS SCHANZLE CASCADE;
create database SCHANZLE;
use SCHANZLE;
CREATE TABLE RET_CATALOG(VENDOR_ID DOUBLE,PRODUCT_ID DOUBLE,WHOLESALE DOUBLE,RETAIL DOUBLE,MARKUP DOUBLE,EXPORT_CODE STRING,EXPORT_LICNSE_DATE STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0429/RET_CATALOG.csv' OVERWRITE INTO TABLE RET_CATALOG;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM RET_CATALOG;
