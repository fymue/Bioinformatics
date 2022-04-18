package geneprediction;

import geneprediction.State;

public class HMM
{
    private static int maxStates = 100;
    private static int totalStates = 0;
    State[] states;
    double[][] transitionP;

    public HMM(State[] states, double[][] transitionP, int mxStates)
    {
        HMM.maxStates = mxStates;
        setStates(states);
        setTransitionProbabilites(transitionP);
        setInitialProbabilities();
    }

    public HMM(double[][] transitionP, int mxStates)
    {
        this(null, transitionP, mxStates);
    }

    public HMM(double[][] transitionP)
    {
        this(null, transitionP, maxStates);
    }

    public State[] getStates() {return states;}
    public double[][] getTransitionProbabilities() {return transitionP;}
    public void setTransitionProbabilites(double[][] transitionP) {this.transitionP = transitionP;}

    private void setStates(State[] states)
    {
        this.states = new State[maxStates];
        if (states != null)
        {
            HMM.totalStates = states.length;
            for (int i=0; i<states.length; i++) this.states[i] = states[i];
        }
    }

    private void setInitialProbabilities()
    {
        int c = 0;
        for (State s: this.states)
        {
            if (s.getInitialP() == 0.0) c++;
        }

        if (c == this.states.length)
        {
            for (State s: states) s.setInitialP(1 / states.length);
        }
    }

    public void addState(State state)
    {
        this.states[totalStates] = state;
        totalStates++; 
    }
}