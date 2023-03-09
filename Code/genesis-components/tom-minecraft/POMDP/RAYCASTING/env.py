import time
import pprint 
import numpy as np
import math

def clip(x,up,low=0):
    return max(low,min(x,up))

def raycast270(gridworld,state,n_rays=70,angle=np.pi*4/9,radius=15):

    (r,c), head, _, _ = state
    summary = world_summary(gridworld)

    walls = {wall for wall in summary if summary[wall]['tag']=='w'}
    slopes = [math.tan(-angle + angle/(n_rays/2)*k)for k in range(1,n_rays)]

    visible = set()

    for a in slopes:

        y = lambda xx: (a * xx + (c-a*r))

        if a >= 0:

            x = lambda yy: ((yy-c)/a + r) * (yy > c) if a else float('inf')

            neigh = [ (r+i,c+j) for i,j in ((0,1),(1,0)) ]
            r_,c_ = [(rr,cc) for rr,cc in neigh \
                        if cc-0.5 <= y(rr-0.5) <= cc+0.5 or rr-0.5 <= x(cc-0.5) <= rr+0.5][0]

            neigh = [ (r_+i,c_+j) for i,j in ((0,1),(1,0)) ]
            temp = [(r_,c_)]

            for _ in range(radius):
                for rr,cc in neigh:
                    if cc-0.5 <= y(rr-0.5) <= cc+0.5 or rr-0.5 <= x(cc-0.5) <= rr+0.5:
                        temp.append((rr,cc))
                        if (rr,cc) in walls:
                            break

                        r_,c_ = rr,cc
                visible.update(temp)
                neigh = [ (r_+i,c_+j) for i,j in ((0,1),(1,0)) if (r_+i,c_+j) not in temp]

        elif a < 0:
            
            x = lambda yy: ((yy-c)/a + r) * (yy < c)

            neigh = [ (r+i,c+j) for i,j in ((1,0),(0,-1)) ]
            r_,c_ = [(rr,cc) for rr,cc in neigh \
                        if cc-0.5 <= y(rr-0.5) <= cc+0.5 or rr-0.5 <= x(cc+0.5) <= rr+0.5][0]

            neigh = [ (r_+i,c_+j) for i,j in ((1,0),(0,-1)) ]
            temp = [(r_,c_)]

            for _ in range(radius):
                for rr,cc in neigh:
                    if cc-0.5 <= y(rr-0.5) <= cc+0.5 or rr-0.5 <= x(cc+0.5) <= rr+0.5:
                        temp.append((rr,cc))
                        if (rr,cc) in walls:
                            break

                        r_,c_ = rr,cc
                visible.update(temp)
                neigh = [ (r_+i,c_+j) for i,j in ((1,0),(0,-1)) if (r_+i,c_+j) not in temp]

    visible.update({(r,c)})
    return visible

def raycast90(gridworld,state,n_rays=70,angle=np.pi*4/9,radius=15):

    (r,c), head, _, _ = state
    summary = world_summary(gridworld)

    walls = {wall for wall in summary if summary[wall]['tag']=='w'}
    slopes = [math.tan(-angle + angle/(n_rays/2)*k)for k in range(1,n_rays)]
    
    visible = set()

    for a in slopes:

        y = lambda xx: (a * xx + (c-a*r))

        if a < 0:

            x = lambda yy: ((yy-c)/a + r) * (yy > c)

            neigh = [ (r+i,c+j) for i,j in ((0,1),(-1,0)) ]

            r_,c_ = [(rr,cc) for rr,cc in neigh \
                        if cc-0.5 <= y(rr+0.5) <= cc+0.5 or rr-0.5 <= x(cc-0.5) <= rr+0.5][0]
            
            neigh = [ (r_+i,c_+j) for i,j in ((0,1),(-1,0)) ]
            temp = [(r_,c_)]

            for _ in range(radius):
                for rr,cc in neigh:
                    if cc-0.5 <= y(rr+0.5) <= cc+0.5 or rr-0.5 <= x(cc-0.5) <= rr+0.5:
                        temp.append((rr,cc))
                        if (rr,cc) in walls:
                            break

                        r_,c_ = rr,cc
                visible.update(temp)
                neigh = [ (r_+i,c_+j) for i,j in ((0,1),(-1,0)) if (r_+i,c_+j) not in temp]

        elif a >= 0:
            
            x = lambda yy: ((yy-c)/a + r) * (yy < c) if a else float('inf')

            neigh = [ (r+i,c+j) for i,j in ((-1,0),(0,-1)) ]
            r_,c_ = [(rr,cc) for rr,cc in neigh \
                        if cc-0.5 <= y(rr+0.5) <= cc+0.5 or rr-0.5 <= x(cc+0.5) <= rr+0.5][0]

            neigh = [ (r_+i,c_+j) for i,j in ((-1,0),(0,-1)) ]
            temp = [(r_,c_)]

            for _ in range(radius):
                for rr,cc in neigh:
                    if cc-0.5 <= y(rr+0.5) <= cc+0.5 or rr-0.5 <= x(cc+0.5) <= rr+0.5:
                        temp.append((rr,cc))
                        if (rr,cc) in walls:
                            break

                        r_,c_ = rr,cc
                visible.update(temp)
                neigh = [ (r_+i,c_+j) for i,j in ((-1,0),(0,-1)) if (r_+i,c_+j) not in temp]

    visible.update({(r,c)})
    return visible

def raycast0(gridworld,state,n_rays=70,angle=np.pi*4/9,radius=15):

    (r,c), head, _, _ = state
    summary = world_summary(gridworld)

    walls = {wall for wall in summary if summary[wall]['tag']=='w'}
    slopes = [math.tan(np.pi/2 - angle + angle/(n_rays/2)*k)for k in range(1,n_rays)]
    
    visible = set()

    for a in slopes:

        x = lambda yy: ((yy-c)/a + r)*(yy>c) if a else r

        if a < 0:

            y = lambda xx: (a * xx + (c-a*r))

            neigh = [ (r+i,c+j) for i,j in ((0,1),(-1,0)) ]

            r_,c_ = [(rr,cc) for rr,cc in neigh \
                        if cc-0.5 <= y(rr+0.5) <= cc+0.5 or rr-0.5 <= x(cc-0.5) <= rr+0.5][0]
            
            neigh = [ (r_+i,c_+j) for i,j in ((0,1),(-1,0)) ]
            temp = [(r_,c_)]

            for _ in range(radius):
                for rr,cc in neigh:
                    if cc-0.5 <= y(rr+0.5) <= cc+0.5 or rr-0.5 <= x(cc-0.5) <= rr+0.5:
                        temp.append((rr,cc))
                        if (rr,cc) in walls:
                            break

                        r_,c_ = rr,cc
                visible.update(temp)
                neigh = [ (r_+i,c_+j) for i,j in ((0,1),(-1,0)) if (r_+i,c_+j) not in temp]

        elif a >= 0:
            
            y = lambda xx: (a * xx + (c-a*r))

            neigh = [ (r+i,c+j) for i,j in ((1,0),(0,1)) ]
            r_,c_ = [(rr,cc) for rr,cc in neigh \
                        if cc-0.5 <= y(rr-0.5) <= cc+0.5 or rr-0.5 <= x(cc-0.5) <= rr+0.5][0]

            neigh = [ (r_+i,c_+j) for i,j in ((1,0),(0,1)) ]
            temp = [(r_,c_)]

            for _ in range(radius):
                for rr,cc in neigh:
                    if cc-0.5 <= y(rr-0.5) <= cc+0.5 or rr-0.5 <= x(cc-0.5) <= rr+0.5:
                        temp.append((rr,cc))
                        if (rr,cc) in walls:
                            break

                        r_,c_ = rr,cc
                visible.update(temp)
                neigh = [ (r_+i,c_+j) for i,j in ((1,0),(0,1)) if (r_+i,c_+j) not in temp]

    visible.update({(r,c)})
    return visible

def raycast180(gridworld,state,n_rays=70,angle=np.pi*2/9,radius=15):

    (r,c), head, _, _ = state
    summary = world_summary(gridworld)

    walls = {wall for wall in summary if summary[wall]['tag']=='w'}
    slopes = [math.tan(np.pi/2 - angle + angle/(n_rays/2)*k)for k in range(1,n_rays)]
    
    visible = set()

    for a in slopes:

        x = lambda yy: ((yy-c)/a + r)*(yy<c) if a else r

        if a >= 0:

            y = lambda xx: (a * xx + (c-a*r))

            neigh = [ (r+i,c+j) for i,j in ((0,-1),(-1,0)) ]

            r_,c_ = [(rr,cc) for rr,cc in neigh \
                        if cc-0.5 <= y(rr+0.5) <= cc+0.5 or rr-0.5 <= x(cc+0.5) <= rr+0.5][0]
            
            neigh = [ (r_+i,c_+j) for i,j in ((0,-1),(-1,0)) ]
            temp = [(r_,c_)]

            for _ in range(radius):
                for rr,cc in neigh:
                    if cc-0.5 <= y(rr+0.5) <= cc+0.5 or rr-0.5 <= x(cc+0.5) <= rr+0.5:
                        temp.append((rr,cc))
                        if (rr,cc) in walls:
                            break

                        r_,c_ = rr,cc
                visible.update(temp)
                neigh = [ (r_+i,c_+j) for i,j in ((0,-1),(-1,0)) if (r_+i,c_+j) not in temp]

        elif a < 0:
            
            y = lambda xx: (a * xx + (c-a*r))

            neigh = [ (r+i,c+j) for i,j in ((1,0),(0,-1)) ]
            r_,c_ = [(rr,cc) for rr,cc in neigh \
                        if cc-0.5 <= y(rr+0.5) <= cc+0.5 or rr-0.5 <= x(cc-0.5) <= rr+0.5][0]

            neigh = [ (r_+i,c_+j) for i,j in ((1,0),(0,-1)) ]
            temp = [(r_,c_)]

            for _ in range(radius):
                for rr,cc in neigh:
                    if cc-0.5 <= y(rr+0.5) <= cc+0.5 or rr-0.5 <= x(cc-0.5) <= rr+0.5:
                        temp.append((rr,cc))
                        if (rr,cc) in walls:
                            break

                        r_,c_ = rr,cc
                visible.update(temp)
                neigh = [ (r_+i,c_+j) for i,j in ((1,0),(0,-1)) if (r_+i,c_+j) not in temp]

    visible.update({(r,c)})
    return visible


def world_summary(gridworld):
    """ 
    * gridworld is a (list) of (list) of tile tags ('?', '.', 'w', 's', 'd', 'v', 'p') 
    * world and model are both gridworlds
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

if __name__ == '__main__':

    pp = pprint.PrettyPrinter(compact=False,width=90)

    world = [['.', '.', '.', '.', '.', '.', '.', '.', '.'],
             ['.', '.', '.', '.', '.', 'w', '.', '.', '.'],
             ['.', '.', 'w', '.', 's', 'w', '.', '.', '.'],
             ['.', '.', 'w', '.', '.', 'w', '.', '.', '.'],
             ['.', '.', 'w', '.', '.', '.', '.', '.', 'w'],
             ['.', '.', 'w', '.', '.', '.', '.', '.', '.'],
             ['.', '.', '.', '.', '.', '.', 'w', '.', '.'],
             ['.', '.', '.', '.', '.', '.', '.', '.', '.'],
             ['.', '.', '.', '.', '.', '.', '.', '.', '.'],
             ['.', '.', '.', '.', '.', '.', '.', '.', '.'],
             ['.', '.', '.', '.', '.', '.', '.', '.', '.'],]

    org = (2,4)
    obstacles = ((4,2),(4,8),(5,6),(5,7),(6,6),(6,4),(6,3))

    state = ((2,4),270,(),())
    ray = raycast270(world,state)
    print(ray)


