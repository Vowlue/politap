from algorithm import runAlgorithm
import json 
import sys
import time

state = ""
numPlans = 5
populationVar = 0.1
compactness = "very"
demographics = []

if __name__ == "__main__":

    start_time = time.time()

    # get the command line args
    print(len(sys.argv))
    print(type(sys.argv))

    # error handling
    if (len(sys.argv)) < 6:
        print("Usage: python main.py state numPlans populationVar compactness demographics")
        print("Note: List demographics one after the other, must list at least one.")
        
    else:

        # parse command line args 
        state = sys.argv[1]
        numPlans = int(sys.argv[2])
        populationVar = float(sys.argv[3])
        compactness = sys.argv[4]
        for i in range(5, len(sys.argv)):
            demographics.append(sys.argv[i])

        data = {}
        data["plans"] = []

        for i in range(numPlans):    
            plan = runAlgorithm()
            data["plans"].append(plan)

        with open("output.json", 'w') as outfile: 
            json.dump(data, outfile)

    end_time = time.time()

    print("Program took {} to run".format(end_time - start_time))

    '''
    print(sys.argv[0])
    print(sys.argv[1])
    print(sys.argv[2])
    print(sys.argv[3])
    print(sys.argv[4])
    print(sys.argv[5])
    print(sys.argv[6])
    '''