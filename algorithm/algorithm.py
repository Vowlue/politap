import json
import random 
import networkx as nx

# JSON Precinct Variable names 
PRECINCT_ID = "id"
PRECINCT_NEIGHBORS_LIST = "neighbors"
PRECINCT_POPULATION = "totalPopulation"

# simluate algo params/job params
idealPopulation = 0
numIterations = 10 
precinctGraphJSONFile = "test.json"
requestedNumDistricts = 4
compactnessMeasure = '' 
populationVariance = 0.3 

globalPrecinctDict = {}
globalClusterList = []

# class def for precinct / class def for cluster
# precinct - contains parsed data from json 
# cluster - node of one or more precincts 

class Precinct: 
    def __init__(self, precinctDict: dict): 
        self.__precinctID = precinctDict[PRECINCT_ID] # GEOID identifier
        self.__precinctNeighborsList = precinctDict[PRECINCT_NEIGHBORS_LIST] # list of nums from json to be converted into precinct object references
        self.__precinctPopulation = precinctDict[PRECINCT_POPULATION] # population of precinct

    def getPrecinctID(self): 
        return self.__precinctID

    def getPrecinctNeighborsList(self): 
        return self.__precinctNeighborsList

    def setPrecinctNeighborsList(self, lst: list):
        self.__precinctNeighborsList = lst

    def getPrecinctPopulation(self): 
        return self.__precinctPopulation

    def __str__(self):
        return 'P{}'.format(self.getPrecinctID())

class Cluster:
    # default precinct=None so we can instantiate empty clusters
    def __init__(self, precinct=None):
        if precinct is not None:
            self.__clusterID = precinct.getPrecinctID()
            self.__precinctList = [precinct]
            self.__neighborsList = precinct.getPrecinctNeighborsList() 
        self.acceptable = False
        self.edgeList = [] # list of tuples
    
    def getClusterID(self): 
        return self.__clusterID

    def getClusterPrecinctsList(self): 
        return self.__precinctList

    def getClusterNeighborsList(self): 
        return self.__neighborsList

    def getClusterEdgeList(self):
        return self.edgeList

    def setClusterID(self, id:str):
        self.__clusterID = id

    def setClusterPrecinctsList(self, lst:list):
        self.__precinctList = lst
    
    def setClusterNeighborsList(self, lst:list):
        self.__neighborsList = lst

    def setClusterEdgeList(self, lst:list):
        self.edgeList = lst

    def __str__(self):
        return 'C{}'.format(self.getClusterID())

    def isAcceptable(self):
        return self.acceptable

    # subject to change, may only need to flip to true once
    def setAcceptable(self):
        self.acceptable = True

    def getTotalPopulation(self): 
        totalPop = 0
        for p in self.__precinctList: 
            totalPop = totalPop + p.getPrecinctPopulation()
        return totalPop

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

# returns boolean based on population and compactness conditions
def isGraphAcceptable(c: Cluster):
    # condition 1: abs(population difference) < user specified population difference 
    # condition 2: compactness score lower than user specified compactness

    return True

# given list of tuples from ST, find acceptability from subgraphs and generate feasible set of edges to cut
def findValidEdges(edgeList: list):
    validEdges = []
    acceptableEdges = []
    for edgeToRemove in edgeList: # check every edge in spanning tree list
        # find the two new subgraphs, test for accepability 
        # nx.connected_components?

        # if both tests pass acceptability test, add to validEdges
        # if one or both fail, see if the edge will better balance the population between subgraphs, if so add to acceeptableEdges
        pass
    return validEdges

# update neighbors for all involved clusters after an edge cut
def updateClusterNeighbors(c: Cluster):
    pass

# choose a random edge to cut from tree
def cutEdge(edgeList: list):
    pass

if __name__ == "__main__":
    # load initial JSON data
    with open(precinctGraphJSONFile) as f:
        precinctsJSONData = json.load(f)
    
    # load initial precinct objects into list
    precinctsJSONList = precinctsJSONData['precincts']
    for p in precinctsJSONList:
        globalPrecinctDict.update({p[PRECINCT_ID]: Precinct(p)})

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
        stringifyList(c.getClusterPrecinctsList())
        print("{:15s} | {:<15d} | {:30s} | {:30s}".format('C'+c.getClusterID(), c.getTotalPopulation(), stringifyList(c.getClusterNeighborsList()), stringifyList(c.getClusterPrecinctsList())))

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
           
        print("{:15s} | {:<15d} | {:30s} | {:30s}".format('C'+c.getClusterID(), c.getTotalPopulation(), stringifyList(c.getClusterNeighborsList()), stringifyList(c.getClusterPrecinctsList())))
        '''
        currNumClusters = len(globalClusterList)

    # print seed districting for debugging
    print()
    print("SEED DISTRICTING")
    print("{:15s} | {:15s} | {:30s} | {:30s}".format("ClusterID", "Population", "Neighbors", "Precincts"))
    for c in globalClusterList: 
        stringifyList(c.getClusterPrecinctsList())
        print("{:15s} | {:<15d} | {:30s} | {:30s}".format('C'+c.getClusterID(), c.getTotalPopulation(), stringifyList(c.getClusterNeighborsList()), stringifyList(c.getClusterPrecinctsList())))
    print()

    # UC30: Generate a random districting satisfying constraints
    # Combine the two sub-graphs to form a new sub-graph of simple nodes. 
    print("Picking two random clusters to merge...")
    randomCluster = random.sample(globalClusterList, 1) # random.sample returns a list
    randomClusterNeighbor = random.sample(randomCluster[0].getClusterNeighborsList(), 1)
    print("Merging {} and {}".format(randomCluster[0], randomClusterNeighbor[0]))
    print()
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
    
    print()
    G = nx.Graph()
    G.add_edges_from(stEdgeList[1:])

    print(list(nx.connected_components(G)))
    print(type(list(nx.connected_components(G))[0]))
    # UC32: Calculate the acceptability of each newly generated sub-graph
    # UC33: Generate a feasible set of edges in the spanning tree to cut
    # UC34: Cut the edge in the combined sub-graph