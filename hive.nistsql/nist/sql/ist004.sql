-- MODULE  IST004  

-- SQL Test Suite, V6.0, Interactive SQL, ist004.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION FLATER            

   SELECT USER FROM HU.ECCO;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:0606 INFO_SCHEM.SCHEMATA definition!

   SELECT DISTINCT SCHEM_OWNER
FROM INFO_SCHEM.SCHEMATA;
-- PASS:0606 If 1 row selected and SCHEM_OWNER = 'FLATER'?

   SELECT COUNT(*)
FROM INFO_SCHEM.SCHEMATA
WHERE SCHEM_NAME IS NULL;
-- PASS:0606 If count = 0?

   SELECT COUNT(*)
FROM INFO_SCHEM.SCHEMATA;
-- PASS:0606 If count = 3?

   SELECT COUNT(*)
FROM INFO_SCHEM.SCHEMATA A,
 INFO_SCHEM.SCHEMATA B
WHERE A.SCHEM_NAME = B.SCHEM_NAME;
-- PASS:0606 If count = 3?

   SELECT COUNT(*)
FROM INFO_SCHEM.SCHEMATA
WHERE SCHEM_NAME = 'FLATER'
AND SCHEM_OWNER = 'FLATER';
-- PASS:0606 If count = 1?

   ROLLBACK WORK;

-- END TEST >>> 0606 <<< END TEST

-- *************************************************////END-OF-MODULE
