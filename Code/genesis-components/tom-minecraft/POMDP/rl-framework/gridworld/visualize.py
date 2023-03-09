import numpy as np
import pdb
import time
import pprint
import random
import turtle
import mdp
import valueiteration as valit

def draw_maze(env,main_loop=False):
    """ draws turtle image of the gridworld """

    screen = turtle.Screen()
    screen.bgcolor('#F7F7F7') # background color
    screen.setup(width = 1000, height = 500) # window size

    ts = 60 # tile size
    screen.register_shape('tile', ((-ts/2,-ts/2), (ts/2,-ts/2), (ts/2,ts/2), (-ts/2,ts/2)))
    screen.register_shape('player', ((0,10), (-4,-10), (4,-10)))

    dc = turtle.Turtle() # dc is just some arbitrary name
    dc.hideturtle()

    n_tiles = len(env.tilesummary)
    for tile in range(n_tiles):

        dc.shape('tile')
        dc.penup()

        x,y = env.tilesummary[tile]['pos']
        dc.goto(x*ts,y*ts)

        if env.tilesummary[tile]['reward'] == -1:
            dc.color('#9AA1A7', '#F2AEB0')
        elif env.tilesummary[tile]['reward'] == 1:
            dc.color('#9AA1A7', '#AAD7B0')
        else:
            dc.color('#9AA1A7', '#E9E9E9')

        dc.stamp()
        screen.update()

    if main_loop:
        screen.mainloop()

    return dc, screen, ts

def play_episode(env,s0,main_loop=True):
    """ plays agent navigating in the gridworld """

    dc, screen, ts = draw_maze(env)
    screen.tracer(1,0)

    episode = valit.episode(env,s0)

    agent = turtle.Turtle()
    agent.shape('player')
    agent.hideturtle()
    agent.penup()

    for s,_ in episode:
        x,y = env.tilesummary[s[0]]['pos']
        agent.goto(x*ts,y*ts)
        agent.setheading(s[1])
        agent.showturtle()
        agent.pendown()
        time.sleep(0.3)
        
    if main_loop:
        screen.mainloop()


if __name__ == '__main__':
    env = mdp.MDP()

    # To see what the gridworld looks like, uncomment below
    # -------------------------------------
    # draw_maze(env,main_loop=True)
    # -------------------------------------

    # To watch the agent navigate to the goal (green) while avoiding cliffs (red)
    # ... uncomment below
    # Note that s0 is the initial state where the agent starts from
    # -------------------------------------
    # s0 = (4,90)
    # play_episode(env,s0,main_loop=True)
    # -------------------------------------
