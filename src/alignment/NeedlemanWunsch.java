package alignment;

import java.util.*;
import alignment.CommandLineHelper;

public class NeedlemanWunsch
{
    private String costf;
    private int match, mismatch, gap;
    private boolean printMax, printTrace, printScores, printOptimal, caseSensitive;

    public NeedlemanWunsch()
    {
        this(0, 1, 1, "min", false, true, true, true, false);
    }

    public NeedlemanWunsch(int match, int mismatch, int gap)
    {
        this(match, mismatch, gap, "min", false, true, false, true, false);
    }

    public NeedlemanWunsch(int match, int mismatch, int gap, String costf, boolean printMax, boolean printTrace,
                           boolean printScores, boolean printOptimal, boolean caseSensitive)
    {
        this.match = match;
        this.mismatch = mismatch;
        this.gap = gap;
        this.costf = costf;
        this.printMax = printMax;
        this.printOptimal = printOptimal;
        this.printScores = printScores;
        this.printTrace = printTrace;
        this.caseSensitive = caseSensitive;
    }

    public long maxAlignments(int m, int n)
    {
        //iterative version of the maximum alignments function
        //requires an m*n sized array

        m++;
        n++;

        long[][] arr = new long[m][n];

        //initialize the 1st row and column to 1 (one empty string and another (non-)emtpy string have exactly 1 alignment)
        for (int i=0; i<m; i++) arr[i][0] = 1;
        for (int i=0; i<n; i++) arr[0][i] = 1;

        //definition of maximum alignments function: A(m, n) = A(m-1, n) + A(m-1, n-1) + A(m, n-1)
        for (int i=1; i<m; i++)
        {
            for (int j=1; j<n; j++)
            {
                long a = arr[i-1][j];
                long b = arr[i-1][j-1];
                long c = arr[i][j-1];
                arr[i][j] = a + b + c;
            }
        }

        return arr[m-1][n-1];
    }

    public void align(String s, String t)
    {
        align(s, t, this.match, this.mismatch, this.gap, this.printMax,
              this.printOptimal, this.printScores, this.printTrace, this.caseSensitive);
    }

    public void align(String s, String t, int match, int mismatch, int gap, boolean printMax,
                      boolean printOptimal, boolean printScores, boolean printTrace, boolean caseSensitive)
    {
        System.out.printf("\nAlignment of '%s' and '%s' using the Needleman-Wunsch algorithm\n\n", s, t);
        System.out.printf("Cost parameters: match = %d, mismatch = %d, gap = %d\n\n", match, mismatch, gap);

        int m = s.length() + 1; //number of rows, s: 1st sequence
        int n = t.length() + 1; //number of columns, t: 2nd sequence

        int[][] nwArr = new int[m][n]; //empty m*n matrix for the needleman wunsch (nw) matrix

        for (int i=0; i<m; i++) nwArr[i][0] = i * gap; //fill first column of the nw matrix
        for (int i=0; i<n; i++) nwArr[0][i] = i * gap; //fill first row of the nw matrix

        //Editing operations: S = Stop, I = Insertion, D = Deletion, E : Replacement

        //matrix for the editing operations (needed for backtracing later)
        ArrayList<ArrayList<HashSet<Character>>> opArr = new ArrayList<ArrayList<HashSet<Character>>>(m);
        for (int i=0; i<m; i++)
        {
            opArr.add(new ArrayList<HashSet<Character>>(n));
            for (int j=0; j<n; j++) opArr.get(i).add(new HashSet<Character>());
        }

        opArr.get(0).get(0).add('S');
        for (int i=1; i<m; i++) opArr.get(i).get(0).add('D'); //fill first column
        for (int i=1; i<n; i++) opArr.get(0).get(i).add('I'); //fill first row

        if (!caseSensitive)
        {
            s = s.toUpperCase();
            t = t.toUpperCase();
        }

        for (int i=1; i<m; i++)
        {
            for (int j=1; j<n; j++)
            {
                //calculate the scores of each operation
                int scoreLeft = nwArr[i-1][j] + gap; //insertion cost
                int scoreUp = nwArr[i][j-1] + gap; //deletion cost
                int scoreDiag = nwArr[i-1][j-1] + ((s.charAt(i-1) == t.charAt(j-1)) ? match : mismatch);  //replacement cost
                
                HashMap<Character, Integer> possibleScores = new HashMap<Character, Integer>();
                possibleScores.put('D', scoreLeft); possibleScores.put('I', scoreUp); possibleScores.put('E', scoreDiag);
                ArrayList<Integer> values = new ArrayList<Integer>(possibleScores.values());
                Collections.sort(values);
                
                //get best score according to cost function (standard=min)
                int score = 0;
                if (costf.equals("min")) score = values.get(0);
                else if (costf.equals("max")) score = values.get(2);               

                nwArr[i][j] = score;

                //add the operation associated with the best score
                for (char op: possibleScores.keySet())
                {
                    if (possibleScores.get(op) == score) opArr.get(i).get(j).add(op);
                }
            }
        }

        if (printTrace)
        {
            ArrayList<String> optimalAlignments = traceBack(s, t, opArr, m-1, n-1, "", "", new ArrayList<String>());
            System.out.printf("Co-optimal alignments (%d total)): \n", optimalAlignments.size() / 2);

            for (int i=0; i<optimalAlignments.size(); i+=2)
            {
                String curr = optimalAlignments.get(i);
                System.out.println(new StringBuilder(curr).reverse().toString());
                curr = optimalAlignments.get(i+1);
                System.out.println(new StringBuilder(curr).reverse().toString());
                System.out.println();
            }

        }
        
        if (printMax) System.out.printf("Total number of possible alignments: A('%s', '%t'): %d\n", s, t, maxAlignments(s.length(), t.length()));

        if (printOptimal) System.out.printf("Optimal alignment score: %d\n\n", nwArr[m-1][n-1]);

        if (printScores)
        {
            System.out.println("Needleman-Wunsch scores matrix:\n");
            System.out.println("      " + String.join("  ", t.split("")));

            for (int i=0; i<m; i++)
            {
                if (i > 0) System.out.print(s.charAt(i-1) + " ");
                else System.out.print("  ");

                for (int j=0; j<n; j++)
                {
                    System.out.printf("%02d ", nwArr[i][j]);
                }
                System.out.println();
            }
            System.out.println("\n");
        }

    }

    public ArrayList<String> traceBack(String s, String t, ArrayList<ArrayList<HashSet<Character>>> opArr, int i, int j, String al1, String al2, ArrayList<String> alignments)
    {   
        //if the stop operation is reached, the traceback is completed ("E", "I", "D" will not be in opArr[i][j])
        if (i == 0 && j == 0) 
        {
            alignments.add(al1);
            alignments.add(al2);
        }

        //if replacement was done, go up diagonally
        if (opArr.get(i).get(j).contains('E')) traceBack(s, t, opArr, i-1, j-1, al1 + s.charAt(i-1), al2 + t.charAt(j-1), alignments);

        //if insertion was done, go to the left
        if (opArr.get(i).get(j).contains('I')) traceBack(s, t, opArr, i, j-1, al1 + "-", al2 + t.charAt(j-1), alignments);

        //if deletion was done, go up
        if (opArr.get(i).get(j).contains('D')) traceBack(s, t, opArr, i-1, j, al1 + s.charAt(i-1), al2 + "-", alignments);
        
        return alignments;
    }

    public static void main(String[] args) {
        List<String> tmpValidCommands = Arrays.asList("-matrix", "-total", "-trace", "-costf", "-optimal", "-m", "-mm", "-g", "-cs");
        List<String> tmpHelpCommands = Arrays.asList("--help", "-help", "-h");
        HashSet<String> validCommands = new HashSet<String>(tmpValidCommands);
        HashSet<String> helpCommands = new HashSet<String>(tmpHelpCommands);
        HashSet<String> allCommands = validCommands;
        allCommands.addAll(helpCommands);
        HashSet<String> cmdArgs = new HashSet<String>(Arrays.asList(args));

        CommandLineHelper helper = new CommandLineHelper("nw");

        boolean valid = helper.isValid(args, allCommands);

        if (args.length == 0  || !valid)
        {
            System.out.println("Usage: NeedlemanWunsch.class [OPTIONS] string1 string2\n");
            System.out.println("use --help, -help or -h to display usage help\n");
        }
        else if (args.length == 1 && helpCommands.contains(args[0])) helper.printHelp();

        else if (args.length >= 2 && valid)
        {
            String s = args[args.length-2];
            String t = args[args.length-1];

            int match = Integer.parseInt(helper.getArgsVal(args, "-m", "0"));
            int mismatch = Integer.parseInt(helper.getArgsVal(args, "-mm", "1"));
            int gap = Integer.parseInt(helper.getArgsVal(args, "-g", "1"));
            String costf = helper.getArgsVal(args, "-costf", "min");
            boolean printMax = cmdArgs.contains("-total") ? true : false;
            boolean printOptimal = cmdArgs.contains("-optimal") ? true : false;
            boolean printScores = cmdArgs.contains("-matrix") ? true : false;
            boolean printTrace = cmdArgs.contains("-trace") ? true : false;
            boolean caseSensitive = cmdArgs.contains("-cs") ? true : false;

            NeedlemanWunsch aligner = new NeedlemanWunsch(match, mismatch, gap, costf, printMax,
                                                          printTrace, printScores, printOptimal, caseSensitive);
            aligner.align(s, t);
        }
    }
}