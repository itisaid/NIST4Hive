-- MODULE  DML185  

-- SQL Test Suite, V6.0, Interactive SQL, dml185.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION FLATER            

   SELECT USER FROM HU.ECCO;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:0898 COLUMN_DEFAULT interpretation!

   CREATE TABLE XX
   (C1 CHAR(18)   DEFAULT  USER,
    C2 CHAR(18)   DEFAULT  'USER',
    C3 CHAR(18)   DEFAULT  'Hello World',
    C4 CHAR(18)   DEFAULT  NULL,
    C5 TIMESTAMP  DEFAULT  TIMESTAMP '1993-11-10 00:02:00',
    C6 REAL       DEFAULT  3.3E1,
    C7 REAL);
-- PASS:0898 If table created successfully?

   COMMIT WORK;

   SELECT COLUMN_DEF
     FROM INFO_SCHEM.COLUMNS
     WHERE TABLE_SCHEM = 'FLATER'
     AND TABLE_NAME = 'XX'
     AND COLUMN_DEF = 'USER';
-- PASS:0898 If COLUMN_DEF = USER (no quotes)?

   SELECT COLUMN_DEF
     FROM INFO_SCHEM.COLUMNS WHERE TABLE_SCHEM = 'FLATER'
     AND TABLE_NAME = 'XX' AND COLUMN_DEF = '''USER''';
-- PASS:0898 If COLUMN_DEF = 'USER' (with quotes)?

   SELECT COLUMN_DEF 
     FROM INFO_SCHEM.COLUMNS WHERE TABLE_SCHEM = 'FLATER'
     AND TABLE_NAME = 'XX' AND COLUMN_DEF = '''Hello World''';
-- PASS:0898 If COLUMN_DEF = 'Hello World' (with quotes)?

   SELECT COLUMN_DEF 
     FROM INFO_SCHEM.COLUMNS WHERE TABLE_SCHEM = 'FLATER'
    AND TABLE_NAME = 'XX' AND COLUMN_DEF = 'NULL';
-- PASS:0898 If COLUMN_DEF = NULL (the character string)?

   SELECT COLUMN_DEF 
     FROM INFO_SCHEM.COLUMNS WHERE TABLE_SCHEM = 'FLATER'
     AND TABLE_NAME = 'XX' AND ORDINAL_POSITION = 5;
-- PASS:0898 If COLUMN_DEF = '1993-11-10 00:02:00'?

   INSERT INTO XX (C4, C5)
     VALUES ('Timestamp', CAST ('1993-11-10 00:02:00' AS TIMESTAMP));
-- PASS:0898 If 1 row inserted successfully?

   SELECT COUNT(*) 
     FROM XX WHERE C4 = 'Timestamp' 
     AND C5 BETWEEN TIMESTAMP '1993-11-10 00:01:00' AND
                 TIMESTAMP '1993-11-10 00:03:00';
-- PASS:0898 If COUNT = 1?

   ROLLBACK WORK;

   SELECT CAST (COLUMN_DEF AS REAL)
     FROM INFO_SCHEM.COLUMNS
     WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'XX'
     AND ORDINAL_POSITION = 6;
-- PASS:0898 If COLUMN_DEF = 33 (+ or - 0.1)?
-- NOTE:  COLUMN_DEF char string value may be 33, 3.3E1, +33, etc!

   SELECT COLUMN_DEF 
     FROM INFO_SCHEM.COLUMNS WHERE TABLE_SCHEM = 'FLATER'
     AND TABLE_NAME = 'XX' AND COLUMN_DEF IS NULL;
-- PASS:0898 If COLUMN_DEF = NULL value?

   COMMIT WORK;

   DROP TABLE XX CASCADE;
-- PASS:0898 If table dropped successfully?

   COMMIT WORK;

-- END TEST >>> 0898 <<< END TEST
-- *********************************************
-- *************************************************////END-OF-MODULE
