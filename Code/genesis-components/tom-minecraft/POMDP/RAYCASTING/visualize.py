import time
import pprint 
import numpy as np
import turtle 

import env

TS = 50

def adjust_xy(row,col,n_rows,n_cols):

    x = col - (n_cols-1)/2 # adjust x
    y = (n_rows-1)/2 - row # adjust y
    return (x,y)

def draw_gridworld(gridworld,main_loop=True):
    """ draws turtle image of the gridworld """

    screen = turtle.Screen()
    screen.bgcolor('#F7F7F7') # background color
    screen.setup(width = 800, height = 600) # window size

    screen.tracer(0,0) # : draw instantly, don't use screen.update() or tess.write() below
    # NOTE screen.tracer(1,0) : if you want to control the turtle with key
    # screen.tracer(1,0) 

    screen.register_shape('tile', ((-TS/2,-TS/2), (TS/2,-TS/2), (TS/2,TS/2), (-TS/2,TS/2)))
    screen.register_shape('door', ((-TS/3,-TS/3), (TS/3,TS/3), (-TS/3,TS/3), (TS/3,-TS/3)))
    screen.register_shape('player', ((0,10), (-4,-10), (4,-10)))

    tess = turtle.Turtle() # tess is just some arbitrary name
    tess.hideturtle()

    tess.shape('tile')
    tess.penup()

    n_rows, n_cols = len(gridworld), len(gridworld[0])

    for r,row in enumerate(gridworld):
        for c,tag in enumerate(row):

            x,y = adjust_xy(r,c,n_rows,n_cols)
            tess.goto(x*TS,y*TS)

            outline, fill = ('#9AA1A7', '#BEBFBA') if tag == 'w' else \
                            ('#9AA1A7', '#AAD7B0') if tag == 'v' else \
                            ('#9AA1A7', '#FFBDA7') if tag == 's' else \
                            ('#9AA1A7', '#DBD976') if tag == 'd' else \
                            ('#9AA1A7', '#778185') if tag == '?' else \
                            ('#9AA1A7', '#E9E9E9') # outline, fill
            tess.color(outline,fill)

            tess.stamp()

            if tag == 'D':
                tess.shape('door')
                tess.color('#9AA1A7', '#C0AF00')
                tess.stamp()
                tess.shape('tile')

            tess.goto((x-0.25)*TS,(y-0.125)*TS)
            # tess.write((r,c),font=("Arial", 12, "normal")) # with write(), instant draw does not work
            # screen.update() # don't use this to draw instantly

    if main_loop:
        screen.mainloop()

    return tess, screen

def check_observation(gridworld,state,obsv):

    tess, screen = draw_gridworld(gridworld,main_loop=False)
    n_rows, n_cols = len(gridworld), len(gridworld[0])

    (r,c), head, _, _ = state
    x,y = adjust_xy(r,c,n_rows,n_cols)

    tess.goto(x*TS,y*TS)
    tess.setheading(head)
    tess.color('black')
    tess.shape('player')
    tess.stamp()

    tess.shape('tile')
    for r,row in enumerate(gridworld):
        for c,tag in enumerate(row):

            if (r,c) not in obsv:

                x,y = adjust_xy(r,c,n_rows,n_cols)
                tess.goto(x*TS,y*TS)

                outline, fill = ('#9AA1A7', '#778185')
                tess.color(outline,fill)

                tess.stamp()

                tess.goto((x-0.25)*TS,(y-0.125)*TS)
                tess.write((r,c),font=("Arial", 12, "normal")) # with write(), instant draw does not work

    screen.mainloop()

if __name__ == '__main__':

    pp = pprint.PrettyPrinter(compact=False,width=100)

    world = [['.', '.', '.', '.', '.', '.', '.', '.', '.'],
             ['.', '.', '.', '.', '.', '.', '.', '.', '.'],
             ['.', '.', '.', '.', 's', '.', '.', '.', '.'],
             ['.', '.', '.', '.', '.', '.', '.', '.', '.'],
             ['.', '.', 'w', '.', '.', '.', '.', '.', 'w'],
             ['.', '.', '.', '.', '.', '.', 'w', 'w', '.'],
             ['.', '.', '.', '.', 'w', '.', 'w', '.', '.'],
             ['.', '.', '.', '.', '.', '.', '.', '.', '.'],
             ['.', '.', '.', '.', '.', '.', '.', '.', '.'],
             ['.', '.', '.', '.', '.', '.', '.', '.', '.'],
             ['.', '.', '.', '.', '.', '.', '.', '.', '.'],]

    state = ((5,3),0,(),())

    obsv = env.raycast0(world,state)
    check_observation(world,state,obsv)
