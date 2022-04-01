class Node:
    #Node class
    #only has "next" field -> contains edge(s) (characters) and node(s) they point to

    def __init__(self):
        self.next = {}
    
    def __str__(self): return str(self.next)

class Trie:
    #Trie class
    #constructs a suffix trie (like a tree) based on a input string/text
    #has a start node, which functions as the root
    #enables O(m) search of a pattern in the provided text (m: length of pattern)

    def __init__(self, text):
        self.text = text
        self.trie = self.construct_trie(text)

    def construct_trie(self, text):
        #construct the suffix trie

        trie = Node() #initialize the trie w/ a start node
        curr_node = trie #store a reference to the current node
        
        for s in range(len(text)): #iterate over all suffixes of the text
            suffix = text[s:]

            #walk along existing tree branches
            #check if the current suffix can be added to an existing branch

            start, curr_node = self.find_branch_start(trie, suffix)

            if start == -1: continue #if suffix start lookup reached the end of a branch, skip the suffix

            for i in range(start, len(suffix)): #iterate over all characters of the current suffix
                c = suffix[i]

                #add an edge w/ the current character as a "weight/"value" to the current (parent) node
                #this edge points to a new (child) node 
                curr_node.next[c] = Node() 
                curr_node = curr_node.next[c] #update the current node reference
            
        return trie
    
    def find_branch_start(self, trie, suffix):
        #walk along existing tree branches
        #check if the current suffix can be added to an existing branch
        #if we reach the end of a branch, this suffix already completely exists in the tree
        #this means this function can also be used for pattern searching later

        curr_node = trie

        for i, c in enumerate(suffix):
            #check if the current character is the "weight" of an edge pointing to a child node

            if c in curr_node.next:
                curr_node = curr_node.next[c]
            else:
                return i, curr_node
        
        if i == len(suffix) - 1: i = -1
    
        return i, curr_node

    def search(self, pattern):
        #uses the find_branch_start() function to check if a pattern is already part of the tree
        #if we run this search after the whole suffix trie has been constructed,
        #we know if the pattern exists in the text or not
        
        return True if self.find_branch_start(self.trie, pattern)[0] == -1 else False


test_patterns = ["sample", "sas", "ample", "aample", "sasample", "ee", "lle"]
control_res = [True, True, True, False, True, False, False]
text = "sasample"

def test(text, patterns, control_res):
    trie = Trie(text)

    for test, comp in zip(patterns, control_res):
        print(f"Test word '{test}' in text: {trie.search(test)} (Algorithm result), {comp} (Real reference)")

if __name__ == "__main__": test(text, test_patterns, control_res)


"""
node = trie.trie.next
for c in node:
    node = node[c].next
    print(c, end=" ")
    while node:
        for c in node:
            print(c, end=" ")
            node = node[c].next
    print()
    node = trie.trie.next

#trie.print_trie()
"""

