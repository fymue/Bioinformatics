package stringsearch;

import java.util.LinkedHashMap;

public class Node
{
    //Node class
    //only has "next" field -> contains edge(s) (characters) and node(s) they point to
    public LinkedHashMap<Character, Node> next;

    public Node()
    {
        this.next = new LinkedHashMap<Character, Node>();
    }
}