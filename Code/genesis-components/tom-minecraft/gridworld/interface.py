import os
from os.path import join, isdir, isfile
from os import listdir, remove, system, mkdir
from PIL import Image
import turtle
from turtle import Turtle
import math
import numpy as np
import time
import imageio
import heapq as hq
import copy

import mdp
import mapreader
import planners
import utils

class VIZ:

    SIZER_TILE_SIZE = {
        3: 60, 6: 60, 12: 30, 13: 30, 24: 16, 46: 12
    }

    def __init__(self, env, USE_INTERFACE=True, SHOW_FINAL_ONLY=False, WIPE_SCREEN=True,
                 MAGNIFY=False, GROUND_TRUTH=False, SHOW_ACTUAL=False,
                 recordings_dir=join('recordings', 'test_interface')):
        self.env = env

        self.USE_INTERFACE = USE_INTERFACE  ## use no interface at all
        self.SHOW_FINAL_ONLY = SHOW_FINAL_ONLY  ## just show the final trajectory
        self.MAGNIFY = MAGNIFY
        self.GROUND_TRUTH = GROUND_TRUTH
        self.SHOW_ACTUAL = SHOW_ACTUAL  ## show the actual map, not what the player can observe
        self.recordings_dir = recordings_dir

        self.init_default()  ## default flags
        self.planner = planners.AStar(env.actions, env.T, env.R, env.get_pos, env.get_dist, goal_state_action='None')
        self.planner.set_VIZ(self)

        if USE_INTERFACE:

            self.init_images()  ## initiate the turtle images to proper size for the map
            if not SHOW_FINAL_ONLY:  ## only make screen in the end
                self.make_screen()
                if not WIPE_SCREEN:
                    self.screen.mainloop()

    def make_screen(self):
        turtle.clearscreen()
        self.screen = self.init_screen()  ## initiate the turtle window
        self.dc = self.draw_maze()  ## draw the maze using turtle "dc"
        self.agent = self.draw_agent()  ## draw the agent using turtle "agent"
        self.marker, self.circler, self.writer, self.macros_marker = self.get_markers()  ## mark macro-actions

        self.screen.update()
        # if self.verbose: print('... finished drawing the maze in ', str(time.time() - start), 'seconds')

    def get_markers(self):

        ## mark the region
        marker = turtle.Turtle()
        marker.hideturtle()
        marker.speed(10)
        marker.pensize(4)

        ## circle the tiles
        circler = turtle.Turtle()
        circler.hideturtle()
        circler.speed(10)
        circler.pensize(4)

        ## write some values
        writer = turtle.Turtle()
        writer.hideturtle()
        writer.speed(10)
        writer.penup()
        writer.pencolor('#000000')  ## '#7f8c8d' grey

        ## circle macro actions
        macros_marker = turtle.Turtle()
        macros_marker.hideturtle()
        macros_marker.speed(10)
        macros_marker.penup()

        return marker, circler, writer, macros_marker

    def init_default(self):

        self.IMG_PLAYER = join("texture", "TILE_SIZE", "playerHEAD.gif")
        self.IMGS = {
            'wall': join("texture", "TILE_SIZE", "wall.gif"),
            'bookshelf': join("texture", "TILE_SIZE", "bookshelf.gif"),
            'entrance': join("texture", "TILE_SIZE", "entrance.gif"),
            'grass': join("texture", "TILE_SIZE", "grass.gif"),
            'fire': join("texture", "TILE_SIZE", "fire.gif"),
            'air': join("texture", "TILE_SIZE", "air.gif"),
            'doorstep': join("texture", "TILE_SIZE", "doorstep.gif"),
            'gravel': join("texture", "TILE_SIZE", "gravel.gif"),
            'door': join("texture", "TILE_SIZE", "door.gif"),
            'victim': join("texture", "TILE_SIZE", "victim-green.gif"),
            'victim-yellow': join("texture", "TILE_SIZE", "victim-yellow.gif"),
            'victim-red': join("texture", "TILE_SIZE", "victim-red.gif"),
            'victim-yellow-06': join("texture", "TILE_SIZE", "victim-yellow-06.gif"),
            'victim-yellow-03': join("texture", "TILE_SIZE", "victim-yellow-03.gif"),
            'victim-06': join("texture", "TILE_SIZE", "victim-green-06.gif"),
            'victim-03': join("texture", "TILE_SIZE", "victim-green-03.gif"),
            'victim-00': join("texture", "TILE_SIZE", "victim-00.gif"),
            'obs-victim-no': join("texture", "TILE_SIZE", "obs-victim-no.gif"),
        }

        self.verbose = False
        self.EXPERIMENT_REPLAY = False

        ## map related
        self.map = self.env.MAP
        self.map_room, self.world_width, self.world_height, self.ts, self.max_iter, self.countdown = mdp.MAP_CONFIG[self.map]

        ## for generating replay screenshot, make image larger
        if self.MAGNIFY:
            self.ts = min(self.ts*2, math.floor(940 / (self.world_height + 6)))
            # self.WINDOW_HEIGHT = min(self.ts * (self.world_height+2) + self.WINDOW_RIGHT_MARGIN*2, 940)
            # self.WINDOW_WIDTH = min(self.ts * (self.world_width+2) + self.WINDOW_RIGHT_MARGIN + self.LEFT_PANEL_WIDTH, 1680)
        utils.resize_gif(self.ts, CHECK=True)

        self.output_name = self.map.replace('.csv', '')

        ## theme related
        self.USE_CHEST = False  ## in Minecraft, the goal is to search and break chests
        self.USE_STATA = False  ## in Stata, the goal is to search and take sanitizers
        self.USE_DARPA = False  ## in DARPA, the victims will die (become red) during the game  ## (REPLAY_IN_RITA or REPLAY_WITH_TOM)

        ## special visualization effects
        self.USE_SAVED_MAP = False  ## for saving initialization time ## TODO: might be unnecessary
        self.USE_HEAT_MAP = False  ## whether to print trace using color depending on number of visits
        self.USE_MAGICK = True  ## to save png with clearer text

        ## optional components for the interface
        self.SHOW_R_TABLE = False  ## takes lots of time for big maps
        self.SHOW_OBS = True  ## show the raycasted area as agent walks around
        self.SHOW_ROOMS = False  ## display the graph of rooms
        self.SHOW_STATIC_IMGS = {
            # ('46by45_2_rooms_colored.gif', 620, 130)
        }  ## for example, snapshot of room segmentation = (image_name in texture/static_imgs folder, x, y)

        self.SHOW_REGION = True  ## in hierarhical planning, draw the current region for plan
        self.SHOW_MACRO_ACTIONS = True  ## in hierarhical planning, draw the current region for plan
        self.SHOW_MAX_TILES = True

        ## window size and offset
        self.WINDOW_RIGHT_MARGIN = 30
        self.LEFT_PANEL_WIDTH = 500

        self.WINDOW_HEIGHT = min(self.ts * (self.world_height + 2) + self.WINDOW_RIGHT_MARGIN * 2, 940)
        self.WINDOW_WIDTH = min(self.ts * (self.world_width + 2) + self.WINDOW_RIGHT_MARGIN + self.LEFT_PANEL_WIDTH, 1680)

        if '48by89' in self.map:
            self.WINDOW_WIDTH = 1680
            self.USE_DARPA = True
        elif '46by45' in self.map:
            # self.WINDOW_WIDTH = 1680
            self.USE_DARPA = True
        elif '36by64' in self.map:
            self.WINDOW_WIDTH = 1350
            self.USE_STATA = True

        if self.USE_DARPA:
            self.USE_CHEST = False
        if self.USE_HEAT_MAP:
            self.VISIT_COUNT = {}

        self.FONT_SIZE = int(self.ts / 5)
        self.last_real_pos = None
        self.color_wheel = initializee_color_wheel(self.max_iter, rainbow=True)

        ## visualization output mode
        self.GENERATE_PNGs = False
        self.GENERATE_GIF = False
        self.GENERATE_PNG = True
        self.drawing_left_most = self.WINDOW_WIDTH / 2
        self.drawing_right_most = 0
        self.drawing_top_most = 0
        self.drawing_bottom_most = self.WINDOW_HEIGHT / 2

    def add_shape(self, img_name):
        if not self.USE_INTERFACE: return
        if not isfile(img_name):
            utils.resize_gif(int(img_name.split('/')[1]), CHECK=False)
        self.screen.register_shape(img_name)

    def init_images(self):
        ## show green and yellow chests instead of yellow and green victims
        # if (EXPERIMENT_REPLAY and not REPLAY_IN_RITA and not REPLAY_WITH_TOM) or USE_CHEST:
        if self.USE_CHEST:
            self.IMGS['victim'] = join("texture", "TILE_SIZE", "chest.gif")
            self.IMGS['victim-yellow'] = join("texture", "TILE_SIZE", "chest-green.gif")
        # elif LEARNING and USE_STATA:
        elif self.USE_STATA:
            self.IMGS['victim'] = join("texture", "TILE_SIZE", "hand-sanitizer.gif")

        self.IMG_PLAYER = self.IMG_PLAYER.replace('TILE_SIZE', str(self.ts))
        for key in self.IMGS.keys():
            self.IMGS[key] = self.IMGS[key].replace('TILE_SIZE', str(self.ts))

    def init_screen(self):
        ts = str(self.ts)
        screen = turtle.Screen()
        # screen.bgcolor('#F7F7F7') # background color
        screen.setup(width=self.WINDOW_WIDTH, height=self.WINDOW_HEIGHT)  # window size
        screen.tracer(0, 0)
        screen.title("RITA's Inverse Planning Component")

        ## registering shape of object tiles
        for type in self.IMGS.keys():
            screen.register_shape(self.IMGS[type])
            if '-0' not in type and '-no' not in type:
                screen.register_shape(self.IMGS[type].replace(ts + '/', ts + '/obs-').replace(ts + '\\', ts + '\\obs-'))
                if 'victim' in type:
                    screen.register_shape(self.IMGS[type].replace(ts + '/', ts + '/open-').replace(ts + '\\', ts + '\\open-'))
        if self.USE_DARPA:
            screen.register_shape(join('texture', ts, 'open-victim.gif'))
            screen.register_shape(join('texture', ts, 'obs-victim-red.gif'))

        ## registering shape of players
        screen.register_shape(join('texture', ts, 'player0.gif'))
        screen.register_shape(join('texture', ts, 'player90.gif'))
        screen.register_shape(join('texture', ts, 'player180.gif'))
        screen.register_shape(join('texture', ts, 'player270.gif'))
        # screen.register_shape(IMGS['air'])

        return screen

    def get_pos(self, tile):
        """ given the tile index, return the position to draw on the maze """

        if isinstance(tile, tuple):
            x, y = tile
        else:
            x, y = self.env.tilesummary[tile]['pos']

        hor = self.WINDOW_WIDTH/2 - self.WINDOW_RIGHT_MARGIN - (self.world_width+1 - x) * self.ts
        ver = (y + (self.world_height-1)/2) * self.ts
        return hor, ver

    def go_to_pos(self, tt, pos, x_shift=0, y_shift=0, ROOM=False):
        # x, y = pos
        # tt.goto(self.MAZE_HOR_OFFSET + x * self.ts, self.MAZE_VER_OFFSET + y * self.ts)
        if ROOM:
            (i,j) = self.env.rooms[pos]['centroid_pos']
            pos = (j,-i) ## -1, +1
        x, y = self.get_pos(pos)
        tt.goto(x-x_shift,y-y_shift)

    def draw_at_with(self, tt, tile=None, type=None, prefix=None, ts=None,
                     img=None, x=None, y=None, state=None, yaw=None):
        """ a fit-all function for drawing an image """

        ## draw object tile
        if tt == self.dc:
            if img == None:  ## when the exact image path is not given
                if type == None:  ## when the exact type is not given
                    if self.GROUND_TRUTH:
                        type = self.env.tilesummary_truth[tile]['type']
                    else:
                        type = self.env.tilesummary[tile]['type']
                img = self.IMGS[type]
                if prefix != None:  ## when a prefix is added to the texture image name, e.g., "obs-" "open-"
                    img = img.replace(f'{str(self.ts)}/', f'{str(self.ts)}/{prefix}-')

        ## draw agent tile
        elif tt == self.agent and state != None:
            if tile == None:
                tile, head = state
            else:
                _, head = state
            tt.clear()  ## there's only one agent on the viz
            if img == None:  ## when the exact image path is not given
                img = self.IMG_PLAYER.replace('HEAD', str(head))  ##.replace('TILE_SIZE', str(self.ts)))

        elif type != None:
            img = self.IMGS[type]

            if ts != None:
                img = img.replace(f'{str(self.ts)}/', f'{str(ts)}/')
                self.add_shape(img)

        ## when the exact location x,y is not given, it must be provided with tile
        if tile!=None and x == None and y == None:
            x, y = self.get_pos(tile)

        tt.shape(img)
        if yaw != None: ## the human player's real yaw
            yaw = round(((-yaw-90) % 360) /10)*10 % 360
            img_name = self.IMG_PLAYER.replace('HEAD', f'-{yaw}')
            self.add_shape(img_name)
            tt.shape(img_name)

        ## draw it
        tt.penup()
        tt.goto(x, y)
        tt.showturtle()
        tt.stamp()

        ## for finding the crop range
        if x < self.drawing_left_most: self.drawing_left_most = x
        if x > self.drawing_right_most: self.drawing_right_most = x
        if y > self.drawing_top_most: self.drawing_top_most = y
        if y < self.drawing_bottom_most: self.drawing_bottom_most = y

    def draw_maze(self):
        """ draws the gridworld """

        # dc is just some arbitrary name
        dc = turtle.Turtle()
        dc.hideturtle()
        dc.speed(10)
        self.dc = dc

        screen = self.screen

        ## find the boundary of the maze
        x_min, x_max, y_min, y_max = 1000, -1000, 1000, -1000
        for tile in self.env.tilesummary_truth.keys():
            i, j = self.env.tilesummary[tile]['pos']
            if i > x_max: x_max = i
            if i < x_min: x_min = i
            if j > y_max: y_max = j
            if j < y_min: y_min = j

        ## if there is a saved map gif, use it
        mypath = join('texture', 'screens')
        file_name = join(mypath, 'screen_' + self.map.replace('/', '_')).replace('.csv','')
        files = [join(mypath, f) for f in listdir(mypath) if
                 self.map.replace('.csv', '.gif') in str(f) and '.gif' in str(f)]

        if self.USE_SAVED_MAP and len(files) == 1:
            screen.register_shape(files[0])
            self.draw_at_with(dc, img=files[0], x=-4, y=4)
            # screen.update()

        ## otherwise, print and save it as gif
        else:

            ## find locations of wall be removing the other blocks
            outer_walls = []
            for y in range(y_min - 1, y_max + 2):
                for x in range(x_min - 1, x_max + 2):
                    outer_walls.append((x, y))

            ## draw all object tiles onto the screen with dc
            for tile in range(len(self.env.tilesummary)):
                outer_walls.remove(self.env.tilesummary[tile]['pos'])
                self.draw_at_with(dc, tile=tile)

            ## what left unpainted are walls
            for loc in outer_walls:
                self.draw_at_with(dc, tile=loc, type='wall')

            ## save the portion as a GIF to be directly stamped the next time
            screen.update()
            screen.getcanvas().postscript(file=file_name+'.eps')

            ## convert it to gif
            im = Image.open(file_name+'.eps')
            fig = im.convert('RGBA')
            fig = fig.resize((self.WINDOW_WIDTH, self.WINDOW_HEIGHT), Image.ANTIALIAS)
            image_gif = file_name+'.gif'
            fig.save(image_gif, save_all=True, append_images=[fig])
            im.close()
            remove(file_name+'.eps')

        ## if RL, initialize heatmap dict
        if self.USE_HEAT_MAP:
            # initializee_color_wheel()
            self.VISIT_COUNT['count'] = 0
            for state in self.env.states:
                temp = {}
                for action in self.env.actions:
                    temp[action] = 0
                self.VISIT_COUNT[state] = temp

        ## show static images if registered in class
        for name, x, y in self.SHOW_STATIC_IMGS:
            img_name = join('texture', 'static_imgs', name)
            screen.addshape(img_name)
            self.draw_at_with(dc, img=img_name, x=x, y=y)

        return dc

    def draw_agent(self):

        agent = turtle.Turtle()
        agent.speed(10)
        agent.hideturtle()
        agent.penup()
        self.agent = agent

        # draw initial player position
        if not self.USE_DARPA:
            self.draw_at_with(agent, state=self.env._pos_agent)
        return agent

    def get_CROP_SIZE(self, USE_MAGICK=False):
        """ crop the map part of the Turtle screen """
        margin = min(self.ts, self.WINDOW_RIGHT_MARGIN)
        left_most = int(self.WINDOW_WIDTH/2 + self.drawing_left_most - margin)
        right_most = int(self.WINDOW_WIDTH/2 + self.drawing_right_most + self.ts + margin - 3)
        top_most = int(self.WINDOW_HEIGHT/2 - self.drawing_top_most - margin)
        bottom_most = int(self.WINDOW_HEIGHT/2 - self.drawing_bottom_most + self.ts + margin - 3)

        if not USE_MAGICK:
            return (left_most, top_most, right_most, bottom_most)
        else:
            ## The width and height give the size of the image that remains after cropping,
            ## x and y are offsets that give the location of the top left corner of the cropped image
            ## -crop 640x620+0+0
            return f'-crop {right_most-left_most}x{bottom_most-top_most}+{left_most}+{top_most}'

    def draw_region(self, tiles, color=None, CLEAR=True):

        if not self.USE_INTERFACE or not self.SHOW_REGION: return
        if isinstance(tiles, int):
            tiles = self.env.rooms[tiles]['tiles']
        if color==None:
            color = self.color_wheel[self.env.step % self.env.max_iter]

        strikes = []  ## list of tuples (x,y) where turtle needs to go
        pos = {}

        left_most = np.inf
        right_most = -np.inf
        top_most = -np.inf
        bottom_most = np.inf

        ## add all positions, find the bounding rectangle
        for tile in tiles:
            x, y = self.env.tilesummary[tile]['pos']
            pos[(x, y)] = tile
            if x < left_most: left_most = x
            if x > right_most: right_most = x
            if y > top_most: top_most = y
            if y < bottom_most: bottom_most = y

        ## time the top and bottom tile of each col
        for x in range(left_most, right_most+1):

            ## bottom
            for y in range(bottom_most, top_most+1):
                if (x,y) in pos:
                    strikes.append( ((x-0.5, y-0.5), (x+0.5, y-0.5)) )
                    break
            ## top
            for y in reversed(list(range(bottom_most, top_most+1))):
                if (x,y) in pos:
                    strikes.append( ((x-0.5, y+0.5), (x+0.5, y+0.5)) )
                    break

        ## time the left and right tile of each row
        for y in range(bottom_most, top_most + 1):

            ## left
            for x in range(left_most, right_most+1):
                if (x,y) in pos:
                    strikes.append(((x - 0.5, y - 0.5), (x - 0.5, y + 0.5)))
                    break
            ## right
            for x in reversed(list(range(left_most, right_most+1))):
                if (x,y) in pos:
                    strikes.append(((x + 0.5, y - 0.5), (x + 0.5, y + 0.5)))
                    break

        self.draw_strikes(strikes, self.marker, color=color, REGION=True, CLEAR=CLEAR)

    def circle_tiles(self, tiles, color=utils.colors.silver, pensize=None, circler=None, CLEAR=True, DEBUG=True):

        strikes = []
        drawn = []
        for tile in tiles:
            if isinstance(tile, tuple): tile = tile[0]  ## in case input is states
            if tile not in drawn:
                drawn.append(tile)
                x, y = self.env.tilesummary[tile]['pos']
                strikes.append(((x - 0.5, y - 0.5), (x - 0.5, y + 0.5)))
                strikes.append(((x - 0.5, y + 0.5), (x + 0.5, y + 0.5)))
                strikes.append(((x + 0.5, y + 0.5), (x + 0.5, y - 0.5)))
                strikes.append(((x + 0.5, y - 0.5), (x - 0.5, y - 0.5)))

        ## circle the tiles
        if circler == None: circler = self.circler
        self.draw_strikes(strikes, circler, color=color, pensize=pensize, CLEAR=CLEAR)

        ## write tile index in each circle
        if DEBUG:
            index = 0
            circler.color(utils.colors.black)
            for tile in drawn:
                index += 1
                circler.penup()
                self.go_to_pos(circler, tile, x_shift=self.ts/2, y_shift=self.ts/3)
                circler.write(f'[{index}]', font=("Courier", 12, 'normal'))

    def draw_strikes(self, strikes, tt, color=utils.colors.silver, pensize=None, REGION=False, CLEAR=True):

        ## draw the strikes
        if CLEAR: tt.clear()
        tt.pencolor(color)
        if REGION: tt.pensize(self.ts//3)
        if pensize!=None: tt.pensize(pensize)

        for strike in strikes:
            tt.penup()
            self.go_to_pos(tt, strike[0])
            tt.pendown()
            self.go_to_pos(tt, strike[1])

        if REGION: tt.pensize(self.ts//6)
        tt.pencolor('#bdc3c7')
        # self.screen.update()

    def take_screenshot(self, PNG = False, FINAL = False, CROP = True,
                        output_name = None, recordings_dir = None):
        """
            take a screenshot of the visualized screen with a name specified by img_name
            save the EPS or PNG into RECORDING_FOLDER
            PNG indicates whether to convert eps to png
            Final indicates whether it's the last image of the plan
        """
        if not self.USE_INTERFACE: return

        ## name of output image
        if output_name == None: output_name = self.output_name
        # if not img_name.endswith('_'): img_name += '_'

        ## directory of output image
        if recordings_dir == None: recordings_dir = self.recordings_dir
        if not isdir(recordings_dir): mkdir(recordings_dir)
        if isdir(join(recordings_dir, 'viz')): recordings_dir = join(recordings_dir, 'viz')

        ## planning and inverse planning
        if self.GENERATE_PNGs or self.GENERATE_GIF:
            recording_name = join(recordings_dir, f'{output_name}_{self.env.step}.eps')
            if self.GENERATE_PNGs: PNG = True

            ## generating demo for slides (tempo is constant, unlike recorded videos)
            if self.GENERATE_GIF:
                if FINAL:
                    self.generate_GIF(recordings_dir, output_name, self.env.step)
                PNG = True
                CROP = True

        ## when taking the last screenshot
        elif self.GENERATE_PNG and FINAL:
            recording_name = join(recordings_dir, f'{output_name}.eps')
            PNG = True

        else:
            return

        if self.verbose: print(recording_name)
        self.screen.getcanvas().postscript(file=recording_name)

        if PNG:
            png_name = recording_name.replace(".eps", ".png")

            if self.USE_MAGICK:
                system(f'convert -flatten {recording_name} -density 300 {png_name}')
                if CROP:
                    crop_size = self.get_CROP_SIZE(USE_MAGICK=True)
                    system(f'convert {png_name} {crop_size} -density 300 {png_name}')
            else:
                im = Image.open(recording_name)
                fig = im.convert('RGBA')
                if CROP: fig = fig.crop(self.get_CROP_SIZE())

                fig.save(png_name, lossless=True)
                im.close()

            remove(recording_name)

    def generate_GIF(self, recordings_dir, output_name, max_step):

        ## Method 1 -- Pillow
        gif_time = time.time()
        frames = []
        frame_first = None
        for index in range(max_step + 1):
            screenshot_name = join(recordings_dir, f'{output_name}_{index}.png')
            im = Image.open(screenshot_name)
            fig = im.convert('RGBA')
            if frame_first == None:
                frame_first = fig
            else:
                frames.append(fig)
            im.close()
            os.remove(screenshot_name)

        image_gif = join(recordings_dir, f'{output_name}.gif')
        frame_first.save(image_gif, save_all=True, append_images=frames)

        if self.verbose: print("... finished generating gif with Pillow in", str(time.time() - gif_time), 'seconds')

        ## Method 2 - imageio (take 2.5 times as long as Pillow)
        # gif_time = time.time()
        # images = []
        # for index in range(max_step + 1):
        #     screenshot_name = join(recordings_dir, f'{output_name}_{index}.png')
        #     images.append(imageio.imread(screenshot_name))
        #     # os.remove(screenshot_name)
        # image_gif = join(recordings_dir, f'{output_name}_2.gif')
        # imageio.mimsave(image_gif, images)
        # print("... finished generating gif in", str(time.time() - gif_time), 'seconds')

    def update_dc(self, s, tiles_to_color=None, tiles_to_change=None, victim_to_change=None, reward=None):

        env = self.env

        ## color the observed tiles on screen to yellow
        if self.SHOW_OBS:

            if tiles_to_color == None:
                tiles_to_color = env.observed_tiles
            for tile in tiles_to_color:
                self.draw_at_with(self.dc, tile=tile, prefix='obs')

        ## change tiles to what they actually are, TODO: may be replicate to tiles_to_color
        if tiles_to_change != None:
            for tile in tiles_to_change:
                self.draw_at_with(self.dc, tile=tile, prefix='obs')

        ## change victim tiles to success color or death color
        if victim_to_change != None:
            for tile in victim_to_change:
                self.draw_at_with(self.dc, tile=tile, prefix='open')

        ## in replay mode, reward is given instead of calculated, but we may not know which object gives it
        if reward != None:
            ## recorded human position can be within 3 block range from the reward
            combinations = [(i, j) for i in range(-1, 2) for j in range(-1, 2)]
            y, x = mapreader.coord(env.tilesummary[s[0]]['pos'])
            for comb in combinations:
                i, j = comb
                if (x + i, y + j) in env.tile_indices.keys():
                    tile = env.tile_indices[(x + i, y + j)]
                    type = env.tilesummary_truth[tile]['type']
                    if (reward == 1 or reward == 2) and (type == 'victim' or type == 'victim-yellow'):
                        self.draw_at_with(self.dc, tile=tile, prefix='open')

    def update_maze(self, s, a=None,
                    reward=None,  ## for replay, update nearby victim status based on scores given
                    real_pos=None, real_yaw=None, ## for replay human accurate position
                    trajectory=None, ## trajectory for visualizing the final trace
                    countdown=None, ## for drawing rainbow tail, indicate time pass
                    tiles_to_color=None, tiles_to_change=None, victim_to_change=None,
                    SCREEN_UPDATE=True):

        if not self.USE_INTERFACE: return
        if self.SHOW_FINAL_ONLY and trajectory==None: return

        env = self.env
        agent, dc, screen, ts = self.agent, self.dc, self.screen, self.ts

        ## the victim might not be directly next to the player, need to find the victim
        if a == 'triage' and real_pos != None:
            tile = self.env.tilesummary[s[0]]
            row, col = tile['row'], tile['col']
            for i in range(row - 2, row + 3):
                for j in range(col - 2, col + 3):
                    if (i,j) in self.env.tile_indices:
                        tile = self.env.tile_indices[(i,j)]
                        if 'victim' in self.env.tilesummary[tile]['type']:
                            victim_to_change.append(tile)

        ## when updating only the last time after all actions are chosen, color all
        # tiles_to_color = env.observed_tiles
        self.update_dc(s, tiles_to_color, tiles_to_change, victim_to_change, reward)
        if SCREEN_UPDATE: self.screen.update()

        ## ------------------------------
        ##  Step 2 --- update the agent, in many different ways
        ## ------------------------------

        ## just show one step
        if trajectory == None:
            if real_pos == None:
                real_pos = env.tilesummary[s[0]]['pos']

            ## draw a colorful trace from the last position to the current position
            if self.last_real_pos != None:
                if self.USE_DARPA and countdown != None:
                    # print('-------------------',math.floor(6000-countdown*10))
                    dc = self.use_color_wheel(dc, math.floor(6000-countdown*10), 6000)
                else:
                    dc = self.use_color_wheel(dc, env.step, env.max_iter)
                dc.penup()
                self.go_to_pos(dc, self.last_real_pos)
                dc.pendown()
                self.go_to_pos(dc, real_pos)
            self.last_real_pos = real_pos

            ## update agent position
            self.draw_at_with(agent, state=s, tile=real_pos, yaw=real_yaw)
            # self.draw_at_with(agent, state=s, x=HOR_OFFSET+x*ts, y=VER_OFFSET+y*ts)

        ## show the whole trajectory in the end
        else:
            length = len(trajectory)
            self.color_wheel = initializee_color_wheel(length, rainbow=True)
            for index in range(len(trajectory)):
                (tile, head), action = trajectory[index]
                pos = env.tilesummary[tile]['pos']
                if index == 0:
                    # dc.goto(HOR_OFFSET + x * ts, VER_OFFSET + y * ts)
                    self.go_to_pos(dc, pos)
                    dc.pendown()
                else:
                    dc = self.use_color_wheel(dc, index, length)
                    dc.pendown()
                    self.go_to_pos(dc, pos)
                    # dc.goto(HOR_OFFSET + x * ts, VER_OFFSET + y * ts)

            self.draw_at_with(agent, state=s, tile=pos)
            # self.draw_at_with(agent, x=HOR_OFFSET + x * ts, y=VER_OFFSET + y * ts)

        if SCREEN_UPDATE: self.screen.update()

    def draw_obs_paths(self, obs_paths):
        planner = self.planner
        writer = turtle.Turtle()
        writer.hideturtle()
        writer.up()
        colors = []
        for doorstep, data in obs_paths.items():
            if len(colors) == 0:
                colors = copy.deepcopy(utils.colors.rainbow)
            color = colors.pop()
            size = 4 + len(colors)
            planner._path = data['path']
            planner.draw_rollout_path(tt=writer, color=color, width=size)

    def draw_macro_children(self, macro, children={}, history=[], verbose=False, CLEAR=False,
                            path_color=utils.colors.blue, macro_color=utils.colors.dark_blue):

        bad_color = utils.colors.silver
        start_color = utils.colors.red

        env = self.env
        planner = self.planner
        macros_marker = self.macros_marker
        if CLEAR: macros_marker.clear()

        ## mark the color of less valuable children
        bad_children = []
        if len(children) == 0:
            if isinstance(macro, int):
                print(macro)
            children = env.get_macro_children(macro, history)
            # bad_children = env.get_bad_children(children, macro)
        # print(len(children), children)
        # print(len(bad_children), bad_children)
        if verbose: print(macro)

        ## reduce the color for longer horizon
        size = len(children)
        path_colors = [path_color] * size
        if path_color != utils.colors.blue: ## not default
            path_colors = generate_color_wheel((path_color, utils.colors.white), int(size*1.5))
        index = 0
        children_tiles = []
        for child in children:
            if verbose: print('   ', child, children[child])
            planner._path = children[child]
            if child in bad_children:
                planner.draw_rollout_path(tt=macros_marker, color=bad_color)
            else:
                planner.draw_rollout_path(tt=macros_marker, color=path_colors[index])
            index += 1

            # children_tiles.append(child[0])
            children_tiles.append(child[2])

        ## highlight me in red and chilren in green
        self.circle_tiles(tiles=[c for c in children_tiles if c in bad_children], circler=macros_marker, color=bad_color, CLEAR=False)
        self.circle_tiles(tiles=[c for c in children_tiles if c not in bad_children], circler=macros_marker, color=macro_color, CLEAR=False)
        self.circle_tiles(tiles=[macro], circler=macros_marker, color=start_color, CLEAR=False)
        self.screen.update()
        # self.take_screenshot(PNG=True, FINAL=True, output_name=self.output_name + f'_{len(history)}')

        return children

    def use_color_wheel(self, dc, step, length, max_w=None, min_w=None):

        color = self.color_wheel[step % length]
        # print(step, length, color)
        dc.pencolor(color)
        dc.fillcolor(color)
        if max_w==None: max_w = max(self.ts//8, 4)
        if self.ts > 18: max_w = max(self.ts//4, 8)
        if min_w==None: min_w = max(max_w*0.5, 3)
        dc.pensize(max_w - (max_w-min_w) * step/length)  ## 2
        dc.penup()
        return dc

        # elif (LEARNING and learning.SHOW_Q_TABLE) or (
        #         PLANNING and tabular.SHOW_Q_TABLE and trajectory != None and False):
        #
        #     Q_table = trace  ## trace here is Q-table
        #
        #     amp = learning.MAX_STEP
        #     color_wheel = initializee_color_wheel(amp)  ## max Q value is 1 for 1 reward
        #
        #     for ss, action_value in Q_table.items():
        #         tile, head = ss
        #
        #         if env.tilesummary[tile]['type'] != 'wall':
        #             x, z = env.tilesummary[tile]['pos']
        #             x = MAZE_HOR_OFFSET + x * ts
        #             z = MAZE_VER_OFFSET + z * ts
        #
        #             for a, value in action_value.items():
        #
        #                 value *= amp
        #
        #                 cir = Turtle()
        #                 cir.hideturtle()
        #                 cir.penup()
        #                 if a == 'go_straight':
        #                     cir.shape("square")
        #                 else:
        #                     cir.shape("triangle")
        #
        #                 if '3by3_' in env.MAP or '6by6_' in env.MAP:
        #                     cir.shapesize(0.25, 0.25, 0)
        #                     b = 20  # arrow from center
        #                     d = 8  # tilted arror from axis
        #                 elif '13by' in env.MAP or '12by' in env.MAP:
        #                     cir.shapesize(0.12, 0.12, 0)
        #                     b = 10  # arrow from center
        #                     d = 4  # tilted arror from axis
        #                 else:  # if 'test' in env.MAP:
        #                     cir.shapesize(0.05, 0.05, 0)
        #                     b = 5  # arrow from center
        #                     d = 2  # tilted arror from axis
        #
        #                 # if value != 0: print(value,len(color_wheel))
        #                 if value < 0:
        #                     value += len(color_wheel)
        #                     count = round(float(max(min(value, len(color_wheel) - 1), 0)))
        #                 else:
        #                     value /= 2
        #                     count = round(float(max(min(value, len(color_wheel) / 2 - 1), 0)))
        #                 # if value != 0:
        #                 #     print(count,len(color_wheel))
        #                 #     print()
        #                 cir.pencolor(color_wheel[count])
        #                 cir.fillcolor(color_wheel[count])
        #
        #                 if head == 0:
        #                     if a == 'turn_right':
        #                         cir.left(-90)  # (-30)
        #                         cir.goto(x + b, z - d)
        #                     elif a == 'go_straight':
        #                         cir.left(0)
        #                         cir.goto(x + b, z)
        #                     elif a == 'turn_left':
        #                         cir.left(90)  # (30)
        #                         cir.goto(x + b, z + d)
        #
        #                 elif head == 90:
        #                     if a == 'turn_right':
        #                         cir.left(0)  # (60)
        #                         cir.goto(x + d, z + b)
        #                     elif a == 'go_straight':
        #                         cir.left(90)
        #                         cir.goto(x, z + b)
        #                     elif a == 'turn_left':
        #                         cir.left(180)  # (120)
        #                         cir.goto(x - d, z + b)
        #
        #                 elif head == 180:
        #                     if a == 'turn_right':
        #                         cir.left(90)  # (150)
        #                         cir.goto(x - b, z + d)
        #                     elif a == 'go_straight':
        #                         cir.left(180)
        #                         cir.goto(x - b, z)
        #                     elif a == 'turn_left':
        #                         cir.left(270)  # (210)
        #                         cir.goto(x - b, z - d)
        #
        #                 elif head == 270:
        #                     if a == 'turn_right':
        #                         cir.left(180)  # (240)
        #                         cir.goto(x - d, z - b)
        #                     elif a == 'go_straight':
        #                         cir.left(270)
        #                         cir.goto(x, z - b)
        #                     elif a == 'turn_left':
        #                         cir.left(0)  # (300)
        #                         cir.goto(x + d, z - b)
        #
        #                 cir.pendown()
        #                 cir.showturtle()
        #                 cir.stamp()
        #                 cir.penup()
        #
        #     ## print my current avatar
        #     if LEARNING:
        #         s = s[0]
        #         x, y = env.tilesummary[s]['pos']
        #         agent.clear()
        #         agent.penup()
        #         agent.goto(MAZE_HOR_OFFSET + x * ts, MAZE_VER_OFFSET + y * ts)
        #         agent.showturtle()
        #         agent.stamp()
        #
        # ## for rl simulations, show past trajectory as colored map - old state space
        # elif LEARNING:
        #
        #     # if learning.SHOW_Q_TABLE:
        #     #     VISIT_COUNT = trace
        #     # else:
        #     VISIT_COUNT[s][a] += 1
        #     VISIT_COUNT['count'] += 1
        #
        #     s_last, a_last = sa_last
        #
        #     ## change tile to air so we know it has been visited
        #     x_last, y_last = env.tilesummary[s_last[0]]['pos']
        #     x_last = MAZE_HOR_OFFSET + x_last * ts
        #     y_last = MAZE_VER_OFFSET + y_last * ts
        #     dc.shape(IMGS['air'])
        #     dc.goto(x_last, y_last)
        #     dc.showturtle()
        #     dc.stamp()
        #
        #     ## update heat map of the grid
        #
        #     cir = Turtle()
        #     cir.hideturtle()
        #     cir.penup()
        #     if a_last == 'go_straight':
        #         cir.shape("square")
        #     else:
        #         cir.shape("triangle")
        #
        #     if '3by3_' in env.MAP or '6by6_' in env.MAP:
        #         cir.shapesize(0.25, 0.25, 0)
        #         b = 20  # arrow from center
        #         d = 8  # tilted arror from axis
        #     elif '13by' in env.MAP or '12by' in env.MAP:
        #         cir.shapesize(0.12, 0.12, 0)
        #         b = 10  # arrow from center
        #         d = 4  # tilted arror from axis
        #     else:  # if 'test' in env.MAP:
        #         cir.shapesize(0.05, 0.05, 0)
        #         b = 5  # arrow from center
        #         d = 2  # tilted arror from axis
        #
        #     cir.pencolor(get_color(sa_last))
        #     cir.fillcolor(get_color(sa_last))
        #
        #     if USE_STATA:
        #         cir.shape("circle")
        #         cir.shapesize(0.2, 0.2, 0)
        #         cir.goto(x_last, y_last)
        #     else:
        #         if s_last[1] == 0:
        #             if a_last == 'turn_right':
        #                 cir.left(-90)  # (-30)
        #                 cir.goto(x_last + b, y_last - d)
        #             elif a_last == 'go_straight':
        #                 cir.left(0)
        #                 cir.goto(x_last + b, y_last)
        #             elif a_last == 'turn_left':
        #                 cir.left(90)  # (30)
        #                 cir.goto(x_last + b, y_last + d)
        #
        #         elif s_last[1] == 90:
        #             if a_last == 'turn_right':
        #                 cir.left(0)  # (60)
        #                 cir.goto(x_last + d, y_last + b)
        #             elif a_last == 'go_straight':
        #                 cir.left(90)
        #                 cir.goto(x_last, y_last + b)
        #             elif a_last == 'turn_left':
        #                 cir.left(180)  # (120)
        #                 cir.goto(x_last - d, y_last + b)
        #
        #         elif s_last[1] == 180:
        #             if a_last == 'turn_right':
        #                 cir.left(90)  # (150)
        #                 cir.goto(x_last - b, y_last + d)
        #             elif a_last == 'go_straight':
        #                 cir.left(180)
        #                 cir.goto(x_last - b, y_last)
        #             elif a_last == 'turn_left':
        #                 cir.left(270)  # (210)
        #                 cir.goto(x_last - b, y_last - d)
        #
        #         elif s_last[1] == 270:
        #             if a_last == 'turn_right':
        #                 cir.left(180)  # (240)
        #                 cir.goto(x_last - d, y_last - b)
        #             elif a_last == 'go_straight':
        #                 cir.left(270)
        #                 cir.goto(x_last, y_last - b)
        #             elif a_last == 'turn_left':
        #                 cir.left(0)  # (300)
        #                 cir.goto(x_last + d, y_last - b)
        #     cir.pendown()
        #     cir.showturtle()
        #     cir.stamp()
        #     cir.penup()
        #
        #     ## print my current avatar
        #     x, y = env.tilesummary[s[0]]['pos']
        #     agent.clear()
        #     agent.penup()
        #     agent.goto(MAZE_HOR_OFFSET + x * ts, MAZE_VER_OFFSET + y * ts)
        #     agent.showturtle()
        #     agent.stamp()
        #
        # ## for inverse planning, show past trajectory as all avatars
        # else:
        #     x, y = env.tilesummary[s[0]]['pos']
        #     agent.goto(MAZE_HOR_OFFSET + x * ts, MAZE_VER_OFFSET + y * ts)
        #     agent.showturtle()
        #     agent.stamp()
        #     screen.update()
        #
        #     ##  ------------- animation
        #     type = env.tilesummary[s[0]]['type']
        #     if type == 'gravel' or type == 'fire':
        #         time.sleep(0.5)
        #         dc.shape(IMGS[type].replace('TILE_SIZE/', str(TILE_SIZE) + '/obs-').replace('TILE_SIZE\\',
        #                                                                                     str(TILE_SIZE) + '\\obs-'))
        #         dc.penup()
        #         dc.goto(MAZE_HOR_OFFSET + x * ts, MAZE_VER_OFFSET + y * ts)
        #         dc.showturtle()
        #         dc.stamp()
        #         screen.update()
        #
        #         time.sleep(0.5)
        #         agent.stamp()
        #         screen.update()
        #
        #     if type == 'gravel':
        #         time.sleep(0.5)
        #         dc.stamp()
        #         screen.update()
        #
        #         time.sleep(0.5)
        #         agent.stamp()
        #         screen.update()
        #
        # if PRINT_CONSOLE: print('finished printing raycasting in', str(time.time() - start), 'seconds')
        #
        # ## for RL, show the entire trajectory along with Q-table or visit count
        # if (LEARNING and learning.JUST_TRAJECTORY) or (PLANNING and tabular.JUST_TRAJECTORY and trajectory != None):
        #
        #     tile_last = None
        #
        #     dc = Turtle()
        #     dc.hideturtle()
        #     dc.speed(10)
        #     dc.pensize(2)
        #     dc.penup()
        #
        #     length = len(trajectory)
        #     for index in range(len(trajectory)):
        #         (tile, head), action = trajectory[index]
        #         x, y = env.tilesummary[tile]['pos']
        #         dc.goto(MAZE_HOR_OFFSET + x * ts, MAZE_VER_OFFSET + y * ts)
        #         dc.pendown()
        #         if index > 0:
        #             # dc.shape(IMGS['air'].replace('TILE_SIZE/',str(TILE_SIZE)+'/obs-').replace('TILE_SIZE\\',str(TILE_SIZE)+'\\obs-'))
        #             # dc.hideturtle()
        #
        #             color = initializee_color_wheel(length, rainbow=True)[index]
        #             dc.pencolor(color)
        #             dc.fillcolor(color)
        #             dc.pendown()
        #             dc.goto(MAZE_HOR_OFFSET + x * ts, MAZE_VER_OFFSET + y * ts)
        #
        #     if PLANNING:
        #         x, y = env.tilesummary[s[0]]['pos']
        #         agent.clear()
        #         agent.penup()
        #         agent.goto(MAZE_HOR_OFFSET + x * ts, MAZE_VER_OFFSET + y * ts)
        #         agent.showturtle()
        #         agent.stamp()

def generate_color_wheel(original_color, size):

    def hex2int(hex1):
        return int('0x'+str(hex1),0)

    def int2hex(int1):
        hex1 = str(hex(int1)).replace('0x','')
        if len(hex1) == 1:
            hex1 = '0'+hex1
        return hex1

    def hex2ints(original_color):
        R_hex = original_color[0:2]
        G_hex = original_color[2:4]
        B_hex = original_color[4:6]
        R_int = hex2int(R_hex)
        G_int = hex2int(G_hex)
        B_int = hex2int(B_hex)
        return R_int, G_int, B_int

    def ints2hex(R_int, G_int, B_int):
        return '#'+int2hex(R_int)+int2hex(G_int)+int2hex(B_int)

    def portion(total, size, index):
        return total + round((225-total) / size * index)

    def gradients(start, end, size, index):
        return start + round((end-start) / size * index)

    color_wheel = []

    ## for experience replay, find all the colors between two colors
    if len(original_color) == 2:

        color1, color2 = original_color
        R1_int, G1_int, B1_int = hex2ints(color1.replace('#',''))
        R2_int, G2_int, B2_int = hex2ints(color2.replace('#',''))
        for index in range(size):
            color_wheel.append(ints2hex(
                gradients(R1_int, R2_int, size, index),
                gradients(G1_int, G2_int, size, index),
                gradients(B1_int, B2_int, size, index)
            ))

    ## for RL, the color of different shades symbolizes frequency
    else:

        R_int, G_int, B_int = hex2ints(original_color.replace('#',''))

        seq = list(range(size))
        seq.reverse()
        for index in seq:
            color_wheel.append(ints2hex(
                portion(R_int, size, index),
                portion(G_int, size, index),
                portion(B_int, size, index)
            ))

    return color_wheel

def initializee_color_wheel(color_density=None, rainbow=False, rollout=None):
    """ return a list of colors in the flat UI color style """

    COLOR_WHEEL = []

    # if EXPERIMENT_REPLAY or INVERSE_PLANNING or PLANNING or rainbow: # or learning.USE_HUMAN_TRAJECTORY
    if rainbow:  ## victims die when when changing from blue to green
        colors = ['#F44336','#E91E63','#9C27B0','#673AB7', '#3F51B5', '#2196F3', '#03A9F4', '#00BCD4',
                  '#009688', '#4CAF50', '#8BC34A',
                  '#CDDC39', '#FFEB3B', '#FFC107', '#FF9800', '#FF5722'
                  ]
        COLOR_DENSITY = math.ceil(color_density/(len(colors)-1))
        for i in range(1,len(colors)):
            COLOR_WHEEL += generate_color_wheel((colors[i-1],colors[i]), COLOR_DENSITY)

    ## get the same color but gradually lighter
    elif rollout != None:
        COLOR_WHEEL += generate_color_wheel((rollout, '#FFFFFF'), color_density*2)

    else:

        # ## a small range of colors of the flat UI color style
        # if learning.USE_HUMAN_TRAJECTORY:
        #     color_density *= 2
        #
        # ## Q-table type
        # if LEARNING and learning.SHOW_Q_TABLE:
        #     colors = [GREEN,RED]
        #     if color_density==None:
        #         color_density = math.ceil(learning.MAX_STEP)
        #
        # elif PLANNING and tabular.SHOW_Q_TABLE:
        #     colors = [RED,GREEN]
        #     if color_density==None:
        #         color_density = math.ceil(learning.MAX_STEP)
        #
        # ## M&M type
        # else:
        BLUE = '#3498db'
        GREEN = '#2ecc71'
        YELLOW = '#f1c40f'
        RED = '#e74c3c'
        colors = [BLUE, GREEN, YELLOW, RED]
        if color_density==None:
            color_density = 30
        color_density = math.ceil(color_density / len(colors))

        for original_color in colors:
            little_wheel = generate_color_wheel(original_color, color_density)
            # if learning.SHOW_Q_TABLE and original_color == RED: little_wheel.reverse()
            COLOR_WHEEL += little_wheel

    return COLOR_WHEEL

def draw_heatmap(summary_dict, get_pos, ts=20):
    max_count = max(summary_dict.values())
    dot_size = ts/50

    cir = Turtle()
    cir.hideturtle()
    cir.penup()
    cir.shape("circle")
    cir.shapesize(dot_size, dot_size, 0)

    color_wheel = initializee_color_wheel(max_count)
    for tile, count in summary_dict.items():
        x, y = get_pos(tile)
        color = color_wheel[min(len(color_wheel) - 1, count)]
        cir.pencolor(color)
        cir.fillcolor(color)
        cir.goto(x, y)
        cir.pendown()
        cir.showturtle()
        cir.stamp()
        cir.penup()
    cir.hideturtle()
    return cir

def test_interface():
    for map in mdp.MAP_CONFIG.keys():
        viz = VIZ(mdp.POMDP(map))
        viz.take_screenshot(PNG=True, FINAL=True)

def test_tasks():
    for map in ['test2.csv','test3.csv','test4.csv','test5.csv']:
        env = mdp.POMDP(map)
        viz = VIZ(env, recordings_dir=join('recordings', 'test_tasks'), SHOW_ACTUAL=True)

        new_obs_tiles = list(env.tilesummary_truth.keys())
        unobserved_in_rooms, obs_rewards, tiles_to_color, tiles_to_change = env.observe(new_obs_tiles, env._pos_agent[0])
        viz.update_maze(env._pos_agent, tiles_to_color=tiles_to_color, tiles_to_change=tiles_to_change)
        viz.take_screenshot(PNG=True, FINAL=True, CROP=True)

def test_abstraction():
    env = mdp.POMDP('48by89_easy.csv') ## , ORACLE_MODE = True
    viz = VIZ(env, MAGNIFY=True, recordings_dir=join('recordings', 'test_abstraction'))

    ## step 1: draw room observation paths
    env.viz = viz
    # obs_paths = env.obs_paths  ## env.initiate_obs_paths(rooms=[3, 6]) ## , 14, 41, 42
    # viz.draw_obs_paths(obs_paths)

    # ## step 2: draw tiles of interest
    # macros = [env._pos_agent[0]]
    # macros.extend(env.get_macro_tiles())
    # viz.circle_tiles(tiles=macros)
    # viz.screen.update()
    # # viz.take_screenshot(PNG=True, FINAL=True, output_name=viz.output_name+'_macros')

    ## step 3: draw the children of each node and the shortest path to get there
    history = []
    queue = []
    hq.heappush(queue, env._pos_agent)
    while len(queue) > 0:
        macro = hq.heappop(queue)
        history.append(macro)
        children = viz.draw_macro_children(macro, history)

        for child in children:
            if child not in history and child not in list(queue):
                hq.heappush(queue, child)

    print('finish')
    viz.take_screenshot(PNG=True, FINAL=True, output_name=viz.output_name + '_option_network')
    viz.screen.mainloop()

def test_planner():
    env = mdp.POMDP('48by89_easy.csv', ORACLE_MODE = True)
    planner = planners.AStar(env.actions, env.T, env.R, env.get_pos, env.get_dist,
                            gamma=env.player['tilelevel_gamma'], goal_state_action=((1000, 0), 'go_straight'))
    planner.run((13,0))
    print(len(planner._path), planner.goal_state_action, planner._path)

if __name__ == '__main__':
    # test_interface()
    # test_tasks()
    test_abstraction()
    # test_planner()

    # env = mdp.POMDP('6by6_3_Z.csv')
    # viz = VIZ(env, WIPE_SCREEN=False)
