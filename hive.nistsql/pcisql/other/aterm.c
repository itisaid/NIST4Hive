/*

INTRODUCTION

If you need to add continuation markers or change the SQL terminator
character for the interactive SQL tests, this program is for you!

SQL-92 specifies:
  <newline> ::= !! implementation-defined end-of-line indicator
   in Clause 5.2 Format section
and
  <direct SQL statement> ::= 
         <directly executable statement> <semicolon>
   in Clause 20.1 Format section

Therefore, a conforming implementation may change the continuation markers
but not the SQL terminator.  Meanwhile, both options are provided to
facilitate testing.

The NIST test files assume that a multiple-line SQL statement does
not require a continuation character at the end of each line, but
is terminated by a semicolon.

TEd cannot be used to accomplish this easily (yet!).

For example, this program can change

       SELECT * FROM WORKS
       WHERE HOURS < 15
       ORDER BY HOURS;

to
       SELECT * FROM WORKS -
       WHERE HOURS < 15 -
       ORDER BY HOURS END-EXEC

by specifying " -" (that is, space followed by a hyphen) as the
continuation marker and " END-EXEC" as the SQL terminator replacement
in response to the prompts.


DETAILED INFORMATION

Compile this file and name the executable aterm (for Add TERMinator).
To delete terminators, this program should be named dterm (for Delete
TERMinator).  [ ** NOTE:  dterm is not completed yet.  ** ]

Currently the only argument supported is -v, for verbose operation.
In this mode, aterm describes exactly what it's doing for every line.
And when NIST means verbose, we MEAN verbose!!

If you want to operate on more than one file, create a list-of-files
file which contains a list of input (and optionally, output)
filenames.  If an output filename is not specified, the input file
will be overwritten.  Each line has one or two "words" separated by
whitespace:

	input.file
	input.file2	output.file2

In this example, input.file is read, modified, and then overwritten
with the modified version.  In the second line, input.file2 is read,
modified, and then written to a different file:  output.file2.  The
input file is ALWAYS the first word on a line.


When invoked, aterm asks if you want to use the list-of-files option.
If you answer "y", it will then ask for the filename of the
list-of-files file.  

If you don't use the list-of-files option, aterm will only work on a
single file.  Enter the input filename as prompted, and then an output
filename.  If you want to overwrite the input file, just press RETURN
at the prompt for the output filename.


CONTINUATION / TERMINATOR STRINGS

The next two prompts are for entering a new SQL continuation string
and/or replace the SQL statement terminator (a semicolon).  If you
don't want to add one, just hit return, but at least one of them must
be specified (otherwise you wouldn't need this program!).

It is not possible to delete the SQL terminator string, but you may
effectively delete it by specifying the replacement string as a space
character.

Note that these strings are appended directly to the end of the
existing lines in the tests.  You may wish to include a space before
your string to avoid mashing strings.  This is especially evident with
the terminator string (since most semicolon terminators do not have a
space before them).  See the introductory example.  We recommend
experimenting on a test file at first!


SUPPORT

This program has not been tested extensively.  If you experience any
technical problems (e.g., compilation errors, core dumps/crashing,
or unexpected generated output) please contact the author at:

Chris Schanzle 
NIST Bldg 225 (Tech) A-266
Gaithersburg, MD 20899
(301) 975-3796

If you have questions on how to apply this to the SQL test suite,
please contact Joan Sullivan (same address) at (301) 975-3258.


*/



/* 

National Institute of Standards and Technology
This software is property of the United States Government.  

This software must only be used on data files that are part of the
NIST SQL Test Suite.  This software is covered under the same terms,
conditions, restrictions and disclaimers provided in your Software
License Agreement.  The customer will not resell, copy, modify, or
otherwise reproduce, or allow to be reproduced, any portion of the
software.  

*/

/* 
    "mytypes.h" - global definitions for "The Terminator" project:
    		  Add or remove terminators, continuation characters
		  for interactive SQL files.
*/



#define MYTYPES			/* allows concatenation of all sources */

#include <stdio.h>
#include <string.h>

struct lines {			/* doubly linked linear linked list */
    char *text;
    int line_num;		/* line number of original file */
    struct lines *next, *prev;	/* forward, backward ptrs */
};


/* These are declared in main.c. (just defined here) */

extern struct lines line_head;		/* head node */
extern int line;			/* line counter */
extern int ig_case;			/* when to ignore case in substr() */
extern int verbose;			/* runtime verbose mode */
extern int mode;			/* ADDTERM or DELTERM operation mode */
extern int save_extra;			/* extra bytes for saving strings */
extern char src_file[],			/* the current source filename */
    	    out_file[];			/* output filename */
extern char term_str[],			/* new SQL terminator string */
	    cont_str[];			/* new SQL continuation string */

char *substr(), *strsave(), *strupper(), *readline();
char *strchr(), *strrchr(), *strucpy();	/* my library functions */
char *zap_lf(), *getlastword();
char *cmd_str();
char *malloc(), *realloc();	/* makes lint more content. */
struct lines *search_lines(), *get_line_obj(), *insert_text();
FILE *open_file();

#define ISWHITE(c) (((c) == ' ' || (c) == '\t' || (c) == '\n') ? 1 : 0)

#define SQLTERM ';'

				/* names to call the program */
#define ADDTERMNAME "aterm"
#define DELTERMNAME "dterm"

#define ADDTERM 1
#define DELTERM 2

#define TRUE 	1
#define FALSE	0
#define ERROR	-1
#define OK	1

#define BUFFLEN	128		/* general line buffer length */
#define FILELEN 80		/* filename buffer length */



/* 

National Institute of Standards and Technology
This software is property of the United States Government.  

This software must only be used on data files that are part of the
NIST SQL Test Suite.  This software is covered under the same terms,
conditions, restrictions and disclaimers provided in your Software
License Agreement.  The customer will not resell, copy, modify, or
otherwise reproduce, or allow to be reproduced, any portion of the
software.  

*/


/*
	"main.c" - global variable declarations and routines for
		"terminator" and "unterminator"
	main() - parses command line options, initializes globals,
		 performs main event loop for batch mode.
	readline() - reads line of text with \n's still attached
	usage() - describes how to invoke from the command line.
	open_file() - tries to open a file, messages if unable.
	nomem() - called when {m,c,re}alloc fails to print msg. and exit.
	get_src_name() - reads next source filename for batch mode,
		optionally an output filename and a new commands file.
*/
	

#ifndef MYTYPES
#include "mytypes.h"
#endif

struct lines line_head;		/* head node */
int line;			/* misc. globals */
int mode;			/* Add terminators or delete terminators? */
int ig_case;			/* when substr() should ignore case */
int verbose;			/* runtime verbose mode */
int save_extra;			/* save extra bytes for adding strings */
char out_file[FILELEN];		/* output filename (may differ from input) */
char src_file[FILELEN];		/* input filename */
char term_str[FILELEN],		/* new SQL terminator string */
     cont_str[FILELEN];		/* new SQL continuation string */
FILE *f_fp;			/* for -f option; src of filenames */

main(argc, argv)
int argc;
char **argv;
{
    char *t;			/* generic ptr */
    char buff[BUFSIZ];		/* buffer for reading options */

    f_fp = NULL;
    *out_file = '\0';
    verbose = 0;
    ig_case = 0;

/*  Select mode of operation; defaults to aterm until dterm is completed. */

    mode = ADDTERM;

    if(argc > 1 && !strncmp("-v", argv[1], 2))
	verbose = verbose ? FALSE : TRUE;

    printf("Do you want to use a list-of-files file? (y/n) ");
    (void)fgets(buff, BUFSIZ-1, stdin);
    for(t = buff; ISWHITE(*t); ++t)
	;
    if(*t == 'y' || *t == 'Y') {
	printf("Enter the list-of-files filename: ");
	if(fgets(buff, FILELEN-1, stdin) == NULL) {
	    fprintf(stderr, "\nEOF reading list-of-files filename\n");
	    exit(1);
	}
	if((f_fp = open_file(zap_lf(buff), "r")) == NULL)
	    exit(1);
	get_src_name();
    }
    else {
	printf("Enter the input filename: ");
	if(fgets(src_file, FILELEN-1, stdin) == NULL) {
	    fprintf(stderr, "\nEOF reading input filename\n");
	    exit(1);
	}
	zap_lf(src_file);

	printf("Enter the output filename (<RETURN> to overwrite input): ");
	if(fgets(out_file, FILELEN-1, stdin) == NULL) {
	    fprintf(stderr, "\nEOF reading output filename\n");
	    exit(1);
	}
	
	if(*out_file == '\n')
	    (void)strcpy(out_file, src_file);
	else
	    zap_lf(out_file);
    }

get_terms:
    printf("\nEnter continuation string (<RETURN> for none): ");
    if(fgets(cont_str, FILELEN-1, stdin) == NULL) {;
	fprintf(stderr, "\nEOF reading cont_str\n");
	exit(1);
    }
    zap_lf(cont_str);

    printf("Enter terminator string (<RETURN> for none): ");
    if(fgets(term_str, FILELEN-1, stdin) == NULL) {
	fprintf(stderr, "\nEOF reading term_str\n");
	exit(1);
    }
    zap_lf(term_str);

    if(!(*term_str || *cont_str)) { 	/* I'm sorry, I just HAD to */
	printf("\nA terminator and/or a continuation string must ");
	printf("be entered.\n\n");
	goto get_terms;			/* use ONE goto!  :-)  */
    }

    save_extra = (strlen(term_str) > strlen(cont_str)) ? strlen(term_str) :
		 strlen(cont_str);

    if(verbose) {
	printf("\nInput  filename: '%s'\nOutput filename: '%s'\n",
	       src_file, out_file);
	printf("Continuation String: ");
	if(*cont_str)
	    printf("'%s'\n", cont_str);
	else
	    puts("(none)");
	printf("Termination String: ");
	if(*term_str)
	    printf("'%s'\n", term_str);
	else
	    puts("(none)");
    }


					/* main event loop */
    while(*src_file != '\0') {
	int status;
	if((status = read_src_lines()) == ERROR)
	    printf("\nHad troubles reading source lines...ignoring file.\n");

	if(status == OK) {
	    if(mode == ADDTERM) {
		if((status = addterm()) == ERROR)
		    printf("\nHad problems adding terminators!\n");
	    }
	    else if(mode == DELTERM)
		if((status = delterm()) == ERROR)
		    printf("\nHad problems removing terminators!\n");
	    
	    if(status == OK) {
		if(write_src_lines() == ERROR)
		    printf("\nHad troubles writing source lines...beware!\n");
	    }
	    else
		printf("Not writing any output...\n");
	}	

	free_lines();
	if(f_fp)		/* reading filenames from a file? */
	    get_src_name();	/* get the next name! */
	else
	    *src_file = '\0';	/* otherwise, we're done */
    }

    if(verbose)
    	printf("\nNormal exit.\n");
    exit(0);
}


/*  read line into s from input pointed to by fp.  Leaves trailing \n */

char *readline(s, fp)
char *s;
FILE *fp;
{
    char *t;

    ++line;

    if(fgets(s, BUFFLEN - 1, fp) == NULL)
	return(NULL);

    if((t = strrchr(s, '\n')) == NULL)
	printf("\nWARNING: line %d truncated during read.\n", line);
    else
	*t = '\0';

    return(s);
}



usage()
{

    fprintf(stderr, "usage:  %s\n", ADDTERMNAME);
    fprintf(stderr, "   or:  %s\n", DELTERMNAME);
/*
    fprintf(stderr, "usage:  %s [option_keys] [options] [source_file]\n",
	    ADDTERMNAME);
    fprintf(stderr, "   or:  %s [option_keys] [options] [source_file]\n\n",
	    DELTERMNAME);
    fputs("where [option_keys] is a single grouping of a minus sign\n",
	  stderr);
    fprintf(stderr,"followed by one or more of the following letters: fiov\n",
	    stderr);
    fputs("\t-f file_of_filenames\n\t", stderr);
    fputs("-i input_source_file\n\t", stderr);
    fputs("-o output_filename\n\t", stderr);
    fputs("-v turns verbose mode ON.\n", stderr);
    fprintf(stderr, "Example:  %s -ovi out in\n", ADDTERMNAME);
    fputs("sets output file to 'out', verbose mode, input file to 'in'.\n",
	  stderr);
    fputs("If the -i option is not used, [source_file] is required.\n",
	  stderr);
    fputs("Note order of [options] must be same order as [option_keys]\n",
	  stderr);
*/
    exit(1);
}



FILE *open_file(file, mode)
char *file, *mode;
{
    FILE *fp;
    
    if((fp = fopen(file, mode)) == NULL) {
	(void)putchar('\n');
	perror(file);
/*	fprintf(stderr, "Trying %s...\n", strucpy(file, file));
	if((fp = fopen(file, mode)) == NULL) {
	    perror(file);
	}
*/
    }
    return(fp);
}



nomem(s)
char *s;
{
    fprintf(stderr, "Line %d:  No memory for %s\n", line, s);
    exit(1);
}


/* assumes we already have an opened "f_fp" which holds lines with 
   one or two words on them.  The first is the source input filename,
   and if the second exists, it's the output filename for the source.
   If the output filename is not specified, the input filename is
   overwritten when the commands are completed.

   Couldn't used fscanf because it'll keep reading to find three arguments,
   whether seperated by whitespace or not.  Using fgets/sscanf lets me be
   sure I'm on a line-by-line basis.
*/
get_src_name()
{
    char buff[BUFFLEN];
    int i;

    *src_file = *out_file = '\0';
    
    if(fgets(buff, BUFFLEN, f_fp) == NULL) {	/* done reading names... */
	if(fclose(f_fp) != 0)
	    printf("Had trouble closing file of filenames...\n");
	if(verbose)
	    puts("\nDone reading file of filenames...\n");
	return;
    }

    i = sscanf(buff, "%s %s", src_file, out_file);

    switch(i) {
 	case 1 : (void)strcpy(out_file, src_file); /* didn't get out_file */
		 break;
	case 2 : /* Got both input and output file specs\n"); */
	         break;
	case 0 : /* fallthrough */
	default:
	    	 printf("Error reading -f args...trying again...\n");
	    	 get_src_name();
	    	 break;
    }

    printf("Input file: '%s', Output file: '%s'\n", src_file, out_file);
}



/*
  	Source:  strutil.c - standard string utilities (and some not-
			     so-standard)

	   *strcpy(s, t) - copies string "t" into string "s", even if
	   		the strings overlap (see comments below).
	   *strchr(s, c) -  returns ptr to a char "c" in "s", searching
			in the forward direction
	   *strrchr(s, c) - returns ptr to a char "c" in "s", searching
			in the reverse direction.
	   *strucpy(s, t) - copies t into s while converting to upper case
	   *strupper(s) - returns ptr to (static) copy of s converted to
	    		upper case.
	   *strlower(s) - same as strupper but to lower case.
	    strigcmp(s, t) - does strcmp on s and t while ignoring case.
	   *stristr(s, t) - does substr while ignoring case.  Uses substr.
	   *substr(s,t) - ptr to t if s is substring of t.
	   *strsave(s) - returns pointer to new copy of a string.
	    getword(s,w) - copies word starting @ is into w, surrounded by
			whitespace.  Returns # of chars scanned thru in s.
	    getlastword(s,w) - copies last word on line s into w.
	   *zap_lf(s) - delete trailing linefeed (\n) from string.

  * - functions marked with asterisk return type (char *)

All these routines are my original code, sometimes using guidelines
from various man pages.

Some compilers don't like to have library functions redefined.  See
notes before these routines to see why I included them here.  If you
think they're ok (and the usually should be), use "#define STRLIB_OK"
*/

#define STRLIB_OK


#ifndef MYTYPES
#include "mytypes.h"
#endif

#ifndef NULL
#  include <stdio.h>
#endif

#include <ctype.h>


/*
  Many man pages warn about doing string copies with pointers that
  overlap.  If you suspect strange things are happening, especially
  with string substitutions, use this strcpy() rather than the one
  provided with your library.
  
  NOTE:  VAX VMS "C" gets VERY confused with this routine defined.
*/

#ifndef STRLIB_OK		/* begin filter of std lib funcs */

#ifndef VAX
char *strcpy(s1, s2)
register char *s1, *s2;
{   char *save = s1;

    while(*s1++ = *s2++)
	;
    return(save);
}
#endif


/* 
  Some systems don't have strchr() and strrchr(), and some have index()
  and rindex() instead, so let's just define our own and forget it!
  
  strchr(s, c) returns a pointer to the first occurrence of character
  `c' in string `s', or NULL if `c' does not occur in the string.
  Note that the NULL character terminating a string *is* considered
  to be part of the string.
*/

    

char *strchr(s, c)
register char *s, c;
{
    while(*s)
	if(*s == c)
	    return(s);
        else ++s;
    return((c == '\0') ? s : NULL);
}


char *strrchr( s, c )
register char *s, c;
{	register char *p;

	for(p = s + strlen(s); p >= s; --p) 
		if(*p==c) 
			return(p);
	return (NULL);
}


#endif /* end filter of std lib funcs */


/* strlcpy(s, t) - copies string "t" into "s" while converting to lower case.
   Note:  tolower(c) may not return a defined result if character 'c' is
   not an upper case character.
*/

char *strlcpy(s, t)
register char *s, *t;
{  char *save = s;
   if(s == NULL || t == NULL)
	return(NULL);
    for(; *t; ++t, ++s)
	*s =  (isupper(*t)) ? tolower(*t) : *t;
   *s = '\0';
   return(save);
}



/* strucpy(s, t) - copies string "t" into "s" while converting to upper case */

char *strucpy(s, t)
register char *s, *t;
{  char *save = s;
   if(s == NULL || t == NULL)
	return(NULL);
    for(; *t; ++t, ++s)
	*s =  (islower(*t)) ? toupper(*t) : *t;
   *s = '\0';
   return(save);
}



/*
  stristr(search, text) - ignore case when checking if s is a substring of t.
  returns ptr in t or NULL if not a substring.  Called by *and* uses substr().
*/

char *stristr(sea, txt)
char *sea, *txt;		/* search string / text string */
{
    char *s, *t, *found;
    int pos;

    (void)strlcpy((s = strsave(sea)), sea); /* convert to one case... */
    (void)strlcpy((t = strsave(txt)), txt);
    ig_case = 0;		/* avoid recursing again in substr() */
    found = substr(s, t);
    pos = found - t;
    free(s), free(t);
    ig_case = 1;		/* reset for next time */
    return(found ? txt + pos : NULL);
}

/* substr(s,t):  if s is substring of t, return ptr at location in t.
   Otherwise, return NULL.  Note:  '\0' *is* considered as part of the
   string.
   If ig_case (global int) is set, then stristr() is called with the
   same arguments for finding substrings while ignoring case.
 */

char* substr(s,t) 
register char *s, *t; 
{  register char *ss, *tt;
   char *strchr();

   if (s == NULL || t == NULL)          /* Initial checks... */
     return(NULL);
   if(*s && !*t)			/* watch for empty strings... */
       return(NULL);
   if(!*s)
       return(t + strlen(t));

   if(ig_case)
       return((char *)stristr(s,t));

   do {  ss = s;
         if((tt = strchr(t, *ss)) == NULL)
	     return(NULL);

         t = tt + 1;		/* advance if we didn't match */

         while((*(++ss) == *(++tt)) && *tt)
		;
      }  while(*ss != '\0');
   return(t - 1);
}



char *strsave(t)			/* copy t into s with save_extra */
char *t;
{   char *s;
    if(t == NULL) return(t);
    if((s = (char *)malloc((unsigned)(strlen(t) + save_extra + 2))) == NULL)
    {	fprintf(stderr, "no memory for strsave %d bytes!\n", strlen(t));
	exit(1);
    }
    return(strcpy(s, t));
}



/* getlastword(s, w) - copies last word on line into w.  Skips trailing
   blanks.  Useful for finding close braces for if/then, for blocks.
   Returns pointer to beginning of word in original string.
*/

char *getlastword(s, w)
register char *s, *w;
{
    register char *tail = s + strlen(s) - 1;
    register char *front = tail;

    while(tail > s && (ISWHITE(*tail))) {
	--tail;
	--front;
    }

    while(front > s && (!ISWHITE(*(front-1))))
	--front;

    (void)strncpy(w, front, tail - front + 1);
    w[tail - front + 1] = '\0';		/* make certain NULL terminated */

    if(ISWHITE(*w)) 			/* special case - all white line */
	*w = '\0';

    
#ifdef DEBUG
   printf("getlastword('%s') is '%s'\n", s, w);
#endif
    return(front);
}



/* read a word starting @ s and copy into w.  Skips preceding white space,
   stops at whitespace.  Returns total number of characters skipped through
   in string s.
*/

getword(s, w)
register char *s,  *w;
{   register char *t = s;	    /* remember start of string */

    while(*s && ISWHITE(*s))	    /* skip over preceding whitespace */
      ++s;

    while(*s && !ISWHITE(*s))	    /* copy the bugger */
      *w++ = *s++;

    *w = '\0';			    /* terminate string */
    return(s - t);
}



/* zap (delete) trailing linefeeds - useful for strings obtained
   via fgets().  Returns beginning of input string.
*/
char *zap_lf(s)
char *s;
{
    char *t, *save = s;
    if((t = strrchr(s, '\n')) != NULL)
	*t = '\0';
    return(save);
}



/*
  	src.c - routines having to do with the source text file which will
		be manipulated by the contents of the change file.
		
	get_line_obj() - allocates memory for linked line structure
	read_src_lines() - reads lines from src_file to linked list of lines
	write_lines() - writes lines to out_file
	print_lines() - prints src lines to stdout (debugging)
	print_lines_rev() - traverses linked list of lines in reverse to
		verify links (debugging).
	free_lines() - destroys linked list of lines
	free_line_obj() - called by free_lines for each object
	init_lines() - inits head node and line counter
*/

#ifndef MYTYPES
#include "mytypes.h"
#endif


struct lines *get_line_obj()
{
    struct lines *lp;

    if((lp = (struct lines *)malloc(sizeof(struct lines))) == NULL)
	nomem("source line structure");

    lp->text = NULL;
    lp->next = lp->prev = NULL;

    return(lp);
}



/* opens source file and reads lines into a null-terminated 
   doubly linked linear list (phew!).
*/

read_src_lines()
{
    FILE *fp;
    char buff[BUFFLEN];
    struct lines *lp, *lp_last = &line_head;
    int n = 0;

    init_lines();

    if((fp = open_file(src_file, "r")) == NULL)
	return(ERROR);
    if(verbose)
	printf("\nReading %s...", src_file);

    while(readline(buff, fp) != NULL) {
	lp = (struct lines *)get_line_obj();

	lp->text = strsave(buff);
	lp->line_num = ++n;

	lp->prev = lp_last;	/* link in new node at end of list*/
	lp->next = NULL;
	lp_last->next = lp;
	lp_last = lp;		/* save for next append */
#ifdef DEBUG
	printf("%d<%s\n", n, lp->text);
#endif
    }
    if(fclose(fp) == EOF)
	fprintf(stderr, "Had trouble closing %s\n", src_file);
    if(verbose)
    	printf("done!  Read %d lines.\n", n);
    return(OK);
}



write_src_lines()
{
    FILE *fp;
    struct lines *lp;

    if(verbose)
    	printf("\nWriting output file '%s'...", out_file);

    if((fp = open_file(out_file, "w")) == NULL)
	return(ERROR);

    for(lp = line_head.next; lp != NULL; lp = lp->next) {
	fputs(lp->text, fp);
	(void)fputc('\n', fp);	/* fput's are faster!! */
    }

    if(fclose(fp) == EOF) {
	fprintf(stderr, "\nHad trouble closing file %s\n", out_file);
	return(ERROR);
    }

    if(verbose)
	printf("done!\n");

    return(OK);
}



#ifdef DEBUG
print_lines()
{
    register struct lines *lp;

    printf("\n*******  Source Lines from %s:\n", src_file);
    for(lp = line_head.next; lp != NULL; lp = lp->next)
	printf("'%s'\n", lp->text);
}



print_lines_rev()
{
    register struct lines *lp;
    
    printf("\n*******  Traversing source linked list backwards \n");
    for(lp = line_head.next; lp->next != NULL; lp = lp->next)
	;
    for(lp = lp; lp != &line_head; lp = lp->prev)
	printf("'%s'\n", lp->text);
}

#endif

free_lines()
{
    register struct lines *lp, *lp_next;

    for(lp = line_head.next; lp != NULL; lp = lp_next) {
	lp_next = lp->next;
	free_line_obj(lp);
    }
}



free_line_obj(lp)
struct lines *lp;
{
	if(lp->text)
	    free(lp->text);
	else
	    printf("strange condition during free_lines() - text was NULL\n");
	free((char *)lp);
}


init_lines()
{
    line_head.text = "Head Node";
    line_head.next = NULL;
    line_head.prev = &line_head; 	/* makes for easer line inserts */
    line = 0;
}



/* 	addterm.c - add continuation EOLN marker and terminator to
		    interactive SQL programs.
*/

#ifndef MYTYPES
#include "mytypes.h"
#endif

/* Note:  we can just tack on the cont_str or term_str since the lines
   were allocated with the length of the lines + the longer of (cont_str,
   term_str).  See main():save_extra; strutil.c:strsave();
*/


addterm()
{
    char word[BUFFLEN];
    struct lines *lp;
    int done, status = OK;

    for(lp = line_head.next; lp; lp = lp->next) {
	getword(lp->text, word);
	if(*word && strncmp(word, "--", 2)) {	/* beginning of SQL stmnt */
	    done = check4term(lp->text);
	    if(done) {				/* add SQLTERMinator */
		char *t;
		if(verbose)
		    printf("Adding SQLTERM to FIRST SQL line %d\n",
			   lp->line_num);
		if((t = strrchr(lp->text, SQLTERM)) == NULL)
		    printf("Huh?  Lost SQLTERM '%c'\n", SQLTERM);
	    	else
		    (void)strcpy(t, term_str);
	    }
	    else {
		if(verbose)
		    printf("Adding continuation string to FIRST SQL line %d\n",
			   lp->line_num);
		(void)strcat(lp->text, cont_str);
	    }
	}
/* we are now in the middle of an SQL statement */
	while(!done) {
	    if(!(lp = lp->next)) {
		printf("Ack!  Found last line in middle of SQL statement\n");
		done = TRUE;
		status = ERROR;
	    }
	    else {
		done = check4term(lp->text);
		if(done) {			/* add SQLTERMinator */
		    char *t;
		    if(verbose)
			printf("Adding SQLTERM to line %d\n", lp->line_num);
		    if((t = strrchr(lp->text, SQLTERM)) == NULL)
			printf("Huh?  Lost SQLTERM '%c'\n", SQLTERM);
		    else
			(void)strcpy(t, term_str);
		}
		else {
		    if(verbose)
			printf("Adding continuation string to line %d\n",
			       lp->line_num);
		    (void)strcat(lp->text, cont_str);
		}
	    }
	}
    }
    return(status);
}


/* return TRUE if the SQL terminator is found at the end of a line
   on a non-comment line that is not a blank line.
*/
check4term(s)
char *s;
{
    char word[BUFFLEN], *t;

    getword(s, word);
    if(!*word || !strncmp(word, "--", 2))
	return(FALSE);

/* check if SQL terminator ';' is at the end of the last word on a line */

    (void)getlastword(s, word);
    t = word + strlen(word);
    if(*word)			/* careful with empty last words... */
	--t;

    if(verbose)
	printf("Looking for '%c' in word '%s' at '%c'(%d)\n",
	       SQLTERM, word, *t, *t);

    return((*t == SQLTERM) ? TRUE : FALSE);
}



/*	delterm.c - delete continuation EOLN marker and terminator to
		    interactive SQL programs.
*/

#ifndef MYTYPES
#include "mytypes.h"
#endif

delterm()
{

    printf("delterm()\n");
    printf("Sorry, delete terminator function not implemented yet.\n");
    printf("Ignore the following error message.\n");

    return(ERROR);

}


