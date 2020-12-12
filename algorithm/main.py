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
    if (len(sys.argv)) == 4:
        # parse command line args 
        state = sys.argv[1]
        populationVar = float(sys.argv[2])
        compactness = sys.argv[3]
        
        data = {}
        data["plans"] = []

        for i in range(numPlans):    
            plan = runAlgorithm(state, populationVar, compactness)
            data["plans"].append(plan)

        with open("output.json", 'w') as outfile: 
            json.dump(data, outfile)
        
    else:
        print("Usage: python main.py state populationVar compactness")

    end_time = time.time()

    print("Program took {} to run".format(end_time - start_time))
