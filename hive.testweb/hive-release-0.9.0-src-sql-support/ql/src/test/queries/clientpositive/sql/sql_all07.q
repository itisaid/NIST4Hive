set hive.ql.mode=hql;
create table t1 (a STRING,b STRING,c INT,d INT,e INT) row format delimited fields terminated by ',' stored as textfile;
load data local inpath '../data/files/sql/sql_t1.txt' into table t1;
create table t3 (x STRING,y STRING,z INT) row format delimited fields terminated by ',' stored as textfile;
load data local inpath '../data/files/sql/sql_t3.txt' into table t3;
set hive.ql.mode=sql;
select a,b,c,d,e from t1 where d != all (select z from t3 where y = 'world1');
