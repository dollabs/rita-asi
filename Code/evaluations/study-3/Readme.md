#### Notes on various uses cases for study-3
Please review all code / scripts before using them. 

#### Grab latest version of RMQ log files from NAS
  * Regression data is in `$ritahome/workspace/regression-data`
  * `cd $ritahome/workspace/regression-data` then 
  * Use `Code/evaluations/study-3/bin/get_regression_data.sh` to pull latest data from NAS. Ensure you pull only latest `*V-*.log` and not all versions.
  * This is a one time operation. 
  
#### Pull and build docker images
  * `cd $ritagit/Code`
  * `./bin/update-git.sh`
  * `docker-compose build`
  * `docker images` to verify new images
  
#### Doing regression testing
  * `cd` to any one of the regression directories `[regression-tests-set-1, regression-tests-set-2, regression-tests-spiral_3-and-4, ]`

  * Ex: `cd regression-tests-set-2` then run `par_check_rita` with appropriate rmq_log directory.

  * `$ritagit/Code/evaluations/study-3/bin/par_check_rita.sh ../regression-data/spiral-3-and-4/ | tee -a par_check.txt`

  * Wait for the regression tests to finish. When no docker containers are running, then it is done. Use `docker ps -a` to check for running instances.

  * `cd` to appropriate run directory. Ex `cd run-April-2022-16--13/`. Then run one of `find_` variants to see what was produced in this run. `$ritagit/Code/evaluations/study-3/bin/find_[find_exceptions.sh, find_interventions.sh, find_mission_ended.sh]`

#### Doing learning
  * Copy/sync contents of `/nfs/projects/RITA/HSR-data-mirror/study-3_2022-rmq-experiments/exp-0001` to `/home/prakash/projects/rita/workspace/regression-tests-learning`
  * Copy RMQ log files to `/home/prakash/projects/rita/workspace/regression-tests-learning/exp-0001`  
  * cd `/home/prakash/projects/rita/workspace/regression-tests-learning`  
  * Then start learning as `$ritagit/Code/evaluations/study-3/bin/par_do_experiment_all.py .`


#### Regression test timing
```
Regression testing for spiral 3 and spiral 4 combined will take about 6 hours at speedup =1.
Regression testing for teams 201-210 will take about 1 hour 40 minutes at speedup =1
Regression testing for teams 211-219 will take about 45 minutes at speedup = 1
```
