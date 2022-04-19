package geneprediction;

import java.util.HashMap;

public class State
{
    //State class
    //used to build a HMM
    //has initial probability and emissions as well as emission probabilities
    
    private String name;
    private double initialP;
    private String[] emissions;
    private HashMap<String, Double> emissionP = new HashMap<String, Double>();

    public State(String name, String[] emissions, double[] emissionP, double initialP)
    {
        setName(name);
        setInitialP(initialP);
        setEmissions(emissions);
        setEmissionProbabilities(emissionP);
    }

    public State(String name, String[] emissions, double[] emissionP)
    {
        this(name, emissions, emissionP, 0.0);
    }

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public double getInitialP() {return initialP;}
    public void setInitialP(double initialP) {this.initialP = initialP;}

    public HashMap<String, Double> getEmissionProbalities() {return emissionP;}
    public void setEmissionProbabilities(double[] emissionP)
    {
        for (int i=0; i<emissionP.length; i++) this.emissionP.put(this.emissions[i], emissionP[i]);
    }

    public String[] getEmissions() {return emissions;}
    public void setEmissions(String[] emissions) {this.emissions = emissions;}
}