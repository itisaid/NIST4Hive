-- MODULE  YTS767  

-- SQL Test Suite, V6.0, Interactive SQL, yts767.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION CTS1              

   SELECT USER FROM HU.ECCO;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:7544 Explicit table constraints in CHECK_CONSTRAINTS view!

   SELECT CONSTRAINT_SCHEMA, CONSTRAINT_NAME
     FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
     WHERE TABLE_SCHEMA = 'CTS1' AND
     TABLE_NAME = 'STAFF7' AND
     CONSTRAINT_TYPE = 'CHECK';
-- PASS:7544 If 1 row selected?

   SELECT CONSTRAINT_SCHEMA, CONSTRAINT_NAME, CHECK_CLAUSE 
     FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS
     ORDER BY CONSTRAINT_SCHEMA, CONSTRAINT_NAME;
-- PASS:7544 If    CONSTRAINT_SCHEMA & CONSTRAINT_NAME = previous   ?
-- PASS:7544       CONSTRAINT_SCHEMA & CONSTRAINT_NAME for exactly  ?
-- PASS:7544       1 row and CHECK_CLAUSE is either NULL or         ?
-- PASS:7544       'CHECK (GRADE BETWEEN 1 AND 20)'                 ?        
-- NOTE:  Alternate spacing in the CHECK value is acceptable!

   SELECT CONSTRAINT_SCHEMA, CONSTRAINT_NAME
     FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
     WHERE TABLE_SCHEMA = 'CTS1B' AND
     TABLE_NAME = 'STAFF7';
-- PASS:7544 If 1 row selected?

   SELECT CONSTRAINT_SCHEMA, CONSTRAINT_NAME, CHECK_CLAUSE 
     FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS
     ORDER BY CONSTRAINT_SCHEMA, CONSTRAINT_NAME;
-- PASS:7544 If     CONSTRAINT_SCHEMA & CONSTRAINT_NAME = previous    ?
-- PASS:7544        CONSTRAINT_SCHEMA & CONSTRAINT_NAME for exactly   ?
-- PASS:7544        1 row and CHECK_CLAUSE is either NULL or          ?     
-- PASS:7544        'CHECK (GRADE BETWEEN 1 AND 20)'                  ?
-- NOTE:  Alternate spacing in the CHECK value is acceptable!

   SELECT CONSTRAINT_SCHEMA, CONSTRAINT_NAME
     FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
     WHERE TABLE_SCHEMA = 'CTS1' AND
     TABLE_NAME = 'PROJ_DURATION';
-- PASS:7544 If 1 row selected?

   SELECT CONSTRAINT_SCHEMA, CONSTRAINT_NAME, CHECK_CLAUSE 
     FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS
     ORDER BY CONSTRAINT_SCHEMA, CONSTRAINT_NAME;
-- PASS:7544 If    CONSTRAINT_SCHEMA & CONSTRAINT_NAME = previous    ?
-- PASS:7544       CONSTRAINT_SCHEMA & CONSTRAINT_NAME for exactly   ?
-- PASS:7544       1 row and CHECK_CLAUSE is either NULL or          ?
-- PASS:7544       'CHECK (MONTHS > 0)'                              ?
-- NOTE:  Alternate spacing in the CHECK value is acceptable!

   SELECT CONSTRAINT_SCHEMA, CONSTRAINT_NAME
     FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
     WHERE TABLE_SCHEMA = 'CTS1B' AND
     TABLE_NAME = 'PROJ_DURATION';
-- PASS:7544 If 1 row selected?

   SELECT CONSTRAINT_SCHEMA, CONSTRAINT_NAME, CHECK_CLAUSE 
     FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS
     ORDER BY CONSTRAINT_SCHEMA, CONSTRAINT_NAME;
-- PASS:7544 If    CONSTRAINT_SCHEMA & CONSTRAINT_NAME = previous     ?
-- PASS:7544       CONSTRAINT_SCHEMA & CONSTRAINT_NAME for exactly    ?
-- PASS:7544       1 row and CHECK_CLAUSE is either NULL or           ?
-- PASS:7544       'CHECK (MONTHS > 0)'                               ?
-- NOTE:  Alternate spacing in the CHECK value is acceptable!

   ROLLBACK WORK;

-- END TEST >>> 7544 <<< END TEST
-- *********************************************

-- TEST:7545 Column constraints in CHECK_CONSTRAINTS view!

   SELECT CONSTRAINT_SCHEMA, CONSTRAINT_NAME
     FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
     WHERE TABLE_SCHEMA = 'CTS1' AND
     CONSTRAINT_TYPE = 'CHECK' AND
     TABLE_NAME = 'STAFFZ';
-- PASS:7545 If 1 row selected?

   SELECT CONSTRAINT_SCHEMA, CONSTRAINT_NAME, CHECK_CLAUSE 
     FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS
     ORDER BY CONSTRAINT_SCHEMA,
     CONSTRAINT_NAME;
-- PASS:7545 If   matches above and CHECK_CLAUSE is NULL or   ?
-- PASS:7545      'CHECK (SALARY > 0)'                        ?

   SELECT CONSTRAINT_SCHEMA, CONSTRAINT_NAME
     FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
     WHERE TABLE_SCHEMA = 'CTS1B' AND
     CONSTRAINT_TYPE = 'CHECK' AND
     TABLE_NAME = 'STAFFZ';
-- PASS:7545 If 1 row selected?

   SELECT  CONSTRAINT_SCHEMA, CONSTRAINT_NAME, CHECK_CLAUSE
     FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS
     ORDER BY CONSTRAINT_SCHEMA, 
     CONSTRAINT_NAME;
-- PASS:7545 If    matches above and CHECK_CLAUSE is NULL or   ?
-- PASS:7545       'CHECK (SALARY > 0)'                        ?

   ROLLBACK WORK;

-- END TEST >>> 7545 <<< END TEST
-- *********************************************

-- TEST:7546 Domain constraints in CHECK_CONSTRAINTS view!

   SELECT CONSTRAINT_SCHEMA, CONSTRAINT_NAME
     FROM INFORMATION_SCHEMA.DOMAIN_CONSTRAINTS
     WHERE DOMAIN_SCHEMA = 'CTS1' AND
     DOMAIN_NAME = 'ESAL';
-- PASS:7546 If 1 row selected?

   SELECT CONSTRAINT_SCHEMA, CONSTRAINT_NAME, CHECK_CLAUSE 
     FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS
     ORDER BY CONSTRAINT_SCHEMA,
     CONSTRAINT_NAME;
-- PASS:7546 If    matches above and CHECK_VALUE is NULL or   ?
-- PASS:7546       'CHECK (VALUE > 500)'                      ?

   SELECT CONSTRAINT_SCHEMA, CONSTRAINT_NAME
     FROM INFORMATION_SCHEMA.DOMAIN_CONSTRAINTS
     WHERE DOMAIN_SCHEMA = 'CTS1B' AND
     DOMAIN_NAME = 'ESAL';
-- PASS:7546 If 1 row selected?

   SELECT CONSTRAINT_SCHEMA, CONSTRAINT_NAME, CHECK_CLAUSE
     FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS
     ORDER BY CONSTRAINT_SCHEMA,
     CONSTRAINT_NAME;
-- PASS:7546 If    matches above and CHECK_VALUE is NULL or   ?
-- PASS:7546       'CHECK (VALUE > 500)'                      ?

   ROLLBACK WORK;

-- END TEST >>> 7546 <<< END TEST
-- *********************************************

-- TEST:7547 Unique identification in CHECK_CONSTRAINTS view!

   SELECT COUNT (*) 
     FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS
     WHERE CONSTRAINT_CATALOG IS NULL;
-- PASS:7547 If COUNT = 0?

   SELECT COUNT (*) 
     FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS
     WHERE CONSTRAINT_SCHEMA IS NULL;
-- PASS:7547 If COUNT = 0?

   SELECT COUNT (*) 
     FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS
     WHERE CONSTRAINT_NAME IS NULL;
-- PASS:7547 If COUNT = 0?

   SELECT COUNT (*) 
     FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS;
-- PASS:7547 If this COUNT is = to the next COUNT?

   SELECT COUNT (*) 
     FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS AS T
     WHERE
     UNIQUE (
          SELECT * FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS
          WHERE CONSTRAINT_CATALOG = T.CONSTRAINT_CATALOG AND
          CONSTRAINT_NAME = T.CONSTRAINT_NAME AND
          CONSTRAINT_SCHEMA = T.CONSTRAINT_SCHEMA
          );
-- PASS:7547 If this COUNT is = to the previous COUNT?

   ROLLBACK WORK;

-- END TEST >>> 7547 <<< END TEST
-- *********************************************
-- *************************************************////END-OF-MODULE
