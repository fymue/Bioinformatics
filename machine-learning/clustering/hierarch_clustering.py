#!/usr/bin/env python3

import matplotlib.pyplot as plt
import numpy as np
from time import time
from sys import argv
from typing import Tuple

def timer(func, *args, times=1):
    # time the execution time of a function

    total_time = 0

    for run in range(times):
        st = time()
        res = func(*args)
        elapsed = time() -st
        total_time += elapsed
    
    print(f"Average execution time ({times} runs): {total_time / times:.5f}s")
    
    return res

class HierarchClustering:
    # perform hierachical clustering of data points

    def __init__(self, input_file: str = None, delimiter: str = " ", method: str = "average") -> None:

        self.data = None

        if input_file is not None:
            data = self.read_input_file(input_file, delimiter)
            if self.validate_data(data): self.data = data
        
        self.calc_cluster_dist = self.set_cluster_dist_method(method)
    
    def read_input_file(self, input_file: str, delimiter: str) -> np.array:
        # read the input file containing x and y coordinates of points

        return np.loadtxt(input_file, delimiter=delimiter, dtype=np.float32)
    
    def validate_data(self, data: np.array) -> bool:
        #check if the input data is valid (same length, all numbers)

        valid = False
        
        if data is not None and not np.isnan(data).any(): valid = True
        else: print("Invalid data structure! Please provide two arrays X and Y as data points!")

        return valid
    
    def calc_dist(self, pt_1: np.array, pt_2: np.array, method: str ="reg") -> float:
        #calculate the euclidian distance between two points

        a = np.power(pt_1[0] - pt_2[:, 0], 2)
        b = np.power(pt_1[1] - pt_2[:, 1], 2)

        if method == "squared": return a + b

        return np.sqrt(a + b)
    
    def calc_dist_matrix(self, data: np.array, method: str = "reg") -> np.ma.array:
        #calculate distances between all points (upper triangular is sufficient)

        n = data.shape[0] # total number of points

        mask = ~np.triu(np.ones((n, n), dtype=bool), k=1)
        dist_matrix = np.ma.empty((n, n), dtype=np.float32)
        dist_matrix.mask = mask

        for row in range(n-1):
            row_cluster = data[row]
            col_clusters = data[row+1:]
            dist_matrix[row, row+1:] = self.calc_dist(row_cluster, col_clusters, method)

        return dist_matrix
    
    def set_cluster_dist_method(self, method):
        # set the method to calculate the
        # distance between two clusters

        if method == "single":
            return lambda c1, c2: np.min(np.column_stack((c1, c2)), axis=1)
        elif method == "complete":
            return lambda c1, c2: np.max(np.column_stack((c1, c2)), axis=1)

        # default: calculate avg. distance
        return lambda c1, c2: (c1 + c2) / 2

    def update_dists(self, dists: np.ma.array, cluster_members: np.array,
                     mask: np.array, merged_clusters: Tuple[int, int]) -> np.array:
        # update the distance matrix after the
        # two closest clusters have been merged

        keep, discard = merged_clusters

        # find cluster distances that need to be updated 
        # (those that aren't masked already)
        # and get indices from resulting boolean array

        dists_to_update = np.logical_and(~cluster_members[keep], mask).nonzero()[0]

        keep_arr = np.repeat(keep, dists_to_update.size) # repeat keep index to later zip it to cluster indices
        discard_arr = np.repeat(discard, dists_to_update.size)

        unmasked_keep_indices = ~dists.mask[dists_to_update, keep_arr]
        unmasked_discard_indices = ~dists.mask[dists_to_update, discard_arr]

        keep_i = np.column_stack((dists_to_update, keep_arr))[unmasked_keep_indices]
        discard_i = np.column_stack((dists_to_update, discard_arr))[unmasked_discard_indices]

        # sort row/col indices (if needed) since we only store upper triangular matrix
        keep_i.sort()
        discard_i.sort()

        mx = keep_i.shape[0] # make sure that we only look at the clusters we actually need to update distances for

        dists[keep_i[:mx, 0], keep_i[:mx, 1]] = self.calc_cluster_dist(dists[keep_i[:mx, 0], keep_i[:mx, 1]], 
                                                                       dists[discard_i[:mx, 0], discard_i[:mx, 1]])

        dists.mask[discard, :] = True
        dists.mask[:, discard] = True

        return dists
    
    def cluster_data(self, k: int, data: np.array = None) -> np.array:
        # cluster the data points using hierarchical clustering

        if data is None: data = self.data

        dists = self.calc_dist_matrix(data)

        cluster_members = np.zeros(dists.shape, dtype=bool)
        np.fill_diagonal(cluster_members, True)

        mask = np.ones(cluster_members.shape[0], dtype=bool)

        while k < data.shape[0]:
            # cluster, until all points are part of only the initial k clusters

            keep, discard = np.unravel_index(dists.argmin(), dists.shape) # get indices of 2 clusters w/ min. distance

            combined_cluster = np.logical_or(cluster_members[keep], cluster_members[discard])
            cluster_members[keep] = combined_cluster
            mask[discard] = False

            dists = self.update_dists(dists, cluster_members, mask, (keep, discard))

            k += 1

        # remove clusters that have been added/merged to another cluster during the clustering
        cluster_members = cluster_members[mask]

        return cluster_members
    
    def plot_clusters(self, cluster_members: np.array, data: np.array = None, title: str = None, output_file: str = None) -> None:
        # plot the points of the different clusters

        if data is None: data = self.data
        
        for i, cluster in enumerate(cluster_members, start=1):
            # separate the point coordinates of the current cluster
            points = data[cluster]
            x, y = points[:, 0], points[:, 1]

            plt.scatter(x, y, s=10, label= f"Cluster {i} ({np.sum(cluster)})") #scatter the points of the current cluster (each cluster will have a different color)

        if title is not None: plt.title(title)

        plt.legend(loc="best")

        if output_file is not None: plt.savefig(output_file, dpi=300)

        plt.show()
        plt.close()

        return None

#handling of command line features
valid_commands = {"-d", "-title", "-out", "-method"}
help_commands = {"--help", "-help", "-h"}

def valid_command(args): return True if all(inp in valid_commands | help_commands for inp in args[1:len(args)-1] if inp[0] == "-") else False

def print_help():
    print("Usage: hierach_clustering.py [OPTIONS] inputfile k\n")
    print("k stands for the number of clusters.")
    print("The input file should contain the x and y coordinates of a data point in the same line.")
    print("The delimiter can be specified.\n")
    print("Options:\n")
    print('-d "delimiter"\t\t\tset the delimiter for the input file (standard: space)')
    print('-title "title"\t\t\tset the title of the cluster plot')
    print("-out filename\t\t\tsave the plot in a file")
    print('-method "method"\t\tinter-cluster distance calculation method (Options: "single" (default), "complete", "average")')

def parse_command():

    def get_args_val(arg, val):
        if arg in args: val = argv[args[arg]+1]
        return val

    args = {arg : i for i, arg in enumerate(argv)}
    targs = len(argv)
    valid = valid_command(argv)
    
    if targs == 1 or not valid:
        print('Usage: hierarch_clustering.py [OPTIONS] inputfile k\n')
        print("use --help, -help or -h to display usage help\n")

    elif targs == 2 and argv[1] in help_commands: print_help()
        
    elif targs >= 3 and valid:
        input_file = argv[-2]
        k = int(argv[-1])
        delimiter = get_args_val("-d", " ")
        method= get_args_val("-method", "single")
        title = get_args_val("-title", "")
        output_file = get_args_val("-out", None)

        clusterer = HierarchClustering(input_file, delimiter, method)
        cluster_members = clusterer.cluster_data(k)
        clusterer.plot_clusters(cluster_members)

if __name__ == "__main__": parse_command()