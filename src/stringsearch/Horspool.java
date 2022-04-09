package stringsearch;

import java.util.*;

public class Horspool
{
    String text;

    public Horspool()
    {
        this("");
    }

    public Horspool(String text)
    {
        this.text = text;
    }

    public boolean match(String t, String p)
    {
        System.out.println(t);
        System.err.println(p);
        for (int i=0; i<t.length(); i++)
        {
            if (t.charAt(i) != p.charAt(i)) return false;
        }
        return true;
    }

    public HashMap<Character, Integer> makeSkipTable(String t, String p)
    {
        HashMap<Character, Integer> skip = new HashMap<Character, Integer>();
        int m = p.length();

        for (int i=0; i<256; i++)
        {
            char c = (char)i;
            skip.put(c, m);
        }
        for (int i=0; i<m-1; i++)
        {
            char c = p.charAt(i);
            skip.put(c, m-i-1);
        }

        return skip;
    }

    public ArrayList<Integer> search(String p)
    {
        return search(this.text, p);
    }

    public ArrayList<Integer> search(String t, String p)
    {
        int pos = 0;
        int n = t.length();
        int m = p.length();
        ArrayList<Integer> hits = new ArrayList<Integer>();
        HashMap<Character, Integer> skip = makeSkipTable(t, p);

        while (pos < n - m + 1)
        {
            String sub = t.substring(pos, pos+m);
            if (match(sub, p)) hits.add(pos);
            pos += skip.get(t.charAt(pos+m-1));
        }

        return hits;
    }

}