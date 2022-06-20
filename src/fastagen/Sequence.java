package fastagen;

import java.util.HashMap;

/**
 * This class holds and calculates relevant information or properties of a nucleotide sequence
 */
public class Sequence
{
    protected char[] alphabet;
    protected boolean isGenomic;
    private String sequence;
    private int gcContent;
    private double molWeight, meltingTemp;
    private HashMap<Character, Double> baseCount;


    public Sequence(String sequence, char[] alphabet)
    {
        this.setSequence(sequence);
        this.alphabet = alphabet;
        this.isGenomic = this.isGenomic();

        if (this.isGenomic) //only run these calculations on genomic sequences
        {
            this.baseCount = this.getBaseCount();
            this.gcContent = this.calcGCContent();
            this.molWeight = this.calcMolWeight();
            this.meltingTemp = this.calcMeltingTemp();
        }
    }

    /**
     * getter method for the nucletoide sequence
     * @return the nucleotide sequence as a String
     */
    public String getSequence()
    {
        return this.sequence;
    }

    /**
     * getter method for the nucletoide sequence's length
     * @return the length of the nucleotide sequence
     */
    public int getLength()
    {
        return this.sequence.length();
    }

    /**
     * setter method for the nucleotide sequence
     * @param sequence the nucleotide sequence as a String
     */
    public void setSequence(String sequence)
    {
        this.sequence = sequence.toUpperCase();
    }

    /*
     * determines whether or not the sequence of this object
     * is a genomic/nucleotide sequence or not
     */
    private boolean isGenomic()
    {
        return (this.sequence.matches("[ACGT]+")) ? true : false;
    }

    private double round(double value, int places)
    {
        /*
        *  round function with decimal places parameter
        */

        double divisor = Math.pow(10.0, (double)places);
        value *= divisor;
        double fraction = value - Math.floor(value);

        return Math.round(value) / divisor;
    }

    private HashMap<Character, Double> getBaseCount()
    {
        //count how often every base occurs in the sequence

        HashMap<Character, Double> baseCount = new HashMap<>();

        for (char base: this.alphabet) baseCount.put(base, 0.0);

        for (int c=0; c<this.getLength(); c++)
        {
            char currBase = this.sequence.charAt(c);
            baseCount.put(currBase, baseCount.get(currBase) + 1.0);
        }
        
        return baseCount;
    }

    /**
     * returns the GC content of <code>sequence</code> in %
     * if <code>sequence</code> is a nucleotide sequence.
     * If not, 0 will be returned.
     * @return the GC content of <code>sequence</code> in %
     */
    public int getGCContent() {return this.gcContent;}

    private int calcGCContent()
    {
        return (int)round((this.baseCount.get('G') + this.baseCount.get('C')) / 
               (this.baseCount.get('G') + this.baseCount.get('C') + 
                this.baseCount.get('A') + this.baseCount.get('T')) * 100.0, 0);
    }

    /**
     * returns the molecular weight of <code>sequence</code>
     * that was calculated using the formula of the 
     * Oligonucleotide Properties Calculator if <code>sequence</code> 
     * is a nucleotide sequence. If not, 0.0 will be returned.
     * @return the molecular weight of <code>sequence</code> in g/mol
     */
    public double getMolWeight() {return this.molWeight;}

    private double calcMolWeight()
    {
        return round(this.baseCount.get('A') * 313.21 + this.baseCount.get('T') * 304.2 +
                     this.baseCount.get('C') * 289.18 + this.baseCount.get('G') * 329.21 - 61.96, 1);
    }

    /**
     * returns the standard melting temperature of <code>sequence</code>
     * that was calculated using the formula of the 
     * Oligonucleotide Properties Calculator if <code>sequence</code>
     * is a nucleotide sequence. If not, 0.0 will be returned.
     * @return the melting temperature of <code>sequence</code> in °C
     */
    public double getMeltingTemp() {return this.meltingTemp;}

    private double calcMeltingTemp()
    {
        return (this.getLength() < 14) ? round((this.baseCount.get('A') + this.baseCount.get('T')) * 2 +
                                               (this.baseCount.get('G') + this.baseCount.get('C')) * 4, 1) :
                                         round(64.9 + 41 * (this.baseCount.get('G') + this.baseCount.get('C') - 16.4) /
                                               (this.baseCount.get('G') + this.baseCount.get('C') + this.baseCount.get('A') + 
                                                this.baseCount.get('T')), 1);
    }

    @Override
    public String toString()
    {
        return String.format("GC Content: %d%%, Molecular Weight: %.1f g/mol, Melting Temperature: %.1f°C\n",
                             this.gcContent, this.molWeight, this.meltingTemp);
    }
}
