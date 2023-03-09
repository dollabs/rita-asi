# Weekly meeting minutes

Here are meeting minutes of each weekly meeting. The most recent weeks come first.


## Week 8 - July 20th

Darren:

* Start implementing his ideas
* Plan to incorporate Yuening’s temporal framework for next week
* Problem he met: Notice that finding a route that get through all the rooms for the agent is a more complex problem then he thought

Christian:

* Almost done with a function that could transfer our’s mdp model to another mdp model
* Plan to work on generating reward function for agents in inverse planning framework

Anna:
* Implemented a memory system for agents, start to change how room values are assigned
* Plan to continue exploring how to change room values so that agent can behave more ‘human’

Isabelle:
* Tried a new algorithm, find out it didn’t work
* Plan to implement tile_level_value_iteration with a different algorithm

---

## Week 7 - July 13th

Slides from UROP presentations

* Darren's slides on recommendation system: [https://docs.google.com/presentation/d/1L5oIImOmCtqgyPgfelrYWuMUcwp_I3hb9LdaqXi8WI8/edit?usp=sharing](https://docs.google.com/presentation/d/1L5oIImOmCtqgyPgfelrYWuMUcwp_I3hb9LdaqXi8WI8/edit?usp=sharing)
* Christian's slides on Bayesian inverse reinforcement learning: [https://docs.google.com/presentation/d/1-BopTio00nzo1_8fXMAHf2oQlSPB4PY6iqJ4VEIOssQ/edit](https://docs.google.com/presentation/d/1-BopTio00nzo1_8fXMAHf2oQlSPB4PY6iqJ4VEIOssQ/edit)
* Anna's: [https://docs.google.com/presentation/d/1BhOze5k_UC4YG-7O_cK7oKi7tf3q8eSeCSTwzGJu5UI/edit?usp=sharing](https://docs.google.com/presentation/d/1BhOze5k_UC4YG-7O_cK7oKi7tf3q8eSeCSTwzGJu5UI/edit?usp=sharing)
* Isabelle's: [https://drive.google.com/drive/folders/1oMdqFolh-2R4-Ev9NmybcmvKsqRjwMnH?usp=sharing](https://drive.google.com/drive/folders/1oMdqFolh-2R4-Ev9NmybcmvKsqRjwMnH?usp=sharing)

---

## Week 6 - July 6th (No meeting due to Independence Day holidays)

---

## Week 5 - June 29th

Progress updates:

Anna:

* Finished pilot test 3, gave observations and strategies
* Was looking into hierarchal code, found some problems and tried to fix

Isabelle:

* Week 3 focused on how to model player behaviors observed
    - Played around w parameters and reward functions
    - Will post on slack channel
* Looked at hierarchal.py and ideas to implement her own branch
* Observed players (pilot test 3) as a participant, looked at results and wrote report, uploaded to git

Christian:

* Did ASU test, conclusions are in meeting notes (maybe)
    - Interesting that their victims died in the middle
    - Strategy was suboptimal but didn’t find out until failed at getting all of them
* Tejas provided lots of supplementary material
    - Read Bayesian reinforcement paper
    - Went over lecture notes from 6.437
* Next step implementing bayesian learning

Yang:

* Showed ASU data, will be useful
* Next Monday we will all give 15 min presentations on behaviors and planning
    - 4-15 pages of slides to use videos/images to summarize plan
    - Optional: give plan for computational model

Yuening's presentation:

* Presentation on temporal networks
* In this case: temporal networks are using for mainly generative activity planning (or conditional planning, not being talked about)
    - Mainly come up with sequence of actions to achieve plan
* Background on lab, MERS
    - Goal: build robots that think for themselves and design how humans program cognitive robots
    - ex: underwater rovers, mars rovers, autonomous vehicles
        - Problem: failures occur
    - Want to write high level programs that tell the robot what to achieve, then the robot can reason how to achieve this goal
        - Ex: put out the fires at loc X and Y, then return in an hour. Avoid no fly zones. Here's a map.
    - Programs are state based and model based
    - Need to have planning and ability to execute plan
    - Need to be aware of the risk and utility
* Simple temporal networks (STN)
    - Scheduling representation
    - We have events and temporal constraints (this occurs after that)
    - This allows us to create flexible execution on the fly
    - System can adapt: can have ranges of when events can occur based on temporal constraints
    - How to detect inconsistencies:
        - Two equalities: change constraint to two opposing directed edges (negate the one going away)
        - Becomes a distance graph
        - Simply check for negative cycles for inconsistencies (APSP)
    - How to dispatch events
        - Key idea: need to assign events in increasing order; once event is dispatched at time t, then you cannot dispatch anything before that anymore
        - Negative edges from APSP dictate ordering constraints
        - Ex: -1 negative edge means that the head needs to occur before the tail
        - Then, propagate time constraint to other events and continue
* Simple temporal networks with uncertainty (STNU)
    - Allows uncontrollable events
    - Ex: cannot control when an event happens, but we know the delay between the previous event to the next event
    - Three types of controllability:
        - Weak, dynamic, and strong
        - Dynamic: can generate schedule online given past uncontrollable durations
        - Strong: generate schedule no matter how long the uncontrollables take
    - Only if consistent or controllable can a schedule be found
* Example application: Uhura
    - A user talks about their schedule then the planner looks for if consistent/controllable; if not, ask them to relax some requirements
    - Algorithm is called temporal relaxation
* If inconsistent, extract negative cycles to find out how to resolve these conflicts
    - Need to change neg cycle to not neg cycle by either: Discrete or Continuous relaxation
    - Discrete: just don't satisfy that constraint
    - Continuous: make it non negative by weakening the constraint (lower/upper bounds)
    - Look for least costly relaxation
* Also: looking at d-Uhura which works in teams
    - When ppl collaborate in tasks, they are constantly overloaded
    - It's an oversubscription problem with multiple users where changes can have a rippling event
    - d-Uhura helps teams adapt their schedule together
        - Locally, try to get individuals to fix schedule without affecting others
        - If not, collaborate with others
* Extensions:
    - Temporal networks to temporal plan network
        - Introduce choice nodes: controllable by agent, or uncontrollable by human/nature etc
        - Ex: have choice nodes that allow the system to select which action to do
        - Choices made by utility and temporal consistency
        - Can dynamically change decision on choices to satisfy consistency
    - Qualitative State Plans (QSP)
        - Introduce episodes: includes temporal constraint that tells how long something takes, includes state constraints. Ex: in between two events, we want to remain in a certain region
        - Plan Completeness: All state constraints satisfied
* Summary
    - TPN allows flexible choices
    - QSP allows desirable states to write state-based goal-directed programs
    - Generative planner: end and beginning states: set of actions and what it needs; the planner gives you a sequence of actions needed to reach goal
    - Temporal networks allow us to dispatch events on the fly flexibly
    - Temporal plan networks allow us to make choices on the fly
    - Qualitative State Plans allow us to define states that we want to get to
    - Uhura/d-Uhura helps diagnose over-subscribed plans to adapt

---

## Week 4 - June 22nd (No meeting due to DARPA Hackathon)

---

## Week 3 - June 15th

Anna:

* Lessons learned from player recordings:
    - (missed the first 2 points of Anna's pres. she can add here if possible)
    - Experienced players make strategies and stick to them
    - Non-experienced players: some make strategies, some do not; tend to pick up whatever they see in the game
    - Most players tend to search left of the room; Some others randomly search left/right; Almost none search right first
    - Time pressure makes strategy change, focusing on path to achieve goals
* Plan:
    - Focus on maze for pilot 2

Isabelle:

* In summary, finished all tasks. Firstly, watched 2 sets of videos, answer 3 prompts related, developed summary of player behaviors. Secondly, finished assigned readings.
* Can classify player behavior into boxes:
    - Some players will search and skim through the room
    - Some others don't have a strategy
    - Third group of people rush through the room, go through carefully later to see if they missed anyone
    - Some people do a BFS way
    - Some people do a DFS approach
* Players don't really stick to one approach
    - Change over time, Ex: Maze isn't what they thought it would be, so they change their strategy
    - Time constraint also changes their strategy
        - Ex: they originally broke all gravel, now they break only enough gravel to search faster
        - Optimizations for time
        - For some players, this makes them change their strategy completely and makes them go random
    - As players go on, some change to become more ordered rather than randomly doing things
    - Players apply a type of strategy, then make decisions based on that
    - Other things that affect gameplay: time constraints, how much information they have about map, pro/noob, personality of person
    - Ex: player 1 shows she's a very thorough player by how she searches the maze very deeply
* How to predict player decisions:
    - Identify player strategy
    - Sometimes they start random so it's hard
    - Other cases it's possible by watching how they play
    - It's easier for people that stick to one particular strategy (usually more experienced players)
* Plan for this week:
    - Working on running examples, changing parameters, see if can attempt to include some new player strategies discussed in 1v1
    - Focus on maze w more rooms

Yang's comments on Isabelle:

* Some players have random actions, but can we still find patterns in this?
    - Ex: some players break all gravel rather than just 2 blocks: why do they do this? Helps to understand the behavior better
    - Find patterns in irrational behavior
    - For rational behavior: there are always patterns that are not always rational
* Note for Christian: Identify reward function for different players,
    - Reward for observing, reward for exploring new locations
    - If cannot determine reward, what other explanations can you think of?
* Note for Darren: Write stories of each player
    - Each mission is a story
    - How is this ONE player is making decisions along the way
    - How is this changing
    - How is this player learning over the 5 tests: are they becoming more strategic, is their attention dropping
    - Can use more symbols to describe this behavior
    - Can talk about rules, heuristics, patterns, to describe this

Darren:
* Talked about ASU experiment, redacted because Christian did not yet do the experiment

Christian:

* Pilot test 1:
    - most interesting speaking in reward functions:
    - Players that are more experienced are really interesting
    - They make a lot of decisions with extreme investigation (opening a lot of things that in this particular task are not that effective, like opening hoppers, chests, etc)
    - Heighten reward function on these misc functions
    - In Christian's case, he skipped everything that others looked into, because he knew that he was looking for wool not things in chests and stuff
    - Interesting that experienced players were walking around rather than sprinting even under time constraint
    - Some experienced players were more prone to getting stressed: possible expectation on how they should do on the task
        - Player 7 started breaking random stuff, dropped his inventory, complex chain of events that can be hard to predict
    - Unexperienced players:
    - Actions are random, once they learn how to open doors they only open doors to get to places
    - More experienced players were more nuanced and interesting

Yang's comments:

* Strategy is reward function: what are they driven by, what do they value and think is a waste of time

Essie:

* Will present tomorrow at 1:30PM EST (A*)

---

## Week 2 - June 8th

(Taken by Darren and editted by Yang)

### Yang: Explanation of tom-minecraft tools and data folders:

Here are some details about the repository that's not documented so far but helpful to know. You should be able to see the codes and folders I mentioned after `git pull`. If not, let me know.

* Check main docs page [https://zt-yang.github.io/tom-minecraft/](https://zt-yang.github.io/tom-minecraft/) for updated talk schedules:
    - This Tuesday and Friday OH, Tejas will give tutorials on numpy and Pytorch, and how value iterature algorithm is implemented
    - Next Monday, Essie will give a talk on her MCTS & A* method for the gridworld search task
* `tom-minecraft/documentation`:
    - Can make notes on how to solve common problems, or notes on reading to share with each other
    - To publish changes: `git push`, PLEASE DON'T `mkdocs deploy`, otherwise you may override other people's changes!
* `tom-minecraft/gridworld/recordings/_test cases`
    - output PNG when running `visualize.py`, yellow tiles represents the blocks that are exposed to the player (calculated by `raycasting.py`)
* `tom-minecraft/gridworld/trajectories`
    - output JSON when running `visualize.py` with PLANNING or HIERARCHICAL_PLANNING mode
    - these JSON files can be input to `visualize.py` with INVERSE_PLANNING or EXPERIMENT_REPLAY mode
* `tom-minecraft/gridworld/recordingsMalmo` (not essential but useful):
    - Human trajectory data are recorded by Malmo in observations.txt, include DistanceTravelled, etc.
    - TXT trajectory data can be downloaded from [dropbox](https://www.dropbox.com/s/xr5gu7ovmhr3dt2/Pilot%202%20trajectories%20-%20txt.zip?dl=0) into `recordingsMalmo`, will be processed by `Recording replay.ipynb` into JSON input for recording replay mode in `visualize.py`.
    - For details see [tools-map-generator.md](data-trajectory).
* `tom-minecraft/gridworld/recordingsMP4` (not essential but useful):
  	- `Crop mp4.ipynb` can segment recorded videos into smaller clips to give to human observers and ask: what will the player do next
		- May also be useful for extracting images to train neural networks
* `tom-minecraft/world-builder` (not essential but useful):
    - Tool for loading minecraft saves folder: load prebuild worlds
    - Drag mca files into interface of [https://pessimistress.github.io/minecraft/](https://pessimistress.github.io/minecraft/) (link is given in [map-generator instructions](tools-map-generator.md)) to visualize and find the building you want to extract
    - `run-map-generator.ipynb` generates map PNG, CSV, and JSON to represent the building (coordinates and block types)
    - Can also generate MP4 videos of human trajectory on the map; can be real time to be used to make predictions of what they like
    - Could be useful for UROPers to implement how algorithm is analyzing human data

* This week, will try to get UROPers to join ASU evaluation task:
    - We don't know the test conditions, so it would be good to test with us (Darren and Christiam have signed up)


### Progress of UROPers:

Christian:

* Setup stuff, some problems with tom-minecraft and Malmo
* Python version 3.7.0 erroneous a bit
* Had issues with Yan'gs Minecraft Playground program crashing after running
    -
    - Solution: make sure recordingsMalmo folder is created manually

Darren:

* Installed tom-minecraft, Malmo, genesis
* Modified Malmo mod to track more data (door opening, block breaking)
* Played around with Genesis
    - Talked with Yuening about how to make multi-agent recommendation systems
    - Planned how to improve tracking to utilize multiplayer tracking

Isabelle:

* Got through everything
* Finished prereq meetings
* Played search and rescue mission
* Still working on predicting player video predictions
* Question:
    - When played search and rescue game: given seconds, then after it runs out still able to play?
        - Countdown: data stops being collected at time 0, experiment officially ends, does not force player to quit
    - Experimental replay:
        - Will be shown later how to change to different algorithms: main purpose is to visualize human trajectory
        - Can be replayed in Malmo to see the trajectory in real time, bird's eye view
        - Help to visualize and compare human behavior

Anna:

* Similar to Isabelle
    - Setup everything
* Checked prereq reading list
    - Understand MDP
* Went through videos, made observations while watching, did not make predictions; can probably do something more about it
* Problem: when running malmo.py: see myself in map, but feel like something's wrong with controls, cannot switch orientation
    - Solution: press enter, then you can look around
* Note to Anna from Yang: if predictions made by Thursday, ready to hear

---

## Week 1 - June 2nd

(Taken by Yang)

Self-introductions, see [Minecraft UROP Team](project-team).
