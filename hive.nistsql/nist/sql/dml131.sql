-- MODULE  DML131  

-- SQL Test Suite, V6.0, Interactive SQL, dml131.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION FLATER            

   SELECT USER FROM HU.ECCO;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:0683 INFO_SCHEM:  Changes are visible!

   SELECT COUNT(*) FROM INFO_SCHEM.TABLES
  WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'VISCHANGE';
-- PASS:0683 If count = 0?

   SELECT COUNT(*) FROM INFO_SCHEM.COLUMNS
  WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'VISCHANGE';
-- PASS:0683 If count = 0?

   ROLLBACK WORK;

   CREATE TABLE VISCHANGE (C1 INT, C2 FLOAT);
-- PASS:0683 If table is created?

   COMMIT WORK;

   SELECT COUNT(*) FROM INFO_SCHEM.TABLES
  WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'VISCHANGE'
  AND TABLE_TYPE = 'BASE TABLE';
-- PASS:0683 If count = 1?

   SELECT COUNT(*) FROM INFO_SCHEM.COLUMNS
  WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'VISCHANGE';
-- PASS:0683 If count = 2?

   ROLLBACK WORK;

   ALTER TABLE VISCHANGE DROP C1 RESTRICT;
-- PASS:0683 If column is dropped?

   COMMIT WORK;

   SELECT COUNT(*) FROM INFO_SCHEM.COLUMNS
  WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'VISCHANGE';
-- PASS:0683 If count = 1?

   SELECT COUNT(*) FROM INFO_SCHEM.COLUMNS
  WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'VISCHANGE'
  AND COLUMN_NAME = 'C2';
-- PASS:0683 If count = 1?

   ROLLBACK WORK;

   DROP TABLE VISCHANGE CASCADE;
-- PASS:0683 If table is dropped?

   COMMIT WORK;

   SELECT COUNT(*) FROM INFO_SCHEM.TABLES
  WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'VISCHANGE';
-- PASS:0683 If count = 0?

   SELECT COUNT(*) FROM INFO_SCHEM.COLUMNS
  WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'VISCHANGE';
-- PASS:0683 If count = 0?

   ROLLBACK WORK;

-- END TEST >>> 0683 <<< END TEST

-- *********************************************

-- TEST:0684 INFO_SCHEM:  Visibility to other users!

   SELECT COUNT(*) FROM INFO_SCHEM.TABLES
  WHERE TABLE_SCHEM = 'HU' AND TABLE_NAME = 'WORKS'
  AND TABLE_TYPE = 'BASE TABLE';
-- PASS:0684 If count = 1?

   SELECT COUNT(*) FROM INFO_SCHEM.COLUMNS
  WHERE TABLE_SCHEM = 'HU' AND TABLE_NAME = 'WORKS';
-- PASS:0684 If count = 3?

   SELECT COUNT(*) FROM INFO_SCHEM.TABLES
  WHERE TABLE_SCHEM = 'HU' AND TABLE_NAME = 'TESTREPORT'
  AND TABLE_TYPE = 'VIEW';
-- PASS:0684 If count = 1?

  SELECT COUNT(*) FROM INFO_SCHEM.VIEWS
  WHERE TABLE_SCHEM = 'HU' AND TABLE_NAME = 'TESTREPORT';
-- PASS:0684 If count = 1?

  SELECT COUNT(*) FROM INFO_SCHEM.COLUMNS
  WHERE TABLE_SCHEM = 'HU' AND TABLE_NAME = 'TESTREPORT';
-- PASS:0684 If count = 3?

  SELECT COUNT(*) FROM INFO_SCHEM.TABLES
  WHERE TABLE_SCHEM = 'HU' AND TABLE_NAME = 'VTABLE'
  AND TABLE_TYPE = 'BASE TABLE';
-- PASS:0684 If count = 1?

  SELECT COUNT(*) FROM INFO_SCHEM.COLUMNS
  WHERE TABLE_SCHEM = 'HU' AND TABLE_NAME = 'VTABLE';
-- PASS:0684 If count = 1?

  SELECT COUNT(*) FROM INFO_SCHEM.TABLES
  WHERE TABLE_SCHEM = 'HU' AND TABLE_NAME = 'FF';
-- PASS:0684 If count = 0?

  SELECT COUNT(*) FROM INFO_SCHEM.COLUMNS
  WHERE TABLE_SCHEM = 'HU' AND TABLE_NAME = 'FF';
-- PASS:0684 If count = 0?

  ROLLBACK WORK;

-- END TEST >>> 0684 <<< END TEST

-- *********************************************

-- TEST:0685 INFO_SCHEM:  Privileges and privilege views!

   SELECT COUNT(*)
  FROM INFO_SCHEM.TABLE_PRIVILEGES
  WHERE TABLE_SCHEM = 'FLATER' AND GRANTOR <> '_SYSTEM'
  AND GRANTEE = 'FLATER';
-- PASS:0685 If count = 0?

   SELECT COUNT(*)
  FROM INFO_SCHEM.COLUMN_PRIVILEGES
  WHERE TABLE_SCHEM = 'FLATER' AND GRANTOR <> '_SYSTEM'
  AND GRANTEE = 'FLATER';
-- PASS:0685 If count = 0?

   SELECT COUNT(*)
  FROM INFO_SCHEM.TABLE_PRIVILEGES
  WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'VS1'
  AND GRANTOR = 'FLATER' AND GRANTEE = 'SCHANZLE'
  AND IS_GRANTABLE = 'NO';
-- PASS:0685 If count = 5?

   SELECT COUNT(*)
  FROM INFO_SCHEM.COLUMN_PRIVILEGES
  WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'VS1'
  AND GRANTOR = 'FLATER' AND GRANTEE = 'SCHANZLE'
  AND IS_GRANTABLE = 'NO';
-- PASS:0685 If count = 10?

   SELECT COUNT(*)
  FROM INFO_SCHEM.TABLE_PRIVILEGES
  WHERE TABLE_SCHEM = 'HU' AND TABLE_NAME = 'PROJ'
  AND GRANTOR = 'HU' AND GRANTEE = 'PUBLIC'
  AND IS_GRANTABLE = 'NO';
-- PASS:0685 If count = 1?

   SELECT COUNT(*)
  FROM INFO_SCHEM.COLUMN_PRIVILEGES
  WHERE TABLE_SCHEM = 'HU' AND TABLE_NAME = 'PROJ'
  AND GRANTOR = 'HU' AND GRANTEE = 'PUBLIC'
  AND IS_GRANTABLE = 'NO';
-- PASS:0685 If count = 5?

   SELECT COUNT(*)
  FROM INFO_SCHEM.TABLE_PRIVILEGES
  WHERE TABLE_SCHEM = 'HU' AND TABLE_NAME = 'TESTREPORT'
  AND GRANTOR = 'HU' AND GRANTEE = 'PUBLIC'
  AND IS_GRANTABLE = 'YES' AND PRIVILEGE_TYPE = 'INSERT';
-- PASS:0685 If count = 1?

  SELECT COUNT(*)
  FROM INFO_SCHEM.COLUMN_PRIVILEGES
  WHERE TABLE_SCHEM = 'HU' AND TABLE_NAME = 'TESTREPORT'
  AND GRANTOR = 'HU' AND GRANTEE = 'PUBLIC'
  AND IS_GRANTABLE = 'YES' AND PRIVILEGE_TYPE = 'INSERT';
-- PASS:0685 If count = 3?

  SELECT COUNT(*)
  FROM INFO_SCHEM.COLUMN_PRIVILEGES
  WHERE TABLE_SCHEM = 'HU' AND TABLE_NAME = 'TESTREPORT'
  AND GRANTOR = 'HU' AND (GRANTEE <> 'PUBLIC'
  OR IS_GRANTABLE <> 'YES' OR PRIVILEGE_TYPE <> 'INSERT');
-- PASS:0685 If count = 0?

  SELECT COUNT(*)
  FROM INFO_SCHEM.TABLE_PRIVILEGES
  WHERE TABLE_SCHEM = 'HU' AND TABLE_NAME = 'VTABLE'
  AND GRANTOR = 'HU';
-- PASS:0685 If count = 0?

  SELECT COUNT(*)
  FROM INFO_SCHEM.COLUMN_PRIVILEGES
  WHERE TABLE_SCHEM = 'HU' AND TABLE_NAME = 'VTABLE'
  AND GRANTOR = 'HU';
-- PASS:0685 If count = 1?

  SELECT COUNT(*)
  FROM INFO_SCHEM.COLUMN_PRIVILEGES
  WHERE TABLE_SCHEM = 'HU' AND TABLE_NAME = 'VTABLE'
  AND GRANTOR = 'HU' AND COLUMN_NAME = 'COL1'
  AND PRIVILEGE_TYPE = 'UPDATE' AND
  IS_GRANTABLE = 'NO' AND GRANTEE = 'FLATER';
-- PASS:0685 If count = 1?

  SELECT COUNT(*)
  FROM INFO_SCHEM.TABLE_PRIVILEGES
  WHERE TABLE_SCHEM = 'HU' AND TABLE_NAME = 'FF';
-- PASS:0685 If count = 0?

  SELECT COUNT(*)
  FROM INFO_SCHEM.COLUMN_PRIVILEGES
  WHERE TABLE_SCHEM = 'HU' AND TABLE_NAME = 'FF';
-- PASS:0685 If count = 0?

  SELECT COUNT(*)
  FROM INFO_SCHEM.TABLE_PRIVILEGES
  WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'BASE_WCOV'
  AND GRANTOR = 'FLATER' AND GRANTEE = 'CUGINI';
-- PASS:0685 If count = 0?

  SELECT COUNT(*)
  FROM INFO_SCHEM.COLUMN_PRIVILEGES
  WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'BASE_WCOV'
  AND GRANTOR = 'FLATER' AND GRANTEE = 'CUGINI';
-- PASS:0685 If count = 0?

  ROLLBACK WORK;

  GRANT DELETE ON BASE_WCOV TO CUGINI;

  COMMIT WORK;

   SELECT COUNT(*)
  FROM INFO_SCHEM.TABLE_PRIVILEGES
  WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'BASE_WCOV'
  AND GRANTOR = 'FLATER' AND GRANTEE = 'CUGINI'
  AND PRIVILEGE_TYPE = 'DELETE' AND IS_GRANTABLE = 'NO';
-- PASS:0685 If count = 1?

   SELECT COUNT(*)
  FROM INFO_SCHEM.TABLE_PRIVILEGES
  WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'BASE_WCOV'
  AND GRANTOR = 'FLATER' AND GRANTEE = 'CUGINI';
-- PASS:0685 If count = 1?

   SELECT COUNT(*)
  FROM INFO_SCHEM.COLUMN_PRIVILEGES
  WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'BASE_WCOV'
  AND GRANTOR = 'FLATER' AND GRANTEE = 'CUGINI'
  AND PRIVILEGE_TYPE = 'DELETE' AND IS_GRANTABLE = 'NO';
-- PASS:0685 If count = 1?

   SELECT COUNT(*)
  FROM INFO_SCHEM.COLUMN_PRIVILEGES
  WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'BASE_WCOV'
  AND GRANTOR = 'FLATER' AND GRANTEE = 'CUGINI';
-- PASS:0685 If count = 1?

   ROLLBACK WORK;

   REVOKE DELETE ON BASE_WCOV FROM CUGINI CASCADE;

   COMMIT WORK;

   SELECT COUNT(*)
  FROM INFO_SCHEM.TABLE_PRIVILEGES
  WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'BASE_WCOV'
  AND GRANTOR = 'FLATER' AND GRANTEE = 'CUGINI';
-- PASS:0685 If count = 0?

   SELECT COUNT(*)
  FROM INFO_SCHEM.COLUMN_PRIVILEGES
  WHERE TABLE_SCHEM = 'FLATER' AND TABLE_NAME = 'BASE_WCOV'
  AND GRANTOR = 'FLATER' AND GRANTEE = 'CUGINI';
-- PASS:0685 If count = 0?

   ROLLBACK WORK;

-- END TEST >>> 0685 <<< END TEST

-- *********************************************

-- TEST:0686 INFO_SCHEM:  Primary key enh. is not null!

   CREATE TABLE FEAT16 (
  EMPNUM INT PRIMARY KEY,
  PNUM   INT UNIQUE);
-- PASS:0686 If table is created?

   COMMIT WORK;

   SELECT COUNT(*)
  FROM INFO_SCHEM.COLUMNS WHERE TABLE_SCHEM = 'FLATER'
  AND TABLE_NAME = 'FEAT16' AND COLUMN_NAME = 'EMPNUM'
  AND IS_NULLABLE = 'NO';
-- PASS:0686 If count = 1?

   SELECT COUNT(*)
  FROM INFO_SCHEM.COLUMNS WHERE TABLE_SCHEM = 'FLATER'
  AND TABLE_NAME = 'FEAT16' AND COLUMN_NAME = 'PNUM'
  AND IS_NULLABLE = 'YES';
-- PASS:0686 If count = 1?

   ROLLBACK WORK;

   DROP TABLE FEAT16 CASCADE;
-- PASS:0686 If table is dropped?

   COMMIT WORK;

-- END TEST >>> 0686 <<< END TEST

-- *********************************************

-- TEST:0687 INFO_SCHEM:  Multiple schemas per user!

   SELECT COUNT(*)
  FROM INFO_SCHEM.SCHEMATA
  WHERE SCHEM_NAME = 'SHIRLEY_HURWITZ' AND
  SCHEM_OWNER = 'FLATER';
-- PASS:0687 If count = 1?

   SELECT COUNT(*)
  FROM INFO_SCHEM.SCHEMATA
  WHERE SCHEM_NAME = 'LEN_GALLAGHER' AND
  SCHEM_OWNER = 'FLATER';
-- PASS:0687 If count = 1?

   COMMIT WORK;

-- END TEST >>> 0687 <<< END TEST

-- *************************************************////END-OF-MODULE
