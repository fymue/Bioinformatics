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
    private char[] emissionsChar;
    private HashMap<String, Double> emissionP = new HashMap<String, Double>();

    public State(String name, String[] emissions, double[] emissionP, double initialP)
    {
        setName(name);
        setInitialP(initialP);
        setEmissions(emissions);
        setEmissionProbabilities(emissionP, emissions);
    }

    public State(String name, char[] emissions, double[] emissionP, double initialP)
    {
        setName(name);
        setInitialP(initialP);
        setEmissions(emissions);
        setEmissionProbabilities(emissionP, emissions);
    }

    public State(String name, String[] emissions, double[] emissionP)
    {
        this(name, emissions, emissionP, 0.0);
    }

    public State(String name, char[] emissions, double[] emissionP)
    {
        this(name, emissions, emissionP, 0.0);
    }

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public double getInitialP() {return initialP;}
    public void setInitialP(double initialP) {this.initialP = initialP;}

    public HashMap<String, Double> getEmissionProbalities() {return emissionP;}

    public void setEmissionProbabilities(double[] emissionP, String[] emissions)
    {
        for (int i=0; i<emissionP.length; i++) this.emissionP.put(emissions[i], emissionP[i]);
    }

    public void setEmissionProbabilities(double[] emissionP, char[] emissions)
    {
        for (int i=0; i<emissionP.length; i++) this.emissionP.put(Character.toString(emissions[i]), emissionP[i]);
    }

    public void setEmissions(String[] emissions) {this.emissions = emissions;}
    public void setEmissions(char[] emissions) {this.emissionsChar = emissions;}
}