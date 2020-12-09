# libraries
import json
import random 
import networkx as nx
import copy
import sys

# self made imports
import myconstants
from precinct import Precinct
from cluster import Cluster

# simluate algo params/job params
idealPopulation = 3000
populationVariance = 0.3 
numIterations = 20
precinctGraphJSONFile = "test.json"
requestedNumDistricts = 5
compactness = '' 

# todo
# figure out compactness 
# take the inputs for vars - how to represent demographics?
# test on real data
# any debug?

globalPrecinctDict = {}
globalClusterList = []

tempClusters = [] # temp var to help keep track of which 2 clusters we're current merging

# helper fxn to print debug info, used to print list info for precinct/neighbor lists
def stringifyList(lst: list):
    printStr = "["
    for i in lst: 
        printStr = printStr + i.__str__() + ", "
    printStr = printStr[:len(printStr) - 2] + "]"
    return printStr

# remove c2 from c1's neighbor cluster list
def removeNeighbor(c1: Cluster, c2: Cluster):
    currList = c1.getClusterNeighborsList()
    currList.remove(c2)
    c1.setClusterNeighborsList(currList)

# add c2 into c1's neighbor cluster list, won't do anything if it's already in
def addNeighbor(c1: Cluster, c2: Cluster):
    currList = c1.getClusterNeighborsList()
    currList.append(c2)
    currList = list(set(currList))
    c1.setClusterNeighborsList(currList)

# merges two clusters for seed districting, updates necessary info within clusters and its neighbors
def mergeClusters(c1: Cluster, c2: Cluster):
    # merge precincts
    c1.setClusterPrecinctsList(c1.getClusterPrecinctsList() + c2.getClusterPrecinctsList()) # union of the two cluster's precincts
    
    # merge neighbors
    c1.setClusterID(c1.getClusterPrecinctsList()[0].getPrecinctID()) # set id to first precinct id
    newNeighborsList = list(set(c1.getClusterNeighborsList() + c2.getClusterNeighborsList())) # combine neighbors list, remove duplicates and themselves
    newNeighborsList.remove(c1)
    newNeighborsList.remove(c2)
    c1.setClusterNeighborsList(newNeighborsList) # combine neighbors while removing duplicates, remove any occurrences of precincts

    # remove merged cluster
    globalClusterList.remove(c2)

    # update neighbors - remove merged cluster from neighbors' neighborlist (remove c1 from c2's neighbor list first)
    removeNeighbor(c2, c1)
    for n in c2.getClusterNeighborsList():
        removeNeighbor(n, c2) # replace instances of c2 with c1
        addNeighbor(n, c1)

# combines two clusters together for uc30
def combineClusters(c1: Cluster, c2: Cluster):
    newCluster = Cluster()
    newCluster.setClusterPrecinctsList(c1.getClusterPrecinctsList() + c2.getClusterPrecinctsList())
    newCluster.setClusterID(c1.getClusterID()) # no need to update neighbors, we'll be creating edges from scratch

    edgeList = createEdgeList(newCluster)
    newCluster.setClusterEdgeList(edgeList)

    return newCluster

# creates edge list based on cluster's precincts' neighbors
def createEdgeList(c: Cluster):
    edgeList = []
    precinctList = c.getClusterPrecinctsList()

    for p in precinctList:
        for n in p.getPrecinctNeighborsList():
            if n in precinctList: 
                edgeList.append(tuple((p.getPrecinctID(), n.getPrecinctID())))
    edgeList = list(set(tuple(sorted(edge)) for edge in edgeList))
    return edgeList

# use BFS to generate a ST
def generateSpanningTree(c: Cluster):
    currPrecinctList = c.getClusterPrecinctsList()
    visited = [False] * (len(currPrecinctList)) # mark initial precincts as unvisited, set as dict instead
    queue = [] # queue for BFS

    randomStartingPrecinct = random.sample(currPrecinctList, 1) # random.sample returns a list
    currPrecinctIndex = currPrecinctList.index(randomStartingPrecinct[0])
    visited[currPrecinctIndex] = True
    queue.append(randomStartingPrecinct[0])

    spanningTreeEdgeList = []
    
    # while queue isn't empty 
    while queue: 
        currPrecinct = queue.pop(0)

        for n in currPrecinct.getPrecinctNeighborsList(): 
            if n in currPrecinctList:
                currPrecinctIndex = currPrecinctList.index(n)
                if visited[currPrecinctIndex] == False: 
                    queue.append(n)
                    visited[currPrecinctIndex] = True
                    spanningTreeEdgeList.append(tuple((currPrecinct.getPrecinctID(), n.getPrecinctID())))

    return spanningTreeEdgeList

# returns the list of associated precincts based on provided edge list
def recreatePrecincts(edgeList: list):
    tempPrecinctSet = set()
    for t in edgeList: 
        tempPrecinctSet.update(t)
    retPrecinctList = list()
    for id in list(tempPrecinctSet):
        retPrecinctList.append(globalPrecinctDict[id])
    return retPrecinctList

# helper method to find the connected components
def generateSubgraphs(edgeList: list, edgeToRemove: tuple):
    precinctList = recreatePrecincts(edgeList)
    copyEdgeList = copy.deepcopy(edgeList)
    copyEdgeList.remove(edgeToRemove)
    G = nx.Graph()
    G.add_edges_from(copyEdgeList)

    for p in precinctList: 
        G.add_node(p.getPrecinctID())

    return list(nx.connected_components(G))

# helper fxn for calc'ing total population of a subgraph
def calculateSubgraphPopulation (precinctIDs: set):
    totalPop = 0
    for precinctID in precinctIDs:
        currPrecinct = globalPrecinctDict[precinctID]
        totalPop = totalPop + currPrecinct.getPrecinctPopulation()
    
    return totalPop

# returns boolean based on population and compactness conditions
def isGraphAcceptable(subgraph: set):
    populationFlag = False
    compactnessFlag = True
    # condition 1: abs(population difference) < user specified population difference 
    subgraphPopulation = calculateSubgraphPopulation(subgraph)
    idealPopulationLowerBound = idealPopulation - populationVariance * idealPopulation
    idealPopulationUpperBound = idealPopulation + populationVariance * idealPopulation

    if subgraphPopulation > idealPopulationLowerBound and subgraphPopulation < idealPopulationUpperBound:
        populationFlag = True

    # condition 2: compactness score lower than user specified compactness
    # on hold til kelly approves of perimeter algo

    return populationFlag and compactnessFlag

# given list of tuples from ST, find acceptability from subgraphs and generate feasible set of edges to cut
def findValidEdges(edgeList: list):
    validEdges = []
    acceptableEdges = []
    for edgeToRemove in edgeList: # check every edge in spanning tree list
        subgraphs = generateSubgraphs(edgeList, edgeToRemove)

        subgraph1 = subgraphs[0]
        subgraph2 = subgraphs[1]
        
        subgraph1_acceptable = isGraphAcceptable(subgraph1)
        subgraph2_acceptable = isGraphAcceptable(subgraph2)

        # if both tests pass acceptability test, add to validEdges
        if subgraph1_acceptable and subgraph2_acceptable: 
            validEdges.append(edgeToRemove)

        # if one or both fail, see if the edge will better balance the population between subgraphs, if so add to acceptableEdges
        else:
            subgraphPopulationDiff = abs(calculateSubgraphPopulation(subgraph1) - calculateSubgraphPopulation(subgraph2))
            currPopulationDiff = abs(tempClusters[0].getClusterTotalPopulation() - tempClusters[1].getClusterTotalPopulation())
            if subgraphPopulationDiff < currPopulationDiff:
                acceptableEdges.append(edgeToRemove)

    # potentially can edit so that you return the best of the acceptable edges
    if not validEdges: 
        return acceptableEdges
        
    return validEdges

# helper fxn to help verify if two clusters are neighbors based on their precincts
def checkClusterNeighbors(c1: Cluster, c2: Cluster):
    clusterPrecinctList1 = c1.getClusterPrecinctsList()
    clusterPrecinctList2 = c2.getClusterPrecinctsList()
    for p in clusterPrecinctList1: 
        for n in p.getPrecinctNeighborsList():
            if n in clusterPrecinctList2:
                return True 
    return False

# choose a random edge to cut from tree, create new clusters, update everything
def cutEdge(edgeList: list, edgeToRemove: tuple):

    oldCluster1 = tempClusters[0]
    oldCluster2 = tempClusters[1]

    subgraphs = generateSubgraphs(edgeList, edgeToRemove)

    # generate new clusters from these subgraphs
    subgraph1 = list(subgraphs[0])
    subgraph2 = list(subgraphs[1])

    newCluster1 = Cluster()
    newCluster2 = Cluster()

    # generate precinct list for new clusters 
    clusterPrecinctList1 = []
    clusterPrecinctList2 = []

    for id in subgraph1: 
        clusterPrecinctList1.append(globalPrecinctDict[id])
    for id in subgraph2: 
        clusterPrecinctList2.append(globalPrecinctDict[id])

    newCluster1.setClusterPrecinctsList(clusterPrecinctList1)
    newCluster2.setClusterPrecinctsList(clusterPrecinctList2)

    # generate cluster id for new clusters 
    newCluster1.setClusterID(clusterPrecinctList1[0].getPrecinctID())
    newCluster2.setClusterID(clusterPrecinctList2[0].getPrecinctID())

    # generate neighbors list for new clusters // # update neighbors for preexisting clusters
    clusterNeighborList1 = [newCluster2]
    clusterNeighborList2 = [newCluster1]

    oldClusterNeighborList1 = oldCluster1.getClusterNeighborsList()
    oldClusterNeighborList1.remove(oldCluster2)
    oldClusterNeighborList2 = oldCluster2.getClusterNeighborsList()
    oldClusterNeighborList2.remove(oldCluster1)

    if oldClusterNeighborList1:
        for c in oldClusterNeighborList1: 
            removeNeighbor(c, oldCluster1) # remove outdated cluster
            if checkClusterNeighbors(c, newCluster1):
                clusterNeighborList1.append(c) # add each other to neighbors list
                addNeighbor(c, newCluster1)
            if checkClusterNeighbors(c, newCluster2):
                clusterNeighborList2.append(c) # add each other to neighbors list
                addNeighbor(c, newCluster2)
    if oldClusterNeighborList2:
        for c in oldClusterNeighborList2: 
            removeNeighbor(c, oldCluster2) # remove outdated cluster
            if checkClusterNeighbors(c, newCluster1):
                clusterNeighborList1.append(c) # add each other to neighbors list
                addNeighbor(c, newCluster1)
            if checkClusterNeighbors(c, newCluster2):
                clusterNeighborList2.append(c) # add each other to neighbors list
                addNeighbor(c, newCluster2)

    clusterNeighborList1 = list(set(clusterNeighborList1)) # remove duplicates
    clusterNeighborList2 = list(set(clusterNeighborList2))

    newCluster1.setClusterNeighborsList(clusterNeighborList1)
    newCluster2.setClusterNeighborsList(clusterNeighborList2)

    # update global cluster list 
    globalClusterList.remove(tempClusters[0])
    globalClusterList.remove(tempClusters[1])
    globalClusterList.append(newCluster1)
    globalClusterList.append(newCluster2)

    return [newCluster1, newCluster2] # used for debugging

def allClustersAcceptableCheck(): 
    return False

#if __name__ == "__main__":
def runAlgorithm():

    globalClusterList.clear()
    
    # load initial JSON data
    with open(precinctGraphJSONFile) as f:
        precinctsJSONData = json.load(f)
    
    # load initial precinct objects into list
    precinctsJSONList = precinctsJSONData['precincts']
    for p in precinctsJSONList:
        globalPrecinctDict.update({p[myconstants.PRECINCT_ID]: Precinct(p)})

    # load initial cluster objects into list, initialize precinct neighborLists with objects instead of numbers
    for p in globalPrecinctDict.values(): 
        neighborLst = []
        for id in p.getPrecinctNeighborsList():
            neighborLst.append(globalPrecinctDict[id])
        p.setPrecinctNeighborsList(neighborLst)
        globalClusterList.append(Cluster(p)) # create a cluster out of precinct
    
    # turn the precinct neighbor list into its corresponding cluster neighbor list
    for c in globalClusterList: 
        #print(c.getNeighorsList())
        correspondingClusterNeighborList = []
        for p in c.getClusterNeighborsList():
            for c1 in globalClusterList:
                if p.getPrecinctID() == c1.getClusterID():
                    correspondingClusterNeighborList.append(c1)
        c.setClusterNeighborsList(correspondingClusterNeighborList)
    
    print()
    print("Initial Precinct Data")
    print("{:15s} | {:15s} | {:30s} | {:30s}".format("ClusterID", "Population", "Neighbors", "Precincts"))
    for c in globalClusterList: 
        print("{:15s} | {:<15d} | {:30s} | {:30s}".format('C'+c.getClusterID(), c.getClusterTotalPopulation(), stringifyList(c.getClusterNeighborsList()), stringifyList(c.getClusterPrecinctsList())))

    # UC29: Generate Seed Districting
    # Merge random clusters until there are {requestedNumDistricts} clusters left 
    currNumClusters = len(globalClusterList)
    while (currNumClusters > requestedNumDistricts):
        randomCluster = random.sample(globalClusterList, 1) # random.sample returns a list
        randomClusterNeighbor = random.sample(randomCluster[0].getClusterNeighborsList(), 1)

        mergeClusters(randomCluster[0], randomClusterNeighbor[0])
        '''
        print("CURRENT SEED DISTRICTING")
        print("{:15s} | {:15s} | {:30s} | {:30s}".format("ClusterID", "Population", "Neighbors", "Precincts"))
        for c in globalClusterList: 
            stringifyList(c.getClusterPrecinctsList())
           
        print("{:15s} | {:<15d} | {:30s} | {:30s}".format('C'+c.getClusterID(), c.getClusterTotalPopulation(), stringifyList(c.getClusterNeighborsList()), stringifyList(c.getClusterPrecinctsList())))
        '''
        currNumClusters = len(globalClusterList)

    # print seed districting for debugging
    print()
    print("SEED DISTRICTING")
    print("{:15s} | {:15s} | {:30s} | {:30s}".format("ClusterID", "Population", "Neighbors", "Precincts"))
    for c in globalClusterList: 
        stringifyList(c.getClusterPrecinctsList())
        print("{:15s} | {:<15d} | {:30s} | {:30s}".format('C'+c.getClusterID(), c.getClusterTotalPopulation(), stringifyList(c.getClusterNeighborsList()), stringifyList(c.getClusterPrecinctsList())))
    print()

    currIterationCount = 1 

    # keep iterating until all clusters are acceptable or until we've hit our iteration limit
    while currIterationCount <= numIterations or allClustersAcceptableCheck():
        # UC30: Generate a random districting satisfying constraints
        # Combine the two sub-graphs to form a new sub-graph of simple nodes. 
        print()
        print("ITERATION {}".format(currIterationCount))
        print("Picking two random clusters to merge...")
        randomCluster = random.sample(globalClusterList, 1) # random.sample returns a list
        randomClusterNeighbor = random.sample(randomCluster[0].getClusterNeighborsList(), 1)
        print("Merging {} and {}".format(randomCluster[0], randomClusterNeighbor[0]))
        print()

        tempClusters.clear() # update tempvar to keep track of currently merged clusters
        tempClusters.append(randomCluster[0])
        tempClusters.append(randomClusterNeighbor[0])

        combinedCluster = combineClusters(randomCluster[0], randomClusterNeighbor[0])
        print("Precincts: ")
        print(stringifyList(combinedCluster.getClusterPrecinctsList()))
        print("Edges: ")
        print(combinedCluster.getClusterEdgeList())
        # combine cluster print precincts/edges...consider edge class?

        # UC31: Generate a spanning tree of the combined sub-graph above
        print()
        print("Generating a spanning tree...")
        stEdgeList = generateSpanningTree(combinedCluster)
        print(stringifyList(stEdgeList))

        # UC33: Generate a feasible set of edges in the spanning tree to cut
        # UC32: Calculate the acceptability of each newly generated sub-graph
        validEdgeList = findValidEdges(stEdgeList)
        print()
        print("Generated valid edge list...")
        print(validEdgeList)
        print()

        if not validEdgeList:
            print("No valid edges, moving onto next iteration.")
        else:
            # UC34: Cut the edge in the combined sub-graph
            print("Choosing a random edge from valid edge list...")
            randomValidEdge = random.sample(validEdgeList, 1) # random.sample returns a list
            print("Chosen edge: {}".format(randomValidEdge[0]))

            print()
            print("Resulting new clusters from edge cut...")
            newlyCreatedClusters = cutEdge(stEdgeList, randomValidEdge[0])
            print("Previous clusters...")
            addNeighbor(tempClusters[0], tempClusters[1]) # dead statements to help with debugging visualization
            addNeighbor(tempClusters[1], tempClusters[0])
            print("{:15s} | {:15s} | {:30s} | {:30s}".format("ClusterID", "Population", "Neighbors", "Precincts"))
            for c in tempClusters: 
                print("{:15s} | {:<15d} | {:30s} | {:30s}".format('C'+c.getClusterID(), c.getClusterTotalPopulation(), stringifyList(c.getClusterNeighborsList()), stringifyList(c.getClusterPrecinctsList())))
            print("New clusters...")
            print("{:15s} | {:15s} | {:30s} | {:30s}".format("ClusterID", "Population", "Neighbors", "Precincts"))
            for c in newlyCreatedClusters: 
                print("{:15s} | {:<15d} | {:30s} | {:30s}".format('C'+c.getClusterID(), c.getClusterTotalPopulation(), stringifyList(c.getClusterNeighborsList()), stringifyList(c.getClusterPrecinctsList())))
            print("Updated cluster list...")
            print("{:15s} | {:15s} | {:30s} | {:30s}".format("ClusterID", "Population", "Neighbors", "Precincts"))
            for c in globalClusterList: 
                print("{:15s} | {:<15d} | {:30s} | {:30s}".format('C'+c.getClusterID(), c.getClusterTotalPopulation(), stringifyList(c.getClusterNeighborsList()), stringifyList(c.getClusterPrecinctsList())))
            print()
        currIterationCount = currIterationCount + 1 # increment counter
    print("Final cluster list...")
    print("{:15s} | {:15s} | {:30s} | {:30s}".format("ClusterID", "Population", "Neighbors", "Precincts"))
    for c in globalClusterList: 
        print("{:15s} | {:<15d} | {:30s} | {:30s}".format('C'+c.getClusterID(), c.getClusterTotalPopulation(), stringifyList(c.getClusterNeighborsList()), stringifyList(c.getClusterPrecinctsList())))

    returnPlan = []
    for c in globalClusterList:
        districtPrecinctList = []

        for p in c.getClusterPrecinctsList():
            districtPrecinctList.append(p.getPrecinctID())

        returnPlan.append(districtPrecinctList)
    
    return returnPlan
