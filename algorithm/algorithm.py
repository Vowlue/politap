# libraries
import json
import random 
import networkx as nx
import copy
import sys
import time

# self made imports
import myconstants
from precinct import Precinct
from cluster import Cluster

# simluate algo params/job params
numIterations = 50
requestedNumDistricts = 4

globalPrecinctDict = {}
globalClusterList = []
globalNXGraph = nx.Graph()

clusterToPrecinctListDict = {}

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

# combines two clusters together for uc30
def combineClusters(n1: str, n2: str):
    newCluster = Cluster()
    #newCluster.setClusterPrecinctsList(c1.getClusterPrecinctsList() + c2.getClusterPrecinctsList())
    newCluster.setClusterPrecinctsList(clusterToPrecinctListDict[n1] + clusterToPrecinctListDict[n2])
    newCluster.setClusterID(n1) # no need to update neighbors, we'll be creating edges from scratch

    edgeList = createEdgeList(newCluster)
    newCluster.setClusterEdgeList(edgeList)

    return newCluster

# creates edge list based on cluster's precincts' neighbors
def createEdgeList(c: Cluster):
    edgeList = []
    precinctList = []
    for id in c.getClusterPrecinctsList():
        precinctList.append(globalPrecinctDict[id])

    for p in precinctList:
        for n in p.getPrecinctNeighbors():
            if n in precinctList: 
                edgeList.append(tuple((p.getPrecinctID(), n.getPrecinctID())))
    edgeList = list(set(tuple(sorted(edge)) for edge in edgeList))
    return edgeList

# use BFS to generate a ST
def generateSpanningTree(c: Cluster):
    currPrecinctList = []
    for id in c.getClusterPrecinctsList():
        currPrecinctList.append(globalPrecinctDict[id])
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

        for n in currPrecinct.getPrecinctNeighbors(): 
            if n in currPrecinctList:
                currPrecinctIndex = currPrecinctList.index(n)
                if visited[currPrecinctIndex] == False: 
                    queue.append(n)
                    visited[currPrecinctIndex] = True
                    spanningTreeEdgeList.append(tuple((currPrecinct.getPrecinctID(), n.getPrecinctID())))

    return spanningTreeEdgeList
'''
def generateSpanningTree(c: Cluster):
    currPrecinctList = c.getClusterPrecinctsList()
    print(currPrecinctList)
    print(len(currPrecinctList))
    print(type(currPrecinctList))
'''
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

def calcPopulationAcceptability(subgraph: set, subgraphPopulation: int, idealPopulation: float, populationVar: float):
    #subgraphPopulation = calculateSubgraphPopulation(subgraph)
    idealPopulationLowerBound = idealPopulation - populationVar * idealPopulation
    idealPopulationUpperBound = idealPopulation + populationVar * idealPopulation

    if subgraphPopulation > idealPopulationLowerBound and subgraphPopulation < idealPopulationUpperBound:
        return True
    return False

def calcCompactnessAcceptability(subgraph: set, compactness: str):
    totalEdges = 0
    cutEdges = 0

    for id in subgraph:
        currPrecinct = globalPrecinctDict[id]
        for neighbor in currPrecinct.getPrecinctNeighbors():
            totalEdges = totalEdges + 1
            if neighbor.getPrecinctID() not in subgraph:
                cutEdges = cutEdges + 1
    
    cutEdgeCompactness = totalEdges / cutEdges
    
    if compactness.upper() == myconstants.COMPACTNESS_ENUM_NOT:
        if cutEdgeCompactness <= 5.0:
            #print(cutEdgeCompactness)
            return True
    elif compactness.upper() == myconstants.COMPACTNESS_ENUM_SOMEWHAT:
        if cutEdgeCompactness >= 5.0 and cutEdgeCompactness <= 15.0:
            #print(cutEdgeCompactness)
            return True
    elif compactness.upper() == myconstants.COMPACTNESS_ENUM_VERY:
        if cutEdgeCompactness >= 15.0 and cutEdgeCompactness <= 50.0:
            #print(cutEdgeCompactness)
            return True
    else:
        if cutEdgeCompactness >= 50:
            #print(cutEdgeCompactness)
            return True
    
    return False

# returns boolean based on population and compactness conditions
def isGraphAcceptable(subgraph: set, subgraphPopulation: int, idealPopulation: float, populationVar: float, compactness: str):
    populationFlag = False
    compactnessFlag = False
    # condition 1: abs(population difference) < user specified population difference 
    populationFlag = calcPopulationAcceptability(subgraph, subgraphPopulation, idealPopulation, populationVar)

    # condition 2: compactness score lower than user specified compactness
    compactnessFlag = calcCompactnessAcceptability(subgraph, compactness)

    return populationFlag and compactnessFlag

def getClusterTotalPopulation(id: str):
    precinctList = clusterToPrecinctListDict[id]
    totalPop = 0
    for id in precinctList:
        totalPop = totalPop + globalPrecinctDict[id].getPrecinctPopulation()
    return totalPop

# given list of tuples from ST, find acceptability from subgraphs and generate feasible set of edges to cut
def findValidEdges(edgeList: list, idealPopulation: float, populationVar: float, compactness: str):
    validEdges = []
    acceptableEdges = []
    for edgeToRemove in edgeList: # check every edge in spanning tree list
        subgraphs = generateSubgraphs(edgeList, edgeToRemove)

        subgraph1 = subgraphs[0]
        subgraph2 = subgraphs[1]

        subgraphPopulation1 = calculateSubgraphPopulation(subgraph1)
        subgraphPopulation2 = calculateSubgraphPopulation(subgraph2)
        
        subgraph1_acceptable = isGraphAcceptable(subgraph1, subgraphPopulation1, idealPopulation, populationVar, compactness)
        subgraph2_acceptable = isGraphAcceptable(subgraph2, subgraphPopulation2, idealPopulation, populationVar, compactness)

        # if both tests pass acceptability test, add to validEdges
        if subgraph1_acceptable and subgraph2_acceptable: 
            validEdges.append(edgeToRemove)

        # if one or both fail, see if the edge will better balance the population between subgraphs, if so add to acceptableEdges
        else:
            subgraphPopulationDiff = abs(subgraphPopulation1 - subgraphPopulation2)
            currPopulationDiff = abs(getClusterTotalPopulation(tempClusters[0]) - getClusterTotalPopulation(tempClusters[1]))
            if subgraphPopulationDiff < currPopulationDiff:
                acceptableEdges.append(edgeToRemove)

    # potentially can edit so that you return the best of the acceptable edges
    if not validEdges: 
        return acceptableEdges
        
    return validEdges

# given list of tuples from ST, find acceptability from subgraphs and generate feasible set of edges to cut
# this version takes the first edge we see, also updated to for subgraph population calculation optimization
def findValidEdge(edgeList: list, idealPopulation: float, populationVar: float, compactness: str):
    for edgeToRemove in edgeList: # check every edge in spanning tree list
        subgraphs = generateSubgraphs(edgeList, edgeToRemove)

        subgraph1 = subgraphs[0]
        subgraph2 = subgraphs[1]

        subgraphPopulation1 = calculateSubgraphPopulation(subgraph1)
        subgraphPopulation2 = calculateSubgraphPopulation(subgraph2)
        
        subgraph1_acceptable = isGraphAcceptable(subgraph1, subgraphPopulation1, idealPopulation, populationVar, compactness)
        subgraph2_acceptable = isGraphAcceptable(subgraph2, subgraphPopulation2, idealPopulation, populationVar, compactness)

        # if both tests pass acceptability test, add to validEdges
        if subgraph1_acceptable and subgraph2_acceptable: 
            return edgeToRemove

        # if one or both fail, see if the edge will better balance the population between subgraphs, if so add to acceptableEdges
        else:
            subgraphPopulationDiff = abs(subgraphPopulation1 - subgraphPopulation2)
            currPopulationDiff = abs(getClusterTotalPopulation(tempClusters[0]) - getClusterTotalPopulation(tempClusters[1]))
            if subgraphPopulationDiff < currPopulationDiff:
                return edgeToRemove

    # potentially can edit so that you return the best of the acceptable edges
    return []

# helper fxn to help verify if two clusters are neighbors based on their precincts
def checkClusterNeighbors(lst1: list, lst2: list):
    clusterPrecinctList1 = []
    clusterPrecinctList2 = []

    for id in lst1: 
        clusterPrecinctList1.append(globalPrecinctDict[id])
    for id in lst2: 
        clusterPrecinctList2.append(globalPrecinctDict[id])
    
    for p in clusterPrecinctList1: 
        for n in p.getPrecinctNeighbors():
            if n in clusterPrecinctList2:
                return True 
    return False

# choose a random edge to cut from tree, create new clusters, update everything
def cutEdge(edgeList: list, edgeToRemove: tuple, G):

    oldCluster1 = tempClusters[0]
    oldCluster2 = tempClusters[1]

    subgraphs = generateSubgraphs(edgeList, edgeToRemove)

    # generate new clusters from these subgraphs
    subgraph1 = list(subgraphs[0])
    subgraph2 = list(subgraphs[1])

    # generate precinct list for new clusters 
    clusterPrecinctList1 = []
    clusterPrecinctList2 = []
    for id in subgraph1: 
        clusterPrecinctList1.append(globalPrecinctDict[id])
    for id in subgraph2: 
        clusterPrecinctList2.append(globalPrecinctDict[id])

    oldClusterNeighborList1 = list(G.adj[oldCluster1])
    oldClusterNeighborList1.remove(oldCluster2) # list of cluster ids
    oldClusterNeighborList2 = list(G.adj[oldCluster2])
    oldClusterNeighborList2.remove(oldCluster1) # list of cluster ids

    G.remove_node(tempClusters[0])
    G.remove_node(tempClusters[1])

    G.add_node(subgraph1[0])
    G.add_node(subgraph2[0])
    G.add_edge(subgraph1[0], subgraph2[0])

    if oldClusterNeighborList1: 
        for clusterid in oldClusterNeighborList1:
            if checkClusterNeighbors(clusterToPrecinctListDict[clusterid], subgraph1):
                #print("added")
                G.add_edge(clusterid, subgraph1[0])
            if checkClusterNeighbors(clusterToPrecinctListDict[clusterid], subgraph2):
                #print("added")
                G.add_edge(clusterid, subgraph2[0])
    if oldClusterNeighborList2: 
        for clusterid in oldClusterNeighborList2:
            if checkClusterNeighbors(clusterToPrecinctListDict[clusterid], subgraph1):
                #print("added")
                G.add_edge(clusterid, subgraph1[0])
            if checkClusterNeighbors(clusterToPrecinctListDict[clusterid], subgraph2):
                #print("added")
                G.add_edge(clusterid, subgraph2[0])

    return [subgraph1, subgraph2]
'''
def allClustersAcceptableCheck(): 
    for id in clusterToPrecinctListDict:
        if clusterToPrecinctListDict[id]:
            print()
        # check and print acceptability of all the clusters ig
'''

def calcIdealPopulation():
    totalPopulation = 0
    for id in globalPrecinctDict:
        totalPopulation = totalPopulation + globalPrecinctDict[id].getPrecinctPopulation()
    idealPopulation = totalPopulation / requestedNumDistricts
    return idealPopulation

#if __name__ == "__main__":
def runAlgorithm(state: str, populationVar: float, compactnessLvl: str):

    globalPrecinctDict.clear()
    globalClusterList.clear()
    tempClusters.clear()
    clusterToPrecinctListDict.clear()

    globalNXGraph = nx.Graph()
    noValidEdgeCount = 0

    precinctsJSONData = {}
    if state.lower() == myconstants.ARKANSAS or state.lower() == myconstants.ARKANSAS_ABBREVIATION:
        requestedNumDistricts = myconstants.ARKANSAS_NUM_DISTRICTS
        with open(myconstants.ARKANSAS_NEIGHBOR_FILENAME) as f:
            precinctsJSONData = json.load(f)
        #print("arkansas selected")
    elif state.lower() == myconstants.VIRGINIA or state.lower() == myconstants.VIRGINIA_ABBREVIATION:
        requestedNumDistricts = myconstants.VIRGINIA_NUM_DISTRICTS
        with open(myconstants.VIRGINIA_NEIGHBOR_FILENAME) as f:
            precinctsJSONData = json.load(f)
        #print("virginia selected")
    else:
        requestedNumDistricts = myconstants.SOUTHCAROLINA_NUM_DISTRICTS
        with open(myconstants.SOUTHCAROLINA_NEIGHBOR_FILENAME) as f:
            precinctsJSONData = json.load(f)
        #print("south carolina selected")

    for id in precinctsJSONData:
        globalPrecinctDict.update({id: Precinct(precinctsJSONData[id])})
        globalPrecinctDict[id].setPrecinctID(id)

    # load initial cluster objects into list, initialize precinct neighborLists with objects instead of numbers
    for p in globalPrecinctDict.values(): 
        neighborLst = []

        for id in p.getPrecinctNeighbors():
            neighborLst.append(globalPrecinctDict[id])
        p.setPrecinctNeighbors(set(neighborLst))
        globalClusterList.append(Cluster(p)) # create a cluster out of precinct
    
    # turn the precinct neighbor list into its corresponding cluster neighbor list
    for c in globalClusterList: 
        correspondingClusterNeighborList = []
        for p in c.getClusterNeighborsList():
            for c1 in globalClusterList:
                if p.getPrecinctID() == c1.getClusterID():
                    correspondingClusterNeighborList.append(c1)
        c.setClusterNeighborsList(correspondingClusterNeighborList)

    # set up job params
    idealPopulation = calcIdealPopulation()
    populationVariance = populationVar
    compactness = compactnessLvl # possible compactness levels: not, somewhat, very, extremely

    # try with contracted_nodes and nx
    for c in globalClusterList:
        globalNXGraph.add_node(c.getClusterID())
    
    edgeList = []
    for c in globalClusterList:
        for n in c.getClusterNeighborsList():
            if c.getClusterID() is not n.getClusterID():
                edgeList.append(tuple((c.getClusterID(), n.getClusterID())))
    edgeList = list(set(tuple(sorted(edge)) for edge in edgeList))
    globalNXGraph.add_edges_from(edgeList)

    for c in globalClusterList:
        clusterToPrecinctListDict.update({c.getClusterID(): [c.getClusterID()]})

    # UC29: Generate Seed Districting
    # Merge random clusters until there are {requestedNumDistricts} clusters left 
    #currNumClusters = len(globalClusterList)
    currNumClusters = len(list(globalNXGraph.nodes))
    while (currNumClusters > requestedNumDistricts):
        randomNode = random.sample(list(globalNXGraph.nodes), 1)
        randomNode = randomNode[0]

        if list(globalNXGraph.adj[randomNode]):
            randomNodeNeighbor = random.sample(list(globalNXGraph.adj[randomNode]), 1)
            randomNodeNeighbor = randomNodeNeighbor[0]

            globalNXGraph = nx.contracted_nodes(globalNXGraph, randomNode, randomNodeNeighbor, self_loops=False)

            precinctList1 = clusterToPrecinctListDict[randomNode] 
            precinctList2 = clusterToPrecinctListDict[randomNodeNeighbor]
            clusterToPrecinctListDict[randomNode] = precinctList1 + precinctList2
            del clusterToPrecinctListDict[randomNodeNeighbor]
        else: 
            continue
 
        currNumClusters = len(list(globalNXGraph.nodes))

    currIterationCount = 1 
    start_time = time.time()

    # keep iterating until all clusters are acceptable or until we've hit our iteration limit
    while currIterationCount <= numIterations: #or allClustersAcceptableCheck():
        # UC30: Generate a random districting satisfying constraints
        # Combine the two sub-graphs to form a new sub-graph of simple nodes. 
        #print()
        #print("ITERATION {}".format(currIterationCount))
        #print("Picking two random clusters to merge...")
        randomNode = random.sample(list(globalNXGraph.nodes), 1)
        randomNode = randomNode[0]

        randomNodeNeighbor = random.sample(list(globalNXGraph.adj[randomNode]), 1)
        randomNodeNeighbor = randomNodeNeighbor[0]
        #randomCluster = random.sample(globalClusterList, 1) # random.sample returns a list
        #randomClusterNeighbor = random.sample(randomCluster[0].getClusterNeighborsList(), 1)
        #print("Merging {} and {}".format(randomCluster[0], randomClusterNeighbor[0]))
        #print()

        tempClusters.clear() # update tempvar to keep track of currently merged clusters
        tempClusters.append(randomNode)
        tempClusters.append(randomNodeNeighbor)

        #print(tempClusters)

        combinedCluster = combineClusters(randomNode, randomNodeNeighbor)
        #print("Precincts: ")
        #print(stringifyList(combinedCluster.getClusterPrecinctsList()))
        #print(len(combinedCluster.getClusterPrecinctsList()))
        #print("Edges: ")
        #print(combinedCluster.getClusterEdgeList())
        #print(len(combinedCluster.getClusterEdgeList()))

        # UC31: Generate a spanning tree of the combined sub-graph above
        #print()
        #print("Generating a spanning tree...")
        stEdgeList = generateSpanningTree(combinedCluster)
        #print(stringifyList(stEdgeList))

        # UC33: Generate a feasible set of edges in the spanning tree to cut
        # UC32: Calculate the acceptability of each newly generated sub-graph
        #validEdgeList = findValidEdges(stEdgeList, idealPopulation, populationVar, compactness)
        validEdge = findValidEdge(stEdgeList, idealPopulation, populationVar, compactness)
        #print()
        #print("Generated valid edge list...")
        #print(len(validEdgeList))
        #print(validEdgeList)
        #print()

        #if not validEdgeList:
        if not validEdge:
            #print("No valid edges, moving onto next iteration.")
            noValidEdgeCount = noValidEdgeCount + 1
            pass
        else:
            # UC34: Cut the edge in the combined sub-graph
            #print("Choosing a random edge from valid edge list...")
            #randomValidEdge = random.sample(validEdgeList, 1) # random.sample returns a list
            #randomValidEdge = randomValidEdge[0]
            #print("Chosen edge: {}".format(randomValidEdge))

            #print()
            #print("Resulting new clusters from edge cut...")
            #newlyCreatedClusters = cutEdge(stEdgeList, randomValidEdge, globalNXGraph)
            newlyCreatedClusters = cutEdge(stEdgeList, validEdge, globalNXGraph)

            del clusterToPrecinctListDict[tempClusters[0]]
            del clusterToPrecinctListDict[tempClusters[1]]

            clusterToPrecinctListDict.update({newlyCreatedClusters[0][0]: newlyCreatedClusters[0]})
            clusterToPrecinctListDict.update({newlyCreatedClusters[1][0]: newlyCreatedClusters[1]})

            # print cluster - population - num precincts
            '''
            print("Merged and Cut Clusters")
            print(tempClusters[0])
            print(tempClusters[1])
            print("Results:")
            print(list(globalNXGraph.nodes))
            tempList = []
            for id in clusterToPrecinctListDict:
                tempList.append(len(clusterToPrecinctListDict[id]))
            print(tempList)
            tempList.clear()
            for id in clusterToPrecinctListDict:
                tempList.append(getClusterTotalPopulation(id))
            print(tempList)
            tempList.clear()
            for id in clusterToPrecinctListDict:
                print(list(globalNXGraph.adj[id]))
            print()
            '''

        currIterationCount = currIterationCount + 1 # increment counter

    returnPlan = []
    for id in clusterToPrecinctListDict:
        returnPlan.append(clusterToPrecinctListDict[id])
    #print("No valid edge times: {}". format(noValidEdgeCount))
    
    return returnPlan
