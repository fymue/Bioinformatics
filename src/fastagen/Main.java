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
        String fastaPath;
        Fasta generatedFasta;
        SequenceCollection<String, Sequence> fastaEntries = new SequenceCollection<>(); //sequence collection

        //valid Fasta file extensions
        HashSet<String> fastaFileExts = new HashSet<>(Arrays.asList(".fna", ".fasta", ".faa", ".fa", ".ffn", ".frn"));
        
        Scanner inp = new Scanner(System.in); //in "read" mode, user can enter a file path to read an existing Fasta file

        InputEvaluator input = new InputEvaluator(args); //evaluate the command line input

        if (input.isValid)
        {
            //if program was started in "read" mode, prompt the user to enter a input Fasta file
            if (input.collector.mode.equals("read")) 
            {
                System.out.println("Please provide a valid Fasta file or press ENTER to exit...");

                /*
                 * check if the provided Fasta file could be read/parsed successfully
                 * by looping until the size of the collection is no longer 0,
                 * which means that the Fasta entries of the input file were successfully
                 * added to the collection.
                 */
                while (fastaEntries.size() == 0)
                {
                    fastaPath = inp.nextLine().strip(); //read the user input (which should be a Fasta file path)

                    //if the user enters ENTER, stop trying to read the input
                    if (fastaPath.isEmpty()) break; 

                    try
                    {
                        //find the start index of the file extension of the input Fasta file
                        int extStart = fastaPath.indexOf(".");
                        
                        //if the input file doesn't have a file extension, throw a NotFastaException
                        if (extStart == -1) throw new NotFastaException("None");

                        String inputFastaExt = fastaPath.substring(extStart); //file extension of the Fasta file

                        //check if the file extension is in the set of valid Fasta file extensions
                        if (!fastaFileExts.contains(inputFastaExt)) throw new NotFastaException(inputFastaExt);

                        //try to read the Fasta file and add all entries to the sequence collection
                        fastaEntries.putAll(readFasta(fastaPath)); 

                        if (input.collector.fileName.equals("SET_ME"))
                        {
                            /*
                             * if the user wishes the input file to be saved (--out option was entered earlier),
                             * replace the file extension with "_parsed.fna"
                             */
                            input.collector.fileName = fastaPath.substring(0, extStart) + "_parsed.fna";  
                        }
                    }
                    catch (FastaMalformattedException e) //catch if the provided Fasta was malformatted and exit
                    {
                        System.out.println(e);
                        break;
                    }
                    catch (FileNotFoundException e) //try again if the file could not be found
                    {
                        System.out.println("This file could not be found! Try again or press ENTER to exit...");
                    }
                    catch (NotFastaException e) //catch if the file doesn't have a proper Fasta file extension
                    {
                        System.out.println(e);
                        System.out.println("Try again or press ENTER to exit...");
                    }
                    catch (IOException e) //catch IO errors while reading the file and exit
                    {
                        System.out.println("Error while reading file!");
                        break;
                    }
                }

                inp.close();
                System.out.println();
            }
            
            else if (input.collector.mode.equals("generate"))
            {
                //create the requested fasta object (the Fasta entries/Sequence objects are automatically generated)
                generatedFasta = FastaCreator.createFasta(input.collector);
                fastaEntries.putAll(generatedFasta.getEntries());
                System.out.println();
            }
                
            /*
             * if not output file argument was provided or if the 
             * input Fasta parsing process was interrupted/ended prematurely,
             * print out the sequences/entries
            */
            if (input.collector.fileName.isEmpty() || input.collector.fileName.equals("SET_ME"))
            {

                for(String id: fastaEntries.keySet())
                {
                    Sequence currSeq = fastaEntries.get(id);

                    System.out.println(id + ": " + currSeq.getSequence());

                    //if sequence is genomic, also print its properties (using modified toString() of the Sequence class)
                    if (currSeq.isGenomic) System.out.println(currSeq); 
                }
            }
            //if an output file was provided, write the entries to that file
            else AbstractFasta.writeEntries(fastaEntries, input.collector.fileName, input.collector.writeProperties);
        }
        
    }

    /**
     * reads the input Fasta file and checks if it is formatted correctly.
     * If not it will throw the proper exceptions.
     * @param inputFasta the path to the input Fasta file
     * @return a <code>SequenceCollection</code> object containing all the parsed header-sequence pairs
     * @throws FastaMalformattedException if the input Fasta file was malformatted
     * @throws IOException if an error occured during the reading/parsing process
     * @throws FileNotFoundException if the input file could not be found
     */
    private static SequenceCollection<String, Sequence> readFasta(String inputFasta)
    throws FastaMalformattedException, IOException, FileNotFoundException
    {
        String l;
        String header = null;
        String seq = null;
        int lineCount = 0; 

        char[] alphabet = {'A', 'C', 'G', 'T'};
        SequenceCollection<String, Sequence> inputSeqs = new SequenceCollection<>();
        BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(inputFasta))); 

        //read the input Fasta file
        while ((l = fin.readLine()) != null)
        {
            lineCount++; //count the lines to throw more accurate exceptions 
            if (l.isBlank() || l.startsWith(";")) continue; //skip blank and comment lines
            l = l.strip();

            if (header == null)
            {
                header = l; //current line is header
                if (!header.matches(">[^>\s]+"))
                {
                    //if the header is not formatted correctly, throw exception
                    throw new FastaMalformattedException(String.format("invalid header (line %d)!", lineCount));
                }
                continue;
            }

            if (header != null)
            {
                seq = l; //current line is sequence of last header
                if (!seq.matches("[ATCG]+"))
                {
                    //if the sequence contains non-IUPAC characters, throw exception
                    throw new FastaMalformattedException(
                    String.format("sequence contains non-IUPAC characters (line %d)! Only genomic (DNA) sequences are supported!", lineCount));
                }

                inputSeqs.put(header, new Sequence(seq, alphabet)); //add the header-sequence pair to the collection
                header = null; //reset the header and sequence strings so the next header-sequence pair can be read
                seq = null;
            }
        }
        
        fin.close();

        return inputSeqs;
    }  
}
