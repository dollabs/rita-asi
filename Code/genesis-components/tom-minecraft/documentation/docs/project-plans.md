# Weekly plans for team and individuals

Here are lists of things you can do each week. The most recent weeks come first.

You are not expected to finish them all and you can catch up with the remaining items the next week. We would like to hear your progress and blocks in following the lists at the our Mon 2 pm meeting of the week after the assigned week.

---

## Week 5 - Specify the behavior to model

### For Everyone

In Week 6 team meeting (6 July), UROP students each has 15 minutes to present on player strategy/type they plan to model in [8 ASU test subjects](https://www.dropbox.com/sh/40l4vos7yrtav2c/AADs2CRlcxuhtBkRzgop3Gvka?dl=0). 4-15 pages of slides are recommended. Plan for computational model is optional.

Presenter order: Anna, Isabelle, Christian, and Darren.

Resources:
- [Minecraft World files of the two maps](https://www.dropbox.com/sh/e0geq503o2amshk/AACJAy_Sb5u3qq-4oXX6aGToa?dl=0)

---

## Week 4 - Meeting-free week!

### For Everyone

- Study the human prediction dataset from Pilot Test 3 and summarize your prediction rationale
    - Watch the video clips in subfolders of [player6](https://www.dropbox.com/sh/ag75nd6oscd7zx6/AADrGFFuqXGfO25CBO3EtRmHa?dl=0) and [player10](https://www.dropbox.com/sh/yw817rjdsg8q75i/AABUhL3jfdu-gOZbdbHtDk8Ba?dl=0).
    - At the end of each clip, try to predict what the player will do next (e.g., turn left/right/around, go straight, triage/give up the victim, clear the blockage...). Record your accuracy as if you are a test participant.
    - Summarize your prediction strategy after watching the clips from each test of each player (e.g., according to what the player sees, or what he tends to do in the last mission).
    - Compare your accuracy and prediction strategy with those of human observers
        - Raw data of prediction questions, answers, and commentary: [Google sheet](https://docs.google.com/spreadsheets/d/1rT4H0OONnfuTJHy88t7uk_XbUpz87oeR79LFQmvTIQg/edit?usp=sharing). Let me know if you have questions about the data format in Slack channel #tom-minecraft-repo so everyone may benefit from the answer.
        - Analysis of observer predictions: [Google slides](https://docs.google.com/presentation/d/1RXMbrn1qvFAdB7Xm-xXjeHYyLpsaFo-nNeTyMh-uCUk/edit#slide=id.g772d6496c2_0_335)
        - Summarize lessons learned; what are the different oberver type (just like player type, they prioritize different information for making prediction decisions)
    - upload your summarizes through a markdown file named `tom-minecraft/documentation/notes-YOUR_NAME.md` by Week 4 Friday night. Yang will give everyone feedback and everyone will share a gist in Week 5 team meeting

### For Anna and Isabelle

- Continue the task in Week 3, since there have been changes to the hierarchical planning algorithm.
- Based on the failure cases of the current hierarchical planning algorithm, find out why the agent get stuck, or moves to and back between two tiles, or choose a unintuitive path. Then make your git branch to change the framework (in `visualize.py`, `hierarchical.py`, `mdp.py`, `mapreader.py`)

### For Darren

- Continue the task in Week 3

### For Christian

- Study prerequisite readings for BIRL (details will be discussed in Week 3 individual meeting)


## Week 3 - Try out codes

### For Anna and Isabelle

- identify a set of player types by looking at the visualized player trajectories ([Download link from Dropbox](https://www.dropbox.com/s/tgoas12z2t50rio/Pilot%202%20trajectories%20-%20png.zip?dl=0))
    - look at the existing player profiles in `player.py`, try out different values for each of the attributes and run `visualize.py` with MODE = HIERARCHICAL_PLANNING, note down how each attribute affects the planning agent's behavior.
- create your own player profiles to replicate part of the human trajectories.
    - Name your player profiles like 'systematic_A_2', which means the second systematic player profile used by Anne.
    - We want to make the profiles generalizable both across players and within the different games of one player.
    - You might need a sequence of player profiles to be able to assimilate one trajectory. To let the planning agent start from the time where the human changed player type, you can modify the map or create your own maps.
- summarize the limitations/failure cases of HIERARCHICAL_PLANNING and the current player profile attributes

### For Christian

- go through this week's [reading](literature-weekly.md) as sent via email by Tejas.
- go through the week 2 task for Pilot Test 2 after your ASU test, focusing on identifying how player's reward functions are different
    - Use the visualized player trajectories ([Download link from Dropbox](https://www.dropbox.com/s/tgoas12z2t50rio/Pilot%202%20trajectories%20-%20png.zip?dl=0)) and game recordings

### For Darren

- go through the week 2 task for Pilot Test 2 after your ASU test, focusing on craft stories of selected players and selected games.
    - Use the visualized player trajectories ([Download link from Dropbox](https://www.dropbox.com/s/tgoas12z2t50rio/Pilot%202%20trajectories%20-%20png.zip?dl=0)) and game recordings
    - it might be interesting to craft the story of one player across five missions
    - if you find it hard to identify stories from Pilot Test 2 trajectories, you may use the narrated video recordings from Pilot Test 1
    - you may give names to the rooms and objects, e.g., it's a search specialist in a search and rescue mission, instead of chest-finding mission.
- try parsing your stories using Genesis


## Week 2 - Study human planning

### For Anna, Christian, Isabelle

* Finish watching the player videos and summarizing player behaviors/strategies. Generate theories about how to make decisions in the game and how to predict player decisions.
    - Checkout the resources assigned to you at [Human behavior in Minecraft SAR missions](project-behavior.md).
    - Share your summaries and theories at your individual meeting this Thursday and team meeting next Monday. Use examples, screenshots, and video clips to illustrate your points.
* Be able to explain the inverse planning framework
    - Check out [weekly reading list](literature-prerequisites.md).
* (Postponed until further notice) Create your own student directory inside `tom-minecraft/UROPs` based on the template directory provided inside. Create your own 12 by 12 map and three player configurations to test the planning algorithms and inverse planning framework. Add more modes or features to your local gridworld if capable.

### For Christain and Darren

* Participate in a 2-hour ASU SAR Pilot experiment. Note down your decision making processes in the two 10-min missions. Think about how you might improve your performance (e.g., get more scores) if you were given a second chance to play each maze.
    - Share your experience and strategic lessons learned at your individual meeting this Thursday (if possible) and team meeting next Monday. Use the [maps of the buildings](https://www.dropbox.com/sh/e0geq503o2amshk/AACJAy_Sb5u3qq-4oXX6aGToa?dl=0) as your demo prop.


## Week 1 - Set up

### For Anna, Christian, Isabelle, and Darren

* Follow the instructions in 'SET IT UP' and 'RUN IT ONCE'
    - You should be able to run the test examples in `gridworld/visualize.py` by following the [test case instructions](tut-planning.md). Yang will demonstrate the test cases on Week 2 Monday 2:30-3pm
    - You should be able to run `gridworld/malmo.py` and find yourself in a map that assimilates Stata center floor 1
* Play the 'Singleplayer' Minecraft search and rescue games on your laptop
    - You need to have [set up Malmo](setup-Malmo.md) and download the two world zipped files ([Dropbox link](https://www.dropbox.com/sh/e0geq503o2amshk/AACJAy_Sb5u3qq-4oXX6aGToa?dl=0)) to your `YOUR_WORKING_DIRECTORY/Malmo/Minecraft/run/saves/` folder, unzip, launch Malmo client, then find the world in the Minecraft interface -> Singleplayer mode. Give yourself 10 minutes to see how many victims you can find.
* Articulate your theories of human player's decision making process
    - You are recommended to view the videos [listed here](project-SAR.md). While you watch, try to predict what the player will do next. Try if your theories of how to make better predictions work by applying it in your mind when watching the next player.
* Be able to explain MDP and value iteration algorithm
    - Check out [prerequisite reading list](literature-prerequisites.md). Tejas will answer questions about it on Week 2 Tuesday OH 1:30-2:30pm

Don't hesitate to ask questions in Slack channel #tom-minecraft-repo and #reading-list, or directly message Yang. Slack is preferred over email :)
