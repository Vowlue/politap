ssh jahua@login.seawulf.stonybrook.edu << EOSSH
cd ~
module load slurm
JOBID=\$(grep -o '[0-9]*' jobids/$1.jobid)
scancel \$JOBID
EOSSH

echo "Finished" > test