-- MODULE  DML134  

-- SQL Test Suite, V6.0, Interactive SQL, dml134.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION FLATER            

   SELECT USER FROM HU.ECCO;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- NO_TEST:0688 INFO_SCHEM:  Dynamic changes are visible!

-- Testing dynamic SQL

-- *********************************************

-- TEST:0689 Many Trans SQL features #1:  inventory system!

   CREATE TABLE COST_CODES (
  COSTCODE INT UNIQUE,
  COSTTEXT VARCHAR (50) NOT NULL);
-- PASS:0689 If table is created?

   COMMIT;

   CREATE TABLE CONDITION_CODES (
  CONDCODE INT UNIQUE,
  CONDTEXT VARCHAR (50) NOT NULL);
-- PASS:0689 If table is created?

   COMMIT;

   CREATE TABLE ITEM_CODES (
  ITEMCODE INT PRIMARY KEY,
  ITEMTEXT VARCHAR (50) NOT NULL);
-- PASS:0689 If table is created?

   COMMIT;

   CREATE TABLE INVENTORY (
  COSTCODE INT REFERENCES COST_CODES (COSTCODE),
  CONDCODE INT REFERENCES CONDITION_CODES (CONDCODE),
  ITEMCODE INT REFERENCES ITEM_CODES);
-- PASS:0689 If table is created?

   COMMIT;

   CREATE VIEW COMPLETES AS
  SELECT ITEMTEXT, CONDTEXT, COSTTEXT
    FROM INVENTORY NATURAL JOIN COST_CODES
                   NATURAL JOIN CONDITION_CODES
                   NATURAL JOIN ITEM_CODES;
-- PASS:0689 If view is created?

   COMMIT;

   CREATE VIEW INCOMPLETES AS
  SELECT ITEMTEXT, CONDTEXT, COSTTEXT
    FROM INVENTORY, COST_CODES, CONDITION_CODES, ITEM_CODES
      WHERE INVENTORY.ITEMCODE = ITEM_CODES.ITEMCODE
        AND ((INVENTORY.CONDCODE = CONDITION_CODES.CONDCODE
              AND INVENTORY.COSTCODE IS NULL
              AND COST_CODES.COSTCODE IS NULL)
          OR (INVENTORY.COSTCODE = COST_CODES.COSTCODE
              AND INVENTORY.CONDCODE IS NULL
              AND CONDITION_CODES.CONDCODE IS NULL));
-- PASS:0689 If view is created?

   COMMIT;

   CREATE VIEW VERBOSE_INV AS
  SELECT * FROM COMPLETES UNION SELECT * FROM INCOMPLETES;
-- PASS:0689 If view is created?

   COMMIT;

   INSERT INTO COST_CODES VALUES (
   NULL,
   TRIM ('No cost code assigned                             '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO COST_CODES VALUES (
   0,
   TRIM ('Expensive                                         '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO COST_CODES VALUES (
   1,
   TRIM ('Absurdly expensive                                '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO COST_CODES VALUES (
   2,
   TRIM ('Outrageously expensive                            '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO COST_CODES VALUES (
   3,
   TRIM ('Robbery; a complete and total rip-off             '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO CONDITION_CODES VALUES (
   NULL,
   TRIM ('Unknown                                           '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO CONDITION_CODES VALUES (
   1,
   TRIM ('Slightly used                                     '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO CONDITION_CODES VALUES (
   2,
   TRIM ('Returned as defective                             '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO CONDITION_CODES VALUES (
   3,
   TRIM ('Visibly damaged (no returns)                      '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO ITEM_CODES VALUES (
   1,
   TRIM ('Lousy excuse for a tape deck                      '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO ITEM_CODES VALUES (
   3,
   TRIM ('World''s worst VCR                                 '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO ITEM_CODES VALUES (
   4,
   TRIM ('Irreparable intermittent CD player                '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO ITEM_CODES VALUES (
   7,
   TRIM ('Self-destruct VGA monitor w/ critical need detect '));
-- PASS:0689 If 1 row is inserted?

   INSERT INTO INVENTORY VALUES (3, NULL, 4);
-- PASS:0689 If 1 row is inserted?

   INSERT INTO INVENTORY VALUES (1, 2, 3);
-- PASS:0689 If 1 row is inserted?

   INSERT INTO INVENTORY VALUES (2, 3, 7);
-- PASS:0689 If 1 row is inserted?

   INSERT INTO INVENTORY VALUES (0, 3, 1);
-- PASS:0689 If 1 row is inserted?

   INSERT INTO INVENTORY VALUES (3, 1, 7);
-- PASS:0689 If 1 row is inserted?

   SELECT COUNT(*) FROM VERBOSE_INV;
-- PASS:0689 If count = 5?

   SELECT COUNT(*) FROM INCOMPLETES;
-- PASS:0689 If count = 1?

   SELECT COUNT(*) FROM COMPLETES;
-- PASS:0689 If count = 4?

   SELECT COUNT(*) FROM VERBOSE_INV
  WHERE ITEMTEXT = 'Irreparable intermittent CD player'
  AND CONDTEXT = 'Unknown'
  AND COSTTEXT = 'Robbery; a complete and total rip-off';
-- PASS:0689 If count = 1?

   SELECT COUNT(*) FROM VERBOSE_INV
  WHERE ITEMTEXT = 'Lousy excuse for a tape deck'
  AND CONDTEXT = 'Visibly damaged (no returns)'
  AND COSTTEXT = 'Expensive';
-- PASS:0689 If count = 1?

   SELECT COUNT(*) FROM VERBOSE_INV
  WHERE ITEMTEXT =
  'Self-destruct VGA monitor w/ critical need detect'
  AND CONDTEXT = 'Slightly used'
  AND COSTTEXT = 'Robbery; a complete and total rip-off';
-- PASS:0689 If count = 1?

   SELECT COUNT(*) FROM VERBOSE_INV
  WHERE ITEMTEXT =
  'Self-destruct VGA monitor w/ critical need detect'
  AND CONDTEXT = 'Visibly damaged (no returns)'
  AND COSTTEXT = 'Outrageously expensive';
-- PASS:0689 If count = 1?

   SELECT COUNT(*) FROM VERBOSE_INV
  WHERE ITEMTEXT = 'World''s worst VCR'
  AND CONDTEXT = 'Returned as defective'
  AND COSTTEXT = 'Absurdly expensive';
-- PASS:0689 If count = 1?

   COMMIT;

   DROP TABLE INVENTORY CASCADE;
-- PASS:0689 If table and 3 views are dropped?

   COMMIT;

   DROP TABLE COST_CODES CASCADE;
-- PASS:0689 If table is dropped?

   COMMIT;

   DROP TABLE CONDITION_CODES CASCADE;
-- PASS:0689 If table is dropped?

   COMMIT;

   DROP TABLE ITEM_CODES CASCADE;
-- PASS:0689 If table is dropped?

   COMMIT;

-- END TEST >>> 0689 <<< END TEST

-- *********************************************

-- TEST:0690 Many Trans SQL features #2:  talk show schedule!

   CREATE TABLE PORGRAM (
  SEGNO    INT PRIMARY KEY,
  STARTS   TIME NOT NULL,
  LASTS    INTERVAL MINUTE TO SECOND NOT NULL,
  SEGMENT  VARCHAR (50));
-- PASS:0690 If table is created?

   COMMIT;

   CREATE VIEW GAPS AS
  SELECT * FROM PORGRAM AS OUTERR WHERE NOT EXISTS
    (SELECT * FROM PORGRAM AS INNERR WHERE OUTERR.STARTS
    + OUTERR.LASTS = INNERR.STARTS);
-- PASS:0690 If view is created?

   COMMIT;

   INSERT INTO PORGRAM VALUES (
  1, TIME '12:00:00',
  CAST ('10:00' AS INTERVAL MINUTE TO SECOND),
  'Monologue');
-- PASS:0690 If 1 row is inserted?

   INSERT INTO PORGRAM VALUES (
  2, TIME '12:10:00',
  CAST ('04:30' AS INTERVAL MINUTE TO SECOND),
  'Stupid commercials');
-- PASS:0690 If 1 row is inserted?

   INSERT INTO PORGRAM VALUES (
  3, TIME '12:14:30',
  CAST ('12:30' AS INTERVAL MINUTE TO SECOND),
  'Braunschweiger, plug Explosion Man II');
-- PASS:0690 If 1 row is inserted?

   INSERT INTO PORGRAM VALUES (
  4, TIME '12:27:00',
  CAST ('03:00' AS INTERVAL MINUTE TO SECOND),
  'Stupid commercials');
-- PASS:0690 If 1 row is inserted?

   INSERT INTO PORGRAM VALUES (
  5, TIME '12:30:00',
  CAST ('00:10' AS INTERVAL MINUTE TO SECOND),
  'Tease');
-- PASS:0690 If 1 row is inserted?

   INSERT INTO PORGRAM VALUES (
  6, TIME '12:30:10',
  CAST ('03:50' AS INTERVAL MINUTE TO SECOND),
  'Stupid commercials, local news');
-- PASS:0690 If 1 row is inserted?

   INSERT INTO PORGRAM VALUES (
  7, TIME '12:34:00',
  CAST ('11:00' AS INTERVAL MINUTE TO SECOND),
  'Spinal Tap, plug Asexual Harassment');
-- PASS:0690 If 1 row is inserted?

   INSERT INTO PORGRAM VALUES (
  8, TIME '12:45:00',
  CAST ('05:00' AS INTERVAL MINUTE TO SECOND),
  'Stupid commercials');
-- PASS:0690 If 1 row is inserted?

   INSERT INTO PORGRAM VALUES (
  9, TIME '12:50:00',
  CAST ('05:00' AS INTERVAL MINUTE TO SECOND),
  'Spinal Tap, play Ode du Toilette');
-- PASS:0690 If 1 row is inserted?

   INSERT INTO PORGRAM VALUES (
  10, TIME '12:55:00',
  CAST ('03:00' AS INTERVAL MINUTE TO SECOND),
  'Stupid commercials');
-- PASS:0690 If 1 row is inserted?

   INSERT INTO PORGRAM VALUES (
  11, TIME '12:58:00',
  CAST ('00:10' AS INTERVAL MINUTE TO SECOND),
  'Credits');
-- PASS:0690 If 1 row is inserted?

   INSERT INTO PORGRAM VALUES (
  12, TIME '12:58:10',
  CAST ('01:50' AS INTERVAL MINUTE TO SECOND),
  'Stupid commercials');
-- PASS:0690 If 1 row is inserted?

   INSERT INTO PORGRAM VALUES (
  13, TIME '13:00:00',
  CAST ('00:00' AS INTERVAL MINUTE TO SECOND),
  'END');
-- PASS:0690 If 1 row is inserted?

   SELECT COUNT(*) FROM GAPS;
-- PASS:0690 If count = 0?

   UPDATE PORGRAM
  SET STARTS = TIME '12:14:30'
  WHERE SEGNO = 7;
-- PASS:0690 If 1 row is updated?

   UPDATE PORGRAM SET STARTS = STARTS -
  CAST ('01:30' AS INTERVAL MINUTE TO SECOND)
  WHERE SEGNO >= 4 AND SEGNO <= 6;
-- PASS:0690 If 3 rows are updated?

   UPDATE PORGRAM SET STARTS = TIME '12:28:40' +
  CAST ('03:50' AS INTERVAL MINUTE TO SECOND)
  WHERE SEGNO = 3;
-- PASS:0690 If 1 row is updated?

   SELECT COUNT(*) FROM GAPS;
-- PASS:0690 If count = 0?

  SELECT SEGNO FROM PORGRAM ORDER BY STARTS;
-- PASS:0690 If 13 rows selected with SEGNO in the following order?
-- PASS:0690      1
-- PASS:0690      2
-- PASS:0690      7
-- PASS:0690      4
-- PASS:0690      5
-- PASS:0690      6
-- PASS:0690      3
-- PASS:0690      8
-- PASS:0690      9
-- PASS:0690     10
-- PASS:0690     11
-- PASS:0690     12
-- PASS:0690     13

   UPDATE PORGRAM SET LASTS = LASTS -
  CAST (30 AS INTERVAL SECOND) WHERE SEGNO
  = 10;
-- PASS:0690 If 1 row is updated?

   SELECT SEGNO FROM GAPS;
-- PASS:0690 If 1 row selected and SEGNO = 10?

   UPDATE PORGRAM SET LASTS = LASTS +
  CAST ('30' AS INTERVAL SECOND) WHERE
  SEGNO = 9;
-- PASS:0690 If 1 row is updated?

   UPDATE PORGRAM SET STARTS = STARTS +
  CAST (30. AS INTERVAL SECOND) WHERE
  SEGNO = 10;
-- PASS:0690 If 1 row is updated?

   SELECT COUNT(*) FROM GAPS;
-- PASS:0690 If count = 0?

   COMMIT;

   DROP TABLE PORGRAM CASCADE;
-- PASS:0690 If table and view are dropped?

   COMMIT;

-- END TEST >>> 0690 <<< END TEST

-- *********************************************

-- TEST:0691 INFO_SCHEM:  SQLSTATEs for length overruns!

   CREATE TABLE LONG1 (
  C1 INT,
  CHECK (
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL));
-- PASS:0691 If WARNING:  search condition too long for information schema?
-- PASS:0691 OR successful completion?

   ROLLBACK WORK;

   CREATE VIEW LONG2 AS
  SELECT * FROM USIG WHERE
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL AND
C1 IS NOT NULL AND C1 IS NOT NULL AND C1 IS NOT NULL;
-- PASS:0691 If WARNING:  query expression too long for information schema?
-- PASS:0691 OR successful completion?

   ROLLBACK WORK;

-- END TEST >>> 0691 <<< END TEST

-- *************************************************////END-OF-MODULE
