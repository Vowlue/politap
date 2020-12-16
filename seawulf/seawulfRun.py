from mpi4py import MPI
import multiprocessing as mp 
from multiprocessing import Pool

from algorithm import runAlgorithm
import json
import sys
from datetime import datetime

def algoWrap(a):
    #starttime = datetime.now()
    #print('Run',str(a),'starting now',starttime.strftime("%H:%M:%S"))
    args = sys.argv
    return runAlgorithm(args[2],float(args[3]),args[4])
    #print('run:',str(a),'is done.')
    #return ret

if __name__ == '__main__':
    pool_size = mp.cpu_count()
    comm = MPI.COMM_WORLD
    size = comm.Get_size()
    rank = comm.Get_rank()
    #print(rank)
    ret = []
    totalruns = int(sys.argv[5])
    with Pool(processes=pool_size) as pool:
        if rank == 0:
            for i in pool.imap_unordered(algoWrap,range(totalruns//2)):
                ret.append(i)
        else:
            for i in pool.imap_unordered(algoWrap,range(totalruns-totalruns//2)):
                ret.append(i)
    #print(rank,ret)
    '''
    if rank == 0:
        #print('Node0: ',ret)
        ret.extend(comm.recv(source = 1))
        #print('Node1: ',ret2)
        with open(sys.argv[1]+'/plan.json','w') as f:
            json.dump({'plans':ret},f)
    else:
        comm.send(ret,dest = 0)
    '''
    with open(sys.argv[1]+'/plan'+str(rank)+'.json','w') as f:
        json.dump({'plans':ret},f)
