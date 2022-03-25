import matplotlib.pyplot as plt
import numpy as np
from sys import argv

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
            plt.scatter(x, y, s=10, label= f"Cluster {i} ({len(cluster)})") #scatter the points of the current cluster (each cluster will have a different color)

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
    
    def cluster(self, x, y, k, cdist_method):
        #cluster the data using the agglomerative hiearchical clustering method
        clusters = {}

        return clusters

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
    print('-method "method"\t\t\t\tinter-cluster distance calculation method (Options: "single" (default), "complete", "average", "centroid")')

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
        method= int(get_args_val("-method", "single"))
        title = get_args_val("-title", "")
        output_file = get_args_val("-out", None)

        hierach_clustering = HierarchClustering(input_file, k, delimiter, output_file, title, method)

if __name__ == "__main__": parse_command()