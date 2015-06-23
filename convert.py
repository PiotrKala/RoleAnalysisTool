#!/usr/bin/env python
 
import sys
import argparse
import networkx as nx
import community
import json
from networkx.readwrite import json_graph
 
 
def graphmltojson(graphfile, outfile):
    """
    Converts GraphML file to json while adding communities/modularity groups
    using python-louvain. JSON output is usable with D3 force layout.
    Usage:
    >>> python convert.py -i mygraph.graphml -o outfile.json
    """
    
    G = nx.read_gml(graphfile)      
 
    #finds best community using louvain
    partition = community.best_partition(G)
 
    node_link = json_graph.node_link_data(G)
    output = str(node_link).replace('u\'','"').replace('\'','"').replace('False', '"False"')
    json_g = json.loads(output)
    del json_g['directed']
    del json_g['multigraph']
    del json_g['graph']
    
    # Write to file
    fo = open(outfile, "w")
    fo.write(json.dumps(json_g))
    fo.close()
 
if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Convert from GraphML to json. ')
    parser.add_argument('-i','--input', help='Input file name (graphml)',required=True)
    parser.add_argument('-o','--output', help='Output file name/path',required=True)
    args = parser.parse_args()
    graphmltojson(args.input, args.output)
