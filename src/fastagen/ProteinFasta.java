package fastagen;

/**
 * This class extends <code>AbstractFasta</code>
 * and creates a Fasta file with <code>totalEntries</code> randomly-generated
 * sequences of amino acids (peptides)
 */
public class ProteinFasta extends AbstractFasta
{
    private static final char[] alphabet = {'A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L',
                                            'M', 'N', 'P', 'Q', 'R', 'S','T', 'V', 'W', 'Y'};

    ProteinFasta(InputCollector c)
    {
        super.alphabet = alphabet;
        super.fileName = c.fileName;
        super.entries = generateEntries(c.totalEntries, c.interval, c.nCores, c.quiet);
    }

    @Override
    public String getAlphabet() {return "Amino Acids (" + new String(alphabet) + ")";}
    
}
