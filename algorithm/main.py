from algorithm import runAlgorithm
import json 
import sys
import time
import myconstants

state = ""
numPlans = 1
populationVar = 0.1
compactness = "very"
demographics = []

if __name__ == "__main__":

    start_time = time.time()

    # get the command line args
    # error handling
    if (len(sys.argv)) == 6:
        # parse command line args
        numPlans = int(sys.argv[1]) 
        state = sys.argv[2]
        populationVar = float(sys.argv[3])
        compactness = sys.argv[4]
        jobID = sys.argv[5]
        
        data = {}
        data["plans"] = []

        for i in range(numPlans):    
            plan = runAlgorithm(state, populationVar, compactness)
            data["plans"].append(plan)

        with open("spring\src\main\\resources\json\generatedDistrictings\\"+state+str(jobID)+".json", 'w') as outfile: 
            json.dump(data, outfile)
        
    else:
        print("Usage: python main.py numPlans state populationVar compactness")

    #end_time = time.time()
    #print("Program took {} to run".format(end_time - start_time))
    print("json\generatedDistrictings\\"+state+str(jobID)+".json");