package geneprediction;

import geneprediction.State;
import geneprediction.HMM;

public class Viterbi
{
    //Python implementation of the viterbi algorithm
    //calculates the most likely (hidden) state path
    //based on a hidden markov model (HMM)

    private State[] states;
    private String[] observedEmissions;
    private double[][] transitionP;

    public Viterbi(HMM hmm, String[] observedEmissions)
    {
        this.states = hmm.getStates();
        this.observedEmissions = observedEmissions;
        this.transitionP = hmm.getTransitionProbabilities();
    }

    public Viterbi(State[] states, double[][] transitionP, String[] observedEmissions)
    {
        this.states = states;
        this.observedEmissions = observedEmissions;
        this.transitionP = transitionP;
    }

    public Viterbi(HMM hmm)
    {
        this(hmm, null);
    }

    private int argMax(double[][] viterbiMatrix, String[] observedEmissions, int currObs, int currState, int totalStates)
    {
        double[] values = new double[totalStates];

        for (int i=0; i<totalStates; i++)
        {
            values[i] = viterbiMatrix[i][currObs] * this.transitionP[i][currState] * this.states[currState].getEmissionProbalities().get(observedEmissions[currObs]);
        }

        double mxValue = 0.0;
        int mxI = 0;
        
        for (int i=0; i<values.length; i++)
        {
            if (values[i] > mxValue)
            {
                mxValue = values[i];
                mxI = i;
            }
        }
        
        return mxI;
    }

    public String[] calcBestStatePath() {return calcBestStatePath(this.observedEmissions);}

    public String[] calcBestStatePath(String[] observedEmissions)
    {
        int totalStates = this.states.length;
        int totalObservations = observedEmissions.length;
        String[] bestStatePath = new String[totalObservations];
        int k = 0;

        //matrix to store the best path probabilities
        double[][] viterbiMatrix = new double[totalStates][totalObservations];

        //matrix to store the indices of the states associated with the current best path probability (used in backtracing later)
        int[][] stateMatrix = new int[totalStates][totalObservations];   

        //initialize the first column of the matrix (==start of the state path)
        for (int s=0; s<totalStates; s++) viterbiMatrix[s][0] = states[s].getInitialP() * states[s].getEmissionProbalities().get(observedEmissions[0]); 

        //calculate the probability of each state path (refer to the recursive definition of the algorithm for details)
        for (int o=1; o<totalObservations; o++)
        {
            for(int s=0; s<totalStates; s++)
            {
                k = argMax(viterbiMatrix, observedEmissions, o, s, totalStates); 
                viterbiMatrix[s][o] = viterbiMatrix[k][o-1] * this.transitionP[k][s] * states[s].getEmissionProbalities().get(observedEmissions[o]);
                stateMatrix[s][o] = k;
            }
        }

        //find the start state for the backtracing (will be the last state of the best state path at the end)
        double mxValue = 0.0;
        for (int i=0; i<totalStates; i++)
        {
            if (viterbiMatrix[i][totalObservations-1] > mxValue)
            {
                mxValue = viterbiMatrix[i][totalObservations-1];
                k = i;
            }
        }

        //find the best state path using the state/pointer matrix built earlier (backtracing)
        for (int o=totalObservations-1; o>-1; o--)
        {
            bestStatePath[totalObservations-o-1] = this.states[k].getName();
            k = stateMatrix[k][o];
        }

        return bestStatePath;
    }

}