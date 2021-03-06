-- MODULE  DML164  

-- SQL Test Suite, V6.0, Interactive SQL, dml164.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION FLATER            

   SELECT USER FROM HU.ECCO;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:0867 LIKE enhancements:  keyword search!

   SELECT COUNT(*) 
     FROM TIDES.LOCATIONS
     WHERE LOC_NAME LIKE '% ' || 'Key' || '%'
     OR LOC_NAME LIKE  '%' || 'Key' || ' %'
     OR LOC_NAME = 'Key';
-- PASS:0867 If COUNT = 13?

   SELECT LOC_ID 
     FROM TIDES.LOCATIONS
     WHERE LOC_NAME LIKE '% ' || 'Anchorage' || '%'
     OR LOC_NAME LIKE  '%' || 'Anchorage' || ' %'
     OR LOC_NAME = 'Anchorage';
-- PASS:0867 If LOC_ID= 10?

   SELECT LOC_ID
     FROM TIDES.LOCATIONS
     WHERE (LOC_NAME LIKE '% ' || 'London' || '%'
     OR LOC_NAME LIKE  '%' || 'London' || ' %'
     OR LOC_NAME = 'London')
     AND (LOC_NAME LIKE '% ' || 'England' || '%'
     OR LOC_NAME LIKE  '%' || 'England' || ' %'
     OR LOC_NAME = 'England');
-- PASS:0867 If LOC_ID = 202?

   COMMIT WORK;

-- END TEST >>> 0867 <<< END TEST
-- *********************************************

-- TEST:0868 More <unique predicate>!

   SELECT COUNT(*) 
     FROM TIDES.LOCATIONS A
     WHERE NOT UNIQUE (
     SELECT * FROM TIDES.LOCATIONS
       WHERE DEG_LATITUDE = A.DEG_LATITUDE
       AND DEG_LONGITUDE = A.DEG_LONGITUDE);
-- PASS:0868 If COUNT = 12?

   SELECT COUNT(*) 
     FROM HU.WORKS A WHERE UNIQUE
     (SELECT EMPNUM FROM HU.STAFF B
      WHERE A.EMPNUM = B.EMPNUM
      AND GRADE = 13
      AND CITY = 'Deale');
-- PASS:0868 If COUNT = 12?

   COMMIT WORK;

-- END TEST >>> 0868 <<< END TEST
-- *********************************************

-- TEST:0869 More table operations!

   CREATE TABLE BILLS_SENT (
     BILLID INT PRIMARY KEY,
     BILLAMOUNT DECIMAL(7,2));
-- PASS:0869 If table created successfully?

   COMMIT WORK;

   CREATE TABLE PAYMENTS (
     BILLID INT,
     PAYID INT PRIMARY KEY,
     PAYAMOUNT DECIMAL(7,2));
-- PASS:0869 If table created successfully?

   COMMIT WORK;

   CREATE VIEW NO_PAY AS
     SELECT * FROM BILLS_SENT EXCEPT CORRESPONDING 
     SELECT * FROM PAYMENTS;
-- PASS:0869 If view created successfully?

   COMMIT WORK;

   INSERT INTO BILLS_SENT VALUES (
     0, 100.00);
   INSERT INTO BILLS_SENT VALUES (
     1, 80.00);
   INSERT INTO BILLS_SENT VALUES (
     2, 50.00);
   INSERT INTO PAYMENTS VALUES (
     0, 0, 100.00);
   INSERT INTO PAYMENTS VALUES (
     1, 1, 40.00);
   INSERT INTO PAYMENTS VALUES (
     1, 2, 40.00);
-- PASS:0869 If 6 rows inserted successfully in 6 previous inserts?

   COMMIT WORK;

   SELECT BILLID 
     FROM NO_PAY;
-- PASS:0869 If BILLID = 2?

   COMMIT WORK;

   CREATE TABLE CORRECT_OUTPUT (
     PORTVAL INT);
-- PASS:0869 If table created successfully?

   COMMIT WORK;

   CREATE TABLE LOSSY_OUTPUT (
     PORTVAL INT);
-- PASS:0869 If table table created successfully?

   COMMIT WORK;

   INSERT INTO LOSSY_OUTPUT VALUES (1);
   INSERT INTO LOSSY_OUTPUT VALUES (2);
   INSERT INTO LOSSY_OUTPUT VALUES (1);
   INSERT INTO LOSSY_OUTPUT VALUES (1);
   INSERT INTO LOSSY_OUTPUT VALUES (3);
   INSERT INTO LOSSY_OUTPUT VALUES (1);
   INSERT INTO LOSSY_OUTPUT VALUES (4);
   INSERT INTO LOSSY_OUTPUT VALUES (1);
   INSERT INTO LOSSY_OUTPUT VALUES (3);
   INSERT INTO LOSSY_OUTPUT VALUES (2);
   INSERT INTO CORRECT_OUTPUT VALUES (1);
   INSERT INTO CORRECT_OUTPUT VALUES (1);
   INSERT INTO CORRECT_OUTPUT VALUES (2);
   INSERT INTO CORRECT_OUTPUT VALUES (1);
   INSERT INTO CORRECT_OUTPUT VALUES (1);
   INSERT INTO CORRECT_OUTPUT VALUES (1);
   INSERT INTO CORRECT_OUTPUT VALUES (3);
   INSERT INTO CORRECT_OUTPUT VALUES (1);
   INSERT INTO CORRECT_OUTPUT VALUES (4);
   INSERT INTO CORRECT_OUTPUT VALUES (1);
   INSERT INTO CORRECT_OUTPUT VALUES (4);
   INSERT INTO CORRECT_OUTPUT VALUES (2);
-- PASS:0869 If 22 rows inserted successfully in 22 previous inserts?

   SELECT * FROM (SELECT * FROM CORRECT_OUTPUT MINUS
     SELECT * FROM LOSSY_OUTPUT)
       ORDER BY PORTVAL;
-- PASS:0869 If 3 rows returned in the following order?
-- PASS:0869 If  1?
-- PASS:0869 If  1?
-- PASS:0869 If  4?

   SELECT * 
       FROM CORRECT_OUTPUT WHERE PORTVAL = 1 
     INTERSECT 
       SELECT * 
         FROM LOSSY_OUTPUT WHERE PORTVAL = 1;
-- PASS:0869 If 5 rows selected?

   SELECT * 
        FROM CORRECT_OUTPUT WHERE PORTVAL = 2
     INTERSECT 
       SELECT * 
         FROM LOSSY_OUTPUT WHERE PORTVAL = 2;
-- PASS:0869 If 2 rows selected?

   SELECT *
        FROM CORRECT_OUTPUT WHERE PORTVAL = 3
     INTERSECT 
       SELECT *
         FROM LOSSY_OUTPUT WHERE PORTVAL = 3;
-- PASS:0869 If 1 row selected?

   SELECT *
        FROM CORRECT_OUTPUT WHERE PORTVAL = 4 
     INTERSECT 
       SELECT *
         FROM LOSSY_OUTPUT WHERE PORTVAL = 4;
-- PASS:0869 If 1 row selected?

   COMMIT WORK;

   DROP TABLE BILLS_SENT CASCADE;
   COMMIT WORK;
   DROP TABLE PAYMENTS CASCADE;
   COMMIT WORK;
   DROP TABLE CORRECT_OUTPUT CASCADE;
   COMMIT WORK;
   DROP TABLE LOSSY_OUTPUT CASCADE;
   COMMIT WORK;
-- PASS:0869 If 4 tables dropped successfully in previous 4 drops?

-- END TEST >>> 0869 <<< END TEST
-- *********************************************
-- *************************************************////END-OF-MODULE
