import copy
import matplotlib.pyplot as plt
plt.style.use('ggplot')
import numpy as np
from matplotlib import collections as matcoll
from matplotlib.ticker import (AutoMinorLocator, MultipleLocator)
from os import listdir, mkdir
from os.path import isfile, isdir, join
import json
import math

from utils import colors, get_dir_file
import player
 ##  and 'h9' not in k and 'h12' not in k
player_types = [k for k in player.players if 'ma*' in k] ##['uct-24']  ##['both', 'yellow', 'with_dog_both', 'with_dog_yellow'] # ['both', 'yellow', 'green', 'with_dog_both', 'with_dog_yellow', 'with_dog_green']
player_params = {
    'observation_reward': [0.01, 0.005, 0.001],
    'cost_go_straight': [0.02, 0.01, 0.005],
    'tilelevel_gamma': [0.9, 0.8],
    'certainty_boost_factor': [10, 30],
}
def get_player_types(player, player_name, CHECK=True):
    players = {player_name: player}
    for item, values in player_params.items():
        for value in values:
            if not CHECK or player[item] != value:
                new_player = copy.deepcopy(player)
                new_player[item] = value
                new_player_name = f"{player_name}-{item[0]}-{str(value).replace('.', ',')}"
                players[new_player_name] = new_player
    return players

player_shifts = {
    'green': (-1 / 3, -1 / 4), 'both': (0, -1 / 4), 'yellow': (1 / 3, -1 / 4),
    'with_dog_green': (-1 / 3, 1 / 4), 'with_dog_both': (0, 1 / 4), 'with_dog_yellow': (1 / 3, 1 / 4)
}

world_configs = {  ## MAP, ranges, json_folder
    'sparky': ('46by45_2.csv', (-2153, -2108, 153, 207, 52, 54), join('maps', '46by45_2_rooms_colored.gif')),
    'falcon': ('48by89.csv', (-2108, -2020, 144, 191, 60, 62), None)
}

block_to_floor = {
    'block_victim_1': 'victim',
    'block_victim_2': 'victim-yellow',
    'block_victim_expired': 'victim-red',
    'gravel': 'wall',
    'bedrock': 'wall',
    'acacia_door': 'door',
    'dark_oak_door': 'door',
    'wooden_door': 'door',
    'fire': 'fire',
    'perturbation_opening': 'air',
    # 'gravel': 'victim',
    # 'bedrock': 'victim',
    # 'acacia_door': 'victim',
    # 'dark_oak_door': 'victim',
    # 'wooden_door': 'victim',
    # 'fire': 'victim',
}

room_names = {
    0: 'Entrance Lobby',
    1: 'Left Hallway',
    2: 'Security Office',
    3: 'The Computer Farm',
    4: 'Left Hallway',
    5: 'Open Break Area',
    6: 'Executive Suite 1',
    7: 'Center Hallway Bottom',
    8: 'Left Hallway',
    9: 'Center Hallway Bottom',
    10: 'Janitor',
    11: 'Left Hallway',
    12: 'Left Hallway',
    13: 'Executive Suite 2',
    14: "King Chris's Office",
    15: "The King's Terrace",
    16: 'Center Hallway Middle',
    17: 'Center Hallway Middle',
    18: 'Herbalife Conference Room',
    19: 'Center Hallway Top',
    20: 'Center Hallway Top',
    21: 'Room 101',
    22: 'Room 102',
    23: 'Room 103',
    24: 'Room 104',
    25: 'Room 105',
    26: 'Room 106',
    27: 'Room 107',
    28: 'Room 108',
    29: 'Room 109',
    30: 'Room 110',
    31: 'Room 111',
    32: 'Right Hallway',
    33: 'Right Hallway',
    34: 'Right Hallway',
    35: 'Right Hallway',
    36: 'Right Hallway',
    37: 'Right Hallway',
    38: 'Right Hallway',
    39: 'Amway Conference Room',
    40: 'Mary Kay Conference Room',
    41: "Women's Room",
    42: "Men's Room",
}

HSR_dir = join('Users', 'z', 'Dropbox (MIT)', 'study-1_2020.08-rmq', 'with-fov', 'with-fov')

def get_conditions(lst=None):
    if lst == None:
        lst = ['TriageSignal_FalconEasy-Static', 'TriageSignal_FalconEasy-Dynamic',
                'TriageSignal_FalconMed-Static', 'TriageSignal_FalconMed-Dynamic',
                'TriageSignal_FalconHard-Static', 'TriageSignal_FalconHard-Dynamic',

                'TriageNoSignal_FalconEasy-Static',
                'TriageNoSignal_FalconMed-Static',
                'TriageNoSignal_FalconHard-Static',

                'NoTriageNoSignal_FalconEasy-Static',
                'NoTriageNoSignal_FalconMed-Static',
                'NoTriageNoSignal_FalconHard-Static',
                ]
    lst = [a.replace('_', '_CondWin-') for a in lst]
    return lst

def get_members():
    members = []
    for memberNumber in range(26, 103):
        members.append(f'Member-{memberNumber}')
    return members

def get_map(string, default='48by89.csv'):
    map = default
    if 'easy' in string.lower():
        map = '48by89_easy.csv'
    elif 'med' in string.lower():
        map = '48by89_med.csv'
    elif 'hard' in string.lower():
        map = '48by89_hard.csv'
    return map

def extract_pauses(pauses_file, pauses_dir):
    # pauses_file = 'NoTriageNoSignal_FalconMed-StaticMap_Trial-105_Member-46.json'
    # pauses_dir = '../recordings/test_analyzer_RITA/video/'

    def smart_linebreak(tt, width=50):
        if tt == "": return ""
        new_text = ""
        new_line = ""
        words = tt.split(" ")
        for word in words:
            propozed_new_line = new_line + " " + word
            if len(propozed_new_line) < width:
                new_line += " " + word
            else:
                new_text += " " + new_line + '\n'
                new_line = word
        new_text += " " + new_line + '\n'
        new_text = new_text[1:]
        return new_text

    def get_countup(t):
        return 600 - (t['min'] * 60 + t['sec'])

    pauses = {}
    self_talks = {}
    anxiety_level = []
    with open(join(pauses_dir, pauses_file)) as f:
        raw = json.load(f)
        player_sketch = raw["Comment"]
        #     print(player_sketch)
        for countup, notes in raw["Pauses"].items():
            countup = round(float(countup), 1)

            text = ''
            text += f"Strategy: {notes['1 strategy']}\n"
            text += smart_linebreak(notes['1 strategy notes'])
            text += f"\nLocalization: {notes['2 localization']}"
            text += f"\n\nConfidence: {notes['3 confidence']}\n"
            text += smart_linebreak(notes['3 confidence notes'])
            text += f"\nAnxiety: {notes['4 anxiety']}\n"
            text += smart_linebreak(notes['4 anxiety notes'])
            text += f"\nEfforts: {notes['5 efforts']}\n"
            text += smart_linebreak(notes['5 efforts notes'])
            pauses[countup] = text[:-2]

            events = notes['6 events that follow']
            for event in events:
                if 'skip' not in event:
                    begin = get_countup(event['begin'])
                    end = get_countup(event['end'])
                    text = smart_linebreak(event['event'], width=12)
                    self_talks[begin] = [text, begin, end]
        #             print(text)
        #             print()

            anxiety = notes['4 anxiety']
            if anxiety == "": anxiety = 7
            anxiety_level.append(anxiety)

    return player_sketch, pauses, self_talks, anxiety_level

def get_beep_location(data, range):
    x_low, x_high, z_low, z_high, y_low, y_high = range
    location = None
    if 'woof_z' in data:
        location = (data['woof_z'] - z_low, data['woof_x'] - x_low)
    elif 'beep_z' in data:
        location = (data['beep_z'] - z_low, data['beep_x'] - x_low)
    else:
        assert "no location found!"
    return location

def countup_to_clock(countup):
    countup = 600 - countup
    minute = math.floor(countup/60)
    second =  int(countup%60)
    if second < 10:
        second = f"0{second}"
    return f"{minute}:{second}"

def make_plot_individual(trial, output_name):
    dir_name, file_name = get_dir_file(output_name)
    dir_name = dir_name.replace('pdf', 'video')

    id = trial['Trial']
    member_id = trial['Member']
    final_score = trial['Triage']['final_score']

    title = f"Analysis of Trial {id}"

    device_yellow_x = trial['Device']['device_yellow']  ## [5, 56, 58, 200, 430]
    device_green_x = trial['Device']['device_green']  ## [12, 15, 33, 56]
    triaged_yellow_x = list(trial['Triage']['triaged_yellow'].keys())  ## [5, 19, 200, 404.2, 490.9]
    triaged_green_x = list(trial['Triage']['triaged_green'].keys())  ## [2.8, 19.5, 100, 700]
    skipped_yellow_x = trial['Triage']['skipped_yellow']  ## [15, 29, 210, 304.2, 390.9]
    skipped_green_x = trial['Triage']['skipped_green']  ## [12.8]
    remembered_green_skipped = trial['Triage']['remembered_green']['skipped']
    remembered_green_triaged = trial['Triage']['remembered_green']['triaged']

    strategy = trial['Triage']['strategy']
    strategy_yellow = trial['Triage']['strategy_yellow']
    strategy_green = trial['Triage']['strategy_green']
    rooms_triaged = trial['Navigation']['rooms_triaged']
    rooms_no_triage = trial['Navigation']['rooms_no_triage']
    rooms_revisit = trial['Navigation']['rooms_revisited']
    events = trial['Events']

    suptitle = f"{file_name.replace('_','   |   ').replace('.pdf','')}   |   Strategy (first half): {strategy}   |   Score: {final_score}" ## Member: {member_id}

    player_sketch, pauses, self_talks, anxiety_level = extract_pauses(file_name.replace('.pdf','.json'), dir_name)

    X = trial['Motion']['X']  ## np.linspace(0, 600, 6001)
    Ya = trial['Motion']['Ya']  ## np.sin(X)
    Yb = trial['Motion']['Yb']  ## np.cos(X) / 2
    Ya_lim = 1
    Yb_lim = 100
    Ya_thre = 0.4
    Yb_thre = 40
    Ya_cap = [int(y >= Ya_thre) * y for y in Ya]
    Yb_cap = [int(y >= Yb_thre) * y for y in Yb]

    time_pressure = list(pauses.keys())
    time_pressure.append(300)

    def add_data(x, height=1.0, axx=None, color=colors.midnight, duration=0, linewidth=1, dot=False):

        if not isinstance(height, list):
            y = [height] * len(x)
        else:
            y = height

        if not dot: ## only draw the dot
            lines = []
            for i in range(len(x)):
                pair = [(x[i], 0), (x[i], y[i])]
                lines.append(pair)

                ## color the whole area
                if duration != 0:
                    for d in range(1, math.floor(duration)):
                        pair = [(x[i]-d, 0), (x[i]-d, y[i])]
                        lines.append(pair)
                    linewidth = 3
            linecoll = matcoll.LineCollection(lines, color=color, linewidths=(linewidth, ))
            axx.add_collection(linecoll)

        if color != colors.white:
            plt.scatter(x, y, color=color)
            all_xticks.extend(x)

    def add_annotatiion(episodes, height, axx=None, color=colors.midnight, linewidth=1, fontsize=10, arrowstyle='|-|'):
        for k, (text, x, y) in episodes.items():
            axx.annotate('', xy=(x, height), xytext=(y, height), xycoords='data', textcoords='data',
                         arrowprops={'arrowstyle': arrowstyle, 'color': color, 'linewidth': linewidth})
            if arrowstyle == '|-|':
                axx.annotate(text, xy=((x + y) / 2, height + 0.3), ha='center', va='bottom',
                            fontsize=fontsize, color=color)
            if color in [colors.red, colors.dark_yellow, colors.green]:
                all_xticks.append(y)

    def add_text(episodes, height=1.0, axx=None, color=colors.midnight, linewidth=1, fontsize=10):
        if not isinstance(height, list):
            height = [height] * len(episodes)
        i = 0
        for k, text in episodes.items():
            axx.annotate(text, xy=(k + 4, height[i] / 2), va='center',  ## ha='center',
                         fontsize=fontsize, color=color)
            i += 1

    fig = plt.figure()
    # fig.set_figheight(20)
    # fig.set_figwidth(15)
    fig.set_size_inches(25, 20)
    label_pad = 10
    label_font = 16
    time_ticks = np.linspace(0, 600, 21)

    ## --------------------------------------------------------
    ##     Level 1 - Story-Level events
    ## --------------------------------------------------------

    ## subplot 1 shows events & episodes
    ax1 = fig.add_subplot(511, frame_on=True)  ## False
    plt.title(title, y=1.24, fontsize=26)
    plt.suptitle(suptitle, y=0.90, fontsize=18)

    all_xticks = []
    add_data(time_pressure, height=9, axx=ax1, color=colors.white, linewidth=2)
    add_annotatiion(self_talks, 1, axx=ax1, color=colors.red, fontsize=10)
    add_annotatiion(strategy_yellow, 5, axx=ax1, color=colors.dark_yellow, linewidth=2, fontsize=14)
    add_annotatiion(strategy_green, 5.5, axx=ax1, color=colors.green, linewidth=2, fontsize=14)
    add_annotatiion(events, 7, axx=ax1, color=colors.blue, fontsize=12)
    ax1.set(yticklabels=[])
    ax1.yaxis.set_ticks([])
    plt.setp(ax1.xaxis.get_majorticklabels(), rotation=45)
    plt.setp(ax1.get_yticklabels(), visible=False)
    plt.grid(False)
    plt.ylabel("Events\n & Strategies", labelpad=label_pad, fontsize=label_font)
    plt.ylim(0, 9)
    plt.xticks(list(set(all_xticks)))
    plt.xlim(0, 600)

    ## subplot 2 shows pauses answers
    ax2 = fig.add_subplot(512)
    all_xticks = []
    if anxiety_level != None:
        add_data([300], height=7.2, axx=ax2, color=colors.white, linewidth=2)
        add_data(list(pauses.keys()), height=anxiety_level, axx=ax2, color=colors.red)
        add_text(pauses, height=7.2, axx=ax2, color=colors.red)
        ax2.set(yticklabels=[])
        ax2.yaxis.set_ticks([])
        ax2.tick_params(axis='y', colors=colors.red)
        ax2.tick_params(axis='x', colors=colors.red)
        ax2.set_ylabel('Pauses\n & Answers', labelpad=label_pad, color=colors.red, fontsize=label_font)
        plt.setp(ax2.get_yticklabels(), visible=False)
        plt.grid(False)
        plt.ylim(0, 7.2)
        plt.xticks(list(set(all_xticks)))
        plt.xlim(0, 600)

    ## --------------------------------------------------------
    ##     Level 2 - Goal-level actions
    ## --------------------------------------------------------

    ## subplot 2 shows lowlevel action statistics
    color3a = colors.orange
    color3b = colors.purple
    color3c = colors.blue

    ## subplot 3b shows wrong rooms entered
    ax3b = fig.add_subplot(513, label="1")
    all_xticks = []
    add_data(time_pressure, height=8, axx=ax3b, color=colors.white, linewidth=2)
    add_annotatiion(rooms_triaged, 5, axx=ax3b, color=color3c, arrowstyle='-', linewidth=2)
    add_annotatiion(rooms_revisit, 6, axx=ax3b, color=color3a, arrowstyle='-', linewidth=2)
    add_annotatiion(rooms_no_triage, 7, axx=ax3b, color=color3b, arrowstyle='-', linewidth=2)
    plt.setp(ax3b.get_yticklabels(), visible=False)
    ax3b.set(yticklabels=[])
    ax3b.yaxis.set_ticks([])
    ax3b.set_ylabel('Rooms Revisited', labelpad=label_pad, color=color3a, fontsize=label_font)
    ax3b.set_xlabel('Rooms Without Triage', labelpad=label_pad, color=color3b, fontsize=label_font)
    ax3b.yaxis.set_label_position('right')
    ax3b.xaxis.set_label_position('top')
    plt.grid(False)
    plt.ylim(0, 8)
    plt.xticks(list(set(all_xticks)))
    plt.xlim(0, 600)

    ## subplot 3a shows devices used and rooms entered
    ax3a = fig.add_subplot(513, label="2", frame_on=False)
    all_xticks = []
    add_data(device_yellow_x, height=1, axx=ax3a, color=colors.yellow)
    add_data(device_green_x, height=1, axx=ax3a, color=colors.green)
    ax3a.yaxis.set_ticks([])
    ax3a.yaxis.set_ticks([])
    plt.setp(ax3a.get_yticklabels(), visible=False)
    plt.grid(False)
    plt.ylabel("Rooms Triaged", labelpad=label_pad, color=color3c, fontsize=label_font)
    plt.xlabel("Device Beeped", labelpad=label_pad, fontsize=label_font)
    plt.ylim(0, 4)
    plt.xticks(list(set(all_xticks)))  ## , rotation='vertical'
    plt.setp(ax3a.xaxis.get_majorticklabels(), rotation=45)
    ax3a.tick_params(axis="x", direction="in", pad=-80)
    ax3a.spines['left'].set_visible(False)
    plt.xlim(0, 600)


    ## subplot 4a shows victims triaged
    ax3 = fig.add_subplot(10, 1, 7, label="1")
    all_xticks = []
    add_data(time_pressure, height=1, axx=ax3, color=colors.white, linewidth=2)
    add_data(triaged_yellow_x, height=0.5, axx=ax3, color=colors.yellow, duration=15)
    add_data(triaged_green_x, height=0.5, axx=ax3, color=colors.green, duration=7.5)
    add_data(remembered_green_triaged, height=0.5, axx=ax3, color=colors.red, dot=True)
    ax3.set(yticklabels=[])
    ax3.yaxis.set_ticks([])
    plt.setp(ax3.get_yticklabels(), visible=False)
    plt.grid(False)
    plt.ylabel("Victims \nTriaged", labelpad=label_pad, fontsize=label_font)
    plt.ylim(0, 1)
    plt.xticks(list(set(all_xticks)))  ## , rotation='vertical'
    plt.setp(ax3.xaxis.get_majorticklabels(), rotation=45)
    ax3.tick_params(axis="x", direction="in", pad=-80)
    plt.xlim(0, 600)

    ax3c = fig.add_subplot(10, 1, 7, label="2", frame_on=False)
    plt.setp(ax3c.get_yticklabels(), visible=False)
    ax3c.set(yticklabels=[])
    ax3c.yaxis.set_ticks([])
    ax3c.set(xticklabels=[])
    ax3c.xaxis.set_ticks([])
    ax3c.set_ylabel('Victims\n Found\n Back', labelpad=label_pad, color=colors.red, fontsize=label_font)
    ax3c.yaxis.set_label_position('right')
    plt.grid(False)

    ## subplot 4b shows victims skipped
    ax4 = fig.add_subplot(10, 1, 8, label="1")
    all_xticks = []
    add_data(time_pressure, height=1, axx=ax4, color=colors.white, linewidth=2)
    add_data(skipped_yellow_x, height=0.5, axx=ax4, color=colors.yellow)
    add_data(skipped_green_x, height=0.5, axx=ax4, color=colors.green)
    add_data(remembered_green_skipped, height=0.5, axx=ax3, color=colors.red, dot=True)
    ax4.set(yticklabels=[])
    ax4.yaxis.set_ticks([])
    plt.setp(ax4.get_yticklabels(), visible=False)
    plt.grid(False)
    plt.ylabel("Victims \nSkipped", labelpad=label_pad, fontsize=label_font)
    plt.ylim(0, 1)
    plt.xticks(list(set(all_xticks)))  ## , rotation='vertical'
    plt.setp(ax4.xaxis.get_majorticklabels(), rotation=45)
    ax4.tick_params(axis="x", direction="in", pad=-80)
    plt.xlim(0, 600)

    ax4c = fig.add_subplot(10, 1, 8, label="2", frame_on=False)
    plt.setp(ax4c.get_yticklabels(), visible=False)
    ax4c.set(yticklabels=[])
    ax4c.yaxis.set_ticks([])
    ax4c.set(xticklabels=[])
    ax4c.xaxis.set_ticks([])
    ax4c.set_ylabel('Victims\n Eventually\n Triaged', labelpad=label_pad, color=colors.red, fontsize=label_font)
    ax4c.yaxis.set_label_position('right')
    plt.grid(False)

    ## --------------------------------------------------------
    ##     Level 3 - Low-level actions
    ## --------------------------------------------------------

    ## subplot 5 shows lowlevel action statistics
    color5a = colors.orange
    color5b = colors.purple
    color5a_trans = colors.orange_trans
    color5b_trans = colors.purple_trans

    ## subplot 2 shows lowlevel action statistics - speed
    ax5a = fig.add_subplot(515, label="1")
    add_data(time_pressure, height=Ya_lim, axx=ax5a, color=colors.white, linewidth=2)
    ax5a.plot(X, Ya, color=color5a_trans)
    ax5a.scatter(X, Ya_cap, color=color5a, linewidths=1)
    ax5a.set_ylabel('Speed', labelpad=label_pad, color=color5a, fontsize=label_font)
    # plt.setp(ax5a.get_xticklabels(), visible=False)
    ax5a.tick_params(axis='y', colors=color5a)
    ax5a.spines['top'].set_visible(False)
    plt.grid(False)
    plt.ylim(0, Ya_lim)
    plt.xlim(0, 600)
    # plt.xticks([300, 600])
    ax5a.xaxis.tick_bottom()
    ax5a.set_xticks([300, 600])
    ax5a.xaxis.set_label_position('bottom')
    ax5a.spines['bottom'].set_visible(True)

    ## subplot 2 shows lowlevel action statistics - turns
    ax5b = fig.add_subplot(515, label="2", frame_on=False)
    ax5b.plot(X, Yb, color=color5b_trans)
    ax5b.scatter(X, Yb_cap, color=color5b, linewidths=1)
    ax5b.yaxis.tick_right()
    ax5b.xaxis.tick_top()
    ax5b.set_ylabel('Turns', labelpad=label_pad, color=color5b, fontsize=label_font)
    ax5b.tick_params(axis='y', colors=color5b)
    plt.grid(False)
    plt.ylim(0, Yb_lim)
    plt.xlim(0, 600)
    # plt.xticks()
    ax5b.set_xticklabels([countup_to_clock(x) for x in time_ticks])
    ax5b.set_xticks(time_ticks)
    ax5b.yaxis.set_label_position('right')
    ax5b.xaxis.set_label_position('top')
    ax5b.spines['top'].set_visible(True)

    # fig.savefig(output_name, dpi=100)
    fig.savefig(output_name)
    plt.close()
    # print('saved fig in', output_name)
    # plt.show()