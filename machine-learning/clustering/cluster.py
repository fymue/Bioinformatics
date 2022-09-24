#!/usr/bin/env python3

import matplotlib.pyplot as plt, numpy as np, argparse
from kneed import KneeLocator
from dbscan import DBScan
from kmeans import KMeans
from hierarch_clustering import HierarchClustering

if __name__ == "__main__":
    # command line features

    # global args
    parser = argparse.ArgumentParser()
    parser.add_argument("--delimiter", "-d", metavar="D", type=str, help="specify the delimiter of the input file")
    parser.add_argument("--title", "-t", metavar="'TITLE'", type=str, help="title of the cluster plot")
    parser.add_argument("--out", "-o", metavar="FILE", type=str, help="save the cluster plot to a file (default: show plot in popup window)")

    # specific args for all clustering algorithms
    subparser = parser.add_subparsers(dest="algorithm", help="Clustering algorithm/method to use")

    dbscan_parser = subparser.add_parser("dbscan")
    dbscan_parser.add_argument("input_file", type=str, help="path to input file containing data points")
    dbscan_parser.add_argument("--epsilon", "-e", metavar="N", type=float, help="set epsilon value")
    dbscan_parser.add_argument("--minpts", "-mp", metavar="N", default=4, type=int, help="set the minimum points within epsilon (default: 4)")
    dbscan_parser.add_argument("--auto", "-a", action="store_true", help="let the program estimate an appropriate epsilon value")

    kmeans_parser = subparser.add_parser("kmeans")
    kmeans_parser.add_argument("input_file", type=str, help="path to input file containing data points")
    kmeans_parser.add_argument("--runs", "-r", metavar="N", type=int, default=1, help="pick the best run (lowest average intra-cluster distance) out of N runs")
    kmeans_parser.add_argument("--plusplus", "-pp", action="store_true", help="Run the kmeans++ version of the algorithm (better initialization of the centroids)")
    kmeans_parser.add_argument("k", type=int, help="number of clusters to form")

    hierarch_parser = subparser.add_parser("hierarchical")
    hierarch_parser.add_argument("input_file", type=str, help="path to input file containing data points")
    hierarch_parser.add_argument("--method", "-m", metavar="METHOD", type=str, help="clustering method (average (default), single, complete")
    hierarch_parser.add_argument("k", type=int, help="number of clusters to form")

    # parse the arguments and run the specified clustering algorithm
    args = parser.parse_args()
    
    if args.algorithm == "kmeans":
        clusterer = KMeans(args.input_file, args.delimiter)
        cluster_members, centroids = clusterer.cluster_data(args.k, pp=args.plusplus, runs=args.runs)
        clusterer.plot_clusters(cluster_members, centroids, title=args.title, output_file=args.out)
    elif args.algorithm == "hierarchical":
        clusterer = HierarchClustering(args.input_file, args.delimiter, args.method)
        cluster_members = clusterer.cluster_data(args.k)
        clusterer.plot_clusters(cluster_members, title=args.title, output_file=args.out)
    elif args.algorithm == "dbscan":
        clusterer = DBScan(args.input_file, args.delimiter)
        cluster_members = clusterer.cluster_data(args.minpts, eps=args.epsilon, auto=args.auto)
        clusterer.plot_clusters(cluster_members, title=args.title, output_file=args.out)
