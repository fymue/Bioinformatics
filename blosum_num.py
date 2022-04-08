import numpy as np
from math import sqrt
from sys import argv

def read_sequences(blocks_file):
    #to create the blocks substitution matrix (BLOSUM), we need "good", gap-free alignments of proteins
    #the blocks database (blocks_5.0.txt) offers such alignments
    #later we can use these alignments to calculate accurate amino acid substitution scores for protein sequence alignments
    
    all_blocks = []
    with open(blocks_file, "r") as fin:
        look = False
        seqs_of_block = []
        for l in fin:
            l = l.rstrip()
            if not l: continue

            if l.startswith("BL "): #look for new block of gap-free, aligned sequences
                look = True
                continue
            
            if l == "//": #end of block marker
                look = False
                all_blocks.append(seqs_of_block) #add sequences of current block
                seqs_of_block = []
                continue

            l = l.split()

            if look: #if program is currently looking through a block, add the sequence
                seqs_of_block.append(l[3])

    return all_blocks

def calc_blosum(blocks):
    #calculation of the BLOSUM using mostly numpy (for performance improvements)
    
    aminos = {c : i for i, c in enumerate("CSTPAGNDEQHRKMILVFYWJ")}
    total_as = len(aminos)

    #create upper triangular matrix with all (one-sided) pairs of amino acids (used in step 1 and 2, see below)
    qij_matrix = [[0] * total_as for i in range(total_as)]

    total_subs = 0 #keep track of the total number of substitutions (subs)

    #Step 1: count the total number of substitutions of all amino acid pairs (Original BLOSUM paper: f_ij-Matrix)
    for i, seqs in enumerate(blocks):
        total_seqs = len(seqs) #total number of sequences of current block

        for j in range(total_seqs):
            curr_seq = seqs[j]

            for amino in range(len(curr_seq)):
                for k in range(j+1, total_seqs):
                    
                    nxt_seq = blocks[i][k]
                    as1, as2 = curr_seq[amino], nxt_seq[amino]
                    index1, index2 = aminos[as1], aminos[as2]
                    qij_matrix[index1][index2] += 1
                    total_subs += 1
   
    #Step 2: divide every value by the total number of subs (to get the relative number of subs) (Original BLOSUM paper: q_ij-Matrix)
    qij_matrix = np.array(qij_matrix) / total_subs #should be triangular only (but this is faster); will be corrected in step 3
    
    #qij_matrix = np.triu(qij_matrix) + np.tril(qij_matrix, k=-1).T #upper triangular only

    #Step 3: sum up the relative frequencies of observed subs for every amino acid (Original BLOSUM paper: p_ij-Matrix)
    colsums = np.sum(qij_matrix, axis=0) / 2
    rowsums = np.sum(qij_matrix, axis=1) / 2
    pij_matrix = np.array([colsums[i] + rowsums[i] for i in range(total_as)])
    
    #Step 4: calculate the estimated sub frequencies for every pair of amino acids (Original BLOSUM paper: e_ij-Matrix)
    sij_matrix = np.array([[pij_matrix[row] * pij_matrix[col] if row == col else pij_matrix[row] * pij_matrix[col] * 2 for col in range(total_as)] for row in range(total_as)])

    #Step 5: divide the observed sub frequencies by the estimated sub frequencies and normalize them (log2) (Original BLOSUM paper: s_ij-Matrix)
    #this matrix equals the BLOSUM
    sij_matrix = np.round(2 * np.log2(qij_matrix / sij_matrix))

    return sij_matrix, aminos

def print_matrix(m, aminos):
    #print the BLOSUM matrix
    print("     " + "    ".join(aminos))
    start = True
    curr = ""

    for as1, i in aminos.items():
        for as2, j in aminos.items():
            if as1 != curr:
                curr = as1
                start = True
                print()
            if start:
                print(as1, end=" ")
                start = False
            if i <= j:
                print(f"{m[i,j]:4.0f}", end=" ")
            else:
                print("    ", end=" ")
    
    print("\n")

def write_matrix(blosum, file, aminos):
    #write the BLOSUM to a file
    with open(file, "w") as fout:
        fout.write("#observed amino acid\treplacement amino acid\tcost\n")
        for as1, row in aminos.items():
            for as2, col in aminos.items():
                if row > col: continue

                fout.write(f"{as1}\t{as2}\t{blosum[row, col]}\n")

#handling of command line features
valid_commands = {"-print", "-save"}
help_commands = {"--help", "-help", "-h"}

def valid_command(args): return False if not all(inp in valid_commands | help_commands for inp in args[1:len(args)-2] if inp[0] == "-" and ord(inp[1]) > 65) else True

def print_help():
    print("Usage: blosum_np.py [OPTIONS] path/to/blocks/file\n")
    print("Options:\n")
    print("-print\t\t\t\tprint blocks substitution matrix (BLOSUM)")
    print("-save file\t\t\t\tsave the BLOSUM to a file\n")

def parse_command():

    def get_args_val(arg, val):
        if arg in args: val = argv[args[arg]+1]
        return val

    args = {arg : i for i, arg in enumerate(argv)}
    targs = len(argv)
    valid = valid_command(argv)

    if targs == 1 or not valid:
        print("Usage: blosum_np.py [OPTIONS] path/to/blocks/file\n")
        print("use --help, -help or -h to display usage help\n")

    elif targs == 2 and argv[1] in help_commands: print_help()
        
    elif targs >= 3 and valid:
        blocks_file = argv[-1]
        output_file = get_args_val("-save", "")
        print_blosum = True if "-print" in args else False
    
    try:
        blocks = read_sequences(blocks_file)
        blosum, aminos = calc_blosum(blocks)

        if print_blosum:
            print("BLOSUM-Matrix:\n")
            print_matrix(blosum, aminos)

        if output_file:
            write_matrix(blosum, output_file, aminos)

    except FileNotFoundError:
        print("This file is of unknown structure or could not be found!")

if __name__ == "__main__": parse_command()

        