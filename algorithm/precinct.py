import myconstants 

# precinct - contains parsed data from json 

class Precinct: 
    def __init__(self, precinctDict: dict): 
        # self.__precinctID = precinctDict[myconstants.PRECINCT_ID] # GEOID identifier
        self.__precinctID = ""
        self.__precinctNeighbors = set(precinctDict[myconstants.PRECINCT_NEIGHBORS_LIST].keys()) # list of nums from json to be converted into precinct object references
        self.__precinctPopulation = precinctDict[myconstants.PRECINCT_POPULATION] # population of precinct

    def getPrecinctID(self): 
        return self.__precinctID

    def setPrecinctID(self, id:str):
        self.__precinctID = id

    def getPrecinctNeighbors(self): 
        return self.__precinctNeighbors

    def setPrecinctNeighbors(self, lst: set):
        self.__precinctNeighbors = lst

    def getPrecinctPopulation(self): 
        return self.__precinctPopulation

    def __str__(self):
        return 'P{}'.format(self.getPrecinctID())