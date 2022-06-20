package fastagen;

import java.util.HashMap;
import org.junit.Test;
import org.junit.Before;
import org.junit.ComparisonFailure;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * This class merely serves as a storage class for all of the properties
 * that will be tested in the <code>SequenceTest</code> class
 */
class TestData
{
    String sequence;
    int gcContent;
    double molWeight, meltingTemp;

    TestData(String seq, int gc, double mW, double mT)
    {
        this.sequence = seq;
        this.gcContent = gc;
        this.molWeight = mW;
        this.meltingTemp = mT;
    }

    @Override
    public String toString()
    {
        /*
         * values calculated by the 'Oligonucleotide Properties Calculator' are given
         * to one exact decimal place, which is why all values are only returned to
         * one excact decimal place in the format string.
         */
        return String.format("GC: %d%%, molecular Weight: %.1f g/mol, melting Temperature: %.1f°C",
                             this.gcContent, this.molWeight, this.meltingTemp);
    }
}

/**
 * This class tests if the methods implemented in the <code>Sequence</code> class
 * are implemented correctly using the JUnit unittesting framework
 */
public class SequenceTest
{
    private HashMap<String, TestData> referenceTestResults;
    private HashMap<String, TestData> seqClassResults;
    private final char[] alphabet = {'A', 'C', 'G', 'T'};

    @Before //execute this before the actual test
    public void setUp()
    {
        this.referenceTestResults = getReferenceTestResults();
        this.seqClassResults = getSequenceClassResults();
    }

    private HashMap<String, TestData> getReferenceTestResults()
    {
        /* The data used in this method was (manually) calculated using the online tool
        *  'Oligonucleotide Properties Calculator' (http://biotools.nubic.northwestern.edu/OligoCalc.html)
        *  and is used to validate the correctness of the implementation of the Sequence class
        */

        HashMap<String, TestData> referenceTestResults = new HashMap<>();

        //some edge cases
        referenceTestResults.put("testSequence1", new TestData("", 0, 0.0, 0.0));
        referenceTestResults.put("testSequence2", new TestData("AAAAAAAAAAAAAA", 0, 4323.0, 16.9));
        referenceTestResults.put("testSequence3", new TestData("GGGGGGGGGGGGGG", 100, 4547.0, 57.9));
        referenceTestResults.put("testSequence9", new TestData("KLEKRLTKELRKLR", 0, 0.0, 0.0));
        referenceTestResults.put("testSequence10", new TestData("G", 100, 267.3, 4.0));
        
        //mixture on IUPAC and non-IUPAC characters -> not possible to calculate any results
        referenceTestResults.put("testSequence11", new TestData("GGGFHAFAHFAHF", 0, 0.0, 0.0));
        referenceTestResults.put("testSequence12", new TestData("JGDGGJDGJJOPT", 0, 0.0, 0.0));

        //some regular cases
        referenceTestResults.put("testSequence4", new TestData("ACGTACGTTGCATG", 50, 4278.8, 37.4));
        referenceTestResults.put("testSequence5", new TestData("CGGAGTAGCATCGC", 64, 4288.8, 43.2));
        referenceTestResults.put("testSequence6", new TestData("ACACGACATCGACC", 57, 4201.8, 40.3));
        referenceTestResults.put("testSequence7", new TestData("GCGCGACACGCATC", 71, 4233.8, 46.2));
        referenceTestResults.put("testSequence8", new TestData("ACTTTATCGATCGT", 36, 4228.8, 31.5));

        return referenceTestResults;
    }

    private HashMap<String, TestData> getSequenceClassResults()
    {
        HashMap<String, TestData> seqClassResults = new HashMap<>();
        Sequence tempSeqObj;

        //calculate GC content, molecular weight and melting temperature using the methods from the Sequence class
        for (String testSeq: this.referenceTestResults.keySet())
        {
            String currSeq = this.referenceTestResults.get(testSeq).sequence;
            tempSeqObj = new Sequence(currSeq, alphabet); //use the Sequence class to calculate the sequence properties
            seqClassResults.put(testSeq, new TestData(currSeq, tempSeqObj.getGCContent(), 
                                                      tempSeqObj.getMolWeight(), 
                                                      tempSeqObj.getMeltingTemp()));
        }

        return seqClassResults;
    }

    /**
     * JUnit unittest:
     * compares the calculated value for GC content, molecular weight and melting temperature
     * by the Online tool (for reference/validation) and the implemented method in the
     * <code>Sequence</code> class
     */
    @Test
    public void compareResults()
    {
        /* compare the manually calculated results for the sample sequences
        *  with the results calculated using the appropriate Sequence class methods
        *  (all results should be exactly the same)
        */

        System.out.print("Testing Sequence class methods for correctness...\n");
        
        int c  = 0;
        int nTests = this.referenceTestResults.size();

        try
        {
            for (String seqID: this.referenceTestResults.keySet())
            {   
                c++;
                TestData testDataResults = this.referenceTestResults.get(seqID);
                TestData seqClassResults = this.seqClassResults.get(seqID);
                /*
                * The toString() method of the TestData class returns a string containing
                * all of the relevant test values (GC content, molecular weight, melting temperature).
                * The toString() method of the Sequence class returns a string
                * that looks exactly the same (syntactically).
                * Comparing the strings returned by the toString() methods of the manually added TestData objects
                * and the calculated data of the Sequence class is thus equivalent
                * to comparing each test value individually.
                */
                assertEquals(testDataResults.toString(), seqClassResults.toString());

                //individual value testing just to be safe (no delta, only exactly the same values are accepted)
                assertEquals(testDataResults.gcContent, seqClassResults.gcContent, 0.0);
                assertEquals(testDataResults.meltingTemp, seqClassResults.meltingTemp, 0.0);
                assertEquals(testDataResults.molWeight, seqClassResults.molWeight, 0.0);
            }
        }
        catch (ComparisonFailure e)
        {
            System.out.println(e);
        }
        finally {System.out.printf("\nDone! %d/%d tests ran successfully, %d tests failed!\n", c, nTests, nTests-c);}
    }
    
    
}
