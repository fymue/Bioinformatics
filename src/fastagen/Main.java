package fastagen;

import java.util.Scanner;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Arrays;

/**
 * This class reads the required program parameters provided 
 * via the command line/standard input and executes the requested tasks
 */
public class Main
{
    /**
     * main method
     * @param args standard main input parameter
     */
    public static void main(String[] args)
    {
        Fasta generatedFasta;
        InputEvaluator input = new InputEvaluator(args); //evaluate the command line input

        if (input.isValid)
        {         
            FastaProcessor processor = new FastaProcessor(input); // process Fasta entries (read or generate)
        }
        
    } 
}
