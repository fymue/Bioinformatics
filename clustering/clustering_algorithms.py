import matplotlib.pyplot as plt
import numpy as np
from sys import argv
from kneed import KneeLocator
from random import random, randint

def read_data(file, delimiter):
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

def validate_data(x, y, eps=None):
        #check if the input data is valid (same length, all numbers)
        valid = False
        if x != None and y != None:
            if len(x) == len(y): valid = True
            else: print("Invalid data structure! x and y must have the same length!")
        else:
            print("Please provide two arrays X and Y as data points!")

        if eps == -1:
            valid = False
            print("Epsilon value has not been set! Please choose an epsilon value or use the -auto option.")
        return valid

def plot_clusters(cluster_members, title, output_file, has_noise=False, centroids=None):
        #plot the points of the different clusters
        
        for i, cluster in enumerate(cluster_members):
            if len(cluster) == 0: continue
            x, y = [], []

            if has_noise and i == 0:
                lgnd = f"Noise points ({len(cluster)})"
            else:
                lgnd = f"Cluster {i+1} ({len(cluster)})"

            for pt1, pt2 in cluster: #separate the point coordinates of the current cluster
                x.append(pt1)
                y.append(pt2)
            plt.scatter(x, y, s=10, label=lgnd) #scatter the points of the current cluster (each cluster will have a different color)

        if centroids:
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

def calc_dist(pt1, pt2, method="reg"):
        #calculate the distance between two points (euclidian distance)
        a = (pt1[0] - pt2[0]) ** 2
        b = (pt1[1] - pt2[1]) ** 2

        if method == "squared": return a + b

        return np.sqrt(a + b)

class KMeans:
    def __init__(self,  k, x=None, y=None, input_file=None, runs=1, delimiter=" ", output_file=None, title="", pp=True, plot=False):

        if not x and not y and input_file:
            x, y = read_data(input_file, delimiter)

        self.x = x
        self.y = y
        self.k = k
        self.runs = runs
        self.delimiter = delimiter
        self.input_file = input_file
        self.output_file = output_file
        self.title = title
        self.pp = pp
        self.plot = plot

        self.valid = validate_data(x, y)
    
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
            curr_dist = calc_dist(point, centroid) #calculate distance of every point to every centroid
            if curr_dist < min_dist:
                min_dist = curr_dist #keep track of the minimum point-centroid distance
                closest_centroid = i

        return (min_dist, closest_centroid)
    
    def calc_dists(self, point, x, y, method):
        return [calc_dist(point, curr_point, method) for curr_point in zip(x, y)]# if point != curr_point]
    
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

    def cluster(self):
        #kmeans clustering algorithm
        if self.valid:

            x = self.x
            y = self.y
            k = self.k
            pp = self.pp
            runs = self.runs
            plot = self.plot

            print(f"k-means clustering algorithm: {k} cluster(s), {runs} run(s).")

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
                    mn_x, mx_x = np.floor(min(x)), np.ceil(max(x))
                    mn_y, mx_y = np.floor(max(y)), np.ceil(max(y))
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

            clusters, centroids = clustering_runs[best_run]

            if self.plot: plot_clusters(clusters, self.title, self.output_file, centroids=centroids)

            return clusters, centroids
        
        return None, None

class HierarchClustering:
    def __init__(self, k, x=None, y=None, input_file=None, delimiter=" ", output_file=None, title="", cdist_method="single", plot=False):

        if not x and not y and input_file:
            x, y = read_data(input_file, delimiter)

        self.x = x
        self.y = y
        self.k = k
        self.delimiter = delimiter
        self.input_file = input_file
        self.output_file = output_file
        self.title = title
        self.cdist_method = cdist_method
        self.plot = plot

        self.valid = validate_data(x, y)
    
    def calc_dist_matrix(self, x, y, mask): #calculate distances between all points (upper triangular is sufficient)
        return np.ma.array([[calc_dist(p1, p2) for p1 in zip(x, y)] for p2 in zip(x, y)], mask=mask)
    
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
            
    def cluster(self):
        #cluster the data using the agglomerative hiearchical clustering method
        if self.valid:

            x = self.x
            y = self.y
            k = self.k
            cdist_method = self.cdist_method
            
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

            if self.plot: plot_clusters(clusters, self.title, self.output_file)

            return clusters
        
        return None
        

class DBScan:
    def __init__(self, x=None, y=None, input_file=None, eps=-1, min_pts=4, delimiter=" ", output_file=None, title="", auto=False, plot=False):

        if not x and not y and input_file:
            x, y = read_data(input_file, delimiter)

        self.x = x
        self.y = y
        self.eps = eps
        self.min_pts = min_pts
        self.delimiter = delimiter
        self.input_file = input_file
        self.output_file = output_file
        self.title = title
        self.auto = auto
        self.plot = plot

        self.valid = validate_data(x, y, eps)

    def estimate_eps(self, x, y, dists, k):
        #estimate a proper value of epsilon
        #1. find the k nearest neighbors of every point
        #2. find the avg. distance of those k distances
        #3. sort the avg. distances and find the "knee point" in the curve

        key = lambda p1, p2: (p1, p2) if (p1, p2) in dists else (p2, p1)
        avg_k_dist = [np.mean(sorted([dists[key(p1, p2)] for p2 in zip(x, y) if p1 != p2])[:k]) for p1 in zip(x, y)]
        avg_k_dist.sort()
        knee_locator = KneeLocator(list(range(len(avg_k_dist))), avg_k_dist, curve="convex", direction="increasing")
        eps = knee_locator.knee_y

        return eps

    def region_query(self, curr_point, x, y, dists, eps):
        #determine the neighboring point of a point within the radius eps
        result = {}
        for point in zip(x, y):
            key = (curr_point, point)
            if key not in dists: key = tuple(reversed(key)) #distance matrix only contains upper triangular - make sure the key fits
            if dists[key] < eps: #if distance between current point and query point is < eps -> add it to result
                result[point] = None 

        return result
    
    def expand_cluster(self, x, y, seedlist, labels, cluster_members, cluster_id, dists, eps, min_pts):
        #if a point is a core point its part of a cluster; all points within its eps will be part of the cluster as well
        c = 0
        while c < len(seedlist): #loop, until the seedlist has been fully processed (might get extended during the process)
            point = tuple(seedlist.keys())[c]
            if labels[point] == "noise": #if point has been labeled "noise", but is in eps of a core point -> border point
                cluster_members[0].remove(point)
                labels[point] = "border"
            else:
                if labels[point] == None: #only process points that haven't been labeled yet 
                    neighbors = self.region_query(point, x, y, dists, eps)
                    if len(neighbors) >= min_pts: #if point is core point, extend the seedlist
                        labels[point] = "core"
                        for p in neighbors:
                            if p not in seedlist: seedlist[p] = None
                    else: labels[point] = "border"
            
            cluster_members[cluster_id].add(point) #add point to the current cluster (every point will either be "core"/"border")
            c += 1 #keep increasing the index for the seedlist
        return

    def calc_dist_matrix(self, x, y): #calculate distances between all points (upper triangular is sufficient)
        return {((x[i], y[i]), (x[j], y[j])) : calc_dist((x[i], y[i]), (x[j], y[j])) for i in range(len(x)) for j in range(i, len(x))}
    
    def init_labels(self, x, y): #initialize label lookup table for all points
        return {(x[i], y[i]) : None for i in range(len(x))}

    def cluster(self):
        #cluster the data using the DBScan algorithm
        if self.valid:

            x = self.x
            y = self.y
            min_pts = self.min_pts

            dists = self.calc_dist_matrix(x, y) #calculate the distances bewtween all the points

            if self.auto: eps = self.estimate_eps(x, y, dists, min_pts)
            else: eps = self.eps

            labels = self.init_labels(x, y)
            cluster_members = [set()] 
            cluster_id = 0

            for point in zip(x, y):
                if labels[point] == None: #only process unlabeled points (some will be labeled in expand_cluster())
                    seedlist = self.region_query(point, x, y, dists, eps) #get neighbor points within eps
                    if len(seedlist) >= min_pts: #if point is a "core" point, make new cluster and expand it
                        labels[point] = "core" 
                        cluster_id += 1
                        cluster_members.append({point}) 
                        self.expand_cluster(x, y, seedlist, labels, cluster_members, cluster_id, dists, eps, min_pts)
                    else:
                        labels[point] = "noise"
                        cluster_members[0].add(point)

            if self.plot: plot_clusters(cluster_members, self.title, self.output_file, has_noise=True)

            return cluster_members
        
        return None

#handling of command line features
valid_global_commands = {"-d", "-title", "-out", "-plot", "-hierarchical", "-kmeans", "-dbscan"}
valid_hierarch_commands = {"-method"}
valid_dbscan_commands = {"-eps", "-minpts", "-auto"}
valid_kmeans_commands = {"-runs", "-pp"}
help_commands = {"--help", "-help", "-h"}

def is_valid_command(args, method):
    if method == "-kmeans":
        method_commands = valid_kmeans_commands
    elif method == "-hierarchical":
        method_commands = valid_hierarch_commands
    elif method == "-dbscan":
        method_commands = valid_dbscan_commands
    else:
        method_commands = set()

    all_valid_commands = valid_global_commands | help_commands | method_commands

    if all(inp in all_valid_commands for inp in args[1:len(args)-1] if inp[0] == "-"):
        return True
    else:
        return False

def print_help(method):
    if method == "-kmeans":
        print("Usage: clustering_algorithms.py -kmeans [OPTIONS] inputfile k\n")
        print("k stands for the number of clusters.")
        print("\nOptions:\n")
        print("-runs value\t\t\tpick the best run (lowest average intra-cluster distance) out of x runs")
        print("-pp\t\t\t\tRun the kmeans++ version of the algorithm (better initialization of the centroids)")
    
    elif method == "-hierarchical":
        print("Usage: clustering_algorithms.py -hierarchical [OPTIONS] inputfile k\n")
        print("k stands for the number of clusters.")
        print("\nOptions:\n")
        print('-method "method"\t\tinter-cluster distance calculation method (Options: "single" (default), "complete", "average")')
    
    elif method == "-dbscan":
        print("Usage: clustering_algorithms.py -dbscan -eps value | -auto [OPTIONS] inputfile\n")
        print("Options:\n")
        print("-eps value\t\tset the epsilon")
        print("-minpts value\t\tset the minimum points within epsilon (standard: 4)")
        print("-auto\t\t\tlet the program estimate an appropriate epsilon value")

    print("\nThe input file should contain the x and y coordinates of a data point in the same line.")
    print("The delimiter can be specified.\n")

    print("Input-/Output-related Options:\n")
    print('-d "delimiter"\t\t\tset the delimiter for the input file (standard: space)')
    print('-title "title"\t\t\tset the title of the cluster plot')
    print("-out filename\t\t\tsave the plot in a file")
    print("-plot\t\t\tdisplay the plot on the screen\n")

def parse_command():

    def get_args_val(arg, val):
        if arg in args: val = argv[args[arg]+1]
        return val

    args = {arg : i for i, arg in enumerate(argv)}
    targs = len(argv)
    method = argv[1] if targs > 1 else None
    valid = is_valid_command(argv, method)
    
    if targs <=2 or not valid:
        print('Usage: clustering_algorithms.py -algorithm [OPTIONS] inputfile (k)\n')
        print("Available algorithms: -kmeans, -hierarchical, -dbscan\n")
        print("Enter clustering_algorithms.py -algorithm --help, -help or -h to display usage help\n")

    else:
        if targs == 3 and argv[-1] in help_commands:
            print_help(argv[-2])

        elif targs >= 4 and valid:

            delimiter = get_args_val("-d", " ")
            runs = int(get_args_val("-runs", 1))
            title = get_args_val("-title", "")
            output_file = get_args_val("-out", None)
            pp = True if "-pp" in args else False
            plot = True if "-plot" in args else False

            if method == "-dbscan":
                input_file = argv[-1]
                min_pts = int(get_args_val("-minpts", 4))
                auto = True if "-auto" in args else False
                eps = None if auto else float(get_args_val("-eps", -1))

                data = DBScan(eps=eps, min_pts=min_pts, input_file=input_file, delimiter=delimiter, \
                        output_file=output_file, title=title, auto=auto, plot=plot)
                
                clusters = data.cluster()

            elif method == "-hierarchical":
                input_file = argv[-2]
                k = int(argv[-1])
                cdist_method= get_args_val("-method", "single")

                data = HierarchClustering(k, input_file=input_file, delimiter=delimiter, \
                        output_file=output_file, title=title, cdist_method=cdist_method, plot=plot)
                
                clusters = data.cluster()
            
            elif method == "-kmeans":
                input_file = argv[-2]
                k = int(argv[-1])

                data = KMeans(k, input_file=input_file, runs=runs, delimiter=delimiter, \
                        output_file=output_file, title=title, pp=pp, plot=plot)

                clusters, centroids = data.cluster()

if __name__ == "__main__": parse_command()
