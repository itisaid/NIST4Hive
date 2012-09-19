-- MODULE  IST003  

-- SQL Test Suite, V6.0, Interactive SQL, ist003.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION FLATER            

   SELECT USER FROM HU.ECCO;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:0605 INFO_SCHEM.COLUMNS definition!

   SELECT COUNT(*)
FROM INFO_SCHEM.COLUMNS
WHERE ORDINAL_POSITION IS NULL;
-- PASS:0605 If count = 0?

   SELECT COUNT(*)
FROM INFO_SCHEM.COLUMNS
WHERE TABLE_SCHEM IS NULL
OR TABLE_NAME IS NULL
OR COLUMN_NAME IS NULL;
-- PASS:0605 If count = 0?

   SELECT COUNT(*)
FROM INFO_SCHEM.COLUMNS;
-- NOTE:0605 Save this result; compare with next two statements.

   SELECT COUNT(*)
FROM INFO_SCHEM.COLUMNS A,
 INFO_SCHEM.COLUMNS B
WHERE A.TABLE_SCHEM = B.TABLE_SCHEM
AND A.TABLE_NAME = B.TABLE_NAME
AND A.COLUMN_NAME = B.COLUMN_NAME;
-- PASS:0605 If count is same as that of previous statement?

   SELECT COUNT(*)
FROM INFO_SCHEM.COLUMNS A,
 INFO_SCHEM.COLUMNS B
WHERE A.TABLE_SCHEM = B.TABLE_SCHEM
AND A.TABLE_NAME = B.TABLE_NAME
AND A.ORDINAL_POSITION = B.ORDINAL_POSITION;
-- PASS:0605 If count is same as that of previous statement?

   SELECT COUNT(*)
FROM INFO_SCHEM.COLUMNS A
WHERE NOT EXISTS (SELECT * FROM
INFO_SCHEM.TABLES B WHERE
A.TABLE_SCHEM = B.TABLE_SCHEM AND
A.TABLE_NAME = B.TABLE_NAME);
-- PASS:0605 If count = 0?

   SELECT COUNT(*)
FROM INFO_SCHEM.COLUMNS
WHERE ORDINAL_POSITION < 1;
-- PASS:0605 If count = 0?

   SELECT COUNT(*)
FROM INFO_SCHEM.COLUMNS
WHERE (IS_NULLABLE <> 'YES'
AND IS_NULLABLE <> 'NO')
OR IS_NULLABLE IS NULL;
-- PASS:0605 If count = 0?

   SELECT COUNT(*)
FROM INFO_SCHEM.COLUMNS
WHERE TABLE_SCHEM = 'FLATER' AND
TABLE_NAME = 'USIG' AND
COLUMN_NAME = 'C1' AND
COLUMN_DEF IS NULL AND
IS_NULLABLE = 'YES' AND
ORDINAL_POSITION = 1 AND
DATA_TYPE = 'INTEGER' AND
NUM_SCALE = 0 AND
CHAR_MAX_LENGTH IS NULL AND
CHAR_OCTET_LENGTH IS NULL AND
NUM_PREC_RADIX IN (2, 10)
AND DATETIME_PREC IS NULL;
-- PASS:0605 If count = 1?

   SELECT COUNT(*)
FROM INFO_SCHEM.COLUMNS
WHERE TABLE_SCHEM = 'FLATER' AND
TABLE_NAME = 'USIG' AND
COLUMN_NAME = 'C_1' AND
COLUMN_DEF IS NULL AND
IS_NULLABLE = 'YES' AND
ORDINAL_POSITION = 2 AND
DATA_TYPE = 'INTEGER' AND
NUM_SCALE = 0 AND
CHAR_MAX_LENGTH IS NULL AND
CHAR_OCTET_LENGTH IS NULL AND
NUM_PREC_RADIX IN (2, 10)
AND DATETIME_PREC IS NULL;
-- PASS:0605 If count = 1?

   SELECT COUNT(*)
FROM INFO_SCHEM.COLUMNS
WHERE TABLE_SCHEM = 'FLATER' AND
TABLE_NAME = 'USIG' AND
ORDINAL_POSITION > 2;
-- PASS:0605 If count = 0?

   SELECT COUNT(*)
FROM INFO_SCHEM.COLUMNS
WHERE TABLE_SCHEM = 'FLATER' AND
TABLE_NAME = 'BASE_WCOV' AND
COLUMN_NAME = 'C1' AND
COLUMN_DEF IS NULL AND
IS_NULLABLE = 'YES' AND
ORDINAL_POSITION = 1 AND
DATA_TYPE = 'INTEGER' AND
NUM_SCALE = 0 AND
CHAR_MAX_LENGTH IS NULL AND
CHAR_OCTET_LENGTH IS NULL AND
NUM_PREC_RADIX IN (2, 10)
AND DATETIME_PREC IS NULL;
-- PASS:0605 If count = 1?

   SELECT COUNT(*)
FROM INFO_SCHEM.COLUMNS
WHERE TABLE_SCHEM = 'FLATER' AND
TABLE_NAME = 'BASE_WCOV' AND
ORDINAL_POSITION <> 1;
-- PASS:0605 If count = 0?

   ROLLBACK WORK;

-- NOTE:0605 This subtest may be removed if there is an implementation
-- NOTE:0605 dependent problem with mixing schema manipulation and data
-- NOTE:0605 statements.

  CREATE TABLE UUUSIG (IRREVERENT DEC (5, 2) DEFAULT 111.22);
-- PASS:0605 If table is created?

  SELECT COUNT(*)
  FROM INFO_SCHEM.COLUMNS
  WHERE TABLE_SCHEM = 'FLATER' AND
  TABLE_NAME = 'UUUSIG' AND
  COLUMN_NAME = 'IRREVERENT' AND
  COLUMN_DEF LIKE '%111.22%' AND
  IS_NULLABLE = 'YES' AND
  ORDINAL_POSITION = 1 AND
  DATA_TYPE = 'DECIMAL' AND
  NUM_SCALE = 2 AND
  CHAR_MAX_LENGTH IS NULL AND
  CHAR_OCTET_LENGTH IS NULL AND
  NUM_PREC_RADIX = 10
  AND DATETIME_PREC IS NULL;
-- PASS:0605 If count = 1?

   ROLLBACK WORK;

-- END TEST >>> 0605 <<< END TEST

-- *************************************************////END-OF-MODULE
