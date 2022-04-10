package stringsearch;

import java.util.ArrayList;

import stringsearch.Node;

public class PatternFSM
{
    //build a finite state machine (FSM) based on pattern (string)
    //enables O(m) text searching after FSM construction (m: length of text)

    String text, pattern;
    Node[] patternFSM;

    //constructors
    //either provide text and pattern, just pattern or nothing (text and pattern should then be passed in search())

    public PatternFSM()
    {
        this("", "");
    }

    public PatternFSM(String p)
    {
        this("", p);

    }

    public PatternFSM(String t, String p)
    {
        this.text = t;
        this.pattern = p;
    }

    private Node[] buildPatternFSM(String p)
    {
        //build the FSM based on the pattern string

        int pLength = p.length(); //m
        int nNodes = pLength + 1;
        Node[] fsm = new Node[nNodes];
        
        //create m+1 nodes (states; last node is accepting end state)
        for (int i=0; i<nNodes; i++) fsm[i] = new Node();
        fsm[nNodes-1].isEndState = true;

        //go over all nodes and the entire alphabet (here: all ASCII characters)
        for (int i=0; i<nNodes; i++)
        {
            for (int j=0; j<256; j++)
            {
                /*if the current character of the alphabet
                equals the character of the pattern at the current node
                create a new edge labeled with c to the next node;
                if not, calculate the border of the pattern concatenated with the current character
                and create a new edge back to the node at position "border"*/

                char c = (char)j;
                if (i < pLength && c == p.charAt(i)) fsm[i].next.put(c, fsm[i+1]);
                else fsm[i].next.put(c, fsm[border(p+c)]);  
            }
        }
        return fsm;
    }

    private int border(String p)
    {
        //border function calculates the length of the longest prefix of p != p that is also a suffix of p
        //this tells us which node to go back to when constructing the FSM

        int pLength = p.length();
        
        for (int i=pLength-1; i>0; i--)
        {
            String prefix = p.substring(0, i);
            String suffix = p.substring(pLength-i);
            if (prefix.equals(suffix)) return i;
        }
        return 0;
    }

    //overloaded search functions to match the different constructors
    public ArrayList<Integer> search() {return search(this.text, this.pattern);}

    public ArrayList<Integer> search(String text) {return search(text, this.pattern);}

    public ArrayList<Integer> search(String text, String pattern)
    {
        //search the pattern in the text using the pattern FSM
        this.pattern = pattern;
        this.patternFSM = buildPatternFSM(pattern);

        int pLength = pattern.length();
        ArrayList<Integer> hits = new ArrayList<Integer>();
        Node currNode = this.patternFSM[0];

        for (int i=0; i<text.length(); i++)
        {
            /*Start at the start node and input all the characters of the text one after another.
            If we reach the only accepting end state, the pattern was part of the text.
            If so, add the position of the 1st character of the pattern in the text to the output array.*/

            Node nxtNode = currNode.next.get(text.charAt(i));
            if (nxtNode.isEndState) hits.add(i - pLength + 1);
            currNode = nxtNode;       
        }
        return hits;
    }
}