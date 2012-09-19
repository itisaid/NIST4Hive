-- MODULE  YTS773  

-- SQL Test Suite, V6.0, Interactive SQL, yts773.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION CTS1              

   SELECT USER FROM HU.ECCO;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:7555 Access to SQL_LANGUAGES view!

   SELECT COUNT (*) 
     FROM INFORMATION_SCHEMA.SQL_LANGUAGES
     WHERE SQL_LANGUAGE_SOURCE IS NULL;
-- PASS:7555 If COUNT = 0?

   SELECT COUNT (*)
     FROM INFORMATION_SCHEMA.SQL_LANGUAGES
     WHERE SQL_LANGUAGE_BINDING_STYLE = 'EMBEDDED'
     AND SQL_LANGUAGE_PROGRAMMING_LANGUAGE = 'C';
-- PASS:7555 If COUNT not 0?

   SELECT COUNT (*) 
     FROM INFORMATION_SCHEMA.SQL_LANGUAGES
     WHERE NOT
     (( SQL_LANGUAGE_SOURCE = 'ISO 9075' AND
       SQL_LANGUAGE_YEAR IS NOT NULL AND
       SQL_LANGUAGE_CONFORMANCE IS NOT NULL AND
       SQL_LANGUAGE_IMPLEMENTATION IS NULL AND
       ( ( SQL_LANGUAGE_YEAR = '1987' AND
           SQL_LANGUAGE_CONFORMANCE IN ( '1', '2' ) AND
           SQL_LANGUAGE_INTEGRITY IS NULL AND
           ( (SQL_LANGUAGE_BINDING_STYLE = 'DIRECT' AND
              SQL_LANGUAGE_PROGRAMMING_LANGUAGE IS NULL )
             OR
              (SQL_LANGUAGE_BINDING_STYLE IN 
              ( 'EMBEDDED', 'MODULE' ) AND
              SQL_LANGUAGE_PROGRAMMING_LANGUAGE IN
              ( 'COBOL', 'FORTRAN', 'PASCAL', 'PLI' ) ) ) )
         OR
         ( SQL_LANGUAGE_YEAR = '1989' AND
           SQL_LANGUAGE_CONFORMANCE IN ( '1', '2' ) AND
           SQL_LANGUAGE_INTEGRITY IN ('NO', 'YES') AND
           ( ( SQL_LANGUAGE_BINDING_STYLE  = 'DIRECT' AND
               SQL_LANGUAGE_PROGRAMMING_LANGUAGE IS NULL )
            OR
             ( SQL_LANGUAGE_BINDING_STYLE  IN 
             ( 'EMBEDDED', 'MODULE' ) AND
               SQL_LANGUAGE_PROGRAMMING_LANGUAGE IN
               ( 'COBOL', 'FORTRAN', 'PASCAL', 'PLI' ) ) ) )
          OR
         ( SQL_LANGUAGE_YEAR = '1992' AND
           SQL_LANGUAGE_CONFORMANCE IN 
               ( 'ENTRY', 'INTERMEDIATE', 'FULL' ) AND
           SQL_LANGUAGE_INTEGRITY IS NULL AND
           ( ( SQL_LANGUAGE_BINDING_STYLE  = 'DIRECT' AND
               SQL_LANGUAGE_PROGRAMMING_LANGUAGE IS NULL )
            OR
             ( SQL_LANGUAGE_BINDING_STYLE  IN 
               ( 'EMBEDDED', 'MODULE' ) AND
               SQL_LANGUAGE_PROGRAMMING_LANGUAGE IN
               ( 'ADA', 'C', 'COBOL', 
                 'FORTRAN', 'MUMPS',  'PASCAL', 'PLI' ) ) ) ) ) )
      OR
      (SQL_LANGUAGE_SOURCE <> 'ISO 9075'));
-- PASS:7555 If COUNT = 0?

   ROLLBACK WORK;

-- END TEST >>> 7555 <<< END TEST
-- *********************************************
-- *************************************************////END-OF-MODULE
