package fastagen;

/**
 * storage class used as datatype for TableView
 * in order to bundle a sequence header and the
 * actual Sequence object 
 */
public class Entry
{
    private String header;
    private int gcContent;
    private double molWeight, meltingTemp;

    public Entry(String header, Sequence seq)
    {
        this.header = header;
        this.gcContent = seq.getGCContent();
        this.meltingTemp = seq.getMeltingTemp();
        this.molWeight = seq.getMolWeight();
    }

    public int getGcContent() {
        return gcContent;
    }

    public String getHeader() {
        return header;
    }

    public double getMeltingTemp() {
        return meltingTemp;
    }

    public double getMolWeight() {
        return molWeight;
    }
}