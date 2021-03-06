-- MODULE  ISI004  

-- SQL Test Suite, V6.0, Interactive SQL, isi004.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION FLATER            

   SELECT USER FROM HU.ECCO;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:0571 INFORMATION_SCHEMA.SCHEMATA definition!

   SELECT DISTINCT SCHEMA_OWNER
     FROM INFORMATION_SCHEMA.SCHEMATA;
-- PASS:0571 If SCHEMA_OWNER = 'FLATER'?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.SCHEMATA
     WHERE SCHEMA_NAME IS NULL;
-- PASS:0571 If COUNT = 0?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.SCHEMATA;
-- PASS:0571 If this COUNT = next COUNT?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.SCHEMATA A,
          INFORMATION_SCHEMA.SCHEMATA B
     WHERE A.SCHEMA_NAME = B.SCHEMA_NAME;
-- PASS:0571 If this COUNT = previous COUNT?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.SCHEMATA
     WHERE SCHEMA_NAME = 'FLATER'
       AND SCHEMA_OWNER = 'FLATER';
-- PASS:0571 If COUNT = 1?

   ROLLBACK WORK;

-- END TEST >>> 0571 <<< END TEST
-- *********************************************
-- *************************************************////END-OF-MODULE
