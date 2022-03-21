import matplotlib.pyplot as plt
from math import sqrt
from numpy import mean
from sys import argv
from kneed import KneeLocator

class DBScan:
    def __init__(self, input_file, eps, min_pts, delimiter, output_file, title, auto):
        x, y = self.read_data(input_file, delimiter) #read the data from the input file
        valid, x, y = self.validate_data(x, y, eps) #validate the data (shape correct etc.)
        if valid:
            dists = self.calc_dist_matrix(x, y) #calculate the distances bewtween all the points
            if auto: eps = self.estimate_eps(x, y, dists, min_pts)
            clusters = self.dbscan(x, y, eps, min_pts, dists)
            self.plot_clusters(clusters, title, output_file)

    def estimate_eps(self, x, y, dists, k):
        #estimate a proper value of epsilon
        #1. find the k nearest neighbors of every point
        #2. find the avg. distance of those k distances
        #3. sort the avg. distances and find the "knee point" in the curve

        key = lambda p1, p2: (p1, p2) if (p1, p2) in dists else (p2, p1)
        avg_k_dist = [mean(sorted([dists[key(p1, p2)] for p2 in zip(x, y) if p1 != p2])[:k]) for p1 in zip(x, y)]
        avg_k_dist.sort()
        knee_locator = KneeLocator(list(range(len(avg_k_dist))), avg_k_dist, curve="convex", direction="increasing")
        eps = knee_locator.knee_y

        #plt.plot(avg_k_dist)
        #plt.show()
        #plt.close()

        return eps

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

        return x,y

    def validate_data(self, x, y, eps):
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
        return valid, x, y
    
    def plot_clusters(self, cluster_members, title, output_file):
        #plot the points of the different clusters
        
        #labels = ["core", "border", "noise"]
        for i, cluster in enumerate(cluster_members):
            x, y = [], []
            if i == 0: lgnd = f"Noise points ({len(cluster)})"
            else: lgnd = f"Cluster {i} ({len(cluster)})"
            for pt1, pt2 in cluster: #separate the point coordinates of the current cluster
                x.append(pt1)
                y.append(pt2)
            plt.scatter(x, y, s=10, label=lgnd) #scatter the points of the current cluster (each cluster will have a different color)

        plt.title(title)
        plt.legend(loc="best")
        if output_file != None: plt.savefig(output_file, dpi=300)
        plt.show()
        plt.close()

    def calc_dist(self, pt1, pt2):
        #calculate the distance between two points (euclidian distance)
        a = abs(pt1[0] - pt2[0]) ** 2
        b = abs(pt1[1] - pt2[1]) ** 2
        return sqrt(a + b)

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
        return {((x[i], y[i]), (x[j], y[j])) : self.calc_dist((x[i], y[i]), (x[j], y[j])) for i in range(len(x)) for j in range(i, len(x))}
    
    def init_labels(self, x, y): #initialize label lookup table for all points
        return {(x[i], y[i]) : None for i in range(len(x))}

    def dbscan(self, x, y, eps, min_pts, dists):
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

        return cluster_members

#handling of command line features
valid_commands = {"-d", "-eps", "-minpts", "-auto", "-title", "-out"}
help_commands = {"--help", "-help", "-h"}

def valid_command(args): return True if all(inp in valid_commands | help_commands for inp in args[1:len(args)-1] if inp[0] == "-") else False

def print_help():
    print("Usage: dbscan.py -eps value | -auto [OPTIONS] inputfile\n")
    print("The input file should contain the x and y coordinates of a data point in the same line.")
    print("The delimiter can be specified.\n")
    print("Options:\n")
    print('-d "delimiter"\t\tset the delimiter for the input file (standard: space)')
    print("-eps value\t\tset the epsilon")
    print("-minpts value\t\tset the minimum points within epsilon (standard: 4)")
    print("-auto\t\t\tlet the program estimate an appropriate epsilon value")
    print('-title "title"\t\tset the title of the cluster plot')
    print("-out filename\t\tsave the plot in a file")

def parse_command():

    def get_args_val(arg, val):
        if arg in args: val = argv[args[arg]+1]
        return val

    args = {arg : i for i, arg in enumerate(argv)}
    targs = len(argv)
    valid = valid_command(argv)
    
    if targs == 1 or not valid:
        print("Usage: dbscan.py [-eps value|-auto] [OPTIONS] inputfile\n")
        print("use --help, -help or -h to display usage help\n")

    elif targs == 2 and argv[1] in help_commands: print_help()
        
    elif targs >= 3 and valid:
        input_file = argv[-1]
        delimiter = get_args_val("-d", " ")
        min_pts = int(get_args_val("-minpts", 4))
        title = get_args_val("-title", "")
        output_file = get_args_val("-out", None)
        auto = True if "-auto" in args else False
        if auto: eps = None
        else: eps = float(get_args_val("-eps", -1))

        #print(input_file, eps, min_pts, delimiter, output_file, title, auto)
        dbscan = DBScan(input_file, eps, min_pts, delimiter, output_file, title, auto)

if __name__ == "__main__": parse_command()