package fastagen;

import java.util.HashMap;

/**
 * This class extends <code>HashMap</code> and is used
 * to store <code>Sequence</code> objects and their
 * associated IDs. One can also use the <code>generateEntries</code>
 * method of this class to generate (additional) <code>Sequence</code> objects
 * of random sequences, which will be stored directly in an object of this class,
 * even if the object already contains <code>Sequence</code> objects.
 */
public class SequenceCollection<K, V> extends HashMap<String, Sequence>
{

    private Sequence generateRandomSequence(int[] interval, char[] alphabet)
    {   
        //generate a random nucleotide sequence (method is used in generateEntries() method)

        //use a StringBuilder object to store/build the string for better performance
        StringBuilder seq = new StringBuilder(); 
        int randomIndex;

        //random number between minimum (interval[0]) and maximum (interval[1]) (both inclusive)
        int seqLength = interval[0] + (int) (Math.random() * (interval[1] - interval[0] + 1));

        for (int i=0; i<=seqLength; i++)
        {
            //random index within the length of the alphabet array to pick a random character
            randomIndex = (int) (Math.random() * alphabet.length); 
            seq.append(alphabet[randomIndex]);
        }

        return new Sequence(seq.toString(), alphabet);
    }

    /**
     * generates <code>totalEntries</code> <code>Sequence</code> objects
     * with a random length within the range of <code>interval</code>
     * @param totalEntries the total number of entries to generate
     * @param interval the minimal and maximal length of a random sequence
     */
    public void generateEntries(int totalEntries, int[] interval, char[] alphabet)
    {
        int nEntries = this.size();

        for (int i=nEntries; i<nEntries+totalEntries; i++)
        {
            String header = ">randomSequence" + (i+1); //create a unique header for the current sequence
            Sequence sequence = this.generateRandomSequence(interval, alphabet);
            this.put(header, sequence); //add the header and sequence to this object
        }
    }
}
