-- MODULE  DML167  

-- SQL Test Suite, V6.0, Interactive SQL, dml167.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION FLATER            

   SELECT USER FROM HU.ECCO;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print


-- TEST:0874 INFORMATION SCHEMA catalog columns!

   SELECT COUNT (DISTINCT TABLE_CATALOG)
     FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS;
-- PASS:0874 If COUNT = 1?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
     WHERE TABLE_CATALOG <> (
       SELECT CATALOG_NAME FROM
       INFORMATION_SCHEMA.INFORMATION_SCHEMA_CATALOG_NAME)
     OR TABLE_CATALOG IS NULL;
-- PASS:0874 If COUNT = 0?

   SELECT COUNT (DISTINCT CONSTRAINT_CATALOG)
     FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE;
-- PASS:0874 If COUNT = 1?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
     WHERE CONSTRAINT_CATALOG <> (
       SELECT CATALOG_NAME FROM
       INFORMATION_SCHEMA.INFORMATION_SCHEMA_CATALOG_NAME)
     OR CONSTRAINT_CATALOG IS NULL
     OR TABLE_CATALOG IS NULL;
-- PASS:0874 If COUNT = 0?

   SELECT COUNT (DISTINCT VIEW_CATALOG)
     FROM INFORMATION_SCHEMA.VIEW_TABLE_USAGE;
-- PASS:0874 If COUNT = 1?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.VIEW_TABLE_USAGE
     WHERE VIEW_CATALOG <> (
       SELECT CATALOG_NAME FROM
       INFORMATION_SCHEMA.INFORMATION_SCHEMA_CATALOG_NAME)
     OR VIEW_CATALOG IS NULL
     OR TABLE_CATALOG IS NULL;
-- PASS:0874 If COUNT = 0?

   SELECT COUNT (DISTINCT VIEW_CATALOG)
     FROM INFORMATION_SCHEMA.VIEW_COLUMN_USAGE;
-- PASS:0874 If COUNT = 1?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.VIEW_COLUMN_USAGE
     WHERE VIEW_CATALOG <> (
       SELECT CATALOG_NAME FROM
       INFORMATION_SCHEMA.INFORMATION_SCHEMA_CATALOG_NAME)
     OR VIEW_CATALOG IS NULL
     OR TABLE_CATALOG IS NULL;
-- PASS:0874 If COUNT = 0?

   SELECT COUNT (DISTINCT CONSTRAINT_CATALOG)
     FROM INFORMATION_SCHEMA.CONSTRAINT_TABLE_USAGE;
-- PASS:0874 If COUNT = 1?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.CONSTRAINT_TABLE_USAGE
     WHERE CONSTRAINT_CATALOG <> (
       SELECT CATALOG_NAME FROM
       INFORMATION_SCHEMA.INFORMATION_SCHEMA_CATALOG_NAME)
     OR CONSTRAINT_CATALOG IS NULL
     OR TABLE_CATALOG IS NULL;
-- PASS:0874 If COUNT = 0?

   SELECT COUNT (DISTINCT CONSTRAINT_CATALOG)
     FROM INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE;
-- PASS:0874 If COUNT = 1?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE
     WHERE CONSTRAINT_CATALOG <> (
       SELECT CATALOG_NAME FROM
       INFORMATION_SCHEMA.INFORMATION_SCHEMA_CATALOG_NAME)
     OR CONSTRAINT_CATALOG IS NULL
     OR TABLE_CATALOG IS NULL;
-- PASS:0874 If COUNT = 0?

   SELECT COUNT (DISTINCT DOMAIN_CATALOG)
     FROM INFORMATION_SCHEMA.COLUMN_DOMAIN_USAGE;
-- PASS:0874 If COUNT = 1?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.COLUMN_DOMAIN_USAGE
     WHERE DOMAIN_CATALOG <> (
       SELECT CATALOG_NAME FROM
       INFORMATION_SCHEMA.INFORMATION_SCHEMA_CATALOG_NAME)
     OR DOMAIN_CATALOG IS NULL
     OR TABLE_CATALOG IS NULL;
-- PASS:0874 If COUNT = 0?

   SELECT COUNT (DISTINCT CONSTRAINT_CATALOG)
     FROM INFORMATION_SCHEMA.DOMAIN_CONSTRAINTS;
-- PASS:0874 If COUNT = 1?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.DOMAIN_CONSTRAINTS
     WHERE CONSTRAINT_CATALOG <> (
       SELECT CATALOG_NAME FROM
       INFORMATION_SCHEMA.INFORMATION_SCHEMA_CATALOG_NAME)
     OR CONSTRAINT_CATALOG IS NULL
     OR DOMAIN_CATALOG IS NULL;
-- PASS:0874 If COUNT = 0?

   COMMIT WORK;

-- END TEST >>> 0874 <<< END TEST
-- *********************************************

-- TEST:0875 INFORMATION_SCHEMA column coverage!

   CREATE TABLE TAB1 (
     C1 INT, C2 INT, C3 INT,
     PRIMARY KEY (C1, C2));
-- PASS:0875 If table created successfully?

   COMMIT WORK;

   CREATE TABLE TAB2 (
     D1 INT PRIMARY KEY, D2 INT, D3 INT,
     FOREIGN KEY (D2, D3) REFERENCES TAB1);
-- PASS:0875 If table created successfully?

   COMMIT WORK;

   SELECT ORDINAL_POSITION 
     FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
     WHERE COLUMN_NAME = 'CONST_ID'
     AND TABLE_NAME = 'CONSTITUENTS'
     AND TABLE_SCHEMA = 'TIDES';
-- PASS:0875 If ORDINAL_POSITION = 2?

   SELECT ORDINAL_POSITION 
     FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
     WHERE COLUMN_NAME = 'D2'
     AND TABLE_NAME = 'TAB2'
     AND TABLE_SCHEMA = 'FLATER';
-- PASS:0875 If ORDINAL_POSITION = 1?

   SELECT ORDINAL_POSITION 
     FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
     WHERE COLUMN_NAME = 'D1'
     AND TABLE_NAME = 'TAB2'
     AND TABLE_SCHEMA = 'FLATER';
-- PASS:0875 If ORDINAL_POSITION = 1?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE
     WHERE TABLE_NAME = 'TAB1'
     AND COLUMN_NAME = 'C1'
     AND TABLE_SCHEMA = 'FLATER'
     AND CONSTRAINT_SCHEMA IS NOT NULL;
-- PASS:0875 If COUNT = 2?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.DOMAIN_CONSTRAINTS;
-- PASS:0875 If this COUNT = the next COUNT?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.DOMAIN_CONSTRAINTS
     WHERE (IS_DEFERRABLE = 'NO' AND INITIALLY_DEFERRED = 'NO') OR
     (IS_DEFERRABLE = 'YES' AND (INITIALLY_DEFERRED = 'NO' OR
     INITIALLY_DEFERRED = 'YES'));
-- PASS:0875 If this COUNT = the previous COUNT?

   COMMIT WORK;

   DROP TABLE TAB1 CASCADE;
-- PASS:0875 If table dropped successfully?

   COMMIT WORK;

   DROP TABLE TAB2 CASCADE;
-- PASS:0875 If table dropped successfully?

   COMMIT WORK;

-- END TEST >>> 0875 <<< END TEST
-- *********************************************
-- *************************************************////END-OF-MODULE
