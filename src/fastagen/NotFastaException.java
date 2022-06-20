package fastagen;

import java.lang.Exception;

/**
 * Custom exception to handle files that don't have a valid Fasta extension
 */
public class NotFastaException extends Exception
{
    public NotFastaException(String ext)
    {
        super(String.format("Invalid file extension (%s)! The provided file is not a Fasta file!", ext));
    }
    
}
