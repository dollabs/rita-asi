# Descriptions of the Experiments

Starting with our ASIST RITA work in December 2020, we've been running our RITA tests on subsets of the HSR trial data, using the non-**test** trials as **training** data. The goal is to randomly select different training and test trials, and call that partitioning an **experiment**.  A given set of experiments can be run through RITA, and the predictions of each experiment can be compared with each other.  

We have been creating **groups** of experiments, where all of the experiments in the group have the same train/test partitioning strategy.  These experiment groups include 20 experiments (so far).  We have been refining our train/test partitioning strategy over time.  I.e., out first group of experiments was created relatively naively, but our fourth group of experiments used a train/test partitioning strategy based on the lessons learned from the previous 3 groups.

## Experiment Group 1: (exp-0001 through exp-0020)
* Percent of Trials in **Training Set**: 75% (on average).  The sampling mechanism had a random component, so that some experiments might use 70% for training and others might use 80% for training, averaging out to approximately 75%.

## Experiment Group 2: (exp-0021 through exp-0040)
* Percent of Trials in **Training Set**: 95% (on average).  

## Experiment Group 3: (exp-0041 through exp-0060)
* Percent of Trials in **Training Set**: 75%. This 75% is consistent for each experiment in the experiment group, so there is a fixed number of training trials in each of the experiments.

## Experiment Group 4: (exp-0061 through exp-0080)
* Percent of **Subjects** in **Training Set**: 75%. This 75% is consistent for each experiment in the experiment group, so there is a fixed number of training trials in each of the experiments.
* One big difference here is that the partitioning was done by Subject, not by Trial.  So, all of the trials for a given Subject will be in *either* the Training or the Test partition.  Previous experiment groups could have 1 trial for Subject-99 in the Training set, and 2 trials in the Test set.
	* When setting up Training sets, it's a "cheat" to include one of the subjects that we will be testing with trials of the same subject for which we will be Testing.
* Another difference here is that the trials for a given subject are done in sequential order.  So, if Subject-99 is in Trial-31 through Trial-33 (and is part of the Test data set), those three trials will be run in sequential order.  Previous experiment groups ran their trials in an arbitrary order.  
	* The goal of maintaining this execution order is to run the tests in the order experienced by the subject, so we can examine whether we can predict/perceive learning by the subject from one trial to any preceding trials.
