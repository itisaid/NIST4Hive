/*  reportzx.c   "New & Improved" NIST test report generator
 *  DWF
 *
 *  This program is considered a part of the NIST SQL Test Suite, and is
 *  therefore subject to any licenses that apply to the test suite.
 *
 *  This is version 2.0, 12/16/96.
 *
 *  Changes from 1.2 to 2.0:    Print date-time, 59-byte ID, TEd Version #
 *
 *  Changes from 1.1.3 to 1.2:  Added UR, DL, and WD to REQ/OPT/NA categories.
                                Fixed reporting of NA tests as missing in
                                problems list bug. -- DWF
 *  Changes from 1.1.2 to 1.1.3:  Enhanced political correctness of comments.
                                  -- DWF
 *  Changes from 1.1.1 to 1.1.2:  More whitespace in output. -- DWF
 *  Changes from 1.1 to 1.1.1:  Changed "wt" and "rt" to "w" and "r" for fopen
 *                              since it made VAX C unhappy and DOS
 *                              defaults to text mode anyhow.  -- DWF
 *  Changes from 1.0 to 1.1:  More headings, more totals, explanation of **,
 *                            configurable max line length, ordered columns
 *                            same as input, more assertions.  -- DWF
 *  Version 1.0 DWF 9/26/94   -- DWF
 */

#include <stdio.h>
#include <time.h>
#include <assert.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>

/*  Maximum line length.  If you are running lots of profiles and get
 *  assertion failures like strlen (buf) < reclen, increase this value.
 *  Each additional profile uses 10 more columns.  The longest column entry
 *  is "missing** ".
 */
#define reclen 132

/* #define noclobber to forbid clobbering the output files */
#undef noclobber

/*  This file gives English translations of report and subreport code numbers,
 *  for example, P020=FIPS Transitional SQL-92.
 */
#define transfile "heading.dat"

/*  This file gives detailed information about individual tests that is printed
 *  if the tests are failed.
 *
 *    Cols 0-3    Test number
 *    Col  4      Space
 *    Cols 5-54   Description
 *    Col  55     Space
 *    Cols 56-61  Program name
 *    Col  62     Space
 *    Cols 63+    Special Notes
 */
#define mfestfile "testcase.dat"

/*  This file is the *SORTED* dump of test results.
 *
 *    Cols 0-3    Report
 *    Col  4      Space
 *    Cols 5-8    Subreport
 *    Col  9      Space
 *    Cols 10-13  Test #
 *    Col  14     Space
 *    Cols 15-17  Language
 *    Col  18     Space
 *    Cols 19-21  REQ/OPT/NA/UR/DL/WD
 *    Col  22     Space
 *    Cols 23-29  pass/fail/nogo/missing/
 *
 *  The last field is optional and ignored if the fourth is NA.
 */
#define dumpfile  "passfail.dat"

/* These are the output files.
 */
#define testnolst "testno.lst"
#define problst   "problems.lst"
#define totlst    "totals.lst"
#define comblst   "combined.lst"

/* How wide to make the big headings in the combined log */
#define headingwidth 60

struct testcase {
  char testno[5];
  char *descript;
  char prog[7];
  char *notes;
  struct testcase *next;
};

struct heading {
  char label[5];
  char *descript;
  struct heading *next;
};

typedef enum {OPT, REQ, NA, UR, DL, WD} optreq;
typedef enum {PASS, FAIL, MISSING, NOGO} passfail;
typedef enum {BIG, MEDIUM, SMALL} headingtype;

struct dumplist {
  char language[4];
  optreq or;
  passfail pf;
  struct dumplist *next;
};

struct dumprec {
  char profile[5];
  char subprofile[5];
  char testno[5];
  struct dumplist *results;
};

struct pfmnrec {
  int pass;
  int fail;
  int missing;
  int nogo;
};

struct statrec {
  char language[4];
  struct pfmnrec required;
  struct pfmnrec optional;
  struct pfmnrec ur;
  int na;
  int dl;
  int wd;
  int total;
  struct statrec *next;
};

struct testcase *testcases = NULL;
struct heading *headings = NULL;

/* date_time declaration */
time_t cal;

/* safmalloc = malloc + error trapping */
void *
safmalloc (unsigned long numbytes)
{
  void *a;
  if (!(a = malloc ((size_t)numbytes))) {
    perror ("malloc");
    fprintf (stderr, "Could not malloc %lu bytes\n", numbytes);
    exit (-1);
  }
  return a;
}

/* saffopen = fopen + error trapping */
FILE *
saffopen (char *fname, char *mode)
{
  FILE *fp;
  assert (fname);
  assert (mode);
  if (!(fp = fopen (fname, mode))) {
    perror ("fopen");
    fprintf (stderr, "Could not open '%s' with mode '%s'\n", fname, mode);
    exit (-1);
  }
  return fp;
}

/* Try to insure that a file does not exist.  Stat is better than trying to
 * fopen since fopen won't work if we don't have read permission.  However,
 * stat is not in ANSI C.
 */
char *
dont_clobber (char *fname)
{
  FILE *fp;
  assert (fname);
  if ((fp = fopen (fname, "r"))) {
#ifdef noclobber
    fprintf (stderr, "Will not overwrite file '%s' -- remove or rename it.\n",
      fname);
    exit (-1);
#else
    fprintf (stderr, "Clobbering file '%s'\n", fname);
    fclose (fp);
#endif
  }
  return fname;
}

/* Read heading.dat into memory. */
void
read_headings (char *hfile)
{
  FILE *fp;
  char buf[reclen+1];
  assert (hfile);
  assert (!headings);
  fp = saffopen (hfile, "r");
  printf ("\nReading %s....\n", hfile);
  while (fgets (buf, reclen+1, fp)) {
    struct heading *t;
    int tlen;
    tlen = strlen (buf);
    assert (tlen >= 6 && tlen < reclen);
    t = safmalloc (sizeof (struct heading));
    t->next = headings;
    headings = t;
    memcpy (t->label, buf, 4);
    t->label[4] = '\0';
    tlen--;
    while (isspace (buf[tlen])) {
      assert (tlen >= 6);
      buf[tlen--] = '\0';
    }
    t->descript = safmalloc (strlen(buf+5)+1);
    strcpy (t->descript, buf+5);

#ifdef debug
    printf ("%s/%s/\n", t->label, t->descript);
#endif
  }
  fclose (fp);
}

/* Read testcase.dat into memory. */
void
read_testcases (char *tfile)
{
  FILE *fp;
  char buf[reclen+1];
  assert (tfile);
  assert (!testcases);
  fp = saffopen (tfile, "r");
  printf ("\nReading %s....\n", tfile);
  while (fgets (buf, reclen+1, fp)) {
    struct testcase *t;
    int tlen;
    tlen = strlen (buf);
    assert (tlen >= 62 && tlen < reclen);
    t = safmalloc (sizeof (struct testcase));
    t->next = testcases;
    testcases = t;
    memcpy (t->testno, buf, 4);
    t->testno[4] = '\0';
    memcpy (t->prog, buf+56, 6);
    t->prog[6] = '\0';
    tlen--;
    while (isspace (buf[tlen])) {
      assert (tlen >= 62);
      buf[tlen--] = '\0';
    }
    t->notes = NULL;
    if (buf[62] != '\0')
      if ((tlen = strlen (buf+63))) {
        t->notes = safmalloc (tlen+1);
        strcpy (t->notes, buf+63);
      }
    buf[55] = '\0';
    tlen = 54;
    while (isspace (buf[tlen])) {
      assert (tlen > 5);
      buf[tlen--] = '\0';
    }
    t->descript = safmalloc (strlen(buf+5)+1);
    strcpy (t->descript, buf+5);

#ifdef debug
    printf ("%s/%s/%s/", t->testno, t->descript, t->prog);
    if (t->notes)
      printf ("%s", t->notes);
    printf ("/\n");
#endif
  }
  fclose (fp);
}

/* This function buffers a line from passfail.dat so that we can read it twice
 * when necessary.  a = 1 to move forward, a = 0 to re-read current line.
 */
int dumpeof = 0;
char *
read_dump (int a, FILE *fp)
{
  static char staticbuf[reclen+1];
  assert (fp);
  if (dumpeof)
    return NULL;
  if (!a)
    return staticbuf;
  if (!(fgets (staticbuf, reclen+1, fp))) {
    dumpeof = 1;
    return NULL;
  }
  assert (strlen (staticbuf) < reclen);
  return staticbuf;
}

/* Pretty-print a heading, with lots of asterisks and things. */
void
pretty_heading (char *heading, FILE *out, headingtype ht)
{
  int a, b;
  char c, d;
  assert (heading);
  assert (out);
  switch (ht) {
  case BIG:
    c = d = '*';
    for (a=0;a<headingwidth;a++)
      fputc ('*', out);
    fprintf (out, "\n");
    break;
  case MEDIUM:
    c = '>';
    d = '<';
    fprintf (out, "\n");
    break;
  case SMALL:
    c = d = '-';
    break;
  default:
    assert (0);
  }
  b = (headingwidth - strlen (heading) - 2) >> 1;
  assert (b > 0);
  if (headingwidth-((b<<1)+strlen(heading)+2))
    fputc (c, out);
  for (a=0;a<b;a++)
    fputc (c, out);
  fprintf (out, " %s ", heading);
  for (a=0;a<b;a++)
    fputc (d, out);
  fprintf (out, "\n");
  switch (ht) {
  case BIG:
    for (a=0;a<headingwidth;a++)
      fputc ('*', out);
    /* Fall through */
  case MEDIUM:
    fprintf (out, "\n");
  }
  fprintf (out, "\n");
}

/* Used to write the combined log. */
void
fcopy (char *fin, FILE *out)
{
  char buf[reclen+1];
  FILE *in;
  assert (fin);
  assert (out);
  in = saffopen (fin, "r");
  while (fgets (buf, reclen+1, in)) {
    assert (strlen (buf) < reclen);
    if (fputs (buf, out) == EOF) {
      perror ("fputs");
      fprintf (stderr, "Couldn't write a string to the combined log\n");
      exit (-1);
    }
  }
  fclose (in);
}

/* Used to write the combined log. */
void
fcat (char *f1, char *f2, char *f3, char *out)
{
  FILE *fpout;
  assert (f1);
  assert (f2);
  assert (f3);
  assert (out);
  puts ("\nCreating combined log....");
  puts ("\n\nTEd Version #");
  fpout = saffopen (out, "w");
  fcopy (f1, fpout);
  fprintf (fpout, "\f\n");
  fcopy (f2, fpout);
  fprintf (fpout, "\f\n");
  fcopy (f3, fpout);
  fclose (fpout);
}

/* Parse a line from passfail.dat.  It is important to note that trailing
 * blanks are preserved in the language specification -- it's always three
 * characters.
 */
void
parse_passfaildat_line (char *line, char profile[5], char subprofile[5],
char testno[5], char language[4], optreq *or, passfail *pf)
{
  assert (line);
  assert (profile);
  assert (subprofile);
  assert (testno);
  assert (language);
  assert (or);
  assert (pf);
  assert (strlen (line) >= 21);

  memcpy (profile, line, 4);
  profile[4] = '\0';
  memcpy (subprofile, line+5, 4);
  subprofile[4] = '\0';
  memcpy (testno, line+10, 4);
  testno[4] = '\0';
  memcpy (language, line+15, 3);
  assert (!isspace(language[0]));
  language[3] = '\0';

  if (!strncmp (line+19, "NA", 2))
    *or = NA;
  else if (!strncmp (line+19, "DL", 2))
    *or = DL;
  else if (!strncmp (line+19, "WD", 2))
    *or = WD;
  else {
    assert (strlen (line) >= 27);

    if (!strncmp (line+19, "OPT", 3))
      *or = OPT;
    else if (!strncmp (line+19, "REQ", 3))
      *or = REQ;
    else if (!strncmp (line+19, "UR", 2))
      *or = UR;
    else {
      char t[4];
      strncpy (t, line+19, 3);
      t[3] = '\0';
      fprintf (stderr, "Unrecognized OPT/REQ/NA/UR/DL/WD spec:  '%s'\n", t);
      exit (-1);
    }

    if (!strncmp (line+23, "pass", 4))
      *pf = PASS;
    else if (!strncmp (line+23, "fail", 4))
      *pf = FAIL;
    else if (!strncmp (line+23, "nogo", 4))
      *pf = NOGO;
    else if (!strncmp (line+23, "missing", 7))
      *pf = MISSING;
    else {
      char t[8];
      int i;
      strncpy (t, line+23, 7);
      t[7] = '\0';
      i = strlen (t);
      while (i) {
        i--;
        if (isspace (t[i]))
          t[i] = '\0';
        else
          break;
      }
      fprintf (stderr, "Unrecognized pass/fail/nogo/missing spec:  '%s'\n", t);
      exit (-1);
    }
  }
}

/* Write to the problem log */
void
print_problems (FILE *fp, struct dumprec *testdat)
{
  struct testcase *tc = testcases;
  struct dumplist *r;
  assert (fp);
  assert (testdat);
  while (tc) {
    if (!strcmp (tc->testno, testdat->testno))
      break;
    tc = tc->next;
  }
  if (!tc) {
    fprintf (stderr, "Error:  test case %s not found\n", testdat->testno);
    exit (-1);
  }
  fprintf (fp, "%s  Test %s in %s:  %s\n", tc->testno, tc->testno, tc->prog,
    tc->descript);
  if (tc->notes)
    fprintf (fp, "%s  NOTE:  %s\n", tc->testno, tc->notes);
  r = testdat->results;
  while (r) {
    switch (r->or) {
    case NA:
    case DL:
    case WD:
      break;
    default:
      if (r->pf == FAIL || r->pf == MISSING) {
        fprintf (fp, "%s    %s = ", tc->testno, r->language);
        if (r->pf == FAIL)
          fprintf (fp, "fail");
        else
          fprintf (fp, "missing");
        if (r->or == OPT)
          fprintf (fp, "**");
        if (r->or == UR)
          fprintf (fp, "++");
        fprintf (fp, "\n");
      }
    }
    r = r->next;
  }
  fprintf (fp, "\n");
}

/* Increment a statistic.  Note that this function does not care in which
 * order the languages are stored or updated.  The ordering of teststats was
 * added to one_test as an afterthought to make the printout look nicer.
 */
void
tally_stats (struct dumplist *dl, struct statrec *teststats)
{
  assert (dl);
  assert (teststats);
  while (teststats) {
    if (!strcmp (teststats->language, dl->language)) {
      if (dl->or == NA)
        (teststats->na)++;
      else if (dl->or == DL)
        (teststats->dl)++;
      else if (dl->or == WD)
        (teststats->wd)++;
      else {
        struct pfmnrec *t;
        switch (dl->or) {
        case OPT:
          t = &(teststats->optional);
          break;
        case REQ:
          t = &(teststats->required);
          break;
        case UR:
          t = &(teststats->ur);
          break;
        default:
          assert (0);
        }
        switch (dl->pf) {
        case PASS:
          (t->pass)++;
          break;
        case FAIL:
          (t->fail)++;
          break;
        case MISSING:
          (t->missing)++;
          break;
        case NOGO:
          (t->nogo)++;
          break;
        default:
          assert (0);
        }
      }
      (teststats->total)++;
      return;
    }
    teststats = teststats->next;
  }
  fprintf (stderr,
    "Error:  inconsistent languages in input!  (This is BAD)\n");
  exit (-1);
}

/* Do one test in some profile.  Returns number of problems. */
int
one_test (struct dumprec *testdat, struct statrec **teststats,
int *headingflag, FILE *fp, FILE *problemfile, int *proboptflag,
int *proburflag)
{
  struct dumplist *t1;
  int numprobs = 0;

  assert (testdat);
  assert (testdat->results);
  assert (teststats);
  assert (headingflag);
  assert (fp);
  assert (problemfile);
  assert (proboptflag);
  assert (proburflag);

  if (!(*headingflag)) {
    t1 = testdat->results;
    fprintf (fp, "TESTNO ");
    while (t1) {
      fprintf (fp, "%s       ", t1->language);  /* 10 columns */
      t1 = t1->next;
    }
    fprintf (fp, "\n");
    *headingflag = 1;
  }

  /* Build teststats based on first test in profile, then complain if there
   * are other languages in later tests in the same profile.
   *
   * The teststats are kept in order so that the columns in the totals tables
   * will be in the same order as the columns in the other tables.
   */
  if (!(*teststats)) {
    struct statrec *last = NULL;
    t1 = testdat->results;
    while (t1) {
      struct statrec *t2;
      t2 = safmalloc (sizeof (struct statrec));
      strcpy (t2->language, t1->language);
      t2->required.pass = t2->required.fail = t2->required.missing =
        t2->required.nogo = t2->optional.pass = t2->optional.fail =
        t2->optional.missing = t2->optional.nogo = t2->ur.pass =
        t2->ur.fail = t2->ur.missing = t2->ur.nogo = t2->na = t2->dl =
        t2->wd = t2->total = 0;
      t2->next = NULL;
      if (last)
        last->next = t2;
      else
        *teststats = t2;
      last = t2;
      t1 = t1->next;
    }
  }

  fprintf (fp, " %s  ", testdat->testno);
  t1 = testdat->results;
  while (t1) {
    tally_stats (t1, *teststats);
    if (t1->or == NA)
      fprintf (fp, "NA        ");
    else if (t1->or == DL)
      fprintf (fp, "DL        ");
    else if (t1->or == WD)
      fprintf (fp, "WD        ");
    else {
      switch (t1->pf) {
      case PASS:
        fprintf (fp, "pass");
        break;
      case FAIL:
        numprobs++;
        if (t1->or == OPT)
          *proboptflag = 1;
        if (t1->or == UR)
          *proburflag = 1;
        fprintf (fp, "fail");
        break;
      case NOGO:
        fprintf (fp, "nogo");
        break;
      case MISSING:
        numprobs++;
        if (t1->or == OPT)
          *proboptflag = 1;
        if (t1->or == UR)
          *proburflag = 1;
        fprintf (fp, "missing");
        break;
      default:
        assert (0);
      }
      switch (t1->or) {
      case OPT:
        fprintf (fp, "**");
        break;
      case UR:
        fprintf (fp, "++");
        break;
      default:
        fprintf (fp, "  ");
        break;
      }
      if (t1->pf != MISSING)
        fprintf (fp, "   ");
      fprintf (fp, " ");
    }
    t1 = t1->next;
  }
  fprintf (fp, "\n");

  if (numprobs)
    print_problems (problemfile, testdat);

  /* Get rid of old results */
  while (testdat->results) {
    t1 = testdat->results;
    testdat->results = t1->next;
    free (t1);
  }

  return numprobs;
}

/* Add another test to the list.  Retrofitting this function to keep the list
 * in the same order as the input required triple indirection.  If you ever
 * need an example of a non-gratuitous use of triple indirection, here it is.
 */
void
add_test (struct dumprec *testdat, char profile[5], char subprofile[5],
char testno[5], char language[4], optreq or, passfail pf,
struct dumplist ***tail)
{
  struct dumplist *t;
  assert (testdat);
  assert (profile);
  assert (subprofile);
  assert (testno);
  assert (language);
  assert (tail);
  if (!(testdat->results)) {
    /* New test */
    strcpy (testdat->profile, profile);
    strcpy (testdat->subprofile, subprofile);
    strcpy (testdat->testno, testno);
    *tail = &(testdat->results);
  }
  t = safmalloc (sizeof (struct dumplist));
  strcpy (t->language, language);
  t->or = or;
  t->pf = pf;
  t->next = NULL;
  **tail = t;
  *tail = &(t->next);
}

/* Generate totals, get rid of statistics. */
void
print_delete_statistics (struct statrec *teststats, FILE *fp)
{
  int grandtotal = 0, probtotal = 0;
  struct statrec *t;

  assert (teststats);
  assert (fp);

  fprintf (fp, "           ");            /* 11 spaces */
  t = teststats;
  while (t) {
    fprintf (fp, "  %s  ", t->language);  /* 7 columns = 5 digits + margins */
    t = t->next;
  }
  fprintf (fp, "\nREQUIRED");
  fprintf (fp, "\n  pass     ");
  t = teststats;
  while (t) {
    fprintf (fp, " %5d ", t->required.pass);
    t = t->next;
  }
  fprintf (fp, "\n  fail     ");
  t = teststats;
  while (t) {
    fprintf (fp, " %5d ", t->required.fail);
    probtotal += t->required.fail;
    t = t->next;
  }
  fprintf (fp, "\n  missing  ");
  t = teststats;
  while (t) {
    fprintf (fp, " %5d ", t->required.missing);
    probtotal += t->required.missing;
    t = t->next;
  }
  fprintf (fp, "\n  nogo     ");
  t = teststats;
  while (t) {
    fprintf (fp, " %5d ", t->required.nogo);
    t = t->next;
  }
  fprintf (fp, "\nOPTIONAL");
  fprintf (fp, "\n  pass     ");
  t = teststats;
  while (t) {
    fprintf (fp, " %5d ", t->optional.pass);
    t = t->next;
  }
  fprintf (fp, "\n  fail     ");
  t = teststats;
  while (t) {
    fprintf (fp, " %5d ", t->optional.fail);
    probtotal += t->optional.fail;
    t = t->next;
  }
  fprintf (fp, "\n  missing  ");
  t = teststats;
  while (t) {
    fprintf (fp, " %5d ", t->optional.missing);
    probtotal += t->optional.missing;
    t = t->next;
  }
  fprintf (fp, "\n  nogo     ");
  t = teststats;
  while (t) {
    fprintf (fp, " %5d ", t->optional.nogo);
    t = t->next;
  }
  fprintf (fp, "\nUNDER REVIEW");
  fprintf (fp, "\n  pass     ");
  t = teststats;
  while (t) {
    fprintf (fp, " %5d ", t->ur.pass);
    t = t->next;
  }
  fprintf (fp, "\n  fail     ");
  t = teststats;
  while (t) {
    fprintf (fp, " %5d ", t->ur.fail);
    probtotal += t->ur.fail;
    t = t->next;
  }
  fprintf (fp, "\n  missing  ");
  t = teststats;
  while (t) {
    fprintf (fp, " %5d ", t->ur.missing);
    probtotal += t->ur.missing;
    t = t->next;
  }
  fprintf (fp, "\n  nogo     ");
  t = teststats;
  while (t) {
    fprintf (fp, " %5d ", t->ur.nogo);
    t = t->next;
  }
  fprintf (fp, "\n\nNA         ");
  t = teststats;
  while (t) {
    fprintf (fp, " %5d ", t->na);
    t = t->next;
  }
  fprintf (fp, "\nDL         ");
  t = teststats;
  while (t) {
    fprintf (fp, " %5d ", t->dl);
    t = t->next;
  }
  fprintf (fp, "\nWD         ");
  t = teststats;
  while (t) {
    fprintf (fp, " %5d ", t->wd);
    t = t->next;
  }

  fprintf (fp, "\n           ");
  t = teststats;
  while (t) {
    fprintf (fp, " ----- ");
    t = t->next;
  }
  fprintf (fp, "\n           ");
  t = teststats;
  while (t) {
    fprintf (fp, " %5d ", t->total);
    grandtotal += t->total;
    t = t->next;
  }

  fprintf (fp, "\n\nGrand total = %d\n", grandtotal);
  fprintf (fp, "Problem total = %d\n", probtotal);

  while (teststats) {
    t = teststats;
    teststats = t->next;
    free (t);
  }
}

/* Translate a profile or subprofile number into English */
char *
translate_profile (char *profile)
{
  struct heading *t = headings;
  assert (profile);
  while (t) {
    if (!strcmp (profile, t->label))
      return t->descript;
    t = t->next;
  }
  fprintf (stderr, "Error:  (Sub)Profile not found:  '%s'\n", profile);
  exit (-1);
}

/* Do one profile.  All language lines pertaining to a given test are buffered
 * before output is generated since otherwise it's hard to figure out what to
 * print in the heading.  Careful when editing this function; it contains a
 * lot of duplicated code, and changes must be made to each instance of each
 * thing you edit.
 */
int first_profile = 1;
void
one_profile (FILE *dfp, FILE *testnofp, FILE *probfp, FILE *totfp)
{
  char *line, current_profile[5], current_subprofile[5], profile[5],
    subprofile[5], testno[5], language[4], *transprof, *transsubprof;
  optreq or;
  passfail pf;
  struct dumprec testdat = {"    ", "    ", "    ", NULL};
  struct dumplist **tail;
  struct statrec *teststats = NULL;
  int headingflag = 0, numprobs = 0, numtests = 0, optflag = 0, urflag = 0,
    proboptflag = 0, proburflag = 0;

  assert (dfp);
  assert (testnofp);
  assert (probfp);
  assert (totfp);

  if (!(line = read_dump (0, dfp)))
    return;
  memcpy (current_profile, line, 4);
  current_profile[4] = '\0';
  assert (transprof = translate_profile (current_profile));
  memcpy (current_subprofile, line+5, 4);
  current_subprofile[4] = '\0';
  assert (transsubprof = translate_profile (current_subprofile));

  if (!first_profile) {
    fprintf (testnofp, "\f\n");
    fprintf (probfp, "\f\n");
    fprintf (totfp, "\f\n");
  } else
    first_profile = 0;

  fprintf (testnofp, "59-byte ID\n");
  fprintf (probfp, "59-byte ID\n");
  fprintf (totfp, "59-byte ID\n");

  time (&cal);
  fprintf (testnofp, "Time Run:  %s\n", ctime (&cal));
  fprintf (probfp, "Time Run:  %s\n", ctime (&cal));
  fprintf (totfp, "Time Run:  %s\n", ctime (&cal));
 
  pretty_heading ("TEST RESULTS", testnofp, BIG);
  pretty_heading ("PROBLEMS", probfp, BIG);
  pretty_heading ("TOTALS", totfp, BIG);
  pretty_heading (transprof, testnofp, MEDIUM);
  pretty_heading (transprof, probfp, MEDIUM);
  pretty_heading (transprof, totfp, MEDIUM);
  pretty_heading (transsubprof, testnofp, SMALL);
  pretty_heading (transsubprof, probfp, SMALL);

  while (line) {
    parse_passfaildat_line (line, profile, subprofile, testno, language, &or,
      &pf);
    if (or == OPT)
      optflag = 1;
    if (or == UR)
      urflag = 1;

    if (testdat.results)
      if (strcmp (testno, testdat.testno)) {
        numprobs +=
          one_test (&testdat, &teststats, &headingflag, testnofp, probfp,
            &proboptflag, &proburflag);
        numtests++;
      }
    add_test (&testdat, profile, subprofile, testno, language, or, pf, &tail);

    if (!(line = read_dump (1, dfp)))
      continue;
    if (strncmp (current_profile, line, 4))
      break;
    if (strncmp (current_subprofile, line+5, 4)) {
      /* Things to be done at the end of each subprofile */
      if (testdat.results) {
        numprobs +=
          one_test (&testdat, &teststats, &headingflag, testnofp, probfp,
            &proboptflag, &proburflag);
        numtests++;
      }
      memcpy (current_subprofile, line+5, 4);
      current_subprofile[4] = '\0';
      assert (transsubprof = translate_profile (current_subprofile));
      fprintf (testnofp, "\n");
      pretty_heading (transsubprof, testnofp, SMALL);
      pretty_heading (transsubprof, probfp, SMALL);
      headingflag = 0;
    }
  }

  /* Things to be done at the end of each profile */
  if (testdat.results) {
    numprobs +=
      one_test (&testdat, &teststats, &headingflag, testnofp, probfp,
        &proboptflag, &proburflag);
    numtests++;
  }
  fprintf (probfp, "Total number of problems:  %d\n", numprobs);
  fprintf (testnofp, "\nTotal number of tests:  %d\n", numtests);
  if (optflag)
    fprintf (testnofp, "\n** denotes OPTIONAL/INFORMATIONAL test\n");
  if (urflag)
    fprintf (testnofp, "\n++ denotes test that is UNDER REVIEW\n");
  if (proboptflag)
    fprintf (probfp, "\n** denotes OPTIONAL/INFORMATIONAL test\n");
  if (proburflag)
    fprintf (probfp, "\n++ denotes test that is UNDER REVIEW\n");
  print_delete_statistics (teststats, totfp);
  /* teststats now points to freed memory, so don't do anything else with it
     before exiting this function.  */
}

/* Do all the profiles. */
void
whole_report (char *tfile, char *mfile, char *dfile, char *testno, char *prob,
char *tot, char *comb)
{
  FILE *dfp, *testnofp, *probfp, *totfp;
  assert (tfile);
  assert (mfile);
  assert (dfile);
  assert (testno);
  assert (prob);
  assert (tot);
  assert (comb);

  read_testcases (mfile);
  read_headings (tfile);

  dfp = saffopen (dfile, "r");
  testnofp = saffopen (dont_clobber (testno), "w");
  probfp   = saffopen (dont_clobber (prob),   "w");
  totfp    = saffopen (dont_clobber (tot),    "w");
  assert (read_dump (1, dfp));
  printf ("\nReporting....\n");
  do
    one_profile (dfp, testnofp, probfp, totfp);
  while (read_dump (0, dfp));

  fclose (dfp);
  fclose (testnofp);
  fclose (probfp);
  fclose (totfp);

  dont_clobber (comb);
  fcat (prob, tot, testno, comb);
}

int
main (int argc, char **argv)
{
  switch (argc) {
  case 1:
    whole_report (transfile, mfestfile, dumpfile, testnolst, problst, totlst,
      comblst);
    break;
  case 4:
    whole_report (argv[1], argv[2], argv[3], testnolst, problst, totlst,
      comblst);
    break;
  case 8:
    whole_report (argv[1], argv[2], argv[3], argv[4], argv[5], argv[6],
      argv[7]);
    break;
  default:
    puts ("Usage:  reportzx [transfile mfestfile dumpfile [testnolst \
problst totlst comblst]]");
    puts ("Defaults are as follows:");
    printf ("  transfile = '%s'\n", transfile);
    printf ("  mfestfile = '%s'\n", mfestfile);
    printf ("  dumpfile  = '%s'\n", dumpfile);
    printf ("  testnolst = '%s'\n", testnolst);
    printf ("  problst   = '%s'\n", problst);
    printf ("  totlst    = '%s'\n", totlst);
    printf ("  comblst   = '%s'\n", comblst);
    return -1;
  }
  return 0;
}
