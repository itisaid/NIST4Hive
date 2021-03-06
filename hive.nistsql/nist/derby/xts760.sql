AUTOCOMMIT OFF;

-- MODULE   XTS760

-- SQL Test Suite, V6.0, Interactive SQL, xts760.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION CTS1
   set schema CTS1;

--O   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:7060 MAX of column derived from <set function specification>!

   CREATE VIEW V000V AS
  SELECT DEPTNO, AVG(SALARY) AS AVSAL
--O  FROM CTS1.TABX760 GROUP BY DEPTNO;
  FROM TABX760 GROUP BY DEPTNO;
-- PASS:7060 If view created successfully?

   COMMIT WORK;

   SELECT MAX(AVSAL) FROM V000V;
-- PASS:7060 If MAX(avsal) is 78000?

   COMMIT WORK;

--O   DROP VIEW V000V CASCADE;
   DROP VIEW V000V ;
-- PASS:7060 If view dropped successfully?

   COMMIT WORK;

-- END TEST >>> 7060 <<< END TEST
-- *********************************************
-- *************************************************////END-OF-MODULE
