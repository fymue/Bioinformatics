package stringsearch;

import stringsearch.Node;

public class Trie
{
    public String text;
    public Node trie;
    private int start;

    public Trie(String text)
    {
        this.text = text;
        this.trie = this.constructTrie(text);
    }

    public Node constructTrie(String text)
    {
        Node trie = new Node();
        int lngth = text.length();

        for (int s=0; s<lngth; s++)
        {
            String suffix = text.substring(s, lngth);

            Node currNode = this.findBranchStart(trie, suffix);

            if (this.start == -1) continue;

            for (int i=this.start; i<suffix.length(); i++)
            {
                char c = suffix.charAt(i);

                currNode.next.put(c, new Node());
                currNode = currNode.next.get(c);
            }
        }
        
        return trie;
    }

    public Node findBranchStart(Node trie, String suffix)
    {
        Node currNode = trie;
        this.start = 0;
        
        for (int i=0; i<suffix.length(); i++)
        {
            char c = suffix.charAt(i);

            if (currNode.next.containsKey(c)) currNode = currNode.next.get(c);
            else return currNode;
            
            this.start++;
        }

        this.start--;

        if (this.start == suffix.length() - 1) this.start = -1;

        return currNode;
    }

    public boolean search(String pattern)
    {
        this.findBranchStart(this.trie, pattern);
        return  this.start == -1;
    }

   

}
