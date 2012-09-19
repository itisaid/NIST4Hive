AUTOCOMMIT OFF;

-- MODULE  DML181  

-- SQL Test Suite, V6.0, Interactive SQL, dml181.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION FLATER
   set schema FLATER;

--O   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:0894 FIPS sizing, length of column lists >= 750!

   CREATE TABLE LONG_NAMED_PEOPLE (
     FIRSTNAME VARCHAR (373) NOT NULL,
     LASTNAME VARCHAR (373) NOT NULL,
     AGE INT,
     PRIMARY KEY (FIRSTNAME, LASTNAME));
-- PASS:0894 If table created successfully?

   COMMIT WORK;

   CREATE TABLE ORDERS (
     FIRSTNAME VARCHAR (373),
     LASTNAME VARCHAR (373),
     TITLE VARCHAR (80),
     COST NUMERIC(5,2),
	FOREIGN KEY (FIRSTNAME, LASTNAME)
	REFERENCES LONG_NAMED_PEOPLE);

-- PASS:0894 If table created successfully?

   COMMIT WORK;

   CREATE VIEW PEOPLE_ORDERS AS
--O     SELECT * FROM LONG_NAMED_PEOPLE  JOIN ORDERS 
     SELECT a.firstname, a.lastname, age, cost FROM LONG_NAMED_PEOPLE a JOIN ORDERS
     ON (a.FIRSTNAME=ORDERS.FIRSTNAME and a.LASTNAME=ORDERS.LASTNAME);
-- PASS:0894 If view created successfully?

   COMMIT WORK;

   INSERT INTO LONG_NAMED_PEOPLE VALUES (
'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' ||
'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' ||
'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' ||
'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' ||
'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' ||
'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' ||
'aaaaaaaaaaaaaaaaaaaaaaaaa', 
'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' ||
'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' ||
'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' ||
'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' ||
'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' ||
'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' ||
'bbbbbbbbbbbbbbbbbbbbbbbbb',
20);
-- PASS:0894 If 1 row inserted successfully?

   INSERT INTO LONG_NAMED_PEOPLE VALUES (
'ccccccccccccccccccccccccccccccccccccccccccccccccccccccccc' ||
'ccccccccccccccccccccccccccccccccccccccccccccccccccccccccc' ||
'ccccccccccccccccccccccccccccccccccccccccccccccccccccccccc' ||
'ccccccccccccccccccccccccccccccccccccccccccccccccccccccccc' ||
'ccccccccccccccccccccccccccccccccccccccccccccccccccccccccc' ||
'ccccccccccccccccccccccccccccccccccccccccccccccccccccccccc' ||
'ccccccccccccccccccccccccccccccc',
'ddddddddddddddddddddddddddddddddddddddddddddddddddddddddd' ||
'ddddddddddddddddddddddddddddddddddddddddddddddddddddddddd' ||
'ddddddddddddddddddddddddddddddddddddddddddddddddddddddddd' ||
'ddddddddddddddddddddddddddddddddddddddddddddddddddddddddd' ||
'ddddddddddddddddddddddddddddddddddddddddddddddddddddddddd' ||
'ddddddddddddddddddddddddddddddddddddddddddddddddddddddddd' ||
'ddddddddddddddddddddddddddddddd',
25);
-- PASS:0894 If 1 row inserted successfully?

   INSERT INTO ORDERS VALUES (
'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' ||
'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' ||
'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' ||
'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' ||
'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' ||
'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' ||
'aaaaaaaaaaaaaaaaaaaaaaaaa',
'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' ||
'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' ||
'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' ||
'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' ||
'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' ||
'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' ||
'bbbbbbbbbbbbbbbbbbbbbbbbb',
'Gidget Goes Skiing',
29.95);
-- PASS:0894 If 1 row inserted successfully?

   INSERT INTO ORDERS VALUES (
'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' ||
'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' ||
'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' ||
'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' ||
'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' ||
'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' ||
'aaaaaaaaaaaaaaaaaaaaaaaaa',
'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' ||
'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' ||
'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' ||
'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' ||
'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' ||
'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' ||
'bbbbbbbbbbbbbbbbbbbbbbbbb',
'Barney Goes Hawaiian',
19.95);
-- PASS:0894 If 1 row inserted successfully?

   INSERT INTO ORDERS VALUES (
'ccccccccccccccccccccccccccccccccccccccccccccccccccccccccc' ||
'ccccccccccccccccccccccccccccccccccccccccccccccccccccccccc' ||
'ccccccccccccccccccccccccccccccccccccccccccccccccccccccccc' ||
'ccccccccccccccccccccccccccccccccccccccccccccccccccccccccc' ||
'ccccccccccccccccccccccccccccccccccccccccccccccccccccccccc' ||
'ccccccccccccccccccccccccccccccccccccccccccccccccccccccccc' ||
'ccccccccccccccccccccccccccccccc',
'ddddddddddddddddddddddddddddddddddddddddddddddddddddddddd' ||
'ddddddddddddddddddddddddddddddddddddddddddddddddddddddddd' ||
'ddddddddddddddddddddddddddddddddddddddddddddddddddddddddd' ||
'ddddddddddddddddddddddddddddddddddddddddddddddddddddddddd' ||
'ddddddddddddddddddddddddddddddddddddddddddddddddddddddddd' ||
'ddddddddddddddddddddddddddddddddddddddddddddddddddddddddd' ||
'ddddddddddddddddddddddddddddddd',
'Invasion of the Smurfs',
9.95);
-- PASS:0894 If 1 row inserted successfully?

   SELECT FIRSTNAME, LASTNAME, AVG(COST)
     FROM PEOPLE_ORDERS
     GROUP BY LASTNAME, FIRSTNAME
     ORDER BY LASTNAME, FIRSTNAME;
-- PASS:0894 If 2 rows are returned in the following order?
-- NOTE:  Columns c1 and c2 are 373 characters each!
--                   c1           c2            c3
--                   ==           ==            ==
-- PASS:0894 If  aaaaaaaa....    bbbbbbbb....  24.95 (+ or - 0.01)?
-- PASS:0894 If  cccccccc....    dddddddd....   9.95 (+ or - 0.01)?

   COMMIT WORK;

--O   DROP TABLE ORDERS CASCADE;
   drop view people_orders;
   DROP TABLE ORDERS ;
-- PASS:0894 If table dropped successfully?

   COMMIT WORK;

--O   DROP TABLE LONG_NAMED_PEOPLE CASCADE;
   DROP TABLE LONG_NAMED_PEOPLE ;
-- PASS:0894 If table dropped successfully?

   COMMIT WORK;

-- END TEST >>> 0894 <<< END TEST
-- *********************************************
-- *************************************************////END-OF-MODULE
