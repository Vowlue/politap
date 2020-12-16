ssh jahua@login.seawulf.stonybrook.edu << EOSSH
cd ~
module load slurm
sbatch seawulf.slurm $1 $2 $3 $4 $5 > jobids/$1.jobid
EOSSH