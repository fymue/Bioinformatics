package stringsearch;

import java.util.HashMap;

public class Node
{
    public HashMap<Character, Node> next;

    public Node()
    {
        this.next = new HashMap<Character, Node>();
    }
}