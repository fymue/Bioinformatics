package fastagen;

import java.util.HashSet;
import java.util.Arrays;

/**
 * This class parses the command line arguments entered by the user
 */
public class CommandLineParser
{
    /**
     * states, whether or not the command line arguments entered by the user
     * are syntactically correct (not necessarily logically correct)
     */
    boolean seemsValid;

    /**
     * states, whether or not the command line input should be further 
     * evaluated to see if the logic is correct
     */
    boolean goAhead, guiMode = false;

    private String[] args;

    private HashSet<String> helpCommands = new HashSet<>(Arrays.asList("--help", "-help", "-h"));
    private HashSet<String> validCommands = new HashSet<>(Arrays.asList("--entries", "-e", "--threads", "-t",
                                                                        "--out", "-o", "--length", "--quiet",
                                                                        "-l", "-q", "--write-properties", "--header"));

    public CommandLineParser(String[] args)
    {
        this.args = args;
        validCommands.addAll(helpCommands);

        //check if command line arguments have correct syntax (doesn't check for correct logic yet)
        this.seemsValid = this.isValid();

        if (args.length == 1 && helpCommands.contains(args[0])) //check if help message was requested
        {
            this.printHelp();
            this.goAhead = false; //again, InputEvaluator doesn't need to start trying to process the input
        }

        else if (args.length == 1 && args[0].toLowerCase().equals("gui")) //check if program should run in GUI mode
        {
            this.guiMode = true;
            this.goAhead = true;
        }

        else if (args.length == 0  || !this.seemsValid) //if command line input was syntactically wrong, print this
        {
            System.out.println("This program can be used from both the command line (CLI) as well as from a user interface (GUI):\n");
            System.out.println("Usage (CLI mode): java -cp /path/to/bin/ -module-path path/to/javafx/lib/ --add-modules javafx.controls,javafx.fxml fastagen.Main (read|generate) (Protein|Genome) [Options]\n");
            System.out.println("Usage (GUI mode): java -cp /path/to/bin/ -module-path path/to/javafx/lib/ --add-modules javafx.controls,javafx.fxml fastagen.Main GUI \n");
            System.out.println("use --help, -help or -h to display usage help\n");
            this.goAhead = false; //InputEvaluator doesn't need to even start trying to process the input
        }

        else if (args.length >= 2 && this.seemsValid)
        {
            //if input was syntactically correct, give InputEvaluator the "go ahead" to evaluate the logic
            this.goAhead = true; 
        }
    }

    private void printHelp()
    {
        System.out.println("Usage (GUI mode): java -cp /path/to/bin/ -module-path path/to/javafx/lib/ --add-modules javafx.controls,javafx.fxml fastagen.Main GUI\n");
        System.out.println("Usage (CLI mode): java -cp /path/to/bin/ -module-path path/to/javafx/lib/ --add-modules javafx.controls,javafx.fxml fastagen.Main (read|generate) (Protein|Genome) [Options]\n");
        System.out.println("(read|generate) specifies whether an existing Fasta file should be read or a new one should be generated");
        System.out.println("(Protein|Genome) specifies which alphabet to use for the sequence generation\n");
        System.out.println("'generate' Mode Options:\n");
        System.out.println("-e, --entries <n>\t\tnumber of sequence entries to generate (default: 1)");
        System.out.println("-l, --length <min..max>\t\tminimum and maximum length of each sequence (default: 20..30)");
        System.out.println("-t, --threads <n>\t\tnumber of threads/cpu cores to use during entry generation (default: 3/4 of available cores)");
        System.out.println("-o, --out <file>\t\tfile (and path) to write the sequence entries to (default: print to console)");
        System.out.println("-q, --quiet\t\t\tdon't print any runtime (thread) information to the console");
        System.out.println("--write-properties\t\talso write the nucleotide sequence properties to the Fasta file");
        System.out.println("--header <prefix>\t\t\tadd a prefix to the header of every generated Fasta entry\n");

        System.out.println("'read' Mode Options:\n");
        System.out.println("-o, --out \t\t\twrite read entries to a new file 'inputfile_parsed.fna'");
        System.out.println("-q, --quiet\t\t\tdon't print any runtime (thread) information to the console");
        System.out.println("--write-properties\t\talso write the nucleotide sequence properties to the Fasta file\n");

    }

    /**
     * parses the value provided by the user for a specified command line argument/option
     * @param args the command line arguments entered by the user
     * @param argLong the long version of the current argument (e.g. "--entries")
     * @param argShort the short version of the current argument (e.g. "-e")
     * @param defaultVal the default value to use within the program if the current argument was not provided
     * @return the argument entered by the user (if specified) or the default argument
     * @see <code>printHelp()</code> for the default arguments the program uses 
     */
    public String getArgVal(String argLong, String argShort, String defaultVal)
    {
        for (int i=0; i<this.args.length; i++)
        {
            if (this.args[i].equals(argLong) || this.args[i].equals(argShort))
            {
                return this.args[i+1];
            }
        }
        return defaultVal;
    }

    /**
     * checks, whether or not a command line option was entered
     * @param argLong the long version of the option (e.g. "--quiet")
     * @param argShort the short version of the option (e.g. "-q")
     * @return true or false, whether or not the option was entered
     */
    public boolean checkOption(String argLong, String argShort)
    {
        for (String arg: this.args)
        {
            if (arg.equals(argLong) || arg.equals(argShort)) return true;
        }
        return false;
    }

    private boolean isValid()
    {
        if (args.length < 2) return false;

        for (int i=0; i<args.length; i++)
        {
            //check if actual values of options are regular strings/numbers
            if (args[i].charAt(0) != '-' && args[i].matches("[a-zA-Z0-9\\._]+")) continue;

            //check if a unspecified option parameter was entered
            if (!validCommands.contains(args[i])) return false;
        }
        return true;
    } 
}
