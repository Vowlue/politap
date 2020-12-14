ssh -T jahua@login.seawulf.stonybrook.edu << EOSSH
cd ~/
module load slurm
RUNSTATUS="RUNNING"
JOBID=\$(grep -o '[0-9]*' jobids/$1.jobid)
scontrol show job \$JOBID > jobstatus/plan$1
RUNSTATUS=\$(grep -oP '(?<=JobState=)[A-Z]*' jobstatus/plan$1)
echo \$RUNSTATUS

#while [ \$RUNSTATUS != "COMPLETED" ]
#do
    #scontrol show job \$JOBID > plan$1/jobstatus
    #RUNSTATUS=\$(grep -oP '(?<=JobState=)[A-Z]*' plan$1/jobstatus)
    #echo \$RUNSTATUS
    #sleep 7200
#done

EOSSH