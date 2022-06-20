package uebung4;

import java.lang.Exception;

/**
 * Custom exception to handle malformatted Fasta files
 */
public class FastaMalformattedException extends Exception
{
    public FastaMalformattedException(String errorMessage)
    {
        super(errorMessage);
    }
    
}
