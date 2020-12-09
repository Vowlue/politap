import myconstants 

# cluster - node of one or more precincts 

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

    def getClusterTotalPopulation(self): 
        totalPop = 0
        for p in self.__precinctList: 
            totalPop = totalPop + p.getPrecinctPopulation()
        return totalPop