-- MODULE  DML112  

-- SQL Test Suite, V6.0, Interactive SQL, dml112.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION FLATER            

   SELECT USER FROM HU.ECCO;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:0621 DATETIME NULLs!

   CREATE TABLE MERCH (
     ITEMKEY INT,
     ORDERED DATE,
     RDATE DATE,
     RTIME TIME,
     SOLD TIMESTAMP);
-- PASS:0621 If table is created?

   COMMIT WORK;

   CREATE TABLE TURNAROUND (
     ITEMKEY INT,
     MWAIT INTERVAL MONTH,
     DWAIT INTERVAL DAY TO HOUR);
-- PASS:0621 If table is created?

   COMMIT WORK;

   CREATE VIEW INVENTORY AS
     SELECT MERCH.ITEMKEY AS ITEMKEY, ORDERED,
     MWAIT, DWAIT FROM MERCH, TURNAROUND COR1 WHERE RDATE
     IS NOT NULL AND SOLD IS NULL AND
     MERCH.ITEMKEY = COR1.ITEMKEY
            UNION
     SELECT ITEMKEY, ORDERED,
     CAST (NULL AS INTERVAL MONTH),
     CAST (NULL AS INTERVAL DAY TO HOUR) FROM
     MERCH WHERE RDATE IS NOT NULL AND SOLD IS NULL
     AND MERCH.ITEMKEY NOT IN (SELECT ITEMKEY
     FROM TURNAROUND);
-- PASS:0621 If view is created?

   COMMIT WORK;

   INSERT INTO MERCH VALUES (0, DATE '1993-11-23', NULL, NULL, NULL);
-- PASS:0621 If 1 row is inserted?

   INSERT INTO MERCH VALUES (1, DATE '1993-12-10', DATE '1994-01-03',
          CAST (NULL AS TIME), NULL);
-- PASS:0621 If 1 row is inserted?

   INSERT INTO MERCH VALUES (2, DATE '1993-12-11', NULL,
          NULL, CAST ('TIMESTAMP ''1993-12-11 13:00:00''' AS TIMESTAMP));
-- PASS:0621 If 1 row is inserted?

   INSERT INTO MERCH VALUES (4, DATE '1993-01-26', DATE '1993-01-27',
          NULL, NULL);
-- PASS:0621 If 1 row is inserted?

   INSERT INTO TURNAROUND VALUES (2, INTERVAL '1' MONTH, 
          INTERVAL '20:0' DAY TO HOUR);
-- PASS:0621 If 1 row is inserted?

   INSERT INTO TURNAROUND VALUES (5, INTERVAL '5' MONTH,
          CAST (NULL AS INTERVAL DAY TO HOUR));
-- PASS:0621 If 1 row is inserted?

   INSERT INTO TURNAROUND VALUES (6, INTERVAL '2' MONTH, NULL);
-- PASS:0621 If 1 row is inserted?

   SELECT COUNT(*) FROM
     MERCH A, MERCH B WHERE A.SOLD = B.SOLD;
-- PASS:0621 If count = 1?

   SELECT COUNT(*) FROM
     MERCH A, MERCH B WHERE A.RTIME = B.RTIME;
-- PASS:0621 If count = 0?

   SELECT COUNT(*) FROM
     MERCH WHERE RDATE IS NULL;
-- PASS:0621 If count = 2?

   SELECT COUNT(*) FROM
     TURNAROUND WHERE DWAIT IS NOT NULL;
-- PASS:0621 If count = 1?

   SELECT EXTRACT (DAY FROM RDATE)
     FROM MERCH, TURNAROUND WHERE MERCH.ITEMKEY =
     TURNAROUND.ITEMKEY;
-- PASS:0621 If 1 row selected and value is NULL?

   SELECT ITEMKEY FROM MERCH WHERE SOLD IS NOT NULL;
-- PASS:0621 If 1 row selected and ITEMKEY is 2?

   SELECT EXTRACT (HOUR FROM AVG (DWAIT))
      FROM MERCH, TURNAROUND WHERE
      MERCH.ITEMKEY = TURNAROUND.ITEMKEY OR
      TURNAROUND.ITEMKEY NOT IN
        (SELECT ITEMKEY FROM MERCH);
-- PASS:0621 If 1 row selected and value is 0?

   SELECT COUNT(*)
     FROM INVENTORY WHERE MWAIT IS NULL
     AND DWAIT IS NULL;
-- PASS:0621 If count = 2?

   COMMIT WORK;

   DROP TABLE MERCH CASCADE;
-- PASS:0621 If table is dropped?

   COMMIT WORK;

   DROP TABLE TURNAROUND CASCADE;
-- PASS:0621 If table is dropped?

   COMMIT WORK;

-- END TEST >>> 0621 <<< END TEST

-- *********************************************

-- TEST:0623 OUTER JOINs with NULLs and empty tables!

   CREATE TABLE JNULL1 (C1 INT, C2 INT);
-- PASS:0623 If table is created?

   COMMIT WORK;

   CREATE TABLE JNULL2 (D1 INT, D2 INT);
-- PASS:0623 If table is created?

   COMMIT WORK;

   CREATE VIEW JNULL3 AS
     SELECT C1, D1, D2 FROM JNULL1 LEFT OUTER JOIN JNULL2
     ON C2 = D2;
-- PASS:0623 If view is created?

   COMMIT WORK;

   CREATE VIEW JNULL4 AS
     SELECT D1, D2 AS C2 FROM JNULL2;
-- PASS:0623 If view is created?

   COMMIT WORK;

   CREATE VIEW JNULL5 AS
     SELECT C1, D1, C2 FROM JNULL1 RIGHT OUTER JOIN JNULL4
     USING (C2);
-- PASS:0623 If view is created?

   COMMIT WORK;

   CREATE VIEW JNULL6 AS
     SELECT * FROM JNULL1 LEFT OUTER JOIN JNULL4
     USING (C2);
-- PASS:0623 If view is created?

   COMMIT WORK;

   INSERT INTO JNULL1 VALUES (NULL, NULL);
-- PASS:0623 If 1 row is inserted?

   INSERT INTO JNULL1 VALUES (1, NULL);
-- PASS:0623 If 1 row is inserted?

   INSERT INTO JNULL1 VALUES (NULL, 1);
-- PASS:0623 If 1 row is inserted?

   INSERT INTO JNULL1 VALUES (1, 1);
-- PASS:0623 If 1 row is inserted?

   INSERT INTO JNULL1 VALUES (2, 2);
-- PASS:0623 If 1 row is inserted?

   SELECT COUNT(*) FROM JNULL3;
-- PASS:0623 If count = 5?

   SELECT COUNT(*) FROM JNULL3
     WHERE D2 IS NOT NULL OR D1 IS NOT NULL;
-- PASS:0623 If count = 0?

   SELECT COUNT(*) FROM JNULL5;
-- PASS:0623 If count = 0?

   SELECT COUNT(*) FROM JNULL6
     WHERE C2 IS NOT NULL;
-- PASS:0623 If count = 3?

   INSERT INTO JNULL2
     SELECT * FROM JNULL1;
-- PASS:0623 If 5 rows are inserted?

   UPDATE JNULL2
     SET D2 = 1 WHERE D2 = 2;
-- PASS:0623 If 1 row is updated?

   SELECT COUNT(*) FROM JNULL3;
-- PASS:0623 If count = 9?

   SELECT COUNT(*) 
     FROM JNULL3 WHERE C1 IS NULL;
-- PASS:0623 If count = 4?

   SELECT COUNT(*) 
     FROM JNULL3 WHERE D1 IS NULL;
-- PASS:0623 If count = 5?

   SELECT COUNT(*) 
     FROM JNULL3 WHERE D2 IS NULL;
-- PASS:0623 If count = 3?

   SELECT AVG(D1) * 10 
     FROM JNULL3;
-- PASS:0623 If value is 15 (approximately)?

   SELECT COUNT(*) 
     FROM JNULL6
      WHERE C2 = 1;
-- PASS:0623 If count = 6?

   SELECT COUNT(*) 
     FROM JNULL6
      WHERE C2 IS NULL;
-- PASS:0623 If count = 2?

   SELECT COUNT(*) 
     FROM JNULL6
      WHERE C2 = C1
        AND D1 IS NULL;
-- PASS:0623 If count = 2?

   COMMIT WORK;

   DROP TABLE JNULL1 CASCADE;
-- PASS:0623 If table is dropped?

   COMMIT WORK;

   DROP TABLE JNULL2 CASCADE;
-- PASS:0623 If table is dropped?

   COMMIT WORK;

-- END TEST >>> 0623 <<< END TEST

-- *********************************************

-- TEST:0625 ADD COLUMN and DROP COLUMN!

   CREATE TABLE CHANGG
     (NAAM CHAR (14) NOT NULL PRIMARY KEY, AGE INT);
-- PASS:0625 If table is created?

   COMMIT WORK;

   CREATE VIEW CHANGGVIEW AS
     SELECT * FROM CHANGG;
-- PASS:0625 If view is created?

   COMMIT WORK;

   ALTER TABLE CHANGG
     DROP NAAM RESTRICT;
-- PASS:0625 If ERROR, view references NAAM?

   COMMIT WORK;

   INSERT INTO CHANGG VALUES ('RALPH', 22);
-- PASS:0625 If 1 row is inserted?

   INSERT INTO CHANGG VALUES ('RUDOLPH', 54);
-- PASS:0625 If 1 row is inserted?

   INSERT INTO CHANGG VALUES ('QUEEG', 33);
-- PASS:0625 If 1 row is inserted?

   INSERT INTO CHANGG VALUES ('BESSIE', 106);
-- PASS:0625 If 1 row is inserted?

   SELECT COUNT(*) 
     FROM CHANGG WHERE DIVORCES IS NULL;
-- PASS:0625 If ERROR, column does not exist?

   COMMIT WORK;

   ALTER TABLE CHANGG ADD NUMBRR CHAR(11);
-- PASS:0625 If column is added?

   COMMIT WORK;

   SELECT MAX(AGE) FROM CHANGGVIEW;
-- PASS:0625 If value is 106?

   SELECT MAX(NUMBRR) FROM CHANGGVIEW;
-- PASS:0625 If ERROR, column does not exist ?

   COMMIT WORK;

   DROP VIEW CHANGGVIEW CASCADE;
-- PASS:0625 If view is dropped?

   COMMIT WORK;

   ALTER TABLE CHANGG
     ADD COLUMN DIVORCES INT DEFAULT 0;
-- PASS:0625 If column is added?

   COMMIT WORK;

   SELECT COUNT(*) 
     FROM CHANGG WHERE NUMBRR IS NOT NULL
     OR DIVORCES <> 0;
-- PASS:0625 If count = 0?

   UPDATE CHANGG
     SET NUMBRR = '837-47-1847', DIVORCES = 3
     WHERE NAAM = 'RUDOLPH';
-- PASS:0625 If 1 row is updated?

   UPDATE CHANGG
     SET NUMBRR = '738-47-1847', DIVORCES = NULL
     WHERE NAAM = 'QUEEG';
-- PASS:0625 If 1 row is updated?

   DELETE FROM CHANGG
     WHERE NUMBRR IS NULL;
-- PASS:0625 If 2 rows are deleted?

   INSERT INTO CHANGG (NAAM, AGE, NUMBRR)
     VALUES ('GOOBER', 16, '000-10-0001');
-- PASS:0625 If 1 row is inserted?

   INSERT INTO CHANGG
     VALUES ('OLIVIA', 20, '111-11-1111', 0);
-- PASS:0625 If 1 row is inserted?

   SELECT AGE, NUMBRR, DIVORCES
     FROM CHANGG
     WHERE NAAM = 'RUDOLPH';
-- PASS:0625 If 1 row selected with values 54, 837-47-1847, 3 ?

   SELECT AGE, NUMBRR, DIVORCES
     FROM CHANGG
     WHERE NAAM = 'QUEEG';
-- PASS:0625 If 1 row selected with values 33, 738-47-1847, NULL ?

   SELECT AGE, NUMBRR, DIVORCES
     FROM CHANGG
     WHERE NAAM = 'GOOBER';
-- PASS:0625 If 1 row selected with values 16, 000-10-0001, 0 ?

   SELECT AGE, NUMBRR, DIVORCES
     FROM CHANGG
     WHERE NAAM = 'OLIVIA';
-- PASS:0625 If 1 row selected with values 20, 111-11-1111, 0 ?

   SELECT COUNT(*) FROM CHANGG;
-- PASS:0625 If count = 4?

   COMMIT WORK;

   ALTER TABLE CHANGG DROP AGE CASCADE;
-- PASS:0625 If column is dropped?

   COMMIT WORK;

   ALTER TABLE CHANGG DROP COLUMN DIVORCES RESTRICT;
-- PASS:0625 If column is dropped?

   COMMIT WORK;

   SELECT COUNT(*) 
     FROM CHANGG WHERE AGE > 30;
-- PASS:0625 If ERROR, column does not exist?

   SELECT COUNT(*) 
     FROM CHANGG WHERE DIVORCES IS NULL;
-- PASS:0625 If ERROR, column does not exist?

   SELECT NAAM 
     FROM CHANGG
     WHERE NUMBRR LIKE '%000%';
-- PASS:0625 If 1 row selected with value GOOBER ?

   COMMIT WORK;

   CREATE TABLE REFERENCE_CHANGG (
    NAAM CHAR (14) NOT NULL PRIMARY KEY
    REFERENCES CHANGG);
-- PASS:0625 If table is created?

   COMMIT WORK;

   INSERT INTO REFERENCE_CHANGG VALUES ('NO SUCH NAAM');
-- PASS:0625 If RI ERROR, parent missing, 0 rows inserted?

   COMMIT WORK;

   ALTER TABLE CHANGG DROP NAAM RESTRICT;
-- PASS:0625 If ERROR, referential constraint exists?

   COMMIT WORK;

   ALTER TABLE CHANGG DROP NAAM CASCADE;
-- PASS:0625 If column is dropped?

   COMMIT WORK;

   INSERT INTO REFERENCE_CHANGG VALUES ('NO SUCH NAAM');
-- PASS:0625 If 1 row is inserted?

   COMMIT WORK;

   ALTER TABLE CHANGG DROP NUMBRR RESTRICT;
-- PASS:0625 If ERROR, last column may not be dropped?

   COMMIT WORK;

   DROP TABLE CHANGG CASCADE;
-- PASS:0625 If table is dropped?

   COMMIT WORK;

   DROP TABLE REFERENCE_CHANGG CASCADE;
-- PASS:0625 If table is dropped?

   COMMIT WORK;

-- END TEST >>> 0625 <<< END TEST

-- *********************************************

-- TEST:0631 Datetimes in a <default clause>!

   CREATE TABLE OBITUARIES (
    NAAM CHAR (14) NOT NULL PRIMARY KEY,
    BORN DATE DEFAULT DATE '1880-01-01',
    DIED DATE DEFAULT CURRENT_DATE,
    ENTERED TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    TESTING1 DATE,
    TESTING2 TIMESTAMP);
-- PASS:0631 If table is created?

   COMMIT WORK;

   CREATE TABLE BIRTHS (
    NAAM CHAR (14) NOT NULL PRIMARY KEY,
    CHECKIN TIME (0)
        DEFAULT TIME '00:00:00',
    LABOR INTERVAL HOUR
        DEFAULT INTERVAL '4' HOUR,
    CHECKOUT TIME
        DEFAULT CURRENT_TIME,
    TESTING TIME);
-- PASS:0631 If table is created?

   COMMIT WORK;

   INSERT INTO OBITUARIES (NAAM, TESTING1, TESTING2)
     VALUES ('KEITH', CURRENT_DATE, CURRENT_TIMESTAMP);
-- PASS:0631 If 1 row is inserted?

   INSERT INTO BIRTHS (NAAM, TESTING)
     VALUES ('BJORN', CURRENT_TIME);
-- PASS:0631 If 1 row is inserted?

   SELECT EXTRACT (HOUR FROM CHECKIN) +
                EXTRACT (MINUTE FROM CHECKIN) +
                EXTRACT (SECOND FROM CHECKIN)
   FROM BIRTHS;
-- PASS:0631 If 1 row selected with value 0?

   SELECT EXTRACT (HOUR FROM LABOR) FROM BIRTHS;
-- PASS:0631 If 1 row selected with value 4?

   SELECT COUNT (*) FROM BIRTHS
     WHERE TESTING <> CHECKOUT OR CHECKOUT IS NULL;
-- PASS:0631 If count = 0?

   SELECT COUNT (*) FROM OBITUARIES
     WHERE BORN <> DATE '1880-01-01'
     OR BORN IS NULL
     OR DIED <> TESTING1
     OR DIED IS NULL
     OR ENTERED <> TESTING2
     OR ENTERED IS NULL;
-- PASS:0631 If count = 0?

   COMMIT WORK;

   DROP TABLE BIRTHS CASCADE;
-- PASS:0631 If table is dropped?

   COMMIT WORK;

   DROP TABLE OBITUARIES CASCADE;
-- PASS:0631 If table is dropped?

   COMMIT WORK;

-- END TEST >>> 0631 <<< END TEST

-- *********************************************

-- TEST:0633 TRIM function!

   CREATE TABLE WEIRDPAD (
    NAAM CHAR (14),
    SPONSOR CHAR (14),
    PADCHAR CHAR (1));
-- PASS:0633 If table is created?

   COMMIT WORK;

   INSERT INTO WEIRDPAD (NAAM, SPONSOR) VALUES
     ('KATEBBBBBBBBBB', '000000000KEITH');
-- PASS:0633 If 1 row is inserted?

   INSERT INTO WEIRDPAD (NAAM, SPONSOR) VALUES
     ('    KEITH     ', 'XXXXKATEXXXXXX');
-- PASS:0633 If 1 row is inserted?

   SELECT TRIM ('X' FROM SPONSOR) 
     FROM WEIRDPAD
     WHERE TRIM (NAAM) = 'KEITH';
-- PASS:0633 If 1 row selected with value KATE ? 

   SELECT TRIM (LEADING 'X' FROM SPONSOR) 
     FROM WEIRDPAD
     WHERE TRIM (TRAILING FROM NAAM) = '    KEITH';
-- PASS:0633 If 1 row selected with value KATEXXXXXX ?

   SELECT TRIM (LEADING 'X' FROM SPONSOR) 
     FROM WEIRDPAD
     WHERE TRIM (TRAILING 'X' FROM SPONSOR) = 'XXXXKATE';
-- PASS:0633 If 1 row selected with value KATEXXXXXX ?

   SELECT TRIM (LEADING FROM B.NAAM)  FROM WEIRDPAD A,
     WEIRDPAD B WHERE TRIM (BOTH 'B' FROM A.NAAM)
                    = TRIM (BOTH 'X' FROM B.SPONSOR);
-- PASS:0633 If 1 row selected with value KEITH ?

   SELECT COUNT(*) FROM WEIRDPAD A,
     WEIRDPAD B WHERE TRIM (LEADING '0' FROM A.SPONSOR)
                    = TRIM (' ' FROM B.NAAM);
-- PASS:0633 If count = 1?

   SELECT TRIM ('BB' FROM NAAM)
     FROM WEIRDPAD WHERE NAAM LIKE 'KATE%';
-- PASS:0633 If ERROR, length of trim character must be 1 ?

   INSERT INTO WEIRDPAD (NAAM, SPONSOR)
     SELECT DISTINCT TRIM (LEADING 'D' FROM HU.STAFF.CITY), 
                     TRIM (TRAILING 'n' FROM PTYPE)
     FROM HU.STAFF, HU.PROJ 
     WHERE EMPNAME = 'Alice';
-- PASS:0633 If 3 rows are inserted?

   SELECT COUNT(*) FROM WEIRDPAD;
-- PASS:0633 If count = 5?

   UPDATE WEIRDPAD
     SET SPONSOR = TRIM ('X' FROM SPONSOR),
             NAAM = TRIM ('B' FROM NAAM);
-- PASS:0633 If 2 rows are updated?

   SELECT COUNT(*) FROM WEIRDPAD
     WHERE NAAM = 'KATE' OR SPONSOR = 'KATE';
-- PASS:0633 If count = 2?

   DELETE FROM WEIRDPAD WHERE
     TRIM(LEADING 'K' FROM 'Kest') = TRIM('T' FROM SPONSOR);
-- PASS:0633 If 1 row is deleted?

   SELECT COUNT(*) FROM WEIRDPAD;
-- PASS:0633 If count = 4?

   UPDATE WEIRDPAD
      SET PADCHAR = '0'
     WHERE SPONSOR = '000000000KEITH'
        OR NAAM    = 'eale';
-- PASS:0633 If 3 rows are updated?

   UPDATE WEIRDPAD
      SET SPONSOR = NULL
     WHERE SPONSOR = 'Desig';
-- PASS:0633 If 1 row is updated?

   SELECT COUNT(*) FROM WEIRDPAD
     WHERE TRIM (PADCHAR FROM SPONSOR) IS NULL;
-- PASS:0633 If count = 2?

   SELECT COUNT(*) FROM WEIRDPAD
     WHERE TRIM (PADCHAR FROM SPONSOR) = 'KEITH';
-- PASS:0633 If count = 1?

   COMMIT WORK;

   DROP TABLE WEIRDPAD CASCADE;
-- PASS:0633 If table is dropped?

   COMMIT WORK;

-- END TEST >>> 0633 <<< END TEST

-- *************************************************////END-OF-MODULE
