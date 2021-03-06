-- MODULE  IST001  

-- SQL Test Suite, V6.0, Interactive SQL, ist001.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION FLATER

   SELECT USER FROM HU.ECCO;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:0603 INFO_SCHEM.TABLES definition!

-- Verify CONSTRAINT TABLE_TYPE_NOT_NULL
   SELECT COUNT(*) FROM INFO_SCHEM.TABLES
   WHERE TABLE_TYPE IS NULL;
-- PASS:0603 If count = 0?

-- Verify CONSTRAINT CHECK_TABLE_IN_COLUMNS
   SELECT COUNT(*) FROM INFO_SCHEM.TABLES A
   WHERE NOT EXISTS (
     SELECT * FROM INFO_SCHEM.COLUMNS B
     WHERE A.TABLE_SCHEM = B.TABLE_SCHEM
     AND A.TABLE_NAME = B.TABLE_NAME);
-- PASS:0603 If count = 0?

-- Verify CONSTRAINT TABLES_PRIMARY_KEY
   SELECT COUNT(*) FROM INFO_SCHEM.TABLES
   WHERE TABLE_SCHEM IS NULL
   OR TABLE_NAME IS NULL;
-- PASS:0603 If count = 0?

   SELECT COUNT(*) FROM INFO_SCHEM.TABLES;
-- PASS:0603 If count is same as that of next statement?

   SELECT COUNT(*) FROM INFO_SCHEM.TABLES A, INFO_SCHEM.TABLES B
   WHERE A.TABLE_SCHEM = B.TABLE_SCHEM
   AND A.TABLE_NAME = B.TABLE_NAME;
-- PASS:0603 If count is same as that of previous statement?

-- Verify CONSTRAINT TABLES_FOREIGN_KEY_SCHEMATA
   SELECT COUNT(*) FROM INFO_SCHEM.TABLES A
   WHERE NOT EXISTS (
     SELECT * FROM INFO_SCHEM.SCHEMATA B
     WHERE A.TABLE_SCHEM = B.SCHEM_NAME);
-- PASS:0603 If count = 0?

-- Verify CONSTRAINT TABLES_CHECK_NOT_VIEW
   SELECT COUNT(*) FROM INFO_SCHEM.TABLES A
   WHERE TABLE_TYPE = 'VIEW' AND NOT EXISTS
   (SELECT * FROM INFO_SCHEM.VIEWS B WHERE A.TABLE_SCHEM =
     B.TABLE_SCHEM AND A.TABLE_NAME = B.TABLE_NAME);
-- PASS:0603 If count = 0?

-- Verify that all tables in TABLES appear in TABLE_PRIVILEGES
-- or COLUMN_PRIVILEGES with FLATER or PUBLIC as GRANTEE, i.e.
-- check the view definition.
   SELECT COUNT(*) FROM INFO_SCHEM.TABLES Z
   WHERE NOT EXISTS (SELECT TABLE_SCHEM, TABLE_NAME
     FROM INFO_SCHEM.TABLE_PRIVILEGES X
     WHERE GRANTEE IN ('PUBLIC', USER)
     AND X.TABLE_SCHEM = Z.TABLE_SCHEM
     AND X.TABLE_NAME = Z.TABLE_NAME
       UNION
     SELECT TABLE_SCHEM, TABLE_NAME
     FROM INFO_SCHEM.COLUMN_PRIVILEGES Y
     WHERE GRANTEE IN ('PUBLIC', USER)
     AND Y.TABLE_SCHEM = Z.TABLE_SCHEM
     AND Y.TABLE_NAME = Z.TABLE_NAME);
-- PASS:0603 If count = 0?

-- Verify the metadata on a few boring tables; no fancy features.
   SELECT TABLE_TYPE FROM INFO_SCHEM.TABLES
   WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'USIG';
-- PASS:0603 If 1 row selected and TABLE_TYPE = BASE TABLE?

   SELECT TABLE_TYPE FROM INFO_SCHEM.TABLES
   WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'U_SIG';
-- PASS:0603 If 1 row selected and TABLE_TYPE = BASE TABLE?

   SELECT TABLE_TYPE FROM INFO_SCHEM.TABLES
   WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'DV1';
-- PASS:0603 If 1 row selected and TABLE_TYPE = VIEW?

   SELECT TABLE_TYPE FROM INFO_SCHEM.TABLES
   WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'BASE_WCOV';
-- PASS:0603 If 1 row selected and TABLE_TYPE = BASE TABLE?

   SELECT TABLE_TYPE FROM INFO_SCHEM.TABLES
   WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'WCOV';
-- PASS:0603 If 1 row selected and TABLE_TYPE = VIEW?

   SELECT TABLE_TYPE FROM INFO_SCHEM.TABLES
   WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'BASE_VS1';
-- PASS:0603 If 1 row selected and TABLE_TYPE = BASE TABLE?

   SELECT TABLE_TYPE FROM INFO_SCHEM.TABLES
   WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'VS1';
-- PASS:0603 If 1 row selected and TABLE_TYPE = VIEW?

   SELECT TABLE_TYPE FROM INFO_SCHEM.TABLES
   WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'VS2';
-- PASS:0603 If 1 row selected and TABLE_TYPE = VIEW?

   SELECT TABLE_TYPE FROM INFO_SCHEM.TABLES
   WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'VS3';
-- PASS:0603 If 1 row selected and TABLE_TYPE = VIEW?

   SELECT TABLE_TYPE FROM INFO_SCHEM.TABLES
   WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'VS4';
-- PASS:0603 If 1 row selected and TABLE_TYPE = VIEW?

   SELECT TABLE_TYPE FROM INFO_SCHEM.TABLES
   WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'VS5';
-- PASS:0603 If 1 row selected and TABLE_TYPE = VIEW?

   SELECT TABLE_TYPE FROM INFO_SCHEM.TABLES
   WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'VS6';
-- PASS:0603 If 1 row selected and TABLE_TYPE = VIEW?

   ROLLBACK WORK;

-- END TEST >>> 0603 <<< END TEST
-- *************************************************////END-OF-MODULE
