from sys import argv

def max_alignments(m, n, check):
    #calculate the maximum number of possible alignments
    #given s (length m) and t (length n) 
    #recursively w/ dynamic programming

    if (m, n) in check: return check[(m, n)] #lookup if calculation has been done already
    if m == 0 or n == 0:
        #recursion termination (one empty string and another (non-)emtpy string have exactly 1 alignment)
        return 1
    
    #definition of maximum alignments function: A(m, n) = A(m-1, n) + A(m-1, n-1) + A(m, n-1)
    a = max_alignments(m - 1, n, check)
    b = max_alignments(m - 1, n - 1, check)
    c = max_alignments(m, n - 1, check)
    check[(m - 1, n)] = a
    check[(m - 1), (n - 1)] = b
    check[(m, n - 1)] = c
    return a + b + c

def max_alignments_iter(m, n):
    #iterative version of the maximum alignments function
    #requires an m*n sized array
    m += 1
    n += 1
    arr = [[0 for i in range(n)] for j in range(m)]

    #initialize the 1st row and column to 1 (one empty string and another (non-)emtpy string have exactly 1 alignment)
    for i in range(m): arr[i][0] = 1
    for j in range(n): arr[0][j] = 1

    #definition of maximum alignments function: A(m, n) = A(m-1, n) + A(m-1, n-1) + A(m, n-1)
    for i in range(1,m):
        for j in range(1,n):
            a = arr[i-1][j]
            b = arr[i-1][j-1]
            c = arr[i][j-1]
            arr[i][j] = a + b + c

    return arr[i][j]

def needleman_wunsch(s, t, match=0, mismatch=1, gap=1, costf=min, print_scores=False, print_trace=False, print_max=False, print_optimal=False):
    print(f"\nAlignment of '{s}' and '{t}' using the Needleman-Wunsch algorithm")
    print(f"Cost parameters: match = {match}, mismatch = {mismatch}, gap = {gap}\n\n")

    m = len(s) + 1 #number of rows, s: 1st sequence
    n = len(t) + 1 #number of columns, t: 2nd sequence

    nw_arr = [[0] * n for _ in range(m)] #empty m*n matrix for the needleman wunsch (nw) matrix

    for i in range(m): nw_arr[i][0] = i * gap  #fill first column of the nw matrix
    for j in range(n): nw_arr[0][j] = j * gap #fill first row of the nw matrix

    #Editing operations: S = Stop, I = Insertion, D = Deletion, E : Replacement

    op_arr = [[set() for __ in range(n)] for _ in range(m)] #matrix for the editing operations necessary (needed for backtracing later)
    for i in range(1,m): op_arr[i][0].add("D") #fill first column 
    for j in range(1,n): op_arr[0][j].add("I") #fill first row
    op_arr[0][0] = {"S"}

    for i in range(1, m):
        for j in range(1, n):
            #calculate the scores of each operation
            score_left = nw_arr[i-1][j] + gap #insertion cost
            score_up = nw_arr[i][j-1] + gap #deletion cost
            score_diag = nw_arr[i-1][j-1] + match if s[i-1] == t[j-1] else nw_arr[i-1][j-1] + mismatch  #replacement cost

            possible_scores = {"D" : score_left, "I" : score_up, "E" : score_diag}
            score = costf(possible_scores.values()) #get best score according to cost function (standard=min)
            nw_arr[i][j] = score

            for op in possible_scores:
                if possible_scores[op] == score:
                    op_arr[i][j].add(op) #add the operation associated with the best score

    #sample co-optimal alignments via backtracing
    i = m - 1
    j = n - 1
    optimal_alignments = nw_traceback(op_arr, s, t, i, j, "", "", [])

    #sample one (co-)optimal alignment iteratively
    """
    optimal_alignment_1 = ""
    optimal_alignment_2 = ""
    while i > 0 or j > 0: #go backwards iteratively until we reach 0 0 (aka the start)
        if "E" in op_arr[i][j]: #if replacement was done, go up diagonally
            optimal_alignment_1 += s[i-1]
            optimal_alignment_2 += t[j-1]
            i -= 1
            j -= 1
        elif "I" in op_arr[i][j]: #if insertion was done, go to the left
            optimal_alignment_1 += "_"
            optimal_alignment_2 += t[j-1]
            j -= 1
        else: #if deletion was done, go up
            optimal_alignment_1 += s[i-1]
            optimal_alignment_2 += "_"
            i -= 1
    """

    #print maximum number of possible alignments
    if print_max: 
        print(f"Total number of possible alignments: A('{s}', '{t}'):", max_alignments(len(s), len(t), {}), "\n")

    #print nw scores array
    if print_scores:
        print("Needleman-Wunsch scores matrix:\n")
        print("      " + " ".join((f"{char:2}" for char in t)))
        for i in range(m):
            row =  " ".join((f"{el:02}" for el in nw_arr[i]))
            if i > 0: print(s[i-1], row)
            else: print(" ", row)
        print("\n")


    #print("One possible optimal alignment:")
    #print(optimal_alignment_1[::-1] + "\n" + optimal_alignment_2[::-1] + "\n")
    
    if print_trace:
        print(f"Co-optimal alignments ({len(optimal_alignments)} total): \n")
        for al1, al2, in optimal_alignments:
            print(al1[::-1])
            print(al2[::-1])
            print()
    
    if print_optimal: print(f"Optimal alignment score: {nw_arr[m-1][n-1]}\n")

    return 

def nw_traceback(op_arr, s, t, i, j, al1, al2, alignments):
    if i == 0 and j == 0: #if the stop operation is reached, the traceback is completed
        alignments.append((al1, al2))

    if "E" in op_arr[i][j]: #if replacement was done, go up diagonally
        nw_traceback(op_arr, s, t, i-1, j-1, al1 + s[i-1], al2 + t[j-1], alignments)
    if "I" in op_arr[i][j]: #if insertion was done, go to the left
        nw_traceback(op_arr, s, t, i, j-1, al1 + "_", al2 + t[j-1], alignments)
    if "D" in op_arr[i][j]: #if deletion was done, go up
        nw_traceback(op_arr, s, t, i-1, j, al1 + s[i-1], al2 + "_", alignments)

    return alignments


#handling of command line features
valid_commands = {"-matrix", "-total", "-trace", "-costf", "-optimal", "-m", "-mm", "-g"}
help_commands = {"--help", "-help", "-h"}

def valid_command(args): return False if not all(inp in valid_commands | help_commands for inp in args[1:len(args)-2] if inp[0] == "-" and ord(inp[1]) > 65) else True

def print_help():
    print("Usage: nw_alignment.py [OPTIONS] string1 string2\n")
    print("Options:\n")
    print("-total\tprint the maximum number of possible alignments")
    print("-matrix\tprint the needleman wunsch scores matrix")
    print("-trace\tsample (co-)optimal alignments")
    print("-costf ['min'/'max']\tuse minimum or maximum to calculate optimal score at each step")
    print("-optimal\tprint the optimal alignment score")
    print("-m [value]\tmatch value (default is 0)")
    print("-mm [value]\tmismatch value (default is 1)")
    print("-g [value]\tgap value (default is 1)\n")

def parse_command():

    def get_args_val(arg, val):
        if arg in args: val = argv[args[arg]+1]
        return val

    args = {arg : i for i, arg in enumerate(argv)}
    targs = len(argv)
    valid = valid_command(argv)

    if targs < 3 and not valid:
        print("Usage: nw_alignment.py [OPTIONS] string1 string2\n")
        print("use --help, -help or -h to display usage help\n")

    elif targs == 2 and argv[1] in help_commands: print_help()
        
    elif targs >= 3 and valid:
        s = argv[-2]
        t = argv[-1]
        match = int(get_args_val("-m", 0))
        mismatch = int(get_args_val("-mm", 1))
        gap = int(get_args_val("-g", 1))
        func = max if get_args_val("-costf", "min") == "max" else min
        print_scores = True if "-matrix" in args else False
        print_trace = True if "-trace" in args else False
        print_max = True if "-total" in args else False
        print_optimal = True if "-optimal" in args else False

        needleman_wunsch(s, t, match, mismatch, gap, func, print_scores, print_trace, print_max, print_optimal)

if __name__ == "__main__": parse_command()