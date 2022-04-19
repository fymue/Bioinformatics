package geneprediction;

import geneprediction.*;

public class GenePredictor
{
    /*gene predictor class
    uses a HMM to estimate coding sequences (cds) and non-coding sequences on a sample genetic sequence.
    requires input sequence(es) to calculate the HMM parameters*/
    
    private String sampleSeq;

    public GenePredictor(String trainingSeq, String sampleSeq)
    {
        this.sampleSeq = sampleSeq;
        HMM hmm = createHMM(trainingSeq);

    }

    public GenePredictor(String trainingSeq)
    {
        this(trainingSeq, "");
    }

    private double[][] calcEmissionProbabilities(String[] emissions, String trainingSeq)
    {
        double[][] emissionP = new double[2][emissions.length];
        return emissionP;
    }

    private double[][] calcTransitionProbabilities(String trainingSeq, int totalStates)
    {
        double[][] transitionP = new double[totalStates][totalStates];
        return transitionP;
    }

    private HMM createHMM(String trainingSeq)
    {
        String[] emissions = {"A", "T", "G", "C"};
        double[][] emissionP = calcEmissionProbabilities(emissions, trainingSeq);
        State[] states = {new State("C", emissions, emissionP[0], 0.0),
                          new State("N", emissions, emissionP[1], 1.0)};
        
        double[][] transitionP = calcTransitionProbabilities(trainingSeq, 2);

        return new HMM(states, transitionP);
    }

    public static void main(String[] args)
    {
        String trainingSeq = "";
        GenePredictor genepredictor = new GenePredictor(trainingSeq);
    }

}