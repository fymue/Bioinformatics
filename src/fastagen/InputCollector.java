package fastagen;

/**
 * This class is used to store the command line arguments passed by the user 
 */
public class InputCollector
{
    /**
     * stores the mode of the program (reading an existing File or generating a new one)
     */
    protected String mode;

    /**
     * stores the type of Fasta file to be created
     */
    protected String type;

    /**
     * stores the minimum and maximum length of the randomly-generated sequences
     */
    protected int[] interval;

    /**
     * stores the total number of entries that will be generated
     */
    protected int totalEntries;

    /**
     * stores the name (and path) of the output file
     */
    protected String fileName;

    /**
     * stores the number of processor cores the worker threads will be divided to
     */
    protected int nCores;

    /**
     * states, whether or not runtime information should be printed to the console
     */
    protected boolean quiet;

    /**
     * states, whether or not nucleotide sequence properties should be written to the Fasta file
     */
    protected boolean writeProperties;
    
}
