package stringsearch;

import stringsearch.Node;
import java.util.ArrayList;
import java.util.Arrays;

public class Trie
{
    //Trie class
    //constructs a trie (like a tree) based on a input string/text
    //has a start node, which functions as the root
    //enables O(m) search of a pattern in the provided text (m: length of pattern)

    public String text;
    public Node trie;
    private int start;

    public Trie(String text)
    {
        this.text = text;
        this.trie = constructTrie(text);
    }

    public Node constructTrie(String text)
    {
        //construct the trie

        Node trie = new Node(); //initialize the trie w/ a start node
        int lngth = text.length(); //store a reference to the current node

        for (int s=0; s<lngth; s++) //iterate over all suffixes of the text
        {
            String suffix = text.substring(s, lngth);
            
            //walk along existing tree branches
            //check if the current suffix can be added to an existing branch

            Node currNode = findBranchStart(trie, suffix);

            if (this.start == -1) continue; //if suffix start lookup reached the end of a branch, skip the suffix

            for (int i=this.start; i<suffix.length(); i++) //iterate over all characters of the current suffix
            {
                char c = suffix.charAt(i);

                //add an edge w/ the current character as a "weight/"value" to the current (parent) node
                //this edge points to a new (child) node 

                currNode.next.put(c, new Node());
                currNode = currNode.next.get(c); //update the current node reference
            }
        }
        
        return trie;
    }

    public Node findBranchStart(Node trie, String suffix)
    {
        //walk along existing tree branches
        //check if the current suffix can be added to an existing branch
        //if we reach the end of a branch, this suffix already completely exists in the tree
        //this means this function can also be used for pattern searching later

        Node currNode = trie;
        this.start = 0;
        
        for (int i=0; i<suffix.length(); i++)
        {
            //check if the current character is the "weight" of an edge pointing to a child node

            char c = suffix.charAt(i);
            this.start = i;

            if (currNode.next.containsKey(c)) currNode = currNode.next.get(c);
            else return currNode;
        }

        if (this.start == suffix.length() - 1) this.start = -1;

        return currNode;
    }

    public boolean search(String pattern)
    {
        //uses the find_branch_start() function to check if a pattern is already part of the tree
        //if we run this search after the whole trie has been constructed,
        //we know if the pattern exists in the text or not
        
        findBranchStart(this.trie, pattern);
        return  this.start == -1;
    }

    public ArrayList<String> reconstructBranches()
        {
            //overloaded function to initialize the values
            return reconstructBranches(this.trie, "", new ArrayList<String>());
        }

    public ArrayList<String> reconstructBranches(Node currNode, String branch, ArrayList<String> branches)
    {
        //recursively reconstruct the branches of the tree

        if (currNode.next.isEmpty())
        {
            //if a node has no children, the current branch ends and the recursion is stopped
            //(since the for loop will have nothing to iterate over)
            branches.add(branch);
        }

        for (char c: currNode.next.keySet()) reconstructBranches(currNode.next.get(c), branch + c, branches);

        return branches;
    }

    private String adjustBranchPrint(String branch, String prev)
    {   
        //helper function to print to current branch correctly
        int c = 0;
        for (int i=0; i<prev.length(); i++)
        {
            c = i;
            if (prev.charAt(i) != branch.charAt(i)) break;
        }

        String space = "";
        for (int i=0; i<c*2; i++) space += " ";
        
        return space + "\u2514" + String.join("\u00B7", branch.substring(c).split("")) + "\u00B7";
    }

    public void printTrie()
    {
        //print the trie like a finite-state machine (FSM)
        //dots are states
        //characters are transitions
        //since its modeling a trie and we want to accept every substring of the input text,
        //every state is a final state

        ArrayList<String> branches = reconstructBranches();
        String prev = branches.get(0);

        System.out.printf("\u00B7\n\u2514" + String.join("\u00B7", prev.split("")) + "\u00B7\n");

        for (int i=1; i<branches.size(); i++)
        {
            String branch = adjustBranchPrint(branches.get(i), prev);
            System.out.println(branch);
            prev = branches.get(i);
        }

        System.out.println();
    } 

}
