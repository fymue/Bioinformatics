package fastagen;

/**
 * This class serves as part of the Factory pattern
 * and handles the creation of the correct Fasta (sub)class
 */
public class FastaCreator
{
    /**
     * Factory method which handles the creation of the correct Fasta class (user specified)
     * @param collector an <code>InputCollector</code> object which holds 
     * all of the passed command line arguments by the user
     * @return an object of type <code>Fasta</code>
     */
    public static Fasta createFasta(InputCollector collector)
    {
        Fasta fasta = switch (collector.type)
        {
            case "protein" -> new ProteinFasta(collector);

            case "genome" -> new GenomeFasta(collector);
                                             
            default -> null;
        };   

        return fasta;
    }
    
}
