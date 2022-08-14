package fastagen;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.lang.Exception;
import java.lang.Thread;

/**
 * This class implements the <code>Callable</code> interface and is used for
 * the generation of random nucleotide/protein sequences in a thread pool
 */
public class SequenceGenerator implements Callable<Sequence>
{
    private char[] alphabet;
    private String name;
    private int[] interval;
    private boolean quiet;


    public SequenceGenerator(String name, int[] interval, char[] alphabet, boolean quiet)
    {
        this.name = name;
        this.interval = interval;
        this.alphabet = alphabet;
        this.quiet = quiet;
    }

    /**
     * implements the call() method of the <code>Callable</code> interface
     * and generates a random sequence of characters from <code>alphabet</code>
     * with a length within <code>interval</code>. The generated sequence
     * is wrapped in a <code>Sequence</code> object,
     * which calculates relevant properties of the sequence
     * @return a <code>Sequence</code> object containing the randomly-generated sequence
     */
    @Override
    public Sequence call()
    {  
        String currentThread = Thread.currentThread().getName();

        if (!quiet) System.out.printf("Starting thread \"%s\" for %s\n", currentThread, name);

        //use a StringBuilder object to store the string for better performance
        StringBuilder seq = new StringBuilder(); 
        int randomIndex;

        try
        {
            //random number between minimum (interval[0]) and maximum (interval[1]) (both inclusive)
            int seqLength = interval[0] + (int) (Math.random() * (interval[1] - interval[0] + 1));

            for (int i=0; i<seqLength; i++)
            {
                //random index within the length of the alphabet array to pick a random character
                randomIndex = (int) (Math.random() * alphabet.length); 
                seq.append(alphabet[randomIndex]);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (!quiet) System.out.printf("Ending thread \"%s\" for %s\n", currentThread, name);
        
        /*
         * convert the StringBuilder to an actual String and pass it to a new Sequence object, which is then returned
         * the property calculations (GC content etc.) are automatically done when invoking the constructor
        */
        return new Sequence(seq.toString(), alphabet);
    }
}
