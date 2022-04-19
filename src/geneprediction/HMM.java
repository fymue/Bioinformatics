package geneprediction;

import geneprediction.State;

public class HMM
{
    /*Hidden Markov Model (HMM) class
    consists of State objects
    probabilities to switch to different states
    must be provided in a nxn transition matrix (n: number of states)*/

    private int maxStates;
    private int totalStates = 0;
    State[] states;
    double[][] transitionP;

    public HMM(State[] states, double[][] transitionP, int maxStates)
    {
        setMaxStates(maxStates);
        setStates(states);
        setTransitionProbabilites(transitionP);
        setInitialProbabilities();
    }

    public HMM(State[] states, double[][] transitionP)
    {

        this(states, transitionP, 100);
    }

    public HMM(double[][] transitionP, int maxStates)
    {
        this(null, transitionP, maxStates);
    }

    public HMM(double[][] transitionP)
    {
        this(null, transitionP, 100);
    }

    public State[] getStates() {return states;}
    public double[][] getTransitionProbabilities() {return transitionP;}
    public void setTransitionProbabilites(double[][] transitionP) {this.transitionP = transitionP;}

    private void setStates(State[] states)
    {
        this.states = new State[maxStates];
        if (states != null)
        {
            totalStates = states.length;
            for (int i=0; i<states.length; i++) this.states[i] = states[i];
        }
    }

    public void setMaxStates(int maxStates) {this.maxStates = maxStates;}
    public int getMaxStates() {return maxStates;}
    public int getTotalStates() {return totalStates;}

    private void setInitialProbabilities()
    {
        /*if not all initital state probabilities are provided,
        assume that each state has the same initital probability*/
    
        int c = 0;
        for (State s: states)
        {
            if (s != null && s.getInitialP() == 0.0) c++;
        }

        if (c == totalStates)
        {
            for (State s: states)
            {
                if (s != null) s.setInitialP(1.0 / totalStates);
            }
        }
    }

    public void addState(State state)
    {
        this.states[totalStates] = state;
        totalStates++; 
    }
}