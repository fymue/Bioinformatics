package fastagen;

/**
 * This class extends <code>AbstractFasta</code>
 * and creates a Fasta file with <code>totalEntries</code> randomly-generated
 * sequences of nucleotides (A, C, G, T)
 */
public class GenomeFasta extends AbstractFasta
{
    private static final char[] alphabet = {'A', 'C', 'T', 'G'};

    GenomeFasta(InputCollector c)
    {
        super.alphabet = alphabet;
        super.fileName = c.fileName;
        super.entries = generateEntries(c.totalEntries, c.interval, c.nCores, c.quiet);
    }

    @Override
    public String getAlphabet() {return "DNA (" + new String(alphabet) + ")";}
    
}
