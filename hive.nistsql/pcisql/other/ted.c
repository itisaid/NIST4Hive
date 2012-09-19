/* 

National Institute of Standards and Technology
This software is property of the United States Government.  

This software is covered under the same terms, conditions,
restrictions and disclaimers provided in your Software License
Agreement.

*/

#define VERSION "5.1 5/17/95"


/* 
    "mytypes.h" - global definitions for the editor project. (TED) 
*/

			/* COMPILIATION OPTIONS */


/*  Comment out the following #define if the compiler cannot compile
    this file because of a "segment too large" or some similar error.
    The help() function is essentially removed for interactive command
    input if HELP_FUNC is not defined.
*/ 

#define HELP_FUNC


/*  Source code is included for the common library functions strcpy,
    strchr, and strrchr.  These are critical to the operation of TEd, but
    are generally not required to be recompiled here.  In fact, some
    systems will not like these functions redefined from their library
    counterparts.  If you think your library functions are ok - and they
    usually are - define STRLIB_OK to use system libraries.
*/

#define STRLIB_OK



/*   If your system C libraries include getopt(), your linker may have
     troubles with having the global variables and getopt() routine
     doubly included with this code.
     Compilation/link errors that point to "opterr", "optind", "optopt",
     or "optarg" are related to getopt().  Although most systems don't
     have troubles, define USE_MY_GETOPT to use the included getopt().
*/

#define USE_MY_GETOPT



/*  When the user is typing commands in the interactive input mode,
    we don't want them to type the EOF character as we may need future
    input.  IACT_TERM is a string that must appear at the beginning of
    a line to act as an EOF marker.  This is modeled conceptually
    from the Unix "Mail" program.
*/

#define IACT_TERM "."


/*  This is the default prompt string when getting interactive input.
    It may change on the fly (i.e., for entering subcommands) but for
    now, it just "prompts."  Change to suite taste.
*/

#define IACT_PROMPT "> "


			/* END OF COMPILATION OPTIONS */

 
#define MYTYPES			/* allows concatenation of all source files */

#include <stdio.h>
#include <string.h>

struct lines {			/* doubly linked linear linked list */
    char *text;
    struct lines *next, *prev;	/* forward, backward ptrs */
};


struct commands {		/* linear linked list */
    char type;			/* INSA, INSB, DEL, REP, SUB */
    char *filespec;		/* which file(s) command applies to */
    char *start_str, *end_str,	/* search range of text OR... */
    	 *match_str;		/* search for lines w/matching strings */
    char **text;		/* lines of text for INSA, INSB, REP. */
    char *search_str, *rep_str;	/* for SUB command */
    int  options;		/* ECHO, IG_CASE, SUB_ALL */
    char *src_file;		/* filename where cmds came from */
    int  line;			/* line no. where cmd is in cmds file */
    int	 debug;			/* debugging flags */
    struct commands *next;	/* forward ptr */
};


/*  These are defined in main.c (just declared here) */

extern struct lines line_head;		/* head node */
extern struct commands cmd_head;	/* head node */
extern int ungetline, line;		/* misc. globals */
extern int ig_case;			/* when to ignore case in substr() */
extern int verbose;			/* runtime verbose mode */
extern int src_mod;			/* input source modified? */
extern int iact_mode;			/* interactive command input */
extern int iact_eof;			/* EOF found on interactive input */
extern char *prompt;			/* if not null, prompt for input */

extern char src_file[],			/* the current source filename */
    	    cmd_file[],			/* commands filename */
    	    out_file[];			/* output filename */

					/* my str library functions */
char *substr(), *strsave(), *strupper(), *readline(), *strmrep();
char *strchr(), *strrchr(), *strucpy();	
char *cmd_str(), *strlcpy(), *stristr(), *getqstr();

char *malloc(), *realloc(), *calloc();	/* commonly found in malloc.h */
struct lines *search_lines(), *get_line_obj(), *insert_text();
struct lines *insert_line(), *delete_line();
struct commands *get_cmd_obj();
FILE *open_file();

#define ISWHITE(c) (((c) == ' ' || (c) == '\t' || (c) == '\n') ? 1 : 0)

#define TRUE 	1
#define FALSE	0
#define ERROR	-1
#define OK	1

#define INSA 	1		/* command types */
#define INSB	2
#define DEL	3
#define REP	4
#define SUB	5
#define COMMENT 6
#define HELP	7

/*  command structure's option flag bits
    Update cmd.c:print_cmd_obj() if more are added! */

#define ECHO	0x1		/* 'verbose' mode -- echo changes */
#define IG_CASE	0x2		/* ignore case when matching char strings */
#define GLOB	0x4		/* <search_spec> is a pattern, not strng */
#define MACRO	0x8		/* command strings contain macros */
#define CTRLS	0x10		/* command strings contain embedded ctrls */

#define BUFFLEN	256		/* general line buffer length */
#define FILELEN 128		/* filename buffer length */

#define PLURAL(a) ((a == 1) ? "" : "s")

#define ishexdigit(c) (c >= '0' && c <= '9' || ((c >= 'a' && c <= 'f') \
	|| (c >= 'A' && c <= 'F')))



/*
	"main.c" - global variable declarations and routines for TED.

*/
	
#ifndef MYTYPES
#include "mytypes.h"
#endif

struct lines line_head;		/* head node */
struct commands cmd_head;	/* head node */
int ungetline, line;		/* misc. globals */
int ig_case;			/* when to ignore case on string comps */
int verbose;			/* runtime verbose mode */
int src_mod;			/* input source lines modified? */
int iact_mode;			/* interactive command input */
int iact_eof;			/* EOF found on interactive input */
char *prompt;			/* if not null, prompt for input */
char src_file[FILELEN];		/* the current source filename */
char cmd_file[FILELEN];		/* change commands filename */
char out_file[FILELEN];		/* output filename (may differ from input) */
FILE *f_fp;			/* for -f option; src of filenames */

main(argc, argv)
int argc;
char **argv;
{
    int c;
    extern int optind;
    extern char *optarg;

    printf("NIST SQL Test EDitor \"TEd\" vers %s\n", VERSION);
 
    (void)strcpy(cmd_file, "tedchg"); /* default cmds filename */
    *out_file = '\0';
    f_fp = NULL;
    init_cmds();

    verbose = ig_case = iact_eof = iact_mode = 0;

    while((c = getopt(argc, argv, "f:o:t:vi")) != EOF)
	switch(c) {
	    case 'f' :	if((f_fp = open_file(optarg, "r")) == NULL)
			    exit(1);
			if(verbose)
			    printf("Reading filenames from '%s'\n", optarg);
			break;
	    case 'i' :	iact_mode = 1;
			printf("Interactive command input mode set.\n");
			(void)strcpy(cmd_file, "standard input");
			break;
	    case 'o' :	(void)strcpy(out_file, optarg);
			break;
	    case 't' :	(void)strcpy(cmd_file, optarg);
			read_cmds();
			break;
	    case 'v' :  verbose = verbose ? 0 : 1;
#ifndef DEBUG
			if(verbose)
#endif
			    printf("Verbose %s\n", verbose ? "on" : "off");
			break;
	    default  :  usage(*argv);
			break;
	}

    if(argv[optind]) { 			/* get input filename */
	(void)getqstr(src_file, argv[optind]);
	if(!*out_file) {		/* set output filename if not set */
	    (void)strcpy(out_file, src_file);
	}
	else {
	    if(argv[optind + 1]) {
		printf("Cannot use -o with multiple files on cmd line!\n");
		exit(1);
	    }
	}
    }
    else if(f_fp)
	get_src_name();
    else
	usage(*argv);
				/* no new filename was given with -t */
    if(!strcmp(cmd_file, "tedchg") || !strcmp(cmd_file, "standard input"))
	read_cmds();


/*  Check to make sure we can open the file before allowing the user to 
    waste their time typing in commands interactively.  We don't try this
    if they are using the list-of-files (-f) option.  Darn considerate,
    if you ask me!
*/
    if(iact_mode && !f_fp) {
	FILE *tmpfp;
	if((tmpfp = open_file(src_file, "r")) == NULL) {
	    printf("\nWe're about ready to have you type some commands, but ");
	    printf("that would be rather \nfutile since I can't open the ");
	    printf("input file called '%s'.  Try again soon!\n", src_file);
	    exit(-1);
	}
	else
	    if(fclose(tmpfp))	/* we're just checking if it could be opened */
		fprintf(stderr, "Had trouble closing %s\n", src_file);

    }

    if(iact_mode) {
	char y[BUFFLEN];
	
	if(cmd_head.next == NULL) { /* any commands on the linked list? */
	    printf("\nNo valid commands were entered. Nothing changes...\n");
	    exit(1);
	}
	
	*y = '\0';
	
	print_cmds();
	printf("\nExecute these commands?  (y/n) ");
	(void)fflush(stdout);
	if(fgets(y, BUFFLEN-1, stdin) == NULL) {
	    fprintf(stderr, "\nError reading reply...see ya.\n");
	    exit(1);
	}
	else {		/* try hard to get a good answer. */
	    while (*y != 'y' && *y != 'n') {
		printf("\nExecute these commands?  (y/n) ");
		(void)fflush(stdout);
		if(fgets(y, BUFFLEN-1, stdin) == NULL) {
		    fprintf(stderr, "\nError reading reply...see ya.\n");
		    exit(1);
		}
	    }
	    if(*y == 'n') {
		printf("\nOK, fine.  I won't do anything then.  Humph.\n");
		exit(1);
	    }
	}
    }

    while(*src_file != '\0') {
	printf("\n\nInput: %s    Output: %s\n\n",
	   src_file, strcmp(out_file, src_file) ? out_file : "(same)");

	if(read_src_lines() == ERROR) {
	    printf("\nHad troubles reading source lines...fix and try again.\n");
	}
	else {
	    process_cmds();
	    if(write_src_lines() == ERROR)
		printf("\nHad troubles writing source lines...beware!\n");
	}
	free_lines();
	if(f_fp)			/* reading filenames from a file? */
	    get_src_name();
	else if(argv[++optind]) {	/* multiple files on cmd line */
	    (void)getqstr(src_file, argv[optind]);
	    (void)strcpy(out_file, src_file);
	}
	else
	    *src_file = '\0';	/* otherwise, we're done */
    }

    free_cmds();
    if(verbose)
    	puts("\nNormal exit.");
    exit(0);
}


/*  read line into s from input pointed to by fp.  
    will put a prompt string for interactive input when required.
*/

char *readline(s, read_fp)
char *s;
FILE *read_fp;
{
    static char lastline[BUFFLEN];

    if(iact_eof)
	return(NULL);

    if(prompt != NULL && !ungetline) {	/* prompt for interactive cmd input */
	(void)fputs(prompt, stdout);
	(void)fflush(stdout);
    }

    if(ungetline)
    {	ungetline = 0;
	return(strcpy(s, lastline));
    }
    else
	++line;

    if(fgets(s, BUFFLEN - 1, read_fp) == NULL)
	return(NULL);

    if(strrchr(s, '\n') == NULL)
	fprintf(stderr,
"Warning line %d exceeds maximum input buffer of %d bytes. \n", line,
		BUFFLEN -1);

    (void)strcpy(lastline, s);	/* save for possible ungetline next time */

    return(s);
}



usage(me)
char *me;
{
    fprintf(stderr, "usage:  %s [options] source_file \n", me);
    fprintf(stderr, "where [options] can be the following:\n\t");
    fprintf(stderr, "-f file_of_files\n\t-t cmds_file\n\t-o output_file\n\t");
    fprintf(stderr, "-v turns verbose mode ON.\n\t");
    fprintf(stderr, "-i enables interactive command input mode\n");
    exit(1);
}


process_cmds()
{
    struct commands *cp, *savecp;

    if(verbose)
    	printf("\nP R O C E S S   C O M M A N D S \n\n");

    savecp = get_cmd_obj();	/* get a spare for macros */

    for(cp = cmd_head.next; cp != NULL; cp = cp->next)
	if(check_filespec(cp)) {
	    ig_case = cp->options & IG_CASE ? 1 : 0;
	    if(cp->options & MACRO) {
		copy_cmd_obj(savecp, cp);
		do_macros(cp);	/* modify cp with macro replacements */
	    }
	    if(cp->options & CTRLS)
		do_ctrls(cp);	/* backslash controls embedded */

	    if(verbose || cp->options & ECHO) {
		puts("---------");
		print_cmd_obj(cp);
	    }
	    switch(cp->type) {
		case INSB :	insert_b(cp);
		    		break;
		case INSA :	insert_a(cp);
		    		break;
		case DEL  :
		case REP  :	delete_em(cp);
				break;
		case SUB  :	sub(cp);
				break;
		default   : 	fprintf(stderr,
					"\nACK!  What is cp->type = %d??\n",
					cp->type);
				break;
	    }
	    if(cp->options & CTRLS)	/* expand \n's into real lines */
		fix_nls();

	    if(cp->options & MACRO) 	/* restore old cp for next src file */
		copy_cmd_obj(cp, savecp);
	    
	}
    free_cmd_obj(savecp);
}



FILE *open_file(file, mode)
char *file, *mode;
{
    FILE *open_fp;
    
    if((open_fp = fopen(file, mode)) == NULL) {
	perror(file);
    }
    return(open_fp);
}


check_filespec(cp)
struct commands *cp;
{
    /* comparisons are done is lower case (case insensitive) */
    char *src_copy = strsave(src_file);
    char *spec_copy= strsave(cp->filespec);
    int status = 0;		/* default is negative match */
    
    strlcpy(src_copy, src_copy);
    strlcpy(spec_copy, spec_copy);
    
    status = glob(src_copy, spec_copy);
    if(!status) {
	if(verbose) {
	    printf("\nCommand %s at line %d in %s was not executed.\n",
		   cmd_str(cp->type), cp->line, cp->src_file);
	    printf("(the source file '%s' didn't match the filespec '%s'.)\n\n",
		   src_copy, spec_copy);
	}
    }
    free(src_copy);
    free(spec_copy);
    return (status);
}



nomem(s)
char *s;
{
    fprintf(stderr, "Line %d:  No memory for %s\n", line, s);
    exit(1);
}


/* assumes we already have an opened "f_fp" which holds lines with 
   one to four words per line. The first is the source input filename,
   and if the second exists, it's the output filename for the source.
   If the third word exists, it is the name of a new change file.
   If the fourth word exists, all existing commands are wiped out before
   reading the new commands from argument 3.  Otherwise, commands in
   argument 3 are tacked on to the tail of any previously read commands.

   If the output filename is not specified, the input filename is
   overwritten when the commands are completed.
*/
get_src_name()
{
    char buff[BUFFLEN], command[FILELEN], wipe[FILELEN];
    char *bp = buff;
    int i;

    *src_file = *out_file = *command = '\0';
    
 loop:
    if(fgets(buff, BUFFLEN, f_fp) == NULL) { /* done reading names... */
	if(fclose(f_fp))
	    fprintf(stderr, "Had trouble closing file of filenames...\n");
	if(verbose)
	    puts("\nDone reading file of filenames...\n");
	return;
    }
    if(strrchr(buff, '\n') == NULL)
	fprintf(stderr,
		"\nWarning line %d:  Following line cut to %d chars.\n'%s'",
		line, BUFFLEN -1, buff);

    if(*buff == '!')		/* One "goto" required per program.  :-) */
	goto loop;

    bp = buff;
    bp = getqstr(src_file, bp);		i  = strlen(src_file) ? 1 : 0;
    bp = getqstr(out_file, bp);		i += strlen(out_file) ? 1 : 0;
    bp = getqstr(command, bp);		i += strlen(command) ? 1 : 0;
    bp = getqstr(wipe, bp);           i += strlen(wipe) ? 1 : 0;

    switch(i) {
    case 4 : printf("Flushing out all previously loaded commands..");
	free_cmds();
	putchar('.');
	init_cmds();
	puts(".done");
    case 3 : (void)strcpy(cmd_file, command);
	printf("\nNew command file '%s'\n\n", command);
	if(iact_mode) {
	    puts("*** Switching from Interactive to Batch mode\n");
	    iact_mode = 0;
	    prompt = NULL;
	}
	read_cmds();
	break;
    case 1 : (void)strcpy(out_file, src_file); /* didn't get out_file */
	break;
    case 2 :			/* Got both input and output file specs\n"); */
	break;
    case 0 :			/* fallthrough */
    default:
	printf("Error reading -f args...trying again...\n");
	get_src_name();
	break;
    }
    printf("\nSource file is '%s', output file is '%s'\n",
	   src_file, out_file);
}



/*  the help function is for help during interactive input of commands.
    Its primary use is to refresh the user's memory on the syntax of a
    command.
    If the user just typed "help", he is given a list of commands to ask
    for more specific help.
*/

help(s)
char *s;
{

#ifndef HELP_FUNC		/* don't compile the help function */
    puts("\nSorry, help was not compiled into this executable.  Define the");
    puts("preprocessor symbol "HELP_FUNC".)\n");
#else

    int i;
    char w[BUFFLEN], *sp = s;
    static char *help[] = {
	"Zero-th element in help?? Unknown command??  Call Chris at (301) 975-3796\n\
telling him how you got this message!",

	"The insert-after command inserts text lines after a particular string\n\
or block of text is found in the source file.  The syntax is:\n\n\
    ins> <filespec> <search_spec> [-options]\n\
    <text>\n\n\
where <search_spec> is a delimited string for either a matching string\n\
(e.g., /PROGRAM/) or two strings delimiting a block of text (e.g.,\n\
/BEGIN DECLARE/END DECLARE/).  The lines contained in <text> are inserted\n\
after each occurrence the <search_spec> is found.  SEE ALSO:  ins<, search\n",

"The insert-before command is exactly like the insert-after command except\n\
that the block of <text> is inserted BEFORE each occurrence of the \n\
<search_spec>.  SEE ALSO:  ins>\n",

"The delete-lines command permits the inclusive deletion of lines matching a\n\
search specification <search_spec>.  Lines that contain a single string\n\
can be deleted by using a <match_string> as the <search_spec>.  A range\n\
of lines can deleted by a <start_string><end_string> pair as the\n\
<search_spec>.  The one-line command syntax is:\n\n\
    del <filespec> <search_spec> [-options]\n\n\
SEE ALSO:  rep, search\n",

"The replace-lines command works exactly like like the delete-lines command\n\
except that after the lines are deleted, the lines included in the block\n\
of <text> are inserted as a replacement.  The syntax is:\n\n\
    rep <filespec> <search_spec> [-options]\n\
    <text>\n\n\
SEE ALSO:  del, search\n",

"The sub command permits string substitutions with the following syntax:\n\n\
    sub <filespec> <search_spec> [-options]\n\
    <search_string><replacement_string>\n\
    [ <search_string><replacement_string> ]\n\n\
The items enclosed in brackets [ ] are optional.  If you have more than one\n\
string to be replaced that meets the same <filespec> and <search_spec>,\n\
simply add a new <search_string><replacement_string> pair underneath.\n\
SEE ALSO:  search\n",

"Comment lines are lines which begin with the comment character,\n\
the exclamation point (!) character.\n",

"Sorry, there's no help for help.  Read the manual.  Get a clue.  :-)\n",

"The <search_spec> is used with all editing commands in TEd.  This allows\n\
each command to operate under three circumstances:\n\n\
   On all lines in the file:                                            //\n\
   On a single line that contains a unique <match> string:       /PROGRAM/\n\
   On a range of lines <start_string><end_string>:             /BEGIN/END/\n\n\
Search criteria are surrounded by delimiters, usually the slash character\n\
as shown above.",

"Options are added to a command by preceding the option letters with a \n\
hyphen after the <search_spec>.  The available options are:\n\n\
    -i    ignore case when searching for <search_spec>s\n\
    -e    echo this command when it is performed\n\
    -p    the <search_spec> has pattern-matching characters:\n\
            asterisk (*) matches zero or more characters\n\
            question (?) matches any ONE character\n\
            brackets [a-z] matches a single character between 'a' and 'z'\n\
            braces {abc,def} matches the string \"abc\" or \"def\"\n\
    -m    macros may be in the <filespec>, <match_string>, <start_string>,\n\
            <end_string> and/or <text> portions of a command.  $if = \n\
            input filename, $of = output filename, $ted = commands \n\
            filename.  Each may be have either an 'h' or 'e' appended to\n\
            specify just the head or the extension of the filename.\n\
            Macros may also be capitalized ($IF) to force upper-case \n\
            translation.\n\
    -c    the following control characters may be embedded in strings:\n\
            \\a, \\b, \\f, \\n, \\r, \\t, \\v, \\\\.  Also, octal or hex\n\
            values may be entered of the form \\nnn or \\xnn, respectively.",

"The <list> command has been included for viewing commands that have been\n\
entered interactively.  Although it can be entered at any time, it is\n\
considered a comment and will terminate multiple-line commands (e.g.,\n\
SUB).",

"BUG!\n"
};


    sp += getword(sp, w);		/* the "help" keyword */
    sp += getword(sp, w);		/* any other interseting info? */

    if(!*w) {				/* just give help on commands */

puts("\nEnter commands just as they would have been entered in a file.");
printf("Type \"%s\" (w/o the quotes) to simulate an end-of-file.\n",
       IACT_TERM);
puts("\nFor additional help, type \"help <cmd>\" where <cmd> is one of the");
puts("following:\n");
puts("    sub        - substitute strings within lines");
puts("    rep        - replace lines");
puts("    ins        - insert lines before or after a search string");
puts("    del        - delete lines");
puts("    search     - help with <search_spec> (used in most commands)");
puts("    options    - the options available to all commands");
puts("    list       - list commands entered thus far");
puts("");
	return;
    }

    (void)strcat(w, " ");	/* to match keywords in get_cmd_type() */

    if((i = get_cmd_type(w)) == ERROR) {
				/* additional help stuff */
	w[strlen(w)] = '\0';	/* zap that stupid space-kluge */
	if(!strncmp(w, "ins", 3))	/* looking for insert-something */
	    printf("\n%s\n", help[1]);
	else if(!strncmp(w, "sea", 3))	/* search string help */
	    printf("\n%s\n", help[8]);
	else if(!strncmp(w, "opt", 3))	/* options help */
	    printf("\n%s\n", help[9]);
	else if(!strncmp(w, "list", 4))
	    printf("\n%s\n", help[10]);
	else
	printf("No help for %s.  Enter 'help' for a list of help arguments.\n",
 		w);
    }
    else
	printf("\n%s\n", help[i]);
#endif

}



/*
	"cmd.c" - routines affecting commands for TED.

*/


#ifndef MYTYPES
#include "mytypes.h"
#endif

#include <ctype.h>		/* for expand_macro() */


static FILE *cmd_fp;	/* globally used in cmd.c, but doesn't */
			/* hurt when all sources are concatenated together. */

read_cmds()
{   char buff[BUFFLEN], word[BUFFLEN];
    char *t;					/* buffer pointer(s) */
    char **get_text();
    struct commands *cmd, *get_cmd_obj();	/* new command */
    char *saved_filename;

    if(iact_mode) {
	cmd_fp = stdin;
	prompt = IACT_PROMPT;
	printf("\nEnter commands now.  Type 'help' for help.  Enter \"%s\"\n",
	       IACT_TERM);
	printf("on its own line to simulate an end-of-file.\n\n");
    }
    else {
	if((cmd_fp = open_file(cmd_file, "r")) == NULL) {
	    perror(cmd_file);
	    return;
	}	
    }
    if(verbose)
	printf("\nReading commands from '%s'\n", cmd_file);

/*  No need to have each cmd structure keep a copy of the input filename,
    so all cp->src_file's point to this saved ptr for this cmd file. */
    saved_filename = strsave(cmd_file);	


    while(readline(buff, cmd_fp) != NULL) {
	int type = COMMENT;		/* allow blank lines & top of file */

	if(iact_mode && !strncmp(buff, IACT_TERM, strlen(IACT_TERM))) {
	    ungetline = 0;		/* just to be sure! */
	    iact_eof = 1;
	    break;
	}

	t = buff;
	t += getword(buff, word);	/* get command word to check...*/

	if(type == COMMENT && (!*word))	/* found blank line while not in */
	    continue;			/* middle of command. */

	if((type = get_cmd_type(buff)) == ERROR) {
    	    printf("\nLine %d: unknown command '%s'\n", line, buff);
	    continue;			/* failure reading command name */
	} 

	if(type == COMMENT)		/* ignore comment lines... */
	    continue;
	if(type == HELP) {		/* give the poor sap some help... */
	    help(buff);
	    continue;
	}

	cmd = get_cmd_obj();		/* gets memory and inits structure */

	cmd->type = type;
	cmd->line = line;		/* save line number for reference */
	cmd->src_file = saved_filename; /* multi-file cmds support */

	t += getword(t, word);		/* get file specification */

	cmd->filespec = (*word) ? strsave(word) : NULL;

	t += get_search(t, &cmd->match_str, &cmd->start_str, &cmd->end_str);

	cmd->options = get_cmd_opts(t);

	if(cmd->type == SUB) {
	    char *dummy;
 	    int first_sub = TRUE;
	    struct commands *cmd2;

	    while(1) {
		if(readline(buff, cmd_fp) == NULL) {
		    if(first_sub == TRUE) {
			fprintf(stderr,
			"Line %d - found EOF while getting SUB args.\n",
			line);
		    }
		    return;
		}
					
		if(get_cmd_type(buff) != ERROR) {
		    ungetline = TRUE; 	/* must be true! */
		    if(first_sub)
			fprintf(stderr,
				"Line %d - didn't find any SUB args!\n",
				line);
		    break;
		}
				/* use cmd as a backup for possible
				   next abbreviated SUB; cmd2 is "it" now. */
		cmd2 = get_cmd_obj();
		copy_cmd_obj(cmd2, cmd);

		cmd2->line = line;

		if(get_search(buff, &dummy, &cmd2->search_str,
			      &cmd2->rep_str) == 0)
		    printf("Line %d - had troubles reading SUB args.\n",
			    line);
		first_sub = FALSE;

		if(dummy)
		    printf("Line %d in %s: Delimiter #3 not found?\n",
			   line, cmd_file);

		if(verify_cmd(cmd2) == OK) {
		    if(iact_mode)
			puts("\nThis SUB command looks OK...");
		    add_cmd(cmd2);
		}
	    }
	    free((char *)cmd);	/* since we only used it as a template */
	}
	else {			/* command other than "sub" */
	    if(cmd->type != DEL) 
		cmd->text = (char **)get_text();

				    /* check for valid cmd args/syntax */
	    if(verify_cmd(cmd) == OK) {
		if(iact_mode) {
		    printf("\nThis command looks OK...\n");
		}
		add_cmd(cmd);
	    }
	}
    }
    if(!iact_mode)
	if(fclose(cmd_fp))
	    fprintf(stderr, "Had trouble closing %s\n", cmd_file);
}


/*  To avoid having <text> look like the start of a new command,
    the docs specify that a command must start at the beginning of
    a line and must be in lower case.  In addition, I make sure there
    is a space after the command word.  Comments *always* start with
    an exclamation "!" at the beginning of a line.
*/
get_cmd_type(s)
char *s;
{
    switch(*s) {
	case 'd' : if(!strncmp(s, "del ", 4))
	    		return(DEL);
		   break;
	case 'h' : if(!strncmp(s, "help", 4))
	    		return(HELP);
		   break;

	case 'i' : if(!strncmp(s, "ins> ", 5))
	    		return(INSA);
	    	   if(!strncmp(s, "ins< ", 5))
			return(INSB);
		   break;
	case 'l' : if(!strncmp(s, "list", 4)) {
	    		print_cmds();
			return(COMMENT);
		   }
		   break;
	case 'r' : if(!strncmp(s, "rep ", 4))
	    		return(REP);
		   break;
	case 's' : if(!strncmp(s, "sub ", 4))
	    		return(SUB);
		   break;
	case '!' : return(COMMENT);
	    	   break;
	default  : /* if end of interactive input, signal recognition of 
			any command.  also see get_text() */
		    if(iact_mode && !strncmp(s, IACT_TERM, strlen(IACT_TERM))){
			iact_eof = 1;
			ungetline = 0;
	    		return(COMMENT); 
		    }
	    	    else
			return(ERROR);
		    break;
    }
    return(ERROR);
}



/* returns number of chars scanned from buff position */

get_search(s, match, start, end)
char *s, **match, **start, **end;
{
    char delim, *t = s, buff[BUFFLEN], *bp = buff;

    while(ISWHITE(*t)) ++t;		/* skip to search specification */

    *match = *start = *end = NULL;

    if((delim = *t) == '-' || delim == '\0')	/* watch for -options */
	return(0);

    if(get_cmd_type(s) != ERROR) /* shouldn't be a command first time thru */
	return(0);

    for(++t; *t && *t != delim; ++t, ++bp) /* copy first arg someplace */
	*bp = *t;
    *bp = '\0';

    if(!*t) {
	printf("Line %d in %s: no ending delimiter found\n", line, cmd_file);
	return(t - s);
    }

    if(strchr(++t, delim)) {		/* keep searching for rep string */
	*start = strsave(buff);

	for(bp = buff; *t && *t != delim; ++t, ++bp)
	    *bp = *t;
	*bp = '\0';
	*end = strsave(buff);
    }
    else
	*match = strsave(buff);

    return(t - s);

}
    

/* read lines of text and stuff into a char ** array */

char **get_text()
{
    unsigned int n = 10, nsaved = 0;
    int first = 1;
    char **s, **sp, buff[BUFFLEN];
    
    if((s = (char **)malloc(n * sizeof(char *))) == NULL) 
	nomem("getting text");

    sp = s;
    while(1) {
	if(readline(buff, cmd_fp) == NULL)
	    if(first) {
	    	fprintf(stderr, "Line %d in %s: bad EOF while getting <text>\n",
			line, cmd_file);
	    	return(NULL);
	    }
	    else
	        break; /* out of while */

	first = 0;		/* made it through first read... */

			/* keep reading until it looks like a command */
	if(get_cmd_type(buff) != ERROR) { 
	    ungetline = TRUE;
	    if(nsaved == 0) {
		free(s);
		s = NULL;
	    }
	    break; /* out of while */
	}
	else {				/* save this line! */
	    if(nsaved == n - 1) {	/* need to get more line ptrs */
		n = n * 1.5 + 1; 		/* get 50% more */
		
		if((s = (char **)realloc((char *)s, n*sizeof(char *))) == NULL)
		    nomem("realloc getting text");
		sp = s + nsaved;
		*(sp + 1) = NULL;
	    }
	    *sp = strsave(buff);
	    ++nsaved;
	    *(++sp) = NULL;		/* to make sure next one is NULL */
	}
    }
    return(s);
}


/* get command options -- s points to somewhere after <search_spec>.
   Searching for options is not strict here...look for a hyphen and
   then ANY valid option character after it is used.
	i - Ignore Case on <search_spec>
	e - Echo command and changes
	p - <search_spec> is a Pattern (see pattern.c:glob()).
	m - strings contain macros
	c - embedded (escape) control characters - see proc_ctrls().
		\n = newline			\\ = backslash
		\t = horizontal tab		\b = backspace
		\r = carriage return		\f = formfeed
		\a = audible alert (bell)
*/

get_cmd_opts(s)
char *s;
{
    int options = 0;
    register char *t;

    if((t = strchr(s, '-')) != NULL)
	while(*t) {
	    if(ISWHITE(*t)) {
		++t;
		continue;
	    }
	    switch(*t++) {
		case '-' : break;
		case 'c' : options |= CTRLS;
			   break;
		case 'i' : options |= IG_CASE;
			   break;
		case 'e' : options |= ECHO;
		    	   break;
		case 'm' : options |= MACRO;
			   break;
		case 'p' : options |= GLOB;
			   break;
		default  :
		    printf("Line %d in %s: confused with option '%c'.\n",
				  line, cmd_file, *(t - 1));
			   break;
	    }
	}
    return(options);
}



/*  add a command structure to the linear linked list.  Note:  Head node
    (cmd_head) is global and already allocated.
*/

add_cmd(cmd)
struct commands *cmd;
{
    struct commands *cp;	/* for navigation... */

				/* swim to end of list... */
    for(cp = &cmd_head; cp->next != NULL; cp = cp->next)
	;

    cp->next = cmd;
    cmd->next = NULL;		/* just to be sure we know the end */

    if(iact_mode)
	print_cmd_obj(cmd);
}



/*  simply returns a character pointer to a text description of a 
    tokenized command.  Useful for those rare debugging occasions.  :) */

char *cmd_str(i)
int i;
{
    switch(i) { case INSA : 	return("ins>");		break;
		case INSB : 	return("ins<"); 	break;
		case DEL  : 	return("del");  	break;
		case REP  : 	return("rep");  	break;
		case SUB  : 	return("sub");  	break;
    		case COMMENT : 	return("comment"); 	break;
		default   : 	break;  /* fallthrough */
    }
    return("** cmd_str: type UNKNOWN **");
}



free_cmds()
{
    struct commands *cp, *cp2;

    for(cp = cmd_head.next; cp != NULL;) {
	cp2 = cp->next;			/* save next while we... */
	free_cmd_obj(cp);		/* unload the structure itself */
	cp = cp2;
    }
}



free_cmd_obj(cp)
struct commands *cp;
{
	if(cp->filespec != NULL)	free(cp->filespec);
	if(cp->start_str != NULL)	free(cp->start_str);
	if(cp->end_str != NULL)    	free(cp->end_str);
	if(cp->match_str != NULL)	free(cp->match_str);
	if(cp->search_str != NULL)	free(cp->search_str);
	if(cp->rep_str != NULL)		free(cp->rep_str);

/*  Can't free cp->src_file for every cmd obj, since read_cmds() doesn't
    strsave() for each cmd obj.  Hmm...live with a small memory leak or
    waste memory?    (we'll take the small memory leak for now) */
/*	if(cp->src_file != NULL)	free(cp->src_file); */

	if(cp->text != NULL) {
	    register char **sp;
	    for(sp = cp->text; *sp != NULL; ++sp) /* free each line */
		free(*sp);
	    free((char*)cp->text);	/* free the block of ptrs */
	}
}




struct commands *get_cmd_obj()
{
    struct commands *cmd;

    if((cmd = (struct commands *)malloc(sizeof(struct commands))) == NULL)
	nomem("command structure");

    cmd->filespec = NULL;	cmd->start_str = NULL;
    cmd->end_str =  NULL;	cmd->match_str = NULL;
    cmd->text = NULL;		cmd->search_str = NULL;
    cmd->rep_str = NULL;	cmd->next = NULL;
    cmd->type = cmd->options = cmd->debug = cmd->line = 0;
    cmd->src_file = NULL;

    return(cmd);
}


copy_cmd_obj(cpto, cpfrom)
struct commands *cpto, *cpfrom;
{
    if(cpfrom->filespec != NULL)
	cpto->filespec = strsave(cpfrom->filespec);
    if(cpfrom->start_str != NULL)
	cpto->start_str = strsave(cpfrom->start_str);
    if(cpfrom->end_str != NULL)
	cpto->end_str = strsave(cpfrom->end_str);
    if(cpfrom->match_str != NULL)
	cpto->match_str = strsave(cpfrom->match_str);
    if(cpfrom->text != NULL) {
	char **t, **t2;
	unsigned ptrs;
	for(ptrs = 0, t = cpfrom->text; *t; ++t, ++ptrs)
	    ;
	cpto->text = (char **)calloc(ptrs + 1, sizeof(char*));

	for(t = cpfrom->text, t2 = cpto->text; *t != NULL; ++t, ++t2) {
	    *t2 = strsave(*t);
	}
	*t2 = NULL;			/* not neces. since calloc, but... */
    }
    if(cpfrom->search_str != NULL)
	cpto->search_str = strsave(cpfrom->search_str);
    if(cpfrom->rep_str != NULL)
	cpto->rep_str = strsave(cpfrom->rep_str);
    cpto->next = cpfrom->next;
    cpto->type = cpfrom->type;
    cpto->options = cpfrom->options;
    cpto->debug = cpfrom->debug;
    cpto->line = cpfrom->line;
    cpto->src_file = cpfrom->src_file;
}



print_cmds()
{
    register struct commands *cp;

    puts("\n****   L i s t    o f    V a l i d   C o m m a n d s   ****\n");
    for(cp = cmd_head.next; cp != NULL; cp = cp->next) {
	print_cmd_obj(cp);
	if(cp->next != NULL)
	    puts("!");
    }
    puts("\n****   E n d   o f   C o m m a n d s   ****\n");
}



print_cmd_obj(p)
struct commands *p;
{
    printf("! From file %s:%d\n", p->src_file, p->line);
    printf("%s %s ", cmd_str(p->type), p->filespec);
    if(p->match_str)
	printf("/%s/ ", p->match_str);
    if(p->start_str && p->end_str)
	printf("/%s/%s/ ", p->start_str, p->end_str);
    if(p->options & (ECHO | IG_CASE | GLOB | MACRO | CTRLS)) {
	putchar('-');
	if(p->options & CTRLS)	putchar('c');
	if(p->options & ECHO) 	putchar('e');
	if(p->options & IG_CASE) putchar('i');
	if(p->options & MACRO)	putchar('m');
	if(p->options & GLOB)	putchar('p');
    }
    putchar('\n');
    if(p->search_str && p->rep_str)
	printf("/%s/%s/", p->search_str, p->rep_str);
    if(p->text != NULL) {
	register char **t = p->text;
	while(*t)
	    fputs(*t++, stdout);
    }
    else if(p->type != DEL)
	putchar('\n');
}


verify_cmd(p)
struct commands *p;
{
    int status = OK;

    if(!p->filespec) {
	status = ERROR;
	puts("empty file specification\n");
    }

/*  check for <match_str> or <start_str>+<end_str> pair */

    if(!p->match_str && (!p->start_str || !p->end_str)) {
	 status = ERROR;
	 printf("no <match_str> or <start_str> <end_str> pair for %s\n",
		   cmd_str(p->type));
    }

					/* check for valid search specs */
    if(p->type == SUB) {
	if(!p->search_str) {
	     status = ERROR;
	     printf("Line %d: missing <search_str> in sub command\n", line);
	}
	if(!p->rep_str) {
	    status = ERROR;
	    printf("Line %d: missing <rep_str> in sub command\n", line);
	}
	if(p->rep_str && !*p->rep_str && verbose)
	    printf("Note line %d <rep_str> empty; will delete <search_str>)\n",
		   p->line);
    }

    if((p->type != DEL && p->type != SUB) && p->text == NULL) {
	status = ERROR;
	printf("Line %d: <text> required for %s\n", p->line, cmd_str(p->type));
    }

    if(p->type == DEL && p->text != NULL)
	printf("Warning - Line %d: <text> not allowed (ignored)\n", p->line);

    if(status != OK)
    	printf("****  Command will not be executed line %d in '%s'  ****\n\n",
	       p->line, cmd_file);

    return(status);
}


init_cmds()
{
    cmd_head.next = NULL;
    cmd_head.filespec = "Head Node";
    cmd_head.type = COMMENT;
    line = 0;
}



/*  do_macros(cp); Searches through command structure to replace certain
    occurrances of strings to (generally) runtime-specific values.  See
    expand_macro() below.  

*/


do_macros(cp)
struct commands *cp;
{
    char **text, *expand_macro();

#ifdef DEBUG
    printf("\nBefore do_macro():\n");
    print_cmd_obj(cp);
#endif

    cp->filespec = expand_macro(cp->src_file, cp->filespec);
    cp->start_str = expand_macro(cp->src_file, cp->start_str);
    cp->end_str = expand_macro(cp->src_file, cp->end_str);
    cp->match_str = expand_macro(cp->src_file, cp->match_str);
    cp->search_str = expand_macro(cp->src_file, cp->search_str);
    cp->rep_str = expand_macro(cp->src_file, cp->rep_str);

    for(text = cp->text; text && *text; ++text) {
	*text = expand_macro(cp->src_file, *text);
    }
#ifdef DEBUG
    printf("\nAfter expanding macros:\n");
    print_cmd_obj(cp);
#endif
}



/*  expand_macro(): perform macro expansion in strings.  Currently sports
    the following macros:

       Macro	Function
	$of	output filename (complete)
	$ofh	output filename head (e.g., "dml001" of "dml001.pc")
	$ofe	output filename extension (e.g., ".pc" of "dml001.pc")
	$if	input filename (complete)
	$ifh	input filename head
	$ife	input filename extension
	$ted	current ted command file name
	$ver	ted version string (main.c)

    Additionally, these may be specified in upper case for upper case
    expansion of macros.  e.g., $OFH is DML001 if $of is dml001.pc or DML001.PC

    Returns pointer to passed string since may have realloced.

    NOTE:  Macros which are substrings of other macros must be listed
	   after the superstring in the declarations for mactok and
	   valtok.  e.g., $of must be after $ofe or $ofh.
*/
char *expand_macro(cmd_srcf, s)
char *cmd_srcf, *s;		/* ptrs to malloc'ed strings */
{
    static char ofe[FILELEN], ofh[FILELEN], ife[FILELEN], ifh[FILELEN];
    static char cmd_src[FILELEN];
    static char *mactok[] =
	{ "$ofe", "$ofh", "$of", "$ife", "$ifh", "$if", "$ted", "$ver", "" };
			    /* token values must match order of mactok! */
    static char *valtok[] =
	{ ofe, ofh, out_file, ife, ifh, src_file, cmd_src, VERSION };
    char *t, *sp;
    int i;

    if(s == NULL)
	return(NULL);
    (void)strcpy(cmd_src, cmd_srcf);

    /* set up macro values (not expensive, even if not used) */
    if((t = strrchr(strcpy(ofh, out_file), '.')) != NULL) {
	*t = '\0';
	(void)strcpy(ofe, t+1);
    }
    else 
	*ofh = *ofe = '\0';
    if((t = strrchr(strcpy(ifh, src_file), '.')) != NULL) {
	*t = '\0';
	(void)strcpy(ife, t+1);
    }
    else
	*ifh = *ife = '\0';

    for(i = 0; *mactok[i]; ++i) {
	while(strlen(s) && (sp = stristr(mactok[i], s)) != NULL) {
	    if(islower(*(sp + 1)))
		s = strmrep(s, (int)(sp - s), mactok[i], valtok[i]);
	    else
		s = strmrep(s, (int)(sp - s), mactok[i],
			    strupper(valtok[i]));
#ifdef DEBUG
	    printf("strmrep returns '%s'\n", s); 
	    if(olds != s)
		printf("s is in new block: from %x to %x\n", olds, s);
#endif
	}
/*	printf("no more %s's found...\n", mactok[i]); */
    }
    return(s);
}



/*  For the -c command option; embedded escapes are converted to their
    ASCII counterparts.  ALL parts of a command are subject to replacement.

*/
do_ctrls(cp)
struct commands *cp;
{
    char **text, *expand_ctrls();

    cp->filespec = expand_ctrls(cp->filespec, cp->line);
    cp->start_str = expand_ctrls(cp->start_str, cp->line);
    cp->end_str = expand_ctrls(cp->end_str, cp->line);
    cp->match_str = expand_ctrls(cp->match_str, cp->line);
    cp->search_str = expand_ctrls(cp->search_str, cp->line);
    cp->rep_str = expand_ctrls(cp->rep_str, cp->line);

    for(text = cp->text; text && *text; ++text) {
	*text = expand_ctrls(*text, cp->line);
    }
}

/*  This routine does the actual expansion of the escapes discussed in
    the do_ctrls() function.  Since all replacements are one character
    of two character strings, we don't have to realloc(s).
    Escapes allowed:  \\, \a, \b, \f, \r, \n, \t, \v.
    Well, what the heck...someone is eventually gonna want to put an
    arbitrary value in there, so let's add in hex and octal replacements:
	\x8 == \x08 == hex 08
	\7 == \07 == \007  == octal 7.
    NOTE:  in this routine, HEX strings should be (but are not restricted
    	to) one or two hex digits and octal digits should be one, two, or
	at most	three octal digits.  Remember, these are going into single-
	byte characters!

		\n = newline			\\ = backslash
		\t = horizontal tab		\b = backspace
		\r = carriage return		\f = formfeed
		\a = audible alert (bell)	\v = vertical tab 
*/

char *expand_ctrls(s, line)
char *s;			/* pointer to malloc-ed string */
int line;
{
    char *save_s = s;

    if(s == NULL || *s == '\0')
	return s;

/*    if(strchr(s, '\\'))
       printf("expand_ctrls():  Original string is:  %s\n", s);
*/

    while(*s) {
	if(*s == '\\') {	/* note:  s points to the backslash */
	    switch(s[1]) {	/* note:  still pointing to backslash! */
		int i;
		case '\0':	/* found escape at end of line?? */
			    printf("Backslash error at end of line!\n");
			    break;
		case '\\':  strcpy(s, s + 1);  break;
		case 'a' :  *s = '\a';	strcpy(s + 1, s + 2);  break;
		case 'b' :  *s = '\b';	strcpy(s + 1, s + 2);  break;
		case 'f' :  *s = '\f';	strcpy(s + 1, s + 2);  break;
		case 'r' :  *s = '\r';	strcpy(s + 1, s + 2);  break;
		case 'n' :  *s = '\n';	strcpy(s + 1, s + 2);  break;
		case 't' :  *s = '\t';	strcpy(s + 1, s + 2);  break;
		case 'v' :  *s = '\v';  strcpy(s + 1, s + 2);  break;
		case 'X' :
		case 'x' :  sscanf(s + 2, "%x", &i);
			    *s = (char)i;
			    /* shift stuff over those digits */
			    for(i = 0; ishexdigit(s[i + 2]); ++i);
			    strcpy(s + 1, s + 2 + i);
			    break;
		case '0' :  case '1' : case '2': case '3': case '4' : 
		case '5' :  case '6' : case '7': case '8': case '9' :
			    sscanf(s + 1, "%o", &i);
			    *s = (char)i;
			    /* shift stuff over those digits */
			    for(i = 0; s[1+i] >= '0' && s[1+i] <= '7'; ++i);
			    strcpy(s + 1, s + 1 + i);
			    break;
		default :   printf("Line %d:  backslash '%c' is \
not a known escape.\n", line, s[1]);
	    }
	}
	++s;
    }
    return save_s;
}	
/*
  	Source:  strutil.c - my string utilities.

  Note: All these routines are my original code, sometimes using
	guidelines from various man pages.  
*/

#ifndef MYTYPES
#include "mytypes.h"
#endif

#ifndef NULL
#  include <stdio.h>
#endif

#include <ctype.h>


#ifndef STRLIB_OK		/* BEGIN FILTER OUT OF STD LIB FUNCS */


/*
  Many man pages warn about doing string copies with pointers that
  overlap.  If you suspect strange things are happening, especially
  with string substitutions, use this strcpy() rather than the one
  provided with your library.
  
  NOTE:  VAX VMS "C" gets VERY confused with this routine defined.
*/

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

/*
char *strchr(s, c)
register char *s, c;
{
    while(*s)
	if(*s == c)
	    return(s);
        else ++s;
    return((c == '\0') ? s : NULL);
}
*/

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



/* strupper(s) - converts entire string s to upper case, returns static
        copy of string. (original is unchanged)
*/
char *strupper(s)
register char *s;
{   static char buff[BUFFLEN];
    register char *t;

    for(t = buff; *s; ++t, ++s)
        *t = (islower(*s)) ? toupper(*s) : *s;
    *t = '\0';
    return(buff);
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
  stristr(text, search) - ignore case when checking if s is a substring of t.
  returns ptr in t or NULL if not a substring.
  
*/

char *stristr(txt, sea)
char *txt, *sea;		/* text string / search string */
{
    char *s, *t, *found;
    int pos;

    (void)strlcpy((t = strsave(txt)), txt);
    (void)strlcpy((s = strsave(sea)), sea); /* convert to one case... */
    found = substr(s, t);
    pos = found - s;
    free(s); free(t);
    return(found ? sea + pos : NULL);
}



/* substr(t, s):  if s is substring of t, return ptr at location in t.
   Otherwise, return NULL.  Note:  '\0' *is* considered as part of the
   string.
 */

char* substr(t, s) 
register char *t, *s; 
{  register char *ss, *tt;
   char *strchr();

   if (s == NULL || t == NULL)          /* Initial checks... */
     return(NULL);
   if(*s && !*t)			/* watch for empty strings... */
       return(NULL);
   if(!*s)
       return(t + strlen(t));

   do {  ss = s;
         if((tt = strchr(t, *ss)) == NULL)
	     return(NULL);

         t = tt + 1;		/* advance if we don't get match */

         while((*(++ss) == *(++tt)) && *tt)
		;
      }  while(*ss != '\0');
   return(t - 1);
}



char *strsave(t)			/* copy t into s */
char *t;
{   char *s;
    if(t == NULL) return(t);
    if((s = (char *)malloc((unsigned)(strlen(t) + 2))) == NULL)
    {	fprintf(stderr, "no memory for strsave %d bytes!\n", strlen(t));
	exit(1);
    }
    return(strcpy(s, t));
}



/* read a word starting @ s and copy into w.  Skips preceding white space,
   stops at whitespace.  Returns total number of characters skipped through
   in string s.  Also, see getalphaword() below.
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


/*  replace string str in s at position pos with rep using malloced strings.
    returns ptr to (possibly new location) of s.
*/

char *strmrep(s, pos, str, rep)
char *s;
int pos;
char *str, *rep;
{
    int nrep = strlen(str);
    unsigned newlen;
    char *savetail = malloc((unsigned)(strlen(s + pos) - nrep + 1));

    if(!savetail)
	nomem("savetail in strmrep()");

    (void)strcpy(savetail, s + pos + nrep); /* save tail of old string */
    newlen = strlen(s) - nrep + strlen(rep) + 1;

    if((s = realloc(s, newlen)) == NULL)
	nomem("realloc in strmrep()");
    (void)strcpy(s + pos, rep);
    (void)strcat(s, savetail);
    free(savetail);
    return(s);
}



/*  getqstr(dest, src) - searches for a word in src and copies it to dest.
    src may be a quoted string (using the quote character below) and thusly 
    including spaces.  Returned is a pointer to one character past the 
    last scanned character in src.  Quote characters are not copied unless
    they are quoted themselves by the qquote character.

    Note:  Version 2.24 only uses this for getting filanames that may be 
    quoted to include spaces for IBM weirdos.

    BUG:  does not work right if quote and qquote are the same character.
*/

char *getqstr(dest, src)
char *dest, *src;
{
    char *s_save = src;
    char quote = '\"';		/* the quote character */
    char qquote = '\\';		/* character to quote the quote */

    while(ISWHITE(*src)) ++src;

    if(*src == quote) {
	while(*++src != quote) {
	    *dest++ = *src;
	    if(*src == qquote)
		*(dest-1) = *++src;
	    if(!*src) {
		fprintf(stderr,
			"\nWarning: Missing quote (%c) in following line:\n",
			quote);
		puts(s_save);
		break;
	    }
	}
    }
    else {			/* copy single word */
	while(*src && !ISWHITE(*src) && *src != quote)
	    *dest++ = *src++;
    }

    *dest = '\0';
    return(*src ? src + 1 : src);
}




/*
  "pattern.c" - pattern matching code similiar to csh.

  "glob" was posted to Usenet by David Koblas during a discussion of
  public-domain pattern matching code.  I found this to be the most
  concise and flexible code posted, so I decided to include it rather
  than write one myself.  In private mail, David said, "The code is
  'Public Domain', feel free to do whatever you want with it."  Although
  now part of a (somewhat) commercial product ("TEd"), this routine may 
  still be considered to be in the Public Domain.

  The code for braces (curly brackets) was broken and needed some
  gentle hacking.  -- Chris Schanzle (chris@speckle.ncsl.nist.gov)

  Fixed it harder.  -- David Flater (dave@case50.ncsl.nist.gov) 4/25/95
*/

/*
 * input: "str" string will attempted to be matched
 * 
 * "pattern" string with wildcards that will match against "str".
 * 
 * wild: '*'    = match 0 or more occurances of anything 
 * "[abc]" 	= match anyof "abc" (ranges supported) 
 * "{xx,yy,zz}" = match anyof "xx", "yy", or "zz"
 * '?'          = match any character
 */


#ifndef TRUE
#define TRUE  1
#endif

#ifndef FALSE
#define FALSE 0
#endif

glob(str, pattern)
char *str, *pattern;
{
    char      c;
    int       done = FALSE, ret_code, ok, count, matchlen;

    while ((*pattern != '\0') && (!done) && (((*str == '\0') &&
	     ((*pattern == '{') || (*pattern == '*'))) || (*str != '\0'))) {
	switch (*pattern) {
	case '\\':		/* wildcard was "escaped" -- match it. */
	    pattern++;
	    if (*str == *pattern) { 	/* similar to DEFAULT, below */
		str++;
		pattern++;
	    } else {
		done = TRUE;
	    }
	    break;
	case '*':
	    pattern++;
	    ret_code = FALSE;
	    while ((*str != '\0') && (!(ret_code = glob(str++, pattern))));
	    if (ret_code) {
		while (*str != '\0')
		    str++;
		while (*pattern != '\0')
		    pattern++;
	    }
	    break;
	case '[':
	    pattern++;
    repeat:
	    if ((*pattern == '\0') || (*pattern == ']')) {
		done = TRUE;
		break;
	    }
	    if (*pattern == '\\') {
		pattern++;
		if (*pattern == '\0') {
		    done = TRUE;
		    break;
		}
	    }
	    if (*(pattern + 1) == '-') {
		c = *pattern;
		pattern += 2;
		if (*pattern == ']') {
		    done = TRUE;
		    break;
		}
		if (*pattern == '\\') {
		    pattern++;
		    if (*pattern == '\0') {
			done = TRUE;
			break;
		    }
		}
		if ((*str < c) || (*str > *pattern)) {
		    pattern++;
		    goto repeat;
		}
	    } else if (*pattern != *str) {
		pattern++;
		goto repeat;
	    }
	    pattern++;
	    while ((*pattern != ']') && (*pattern != '\0')) {
		if ((*pattern == '\\') && (*(pattern + 1) != '\0'))
		    pattern++;
		pattern++;
	    }
	    if (*pattern != '\0') {
		pattern++;
		str++;
	    }
	    break;
	case '?':
	    pattern++;
	    str++;
	    break;
        case '{':               /* } */
            ++pattern;
            ok = FALSE;         /* ok == found matching pattern */
	    matchlen=0;

            /* DWF 4/25/95  Take longest match instead of first match. */
            while(*pattern && *str && *pattern != '}') {
                count = 0;
                while((*pattern == *str) && *pattern != ',' && *pattern != '}')
                    ++pattern, ++str, ++count;
                if(*pattern == ',' || *pattern == '}') { /* end of a pattern? */
                    if (ok) {
                        if (count > matchlen)
                            matchlen = count;
                    } else {
                        ok = TRUE;
                        matchlen = count;
                    }
                }
                str -= count;   /* back up string to check more patterns */
                /* skip to next pattern */
                while(*pattern && *pattern != ',' && *pattern != '}')
                    ++pattern;
                if (*pattern == '}')
                    break;
                if(*pattern)    /* skip over ',' */
                    ++pattern;
                /* Handle special case of ,} at end of string */
                if (*pattern == '}' && (!ok)) {
                    ok = TRUE;
                    matchlen = 0;
                }
            }
            /* Scuttle the boat if we didn't match anything */
            if (!ok)
                return FALSE;
            str += matchlen;
            if(*pattern != '}')
                while(*pattern && *++pattern != '}')
                    ;
            if(*pattern)
                ++pattern;
            break;
	default:
	    if (*str == *pattern) {
		str++;
		pattern++;
	    } else {
		done = TRUE;
	    }
	}
    }
    while (*pattern == '*')
	pattern++;

    /* NOTE:  If there's a newline at the end of str and we're at the
       end of the pattern, go ahead and match the string to the pattern.
       This is not normal, but applicable to TEd.
    */
    return ((*str == '\0' || (*str == '\n')) && (*pattern == '\0'));
}



/*
  	src.c - routines to maintain the doubly linked list of text lines
		to be edited.
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
    char buf2small = 0;

    init_lines();

    iact_eof = 0;
    prompt = NULL;

    if((fp = open_file(src_file, "r")) == NULL)
	return(ERROR);
    if(verbose)
	printf("\nReading %s...", src_file);

    ungetline = FALSE;

    while(fgets(buff, BUFFLEN - 1, fp) != NULL) {
	if(strrchr(buff, '\n') == NULL) { 	/* line exceeds buffer size */
	    buf2small = 1;
	}
	else
	    ++n;

	lp = (struct lines *)get_line_obj();

	lp->text = strsave(buff);

	lp->prev = lp_last;	/* link in new node at end of list*/
	lp->next = NULL;
	lp_last->next = lp;
	lp_last = lp;		/* save for next append */
    }
    if(fclose(fp))
	fprintf(stderr, "Had trouble closing %s\n", src_file);
    if(verbose)
    	printf("done!  Read %d lines.\n", n);

    if(buf2small)
	fix_nls();

    return(OK);
}




write_src_lines()
{
    FILE *fp;
    struct lines *lp;

    if(!src_mod && !strcmp(src_file, out_file)) {
	puts("As best as I can remember, the source file was not modified");
        puts("and the input/output filenames are the same, so I don't see");
	printf("any reason to write '%s'.\n", out_file);
	return(OK);
    }
    if(verbose)
    	printf("\nWriting output file '%s'...", out_file);

    if((fp = open_file(out_file, "w")) == NULL)
	return(ERROR);

    for(lp = line_head.next; lp != NULL; lp = lp->next) {
	fputs(lp->text, fp);
    }

    if(fclose(fp)) {
	fprintf(stderr, "\nHad trouble closing file %s\n", out_file);
	return(ERROR);
    }

    if(verbose)
	printf("done!\n");

    return(OK);
}



#ifdef DEBUG
print_src_lines()
{
    register struct lines *lp;

    printf("\n*******  Source Lines from %s:\n", src_file);
    for(lp = line_head.next; lp != NULL; lp = lp->next)
	printf("'%s'", lp->text);
}



print_src_lines_rev()
{
    register struct lines *lp;
    
    printf("\n*******  Traversing source linked list backwards \n");
    for(lp = line_head.next; lp->next != NULL; lp = lp->next)
	;
    for(lp = lp; lp != &line_head; lp = lp->prev)
	printf("'%s'", lp->text);
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
				/* initialize doubly linked list head node */
    line_head.text = "Head Node";
    line_head.next = NULL;
    line_head.prev = &line_head;


    line = 0;			/* keep track of input line number */
    src_mod = 0;		/* nonzero if source lines were modified */
}



/*
  	"insert.c" - routines for line insertions (insert-before and
		     insert-after).
*/

#ifndef MYTYPES
#include "mytypes.h"
#endif


/*
    searches source lines (forwards) looking for string "s" starting at
    "start" in the linked list.
*/

struct lines *search_lines(start, s, glob_mode)
struct lines *start;
register char *s;
int glob_mode;			/* doing pattern matching? */
{
    register struct lines *lp = start;
    register char *tt;
    int matched;

    if(ig_case)			/* convert s to lower case outside loop */
	(void)strlcpy((s = strsave(s)), s);

    for(lp = start; lp != NULL; lp = lp->next) {
	if(ig_case) 		/* convert line to lower case for matching */
	    (void)strlcpy((tt = strsave(lp->text)), lp->text);
	else
	    tt = lp->text;

	matched = glob_mode ? glob(tt, s) : (int)substr(tt, s);

	if(ig_case)
	    free(tt);
	if(matched)
	    return(lp);
    }
    if(ig_case)
	free(s);

    return(NULL);
}


/*  insert a line s after line lp, returning pointer to new line struct.
    a new (malloc-ed) copy of s will be saved.
*/

struct lines *insert_line(lp, s)
struct lines *lp;
char *s;
{
    struct lines *newlp;

    newlp = get_line_obj();
    newlp->text = strsave(s);
    newlp->next = lp->next;
    newlp->prev = lp;
    if(newlp->next)			/* watch for appends at end of list */
	newlp->next->prev = newlp;
    
    lp->next = newlp;
    return(newlp);
}



/*  lines of text in the command structure (cp) are inserted AFTER
    the source line (lp).  Returns ptr to the last line inserted.
*/
struct lines *insert_text(cp, lp)
struct commands *cp;
struct lines *lp;
{
    register struct lines *newlp = NULL;
    char **t;

    if(verbose || cp->options & ECHO) {
	if(lp == &line_head)
	    printf("Inserting <text> at beginning of file\n");
    	else if(lp->next == NULL)
	    printf("Appending <text> to end of file\n");
	else
	    printf("Inserting <text> after line %s", lp->text);
    }

    for(newlp = lp, t = cp->text; t && *t; ++t) {
	newlp = insert_line(newlp, *t);
    }

    src_mod = 1;		/* we modified the src_lines; must write. */
    return(newlp);
}



/*  Insert-Before command.  Inserts <text> before a particular string
    or block.
*/

insert_b(cp)
struct commands *cp;
{
    struct lines *lp, *lp_start, *lp_end, *lp_search;
    int	text_lines,
	ranges_ins;
    char **t;

    text_lines = ranges_ins = 0;		/* status info */
    for(text_lines = 0, t = cp->text; t && *t; ++t)
	++text_lines;

    lp_search = line_head.next;

    if(cp->match_str)
	while((lp = search_lines(lp_search,
				 cp->match_str,
				 cp->options & GLOB)) != NULL) {
	    lp_search = (insert_text(cp, lp->prev))->next;
	    if(lp_search)
		lp_search = lp_search->next;
	    ++ranges_ins;
	}
    else {
	while((lp_start = search_lines(lp_search,
				       cp->start_str,
				       cp->options & GLOB)) != NULL) {
	    if((lp_end = search_lines(lp_start,
				      cp->end_str,
				      cp->options & GLOB)) == NULL) {
		printf("\nNote: After pass #%d, <start_str> found but not ",
		       ranges_ins);
		printf("<end_str> for the following:\n");
		break;
	    }
	    (void)insert_text(cp, lp_start->prev);
	    lp_search = lp_end->next;
	    ++ranges_ins;
	}
    }
    ins_status(cp, text_lines, ranges_ins);
}



/*
    handle the "ins>" (insert-after) command
*/
insert_a(cp)
struct commands *cp;
{
    struct lines *lp, *lp_start, *lp_end, *lp_search;
    int	text_lines,
	ranges_ins;
    char **t;

    ranges_ins = 0;
    for(text_lines = 0, t = cp->text; t && *t; ++t) /* status info */
	++text_lines;

    lp_search = line_head.next;
    if(cp->match_str) {			/* <match_str> */
	while((lp = search_lines(lp_search,
				 cp->match_str,
				 cp->options & GLOB)) != NULL) {
	    lp_search = (insert_text(cp, lp))->next;
    	    ++ranges_ins;
        }
    }
    else {				/* <start_str> <end_str> */
	while((lp_start = search_lines(lp_search,
				       cp->start_str,
				       cp->options & GLOB)) != NULL) {
	    if((lp_end = search_lines(lp_start,
				      cp->end_str,
				      cp->options & GLOB)) == NULL) {
		printf("\nNote:  After pass #%d, <start_str> found but not ",
		       ranges_ins);
		printf("<end_str> for the following:\n");
		break;
	    }
	    lp_search = (insert_text(cp, lp_end))->next;
	    ++ranges_ins;
	}
    }

    ins_status(cp, text_lines, ranges_ins);
}



ins_status(cp, blk_siz, nranges)
struct commands *cp;
int blk_siz, nranges;
{
    printf("%s from %s:%d inserted blks of %d line%s %d time%s\n",
	   cmd_str(cp->type), cp->src_file, cp->line, blk_siz, PLURAL(blk_siz),
	   nranges, PLURAL(nranges));
}



/*
    Commands that may embed control characters may embed OR delete
    newlines as well with the -c option.  This routine checks if no
    newlines or multiple newlines exist on a line.

    If no newlines exist, the next line is concatonated with the current
    (non-newlined) line.  Also, we re-do the current line in case the
    concatated line did not have a newline.

    If multiple newlines exist, we split the lines into separate lines by
    creating a new copy of the line with nulls inserted after newlines and
    saving pointers to the beginning of those lines, deleting the original
    line, then finally inserting each of our saved lines into the linked
    list of lines by calling insert_line().  Phew! 
*/

fix_nls()
{
    struct lines *lp, *nextlp;
    char *s, *t, *saved, **text;
    int nnls;

    for(lp = line_head.next; lp; lp = lp->next) {
	if((s = strchr(lp->text, '\n')) == NULL) {
	    /* concatonate this line with the next line */
	    if (lp->next == NULL) {	/* no next line?  Hmmm... */
		printf("\nWarning: you just deleted the newline on the ");
		puts("last line in the file.");
	    }
	    else {
		/* join this line with the next line. */
		if ((s = (char *)realloc(lp->text, strlen(lp->text) +
					 strlen(lp->next->text) + 1)) == NULL)
		    nomem("realloc fixing newlines");
		lp->text = s;
		(void)strcat(lp->text, lp->next->text);
		(void)delete_line(NULL, lp->next);
		/* back up in case no newline on line we just concatonated */
		lp = lp->prev;
	    }
	}
	else if((s = strchr(s + 1, '\n')) != NULL) { /* > 1 newlines...work! */
	    int i = 0;

	    for(nnls = 0, s = lp->text; s = strchr(s, '\n'); ++s, ++nnls)
		;			/* just counting newlines... */
	    
	    text = (char **)calloc((unsigned)(nnls + 1), sizeof(char *));
	    saved = malloc((unsigned)(strlen(lp->text) + nnls + 1));

	    if(text == NULL || saved == NULL) {
		fprintf(stderr,
			"Out of memory while fixing newlines for %d bytes\n",
			strlen(lp->text) + nnls + 1);
		exit(1);
	    }
		
	    for(*text = s = saved, t = lp->text; *t; ++s, ++t)
		if((*s = *t) == '\n') {
		    *++s = '\0';
		    text[++i] = s + 1;
		}
/*	    nextlp = (delete_line(NULL, lp))->prev; /* BAD if NULL returns */
	    nextlp = lp->prev;			    /* the fix.... */
	    (void)delete_line(NULL, lp);
	    
	    for(i = 0; i < nnls; ++i) {
		nextlp = insert_line(nextlp, text[i]);
	    }
	    free(saved);	/* no memory leaks around here! */
	    free((char*)text);
	    lp = nextlp;	/* since we deleted lp just above */
	}
    }
}



/*
	"delete.c" - routines for line deletion (and replacement).
*/

#ifndef MYTYPES
#include "mytypes.h"
#endif


/*  deletes and free()'s memory for a line structure.  Returns ptr to
    the next line in the linked list.  If cp is null, it'll still delete,
    but it'll be quite about it.
*/

struct lines *delete_line(cp, lp)
struct commands *cp;
register struct lines *lp;
{
    struct lines *lp_save = lp->next;

    src_mod = 1;		/* we modified the src_lines; must write. */
    if(lp->text) {
	if(cp && (verbose || (cp->options & ECHO) && cp->type == DEL))
	    printf("Deleted: %s", lp->text);
	lp->prev->next = lp->next;	/* unlink from linked list */
	if(lp->next)			/* watch out for last line (null!) */
	    lp->next->prev = lp->prev;
	free(lp->text);			/* dispose of that hummer! */
    }
    else {
	puts("\nStrange Error: lp->text = null during delete_line()");
	if(cp)
	    printf("from command at line %d in %s\n", cp->line, cp->src_file);
    }
    free((char *)lp);
    return(lp_save);
}




/*  delete_em() - deletes lines matching a <match_str> or a range of
    lines specified by <start_str> and <end_str>.  
    If command is REP, also inserts block of <text>. 
*/
delete_em(cp)
struct commands *cp;
{
    struct lines *lp, *lp_start, *lp_end, *lp_search, *lp_save;
    int lines_del,			/* status variables */
	blks_ins,
	ranges_del,
        cp_lines;			/* number of replacement lines */
    char **t;				/* for counting cp_lines */

    lines_del = blks_ins = ranges_del = cp_lines = 0;

    if(cp->type == REP)
	for(t = cp->text; t && *t; ++t) 	/* count lines to inserted */
	    ++cp_lines;

    lp_search = line_head.next;
    if(cp->match_str) {
	while((lp = search_lines(lp_search,
				 cp->match_str,
				 cp->options & GLOB)) != NULL) {
	    if(cp->type == REP) {
		lp_save = lp->prev;
	    	(void)delete_line(cp, lp);
		++lines_del;
		lp_search = (insert_text(cp, lp_save))->next;
		++blks_ins;
	    }
	    else if(cp->type == DEL) {
		lp_search = delete_line(cp, lp);
		++lines_del;
	    }
	}
    }
    else { 			/* have <start_str> <end_str> pair */
	struct lines *lp_temp;
	while((lp_start = search_lines(lp_search,
				       cp->start_str,
				       cp->options & GLOB)) != NULL) {
	    if((lp_end = search_lines(lp_start,
				      cp->end_str,
				      cp->options & GLOB)) == NULL) {
		printf("\nNote:  After pass #%d, <start_str> found but not ",
		       ranges_del);
		printf("<end_str> for the following:\n");
		break;
	    }
	    lp_temp = lp_start->prev;	/* save for REP below */

					/* delete the range _inclusively_ */
	    lp_end = lp_end->next;
	    for(lp = lp_start; lp != lp_end; ++lines_del) {
		lp = delete_line(cp, lp);
	    }
	    ++ranges_del;

	    lp_search = lp_end;		/* continue search AFTER EO Range */
	    if(cp->type == REP) { 	/* insert the replacement text */
		lp_search = (insert_text(cp, lp_temp))->next;
		++blks_ins;
	    }
	}
    }
    del_status(cp, lines_del, blks_ins, ranges_del, cp_lines);
}



del_status(cp, lines_del, blks_ins, ranges_del, cp_lines)
struct commands *cp;
int lines_del, blks_ins, ranges_del, cp_lines;
{
    printf("%s from %s:%d ", (cp->type == DEL) ? "del" : "rep",
	   cp->src_file, cp->line);

    printf("deleted %d line%s", lines_del, PLURAL(lines_del));
    if(cp->start_str)		/* range of lines */
	printf(" from %d range%s", ranges_del, PLURAL(ranges_del));

    if(cp->type == REP)
	printf(", replaced %d line%s %d time%s",
	       cp_lines, PLURAL(cp_lines),
	       blks_ins, PLURAL(blks_ins));
    (void)putchar('\n');
}
	



/*
	@(#)getopt.c	2.2 (smail) 1/26/87
*/

/* [this was posted to comp.sources.misc, in volume2 at uunet.uu.net.]
   Here's something you've all been waiting for:  the AT&T Public Domain
   source for getopt(3).  It is the code which was given out at the 1985
   UNIFORUM conference in Dallas.  I obtained it by electronic mail
   directly from AT&T.  The people there assure me that it is indeed
   in the public domain.
   
   There is no manual page.  That is because the one they gave out at
   UNIFORUM was slightly different from the current System V Release 2
   manual page.  The difference apparently involved a note about the
   famous rules 5 and 6, recommending using white space between an option
   and its first argument, and not grouping options that have arguments.
   Getopt itself is currently lenient about both of these things White
   space is allowed, but not mandatory, and the last option in a group can
   have an argument.  That particular version of the man page evidently
   has no official existence, and my source at AT&T did not send a copy.
   The current SVR2 man page reflects the actual behavor of this getopt.
   However, I am not about to post a copy of anything licensed by AT&T.
*/

/* 
   Changed the ERR macro to use stdio to avoid portability probs with
   write().  Includes <stdio.h> if NULL not defined.  -Chris Schanzle
*/

#ifdef USE_MY_GETOPT

/* #define BSD */

#ifdef BSD
#include <strings.h>
#else
#include <string.h>
#define index strchr
#endif

#ifndef stderr
#include <stdio.h>
#endif

#define ERR(S, C) {if(opterr) fprintf(stderr, "%s%s%c\n", *argv, (S), (C));}

extern char *index();

int	opterr = 1;
int	optind = 1;
int	optopt;
char	*optarg;

int
getopt(argc, argv, opts)
int	argc;
char	**argv, *opts;
{
	static int sp = 1;
	register int c;
	register char *cp;

	if(sp == 1)
		if(optind >= argc ||
		   argv[optind][0] != '-' || argv[optind][1] == '\0')
			return(EOF);
		else if(strcmp(argv[optind], "--") == 0) {
			optind++;
			return(EOF);
		}
	optopt = c = argv[optind][sp];
	if(c == ':' || (cp=index(opts, c)) == NULL) {
		ERR(": illegal option -- ", c);
		if(argv[optind][++sp] == '\0') {
			optind++;
			sp = 1;
		}
		return('?');
	}
	if(*++cp == ':') {
		if(argv[optind][sp+1] != '\0')
			optarg = &argv[optind++][sp+1];
		else if(++optind >= argc) {
			ERR(": option requires an argument -- ", c);
			sp = 1;
			return('?');
		} else
			optarg = argv[optind++];
		sp = 1;
	} else {
		if(argv[optind][++sp] == '\0') {
			sp = 1;
			optind++;
		}
		optarg = NULL;
	}
	return(c);
}

#endif /* USE_LIBC_GETOPT */



/*
  	"sub.c" - routines for string substitutions.

	sub() - main routine to do string substitution given a command.
	line_sub() - actually performs the string substitution in a line.
*/

#ifndef MYTYPES
#include "mytypes.h"
#endif

sub(cp)
struct commands *cp;
{
    struct lines *lp, *lp_start, *lp_end, *lp_search = line_head.next;
    int searched, ranges, changed;

    searched = ranges = changed = 0;

    if(cp->match_str) {	    /* just sub for lines that match <match_str> */
	while((lp = search_lines(lp_search,
				 cp->match_str,
				 cp->options & GLOB)) != NULL) {
	    ++searched;
	    changed += line_sub(cp, lp);
	    lp_search = lp->next;
	}
    }
    else {			/* got <start_str> <end_str> */
	while((lp_start = search_lines(lp_search,
				       cp->start_str,
				       cp->options & GLOB)) != NULL) {
	    if((lp_end = search_lines(lp_start,
				      cp->end_str,
				      cp->options & GLOB)) == NULL) {
		break;
	    }
	    ++ranges;

	    for(lp = lp_start; lp != lp_end->next;) {
		changed += line_sub(cp, lp);
		lp = lp->next;		
		++searched;
	    }
	    lp_search = lp_end->next;
	}
    }
				/* S T A T U S */
    printf("sub from %s:%d ", cp->src_file, cp->line);
    if(cp->match_str)
	printf("found %d line%s with the <search_str> and made %d sub%s\n",
	       searched, PLURAL(searched), changed, PLURAL(changed));
    else
	printf("searched %d line%s in %d range%s and made %d sub%s\n",
	       searched, PLURAL(searched), ranges, PLURAL(ranges),
	       changed, PLURAL(changed));
}



/*  This is a touchy little routine to do the string substitutions
    for the search_str and rep_str (in cp) in the text line (lp).
    (got that?)
*/
line_sub(cp, lp)
struct commands *cp;
struct lines *lp;
{
    char *tail;
    char *t, *save;
    unsigned rep_len, sea_len;	/* replace, search string lengths */
    int pos = 0;
    int changed = FALSE;
    int n_found = 0;		/* return status */


    rep_len = strlen(cp->rep_str);
    sea_len = strlen(cp->search_str);

    if(verbose || cp->options & ECHO)	/* save line for later echo */
	save = strsave(lp->text);

    while((t = substr(lp->text + pos, cp->search_str)) != NULL) {
	if(rep_len > sea_len) {
	    lp->text = realloc(lp->text,
			       (unsigned)(strlen(lp->text) + rep_len + 2));
	    if(!lp->text)
		nomem("line_sub realloc");
	    if((t = substr(lp->text + pos, cp->search_str)) == NULL)
		printf(
		"\n\007Whoa! '%.30s' was (below) there a second ago!\n%s\n",
		       cp->search_str, lp->text);
	}
	if((tail = malloc((unsigned)(strlen(t + sea_len) + 1))) == NULL)
	    nomem("line_sub realloc tail");
	(void)strcpy(tail, t + sea_len);
	(void)strcat(strcpy(t, cp->rep_str), tail);
	free(tail);
	pos = (t - lp->text) + rep_len;
	changed = src_mod = TRUE;
	++n_found;
    }
			/* display line after all changes have been made */

    if(verbose || cp->options & ECHO) {
	if(changed)
	    printf("<%s>%s\n", save, lp->text);
	free(save);
    }
    return(n_found);
}



