-- MODULE  DML117  

-- SQL Test Suite, V6.0, Interactive SQL, dml117.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION FLATER            

   SELECT USER FROM HU.ECCO;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:0645 Feature 19, Referential delete actions (static)!

   CREATE TABLE LUSERS (
  PRIMARY KEY (LUSER_ID),
  NAAM         CHAR (10),
  LUSER_ID     INT,
  FILE_QUOTA   INT,
  FILE_USAGE   INT NOT NULL,
  CHECK (FILE_USAGE >= 0 AND
    (FILE_QUOTA IS NULL OR FILE_QUOTA >= FILE_USAGE)));
-- PASS:0645 If table is created?

   COMMIT WORK;

   CREATE TABLE LUSER_DATA (
  FOREIGN KEY (LUSER_ID) REFERENCES LUSERS ON DELETE CASCADE,
  PRIMARY KEY (FILE_NAME, LUSER_ID),
  FILE_NAME     CHAR (8) NOT NULL,
  LUSER_ID     INT NOT NULL,
  LUSER_DATA   CHAR (30));
-- PASS:0645 If table is created?

   COMMIT WORK;

   CREATE TABLE AUDIT_CODES (
  ACTION_KEY       INT PRIMARY KEY,
  LUSER_ACTION     CHAR (6) NOT NULL,
  CHECK (LUSER_ACTION = 'INSERT' OR LUSER_ACTION = 'ACCVIO'
         OR LUSER_ACTION = 'DELETE'));
-- PASS:0645 If table is created?

   COMMIT WORK;

   CREATE TABLE ALL_USER_IDS (LUSER_ID INT UNIQUE);
-- PASS:0645 If table is created?

   COMMIT WORK;

   CREATE TABLE AUDIT_RECORDS (
  FOREIGN KEY (LUSER_ID) REFERENCES LUSERS ON DELETE SET NULL,
  LUSER_ID         INT,
  SAVED_LUSER_ID   INT NOT NULL
    REFERENCES ALL_USER_IDS (LUSER_ID) ON DELETE NO ACTION,
  ACTION_KEY       INT DEFAULT 0 NOT NULL
    REFERENCES AUDIT_CODES ON DELETE SET DEFAULT);
-- PASS:0645 If table is created?

   COMMIT WORK;

   INSERT INTO AUDIT_CODES VALUES (0, 'ACCVIO');
-- PASS:0645 If 1 row is inserted?

   INSERT INTO AUDIT_CODES VALUES (1, 'INSERT');
-- PASS:0645 If 1 row is inserted?

   INSERT INTO AUDIT_CODES VALUES (2, 'DELETE');
-- PASS:0645 If 1 row is inserted?

   INSERT INTO LUSERS VALUES ('root', 0, NULL, 2);
-- PASS:0645 If 1 row is inserted?

   INSERT INTO LUSERS VALUES ('BIFF', 1, 0, 0);
-- PASS:0645 If 1 row is inserted?

   INSERT INTO LUSERS VALUES ('Kibo', 2, 1, 1);
-- PASS:0645 If 1 row is inserted?

   INSERT INTO ALL_USER_IDS VALUES (0);
-- PASS:0645 If 1 row is inserted?

   INSERT INTO ALL_USER_IDS VALUES (1);
-- PASS:0645 If 1 row is inserted?

   INSERT INTO ALL_USER_IDS VALUES (2);
-- PASS:0645 If 1 row is inserted?

   INSERT INTO LUSER_DATA VALUES ('ROOT1',
  0, 'BIFF is a total loser');
-- PASS:0645 If 1 row is inserted?

   INSERT INTO LUSER_DATA VALUES ('ROOT2',
  0, 'Kibo wastes disk space');
-- PASS:0645 If 1 row is inserted?

   INSERT INTO AUDIT_RECORDS VALUES (0, 0, 1);
-- PASS:0645 If 1 row is inserted?

   INSERT INTO AUDIT_RECORDS VALUES (0, 0, 1);
-- PASS:0645 If 1 row is inserted?

   INSERT INTO AUDIT_RECORDS VALUES (2, 2, 1);
-- PASS:0645 If 1 row is inserted?

   INSERT INTO AUDIT_RECORDS VALUES (1, 1, 0);
-- PASS:0645 If 1 row is inserted?

   INSERT INTO AUDIT_RECORDS VALUES (1, 1, 0);
-- PASS:0645 If 1 row is inserted?

   INSERT INTO AUDIT_RECORDS VALUES (1, 1, 0);
-- PASS:0645 If 1 row is inserted?

   INSERT INTO AUDIT_RECORDS VALUES (1, 1, 0);
-- PASS:0645 If 1 row is inserted?

   INSERT INTO AUDIT_RECORDS VALUES (1, 1, 0);
-- PASS:0645 If 1 row is inserted?

   INSERT INTO AUDIT_RECORDS VALUES (1, 1, 0);
-- PASS:0645 If 1 row is inserted?

   INSERT INTO AUDIT_RECORDS VALUES (2, 2, 0);
-- PASS:0645 If 1 row is inserted?

   INSERT INTO AUDIT_RECORDS VALUES (2, 2, 2);
-- PASS:0645 If 1 row is inserted?

   INSERT INTO AUDIT_RECORDS VALUES (2, 2, 1);
-- PASS:0645 If 1 row is inserted?

   INSERT INTO LUSER_DATA VALUES ('HAHA',
  2, 'I G0T KIB0Z PASSW0RD!!!');
-- PASS:0645 If 1 row is inserted?

   DELETE FROM LUSERS WHERE NAAM <> 'root';
-- PASS:0645 If 2 rows are deleted?

   SELECT COUNT(*) FROM LUSER_DATA;
-- PASS:0645 If count = 2?

   SELECT COUNT(*) FROM LUSERS;
-- PASS:0645 If count = 1?

   SELECT COUNT(*) FROM AUDIT_RECORDS;
-- PASS:0645 If count = 12?

   SELECT COUNT(*) FROM AUDIT_RECORDS
  WHERE LUSER_ID IS NULL;
-- PASS:0645 If count = 10?

   SELECT COUNT(*) FROM AUDIT_RECORDS
  WHERE SAVED_LUSER_ID IS NULL;
-- PASS:0645 If count = 0?

   DELETE FROM AUDIT_CODES
  WHERE LUSER_ACTION = 'DELETE';
-- PASS:0645 If 1 row is deleted?

   SELECT COUNT(*) FROM AUDIT_RECORDS
  WHERE ACTION_KEY = 2;
-- PASS:0645 If count = 0?

   SELECT COUNT(*) FROM AUDIT_RECORDS
  WHERE ACTION_KEY = 0;
-- PASS:0645 If count = 8?

   DELETE FROM ALL_USER_IDS;
-- PASS:0645 If RI ERROR, children exist, 0 rows deleted?

   COMMIT WORK;

   DROP TABLE AUDIT_RECORDS CASCADE;
-- PASS:0645 If table is dropped?

   COMMIT WORK;

   DROP TABLE ALL_USER_IDS CASCADE;
-- PASS:0645 If table is dropped?

   COMMIT WORK;

   DROP TABLE AUDIT_CODES CASCADE;
-- PASS:0645 If table is dropped?

   COMMIT WORK;

   DROP TABLE LUSER_DATA CASCADE;
-- PASS:0645 If table is dropped?

   COMMIT WORK;

   DROP TABLE LUSERS CASCADE;
-- PASS:0645 If table is dropped?

   COMMIT WORK;

-- END TEST >>> 0645 <<< END TEST
-- *************************************************////END-OF-MODULE
