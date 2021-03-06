-- SQL Test Suite, V6.0, Schema Definition, cts5sch2.sql
-- 59-byte ID
-- TEd Version #
-- date_time print
-- ***************************************************************
-- ****** THIS FILE SHOULD BE RUN UNDER SCHEMA ID CTS1 ******
-- ***************************************************************

--  The following command is supported only at INTERMEDIATE level
  CREATE SCHEMA CTS1;
use CTS1;
--  The following command should be used if ENTRY level rather than
--  intermediate is supported.
--  CREATE SCHEMA AUTHORIZATION CTS1;

-- ************* create character set statements *****

   CREATE CHARACTER SET CS GET SQL_TEXT;

-- ************* create table statements *************


  CREATE TABLE ECCO (C1 CHAR(2));

  CREATE TABLE FIPS1
   (FIPS_TEST CHAR(20));


  CREATE TABLE STAFF
  (EMPNUM   CHAR(3) NOT NULL UNIQUE,
   EMPNAME  CHAR(20),
   GRADE    DECIMAL(4),
   CITY     CHAR(15));

  CREATE TABLE WORKS
  (EMPNUM   CHAR(3) NOT NULL,
   PNUM     CHAR(3) NOT NULL,
   HOURS    DECIMAL(5),
   UNIQUE(EMPNUM,PNUM));

  CREATE TABLE STAFF1
  (EMPNUM    CHAR(3) NOT NULL,
  EMPNAME  CHAR(20),
  GRADE DECIMAL(4),
  CITY   CHAR(15));


  CREATE TABLE STAFF4
  (EMPNUM    CHAR(3) NOT NULL,
  EMPNAME  CHAR(20),
  GRADE DECIMAL(4),
  CITY   CHAR(15));

  CREATE TABLE VTABLE
  (COL1   INTEGER,
   COL2   INTEGER,
   COL3   INTEGER,
   COL4   INTEGER,
   COL5   DECIMAL(7,2));


  CREATE TABLE STAFF3
  (EMPNUM   CHAR(3) NOT NULL,
   EMPNAME  CHAR(20),
   GRADE    DECIMAL(4),
   CITY     CHAR(15),
   UNIQUE (EMPNUM));


  CREATE TABLE PROJ3
  (PNUM     CHAR(3) NOT NULL,
   PNAME    CHAR(20),
   PTYPE    CHAR(6),
   BUDGET   DECIMAL(9),
   CITY     CHAR(15),
   UNIQUE (PNUM));


 CREATE TABLE STAFF7 (EMPNUM    CHAR(3) NOT NULL,
  EMPNAME  CHAR(20),
  GRADE DECIMAL(4),
  CITY   CHAR(15),
  PRIMARY KEY (EMPNUM),
  CHECK (GRADE BETWEEN 1 AND 20));

   CREATE TABLE WORKS3a
   (EMPNUM   CHAR(3) NOT NULL,
   PNUM     CHAR(3) NOT NULL,
   HOURS    DECIMAL(5),
   FOREIGN KEY (PNUM) REFERENCES PROJ3(PNUM));


   CREATE TABLE STAFFa
   ( HOURS   INTEGER,
     SALARY  DECIMAL(6),
     EMPNUM  CHAR(3),
     PNUM    DECIMAL(4),
     EMPNAME CHAR(20));

  CREATE TABLE STAFFb
   ( SALARY   DECIMAL(6),
     EMPNAME  CHAR(20),
     HOURS    INTEGER,
     PNUM     CHAR(3),
     CITY     CHAR(15),
     SEX      CHAR);

  CREATE TABLE STAFFc
  (  EMPNUM   CHAR(3) NOT NULL,
     EMPNAME  CHAR(20),
     GRADE    DECIMAL(4),
     CITY     CHAR(15),
     MGR      CHAR(3),
     UNIQUE   (EMPNUM));

  CREATE TABLE STAFFd
  (  EMPNUM   CHAR(3) NOT NULL,
     GRADE    DECIMAL(4),
     MGR      CHAR(3));

  CREATE TABLE STAFF_CTS
  (  PNUM   CHAR(3),
     CITY   CHAR(15),
     GRADE  DECIMAL(4),
     EMPNAME CHAR(20));

  CREATE TABLE STAFFz
  ( EMPNUM CHAR(3) REFERENCES STAFF3(EMPNUM),
    SALARY DECIMAL(6) CHECK (SALARY > 0));

  CREATE TABLE PROJ_DURATION
  ( MONTHS  INTEGER,
    TIME_LEFT   INTEGER,
    EMP_HOURS      INTEGER,
    CHECK (MONTHS > 0));

  CREATE TABLE STAFF_CTS2
  (EMPNUM    CHAR(3) NOT NULL,
  EMPNAME  CHAR(20),
  GRADE DECIMAL(4),
  CITY   CHAR(15));

  CREATE TABLE EMPLOYEES2
  (  name     CHAR(10),
     empno    INTEGER);

  CREATE TABLE A
  (   p   INTEGER,
      q   INTEGER );

  CREATE TABLE TT
  (TTA   INTEGER,
   TTB   INTEGER,
   TTC   INTEGER);

  CREATE TABLE TU
  (TUD   CHAR(2),
   TUE   INTEGER);

  CREATE TABLE TT2
  (TTA INTEGER,
   TTB INTERVAL YEAR TO MONTH,
   TTC DECIMAL(6,0));

  CREATE TABLE TV
  (A   INTEGER,
   B   CHAR);

  CREATE TABLE TW
  (D   CHAR,
   E   INTEGER);

  CREATE TABLE TX
  (TX1     INTEGER,
   TX2     CHARACTER(5),
   TX3     CHARACTER VARYING (10));

  CREATE TABLE COMP_BUDG
  (P_REF   CHAR(3) NOT NULL,
   BUDGET  DECIMAL(20),
   HOURS   INTEGER,
   SALARY  DECIMAL(6),
   FOREIGN KEY (P_REF) REFERENCES CTS2.PROJ_MAN(P_REF));

  CREATE TABLE PROJ_STATUS
  ( MGR    CHAR(15)  REFERENCES CTS2.PROJ_MAN(MGR),
    P_REF  CHAR(3),
    ONTIME CHAR,
    BUDGET DECIMAL(20),
    COST   DECIMAL(20));

  CREATE TABLE DATA_TYPE
  (  NUM   NUMERIC,
     DEC   DECIMAL,
     ING   INTEGER,
     SMA   SMALLINT,
     FLO   FLOAT,
     REA   REAL,
     DOU   DOUBLE PRECISION);

  CREATE TABLE TTIME_BASE
  (PK           INTEGER,
   TT           TIME,
   TS           TIMESTAMP,
   TT2          TIME WITH TIME ZONE,
   TS2          TIMESTAMP WITH TIME ZONE,
   PRIMARY KEY (PK));
    
  CREATE TABLE CL_DATA_TYPE
   (CL_CHAR CHAR(10),
    CL_NUM NUMERIC,
    CL_DEC DECIMAL,
    CL_REAL REAL);

  CREATE TABLE CL_EMPLOYEE 
      (EMPNUM  NUMERIC(5) PRIMARY KEY,
       DEPTNO  CHAR(3),
       LOC     CHAR(15),
       EMPNAME CHAR(20),
       SALARY  DECIMAL(6),
       GRADE   DECIMAL(4),
       HOURS   DECIMAL(5));

  CREATE TABLE TEST6740A
   (TNUM NUMERIC(4),
    TCHARA CHAR(10));

  CREATE TABLE TEST6740B
   (TNUM NUMERIC(4),
    TCHARB CHAR(10));

  CREATE TABLE TEST6740C
   (TNUMERIC NUMERIC(4),
    TCHAR CHAR(10));

  CREATE TABLE TEST6840A
   (NUM_A NUMERIC(4),
    CH_A CHAR(10));

  CREATE TABLE TEST6840B
   (NUM_B NUMERIC(4),
    CH_B CHAR(10));

  CREATE TABLE TEST6840C
   (NUM_C1 NUMERIC(4),
    CH_C1 CHAR(10),
    NUM_C2 NUMERIC(4),
    CH_C2 CHAR(10));

  CREATE TABLE TEST12849B
   (col_num3 NUMERIC(3),
    PRIMARY KEY (col_num3));

  CREATE TABLE TEST12849A
   (col_num1   NUMERIC(5) PRIMARY KEY,
    col_str1   VARCHAR(15) NOT NULL,
    col_str2   VARCHAR(10),
    col_num2   NUMERIC(5) CONSTRAINT constr_1 REFERENCES TEST12849A,
    col_str3   VARCHAR(25),
    col_num3   NUMERIC(7,2),
    col_num4   NUMERIC(3) NOT NULL
      CONSTRAINT constr_3 REFERENCES TEST12849B
      ON DELETE CASCADE,
    CONSTRAINT constr_2 UNIQUE (col_str1, col_str2));

  CREATE TABLE T4
   (STR110 CHAR(110) NOT NULL,
    NUM6   NUMERIC(6) NOT NULL,
    COL3   CHAR(10),
    COL4 CHAR(20),
    UNIQUE(STR110,NUM6));


CREATE TABLE EMPTY740
(COL_1   CHAR(10),
 COL_2   VARCHAR(5),
 COL_3   NUMERIC(5),
 COL_4   DECIMAL(6),
 COL_5   TIME);


CREATE TABLE TABX760
  ( DEPTNO  NUMERIC(5) UNIQUE NOT NULL,
  EMPNAME CHAR(20)   UNIQUE NOT NULL, 
  SALARY  DECIMAL(7));

CREATE TABLE TABCS
   ( COLUN   NUMERIC(5) UNIQUE,
     COLSTR1 CHAR(10)    CHARACTER SET CS,
     COLSTR2 VARCHAR(10) CHARACTER SET CS);


  CREATE TABLE CL_STANDARD
   (
    COL_NUM1 NUMERIC(4),
    COL_CH1  CHAR(10),
    COL_NUM2 NUMERIC(4),
    COL_CH2  CHAR(10));

  CREATE TABLE TABLE728a 
   (
    C1 CHAR(10),
    C2 CHAR(10));

  CREATE TABLE TABLE728b
   (
     COL_1 CHAR(10),
     COL_2 CHAR(10));


  CREATE TABLE TAB734
   ( CSTR1 NCHAR(10),
     CSTR2 NCHAR VARYING(12));


-- LATIN1 is not required by SQL-92 DWF 1996-02-21
--  CREATE TABLE TABLATIN1
--  ( COL1 CHARACTER(10) CHARACTER SET LATIN1,
--    COL2 CHAR(12)      CHARACTER SET LATIN1,
--    COL3 VARCHAR(15)   CHARACTER SET LATIN1,
--    COL4 NUMERIC(5));

  CREATE TABLE ET
  (col1    CHAR(3),
   col2    CHAR(20),
   col3    DECIMAL(4),
   col4    CHAR(15),
   col5    INTEGER,
   col6    INTEGER);

  CREATE TABLE TTSTORE
  (numx    INTEGER,
   colthu  INTEGER,
   coltmu  INTEGER,
   TT      TIME);

  CREATE TABLE TTSTORE2
  (num     INTEGER,
   colthu  INTEGER,
   coltmu  INTEGER,
   TT2     TIME WITH TIME ZONE);

  CREATE TABLE CONCATBUF (ZZ CHAR(240));


CREATE VIEW TESTREPORT AS
    SELECT TESTNO, RESULT, TESTTYPE
    FROM HU.TESTREPORT;

CREATE VIEW TTIME (PK, TT, TS) AS
    SELECT PK, TT, TS
    FROM TTIME_BASE;

CREATE VIEW TTIME2 (PK, TT2, TS2) AS
    SELECT PK, TT2, TS2
    FROM TTIME_BASE;

CREATE VIEW TTIME3 (PK, TT, TT2, TS2) AS
    SELECT PK, TT, TT2, TS2
    FROM TTIME_BASE;

-- ************* create domain statements ***********

   CREATE DOMAIN esal AS INTEGER 
   CHECK (VALUE > 500);

   CREATE DOMAIN atom CHARACTER
   CHECK ('a' <= VALUE)
   CHECK ('m' >= VALUE);

   CREATE DOMAIN smint INTEGER
   CHECK (1<= VALUE)
   CHECK (100 >= VALUE);

-- ************* grant statements follow *************

   GRANT ALL PRIVILEGES ON CONCATBUF TO PUBLIC;

   GRANT SELECT ON CTS1.ECCO TO PUBLIC;

   GRANT INSERT ON TESTREPORT TO PUBLIC WITH GRANT OPTION;

   GRANT SELECT ON CTS1.DATA_TYPE TO CTS4;

-- ************* End of Schema *************
