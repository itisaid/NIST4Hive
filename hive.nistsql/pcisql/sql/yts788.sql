-- MODULE  YTS788  

-- SQL Test Suite, V6.0, Interactive SQL, yts788.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION CTS1              

   SELECT USER FROM HU.ECCO;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:7522 CREATE CHARACTER SET, implicit default collation!

   SELECT COUNT (*) 
     FROM INFORMATION_SCHEMA.CHARACTER_SETS
     WHERE CHARACTER_SET_SCHEMA = 'CTS1'
     AND CHARACTER_SET_NAME = 'TEST_SET';
-- PASS:7522 If COUNT = 0?

   COMMIT WORK;

   CREATE CHARACTER SET test_set GET SQL_TEXT;
-- PASS:7522 If create completed successfully?

   COMMIT WORK;

   SELECT COUNT (*) 
     FROM INFORMATION_SCHEMA.CHARACTER_SETS
     WHERE CHARACTER_SET_SCHEMA = 'CTS1'
     AND CHARACTER_SET_NAME = 'TEST_SET';
-- PASS:7522 If COUNT = 1?

   SELECT FORM_OF_USE, NUMBER_OF_CHARACTERS,
     DEFAULT_COLLATE_CATALOG, DEFAULT_COLLATE_SCHEMA,
     DEFAULT_COLLATE_NAME 
     FROM INFORMATION_SCHEMA.CHARACTER_SETS
     WHERE CHARACTER_SET_SCHEMA = 'CTS1'
     AND CHARACTER_SET_NAME = 'TEST_SET';
-- PASS:7522 If selected values = the values of the next SELECT?

   SELECT FORM_OF_USE, NUMBER_OF_CHARACTERS,
     DEFAULT_COLLATE_CATALOG, DEFAULT_COLLATE_SCHEMA,
     DEFAULT_COLLATE_NAME 
     FROM INFORMATION_SCHEMA.CHARACTER_SETS
     WHERE CHARACTER_SET_SCHEMA = 'INFORMATION_SCHEMA'
     AND CHARACTER_SET_NAME = 'SQL_TEXT';
-- PASS:7522 If selected values = the values of the previous SELECT?

   ROLLBACK WORK;

   DROP CHARACTER SET test_set;
-- PASS:7522 If drop completed successfully?

   COMMIT WORK;

-- END TEST >>> 7522 <<< END TEST
-- *********************************************
-- *************************************************////END-OF-MODULE
