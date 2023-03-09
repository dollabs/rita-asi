import time
from pprint import pprint
import numpy as np
import pandas as pd
import math
import json

def clip(x,up,low=0):
    return max(low,min(x,up))

def get_visible(slopes, r, c, head, radius, walls):

    visible = {(r,c)}

    for a in slopes:

        y = lambda xx: (a * xx + (c-a*r))

        if head == 270:
            if a >= 0:
                x = lambda yy: ((yy-c)/a + r) * (yy > c) if a else float('inf')
                adj = ((0,1),(1,0))
                row_offset = -0.5
                col_offset = -0.5
            elif a < 0:
                x = lambda yy: ((yy-c)/a + r) * (yy < c)
                adj = ((1,0),(0,-1))
                row_offset = -0.5
                col_offset =  0.5

        elif head == 90:
            if a >= 0:
                x = lambda yy: ((yy-c)/a + r) * (yy < c) if a else float('inf')
                adj = ((-1,0),(0,-1))
                row_offset =  0.5
                col_offset =  0.5
            elif a < 0:
                x = lambda yy: ((yy-c)/a + r) * (yy > c)
                adj = ((0,1),(-1,0))
                row_offset =  0.5
                col_offset = -0.5

        elif head == 0:
            if a >= 0:
                x = lambda yy: ((yy-c)/a + r)*(yy>c) if a else r
                adj = ((1,0),(0,1))
                row_offset = -0.5
                col_offset = -0.5
            elif a < 0:
                x = lambda yy: ((yy-c)/a + r)*(yy>c) if a else r
                adj = ((0,1),(-1,0))
                row_offset =  0.5
                col_offset = -0.5

        elif head == 180:
            if a >= 0:
                x = lambda yy: ((yy-c)/a + r)*(yy<c) if a else r
                adj = ((0,-1),(-1,0))
                row_offset =  0.5
                col_offset =  0.5
            elif a < 0:
                x = lambda yy: ((yy-c)/a + r)*(yy<c) if a else r
                adj = ((1,0),(0,-1))
                row_offset =  0.5
                col_offset = -0.5

        r_,c_ = r,c
        hits = [(r_,c_)]

        for _ in range(radius):
            neigh = [ (r_+i,c_+j) for i,j in adj if (r_+i,c_+j) not in hits ]
            rc_ = [(rr,cc) for rr,cc in neigh if cc-0.5 <= y(rr+row_offset) <= cc+0.5 or rr-0.5 <= x(cc+col_offset) <= rr+0.5]
            if len(rc_) > 0:
                r_,c_ = rc_[0]
            hits.append((r_,c_))
            if (r_,c_) in walls:
                break
        visible.update(hits)

    return visible

def raycast(gridworld,state,n_rays=100,angle=np.pi*4/9,radius=15):

    (r,c), head = state
    summary = world_summary(gridworld)
    walls = [wall for wall in summary if summary[wall]['tag']=='w']

    # print(walls)
    x_min = np.inf
    x_max = -np.inf
    y_min = np.inf
    y_max = -np.inf
    for tile in walls:
        x,y = tile
        if x < x_min: x_min = x
        if x > x_max: x_max = x
        if y < y_min: y_min = y
        if y > y_max: y_max = y

    ## in replay, yaw data is continuous
    if type(angle)==tuple:
        angle1o, angle2o = angle
        # print(angle1o, angle2o)
        angle1 = math.radians(angle1o)
        angle2 = math.radians(angle2o)

        if head in (90,270):
            slopes = [math.tan(-angle1 + angle1/(n_rays/2)*k)for k in range(1,round(n_rays/2))]
            slopes += [math.tan(-angle2 + angle2/(n_rays/2)*k)for k in range(round(n_rays/2),n_rays)]

        elif head == 180:
            if angle1o <= 0 and angle1o >= -45:
                slopes = [math.tan(np.pi/2 + angle1 - angle1/(n_rays)*k)for k in range(1,round(n_rays/2))]
            else: # (angle1o < -45 and angle1o >= -90):
                slopes = [math.tan(np.pi/2 + angle1 - angle1/(n_rays/2)*k)for k in range(1,round(n_rays/2))]

            slopes += [math.tan(np.pi/2 - angle2 + angle2/(n_rays/2)*k)for k in range(round(n_rays/2),n_rays)]

        elif head == 0:
            slopes = [math.tan(np.pi/2 - angle1 + angle1/(n_rays)*k)for k in range(1,round(n_rays/2))]

            if (angle2o >= 0 and angle2o < 45):
                slopes += [math.tan(np.pi/2 - angle2 + angle2/(n_rays)*k)for k in range(round(n_rays/2),n_rays)]
            else: # (angle2o >= 45 and angle2o <= 90):
                slopes += [math.tan(np.pi/2 - angle2 + angle2/(n_rays/2)*k)for k in range(round(n_rays/2),n_rays)]

    ## when angle is fixed
    else:

        ## when angle is specified in degrees, otherwise in radians
        if type(angle)==int:
            angle = math.radians(angle)

        if head in (90,270):
            slopes = [math.tan(-angle + angle/(n_rays/2)*k)for k in range(1,n_rays)]
        elif head in (0,180):
            slopes = [math.tan(np.pi/2 - angle + angle/(n_rays/2)*k)for k in range(1,n_rays)]

    visible = get_visible(slopes, r, c, head, radius, walls)

    ## eliminate blocks that's outside of range
    df = pd.DataFrame(gridworld, index = range(len(gridworld)), columns = range(len(gridworld[0])))
    visible_new = []
    for light in visible:
        if light[0] in df.index and light[1] in df.columns:
            visible_new.append(light)

    return visible_new

def world_summary(gridworld):
    """
    * input: gridworld is a (list) of (list) of tile tags ('?', '.', 'w', 's', 'd', 'v', 'p')
    * output: summary includes the type of block in cell and the index of its neighbors
    """

    summary = {}
    nrows, ncols = len(gridworld), len(gridworld[0])

    for r,row in enumerate(gridworld):
        for c,tag in enumerate(row):
            summary[(r,c)] = {  0: (r, clip(c+1,ncols-1)),
                               90: (clip(r-1,nrows-1), c),
                              180: (r, clip(c-1,ncols-1)),
                              270: (clip(r+1,nrows-1), c),
                              'tag': tag}
    return summary

def get_obs(world,state,n_rays=70,angle=np.pi*4/9,radius=15):

    ray = raycast(world,state,n_rays=n_rays,angle=angle,radius=radius)
    world_height = len(world)
    world_width = len(world[0])
    new_obs = []
    for tile in ray:
        i,j = tile
        if i >= 0 and i < world_height and j >= 0 and j < world_width:
            new_obs.append((i,j))

    return new_obs

def show(world, state, ray): ## print a dataframe in log
    df = pd.DataFrame(world, index = range(len(world)), columns = range(len(world[0])))
    for light in ray:
        if df.loc[light] != 'w':
            df.loc[light] = '+'
    df.loc[state[0]] = state[1]
    print(df)
    # print(ray)

def show_df(df, state, ray): ## print a dataframe in log
    for light in ray:
        if df.loc[light] != 'w':
            df.loc[light] = '+'
            # print(light)
            # print(df.loc[light])
    df.loc[state[0][1],state[0][0]] = state[1]
    print(df)
    return df

def json_to_world(blocks_in_building_file):
    """
        input: blocks_in_building.json files generated by tom-minecraft/world-builder/map_generator.py
        output: 2D world list and pandas world dataframe
    """
    objects2grids = {
        'wool':'v',
        'prismarine':'v',
        'gold_block':'vv',
        'wooden_door':'w',
        'gravel':'w',
        'fire':'',
        'air':''
    }

    with open(blocks_in_building_file) as json_file:
        data = json.load(json_file)
        blocks = data['blocks']

        # print(data['region'].values())
        x_low, x_high, z_low, z_high, y_low, y_high = data['region'].values()

        ## for Singleplayer
        x_high = -2142

        world = []
        for z in range(z_low, z_high):
            row = []
            for x in range(x_low, x_high):
                key = str((x,y_low+1,z)).replace('(','').replace(' ','').replace(')','')
                type = blocks[key]
                if type not in objects2grids.keys():
                    type = 'w'
                else:
                    type = objects2grids[type]
                row.append(type)
            world.append(row)

        df = pd.DataFrame(world, index = range(z_low, z_high), columns = range(x_low, x_high))

    return world, df

def test_1():
    world = [['.', '.', '.', '.', '.', '.', '.', '.', '.'],
             ['.', '.', '.', '.', '.', '.', '.', '.', '.'],
             ['.', '.', '.', '.', '.', '.', '.', '.', '.'],
             ['.', '.', '.', '.', '.', '.', '.', '.', '.'],
             ['.', '.', 'w', '.', '.', '.', '.', '.', 'w'],
             ['.', '.', '.', '.', '.', '.', 'w', 'w', '.'],
             ['.', '.', '.', '.', 'w', '.', 'w', '.', '.'],
             ['.', '.', '.', '.', '.', '.', '.', '.', '.'],
             ['.', '.', '.', '.', '.', '.', '.', '.', '.'],
             ['.', '.', '.', '.', '.', '.', '.', '.', '.'],
             ['.', '.', '.', '.', '.', '.', '.', '.', '.'],]
    # obstacles = ((4,2),(4,8),(5,6),(5,7),(6,6),(6,4),(6,3))

    state = ((6,3),0)
    ray = raycast(world, state)
    show(world, state, ray)


def test_2():
    world = [['.', '.', 'w', '.', '.', '.'],
             ['.', '.', 'w', '.', '.', '.'],
             ['w', 'w', 'w', 'w', 'w', 'w'],
             ['w', 'w', '.', '.', '.', '.'],
             ['.', '.', 'w', 'w', 'w', 'w'],
             ['.', '.', 'w', '.', '.', '.'],
             ['.', '.', 'w', '.', '.', '.']]

    state = ((5,0),0)
    ray = get_obs(world,state)
    show(world, state, ray)


def raycast2type(rays, blocks_file):
    blocks_observed = {}
    with open(blocks_file) as json_file:
        data = json.load(json_file)
        blocks = data['blocks']
        x_low, x_high, z_low, z_high, y_low, y_high = data['region'].values()

    for light in rays:
        z, x = light
        for y in range(y_low, y_high+1):
            key = str(x) + ',' + str(y) + ',' + str(z)
            blocks_observed[key] = blocks[key]

    return blocks_observed


def print_by_type(blocks_observed):
    types = set(blocks_observed.values())
    blocks_by_types = {}
    for type in types:
        blocks_by_types[type] = []

    for block,type in blocks_observed.items():
        blocks_by_types[type].append('('+block+')')

    for type in types:
        if type != 'air':
            print(type, blocks_by_types[type])
    print()


def test_3(location, yaw, blocks_file):

    world, df = json_to_world(blocks_file)

    def loc_to_index(loc): ## e.g., from (-2192, 144) to (0,0)
        return (loc[0]-df.columns.start, loc[1]-df.index.start)

    def index_to_loc(index): ## e.g., from (0,0) to (-2192, 144)
        return (index[1]+df.index.start, index[0]+df.columns.start)

    def yaw2head(yaw):
        if (yaw <= 45 and yaw > -45) or (yaw > 270 + 45) or (yaw < - 270 - 45):
            return 270
        elif (yaw <= 90 + 45 and yaw > 45) or (yaw <= -45 - 180 and yaw > -45 -270):
            return 180
        elif (yaw <= 180 + 45 and yaw > 90 + 45) or (yaw <= -45 -90 and yaw > -45 -180):
            return 90
        else:
            return 0

    def yaw2angle(yaw):
        if yaw <= 0: yaw += 360

        ## 270 right, left
        if (yaw > 0 and yaw <= 45):
            angle = (45+yaw, 45-yaw)

        ## 180 right
        elif yaw > 45 and yaw <= 135:
            angle = (45-yaw, 45+(90-yaw))

        ## 90 right
        elif yaw > 135 and yaw <= 225:
            angle = (45-(180-yaw),45+(180-yaw))

        ## 0 right
        elif yaw > 225 and yaw <= 315:
            angle = (45-(270-yaw), 45+(270-yaw))

        ## 270 right left
        elif yaw > 315 and yaw <= 360:
            angle = (45-(360-yaw), 45+(360-yaw))

        print(angle)
        return angle

    ## get raycasting result
    head = yaw2head(yaw)
    state = (loc_to_index(location), head)
    ray = get_obs(world, state, angle = yaw2angle(yaw))
    ray_new = []
    for light in ray:
        z, x = index_to_loc(light)
        if x > df.columns.start and x < df.columns.stop and  z > df.index.start and z < df.index.stop:
            ray_new.append(index_to_loc(light))
    blocks_observed = raycast2type(ray_new, blocks_file)

    ## print the blocks by type
    print_by_type(blocks_observed)

    ## print current location and raycasted blocks onto the map
    state = (location, head)
    df = show_df(df, state, ray_new)

    return blocks_observed

    # print(loc_to_index((-2192, 144)))
    # print(loc_to_index((0, 0)))
    # print(df.loc[(166, -2187)])
    # print(df[-2187][166])


if __name__ == '__main__':

    # test_1()
    # test_2()
    test_3((-2187,166))
