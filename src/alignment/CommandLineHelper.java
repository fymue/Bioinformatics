package alignment;

import java.util.HashSet;

public class CommandLineHelper
{
    public void printHelp()
    {
        System.out.println("Usage: NeedlemanWunsch.class [OPTIONS] string1 string2\n");
        System.out.println("Options:\n");
        System.out.println("-total\t\t\t\tprint the maximum number of possible alignments");
        System.out.println("-matrix\t\t\t\tprint the needleman wunsch scores matrix");
        System.out.println("-trace\t\t\t\tsample (co-)optimal alignments");
        System.out.println("-costf 'min'|'max'\t\tuse minimum or maximum to calculate optimal score at each step");
        System.out.println("-optimal\t\t\tprint the optimal alignment score");
        System.out.println("-m value\t\t\tmatch value (default is 0)");
        System.out.println("-mm value\t\t\tmismatch value (default is 1)");
        System.out.println("-g value\t\t\tgap value (default is 1)\n");
    }

    public String getArgsVal(String[] args, String arg, String defVal)
    {
        for (int i=0; i<args.length; i++)
        {
            if (args[i].equals(arg))
            {
                return args[i+1];
            }
        }
        return defVal;
    }

    public boolean isValid(String[] args, HashSet<String> allCommands)
    {
        for (int i=1; i<args.length-2; i++)
        {
            if (args[i].charAt(0) != '-' || (int)args[i].charAt(1) <= 65) continue;
            if (!allCommands.contains(args[i])) return false;
        }
        return true;
    }
    
}
