package geneprediction;

import geneprediction.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class GenePredictor
{
    /*gene predictor class
    uses a HMM to estimate coding sequences (cds) and non-coding sequences on a sample genetic sequence.
    requires input sequence(es) to calculate the HMM parameters*/
    
    private String sampleSeqFile;
    private String trainingSeqFile;
    private String sampleSeq;
    private String cdsSeq;
    private HMM hmm;

    public GenePredictor(String trainingSeqFile, String sampleSeqFile)
    {
        this.sampleSeqFile = sampleSeqFile;
        this.trainingSeqFile = trainingSeqFile;

    }

    public GenePredictor(String trainingSeq)
    {
        this(trainingSeq, "");
    }

    private double[][] calcEmissionProbabilities(HashMap<Character, Integer> emissions, String trainingSeq, String cdsSeq)
    {
        double[][] emissionP = new double[2][emissions.size()];
        int totalCoding = 0;
        int totalNonCoding = 0;

        for (int i=0; i<trainingSeq.length(); i++)
        {
            char c = trainingSeq.charAt(i);
            if (cdsSeq.charAt(i) == 'C')
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
        double[][] transitionP = new double[2][2];
        double transitionStateProb = totalGenes / trainingSeq.length();
        double sameStateProb = 1 - transitionStateProb;

        transitionP[0][0] = sameStateProb;
        transitionP[0][1] = transitionStateProb;
        transitionP[1][0] = transitionStateProb;
        transitionP[1][1] = sameStateProb;

        return transitionP;
    }

    private HMM createHMM(String trainingSeq, String cdsSeq)
    {
        char[] emissions = {'A', 'T', 'G', 'C'};
        HashMap<Character, Integer> emissionsLookup = new HashMap<Character, Integer>();

        for (int i=0; i<emissions.length; i++) emissionsLookup.put(emissions[i], i);

        double[][] emissionP = calcEmissionProbabilities(emissionsLookup, trainingSeq, cdsSeq);

        State[] states = {new State("C", emissions, emissionP[0], 0.0),
                          new State("N", emissions, emissionP[1], 1.0)};
        
        int totalGenes = 0;
        double[][] transitionP = calcTransitionProbabilities(trainingSeq, totalGenes);

        return new HMM(states, transitionP);
    }

    private String getObservedEmissions(String sampleSeq)
    {
        String seqName = "";
        String seq = "";

        try
        {
            Scanner fin = new Scanner(new File(sampleSeq));
            
            while (fin.hasNextLine())
            {
                String l = fin.nextLine();
                if (l.isBlank()) continue;
                l = l.strip();
                if (l.charAt(0) == '>')
                {
                    seqName = l.substring(1);
                    continue;
                }
                seq += l;
            }

            fin.close();
        }
        catch (FileNotFoundException ex)
        {
            System.out.println("The file could not be found!");
        }
        
        return seq;
    }

    public String predictCDS() {return predictCDS(this.sampleSeq);}

    public String predictCDS(String sampleSeq)
    {
        Viterbi viterbi = new Viterbi(this.hmm);
        String observedEmissions = getObservedEmissions(sampleSeq);
        return viterbi.calcBestStatePath(observedEmissions);
    }

    public static void main(String[] args)
    {
        String trainingSeq = "";
        String sampleSeq = "";

        GenePredictor genepredictor = new GenePredictor(trainingSeq);
        System.out.println(genepredictor.predictCDS(sampleSeq));
    }
}