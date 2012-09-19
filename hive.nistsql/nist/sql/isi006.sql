-- MODULE  ISI006  

-- SQL Test Suite, V6.0, Interactive SQL, isi006.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION FLATER            

   SELECT USER FROM HU.ECCO;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:0573 INFORMATION_SCHEMA.COLUMN_PRIVILEGES definition!

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.COLUMN_PRIVILEGES
     WHERE GRANTEE NOT IN ('PUBLIC', USER)
     AND GRANTOR <> USER;
-- PASS:0573 If COUNT = 0?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.COLUMN_PRIVILEGES
     WHERE IS_GRANTABLE IS NULL;
-- PASS:0573 If COUNT = 0?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.COLUMN_PRIVILEGES
     WHERE IS_GRANTABLE NOT IN ('YES', 'NO');
-- PASS:0573 If COUNT = 0?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.COLUMN_PRIVILEGES
     WHERE GRANTOR IS NULL
        OR GRANTEE IS NULL
        OR TABLE_SCHEMA IS NULL
        OR TABLE_NAME IS NULL
        OR PRIVILEGE_TYPE IS NULL
        OR COLUMN_NAME IS NULL;
-- PASS:0573 If COUNT = 0?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.COLUMN_PRIVILEGES;
-- PASS:0573 If this COUNT = next COUNT?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.COLUMN_PRIVILEGES A,
     INFORMATION_SCHEMA.COLUMN_PRIVILEGES B
     WHERE A.GRANTOR = B.GRANTOR
       AND A.GRANTEE = B.GRANTEE
       AND A.TABLE_SCHEMA = B.TABLE_SCHEMA
       AND A.TABLE_NAME = B.TABLE_NAME
       AND A.PRIVILEGE_TYPE = B.PRIVILEGE_TYPE
       AND A.COLUMN_NAME = B.COLUMN_NAME;
-- PASS:0573 If this COUNT = previous COUNT?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.COLUMN_PRIVILEGES A
     WHERE NOT EXISTS (
       SELECT * FROM INFORMATION_SCHEMA.COLUMNS B
         WHERE A.TABLE_SCHEMA = B.TABLE_SCHEMA
           AND A.TABLE_NAME = B.TABLE_NAME
           AND A.COLUMN_NAME = B.COLUMN_NAME);
-- PASS:0573 If COUNT = 0?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.COLUMN_PRIVILEGES
     WHERE GRANTOR = '_SYSTEM'
       AND GRANTEE = 'FLATER'
       AND IS_GRANTABLE = 'YES'
       AND TABLE_SCHEMA = 'FLATER'
       AND TABLE_NAME = 'USIG'
       AND COLUMN_NAME = 'C_1'
       AND PRIVILEGE_TYPE = 'SELECT';
-- PASS:0573 If COUNT = 1?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.COLUMN_PRIVILEGES
     WHERE GRANTOR = '_SYSTEM'
       AND GRANTEE = 'FLATER'
       AND IS_GRANTABLE = 'YES'
       AND TABLE_SCHEMA = 'FLATER'
       AND TABLE_NAME = 'USIG'
       AND COLUMN_NAME = 'C_1'
       AND PRIVILEGE_TYPE = 'INSERT';
-- PASS:0573 If COUNT = 1?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.COLUMN_PRIVILEGES
     WHERE GRANTOR = '_SYSTEM'
       AND GRANTEE = 'FLATER'
       AND IS_GRANTABLE = 'YES'
       AND TABLE_SCHEMA = 'FLATER'
       AND TABLE_NAME = 'USIG'
       AND COLUMN_NAME = 'C_1'
       AND PRIVILEGE_TYPE = 'UPDATE';
-- PASS:0573 If COUNT = 1?

   SELECT COUNT(*)
     FROM INFORMATION_SCHEMA.COLUMN_PRIVILEGES
     WHERE GRANTOR = '_SYSTEM'
       AND GRANTEE = 'FLATER'
       AND IS_GRANTABLE = 'YES'
       AND TABLE_SCHEMA = 'FLATER'
       AND TABLE_NAME = 'USIG'
       AND COLUMN_NAME = 'C_1'
       AND PRIVILEGE_TYPE = 'REFERENCES';
-- PASS:0573 If COUNT = 1?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.COLUMN_PRIVILEGES
     WHERE GRANTOR = 'FLATER'
       AND GRANTEE = 'SCHANZLE'
       AND IS_GRANTABLE = 'NO'
       AND TABLE_SCHEMA = 'FLATER'
       AND TABLE_NAME = 'WCOV'
       AND COLUMN_NAME = 'C1'
       AND PRIVILEGE_TYPE = 'SELECT';
-- PASS:0573 If COUNT = 1?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.COLUMN_PRIVILEGES
     WHERE GRANTOR = 'FLATER'
       AND GRANTEE = 'SCHANZLE'
       AND IS_GRANTABLE = 'NO'
       AND TABLE_SCHEMA = 'FLATER'
       AND TABLE_NAME = 'WCOV'
       AND COLUMN_NAME = 'C1'
       AND PRIVILEGE_TYPE = 'INSERT';
-- PASS:0573 If COUNT = 1?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.COLUMN_PRIVILEGES
     WHERE GRANTOR = 'FLATER'
       AND GRANTEE = 'SCHANZLE'
       AND IS_GRANTABLE = 'NO'
       AND TABLE_SCHEMA = 'FLATER'
       AND TABLE_NAME = 'WCOV'
       AND COLUMN_NAME = 'C1'
       AND PRIVILEGE_TYPE = 'UPDATE';
-- PASS:0573 If COUNT = 1?

   SELECT COUNT(*) 
     FROM INFORMATION_SCHEMA.COLUMN_PRIVILEGES
     WHERE GRANTOR = 'FLATER'
       AND GRANTEE = 'SCHANZLE'
       AND IS_GRANTABLE = 'NO'
       AND TABLE_SCHEMA = 'FLATER'
       AND TABLE_NAME = 'WCOV'
       AND COLUMN_NAME = 'C1'
       AND PRIVILEGE_TYPE = 'REFERENCES';
-- PASS:0573 If COUNT = 1?

   ROLLBACK WORK;

-- END TEST >>> 0573 <<< END TEST
-- *********************************************
-- *************************************************////END-OF-MODULE
