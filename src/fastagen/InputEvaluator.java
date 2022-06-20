package fastagen;

import java.lang.NumberFormatException;
import java.lang.IndexOutOfBoundsException;

/**
 * This class is used to check if all the user-provided
 * command line arguments are logically correct
 */
public class InputEvaluator
{
    /**
     * holds all passed command line arguments
     */
    protected InputCollector collector = new InputCollector();

    /**
     * stores the result of the <code>evaluateInput()</code> method
     */
    protected boolean isValid = false;

    private int maxCores = Runtime.getRuntime().availableProcessors(); //get maximum available cpu cores

    public InputEvaluator(String[] args)
    {
        CommandLineParser parser = new CommandLineParser(args);

        if (parser.goAhead) this.isValid = evaluateInput(parser, args);
    }

    /**
     * evaluates all passed command line arguments and checks for correctness
     * @param args the provided command line arguments
     * @return a boolean indicating whether the entire input was correct
     */
    private boolean evaluateInput(CommandLineParser parser, String[] args)
    {
        /* check if the command line input is correct
        *  and store the user input from args in the InputCollector object
        *  (if it passed the evaluation)
        */

        String mode = args[0].toLowerCase(); // read/generate mode
        if (!mode.equals("read") && !mode.equals("generate"))
        {
            System.out.println("mode must be either 'read' or 'generate'!");
            return false; //return early in this case since checking the remaining parameters is unnecessary
        }

        this.collector.mode = mode; //add the input to the collector

        String type = args[1].toLowerCase(); //type of alphabet to use for sequence generation
        if (!type.equals("protein") && !type.equals("genome"))
        {
            System.out.println("type must be either 'Protein' or 'Genome'!");
            return false;
        }
    
        this.collector.type = type; 

        try
        {
            int totalEntries = Integer.parseInt(parser.getArgVal("--entries", "-e", "1"));

            //if provided number is too small, throw exception and jump to the catch block
            if (totalEntries < 1) throw new NumberFormatException();

            this.collector.totalEntries = totalEntries;
        }
        catch (NumberFormatException e)
        {
            System.out.println("totalEntries must be a whole number bigger than 0!");
            return false;
        }

        try
        {
            //store the specified interval as an array of two integers
            String intervalString = parser.getArgVal("--length", "-l", "20..30");
            String[] tmpInterval = intervalString.split("\\.\\.");
            int[] interval = {Integer.parseInt(tmpInterval[0]), Integer.parseInt(tmpInterval[1])};
            
            //if 2nd provided number is smaller the 1st number, throw exception and jump to the catch block
            if (interval[0] >= interval[1]) throw new NumberFormatException();

            this.collector.interval = interval;
        }
        /* IndexOutOfBoundsException handles case where the interval input was incorrect
        * (meaning if split() couldn't split the string and the array only has one value)
        */
        catch (NumberFormatException | IndexOutOfBoundsException e)
        {
            System.out.println("sequence length interval must be provided as 'min_length..max_length'!");
            System.out.println("min_length must be smaller than max_length!");
            System.out.println("min_length and max_length must be whole numbers bigger than 0!");
            return false;
        }

        try
        {   
            //default: 75% of available cores will be used
            int nCores = Integer.parseInt(parser.getArgVal("--threads", "-t", 
                                          Integer.toString((int)(maxCores * 0.75))));

            //if provided number is too small or too big, throw exception and jump to the catch block
            if (nCores < 1 || nCores > maxCores) throw new NumberFormatException();

            this.collector.nCores = nCores;
        }
        catch (NumberFormatException e)
        {
            System.out.printf("The number of cores to use must be bigger than 1 and smaller than %d!\n", maxCores+1);
            return false;
        }

        String fileName;
        if (mode.equals("generate")) fileName = parser.getArgVal("--out", "-o", "");
        /*
        * if the mode is 'read' (else case), check if the user wishes the input file to saved as a
        * different/modified file (has to pass --out argument for that).
        * If so, the file name will be an altered version of the input file
        * which will be entered after this cmd line arguments
        * (which is why it is temporarily set to "SET_ME" here)
        */
        else fileName = (parser.checkOption("--out", "-o")) ? "SET_ME" : ""; 

        this.collector.fileName = fileName;
        
        boolean quiet = parser.checkOption("--quiet", "-q");
        this.collector.quiet = quiet;

        boolean writeProperties = parser.checkOption("--write-properties", "");
        this.collector.writeProperties = writeProperties;

        return true; //if we get to this point, the user input is correct
    }  
}
