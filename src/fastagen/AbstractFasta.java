package fastagen;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.HashMap;

import java.io.IOException;
import java.lang.InterruptedException;
import java.util.concurrent.ExecutionException;

/**
 * The abstract class <code>AbstractFasta</code> implements the <code>Fasta</code> interface
 * and serves as a superclass bundling all common methods
 * of concrete Fasta classes
 */
public abstract class AbstractFasta implements Fasta
{
    /**
     * stores all of the generated <code>Sequence</code> objects
     */
    protected SequenceCollection<String, Sequence> entries;

    /**
     * stores the name of the output file (user input)
     */
    protected String fileName;

    /**
     * stores the alphabet that the entries are composed of
     */
    protected char[] alphabet;

    @Override
    public int getTotalEntries() {return entries.size();}

    @Override
    public SequenceCollection<String, Sequence> getEntries() {return entries;}

    @Override
    public SequenceCollection<String, Sequence> generateEntries(int totalEntries, int[] interval, int nCores, boolean quiet)
    {
        SequenceCollection<String, Sequence> entries = new SequenceCollection<>();

        if (!quiet) System.out.printf("\nOpening thread pool with %d threads...\n\n", nCores);

        ExecutorService threadPool = Executors.newFixedThreadPool(nCores); //create a thread pool with nCores cpu cores
        HashMap<String, Future<Sequence>> futures = new HashMap<>(); //store the results of the call method of a Callable here

        for (int i=0; i<totalEntries; i++)
        {
            String header = ">randomSequence" + (i+1); //create a unique header for the current sequence
            SequenceGenerator seq = new SequenceGenerator(header, interval, alphabet, quiet); //Callable object
            
            //add the callable to the thread pool and let the executor invoke its call method
            Future<Sequence> future = threadPool.submit(seq); 

            futures.put(header, future); //store the future object (==returned object of the Callable)
        }

        for (String header: futures.keySet())
        {
            try
            {
                entries.put(header, futures.get(header).get()); //collect the future objects
            }
            catch (InterruptedException | ExecutionException e) 
            {
                e.printStackTrace();
            }
        }

        if (!quiet) System.out.print("\nShutting down thread pool...");

        threadPool.shutdown(); //shut down the Executor service
        while (!threadPool.isTerminated()) {} //wait until the Executor service is terminated

        if (!quiet) System.out.println("Done!\n");

        return entries;
    }

    /**
     * writes the generated Fasta entries to a file in Fasta format
     * @param entries a <code>SequenceCollection</code> object containing the entries to be written to the file
     * @param fileName the name (and path) of the output file
     * @param writeProperties states, whether or not nucleotide sequence properties should be written to the file
     */
    public static void writeEntries(SequenceCollection<String, Sequence> entries, String fileName, boolean writeProperties)
    {
        //write all entries to a file in Fasta format
        try
        {
            BufferedWriter fout = new BufferedWriter(new FileWriter(fileName));

            for (String header: entries.keySet())
            {
                Sequence currSeq = entries.get(header);
                fout.write(header);
                fout.newLine();

                /*
                 * add a comment line with GC content, molecular weight and melting temperature
                 * of the current sequence to the Fasta file if the sequence is a genomic sequence
                 * (using the toString() method of the Sequence class)
                */
                if (currSeq.isGenomic && writeProperties) fout.write(";" + currSeq);

                fout.write(currSeq.getSequence());
                fout.newLine();
            }
            fout.close();
        }
        catch (IOException e)
        {
            //catch IO errors if they appear
            System.out.println("Error occured while writing to file!");
            e.printStackTrace();
        }

    }

    @Override
    public abstract String getAlphabet();
    
}
