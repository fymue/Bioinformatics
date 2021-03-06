package alignment;

import java.util.*;
import java.io.*;

public class Blosum
{
    public HashMap<String, Double> blosum;
    private String aminos = "CSTPAGNDEQHRKMILVFYWJ";

    public Blosum(String blocksfile)
    {
        this(blocksfile, true, "");
    }

    public Blosum(String blocksFile, boolean printBlosum, String outputFile)
    {
        ArrayList<ArrayList<String>> blocks = readSequences(blocksFile);
        this.blosum = calcBlosum(blocks, aminos);

        if (printBlosum) printBlosum(blosum, aminos);
        if (!outputFile.isEmpty()) writeBlosum(blosum, outputFile);


    }

    private static double log2(double x) {return Math.log(x) / Math.log(2);}

    ArrayList<ArrayList<String>> readSequences(String blocksFile)
    {
        ArrayList<ArrayList<String>> blocks = new ArrayList<ArrayList<String>>();
        ArrayList<String> seqsOfBlock = new ArrayList<String>();
        boolean look = false;

        try
        {
            Scanner fin = new Scanner(new File(blocksFile));
            
            while (fin.hasNextLine())
            {
                String l = fin.nextLine();

                if (l.isBlank()) continue;

                //look for new block of gap-free, aligned sequences
                if (l.startsWith("BL "))
                {
                    look = true;
                    continue;
                }
                if (l.equals("//")) //end of block marker
                {
                    look = false;
                    blocks.add(seqsOfBlock); //add all sequences of current block
                    seqsOfBlock = new ArrayList<String>();
                    continue;
                }
        
                //if program is currently looking through a block, add the sequence
                if (look) seqsOfBlock.add(l.strip().split("\\)  ")[1]);
            }
            fin.close();
        }
        catch (FileNotFoundException ex)
        {
            System.out.println("The file could not be found!");
        }
        return blocks;
    }

    HashMap<String, Double> calcBlosum(ArrayList<ArrayList<String>> blocks, String aminos)
    {
        HashMap<String, Double> qijMatrix = new HashMap<String, Double>();
        HashMap<Character, Double> pijMatrix = new HashMap<Character, Double>();
        HashMap<String, Double> sijMatrix = new HashMap<String, Double>();
        int totalSubs = 0; //keep track of the total number of substitutions (subs)

        //create upper triangular matrix with all (one-sided) pairs of amino acids (used in step 1 and 2, see below)
        for (int i=0; i<aminos.length(); i++)
        {
            pijMatrix.put(aminos.charAt(i), 0.0);

            for (int j=i; j<aminos.length(); j++)
            {
                String aminopair = Character.toString(aminos.charAt(i)) + Character.toString(aminos.charAt(j));
                qijMatrix.put(aminopair, 0.0);
            }
        }

        //Step 1: count the total number of substitutions of all amino acid pairs (Original BLOSUM paper: f_ij-Matrix)
        for (int i=0; i<blocks.size(); i++)
        {
            ArrayList<String> seqs = blocks.get(i);
            int totalSeqs = seqs.size();

            for (int j=0; j<totalSeqs; j++)
            {
                String currSeq = seqs.get(j);

                for (int k=0; k<currSeq.length(); k++)
                {
                    for (int l=j+1; l<totalSeqs; l++)
                    {
                        String nxtSeq = seqs.get(l);
                        String aminopair = Character.toString(currSeq.charAt(k)) + Character.toString(nxtSeq.charAt(k));
                        if (!qijMatrix.containsKey(aminopair)) aminopair = new StringBuilder(aminopair).reverse().toString();
                        qijMatrix.put(aminopair, qijMatrix.get(aminopair) + 1);
                        totalSubs++;
                    }
                }
            }
        }

        for (String aminopair: qijMatrix.keySet())
        {
            //Step 2: Divide every value by the total number of subs (to get the relative number of subs) (Original BLOSUM paper: q_ij-Matrix)
            qijMatrix.put(aminopair, qijMatrix.get(aminopair) / totalSubs);

            char as1 = aminopair.charAt(0);
            char as2 = aminopair.charAt(1);

            //Step 3: Sum up the relative frequencies of observed subs for every amino acid (Original BLOSUM paper: p_ij-Matrix)
            if (as1 == as2) pijMatrix.put(as1, pijMatrix.get(as1) + qijMatrix.get(aminopair));
            else
            {
                pijMatrix.put(as1, pijMatrix.get(as1) + qijMatrix.get(aminopair) / 2);
                pijMatrix.put(as2, pijMatrix.get(as2) + qijMatrix.get(aminopair) / 2);
            }
        }

        for (String aminopair: qijMatrix.keySet())
        {
            char as1 = aminopair.charAt(0);
            char as2 = aminopair.charAt(1);

            //Step 4: Calculate the estimated sub frequencies for every pair of amino acids (Original BLOSUM paper: e_ij-Matrix)
            double value = pijMatrix.get(as1) * pijMatrix.get(as2);
            if (as1 != as2) value *= 2;
            
            //Step 5: Divide the observed sub frequencies by the estimated sub frequencies and normalize them (log2) (Original BLOSUM paper: s_ij-Matrix)
            //this matrix equals the BLOSUM
            value = (double)Math.round(2 * log2(qijMatrix.get(aminopair) / value));
            sijMatrix.put(aminopair, value);
        }

        return sijMatrix;
    }

    public void printBlosum(HashMap<String, Double> blosum, String aminos)
    {
        //print the BLOSUM matrix
        System.out.println("   " + String.join("    ", aminos.split("")));
        boolean start = true;
        char curr = '0';

        for (int i=0; i<aminos.length(); i++)
        {
            char as1 = aminos.charAt(i);

            for (int j=0; j<aminos.length(); j++)
            {
                char as2 = aminos.charAt(j);
                String aminopair = Character.toString(as1) + Character.toString(as2);

                if (as1 != curr)
                {
                    curr = as1;
                    start = true;
                    System.out.println();
                }
                if (start)
                {
                    System.out.print(as1);
                    start = false;
                }
                if (blosum.containsKey(aminopair)) System.out.printf("%4.0f ", blosum.get(aminopair));
                else System.out.print("     ");
            }
        }
        System.out.println("\n");
    }

    public void writeBlosum(HashMap<String, Double> blosum, String outputFile)
    {
        try
        {
            BufferedWriter fout = new BufferedWriter(new FileWriter(outputFile));
            fout.write("#observed amino acid\treplacement amino acid\tcost\n");
            for (String aminopair: blosum.keySet())
            {
                String as1 = Character.toString(aminopair.charAt(0));
                String as2 = Character.toString(aminopair.charAt(1));
                String cost = Integer.toString((int)((double)blosum.get(aminopair)));
                fout.write(as1 + "\t" + as2 + "\t" + cost + "\n");
            }
            fout.close();
        }
        catch (IOException e)
        {
            System.out.println("Error occured while writing to file!");
            System.out.println(e);
        }
    }

    public static void main(String[] args)
    {
        List<String> tmpValidCommands = Arrays.asList("-matrix", "-save");
        List<String> tmpHelpCommands = Arrays.asList("--help", "-help", "-h");
        HashSet<String> validCommands = new HashSet<String>(tmpValidCommands);
        HashSet<String> helpCommands = new HashSet<String>(tmpHelpCommands);
        HashSet<String> allCommands = validCommands;
        allCommands.addAll(helpCommands);
        HashSet<String> cmdArgs = new HashSet<String>(Arrays.asList(args));

        CommandLineHelper helper = new CommandLineHelper("blosum");

        boolean valid = helper.isValid(args, allCommands);

        if (args.length == 0  || !valid)
        {
            System.out.println("Usage: Blosum.class [OPTIONS] path/to/blocks/file\n");
            System.out.println("use --help, -help or -h to display usage help\n");
        }
        else if (args.length == 1 && helpCommands.contains(args[0])) helper.printHelp();

        else if (args.length >= 2 && valid)
        {
            String blocksFile = args[args.length-1];
            
            String outputFile = helper.getArgsVal(args, "-save", "");
            boolean printMatrix = cmdArgs.contains("-print") ? true : false;
        
            Blosum blosum = new Blosum(blocksFile, printMatrix, outputFile);
        }
        
    }
}