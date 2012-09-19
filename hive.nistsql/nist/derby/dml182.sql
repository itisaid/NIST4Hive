AUTOCOMMIT OFF;

-- MODULE  DML182  

-- SQL Test Suite, V6.0, Interactive SQL, dml182.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION FLATER
   set schema FLATER;

--O   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:0895 FIPS sizing, columns in list >= 15!

   CREATE TABLE ID_CODES (
     CODE1 INT NOT NULL,
     CODE2 INT NOT NULL,
     CODE3 INT NOT NULL,
     CODE4 INT NOT NULL,
     CODE5 INT NOT NULL,
     CODE6 INT NOT NULL,
     CODE7 INT NOT NULL,
     CODE8 INT NOT NULL,
     CODE9 INT NOT NULL,
     CODE10 INT NOT NULL,
     CODE11 INT NOT NULL,
     CODE12 INT NOT NULL,
     CODE13 INT NOT NULL,
     CODE14 INT NOT NULL,
     CODE15 INT NOT NULL,
     PRIMARY KEY (CODE1, CODE2, CODE3, CODE4, CODE5,
       CODE6, CODE7, CODE8, CODE9, CODE10,
       CODE11, CODE12, CODE13, CODE14, CODE15));
-- PASS:0895 If table created successfully?

   COMMIT WORK;

   CREATE TABLE ORDERS (
     CODE1 INT,
     CODE2 INT,
     CODE3 INT,
     CODE4 INT,
     CODE5 INT,
     CODE6 INT,
     CODE7 INT,
     CODE8 INT,
     CODE9 INT,
     CODE10 INT,
     CODE11 INT,
     CODE12 INT,
     CODE13 INT,
     CODE14 INT,
     CODE15 INT,
     TITLE VARCHAR (80),
     COST NUMERIC(5,2),
     FOREIGN KEY (CODE1, CODE2, CODE3, CODE4, CODE5,
       CODE6, CODE7, CODE8, CODE9, CODE10,
       CODE11, CODE12, CODE13, CODE14, CODE15)
     REFERENCES ID_CODES);
	
-- PASS:0895 If table created successfully?

   COMMIT WORK;

   CREATE VIEW ID_ORDERS (CODE1, CODE2, CODE3, CODE4, 
	CODE5, CODE6, CODE7, CODE8, CODE9, CODE10,
	CODE11, CODE12, CODE13, CODE14, CODE15, title, cost) AS
--O     SELECT * FROM ID_CODES JOIN ORDERS
     SELECT  
       a.CODE1, a.CODE2, a.CODE3, a.CODE4, a.CODE5,
         a.CODE6, a.CODE7, a.CODE8, a.CODE9, a.CODE10,
         a.CODE11, a.CODE12, a.CODE13, a.CODE14, a.CODE15, title, cost
	FROM ID_CODES a JOIN ORDERS
       ON (a.CODE1=ORDERS.CODE1 and a.CODE2=ORDERS.CODE2 and a.CODE3=ORDERS.CODE3 and a.CODE4=ORDERS.CODE4 and a.CODE5=ORDERS.CODE5 and
         a.CODE6=ORDERS.CODE6 and a.CODE7=ORDERS.CODE7 and a.CODE8=ORDERS.CODE8 and a.CODE9=ORDERS.CODE9 and a.CODE10=ORDERS.CODE10 and
         a.CODE11=ORDERS.CODE11 and a.CODE12=ORDERS.CODE12 and a.CODE13=ORDERS.CODE13 and a.CODE14=ORDERS.CODE14 and a.CODE15=ORDERS.CODE15);
-- PASS:0895 If view created successfully

   COMMIT WORK;

   INSERT INTO ID_CODES VALUES (
     1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
   INSERT INTO ID_CODES VALUES (
     1, 2, 3, 4, 5, 6, 7, 9, 8, 10, 11, 12, 13, 14, 15);
   INSERT INTO ORDERS VALUES (
     1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
     'Gidget Goes Skiing',
     29.95);
   INSERT INTO ORDERS VALUES (
     1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
     'Barney Goes Hawaiian',
     19.95);
   INSERT INTO ORDERS VALUES (
     1, 2, 3, 4, 5, 6, 7, 9, 8, 10, 11, 12, 13, 14, 15,
     'Invasion of the Smurfs',
     9.95);
-- PASS:0895 If 5 rows inserted successfully in previous 5 inserts?

   SELECT CODE1, CODE2, CODE3, CODE4, CODE5,
       CODE6, CODE7, CODE8, CODE9, CODE10,
       CODE11, CODE12, CODE13, CODE14, CODE15,
     AVG(COST)
     FROM ID_ORDERS
     GROUP BY CODE1, CODE2, CODE3, CODE4, CODE5,
       CODE6, CODE7, CODE8, CODE9, CODE10,
       CODE11, CODE12, CODE13, CODE14, CODE15
     ORDER BY CODE1, CODE2, CODE3, CODE4, CODE5,
       CODE6, CODE7, CODE8, CODE9, CODE10,
       CODE11, CODE12, CODE13, CODE14, CODE15;
-- PASS:0895 If 2 rows are returned?
--                 avg(cost)
--                 =========
-- PASS:0895 If    24.95 (+ or - 0.01)  ?
-- PASS:0895 If     9.95 (+ or - 0.01)  ?

   COMMIT WORK;

--O   DROP TABLE ORDERS CASCADE;
   drop view id_orders;
   DROP TABLE ORDERS ;
-- PASS:0895 If table dropped successfully?

   COMMIT WORK;

--O   DROP TABLE ID_CODES CASCADE;
   DROP TABLE ID_CODES ;
-- PASS:0895 If table dropped successfully?

   COMMIT WORK;

-- END TEST >>> 0895 <<< END TEST
-- *********************************************
-- *************************************************////END-OF-MODULE
