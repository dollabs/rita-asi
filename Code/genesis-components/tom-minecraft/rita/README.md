# Theory of Mind in Minecraft within RITA

Updated by Yang on 8 June, 2020

This document includes the setup process and test runs of Yang's Theory of Mind in Minecraft (tom-minecraft) components for DARPA evaluation. For June dry run, tom-minecraft will publish the following messages:

* "raycasting" - a list of 2D coordinates of blocks visible to player's 90-degree point of view at that location and orientation
* "player-strategy" - to be updated
* "player-condition" - to be updated

## Install Python libraries

First, to set up a Python virtual environment for this project, you might need to [install miniconda](https://docs.conda.io/projects/conda/en/latest/user-guide/install/index.html). Another way is to use python's native package manager `pip install` to install the list of libraries instead, but I haven't test if any error will occur if you already have other versions of those libraries installed.

Then, create a python 3.7 virtual environment and install the Python libraries:
```
(base) $ conda create --name mine python=3.7
(base) $ conda activate mine
(mine) $ conda install matplotlib imageio pandas numpy scipy Pillow tqdm networkx moviepy future opencv
```
Some of the above libraries are for visualizing the trajectory on a 2D panel and for generating 2D videos of the missions. They might not be used in the test cases below, but they are necessary for the codes to run successfully.

To run codes in tom-minecraft, you need to activate the environment:
```
(base) $ conda activate mine
(mine) $ conda deactivate     ## exit from the environment
```

## Test raycasting messages

To test the integration of tom-minecraft raycasting messages, we first activate its process:
```
(mine) $ brew services run rabbitmq
(mine) $ cd Code
(mine) $ cd tom-minecraft
(mine) $ cd rita
(mine) $ python rmq-main.py
```

You will see the following output:
```
rmq-main.py as script
exchange: rita, host: localhost, port: 5672
Waiting for commands
```

Then we give it example data that are pre-recorded testbed messages using another terminal window. Note that if you haven't had the csv file in your `Codes/data/` directory or you don't have `rmq-log-player-0.2.0-SNAPSHOT.jar` in your `Codes/target/` directory, download them from `asist-rita` Dropbox and unzip them in their corresponding location.

```
(base) $ cd Code
(base) $ cd target
(base) $ java -jar rmq-log-player-0.2.0-SNAPSHOT.jar -e rita ../data/march-5-5.csv -s 20 -l 400
```

You will see the following in this terminal panel:
```
...
Realtime RMQ log player
{:host "localhost",
 :port 5672,
 :exchange "rita",
 ...
First and last event time in mills: [ 0 15172 ]
Number of events: 2000
Number of timers scheduled: 1948
```

Note: The options `-s 10 -l 2000` means that the 2000 first messages will be replayed at a speed of 10 per second. Nothing will be printed in the first 10 seconds because the first 1000 messages report that the player is at a location outside of the SAR building.

At the same time, you will see the lists of observed block coordinates and the updated maps printed in the panel with rmq-main.py running:
```
2020-03-05T21:41:53.963656Z
{'entity_type': 'human',
 'id': 'b5730d95-d519-3d6d-8764-250de4096d2b',
 ...
       2020-03-05T21:42:46.111683Z (-2191, 179) -95.625
redstone_block ['(-2191,28,189)']
 ... (map)

       2020-03-05T21:42:46.212870Z (-2191, 167) -95.625
redstone_wire ['(-2190,28,170)']
wall_sign ['(-2190,30,168)']
lit_redstone_lamp ['(-2191,30,169)']
lever ['(-2190,29,169)']
clay ['(-2191,28,169)', '(-2191,29,169)', '(-2190,28,169)', '(-2190,30,169)', '(-2189,28,169)', '(-2189,29,169)', '(-2189,30,169)', '(-2190,29,170)']
redstone_block ['(-2191,30,170)']
unpowered_repeater ['(-2191,29,174)']
wool ['(-2191,28,174)']
 ... (map)
```
