#!/usr/bin/env python3

import matplotlib.pyplot as plt, numpy as np, argparse
from time import time
from typing import Tuple

def timer(func, *args, times: int = 1):
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
    
    def calc_dist(self, pt_1: np.array, pts_2: np.array, method: str ="reg") -> np.array:
        #calculate the euclidian distance between a point (pt_1) and 1-n points (pts_2)

        a = np.power(pt_1[0] - pts_2[:, 0], 2)
        b = np.power(pt_1[1] - pts_2[:, 1], 2)

        if method == "squared": return a + b

        return np.sqrt(a + b)
    
    def calc_dist_matrix(self, data: np.array, method: str = "reg") -> np.ma.array:
        #calculate distances between all points (upper triangular is sufficient)

        n = data.shape[0] # total number of points

        mask = ~np.triu(np.ones((n, n), dtype=bool), k=1) # mask the lower triangular and main diagonal
        dist_matrix = np.ma.empty((n, n), dtype=np.float32)
        dist_matrix.mask = mask

        for row in range(n-1):
            # calculate distances from cluster of current row 
            # to every cluster of current col as vector operation

            row_cluster = data[row]
            col_clusters = data[row+1:]
            dist_matrix[row, row+1:] = self.calc_dist(row_cluster, col_clusters, method)

        return dist_matrix
    
    def set_cluster_dist_method(self, method):
        # set the method to calculate the
        # distance between two clusters (vectorized)

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
        
        # mask the row and col of one of the clusters previously merged 
        dists.mask[discard, :] = True
        dists.mask[:, discard] = True

        return dists
    
    def cluster_data(self, k: int, data: np.array = None) -> np.array:
        # cluster the data points using hierarchical clustering

        if data is None: data = self.data

        dists = self.calc_dist_matrix(data)

        cluster_members = np.zeros(dists.shape, dtype=bool) # boolean array for members of every cluster
        np.fill_diagonal(cluster_members, True)

        mask = np.ones(cluster_members.shape[0], dtype=bool) # mask for clusters that have been merged (rows/cols will be ignored)

        while k < data.shape[0]:
            # cluster, until all points are part of only the initial k clusters

            keep, discard = np.unravel_index(dists.argmin(), dists.shape) # get indices of 2 clusters w/ min. distance

            # update members of one of the merge members and mask the other one
            combined_cluster = np.logical_or(cluster_members[keep], cluster_members[discard])
            cluster_members[keep] = combined_cluster
            mask[discard] = False

            # update the distance matrix after the merge
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

if __name__ == "__main__":
    # handling of command line features

    parser = argparse.ArgumentParser()
    parser.add_argument("--delimiter", "-d", metavar="D", type=str, help="specify the delimiter of the input file")
    parser.add_argument("--title", "-t", metavar="'TITLE'", type=str, help="title of the cluster plot")
    parser.add_argument("--out", "-o", metavar="FILE", type=str, help="save the cluster plot to a file")
    parser.add_argument("--method", "-m", metavar="METHOD", type=str, help="clustering method (average (default), single, complete")
    parser.add_argument("input_file", type=str, help="path to input file containing data points")
    parser.add_argument("k", type=int, help="number of clusters to form")

    args = parser.parse_args()

    clusterer = HierarchClustering(args.input_file, args.delimiter, args.method)
    cluster_members = clusterer.cluster_data(args.k)
    clusterer.plot_clusters(cluster_members, title=args.title, output_file=args.out)