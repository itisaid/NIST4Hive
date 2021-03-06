-- ************* NON-STANDARD VERSION ************* 
-- Schema for reporting structure of SQL Test Suite, Version 5.0
-- Followed by sample data and queries

-- =========================================================
--   Static tables to define the test suite structure
-- =========================================================

-- This table is an enumeration of all features 
--    and collections of features (profiles) to be tested.
-- This is a reference table of codes (FEATURE1) and a
--    lookup table of names.
-- A profile has the value P in column1 of FEATURE1.
-- A logical profile has the value L in column1 of FEATURE1.
-- A lowest-level "leaf" feature is numeric in column1 of FEATURE1.
-- [A logical profile is a convenient grouping of features or
--   profiles, for purposes of recursion, but not reporting.].

   CREATE TABLE REPORTFEATURE
     (FEATURE1      CHAR(4) NOT NULL,
      FEATURENAME   CHAR(30) NOT NULL);

   CREATE UNIQUE INDEX REPORTFEATURE_IND ON REPORTFEATURE (FEATURE1);

-- This table describes the reporting structure for SQL testing;
--   i.e., the network of relationships between REPORTFEATURE rows.
-- Each row is a directed arc in the network.

-- Profiles and logical profiles are nodes in the hierarchy.

   CREATE TABLE IMPLICATION
     (PARENT_F  CHAR(4) NOT NULL,
      CHILD_F   CHAR(4) NOT NULL);

-- List of programs, authorization identifiers, and
--  special notes on how to run each the program.
-- P_NOTE indicates whether a test is a concurrency test,
--  requires a subroutine, etc.

   CREATE TABLE TESTPROG
     (PROG      CHAR(6) NOT NULL,
      AUTHID    CHAR(18) NOT NULL,
      P_NOTE    CHAR(10));

   CREATE UNIQUE INDEX TESTPROG_IND ON TESTPROG (PROG);

-- List of test cases, descriptions, containing program, and
--  special notes on operational problems, such as
--  may not compile, may cause segmentation error, requires
--  visual inspection (ergo no pass/fail in TESTREPORT), etc.

   CREATE TABLE TESTCASE
     (TESTNO    CHAR(4) NOT NULL,
      DESCR     CHAR(50) NOT NULL,
      PROG      CHAR(6) NOT NULL,
      T_NOTE    CHAR(10),
      ISQL_CT   DECIMAL(2) NOT NULL);

   CREATE UNIQUE INDEX TESTCASE_IND ON TESTCASE (TESTNO);

-- Each test is for one or more features.
-- This table describes the test cases in the programs.

   CREATE TABLE TESTFEATURE
     (TESTNO    CHAR(4) NOT NULL,
      FEATURE1  CHAR(4) NOT NULL);

   CREATE UNIQUE INDEX TESTFEATURE_IND ON TESTFEATURE (TESTNO, FEATURE1);

-- On-line cross reference to SQL-92 (population deferred)
-- Sequence number is decimal to facilitate adding references
--  between existing references without renumbering.
-- [same idea as Dewey Decimal system used in libraries]

   CREATE TABLE TESTREFERENCE
     (TESTNO    CHAR(4) NOT NULL,
      SEQ       DECIMAL (6,4) NOT NULL,
      TESTREF   CHAR(50) NOT NULL);

   CREATE UNIQUE INDEX TESTREFERENCE_IND ON TESTREFERENCE (TESTNO, SEQ);


-- =========================================================
--   Tables to specify vendor claims follow:
-- =========================================================

-- Features supported for this testing:

   CREATE TABLE FEATURE_CLAIMED
     (FEATURE1  CHAR(4) NOT NULL);

   CREATE UNIQUE INDEX FEATURE_CLAIMED_I ON FEATURE_CLAIMED (FEATURE1);

-- Bindings supported for this testing:

   CREATE TABLE BINDING_CLAIMED
     (BINDING1  CHAR(3) NOT NULL);

   CREATE UNIQUE INDEX BINDING_CLAIMED_I ON BINDING_CLAIMED (BINDING1);

-- =========================================================
--   Tables to generate vendor-specific requirements follow:
-- =========================================================

-- Features required, to be derived recursively,
--  including claim to be tested -- C1,
--            lowest reporting profile -- P1, and
--            lowest recursive link -- L1.
-- F1 is the feature to be tested.

   CREATE TABLE F_REQ
     (C1           CHAR(4) NOT NULL,
      P1           CHAR(4) NOT NULL,
      F1           CHAR(4) NOT NULL);

-- Working version of F_REQ, 
--   needed because an INSERT cannot be self-referencing.

   CREATE TABLE F_TEMP
     (C1           CHAR(4) NOT NULL,
      P1           CHAR(4) NOT NULL,
      L1           CHAR(4) NOT NULL);

   CREATE TABLE R_STRUCTURE
     (C1        CHAR(4) NOT NULL,
      P1        CHAR(4) NOT NULL,
      TESTNO    CHAR(4) NOT NULL);

-- Tests selected for this validation,
--   corresponding to the features selected and
--   corresponding to the bindings selected.
-- Result will be derived later from TESTREPORT.

   CREATE TABLE T_REQ
     (TESTNO    CHAR(4) NOT NULL,
      PROG      CHAR(6) NOT NULL,
      BINDING1  CHAR(3) NOT NULL,
      REQOPTNA  CHAR(3) NOT NULL,
      RESULT    CHAR(4));

COMMIT WORK;

