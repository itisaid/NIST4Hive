set hive.ql.mode=hql;
create table t1 (a STRING,b STRING,c INT,d INT,e INT) row format delimited fields terminated by ',' stored as textfile;
load data local inpath '../data/files/sql/sql_t1.txt' into table t1;
set hive.ql.mode=sql;
select a,b,c,d from t1 where c < SOME (select d from t1);
select a,b,c,d from t1 where c < ANY (select d from t1);
