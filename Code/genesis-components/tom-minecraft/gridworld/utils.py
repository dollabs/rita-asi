from datetime import datetime
import numpy as np
from os import listdir, getcwd, mkdir
from os.path import join, isfile, isdir, abspath
from pdf2image import convert_from_path
import time
import csv
import math
import copy
from shutil import copyfile
import json

# ## folorful console msg, DEBUG is green, ERROR is red
# import coloredlogs, logging
# logging.basicConfig(
#     filename='recordings/_logging.log',
#     level=logging.DEBUG,
#     format='%(asctime)s.%(msecs)03d %(levelname)s %(module)s - %(funcName)s: %(message)s',
#     datefmt='%Y-%m-%d %H:%M:%S',
# )
# coloredlogs.install(level='DEBUG')
# z = logging.getLogger(__name__)

from texture.GIF_resizer import resize

class colors():
    black = '#000000'
    white = '#ffffff'
    red = '#e74c3c'
    orange = carrot = '#e67e22'
    orange_trans = '#edcaab'
    dark_organge = pumpkin = '#d35400'
    yellow = sun_flower = '#f1c40f'
    dark_yellow = '#ebbd07'
    light_green = '#80d9c7'  ## #b8e994
    green = '#1abc9c'
    dark_green = '#16a085'
    blue = '#3498db'
    dark_blue = '#2980b9'
    midnight = '#2c3e50'
    purple = '#9b59b6'
    purple_trans = '#debeeb'
    dark_purple = '#8e44ad'
    clouds = '#ecf0f1'
    silver = '#bdc3c7'
    concrete = '#95a5a6'
    rainbow = [red, orange, yellow, green, blue, purple, midnight]

class NpEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, np.integer):
            return int(obj)
        elif isinstance(obj, np.floating):
            return float(obj)
        elif isinstance(obj, np.ndarray):
            return obj.tolist()
        else:
            return super(NpEncoder, self).default(obj)

class Timer():

    def __init__(self, log=None, verbose=True):
        self.my_time = time.time()
        self.log = log
        self.round = {}
        self.titles = []
        self.written_titles = False
        self.verbose = verbose
        self.recordings_folder = None

    def add(self, ori_text):
        time_diff = round(time.time()-self.my_time, 3)

        text = f'.... finished {ori_text} in {time_diff} seconds'
        # print(text)
        ## the columns to be printed
        if self.recordings_folder != None and text not in self.titles:
            if ori_text not in self.titles:
                self.titles.append(ori_text)
            self.round[ori_text] = time_diff

        if time_diff > 0.001:
            if self.log != None:
                self.log(text)
            elif self.verbose:
                print(text)

        self.my_time = time.time()

    def start_record(self, recordings_folder, output_name=None, CSV_DIR=True):
        self.recordings_folder = recordings_folder

        if output_name == None:
            output_name = get_time(DATE=True)
        self.output_name = output_name

        ## crete a csv dir within the recordings folder
        if CSV_DIR:
            csv_dir = join(self.recordings_folder, 'csv')
            if not isdir(csv_dir):
                mkdir(csv_dir)
            self.recordings_folder = csv_dir

    def record(self, countdown):
        if self.recordings_folder != None:
            with open(join(self.recordings_folder, f'times-{self.output_name}.csv'), mode='a') as times_file:
                times_writer = csv.writer(times_file, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
                if not self.written_titles:
                    self.written_titles = True
                    titles = ['countdown']
                    titles.extend(self.titles)
                    times_writer.writerow(titles)

                round = [countdown]
                for item in self.titles:
                    if item in self.round:
                        round.append(self.round[item])
                    else:
                        round.append('')
                times_writer.writerow(round)
                self.round = {}

def get_class_name(obj):
    return str(type(obj)).split('.')[1].split("'")[0]

def normalize(my_list):
    """ normalize a list of numbers to within 0, 1 """
    my_min = min(my_list)
    # print('original', my_list)

    ## method 1 works for hvi
    new_list = []
    my_sum = sum(my_list) - len(my_list)*my_min
    if my_sum == 0: return [0] * len(my_list)
    for num in my_list:
        new_list.append((num - my_min) /my_sum)
    # print('new list', new_list)
    return new_list

    # ## method 2 works for lrtdp
    # if my_min < 0:
    #     epsilon = 0.01
    #     my_list = [l-my_min+epsilon for l in my_list]
    #
    # my_sum = sum(my_list)
    # my_list = [l/my_sum for l in my_list]
    # print('final list', my_list)
    #
    # return my_list

def deepcopy(my_dict):
    if isinstance(my_dict, dict):
        return {k : v.copy() for k, v in my_dict.items()}
    elif isinstance(my_dict, list):
        return [v for v in my_dict]

def convert_pdf(dir):
    files = [f for f in listdir(dir) if isfile(join(dir, f)) and ('.pdf' in f)]
    for file in files:
        print(file)
        in_file = join(dir, file)
        out_file = join(dir, 'submit', file.replace('.pdf', '.png'))
        page = convert_from_path(in_file, 500)[0]
        page.save(out_file, 'PNG')

def get_time(SECONDS = False, DATE = False):
    if SECONDS: return datetime.now().strftime("%Y-%m-%dT%H:%M:%S.%f")
    if not DATE: return datetime.now().strftime("%H%M%S")
    return datetime.now().strftime("%m-%d-%H-%M-%S")

def format_time(timestamp):
    if '.' in timestamp:
        return datetime.strptime(timestamp, '%Y-%m-%dT%H:%M:%S.%fZ')
    else:
        return datetime.strptime(timestamp, '%Y-%m-%dT%H:%M:%SZ')

def get_dir_file(file):
    """ separate the dir string and name string from a file string """
    name = file.split('/')[-1]
    return file[:file.index(name)], name

def point_distance(x1, y1, x2, y2):
    return math.sqrt((x1 - x2) ** 2 + (y1 - y2) ** 2)

def tile_changed(x, z, yaw, x_last, z_last, yaw_last):
    # if abs(x-x_last) > 2 or abs(z-z_last) > 2: return False     ## the player has been teleported
    # print(f'from ({x_last},{z_last})-{head_last} to ({x},{z})-{head}')
    def large_diff(a, b):
        return (abs(a-b) >= 1)

    def small_diff(a, b):
        return (abs(a - b) <= 3)

    MOVED = large_diff(x, x_last) or large_diff(z, z_last) or abs(yaw-yaw_last) >= 90
    NOT_TOO_BIG = small_diff(x, x_last) and small_diff(z, z_last)

    return MOVED and NOT_TOO_BIG

def num2col(n):
    string = ""
    while n > 0:
        n, remainder = divmod(n - 1, 26)
        string = chr(65 + remainder) + string
    return string

def col2num(col_str):
    """ Convert base26 column string to number. """
    expn = 0
    col_num = 0
    for char in reversed(col_str):
        col_num += (ord(char) - ord('A') + 1) * (26 ** expn)
        expn += 1
    return col_num

def get_reL_path(thing):
    ## get the relative path from absolute path
    if 'Users' in thing:
        current_dir = abspath(getcwd())
        num = len(current_dir) - len(current_dir.replace('/', ''))
        for i in range(num):
            thing = join('..', thing)
    return thing

def resize_gif(size, CHECK=True):
    resize(size, CHECK=CHECK)
    pass

def assign_rooms(old_room_csv, new_map_csv):
    path = join('maps')

    old_room_table = copy.deepcopy(list(csv.reader(open(join(path, old_room_csv)), delimiter=',')))
    new_room_table = copy.deepcopy(list(csv.reader(open(join(path, new_map_csv)), delimiter=',')))

    for i in range(len(old_room_table)):
        for j in range(len(old_room_table[0])):
            if new_room_table[i][j] in ['', 'V', 'VV', 'B', 'D']:
                if old_room_table[i][j].isdigit():
                    new_room_table[i][j] = old_room_table[i][j]
                elif old_room_table[i][j] == 'W':
                    for m, n in [(0, 1), (0, -1), (1, 0), (-1, 0)]:
                        if old_room_table[i+m][j+n].isdigit():
                            new_room_table[i][j] = old_room_table[i+m][j+n]
                            break
            ## make the rest to - which is easy to look up and write ourselves
            if new_room_table[i][j] == "":
                new_room_table[i][j] = "-"

    new_room_csv = join(path, new_map_csv.replace('.csv','_rooms.csv'))
    with open(new_room_csv, mode='w') as file:
        writer = csv.writer(file, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
        writer.writerows(new_room_table)

if __name__ == '__main__':
    # resize(20)
    # convert_pdf(join('recordings', '__report Leslie'))
    assign_rooms('48by89_rooms.csv', '48by89_hard.csv')
    assign_rooms('48by89_rooms.csv', '48by89_med.csv')
    assign_rooms('48by89_rooms.csv', '48by89_easy.csv')
