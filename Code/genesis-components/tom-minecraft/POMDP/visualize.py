import numpy as np
import pdb
import time
import pprint
import random
import turtle

import mdp
import pomdp
import valueiteration as valit

MAP = '6by6.csv'
TS = 60

def line_of_sight():
    pass

# helper (play_episode)
def draw_maze(tilesummary,tile_number=False,main_loop=False):
    """ visualizes environment only, and returns turtle variables """

    # setup screen
    screen = turtle.Screen()
    screen.bgcolor('#F7F7F7') # background color
    screen.setup(width=700,height=700) # window size
    screen.register_shape('tile',((-TS/2,-TS/2), (TS/2,-TS/2), (TS/2,TS/2), (-TS/2,TS/2)))
    screen.register_shape('player', ((0,10), (-4,-10), (4,-10)))

    # setupt turtle
    tess = turtle.Turtle()
    tess._tracer(0) # draw instantly
    tess.penup()
    tess.hideturtle()

    # draw grid-world environment: tiles
    tess.shape('tile')

    for tile in tilesummary:

        # tile
        (r,c),tag = tilesummary[tile]['pos'], tilesummary[tile]['tag']
        tile_color =    ('#D4D3CF', '#A9ABA8') if tag=='W' else  \
                        ('#D4D3CF', '#FFC5C3') if tag=='S' else  \
                        ('#D4D3CF', '#AAD7B0') if tag=='G' else  \
                        ('#D4D3CF', '#E9E9E9') # outline,fill

        tess.color(*tile_color) # outline, fill
        tess.goto(c*TS,r*TS)
        tess.stamp()

        # tile number
        tess.color('gray')
        tess.goto(c*TS,r*TS-TS/4)
        tess.write(str(tile), move=True, align="center",font=("Arial", 20, "normal"))

    if main_loop:
        screen.mainloop()

    return tess, screen

def play_episode(env,s0,tile_number=False,main_loop=True,pomdp=True):
    """ plays agent optimally navigating in the gridworld """

    dc, screen = draw_maze(env.tilesummary,tile_number=False)

    if pomdp: # pomdp case
        dc.penup()
        dc.color('#D4D3CF', '#AAD7B0') # goal tile color

        for tile in s0[-1]: # stamp goal tiles
            r,c = env.tilesummary[tile]['pos']
            dc.goto(c*TS,r*TS)
            dc.stamp()
            dc.goto(c*TS,r*TS-TS/4)
            dc.write(str(tile), move=True, align="center",font=("Arial", 20, "normal"))
        episode = valit.episode_b(env,s0)
    else:
        episode = valit.episode(env,s0)

    screen.tracer(1,0)

    agent = turtle.Turtle()
    agent.shape('player')
    agent.pensize(2)
    agent.hideturtle()
    agent.penup()

    for s,_ in episode:
        x,y = env.tilesummary[s[0]]['pos']
        agent.goto(y*TS,x*TS) # don't forget that it's x,y = col,row!
        agent.setheading(s[1])
        agent.showturtle()
        agent.pendown()
        time.sleep(0.3)
        
    if main_loop:
        screen.mainloop()


if __name__ == '__main__':
    import pprint
    pp = pprint.PrettyPrinter(compact=False,width=100)

    # To see what the gridworld looks like, uncomment below
    # -------------------------------------
    # tilesummary = mdp.MDP(MAP).tilesummary
    # draw_maze(tilesummary,main_loop=True)
    # -------------------------------------

    # To watch the agent navigate to the goal (green) while avoiding cliffs (red)
    # ... uncomment below
    # Note that s0 is the initial state where the agent starts from
    # -------------------------------------
    # MAP = '6by6_1.csv'
    # env = mdp.MDP_multiple_goals(MAP)
    # s0 = (30,90,())
    # play_episode(env,s0,main_loop=True,pomdp=False)
    # -------------------------------------
    # -------------------------------------
    MAP = '6by6.csv'
    env = pomdp.POMDP(MAP)
    s0 = (30,90,(0,))
    play_episode(env,s0,main_loop=True)
    # -------------------------------------
