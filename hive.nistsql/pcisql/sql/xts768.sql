-- MODULE   XTS768

-- SQL Test Suite, V6.0, Interactive SQL, xts768.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION CTS1              

   SELECT USER FROM HU.ECCO;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:7068 Presence of SQL_TEXT in CHARACTER_SETS view!

   SELECT COUNT(CHARACTER_SET_NAME) 
         FROM INFORMATION_SCHEMA.CHARACTER_SETS
         WHERE CHARACTER_SET_SCHEMA = 'INFORMATION_SCHEMA'
         AND CHARACTER_SET_NAME = 'SQL_TEXT';
-- PASS:7068 If COUNT = 1?

   SELECT DEFAULT_COLLATE_SCHEMA,DEFAULT_COLLATE_NAME,
         CHARACTER_SET_CATALOG,DEFAULT_COLLATE_CATALOG,
         NUMBER_OF_CHARACTERS
         FROM INFORMATION_SCHEMA.CHARACTER_SETS
         WHERE CHARACTER_SET_SCHEMA = 'INFORMATION_SCHEMA'
         AND CHARACTER_SET_NAME = 'SQL_TEXT';
-- PASS:7068 If DEFAULT_COLLATE_SCHEMA = INFORMATION_SCHEMA?
-- PASS:7068 If DEFAULT_COLLATE_NAME = SQL_TEXT
-- PASS:7068 If CHARACTER_SET_CATALOG is not NULL?
-- PASS:7068 If DEFAULT_COLLATE_CATALOG is not NULL?
-- PASS:7068 If NUMBER_OF_CHARACTERS >= 83?

   ROLLBACK WORK;

-- END TEST >>> 7068 <<< END TEST
-- *********************************************
-- *************************************************////END-OF-MODULE
