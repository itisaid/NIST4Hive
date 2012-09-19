-- MODULE DML005

-- SQL Test Suite, V6.0, Interactive SQL, dml005.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION HU
   set schema HU;

--O   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment

-- date_time print

-- TEST:0011 FIPS sizing - DECIMAL (15)!
-- FIPS sizing TEST

-- setup
--O     DELETE FROM LONGINT;
     DELETE FROM LONGINTTAB;

-- setup
--O     INSERT INTO LONGINT
     INSERT INTO LONGINTTAB
            VALUES(123456789012345.);
-- PASS:0011 If 1 row is inserted?

     SELECT LONG_INT, LONG_INT /1000000, LONG_INT - 123456789000000.
--O          FROM LONGINT;
          FROM LONGINTTAB;

-- PASS:0011 If values are (123456789012345, 123456789, 12345), but?
-- PASS:0011 Second value may be between 123456788 and 123456790?

-- END TEST >>> 0011 <<< END TEST
-- *************************************************////END-OF-MODULE
