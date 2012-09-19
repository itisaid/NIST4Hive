AUTOCOMMIT OFF;

-- MODULE  DML162  

-- SQL Test Suite, V6.0, Interactive SQL, dml162.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION FLATER
   set schema FLATER;

--0   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:0863 <joined table> directly contained in cursor,view!

   CREATE VIEW BLIVET (CITY, PNUM, EMPNUM, EMPNAME, GRADE,
      HOURS, PNAME, PTYPE, BUDGET) AS
--0      HU.STAFF NATURAL JOIN HU.WORKS NATURAL JOIN HU.PROJ;
	  SELECT HU.PROJ.CITY, HU.PROJ.PNUM, HU.STAFF.EMPNUM, EMPNAME, GRADE, HOURS, PNAME, PTYPE, BUDGET
      FROM HU.STAFF JOIN HU.WORKS ON (HU.STAFF.EMPNUM=HU.WORKS.EMPNUM) JOIN HU.PROJ ON (HU.PROJ.PNUM=HU.WORKS.PNUM AND HU.PROJ.CITY=HU.STAFF.CITY)
	  ;
-- PASS:0863 If view created successfully?

   COMMIT WORK;

   SELECT COUNT(*) 
     FROM BLIVET WHERE EMPNUM = 'E1';
-- PASS:0863 If COUNT = 3?

   SELECT COUNT(*) 
     FROM BLIVET WHERE EMPNUM <> 'E1';
-- PASS:0863 If COUNT = 3?

   SELECT * FROM HU.STAFF LEFT OUTER JOIN HU.WORKS
      ON (HU.STAFF.EMPNUM=HU.WORKS.EMPNUM);
-- PASS:0863 If 13 rows are returned?

   COMMIT WORK;

--0   DROP VIEW BLIVET CASCADE;
   DROP VIEW BLIVET ;

   COMMIT WORK;

-- END TEST >>> 0863 <<< END TEST
-- *********************************************
-- *************************************************////END-OF-MODULE
