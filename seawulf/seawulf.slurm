#!/usr/bin/env bash

#SBATCH --job-name=test_districting
#SBATCH --output=log
#SBATCH --ntasks-per-node=28
#SBATCH --nodes=2
#SBATCH --time=12:00:00
#SBATCH -p long-28core
#SBATCH --mail-type=BEGIN,END
#SBATCH --mail-user=james.hua@stonybrook.edu

module load anaconda/3
module load mpi4py/3.0.3

pip install networkx
mkdir plan$1

mpirun -np 2 python multi.py plan$1 $2 $3 $4 $5
