package geneprediction;

import geneprediction.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.BitSet;

public class GenePredictor
{
    /*gene predictor class
    uses a HMM to estimate coding sequences (cds) and non-coding sequences on a sample genetic sequence.
    requires input sequence(s) to calculate the HMM parameters*/
    
    private String sampleSeq;
    private String trainingSeq;
    private BitSet cdsSeq;
    private HMM hmm;
    private int totalGenes;

    public GenePredictor(String trainingOrgId, String sampleOrgId, String saveDir)
    {   
        SeqDataParser dataParser = new SeqDataParser(trainingOrgId, sampleOrgId, saveDir);
        this.sampleSeq = dataParser.sampleSeq;
        this.hmm = createHMM(dataParser.trainingSeq, dataParser.cdsSeq, dataParser.totalGenes);

    }

    private double[][] calcEmissionProbabilities(HashMap<Character, Integer> emissions, String trainingSeq, BitSet cdsSeq)
    {
        //calculate the emission probabilites of every state of the HMM

        double[][] emissionP = new double[2][4];
        int totalCoding = 0;
        int totalNonCoding = 0;

        for (int i=0; i<trainingSeq.length(); i++)
        {
            char c = trainingSeq.charAt(i);

            if (cdsSeq.get(i)) //check if bit at current index is set to "true" (meaning its part of a cds)
            {
                totalCoding++;
                emissionP[0][emissions.get(c)]++;
            }
            else
            {
                totalNonCoding++;
                emissionP[1][emissions.get(c)]++;
            }
        }

        for (int i=0; i<emissionP[0].length; i++) emissionP[0][i] /= totalCoding;
        for (int i=0; i<emissionP[1].length; i++) emissionP[1][i] /= totalNonCoding;

        return emissionP;
    }

    private double[][] calcTransitionProbabilities(String trainingSeq, int totalGenes)
    {
        //calculate the transition probabilites between every state of the HMM
        
        double[][] transitionP = new double[2][2];
        double transitionStateProb = (double)totalGenes / trainingSeq.length();
        double sameStateProb = 1 - transitionStateProb;

        transitionP[0][0] = sameStateProb;
        transitionP[0][1] = transitionStateProb;
        transitionP[1][0] = transitionStateProb;
        transitionP[1][1] = sameStateProb;

        return transitionP;
    }

    private HMM createHMM(String trainingSeq, BitSet cdsSeq, int totalGenes)
    {
        //create the HMM

        char[] emissions = {'A', 'T', 'G', 'C'};
        HashMap<Character, Integer> emissionsLookup = new HashMap<Character, Integer>();

        for (int i=0; i<emissions.length; i++) emissionsLookup.put(emissions[i], i);

        double[][] emissionP = calcEmissionProbabilities(emissionsLookup, trainingSeq, cdsSeq);

        State[] states = {new State("C", emissions, emissionP[0], 0.0),
                          new State("N", emissions, emissionP[1], 1.0)};
        
        double[][] transitionP = calcTransitionProbabilities(trainingSeq, totalGenes);
        
        return new HMM(states, transitionP);
    }

    public String predictCDS() {return predictCDS(this.sampleSeq);}

    public String predictCDS(String sampleSeq)
    {
        //run the Viterbi algorithm using the sample sequence as observed emissions
        return new Viterbi(this.hmm).calcBestStatePath(sampleSeq);
    }

    public static void main(String[] args)
    {
        String trainingOrgId = "GCF_000005845.2_ASM584v2"; //ncbi refseq ID + "_" + strain name
        String sampleOrgId = "GCF_000008865.2_ASM886v2";
        String saveDir = "/home/fymue100/tmp/";

        GenePredictor genepredictor = new GenePredictor(trainingOrgId, sampleOrgId, saveDir);
        String output = genepredictor.predictCDS();
    }
}