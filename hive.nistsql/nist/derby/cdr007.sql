--
--   Licensed to the Apache Software Foundation (ASF) under one or more
--   contributor license agreements.  See the NOTICE file distributed with
--   this work for additional information regarding copyright ownership.
--   The ASF licenses this file to You under the Apache License, Version 2.0
--   (the "License"); you may not use this file except in compliance with
--   the License.  You may obtain a copy of the License at
--
--      http://www.apache.org/licenses/LICENSE-2.0
--
--   Unless required by applicable law or agreed to in writing, software
--   distributed under the License is distributed on an "AS IS" BASIS,
--   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--   See the License for the specific language governing permissions and
--   limitations under the License.
--
AUTOCOMMIT OFF;

-- MODULE CDR007

-- SQL Test Suite, V6.0, Interactive SQL, cdr007.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION SUN
   set schema SUN;

--O   SELECT USER FROM SUN.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment

-- date_time print

-- TEST:0319 CHECK <in predicate> in <tab. cons.>, update!

-- setup
  DELETE FROM STAFF10;

  INSERT INTO STAFF10
        VALUES('E3','Susan',11,'Hawaii');

  UPDATE STAFF10
        SET GRADE = 5
        WHERE EMPNUM = 'E3';
-- PASS:0319 If ERROR, check constraint, 0 rows updated?

  SELECT COUNT(*) FROM STAFF10
        WHERE GRADE = 11;
-- PASS:0319 If count = 1?


-- END TEST >>> 0319 <<< END TEST

-- *************************************************


-- TEST:0320 CHECK combination pred. in <tab. cons.>, update!

-- setup
  DELETE FROM STAFF11;

  INSERT INTO STAFF11
        VALUES('E3','Susan',11,'Hawaii');

  UPDATE STAFF11
        SET GRADE = 5
        WHERE EMPNUM = 'E3';
-- PASS:0320 If ERROR, check constraint, 0 rows updated?

  UPDATE STAFF11
        SET EMPNAME = 'Tom'
        WHERE EMPNUM = 'E3';
-- PASS:0320 If ERROR, check constraint, 0 rows updated?

  SELECT COUNT(*) FROM STAFF11
        WHERE EMPNAME = 'Susan' AND GRADE = 11;
-- PASS:0320 If count = 1?

-- END TEST >>> 0320 <<< END TEST

-- *************************************************


-- TEST:0321 CHECK if X NOT LIKE/IN, NOT X LIKE/IN same, update!

-- setup
  DELETE FROM STAFF12;

  INSERT INTO STAFF12
        VALUES('E3','Susan',11,'Hawaii');

  UPDATE STAFF12
        SET GRADE = 5
        WHERE EMPNUM = 'E3';
-- PASS:0321 If ERROR, check constraint, 0 rows updated?

  SELECT COUNT(*) FROM STAFF12
        WHERE GRADE = 11;
-- PASS:0321 If count = 1?


-- END TEST >>> 0321 <<< END TEST

-- *************************************************


-- TEST:0322 CHECK <null predicate> in <col. cons>, update!

-- setup
  DELETE FROM STAFF15;

  INSERT INTO STAFF15
        VALUES('E1','Alice',52,'Deale');

  UPDATE STAFF15
        SET EMPNAME = NULL
        WHERE EMPNUM = 'E1';
-- PASS:0322 If ERROR, check constraint, 0 rows updated?

  SELECT COUNT(*) FROM STAFF15
        WHERE EMPNAME = 'Alice';
-- PASS:0322 If count = 1?


  COMMIT WORK;
 
-- END TEST >>> 0322 <<< END TEST

-- *************************************************////END-OF-MODULE
