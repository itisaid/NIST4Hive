/* makesql.c  Generate dataload.sql from data files.
   DWF  12/27/94

   1/11/94 DWF
     Fixed it harder (constraints again).

   1/10/94 DWF
     Fixed for constraints mode = immediate.
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <ctype.h>

FILE *
saffopen (fname)
char *fname;
{
  FILE *fp;
  assert (fname);
  if (!(fp = fopen (fname, "r"))) {
    perror ("fopen");
    exit (-1);
  }
  return fp;
}

/* Convert ' to '', truncate to specified length */
void
put_literal (src, len)
char *src;
int len;
{
  int srclen, l;
  assert (src);
  assert (len > 0);
  srclen = strlen (src);
  if (srclen > len)
    srclen = len;
  /* Strip trailing newlines */
  while (srclen) {
    if (!iscntrl(src[srclen-1]))
      break;
    src[--srclen] = '\0';
  }
  putchar ('\'');
  for (l=0;l<srclen;l++) {
    putchar (src[l]);
    if (src[l] == '\'')
      putchar (src[l]);
  }
  putchar ('\'');
}

void
reportfeature ()
{
  FILE *fp;
  char linrec[80];
  fp = saffopen ("REPTFT.DAT");
  while (fgets (linrec, 80, fp)) {
    assert (strlen (linrec) > 4);
    printf ("INSERT INTO REPORTFEATURE VALUES (");
    put_literal (linrec, 4);
    printf (", ");
    put_literal (linrec+5, 30);
    printf (");\n");
  }
  fclose (fp);
  puts ("COMMIT WORK;");
}

void
implication ()
{
  FILE *fp;
  char linrec[80];
  fp = saffopen ("IMPLIC.DAT");
  while (fgets (linrec, 80, fp)) {
    assert (strlen (linrec) > 4);
    printf ("INSERT INTO IMPLICATION VALUES (");
    put_literal (linrec, 4);
    printf (", ");
    put_literal (linrec+5, 4);
    printf (");\n");
  }
  fclose (fp);
  puts ("COMMIT WORK;");
}

void
testprog ()
{
  FILE *fp;
  char linrec[80];
  int tlen;
  fp = saffopen ("TSTPRG.DAT");
  while (fgets (linrec, 80, fp)) {
    assert (strlen (linrec) > 25);
    printf ("INSERT INTO TESTPROG VALUES (");
    put_literal (linrec, 6);
    printf (", ");
    put_literal (linrec+7, 18);
    printf (", ");
    tlen = 10;
    if (strlen (linrec+26) < tlen)
      tlen = strlen (linrec+26);
    while (tlen > 0 && iscntrl (*(linrec+25+tlen)))
      tlen--;
    if (strncmp (linrec+26, "NULL      ", tlen))
      put_literal (linrec+26, 10);
    else
      printf ("NULL");
    printf (");\n");
  }
  fclose (fp);
  puts ("COMMIT WORK;");
}

void
testcase ()
{
  FILE *fp;
  char linrec[80];
  short col45;
  fp = saffopen ("TSTCAS.DAT");
  while (fgets (linrec, 80, fp)) {
    assert (strlen (linrec) > 74);
    printf ("INSERT INTO TESTCASE VALUES (");
    put_literal (linrec, 4);
    printf (", ");
    put_literal (linrec+5, 50);
    printf (", ");
    put_literal (linrec+56, 6);
    printf (", ");
    if (strncmp (linrec+63, "NULL      ", 10))
      put_literal (linrec+63, 10);
    else
      printf ("NULL");
    printf (", ");
    assert (sscanf (linrec+74, "%hd", &col45) == 1);
    assert (col45 >= 0);
    printf ("%d);\n", col45);
  }
  fclose (fp);
  puts ("COMMIT WORK;");
}

void
testfeature ()
{
  FILE *fp;
  char linrec[80];
  fp = saffopen ("TESTFT.DAT");
  while (fgets (linrec, 80, fp)) {
    assert (strlen (linrec) > 4);
    printf ("INSERT INTO TESTFEATURE VALUES (");
    put_literal (linrec, 4);
    printf (", ");
    put_literal (linrec+5, 4);
    printf (");\n");
  }
  fclose (fp);
  puts ("COMMIT WORK;");
}

int
main ()
{
  if (!freopen ("dataload.sql", "w", stdout)) {
    perror ("freopen");
    exit (-1);
  }

  puts ("DELETE FROM F_TEMP;");
  puts ("INSERT INTO F_TEMP SELECT FEATURE1, 'AAAA', 'AAAA', 0 \
FROM FEATURE_CLAIMED;");
  puts ("DELETE FROM FEATURE_CLAIMED;");
  puts ("COMMIT WORK;");

  puts ("DELETE FROM TESTFEATURE;");
  puts ("DELETE FROM TESTCASE;");
  puts ("DELETE FROM TESTPROG;");
  puts ("DELETE FROM IMPLICATION;");
  puts ("DELETE FROM REPORTFEATURE;");
  puts ("COMMIT WORK;");

  reportfeature ();
  implication ();
  testprog ();
  testcase ();
  testfeature ();

  puts ("INSERT INTO FEATURE_CLAIMED SELECT C1 FROM F_TEMP;");
  puts ("COMMIT WORK;");
  puts ("DELETE FROM F_TEMP;");
  puts ("COMMIT WORK;");

  exit (0);
}
