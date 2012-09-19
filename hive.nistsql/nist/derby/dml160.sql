AUTOCOMMIT OFF;

-- MODULE  DML160  

-- SQL Test Suite, V6.0, Interactive SQL, dml160.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION FLATER
   set schema FLATER;

--O   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:0859 <joined table> contained in <select list>!

   SELECT EMPNUM, (SELECT COUNT(*) FROM HU.WORKS JOIN HU.PROJ
     ON HU.WORKS.PNUM = HU.PROJ.PNUM
     AND BUDGET > AVG (OSTAFF.GRADE) * 1000
     WHERE HU.WORKS.EMPNUM = OSTAFF.EMPNUM) FROM HU.STAFF AS OSTAFF
     ORDER BY 2, 1;
-- PASS:0859 If 5 rows are returned in the following order?
--               empnum   count
--               ======   =====
-- PASS:0859 If    E5       0  ?
-- PASS:0859 If    E2       1  ?
-- PASS:0859 If    E3       1  ?
-- PASS:0859 If    E4       2  ?
-- PASS:0859 If    E1       4  ?  

   COMMIT WORK;

-- END TEST >>> 0859 <<< END TEST
-- *********************************************

-- TEST:0860 Domains over various data types!

--O   CREATE DOMAIN EPOCH_NOT_NORM AS DECIMAL (5, 2);
--O-- PASS:0860 If domain created successfully?
--O
--O   COMMIT WORK;
--O
--O   CREATE DOMAIN RAD_EPOCH_TYPE FLOAT (20)
--O     CHECK (VALUE BETWEEN 0E0 AND 2E0 * 3.1416E0);
--O-- PASS:0860 If domain created successfully?
--O
--O   COMMIT WORK;
--O
--O   CREATE DOMAIN RAD_EPOCH_NOT_NORM REAL;
--O-- PASS:0860 If domain created successfully?
--O
--O   COMMIT WORK;
--O
--O   CREATE DOMAIN TIDEDATE AS DATE
--O    CHECK (VALUE BETWEEN DATE( '1994-01-01') AND DATE( '2025-12-31'));
--O-- PASS:0860 If domain created successfully?
--O
--O   COMMIT WORK;
--O
--O   CREATE DOMAIN TIDETIMESTAMP AS TIMESTAMP WITH TIME ZONE
--O    CHECK (VALUE BETWEEN TIMESTAMP( '1994-01-01 00:00:00+00:00')
--O    AND TIMESTAMP( '2025-12-31 23:59:59+00:00'));
--O-- PASS:0860 If domain created successfully?
--O
--O   COMMIT WORK;
--O
--O   CREATE DOMAIN DINNERTIME AS TIME
--O    CHECK (VALUE BETWEEN TIME( '17:30:00') AND TIME( '19:00:00'));
--O-- PASS:0860 If domain created successfully?
--O
--O   COMMIT WORK;
--O
--O   CREATE TABLE CONST_NOT_NORM (
--O     LOC_ID DEC (7) NOT NULL,
--O     CONST_ID TIDES.CONST_ID_TYPE NOT NULL,
--O     UNIQUE (LOC_ID, CONST_ID),
--O     AMPLITUDE TIDES.AMPLITUDE_TYPE,
--O     EPOCH EPOCH_NOT_NORM);
--O-- PASS:0860 If table created successfully?
--O
--O   COMMIT WORK;
--O
--O   CREATE VIEW CONST_RAD (LOC_ID, CONST_ID,
--O     AMPLITUDE, EPOCH) AS
--O     SELECT LOC_ID, CONST_ID, AMPLITUDE,
--O     CAST (EPOCH * 3.14159265358979E0 / 180E0 AS RAD_EPOCH_TYPE)
--O     FROM TIDES.CONSTITUENTS;
--O-- PASS:0860 If view created successfully?
--O
--O   COMMIT WORK;
--O
--O   CREATE VIEW CONST_RAD_NOT_NORM (LOC_ID, CONST_ID,
--O     AMPLITUDE, EPOCH) AS
--O     SELECT LOC_ID, CONST_ID, AMPLITUDE,
--O     CAST (EPOCH * 3.14159265358979E0 / 180E0 AS RAD_EPOCH_NOT_NORM)
--O     FROM CONST_NOT_NORM;
--O-- PASS:0860 If view created successfully?
--O
--O   COMMIT WORK;
--O
--O   CREATE TABLE PENDING (
--O     LOC_ID DEC (7) NOT NULL,
--O     FROMTIME TIDETIMESTAMP NOT NULL,
--O     TOTIME TIDETIMESTAMP NOT NULL,
--O     CHECK (FROMTIME <= TOTIME),
--O     JOB_ID INT PRIMARY KEY);
--O-- PASS:0860 If table created successfully?
--O
--O   COMMIT WORK;
--O
--O   CREATE VIEW CHECK_PTS (CHECK_DATES, JOB_ID, FLAG) AS
--O     SELECT CAST (FROMTIME AS TIDEDATE), JOB_ID,
--O     CAST (0 AS INT) FROM PENDING
--O       UNION
--O     SELECT CAST (TOTIME AS TIDEDATE), JOB_ID,
--O     CAST (1 AS INT) FROM PENDING;
--O-- PASS:0860 If view created successfully?
--O
--O   COMMIT WORK;
--O
--O   CREATE TABLE DINNER_CLUB (
--O     LOC_ID DEC (7) NOT NULL,
--O     DINNER DINNERTIME);
--O-- PASS:0860 If table created successfully?
--O
--O   COMMIT WORK;
--O
--O   SELECT HOUR( MERIDIAN), EXTRACT
--O     (MINUTE FROM MERIDIAN) 
--O     FROM TIDES.LOCATIONS WHERE LOC_NAME LIKE '%Newfound%';
--O-- PASS:0860 If xhour = -3?
--O-- PASS:0860 If xminute = -30?
--O
--O   INSERT INTO TIDES.LOCATIONS VALUES (
--O     300, 'Atlantis', 160.0000, 3.0000, 0, 1.2E0,
--O     INTERVAL -'13:00' HOUR TO MINUTE, 'GMT-13');
--O-- PASS:0860 If ERROR - integrity constraint violation?
--O
--O   UPDATE TIDES.CONSTITUENTS
--O     SET AMPLITUDE = - AMPLITUDE
--O     WHERE LOC_ID = 100
--O     AND CONST_ID = 0;
--O-- PASS:0860 If ERROR - integrity constraint violation?
--O
--O   INSERT INTO TIDES.LOCATIONS VALUES (300,
--O     'Bath, Maine', -69.8133, 43.9183,
--O     1, 3.422E0, INTERVAL '-05:00' HOUR TO MINUTE, ':US/Eastern');
--O-- PASS:0860 If 1 row inserted successfully?
--O
--O   INSERT INTO TIDES.CONSTITUENTS VALUES (300, 2, 0.134E0, 385.0);
--O-- PASS:0860 If ERROR - integrity constraint violation?
--O
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 0, 0.021E0, 151.6);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 1, 0.324E0, 144.5);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 2, 0.134E0, 385.0);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 3, 0.181E0, 40.9);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 4, 0.037E0, 150.0);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 5, 3.143E0, 352.3);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 6, 0.000E0, 50.0);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 7, 0.104E0, 242.8);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 8, 0.031E0, 158.6);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 9, 0.000E0, 133.3);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 10, 0.744E0, 322.0);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 11, 0.087E0, 307.4);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 12, 0.260E0, 130.4);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 13, 0.011E0, 158.7);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 14, 0.107E0, 140.8);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 15, 0.043E0, 114.3);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 16, 0.007E0, 116.4);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 17, 0.004E0, 383.2);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 18, 0.000E0, 17.3);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 19, 0.488E0, 383.4);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 20, 0.000E0, 69.0);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 21, 0.000E0, 103.5);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 22, 0.053E0, 365.8);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 23, 0.053E0, 37.3);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 24, 0.023E0, 297.8);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 25, 0.138E0, 328.3);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 26, 0.010E0, 124.4);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 27, 0.000E0, 50.6);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 28, 0.000E0, 49.4);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 29, 0.000E0, 66.0);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 30, 0.000E0, 67.8);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 31, 0.000E0, 35.7);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 32, 0.073E0, 285.0);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 33, 0.033E0, 257.3);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 34, 0.000E0, 0.6);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 35, 0.056E0, 128.8);
--O   INSERT INTO CONST_NOT_NORM VALUES (300, 36, 0.038E0, 97.7);
--O-- PASS:0860 If 37 rows inserted from previous 37 inserts?
--O
--O   SELECT EPOCH FROM CONST_RAD
--O     WHERE LOC_ID = 100
--O     AND CONST_ID = 0;
--O-- PASS:0860 If EPOCH = 2.11 (+ or - 0.01)?
--O
--O   SELECT COUNT(*) 
--O     FROM CONST_RAD_NOT_NORM
--O     WHERE EPOCH > 6.2831853E0;
--O-- PASS:0860 If COUNT = 4?
--O
--O   INSERT INTO PENDING VALUES (
--O     300, TIMESTAMP( '1995-12-15 00:00:00-05:00'),
--O          TIMESTAMP( '1995-12-17 00:00:00-05:00'), 0);
--O-- PASS:0860 If 1 row inserted successfully?
--O
--O   INSERT INTO PENDING VALUES (
--O     101, TIMESTAMP( '2025-12-30 19:00:00-05:00'),
--O          TIMESTAMP( '2025-12-31 19:00:00-05:00'), 1);
--O-- PASS:0860 If ERROR - integrity constraint violation?
--O
--O   INSERT INTO PENDING VALUES (
--O     101, TIMESTAMP( '2025-12-30 19:00:00-05:00'),
--O          TIMESTAMP( '2025-12-31 18:59:59-05:00'), 1);
--O-- PASS:0860 If 1 row inserted successfully?
--O
--O   INSERT INTO PENDING VALUES (
--O     102, TIMESTAMP( '1993-12-31 19:00:00-05:00'),
--O       TIMESTAMP( '1994-01-02 00:00:00-05:00'), 2);
--O-- PASS:0860 If 1 row inserted successfully?
--O
--O   SELECT YEAR( CHECK_DATES)
--O     FROM CHECK_PTS WHERE JOB_ID = 2 AND FLAG = 0;
--O-- PASS:0860 If ERROR - integrity constraint violation?
--O
--O   SELECT YEAR( CHECK_DATES)
--O     FROM CHECK_PTS WHERE JOB_ID = 2 AND FLAG = 1;
--O-- PASS:0860 If xyear = 1994?
--O
--O   INSERT INTO DINNER_CLUB VALUES
--O     (0, TIME( '17:30:00'));
--O-- PASS:0860 If 1 row inserted successfully?
--O
--O   INSERT INTO DINNER_CLUB VALUES
--O     (1, CAST (TIME( '18:00:00') AS DINNERTIME));
--O-- PASS:0860 If 1 row inserted successfully?
--O
--O   INSERT INTO DINNER_CLUB VALUES
--O     (2, TIME( '19:30:00'));
--O-- PASS:0860 If ERROR - integrity constraint violation?
--O
--O   COMMIT WORK;
--O
--O   DROP DOMAIN EPOCH_NOT_NORM CASCADE;
--O   COMMIT WORK;
--O   DROP DOMAIN RAD_EPOCH_TYPE CASCADE;
--O   COMMIT WORK;
--O   DROP DOMAIN RAD_EPOCH_NOT_NORM CASCADE;
--O   COMMIT WORK;
--O   DROP DOMAIN TIDEDATE CASCADE;
--O   COMMIT WORK;
--O   DROP DOMAIN TIDETIMESTAMP CASCADE;
--O   COMMIT WORK;
--O   DROP DOMAIN DINNERTIME CASCADE;
--O   COMMIT WORK;
--O-- PASS:0860 If domains dropped successfully in 6 previous drops?
--O
--O   DROP TABLE CONST_NOT_NORM CASCADE;
--O   COMMIT WORK;
--O   DROP VIEW CONST_RAD CASCADE;
--O   COMMIT WORK;
--O   DROP TABLE PENDING CASCADE;
--O   COMMIT WORK;
--O   DROP TABLE DINNER_CLUB CASCADE;
--O   COMMIT WORK;
--O-- PASS:0860 If tables and view dropped in 4 previous drops?
--O
--O   DELETE FROM TIDES.LOCATIONS
--O     WHERE LOC_ID = 300;
--O-- PASS:0860 If delete completed successfully?
--O
--O   COMMIT WORK;
--O
--O-- END TEST >>> 0860 <<< END TEST
--O-- *********************************************
--O-- *************************************************////END-OF-MODULE

