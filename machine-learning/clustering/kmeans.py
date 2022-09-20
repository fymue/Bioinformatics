#!/usr/bin/env python3

import matplotlib.pyplot as plt, numpy as np, argparse
from time import time

class KMeans:
    def __init__(self, input_file: str = None, delimiter: str = " ") -> None:

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
    
    def plot_clusters(self, cluster_members: np.array, centroids: np.array, data: np.array = None, title: str = None, output_file: str = None) -> None:
        # plot the points of the different clusters

        if data is None: data = self.data
        
        for i, cluster in enumerate(cluster_members, start=1):
            # separate the point coordinates of the current cluster
            points = data[cluster]
            x, y = points[:, 0], points[:, 1]

            plt.scatter(x, y, s=10, label=f"Cluster {i} ({np.sum(cluster)})") #scatter the points of the current cluster (each cluster will have a different color)

        plt.scatter(centroids[:, 0], centroids[:, 1], s=10, label=f"Centroids ({len(centroids)})")

        if title is not None: plt.title(title)

        plt.legend(loc="best")

        if output_file is not None: plt.savefig(output_file, dpi=300)

        plt.show()
        plt.close()

        return None
    
    def init_random_centroids(self, k: int, points: np.array = None) -> np.array:
        # initialize random centroids within the bounds of the border points

        if points is None: points = self.data

        min_x, min_y = np.min(points, axis=0)
        max_x, max_y = np.max(points, axis=0)

        centroids = [(min_x + np.random.random() * abs(max_x - min_x), 
                      min_y + np.random.random() * abs(max_y - min_y)) for i in range(k)]

        return np.array(centroids, dtype=np.float32)
    
    def init_smart_centroids(self, k: int, points: np.array = None) -> np.array:
        #kmeans++ version of centroid initialization

        if points is None: points = self.data

        points_i = np.arange(points.shape[0])
        centroids = np.empty((k, 2), dtype=np.float32)
        weights = None #in the 1st iteration, the weights of the points will be uniform
        
        c = 0
        
        for i in range(k):
            curr_centroid = points[np.random.choice(points_i, p=weights)] #pick a random point as a centroid (based on the weights of the points)
            centroids[i] = curr_centroid

            #the weights are calculated by dividing the squared euclidian distance of a point to a chosen centroid
            #by the sum of the distances between all points and the chosen centroid
            #after the 1st iteration (i.e. after the 1st centroid has been chosen),
            #the minimum distance between a point and all chosen centroids is chosen as the basis of the point's weight

            weights = np.min([self.calc_dist(curr_centroid, points, "squared") for curr_centroid in centroids], axis=0)
            weights /= np.sum(weights)
        
        return centroids
    
    def update_centroids(self, centroids: np.array, points: np.array, cluster_members: np.array) -> np.array:
        # update the centroid of a cluster after all points
        # have been assigned to a (new) centroid/cluster
        
        for i in range(len(centroids)):
            centroids[i] = np.sum(points[cluster_members[i]], axis=0) / np.sum(cluster_members[i])

        return centroids

    def cluster_data(self, k: int, points: np.array = None, pp: bool = True, runs: int = 1) -> np.array:
        # cluster the data points using the kmeans clustering method

        print(f"k-means clustering algorithm: {k} cluster(s), {runs} run(s).")

        if points is None: points = self.data

        cluster_members = None
        centroids = None
        best_run = None
        avg_intra_cluster_dist = np.Inf

        for run in range(runs):
            #print(f"Clustering Run {run+1}/{runs}...") 

            cluster_members_of_run = np.zeros((k, len(points)), dtype=bool)
            dists_to_centroids = np.empty((k, len(points)), dtype=np.float32)
            centroids_of_run = self.init_smart_centroids(k) if pp else self.init_random_centroids(k)

            points_i = np.arange(len(points))

            while True:
                # cluster until no point changes cluster membership (see break condition below)

                points_per_cluster_before = np.sum(cluster_members_of_run, axis=1)

                # calculate distance of every point to the current cluster
                for i, centroid in enumerate(centroids_of_run): dists_to_centroids[i] = self.calc_dist(centroid, points)

                # figure out which cluster a point is closest to
                closest_centroid_of_points = np.argmin(dists_to_centroids, axis=0)
                cluster_members_of_run[closest_centroid_of_points, points_i] = True
                
                points_per_cluster_after = np.sum(cluster_members_of_run, axis=1)

                # stop clustering if the sum of points assigned to every cluster
                # before and after the clustering is the same
                # (i.e. no point changed cluster membership)
                if np.all(points_per_cluster_after == points_per_cluster_before): break
                
                centroids_of_run = self.update_centroids(centroids_of_run, points, cluster_members_of_run)

            # calculate the current average intra-cluster distance
            # between the cluster's centroid and all its points
            curr_avg_intra_cluster_dist = np.sum(dists_to_centroids[cluster_members_of_run]) / len(points)

            if curr_avg_intra_cluster_dist < avg_intra_cluster_dist:
                # update the variables if the current clustering run
                # produced a smaller average intra-cluster distance
                cluster_members = cluster_members_of_run
                centroids = centroids_of_run
                avg_intra_cluster_dist = curr_avg_intra_cluster_dist
        
        return cluster_members, centroids

if __name__ == "__main__":
    # handling of command line features

    parser = argparse.ArgumentParser()
    parser.add_argument("--delimiter", "-d", metavar="D", type=str, help="specify the delimiter of the input file")
    parser.add_argument("--title", "-t", metavar="'TITLE'", type=str, help="title of the cluster plot")
    parser.add_argument("--out", "-o", metavar="FILE", type=str, help="save the cluster plot to a file")
    parser.add_argument("--runs", "-r", metavar="N", type=int, default=1, help="pick the best run (lowest average intra-cluster distance) out of x runs")
    parser.add_argument("--plusplus", "-pp", action="store_true", help="Run the kmeans++ version of the algorithm (better initialization of the centroids)")
    parser.add_argument("input_file", type=str, help="path to input file containing data points")
    parser.add_argument("k", type=int, help="number of clusters to form")

    args = parser.parse_args()

    clusterer = KMeans(args.input_file, args.delimiter)
    cluster_members, centroids = clusterer.cluster_data(args.k, pp=args.plusplus, runs=args.runs)
    clusterer.plot_clusters(cluster_members, centroids, title=args.title, output_file=args.out)