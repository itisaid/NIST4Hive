-- MODULE MPA002I  initialization  

-- SQL Test Suite, V6.0, Interactive SQL, mpa002i.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION SULLIVAN1

   SELECT USER FROM HU.ECCO;
-- RERUN if USER value does not match preceding AUTHORIZATION comment

-- date_time print

-- TEST:0231 Transactions serializable - SELECT/UPDATE(replace)!

     DELETE FROM TT;


     INSERT INTO TT VALUES (500,  1);
     INSERT INTO TT VALUES (500,  2);
     INSERT INTO TT VALUES (500,  3);
     INSERT INTO TT VALUES (500,  4);
     INSERT INTO TT VALUES (500,  5);
     INSERT INTO TT VALUES (500,  6);
     INSERT INTO TT VALUES (500,  7);
     INSERT INTO TT VALUES (500,  8);
     INSERT INTO TT VALUES (500,  9);
     INSERT INTO TT VALUES (500, 10);
     INSERT INTO TT VALUES (500, 11);
     INSERT INTO TT VALUES (500, 12);
     INSERT INTO TT VALUES (500, 13);
     INSERT INTO TT VALUES (500, 14);
     INSERT INTO TT VALUES (500, 15);
     INSERT INTO TT VALUES (500, 16);
     INSERT INTO TT VALUES (500, 17);
     INSERT INTO TT VALUES (500, 18);
     INSERT INTO TT VALUES (500, 19);
     INSERT INTO TT VALUES (500, 20);
     INSERT INTO TT VALUES (500, 21);
     INSERT INTO TT VALUES (500, 22);
     INSERT INTO TT VALUES (500, 23);
     INSERT INTO TT VALUES (500, 24);
     INSERT INTO TT VALUES (500, 25);
     INSERT INTO TT VALUES (500, 26);
     INSERT INTO TT VALUES (500, 27);
     INSERT INTO TT VALUES (500, 28);
     INSERT INTO TT VALUES (500, 29);
     INSERT INTO TT VALUES (500, 30);
     INSERT INTO TT VALUES (500, 31);
     INSERT INTO TT VALUES (500, 32);
     INSERT INTO TT VALUES (500, 33);
     INSERT INTO TT VALUES (500, 34);
     INSERT INTO TT VALUES (500, 35);
     INSERT INTO TT VALUES (500, 36);
     INSERT INTO TT VALUES (500, 37);
     INSERT INTO TT VALUES (500, 38);
     INSERT INTO TT VALUES (500, 39);
     INSERT INTO TT VALUES (500, 40);
     INSERT INTO TT VALUES (500, 41);
     INSERT INTO TT VALUES (500, 42);
     INSERT INTO TT VALUES (500, 43);
     INSERT INTO TT VALUES (500, 44);
     INSERT INTO TT VALUES (500, 45);
     INSERT INTO TT VALUES (500, 46);
     INSERT INTO TT VALUES (500, 47);
     INSERT INTO TT VALUES (500, 48);
     INSERT INTO TT VALUES (500, 49);
     INSERT INTO TT VALUES (500, 50);

     COMMIT WORK;

     SELECT SUM(DOLLARS)
        FROM TT
        WHERE ANUM = 1  OR  ANUM = 25  OR ANUM = 50;
-- PASS:0231 If SUM(DOLLARS) = 1500?

-- *************************************************////END-OF-MODULE
