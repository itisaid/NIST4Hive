set hive.ql.mode=hql;
create table t1 (a STRING,b STRING,c INT,d INT,e INT) row format delimited fields terminated by ',' stored as textfile; 
load data local inpath '../data/files/sql/sql_t1.txt' into table t1;
create table t2 (x STRING,y STRING) row format delimited fields terminated by ',' stored as textfile;
load data local inpath '../data/files/sql/sql_t2.txt' into table t2;
create table t3 (x STRING,y STRING,z INT) row format delimited fields terminated by ',' stored as textfile;
load data local inpath '../data/files/sql/sql_t3.txt' into table t3;
set hive.ql.mode=sql;
select a,b from t1 union all select x,y from t2 union all select x,y from t3 order by 1,2;
