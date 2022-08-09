package fastagen;

/**
 * The <code>Fasta</code> interface defines all methods that will be accessible
 * in every Fasta (sub)class
 */
public interface Fasta
{
    /**
     * returns the total number of entries of the Fasta file
     * @return the total number of entries in the Fasta file
     */
    int getTotalEntries();

    /**
     * returns a <code>SequenceCollection</code> object
     * containing the header and <code>Sequence</code> 
     * of every generated Fasta entry
     * @return all generated entries for the Fasta file
     * */
    SequenceCollection<String, Sequence> getEntries();

    /**
     * generates <code>totalEntries</code> <code>Sequence</code> objects
     * with a random length within the range of <code>interval</code>
     * using a thread pool to distribute the jobs to multiple cores/threads
     * @param totalEntries the total number of entries to generate
     * @param interval the minimal and maximal length of a random sequence
     * @param nCores the number of cpu cores to distribute the threads to
     * @param quiet states, whether or not runtime information should be printed to the console
     * @param fastaHeaderPrefix specified, which prefix to put in the header of every Fasta entry
     * @return a <code>SequenceCollection</code> object storing the IDs as well as the generated <code>Sequence</code> objects
     */
    SequenceCollection<String, Sequence> generateEntries(int totalEntries, int[] interval, int nCores, boolean quiet, String fastaHeaderPrefix);

    /**
     * returns the type and characters of the alphabet specified by the user
     * @return the type and characters of the alphabet
     */
    String getAlphabet();
}