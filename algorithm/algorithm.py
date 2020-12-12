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
numIterations = 1000
requestedNumDistricts = 4
compactness = '' 

# todo
# figure out compactness 
# take the inputs for vars - how to represent demographics?
# test on real data
# any debug?
# integrity checks at the stages *********** 
# clean up code 
# test on other 3 sets of data
# restore loop 
# get all command args 
# get ricky a test json copy
# how tf does ethnic group factor into our plans?


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

# merges two clusters for seed districting, updates necessary info within clusters and its neighbors
def mergeClusters(n1: str, n2: str):

    #newGraph = nx.contracted_nodes(globalNXGraph, n1, n2)
    #newGraph.nodes[n1]["precinctList"].append(n2)

    #return newGraph
    '''
    # merge precincts
    c1.setClusterPrecinctsList(c1.getClusterPrecinctsList() + c2.getClusterPrecinctsList()) # union of the two cluster's precincts
    
    # merge neighbors
    c1.setClusterID(c1.getClusterPrecinctsList()[0].getPrecinctID()) # set id to first precinct id
    newNeighborsList = list(set(c1.getClusterNeighborsList() + c2.getClusterNeighborsList())) # combine neighbors list, remove duplicates and themselves

    # account for possible data errors
    newNeighborsList.append(c1)
    newNeighborsList.append(c2)
    newNeighborsList = list(set(newNeighborsList))
    '''
    '''
    print(stringifyList(newNeighborsList))
    print(newNeighborsList)
    print(c1 in newNeighborsList)
    print(c2 in newNeighborsList)
    print()
    '''
    '''
    newNeighborsList.remove(c1)
    newNeighborsList.remove(c2)
    c1.setClusterNeighborsList(newNeighborsList) # combine neighbors while removing duplicates, remove any occurrences of precincts
    '''
    # update neighbors - remove merged cluster from neighbors' neighborlist (remove c1 from c2's neighbor list first)
    #removeNeighbor(c2, c1)
    #for n in c2.getClusterNeighborsList():
    '''
        print(c2.getClusterNeighborsList())
        print(n.getClusterNeighborsList())
        print([c2])
        print([n])
    '''
        #print(c2.getClusterID())
        #print(n.getClusterID())
        # potentially bugged - unknown if code end or preprocessing end   
        #if c2 in n.getClusterNeighborsList(): 
            #removeNeighbor(n, c2) # replace instances of c2 with c1
            #addNeighbor(n, c1)
    
    # remove merged cluster
    '''
    print(len(globalClusterList))
    print(globalClusterList)
    print([c2])
    '''
    #globalClusterList.remove(c2)

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
    totalEdges = 0
    cutEdges = 0

    for id in subgraph:
        currPrecinct = globalPrecinctDict[id]
        for neighbor in currPrecinct.getPrecinctNeighbors():
            totalEdges = totalEdges + 1
            if neighbor.getPrecinctID() not in subgraph:
                cutEdges = cutEdges + 1
    
    cutEdgeCompactness = totalEdges / cutEdges

    return populationFlag and compactnessFlag

def getClusterTotalPopulation(id: str):
    precinctList = clusterToPrecinctListDict[id]
    totalPop = 0
    for id in precinctList:
        totalPop = totalPop + globalPrecinctDict[id].getPrecinctPopulation()
    return totalPop

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
            currPopulationDiff = abs(getClusterTotalPopulation(tempClusters[0]) - getClusterTotalPopulation(tempClusters[1]))
            if subgraphPopulationDiff < currPopulationDiff:
                acceptableEdges.append(edgeToRemove)

    # potentially can edit so that you return the best of the acceptable edges
    if not validEdges: 
        return acceptableEdges
        
    return validEdges

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

    #newCluster1.setClusterPrecinctsList(clusterPrecinctList1)
    #newCluster2.setClusterPrecinctsList(clusterPrecinctList2)

    # generate cluster id for new clusters 
    #newCluster1.setClusterID(clusterPrecinctList1[0].getPrecinctID())
    #newCluster2.setClusterID(clusterPrecinctList2[0].getPrecinctID())

    # generate neighbors list for new clusters // # update neighbors for preexisting clusters
    #clusterNeighborList1 = [newCluster2]
    #clusterNeighborList2 = [newCluster1]
    '''
    oldClusterNeighborList1 = oldCluster1.getClusterNeighborsList()
    oldClusterNeighborList1.remove(oldCluster2)
    oldClusterNeighborList2 = oldCluster2.getClusterNeighborsList()
    oldClusterNeighborList2.remove(oldCluster1)
    '''

    '''
    if oldClusterNeighborList1:
        for c in oldClusterNeighborList1: 
            if oldCluster1 in c.getClusterNeighborsList():
                removeNeighbor(c, oldCluster1) # remove outdated cluster
            if checkClusterNeighbors(c, newCluster1):
                clusterNeighborList1.append(c) # add each other to neighbors list
                addNeighbor(c, newCluster1)
            if checkClusterNeighbors(c, newCluster2):
                clusterNeighborList2.append(c) # add each other to neighbors list
                addNeighbor(c, newCluster2)
    if oldClusterNeighborList2:
        for c in oldClusterNeighborList2: 
            if oldCluster2 in c.getClusterNeighborsList():
                removeNeighbor(c, oldCluster2) # remove outdated cluster
            if checkClusterNeighbors(c, newCluster1):
                clusterNeighborList1.append(c) # add each other to neighbors list
                addNeighbor(c, newCluster1)
            if checkClusterNeighbors(c, newCluster2):
                clusterNeighborList2.append(c) # add each other to neighbors list
                addNeighbor(c, newCluster2)
    '''

    #clusterNeighborList1 = list(set(clusterNeighborList1)) # remove duplicates
    #clusterNeighborList2 = list(set(clusterNeighborList2))

    #newCluster1.setClusterNeighborsList(clusterNeighborList1)
    #newCluster2.setClusterNeighborsList(clusterNeighborList2)

    # update global cluster list 
    '''
    print(tempClusters[0] in globalClusterList)
    print(tempClusters[1] in globalClusterList)
    print([tempClusters[0]])
    print([tempClusters[1]])
    print(globalClusterList)

    print(tempClusters[0])
    print(tempClusters[1])
    print(stringifyList(globalClusterList))
    if tempClusters[0] in globalClusterList:
        globalClusterList.remove(tempClusters[0])
    if tempClusters[1] in globalClusterList:    
        globalClusterList.remove(tempClusters[1])
    globalClusterList.append(newCluster1)
    globalClusterList.append(newCluster2)

    print(globalClusterList)
    print(len(globalClusterList))
    '''

def allClustersAcceptableCheck(): 
    for c in globalClusterList:
        if not c.isAcceptable():
            return False
    return True

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

    ''' # old code used for tester json file
    # load initial JSON data
    with open(precinctGraphJSONFile) as f:
        precinctsJSONData = json.load(f)
    
    # load initial precinct objects into list
    precinctsJSONList = precinctsJSONData['precincts']
    for p in precinctsJSONList:
        globalPrecinctDict.update({p[myconstants.PRECINCT_ID]: Precinct(p)})
    ''' 

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
    compactness = compactnessLvl

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

    '''
    print()
    print("Initial Precinct Data")
    print("{:15s} | {:15s} | {:30s} | {:30s}".format("ClusterID", "Population", "Neighbors", "Precincts"))
    for c in globalClusterList: 
        print("{:15s} | {:<15d} | {:30s} | {:30s}".format('C'+c.getClusterID(), c.getClusterTotalPopulation(), stringifyList(c.getClusterNeighborsList()), stringifyList(c.getClusterPrecinctsList())))
    '''

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
        '''
        print("CURRENT SEED DISTRICTING")
        print("{:15s} | {:15s} | {:30s} | {:30s}".format("ClusterID", "Population", "Neighbors", "Precincts"))
        for c in globalClusterList: 
            stringifyList(c.getClusterPrecinctsList())
           
        print("{:15s} | {:<15d} | {:30s} | {:30s}".format('C'+c.getClusterID(), c.getClusterTotalPopulation(), stringifyList(c.getClusterNeighborsList()), stringifyList(c.getClusterPrecinctsList())))
        '''
        currNumClusters = len(list(globalNXGraph.nodes))
    
    '''
    print("INTEGRITY TEST***************")
    if nx.selfloop_edges(globalNXGraph):
        globalNXGraph.remove_edges_from(nx.selfloop_edges(globalNXGraph))
    print(len(list(globalNXGraph.nodes)))
    print(len(list(globalNXGraph.edges)))
    print(list(globalNXGraph.nodes))
    print(list(globalNXGraph.edges))

    print(len(clusterToPrecinctListDict))
    for i in list(globalNXGraph.nodes):
        print(len(clusterToPrecinctListDict[i]))
    '''

    '''
    # print seed districting for debugging
    print()
    print("SEED DISTRICTING")
    print("{:15s} | {:15s} | {:30s} | {:30s}".format("ClusterID", "Population", "Neighbors", "Precincts"))
    for c in globalClusterList: 
        stringifyList(c.getClusterPrecinctsList())
        print("{:15s} | {:<15d} | {:30s} | {:30s}".format('C'+c.getClusterID(), c.getClusterTotalPopulation(), stringifyList(c.getClusterNeighborsList()), stringifyList(c.getClusterPrecinctsList())))
    print()
    ''' 
    '''
    print(list(globalNXGraph.nodes))
    for id in clusterToPrecinctListDict:
                print(list(globalNXGraph.adj[id]))
                '''

    currIterationCount = 1 

    # keep iterating until all clusters are acceptable or until we've hit our iteration limit
    while currIterationCount <= numIterations or allClustersAcceptableCheck():
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
        validEdgeList = findValidEdges(stEdgeList)
        #print()
        #print("Generated valid edge list...")
        #print(len(validEdgeList))
        #print(validEdgeList)
        #print()

        if not validEdgeList:
            #print("No valid edges, moving onto next iteration.")
        else:
            # UC34: Cut the edge in the combined sub-graph
            #print("Choosing a random edge from valid edge list...")
            randomValidEdge = random.sample(validEdgeList, 1) # random.sample returns a list
            randomValidEdge = randomValidEdge[0]
            #print("Chosen edge: {}".format(randomValidEdge))

            #print()
            #print("Resulting new clusters from edge cut...")
            newlyCreatedClusters = cutEdge(stEdgeList, randomValidEdge, globalNXGraph)

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
            '''

            '''
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
            '''
            '''
            print("Updated cluster list...")
            print("{:15s} | {:15s} | {:30s} | {:30s}".format("ClusterID", "Population", "Neighbors", "Precincts"))
            for c in globalClusterList: 
                print("{:15s} | {:<15d} | {:30s} | {:30s}".format('C'+c.getClusterID(), c.getClusterTotalPopulation(), stringifyList(c.getClusterNeighborsList()), stringifyList(c.getClusterPrecinctsList())))
            print()
            '''
        currIterationCount = currIterationCount + 1 # increment counter
    '''
    print("Final cluster list...")
    print("{:15s} | {:15s} | {:30s} | {:30s}".format("ClusterID", "Population", "Neighbors", "Precincts"))
    for c in globalClusterList: 
        print("{:15s} | {:<15d} | {:30s} | {:30s}".format('C'+c.getClusterID(), c.getClusterTotalPopulation(), stringifyList(c.getClusterNeighborsList()), stringifyList(c.getClusterPrecinctsList())))
    '''
    returnPlan = []
    for id in clusterToPrecinctListDict:
        returnPlan.append(clusterToPrecinctListDict[id])
    
    return returnPlan
