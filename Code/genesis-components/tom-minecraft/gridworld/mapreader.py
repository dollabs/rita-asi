import csv
import pandas as pd
import numpy as np
import time
import copy
import math
import os
from os.path import join, isfile

import visualize
import player
import raycasting
import utils

DEBUG = True


def get_offset():
    LENGTH = int(360/visualize.TILE_SIZE)
    offset_horizontal = int(- LENGTH/2 + 1) # -2
    offset_vertical = int(LENGTH/2) # 3
    if visualize.USE_STATA: offset_horizontal -= 12
    return offset_horizontal, offset_vertical

def coord(pos):
    i, j = pos
    return -j, i

    # offset_horizontal, offset_vertical = get_offset()
    # i = i - offset_horizontal
    # j = - j + offset_vertical
    # return i,j

def summarize_rooms(data):
    """
    rooms is the dict that summarizes room info, it consists of
    0: {
        'tiles': [0, 1, 5, 6, 10],
        'top': 0, 'bottom': 2, 'left': 0, 'right': 1,
        'doors': [10],
        'neighbors': { 2: [(10, 13, 270)] },
        'bark': 0,
        'centroid': 5,
        'centroid_pos': (2, 3),
        'actions': {2: [5, 6, 10, 13, 16, 14]}  ## run A* on the tiles in the two rooms
    },
    """

    rooms = {}
    tiles = {}
    tiles2room = {}
    index = 0
    for i in range(len(data)):
        for j in range(len(data[0])):
            item = data[i][j]
            tiles[(i,j)] = '-'
            if item != 'W':
                tiles[(i,j)] = index
                room = int(item)
                tiles2room[index] = room
                if room not in rooms.keys():
                    rooms[room] = {}
                    rooms[room]['tiles'] = [index]
                    rooms[room]['top'] = len(data)
                    rooms[room]['bottom'] = 0
                    rooms[room]['left'] = len(data[0])
                    rooms[room]['right'] = 0

                else:
                    rooms[room]['tiles'].append(index)

                if i < rooms[room]['top']:
                    rooms[room]['top'] = i
                if i > rooms[room]['bottom']:
                    rooms[room]['bottom'] = i
                if j < rooms[room]['left']:
                    rooms[room]['left'] = j
                if j > rooms[room]['right']:
                    rooms[room]['right'] = j

                # index += 1
            else:
                tiles2room[index] = None
            index += 1

    ## estimate the centroid of every room
    for room_index, room in rooms.items():
        c_x, c_y = round((room['top']+room['bottom'])/2), round((room['left']+room['right'])/2)
        room['centroid_pos'] = (c_x, c_y)

    return rooms, tiles, tiles2room

def read_rooms(file_name, tilesummary, floor = None):

    # if file_name == None:
    #     return None, tilesummary, None

    ## read from floor file if not given the floor table
    if floor == None:
        with open(join('maps',file_name), encoding='utf-8-sig') as csv_file:
            floor = list(csv.reader(csv_file, delimiter=','))
    rooms, tiles, tiles2room = summarize_rooms(floor)

    # for each room
    for index in rooms.keys():
        room = rooms[index]
        room['doors'] = []
        room['doorsteps'] = []
        room['tilesummary'] = {}
        BARK = 0
        for tile in room['tiles']:
            room['tilesummary'][tile] = {}

            ## for checking how the dog will bark at the doors of the room
            if tilesummary[tile]['type'] == 'victim-yellow':
                BARK = 2
            elif tilesummary[tile]['type'] == 'victim' and BARK == 0:
                BARK = 1

            ## only doors that exist between rooms
            elif tilesummary[tile]['type'] == 'door':
                adj = []
                for head in [0, 90, 180, 270]:
                    adj_tile = tilesummary[tile][head]
                    if adj_tile != 'wall':
                        adj.append(tiles2room[adj_tile])
                if len(set(adj)) == 2:
                    rooms[index]['doors'].append(tile)

            ## for adding neighbors to the room
            if 'neighbors' not in room.keys():
                rooms[index]['neighbors'] = {}
            for angle in [0,90,180,270]:
                other = tilesummary[tile][angle]
                if other != 'wall' and tile != None:
                    if tiles2room[other] != index and tiles2room[other] != None:
                        if tiles2room[other] not in rooms[index]['neighbors']:
                            rooms[index]['neighbors'][tiles2room[other]] = []
                        rooms[index]['neighbors'][tiles2room[other]].append((tile, other, angle))

        rooms[index]['bark'] = BARK

    def get_xy(loc):
        if isinstance(loc, tuple):
            return loc
        else:
            return tilesummary[loc]['col'], tilesummary[loc]['row']

    def get_dist(loc1, loc2):
        x1, y1 = get_xy(loc1)
        x2, y2 = get_xy(loc2)
        return math.sqrt((x1-x2)**2 + (y1-y2)**2)

    ## estimate the distance between every pair of neighboring rooms
    for room_index, room in rooms.items():
        mycen = room['centroid_pos']
        room['actions'] = {}
        ## there may not be any neighbors if the player doesn't assume there must be a way to get there
        if 'neighbors' in room:
            for neighbor, adjacents in room['neighbors'].items():
                yourcen = rooms[neighbor]['centroid_pos']
                shortest_dist = np.inf
                shortest_path = []
                for mydoor, yourdoor, angle in adjacents:
                    mydoor = get_xy(mydoor)
                    yourdoor = get_xy(yourdoor)
                    dist = get_dist(mydoor, mycen) + get_dist(yourdoor, yourcen) + 1
                    if dist < shortest_dist:
                        shortest_dist = dist
                        shortest_path = [mycen, mydoor, yourdoor, yourcen]
                room['actions'][neighbor] = (round(shortest_dist,2), shortest_path)

    return rooms, tiles2room, floor

def read_cvs(file_name, rewards, LEARNING=False):

    if visualize.LEARNING: LEARNING = True
    offset_horizontal, offset_vertical = get_offset()

    type_to_grid = {
        'victim':'V', 'victim-yellow':'VV', 'victim-red':'VR', 'fire':'F', 'gravel':'G', 'door':'D', 'wall':'W', 'air':'',
        'hand-sanitizer': 'H', 'entrance':'E', 'grass':'O', 'bookshelf':'B',
        'iron':'V', 'diamond':'VV', 'crafting-iron': 'R', 'crafting-diamond': 'N',
    }
    grid_to_type = {
        '':'air', 'S':'air', '0':'air', '90':'air', '180':'air', '270':'air',
        'V':'victim', 'VV':'victim-yellow', 'VR':'victim-red', 'F':'fire', 'G':'gravel', 'D':'door',
        'E':'entrance', 'O':'grass', 'W':'wall', 'H':'victim', 'B':'bookshelf'
    }
    reward_by_grid = {
        'S':0, '0':0, '90':0, '180':0, '270':0,
        'F':0, 'G':0, 'O':0, 'E':0, 'B':0,
    }

    for type in rewards.keys():
        reward_by_grid[type_to_grid[type]] = rewards[type]

    with open(join('maps', file_name), encoding='utf-8-sig') as csv_file:
        csv_reader = csv.reader(csv_file, delimiter=',')
        world_grids = []
        for row in csv_reader:
            world_grids.append(row)
        world_height = len(world_grids)
        world_width = len(world_grids[0])

    tile_index = 0

    victim_summary = {}
    victim_summary['V'] = []
    victim_summary['VV'] = []

    tile_indices = {} # mapping (x,y) coord to tile index
    tilesummary = {}
    FOUND_START = False
    annotated_csv = copy.deepcopy(world_grids)

    for i in range(world_height):
        for j in range(world_width):
            grid = str(world_grids[i][j])

            ## certain blocks exist for decorations only
            if grid == 'B': grid = 'W'
            # if grid == 'T' or grid == 'B': grid = ''   ## table and bench
            # if grid == 'O' or grid == 'E': grid = 'W'  ## Elevator door
            if grid == 'H': grid = 'V'  ## Iron wall
            if grid == 'I': grid = 'W'  ## Door to iron wall
            if grid == 'L': grid = 'D'  ## Elevator door
            if ':' in grid: grid = ''  ## Signage

            ## blocks useful for the missions
            if grid == 'S' or grid == '0':
                FOUND_START = True
                start_index = tile_index
                start_heading = 0
            elif grid == '90':
                FOUND_START = True
                start_index = tile_index
                start_heading = 90
            elif grid == '180':
                FOUND_START = True
                start_index = tile_index
                start_heading = 180
            elif grid == '270':
                FOUND_START = True
                start_index = tile_index
                start_heading = 270

            elif grid == 'V' or grid == 'VV' or grid == 'H':
                ## need the list of victim location for changing victim death
                victim_summary[grid].append(tile_index)

            if grid != 'W' or LEARNING or True:
                grid_states = {}
                grid_states['pos'] = (i,j)
                grid_states['type'] = grid_to_type[grid]
                tile_indices[(i,j)] = tile_index
                grid_states['reward'] = reward_by_grid[grid]

                ## add the outermost walls
                # if not visualize.LEARNING:
                for item in ['W','E']:
                    if j == world_width - 1 or world_grids[i][j+1] == item:
                        grid_states[0] = "wall"
                    if i == 0 or world_grids[i-1][j] == item:
                        grid_states[90] = "wall"
                    if j == 0 or world_grids[i][j-1] == item:
                        grid_states[180] = "wall"
                    if i == world_height - 1 or world_grids[i+1][j] == item:
                        grid_states[270] = "wall"

                tilesummary[tile_index] = grid_states
                annotated_csv[i][j] = tile_index
                tile_index += 1

            if grid == 'W':
                annotated_csv[i][j] = 'W'

    ## generate csv file with the tile indices
    tiles_file = join('maps', file_name.replace('.csv','_tiles.csv'))
    if not isfile(tiles_file) or True:
        with open(tiles_file, "w", newline="") as f:
            writer = csv.writer(f)
            writer.writerows(annotated_csv)

    if not FOUND_START:
        start_heading = 0
        start_index = 0
        while tilesummary[start_index]['type'] == 'wall':
            start_index += 1

    # for key, value in tilesummary.items():
    #     print(key, ':', value,',')

    ## add neighboring walls
    for k in tilesummary.keys():
        tile = tilesummary[k]
        i,j = tile['pos']
        if 0 not in tile.keys():
            if (i,j+1) in tile_indices.keys():
                tile[0] = tile_indices[(i,j+1)]
            else:
                tile[0] = 'wall'
        if 90 not in tile.keys():
            if (i-1,j) in tile_indices.keys():
                tile[90] = tile_indices[(i-1,j)]
            else:
                tile[90] = 'wall'
        if 180 not in tile.keys():
            if (i,j-1) in tile_indices.keys():
                tile[180] = tile_indices[(i,j-1)]
            else:
                tile[180] = 'wall'
        if 270 not in tile.keys():
            if (i+1,j) in tile_indices.keys():
                tile[270] = tile_indices[(i+1,j)]
            else:
                tile[270] = 'wall'
        tile['row'] = i
        tile['col'] = j
        # tile['pos'] = (j + offset_horizontal, -i + offset_vertical)
        tile['pos'] = (j, -i)

    # print(tilesummary)
    return world_grids, tile_indices, tilesummary, start_index, start_heading, victim_summary

def print_shadow(env, s, angle=45, radius=60, n_rays=70, INIT=False):  ## radius=80
    tilesummary = env.tilesummary_truth
    world_grids = env.world_grids_truth
    if INIT:
        tilesummary = env.tilesummary
        world_grids = env.world_grids
    tile_indices = env.tile_indices

    ## convert the world grid to array of arrays - takes 0.015 seconds
    grids = pd.DataFrame(world_grids).to_numpy()
    world_height = len(world_grids)
    world_width = len(world_grids[0])
    world = []
    for i in range(world_height):
        row = []
        for j in range(world_width):
            if (i,j) not in tile_indices:
                grids[i][j] = 'w'
            elif grids[i][j] == 'W':
                grids[i][j] = 'w'
            elif grids[i][j] == 'G' or grids[i][j] == 'D':
                grids[i][j] = 'w'
            else:
                grids[i][j] = '.'
            row.append(grids[i][j])
        world.append(row)

    ## run raycast algorithm - takes 0.025 - 0.095 seconds
    # j,i = coord(tilesummary[s[0]]['pos'])
    i, j = coord(tilesummary[s[0]]['pos'])
    obs = raycasting.get_obs(world,((i,j),s[1]), n_rays=n_rays, angle=angle, radius=radius)

    ## remove the walls
    new_obs = []
    for tile in obs:
        if tile in tile_indices:
            new_obs.append(tile_indices[tile])

    new_obs.append(s[0])

    # print('... finished updating observation using raycasting at', ((i,j),s[1]),' in', str(time.time() - start), 'seconds')

    return list(set(new_obs))

# if __name__ == '__main__':
    # world_grids, tile_indices, tilesummary, start_index, victim_summary = read_cvs('6by6_1.csv')
    # print(tilesummary, start_index, victim_summary)
    # read_rooms('6by6_rooms.csv',tilesummary)
    # print(print_shadow(world_grids, tilesummary, tile_indices, (2,0), 0))
