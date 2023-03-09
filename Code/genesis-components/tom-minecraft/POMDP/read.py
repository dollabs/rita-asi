import numpy as np
import pdb
import time
import pprint
import random

import csv # for reading csv
import pandas as pd # for reading csv

REWARD = {'G':1, 'W':0, 'S':0}

def clip(x,up):
    return max(min(x,up),0)

def line_of_sight(tilesummary,s):
    """ takes in partial information of s: (tile,head) """

    tile,head = s[:2]
    tile_p = tilesummary[tile][head]

    if tile_p=='wall':
        return []

    tile_p = tilesummary[tile][head]
    
    return [tile_p] + line_of_sight(tilesummary,(tile_p,head))

def area_of_sight(tilesummary,s):
    """ takes in partial information of s: (tile,head) """

    tile,head = s[:2]
    area_visible = line_of_sight(tilesummary,(tile,head))

    # XXX
    tile_p = tilesummary[tile][head]
    if tile_p != 'wall':

        tile_p_left = tilesummary[tile_p][np.mod(head+90,360)]
        if tile_p_left != 'wall':
            left_ahead = line_of_sight(tilesummary,(tile_p_left,head))
            area_visible += [tile_p_left,] + left_ahead 

        tile_p_right = tilesummary[tile_p][np.mod(head-90,360)]
        if tile_p_right != 'wall':
            right_ahead = line_of_sight(tilesummary,(tile_p_right,head))
            area_visible += [tile_p_right,] + right_ahead

    return [tile] + area_visible


def read_csv(file_name):

    with open(file_name, encoding='utf-8-sig') as csv_file:
        csv_reader = csv.reader(csv_file)

        world_grids = [row for row in csv_reader]
        n_row, n_col = len(world_grids),len(world_grids[0])

    tilesummary = {}

    for r,row in enumerate(world_grids):
        for c,value in enumerate(row):

            temp = {'pos': (n_row-r-(n_row-1)/2,c-(n_col-1)/2), # reorient (row,col) & match center of gridworld to (0,0)
                    'tag': world_grids[r][c]}

            head_coord = dict((head,(clip(r+i,n_row-1),clip(c+j,n_col-1))) for head,i,j in ((0,0,1),(90,-1,0),(180,0,-1),(270,1,0)))

            for head,(rr,cc) in head_coord.items():
                temp.update({head: 'wall' if (rr,cc)==(r,c) or world_grids[rr][cc]=='W' else rr*n_col + cc})
            
            tilesummary.update({r*n_col+c:temp})

    for tile in tilesummary:
        tilesummary[tile]['ahead'] = {}
        for head in (0,90,180,270):
            tilesummary[tile]['ahead'][head] = area_of_sight(tilesummary,(tile,head))

    return tilesummary
