-- MODULE  DML183  

-- SQL Test Suite, V6.0, Interactive SQL, dml183.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION FLATER            

   SELECT USER FROM HU.ECCO;
-- RERUN if USER value does not match preceding AUTHORIZATION comment
   ROLLBACK WORK;

-- date_time print

-- TEST:0896 FIPS sizing, 50 WHEN clauses in a CASE expression!

   SELECT EMPNUM,
     CASE GRADE
       WHEN 0 THEN 1000
       WHEN 1 THEN 997
       WHEN 2 THEN 994
       WHEN 3 THEN 991
       WHEN 4 THEN 988
       WHEN 5 THEN 985
       WHEN 6 THEN 982
       WHEN 7 THEN 979
       WHEN 8 THEN 976
       WHEN 9 THEN 973
       WHEN 10 THEN 970
       WHEN 11 THEN 967
       WHEN 12 THEN 964
       WHEN 13 THEN 961
       WHEN 14 THEN 958
       WHEN 15 THEN 955
       WHEN 16 THEN 952
       WHEN 17 THEN 949
       WHEN 18 THEN 946
       WHEN 19 THEN 943
       WHEN 20 THEN 940
       WHEN 21 THEN 937
       WHEN 22 THEN 934
       WHEN 23 THEN 931
       WHEN 24 THEN 928
       WHEN 25 THEN 925
       WHEN 26 THEN 922
       WHEN 27 THEN 919
       WHEN 28 THEN 916
       WHEN 29 THEN 913
       WHEN 30 THEN 910
       WHEN 31 THEN 907
       WHEN 32 THEN 904
       WHEN 33 THEN 901
       WHEN 34 THEN 898
       WHEN 35 THEN 895
       WHEN 36 THEN 892
       WHEN 37 THEN 889
       WHEN 38 THEN 886
       WHEN 39 THEN 883
       WHEN 40 THEN 880
       WHEN 41 THEN 877
       WHEN 42 THEN 874
       WHEN 43 THEN 871
       WHEN 44 THEN 868
       WHEN 45 THEN 865
       WHEN 46 THEN 862
       WHEN 47 THEN 859
       WHEN 48 THEN 856
       WHEN 49 THEN 853
     END
     FROM HU.STAFF
     WHERE EMPNAME = 'Betty';
-- PASS:0896 If empnum =  'E2' and casgrd = 970?

   SELECT EMPNUM,
     CASE
       WHEN GRADE = 0 THEN 1000
       WHEN GRADE = 1 THEN 997
       WHEN GRADE = 2 THEN 994
       WHEN GRADE = 3 THEN 991
       WHEN GRADE = 4 THEN 988
       WHEN GRADE = 5 THEN 985
       WHEN GRADE = 6 THEN 982
       WHEN GRADE = 7 THEN 979
       WHEN GRADE = 8 THEN 976
       WHEN GRADE = 9 THEN 973
       WHEN GRADE = 11 THEN 967
       WHEN GRADE = 12 THEN 964
       WHEN GRADE = 13 THEN 961
       WHEN GRADE = 14 THEN 958
       WHEN GRADE = 15 THEN 955
       WHEN GRADE = 16 THEN 952
       WHEN GRADE = 17 THEN 949
       WHEN GRADE = 18 THEN 946
       WHEN GRADE = 19 THEN 943
       WHEN GRADE = 20 THEN 940
       WHEN GRADE = 21 THEN 937
       WHEN GRADE = 22 THEN 934
       WHEN GRADE = 23 THEN 931
       WHEN GRADE = 24 THEN 928
       WHEN GRADE = 25 THEN 925
       WHEN GRADE = 26 THEN 922
       WHEN GRADE = 27 THEN 919
       WHEN GRADE = 28 THEN 916
       WHEN GRADE = 29 THEN 913
       WHEN GRADE = 30 THEN 910
       WHEN GRADE = 31 THEN 907
       WHEN GRADE = 32 THEN 904
       WHEN GRADE = 33 THEN 901
       WHEN GRADE = 34 THEN 898
       WHEN GRADE = 35 THEN 895
       WHEN GRADE = 36 THEN 892
       WHEN GRADE = 37 THEN 889
       WHEN GRADE = 38 THEN 886
       WHEN GRADE = 39 THEN 883
       WHEN GRADE = 40 THEN 880
       WHEN GRADE = 41 THEN 877
       WHEN GRADE = 42 THEN 874
       WHEN GRADE = 43 THEN 871
       WHEN GRADE = 44 THEN 868
       WHEN GRADE = 45 THEN 865
       WHEN GRADE = 46 THEN 862
       WHEN GRADE = 47 THEN 859
       WHEN GRADE = 48 THEN 856
       WHEN GRADE = 49 THEN 853
       WHEN GRADE = 10 THEN 970
     END
     FROM HU.STAFF
     WHERE EMPNAME = 'Betty';
-- PASS:0896 If empnum = 'E2' and casgrd = 970?

   COMMIT WORK;

-- END TEST >>> 0896 <<< END TEST
-- *********************************************
-- *************************************************////END-OF-MODULE
