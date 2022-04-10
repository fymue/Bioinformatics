package stringsearch;

import java.util.ArrayList;

import stringsearch.Node;

public class PatternFSM
{
    String text, pattern;
    Node[] patternFSM;

    public PatternFSM(String t, String p)
    {
        this.text = t;
        this.pattern = p;
        this.patternFSM = buildPatternFSM(p);
    }

    private Node[] buildPatternFSM(String p)
    {
        int pLength = p.length();
        int nNodes = pLength + 1;
        Node[] fsm = new Node[nNodes];
        
        for (int i=0; i<nNodes; i++) fsm[i] = new Node();
        fsm[nNodes-2].isEndState = true;

        for (int i=0; i<nNodes; i++)
        {
            for (int j=0; j<256; j++)
            {
                char c = (char)j;
                if (i < pLength && c == p.charAt(i)) fsm[i].next.put(c, fsm[i+1]);
                else fsm[i].next.put(c, fsm[border(p+c)]);  
            }
        }

        return fsm;
    }

    private int border(String p)
    {
        int pLength = p.length();
        
        for (int i=pLength-1; i>0; i--)
        {
            String prefix = p.substring(0, i);
            String suffix = p.substring(pLength-i);
            if (prefix.equals(suffix)) return i;
        }
        return 0;
    }

    public ArrayList<Integer> search() {return search(this.text);}

    public ArrayList<Integer> search(String text)
    {
        int pLength = this.pattern.length();
        ArrayList<Integer> hits = new ArrayList<Integer>();
        Node currNode = this.patternFSM[0];

        for (int i=0; i<text.length(); i++)
        {
            if (currNode.isEndState) hits.add(i - pLength + 1);
            currNode = currNode.next.get(text.charAt(i));       
        }
        return hits;
    }


    public static void main(String[] args) {
        PatternFSM fsm = new PatternFSM("sample text", "sample");
        System.out.println(fsm.search());
    }
}