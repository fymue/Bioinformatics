#!/usr/bin/env python3

import numpy as np, argparse
from typing import List, Dict

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

def calc_blosum(blocks: List[List[str]], aminos: Dict[str, int]) -> np.array:
    #calculation of the BLOSUM using mostly numpy (for performance improvements)
    
    total_as = len(aminos)

    #create upper triangular matrix with all (one-sided) pairs of amino acids (used in step 1 and 2, see below)
    qij_matrix = [[0] * total_as for i in range(total_as)]

    #qij_matrix = np.zeros((total_as, total_as), dtype=np.uint32)

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
    
    #Step 3: sum up the relative frequencies of observed subs for every amino acid (Original BLOSUM paper: p_ij-Matrix)
    colsums = np.sum(qij_matrix, axis=0)
    rowsums = np.sum(qij_matrix, axis=1)
    pij_matrix = (colsums + rowsums) / 2

    #Step 4: calculate the estimated sub frequencies for every pair of amino acids (Original BLOSUM paper: e_ij-Matrix)
    sij_matrix = np.empty((total_as, total_as), dtype=np.float32)
    for row in range(total_as): sij_matrix[row] = pij_matrix * pij_matrix[row] * 2
    sij_matrix[np.arange(total_as), np.arange(total_as)] /= 2

    #Step 5: divide the observed sub frequencies by the estimated sub frequencies and normalize them (log2) (Original BLOSUM paper: s_ij-Matrix)
    #this matrix equals the BLOSUM
    sij_matrix = np.round(2 * np.log2(qij_matrix / sij_matrix))

    return sij_matrix

def print_matrix(m: np.array, aminos: Dict[str, int]) -> None:
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
    return None

def write_matrix(blosum: np.array, file: str, aminos: Dict[str, int]) -> None:
    #write the BLOSUM to a file
    with open(file, "w") as fout:
        fout.write("#observed amino acid\treplacement amino acid\tcost\n")
        for as1, row in aminos.items():
            for as2, col in aminos.items():
                if row > col: continue

                fout.write(f"{as1}\t{as2}\t{blosum[row, col]}\n")
    
    return None

if __name__ == "__main__": 
    # handling of command line features

    parser = argparse.ArgumentParser()
    parser.add_argument("--print", "-p", action="store_true", help="print blocks substitution matrix (BLOSUM)")
    parser.add_argument("--save", "-s", metavar="FILE", type=str, help="ave the BLOSUM to a file")
    parser.add_argument("blocks_file", type=str, help="path to blocks file")
    
    args = parser.parse_args()

    aminos = {c : i for i, c in enumerate("CSTPAGNDEQHRKMILVFYWJ")}
    blocks = read_sequences(args.blocks_file)
    blosum = calc_blosum(blocks, aminos)

    if args.print: print_matrix(blosum, aminos)
    if args.save: write_matrix(blosum, args.save, aminos)     