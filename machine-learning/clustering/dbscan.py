#!/usr/bin/env python3

import numpy as np, matplotlib.pyplot as plt, argparse
from kneed import KneeLocator

class DBScan:
    def __init__(self, input_file: str = None, delimiter: str = " "):
        self.data = None

        if input_file is not None:
            data = self.read_input_file(input_file, delimiter)
            if self.validate_data(data): self.data = data
    
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
        #calculate the euclidian distance between a point (pt_1) and 1-n points (pt_2)

        a = np.power(pt_1[0] - pts_2[:, 0], 2)
        b = np.power(pt_1[1] - pts_2[:, 1], 2)

        if method == "squared": return a + b

        return np.sqrt(a + b)
    
    def calc_dist_matrix(self, data: np.array = None, method: str = "reg") -> np.array:
        #calculate distances between all points (upper triangular is sufficient)

        if data is None: data = self.data 

        n = len(data) # total number of points

        dist_matrix = np.empty((n, n), dtype=np.float32)

        for row in range(n-1):
            # calculate distances from cluster of current row 
            # to every cluster of current col as vector operation

            row_point = data[row]
            col_points = data[row:]
            dist_matrix[row, row:] = self.calc_dist(row_point, col_points, method)
        
        dist_matrix[n-1, n-1] = 0.0

        return dist_matrix

    def calc_dist_indices(self, n: int) -> np.array:
        # calculate the row and col indices
        # for all distances of a point to all
        # other points in a upper triangular matrix

        indices = np.empty((2, n, n), dtype=np.uint16)

        for i in range(n): 
            row_i = np.concatenate((np.arange(i), np.repeat(i, n-i)))
            col_i = np.concatenate((np.repeat(i, i), np.arange(i, n)))

            indices[0, i] = row_i
            indices[1, i] = col_i

        return indices
    
    def estimate_eps(self, dist_matrix: np.array, k: int, data: np.array = None, indices: np.array = None) -> float:
        # estimate a proper value of epsilon
        # 1. find the k nearest neighbors of every point
        # 2. find the avg. distance of those k distances
        # 3. sort the avg. distances and find the "knee point" in the curve

        if data is None: data = self.data
        n = len(data)
        if indices is None: indices = self.calc_dist_indices(n)

        # Steps 1 & 2
        avg_k_dist = np.mean(np.sort(dist_matrix[indices[0], indices[1]], axis=1)[:, 1:k+1], axis=1)

        # Step 3
        avg_k_dist.sort()

        knee_locator = KneeLocator(np.arange(len(data)), avg_k_dist, curve="convex", direction="increasing")
        eps = knee_locator.knee_y
        
        return eps
    
    def region_query(self, point_i: int, dists: np.array, indices: np.array, eps: float) -> np.array:
        # find all points within the radius eps of a point

        dists_to_point_i = dists[indices[0, point_i], indices[1, point_i]] <= eps

        return dists_to_point_i.nonzero()[0]
    
    def expand_cluster(self, cluster_members: np.array, labels: np.array, neighbors: np.array, 
                       dists: np.array, indices: np.array, cluster_id: int, eps: float, min_pts: int) -> None:
            
        # expand the current cluster by adding all neighbors
        # of core points to the cluster

        i = 0

        while i < len(neighbors):
            # go over all points of the seed list
            # (will potentially be extended during the loop)

            point_i = neighbors[i]
            i += 1

            if cluster_members[0, point_i]: 
                # if point was previously labeled as noise,
                # add to current cluster (as border point)
                cluster_members[0, point_i] = False
                cluster_members[cluster_id, point_i] = True
                continue

            if not labels[point_i]:
                labels[point_i] = True
                neighbors_ext = self.region_query(point_i, dists, indices, eps)

                # extend neighbors (==cluster)
                if len(neighbors_ext) >= min_pts:
                    # find all points that are in the neighborhood
                    # of the last core point that aren't already
                    # in the neighbor array
                    neighbors_diff = np.setdiff1d(neighbors_ext, neighbors, assume_unique=True)
                    neighbors = np.concatenate((neighbors, neighbors_diff))

                # add point to current cluster
                cluster_members[cluster_id, point_i] = True
        
        return None
    
    def cluster_data(self, min_pts: int, eps: float = None, data: np.array = None, auto: bool = True) -> np.array:
        # cluster the data using the DBScan clustering method

        if data is None: data = self.data

        dists = self.calc_dist_matrix(data)
        indices = self.calc_dist_indices(len(data))

        if auto or eps is None: eps = self.estimate_eps(dists, min_pts, indices=indices)

        # boolean array for all (potential) clusters
        cluster_members = np.zeros(dists.shape, dtype=bool)

        # keep track which points have been assigned to a cluster
        labels = np.zeros(len(data), dtype=bool)

        cluster_id = 0

        for i, point in enumerate(data):
            if not labels[i]:
                # only cluster unlabeled points
                # (some will be labeled in expand_cluster)

                labels[i] = True
                neighbors = self.region_query(i, dists, indices, eps)

                if len(neighbors) >= min_pts:
                    # core point -> make new cluster and expand
                    cluster_id += 1
                    cluster_members[cluster_id, i] = True
                    self.expand_cluster(cluster_members, labels, neighbors, dists, indices, cluster_id, eps, min_pts)
                else:
                    # noise point
                    cluster_members[0, i] = True

        # return only the clusters which contain points
        # (the remaining rows were just placeholders)
        return cluster_members[:cluster_id+1]

    def plot_clusters(self, cluster_members: np.array, data: np.array = None, title: str = None, output_file: str = None) -> None:
        # plot the points of the different clusters

        if data is None: data = self.data

        for i, cluster in enumerate(cluster_members):
            # separate the point coordinates of the current cluster
            points = data[cluster]
            x, y = points[:, 0], points[:, 1]

            if i == 0: label = f"Noise points ({np.sum(cluster)})"
            else: label = f"Cluster {i} ({np.sum(cluster)})"

            plt.scatter(x, y, s=10, label=label) #scatter the points of the current cluster (each cluster will have a different color)

        if title is not None: plt.title(title)

        plt.legend(loc="best")

        if output_file is not None:
            plt.savefig(output_file, dpi=300)
        else:
            plt.show()
            plt.close()

        return None

if __name__ == "__main__":
    # handling of command line features

    parser = argparse.ArgumentParser()
    parser.add_argument("--delimiter", "-d", metavar="D", type=str, help="specify the delimiter of the input file")
    parser.add_argument("--title", "-t", metavar="'TITLE'", type=str, help="title of the cluster plot")
    parser.add_argument("--out", "-o", metavar="FILE", type=str, help="save the cluster plot to a file")
    parser.add_argument("--epsilon", "-e", metavar="N", type=float, help="set epsilon value")
    parser.add_argument("--minpts", "-mp", metavar="N", default=4, type=int, help="set the minimum points within epsilon (default: 4)")
    parser.add_argument("--auto", "-a", action="store_true", help="let the program estimate an appropriate epsilon value")
    parser.add_argument("input_file", type=str, help="path to input file containing data points")

    args = parser.parse_args()
    
    clusterer = DBScan(args.input_file, args.delimiter)
    cluster_members = clusterer.cluster_data(args.minpts, eps=args.epsilon, auto=args.auto)
    clusterer.plot_clusters(cluster_members)