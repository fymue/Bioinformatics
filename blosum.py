from numpy import log2
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
    
    all_aminos = [c for c in "CSTPAGNDEQHRKMILVFYWJ"]

    #create upper triangular matrix with all (one-sided) pairs of amino acids (used in step 1 and 2, see below)
    q_matrix = {(all_aminos[as1], all_aminos[as2]) : 0 for as1 in range(len(all_aminos)) for as2 in range(as1, len(all_aminos))}
    
    p_matrix = {amino : 0 for amino in all_aminos}
    total_subs = 0 #keep track of the total number of substitutions (subs)

    #Step 1: count the total number of substitutions of all amino acid pairs (Original BLOSUM paper: f_ij-Matrix)

    for i, seqs in enumerate(blocks):
        total_seqs = len(seqs) #total number of sequences of current block

        for j in range(total_seqs):
            curr_seq = seqs[j]

            for amino in range(len(curr_seq)):
                for k in range(j+1, total_seqs):
                    
                    nxt_seq = blocks[i][k]
                    amino_pair = (curr_seq[amino], nxt_seq[amino])
                    if amino_pair not in q_matrix: amino_pair = tuple(reversed(amino_pair)) #make sure the amino_pair is correct (since we only have a triangular matrix)
                    q_matrix[amino_pair] += 1
                    total_subs += 1
   
    #Step 2: Divide every value by the total number of subs (to get the relative number of subs) (Original BLOSUM paper: q_ij-Matrix)
    for amino_pair in q_matrix: q_matrix[amino_pair] /= total_subs

    #Step 3: Sum up the relative frequencies of observed subs for every amino acid (Original BLOSUM paper: p_ij-Matrix)
    for amino_pair in q_matrix:
        if amino_pair[0] == amino_pair[1]:
            p_matrix[amino_pair[0]] += q_matrix[amino_pair]
        else:
            p_matrix[amino_pair[0]] += q_matrix[amino_pair] / 2
            p_matrix[amino_pair[1]] += q_matrix[amino_pair] / 2

    #Step 4: Calculate the estimated sub frequencies for every pair of amino acids (Original BLOSUM paper: e_ij-Matrix)
    e_matrix = {amino_pair : p_matrix[amino_pair[0]] * p_matrix[amino_pair[1]] if amino_pair[0] == amino_pair[1] else p_matrix[amino_pair[0]] * p_matrix[amino_pair[1]] * 2 for amino_pair in q_matrix}

    #Step 5: Divide the observed sub frequencies by the estimated sub frequencies and normalize them (log2) (Original BLOSUM paper: s_ij-Matrix)
    #this matrix equals the BLOSUM
    s_matrix = {amino_pair : round( 2 * log2(q_matrix[amino_pair] / e_matrix[amino_pair])) for amino_pair in q_matrix}
    
    return s_matrix, all_aminos

def print_matrix(m, aminos):
    #print the BLOSUM matrix
    print("     " + "    ".join(aminos))
    start = True
    curr = ""

    for as1 in aminos:
        for as2 in aminos:
            amino_pair = (as1, as2)
            if as1 != curr:
                curr = as1
                start = True
                print()
            if start:
                print(as1, end=" ")
                start = False
            if amino_pair in m:
                print(f"{m[amino_pair]:4.0f}", end=" ")
            else:
                print("    ", end=" ")
    print("\n")


if __name__ == "__main__":
    if len(argv) == 1:
        print("Usage: blosum.py path/to/blocks/file\n")
    else:
        blocks_file = argv[1]
        try:
            blocks = read_sequences(blocks_file)
            blosum_matrix, aminos = calc_blosum(blocks)
            print("BLOSUM-Matrix:\n")
            print_matrix(blosum_matrix, aminos)
        except FileNotFoundError:
            print("This file is of unknown structure or could not be found!")