ssh -T jahua@login.seawulf.stonybrook.edu << EOSSH
cd ~/
module load slurm
RUNSTATUS="RUNNING"
JOBID=\$(grep -o '[0-9]*' jobids/$1.jobid)
scontrol show job \$JOBID > jobstatus/plan$1
RUNSTATUS=\$(grep -oP '(?<=JobState=)[A-Z]*' jobstatus/plan$1)

while [ \$RUNSTATUS != "COMPLETED" ]
do
    scontrol show job \$JOBID > plan$1/jobstatus
    RUNSTATUS=\$(grep -oP '(?<=JobState=)[A-Z]*' plan$1/jobstatus)
    if [ \$RUNSTATUS = "FAILED" ]
    then
        exit
    fi
    if [ \$RUNSTATUS = "TIMEOUT" ]
    then
        exit
    fi
    echo \$RUNSTATUS
    sleep 300
done

EOSSH

scp -r jahua@login.seawulf.stonybrook.edu:/gpfs/home/jahua/plan$1/ testdata/