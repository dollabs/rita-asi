import numpy as np
import pdb
import time
import pprint
import random

class MDP:
    def __init__(self):

        """
        0    1   2   *3
        4   *5   6    7
        8   *9   10  [11]

        Gridworld wherein an agent needs to navigate to the goal (i.e. grid 11).
        The agent may start from any grid facing any direction (north, south, east, west).
        The agent will receive +1 reward at grid 11 and -1 reward at grids 3,5,9 (marked by *).
        This MDP class defines a state transition function T, and a reward function R.
        State is defined as (grid number,heading).
        """
        
        # action space
        self.actions = ('go_forth','turn_left','turn_right')
        # state space
        self.states = tuple([(tile,head) 
                        for tile in range(12) 
                        for head in (0,90,180,270)]) 
        # data for ease of computation: specific to this MDP
        self.tilesummary = {
            0: {0:1,90:'wall',180:'wall',270:4,'reward':0,'pos':(-2,2)},
            1: {0:2,90:'wall',180:0,270:5,'reward':0,'pos':(-1,2)},
            2: {0:3,90:'wall',180:1,270:6,'reward':0,'pos':(0,2)},
            3: {0:'wall',90:'wall',180:2,270:7,'reward':-1,'pos':(1,2)},
            4: {0:5,90:0,180:'wall',270:8,'reward':0,'pos':(-2,1)},
            5: {0:6,90:1,180:4,270:9,'reward':-1,'pos':(-1,1)},
            6: {0:7,90:2,180:5,270:10,'reward':0,'pos':(0,1)},
            7: {0:'wall',90:3,180:6,270:11,'reward':0,'pos':(1,1)},
            8: {0:9,90:4,180:'wall',270:'wall','reward':0,'pos':(-2,0)},
            9: {0:10,90:5,180:8,270:'wall','reward':-1,'pos':(-1,0)},
            10: {0:11,90:6,180:9,270:'wall','reward':0,'pos':(0,0)},
            11: {0:'wall',90:7,180:10,270:'wall','reward':1,'pos':(1,0)}
        }

    def T(self,s,a):
        """ transition function """
        tile,head = s

        if a == 'go_forth':
            tile = self.tilesummary[tile][head]
        elif a == 'turn_left':
            head = np.mod(head+90,360)
        elif a == 'turn_right':
            head = np.mod(head-90,360)

        return (tile,head)
    
    def R(self,s):
        """ reward function """
        
        return self.tilesummary[s[0]]['reward']

if __name__ == '__main__':
    env = MDP()
