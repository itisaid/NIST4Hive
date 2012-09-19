set hive.exec.dynamic.partition=true;
set hive.exec.max.dynamic.partitions.pernode=200;
set hive.input.format=org.apache.hadoop.hive.ql.io.CombineHiveInputFormat;
set hive.default.fileformat=RCFILE;
DROP TABLE IF EXISTS escape2;
DROP TABLE IF EXISTS escape_raw;

CREATE TABLE escape_raw (s STRING) STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '../data/files/escapetest.txt' INTO TABLE  escape_raw;

SELECT count(*) from escape_raw;
SELECT * from escape_raw;

CREATE TABLE escape2(a STRING) PARTITIONED BY (ds STRING, part STRING);
INSERT OVERWRITE TABLE escape2 PARTITION (ds='1', part) SELECT '1', s from 
escape_raw;

SELECT count(*) from escape2;
SELECT * from escape2;
SHOW PARTITIONS escape2;

-- ASCII values 1, 10, 13, 59, 92, 127 were not included in the below commands

-- ASCII 2
ALTER TABLE escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 3
ALTER TABLE escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 4
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 5
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 6
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 7
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 8
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 9
ALTER table escape2 PARTITION (ds='1', part='	') CONCATENATE;
-- ASCII 11
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 12
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 14
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 15
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 16
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 17
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 18
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 19
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 20
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 21
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 22
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 23
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 24
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 25
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 26
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 27
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 28
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 29
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 30
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
-- ASCII 31
ALTER table escape2 PARTITION (ds='1', part='') CONCATENATE;
ALTER table escape2 PARTITION (ds='1', part=' ') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='!') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='"') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='#') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='$') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='%') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='&') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part="'") CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='(') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part=')') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='*') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='+') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part=',') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='-') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='.') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='/') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='0') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='1') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='2') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='3') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='4') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='5') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='6') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='7') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='8') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='9') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part=':') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='<') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='=') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='>') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='?') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='@') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='A') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='B') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='C') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='D') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='E') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='F') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='G') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='H') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='I') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='J') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='K') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='L') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='M') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='N') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='O') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='P') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='Q') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='R') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='S') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='T') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='U') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='V') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='W') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='X') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='Y') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='Z') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='[') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part=']') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='_') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='`') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='a') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='b') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='c') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='d') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='e') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='f') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='g') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='h') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='i') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='j') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='k') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='l') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='m') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='n') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='o') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='p') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='q') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='r') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='s') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='t') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='u') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='v') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='w') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='x') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='y') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='z') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='{') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='|') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='}') CONCATENATE;
ALTER TABLE escape2 PARTITION (ds='1', part='~') CONCATENATE;

DROP TABLE escape2;
DROP TABLE escape_raw;
