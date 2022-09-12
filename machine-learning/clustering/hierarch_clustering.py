import matplotlib.pyplot as plt
import numpy as np
from sys import argv
from typing import List, Tuple

class HierarchClustering:
    # perform hierachical clustering of data points

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
    
    def calc_dist(self, pt_1: np.array, pt_2: np.array, method: str ="reg") -> float:
        #calculate the euclidian distance between two points

        a = abs(pt_1[0] - pt_2[0]) ** 2
        b = abs(pt_1[1] - pt_2[1]) ** 2

        if method == "squared": return a + b

        return np.sqrt(a + b)
    
    def calc_dist_matrix(self, data: np.array, method: str = "reg") -> np.array:
        #calculate distances between all points (upper triangular is sufficient)

        n = data.shape[0] # total number of points
        dist_matrix = np.empty((n, n), dtype=np.float32)

        for row in range(n):
            for col in range(row, n):
                pt_1 = data[row]
                pt_2 = data[col]
                dist_matrix[row, col] = self.calc_dist(pt_1, pt_2, method)

        return dist_matrix
    
    def cluster_data(self, data: np.array, k: int) -> List[Tuple[float, float]]:
        # cluster the data points using hierarchical clustering

        if self.data is not None: data = self.data

        # TODO: figure out how to efficiently store and update cluster members/ distance matrix

        cluster_members = None

        dists = self.calc_dist_matrix(data)

        while k < data.shape[0]:
            # cluster, until all points are part of only the initial k clusters



            k += 1

"""
class HierarchClustering:
    def __init__(self, input_file, k, delimiter, output_file, title, cdist_method):
        x, y = self.read_data(input_file, delimiter)
        clusters = self.cluster(x, y, k, cdist_method)
        self.plot_clusters(clusters, title, output_file)
    
    def read_data(self, file, delimiter):
        x = []
        y = []
        with open(file, "r") as fin:
            for l in fin:
                l = l.rstrip()
                if not l: continue
                l = l.split(delimiter)
                if len(l) != 2: return None, None
                x.append(float(l[0]))
                y.append(float(l[1]))

        return x, y
    
    
    def validate_data(self, x, y):
        #check if the input data is valid (same length, all numbers)
        valid = False
        if x != None and y != None:
            if len(x) == len(y): valid = True
            else: print("Invalid data structure! x and y must have the same length!")
        else:
            print("Please provide two arrays X and Y as data points!")

        return valid, x, y
    
    def plot_clusters(self, cluster_members, title, output_file):
        #plot the points of the different clusters
        
        for i, cluster in enumerate(cluster_members):
            if len(cluster) == 0: continue
            x, y = [], []
            for pt1, pt2 in cluster: #separate the point coordinates of the current cluster
                x.append(pt1)
                y.append(pt2)
            plt.scatter(x, y, s=10, label= f"Cluster {i+1} ({len(cluster)})") #scatter the points of the current cluster (each cluster will have a different color)

        plt.title(title)
        plt.legend(loc="best")
        if output_file != None: plt.savefig(output_file, dpi=300)
        plt.show()
        plt.close()

    def calc_dist(self, pt1, pt2, method="reg"):
        #calculate the distance between two points (euclidian distance)
        a = abs(pt1[0] - pt2[0]) ** 2
        b = abs(pt1[1] - pt2[1]) ** 2

        if method == "squared": return a + b

        return np.sqrt(a + b)
    
    def calc_dist_matrix(self, x, y, mask): #calculate distances between all points (upper triangular is sufficient)
        return np.ma.array([[self.calc_dist(p1, p2) for p1 in zip(x, y)] for p2 in zip(x, y)], mask=mask)
    
    def merge_clusters(self, dists, clusters, row, col, cdist_method, masked_rows):
        
        new_cluster = clusters[row] | clusters[col] #collect all the points of the new cluster

        #make a subset consisting of the distances of all these points to the other points (rows)
        sub = dists[list(new_cluster)] 

        #figure out which row makes more sense to be masked (ignored) and which to be updated
        update, mask = (row, col) if len(clusters[row]) > len(clusters[col]) else (col, row)

        #collapse the subset according to the cluster distance method specified by the user
        if cdist_method == "single":
            sub = np.ma.min(sub, axis=0)

        elif cdist_method == "complete":
            sub = np.ma.max(sub, axis=0)
        
        elif cdist_method == "average":
            sub = np.ma.mean(sub, axis=0)

        #update and mask the correct distance matrix values
        for i in range(len(sub)):
            if not dists.mask[update, i] and sub[i]: dists[update, i] = sub[i]
            if not dists.mask[i, update] and sub[i]: dists[i, update] = sub[i]
            dists[mask, i] = np.ma.masked
            dists[i, mask] = np.ma.masked

        masked_rows.add(mask) #add the row (cluster) to be ignored later

        #add the new members to the cluster
        #(to avoid errors the members of the masked clusters have to be added as well;
        #these (duplicate) clusters will be ignored later)
        clusters[mask] = new_cluster
        clusters[update] = new_cluster
    
    def print_matrix(self, arr):
        #function for better-looking printing of a (masked) matrix
        for row in arr:
            for el in row:
                if type(el) != np.float64: print(" -- ", end=" ")
                else:
                    print(f"{el: 4.2f}", end = " ")
            print()
            
    def cluster(self, x, y, k, cdist_method):
        #cluster the data using the agglomerative hiearchical clustering method

        #make a mask for the distance matrix (we only care about the (lower) triangular matrix (excluding diagonal))
        mask = np.array([[False if i > j else True for j in range(len(x))] for i in range(len(x))])

        #calculate the distance matrix w/ all distances between all points
        dists = self.calc_dist_matrix(x, y, mask) 

        clusters = [{i} for i in range(len(x))] #keep track of the clusters (beginning: every point = 1 cluster)
        masked_rows = set() #keep track of rows (points) which have been merged into a different cluster

        mx_merges = len(x) - k
        merge_c = 0

        while merge_c < mx_merges: #merge clusters until only k clusters are left

            #find the two clusters (row and col indices) which currently have smallest distance
            row, col = np.unravel_index(dists.argmin(), dists.shape) 

            #merge the clusters and update the distance matrix
            self.merge_clusters(dists, clusters, row, col, cdist_method, masked_rows)

            merge_c += 1

        #fill every cluster with the actual points it contains (ignore the masked rows/clusters)
        clusters = [{(x[el], y[el]) for el in clusters[i]} for i in range(len(clusters)) if i not in masked_rows]

        return clusters
"""

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

        hierach_clustering = HierarchClustering(input_file, k, delimiter, output_file, title, method)

if __name__ == "__main__": parse_command()