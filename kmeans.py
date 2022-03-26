import matplotlib.pyplot as plt
import numpy as np
from math import sqrt, ceil, floor
from sys import argv
from random import random, randint

class KMeans:
    def __init__(self, input_file, k, runs, delimiter, output_file, title, pp):
        x, y = self.read_data(input_file, delimiter)
        clusters, centroids = self.kmeans(x, y, k, pp, runs)
        self.plot_clusters(clusters, centroids, title, output_file)
    
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
    
    def plot_clusters(self, cluster_members, centroids, title, output_file):
        #plot the points of the different clusters
        
        for i, cluster in enumerate(cluster_members):
            if len(cluster) == 0: continue
            x, y = [], []
            for pt1, pt2 in cluster: #separate the point coordinates of the current cluster
                x.append(pt1)
                y.append(pt2)
            plt.scatter(x, y, s=10, label= f"Cluster {i} ({len(cluster)})") #scatter the points of the current cluster (each cluster will have a different color)

        x, y = [], []
        for pt1, pt2 in centroids:
            x.append(pt1)
            y.append(pt2)
        plt.scatter(x, y, s=10, label=f"Centroids ({len(centroids)})") #also plot the centroids for reference

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

        return sqrt(a + b)
    
    def new_centroid(self, mx_x, mx_y): return (random() * mx_x, random() * mx_y) #generate a random point (centroid)

    def adjust_centroid(self, cluster_members):
        #calculate a new centroid (average of all points in a cluster)
        x, y = [], []
        for (pt1, pt2) in cluster_members:
            x.append(pt1)
            y.append(pt2)
        return (np.mean(x), np.mean(y)) 
    
    def find_closest_centroid(self, point, centroids, min_dist, closest_centroid):
        for i, centroid in enumerate(centroids):
            curr_dist = self.calc_dist(point, centroid) #calculate distance of every point to every centroid
            if curr_dist < min_dist:
                min_dist = curr_dist #keep track of the minimum point-centroid distance
                closest_centroid = i

        return (min_dist, closest_centroid)
    
    def calc_dists(self, point, x, y, method):
        return [self.calc_dist(point, curr_point, method) for curr_point in zip(x, y)]# if point != curr_point]
    
    def find_smart_centroids(self, k, x, y):
        #kmeans++ version of centroid initialization

        points = list(zip(x, y))
        points_i = np.arange(len(points))
        centroids = []
        weights = None #in the 1st iteration, the weights of the points will be uniform

        while len(centroids) < k:
            curr_centroid = points[np.random.choice(points_i, p=weights)] #pick a random point as a centroid (based on the weights of the points)
            centroids.append(curr_centroid)

            #the weights are calculated by dividing the squared euclidian distance of a point to a chosen centroid
            #by the sum of the distances between all points and the chosen centroid
            #after the 1st iteration (i.e. after the 1st centroid has been chosen),
            #the minimum distance between a point and all chosen centroids is chosen as the basis of the point's weight

            weights = np.min(np.array([self.calc_dists(curr_centroid, x, y, "squared") for curr_centroid in centroids]), axis=0)
            weights /= np.sum(weights)
        
        return centroids  

    def kmeans(self, x, y, k, pp, runs):
        #kmeans clustering algorithm
        print(f"k-means clustering algorithm: {k} cluster(s), {runs} run(s).")
        valid, x, y = self.validate_data(x, y) #validate the data (shape etc.)
        if valid:
            
            clustering_runs = [] #collect the clustering results of all runs here
            total_avg_cdist = [] #collect the average intra-cluster distance of every point to its centroid here (for optimization)
            
            for run in range(runs):
                print(f"Clustering Run {run+1}/{runs}...", end="\r") 
                member_change = True
                cluster_members = [set() for i in range(k)] #points belonging to the respective clusters will go here

                if pp:
                    centroids = self.find_smart_centroids(k, x, y) #generate the centroids using the kmeans++ version of centroid initialization
                else:
                    #if kmeans++ centroid initialization is not supposed to be done, place the centroids randomly 
                    #make sure that the initial random x and y coordinates are within the borders of the most outside points
                    mn_x, mx_x = floor(min(x)), ceil(max(x))
                    mn_y, mx_y = floor(max(y)), ceil(max(y))
                    x_range = randint(mn_x, mx_x)
                    y_range = randint(mn_y, mx_y)

                    centroids = [self.new_centroid(x_range, y_range) for _ in range(k)] #generate k randomly-placed centroids

                curr_avg_cdist = [0] * k #keep track of the avg. intra-cluster distance averaged over all clusters of a run
                cluster_dists = {point : (None, None) for point in zip(x, y)} #keep track of the current cluster and distance to closest centroid of each point
                
                while member_change: #cluster until no point changes cluster membership
                    member_change = False
                    for point in zip(x, y):
                        min_dist, closest_cluster = self.find_closest_centroid(point, centroids, 10e18, None)
                        curr_cluster = cluster_dists[point][1]

                        if closest_cluster != curr_cluster: #check if current point has to change cluster membership
                            member_change = True
                            cluster_members[closest_cluster].add(point) #add point to new cluster

                            if curr_cluster != None:
                                cluster_members[curr_cluster].remove(point) #remove point from old cluster

                            cluster_dists[point] = min_dist, closest_cluster #keep track of current cluster of current point
                    
                    for c, members in enumerate(cluster_members):
                        if len(members) != 0:
                            #adjust the coordinates of the centroids
                            centroids[c] = self.adjust_centroid(members)

                #optimization calculations
                for point in cluster_dists:
                    #for every cluster, calculate the avg. distance bewtween every member point and the centroid
                    curr_dist_to_cluster, curr_cluster = cluster_dists[point]
                    curr_avg_cdist[curr_cluster] += curr_dist_to_cluster
                
                n = k
                for c, members in enumerate(cluster_members):
                    pts_per_cluster = len(members)
                    if pts_per_cluster == 0:
                        n -= 1
                    else:
                        curr_avg_cdist[c] /= pts_per_cluster
                
                #calculate the avg. of all intra-cluster distance averages
                total_avg_cdist.append(sum(curr_avg_cdist) / n)
                clustering_runs.append((cluster_members, centroids))

            #find and plot the run with the min. avg. intra-cluster distance 
            min_dist, best_run = min((avg_dist, i) for i, avg_dist in enumerate(total_avg_cdist))
            print("\nDone!")

            return clustering_runs[best_run]

        return None

#handling of command line features
valid_commands = {"-d", "-runs", "-title", "-out", "-pp"}
help_commands = {"--help", "-help", "-h"}

def valid_command(args): return True if all(inp in valid_commands | help_commands for inp in args[1:len(args)-1] if inp[0] == "-") else False

def print_help():
    print("Usage: kmeans.py [OPTIONS] inputfile k\n")
    print("k stands for the number of clusters.")
    print("The input file should contain the x and y coordinates of a data point in the same line.")
    print("The delimiter can be specified.\n")
    print("Options:\n")
    print('-d "delimiter"\t\t\tset the delimiter for the input file (standard: space)')
    print("-runs value\t\t\tpick the best run (lowest average intra-cluster distance) out of x runs")
    print('-title "title"\t\t\tset the title of the cluster plot')
    print("-out filename\t\t\tsave the plot in a file")
    print("-pp\t\t\t\tRun the kmeans++ version of the algorithm (better initialization of the centroids)")

def parse_command():

    def get_args_val(arg, val):
        if arg in args: val = argv[args[arg]+1]
        return val

    args = {arg : i for i, arg in enumerate(argv)}
    targs = len(argv)
    valid = valid_command(argv)
    
    if targs == 1 or not valid:
        print("Usage: kmeans.py [OPTIONS] inputfile k\n")
        print("use --help, -help or -h to display usage help\n")

    elif targs == 2 and argv[1] in help_commands: print_help()
        
    elif targs >= 3 and valid:
        input_file = argv[-2]
        k = int(argv[-1])
        delimiter = get_args_val("-d", " ")
        runs = int(get_args_val("-runs", 1))
        title = get_args_val("-title", "")
        output_file = get_args_val("-out", None)
        pp = True if "-pp" in args else False

        kmeans = KMeans(input_file, k, runs, delimiter, output_file, title, pp)

if __name__ == "__main__": parse_command()