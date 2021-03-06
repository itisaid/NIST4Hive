-- MODULE   XTS745

-- SQL Test Suite, V6.0, Interactive SQL, xts745.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION CTS1              

   SELECT USER FROM HU.ECCO;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

   ROLLBACK WORK;

-- TEST:7045 Presence of ASCII_GRAPHIC in CHARACTER_SETS view!

  SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.CHARACTER_SETS
     WHERE CHARACTER_SET_SCHEMA = 'INFORMATION_SCHEMA'
     AND CHARACTER_SET_NAME = 'ASCII_GRAPHIC'
     AND NUMBER_OF_CHARACTERS = 95
     AND CHARACTER_SET_CATALOG IS NOT NULL
     AND DEFAULT_COLLATE_CATALOG IS NOT NULL
     AND DEFAULT_COLLATE_SCHEMA IS NOT NULL
     AND DEFAULT_COLLATE_NAME IS NOT NULL;
-- PASS:7045 If COUNT = 1?

   ROLLBACK WORK;

-- END TEST >>> 7045 <<< END TEST
-- *********************************************
-- *************************************************////END-OF-MODULE
