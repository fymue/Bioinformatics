#collection of fast string searching algorithms 

class Node:
    #Node class
    #only has "next" field -> contains edge(s) (characters) and node(s) they point to

    def __init__(self):
        self.is_end_state = False
        self.next = {}
    
    def __str__(self): return str(self.next)

class Trie:
    #Trie class
    #constructs a trie (like a tree) based on a input string/text
    #has a start node, which functions as the root
    #enables O(m) search of a pattern in the provided text (m: length of pattern)

    def __init__(self, text):
        self.text = text
        self.trie = self.construct_trie(text)

    def construct_trie(self, text):
        #construct the trie

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
        #if we run this search after the whole trie has been constructed,
        #we know if the pattern exists in the text or not
        
        return True if self.find_branch_start(self.trie, pattern)[0] == -1 else False
    
    def reconstruct_branches(self, curr_node, branch="", branches=[]):
        #recursively reconstruct the branches of the tree

        if not curr_node.next:
            #if a node has no children, the current branch ends and the recursion is stopped
            branches.append(branch)
            return

        for c in curr_node.next:
            self.reconstruct_branches(curr_node.next[c], branch + "" + c)
        
        return branches

    def print_trie(self):
        #print the trie like a finite-state machine (FSM)
        #dots are states
        #characters are transitions
        #since its modeling a trie and we want to accept every substring of the input text,
        #every state is a final state

        def adjust_print(branch, prev):
            for i, c in enumerate(prev):
                if c != branch[i]: break
            
            return " " * i * 2 + "\u2514" + "\u00B7".join(branch[i:]) + "\u00B7"

        branches = self.reconstruct_branches(self.trie)
        prev = branches[0]

        print(f"\u00B7\n\u2514" + "\u00B7".join(branches[0]) + "\u00B7")

        for i in range(1, len(branches)):
            branch = adjust_print(branches[i], prev)
            print(branch)
            prev = branches[i]
        
        print()

class PatternFSM:
    #build a finite state machine (FSM) based on pattern (string)
    #enables O(m) text searching after FSM construction (m: length of text)

    def __init__(self, p=""):
        self.p = p
    
    def border(self, p):
        #border function calculates the length of the longest prefix of p != p that is also a suffix of p
        #this tells us which node to go back to when constructing the FSM

        p_length = len(p)

        for i in range(p_length-1, -1, -1):
            prefix = p[:i]
            suffix = p[p_length-i:]
            
            if prefix == suffix: return i
        
        return 0

    def buildPatternFSM(self, p):
        #build the FSM based on the pattern string

        p_length = len(p)
        n_nodes = p_length + 1

        #create m+1 nodes (states; last node is accepting end state)
        fsm = [Node() for _ in range(n_nodes)]
        fsm[n_nodes-1].is_end_state = True

        #go over all nodes and the entire alphabet (here: all ASCII characters)
        for i in range(n_nodes):
            for j in range(256):
                #if the current character of the alphabet
                #equals the character of the pattern at the current node
                #create a new edge labeled with c to the next node;
                #if not, calculate the border of the pattern concatenated with the current character
                #and create a new edge back to the node at position "border"

                c = chr(j)
                if i < p_length and p[i] == c: fsm[i].next[c] = fsm[i+1]
                else: fsm[i].next[c] = fsm[self.border(p+c)]
        
        return fsm

    def search(self, t, p=""):
        #search the pattern in the text using the pattern FSM

        if not p: p = self.p
        
        self.patternFSM = self.buildPatternFSM(p)
        curr_node  = self.patternFSM[0]

        p_length = len(p)
        hits = []

        for i, c in enumerate(t):
            #Start at the start node and input all the characters of the text one after another.
            #If we reach the only accepting end state, the pattern was part of the text.
            #If so, add the position of the 1st character of the pattern in the text to the output array.

            nxt_node = curr_node.next[c]
            if nxt_node.is_end_state: hits.append(i - p_length + 1)
            curr_node = nxt_node

        return hits

class SuffixArray:
    #make a suffix array from a given text
    #enables fast searching if and how often a pattern exists in the text

    def __init__(self, text):
        self.text = text
        self.suffix_array = self.make_suffix_array(text)

    def make_suffix_array(self, t):
        #create the suffix array
        return sorted(list(range(len(t))), key= lambda i: t[i:])

    def search(self, pattern):
        #search if and how often a pattern exists in the text using binary search
        #all suffices containing the pattern will be grouped together in the suffix array
        #the first binary search finds the index of the first suffix in the suffix array containing the pattern
        #the second binary search finds the index + 1 of the last suffix in the suffix array containing the pattern

        #returns a list w/ the starting index/indices of the pattern(s) found in the text
        #if the text doesn't contain the pattern, an empty list will be returned

        l = 0
        n = len(self.text)
        p = len(pattern)
        r = n

        #first binary search
        while l < r: 
            mid = (l + r) // 2
            if pattern > self.text[self.suffix_array[mid]:]:
                l = mid + 1
            else:
                r = mid
        
        s = l #store the position (index) of the first suffix containing the pattern
        r = n

        #second binary search (find index + 1 of the last suffix containing the pattern)
        while l < r:
            mid = (l + r) // 2
            if pattern == self.text[self.suffix_array[mid]:self.suffix_array[mid]+p]:
                l = mid + 1
            else:
                r = mid

        return sorted(self.suffix_array[s:r]) #sort the output indices so they are in increasing order


