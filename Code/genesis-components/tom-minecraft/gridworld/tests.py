from os.path import join, isdir
from os import mkdir
import shutil
from pprint import pformat
from tabulate import tabulate
import csv
import pandas as pd
import numpy as np
import json
from pprint import pprint
import matplotlib.pyplot as plt

plt.style.use('ggplot')
# import coloredlogs, logging

import planners
import player
import interface
import mdp
import ASIST_settings


def run_single_planning(map, player_name, planner_name, recordings_dir, seed=None, timeout=10,
                        goal_state_action=None, player_types_name=None, USE_INTERFACE=True, SHOW_FINAL_ONLY=False,
                        WIPE_VIZ=True, MAGNIFY=False, planner_param=None, verbose=False):
    ## creating the environment, planner, recording mechanism, and visualization interface
    env = mdp.POMDP(map, player_name=player_name)  ## , ORACLE_MODE=True
    if player_types_name != None:
        actual_player_types = ASIST_settings.get_player_types(env.player, player_name)
        if player_types_name not in actual_player_types:
            return
        env.change_player(player_types_name, player=actual_player_types[player_types_name])

    planner = planners.get_planner(env, planner_name, goal_state_action=goal_state_action, verbose=verbose, seed=seed,
                                   timeout=timeout, planner_param=planner_param)
    planner.init_recordings(recordings_dir, initial_text=pformat(env.player, indent=1))
    planner.set_VIZ(interface.VIZ(env, USE_INTERFACE=USE_INTERFACE, SHOW_FINAL_ONLY=SHOW_FINAL_ONLY, MAGNIFY=MAGNIFY))

    ## special settings for the experiment
    planner.WIPE_VIZ = WIPE_VIZ

    result = planners.plan(env, planner)
    return result, (
    len(env.victim_summary['V']) + len(env.victim_summary['VV']), env.max_iter, round(planner._timeout_game))


def run_single_planning_method(map, player_name, planner_name, recordings_dir, seeds=10, timeout=10,
                               HOW_FINAL_ONLY=True, WIPE_VIZ=True, MAGNIFY=False, planner_param=None):
    results = []
    for seed in range(seeds):
        result, upper_bounds = \
            run_single_planning(map, player_name, planner_name, recordings_dir,
                                seed=seed, planner_param=planner_param, timeout=timeout,
                                SHOW_FINAL_ONLY=HOW_FINAL_ONLY, WIPE_VIZ=WIPE_VIZ, MAGNIFY=MAGNIFY)
        results.append(result)
    return results, upper_bounds


def print_results(level, all_results, columns, recordings_dir, STD=False, CSV=False, CSV_PREFIX=''):
    ## make the directory for csv results
    recordings_dir = join(recordings_dir, 'results')
    if not isdir(recordings_dir):
        mkdir(recordings_dir)
    writer = open(join(recordings_dir, 'results.txt'), "w")

    def write(text):
        print(text)
        writer.write(text + '\n')

    write(f"\n### Map: {level} ###")
    mean_table = [(a,) + tuple(np.mean(all_results[level][a], axis=0)) for a in sorted(all_results[level])]
    std_table = [(a,) + tuple(np.std(all_results[level][a], axis=0)) for a in sorted(all_results[level])]
    # print("\n# Means #")
    write(tabulate(mean_table, headers=columns))

    if STD:
        write("\n# Standard Deviations #")
        write(tabulate(std_table, headers=columns))
    if CSV:
        with open(join(recordings_dir, f'means_{CSV_PREFIX}{level}.csv'), 'w', newline='') as myfile:
            wr = csv.writer(myfile, quoting=csv.QUOTE_ALL)
            # wr.writerow([f'LEVEL {level}'])
            wr.writerow(columns)
            for row in mean_table:
                wr.writerow(list(row))
            wr.writerow('')
        with open(join(recordings_dir, f'std_{CSV_PREFIX}{level}.csv'), 'w', newline='') as myfile:
            wr = csv.writer(myfile, quoting=csv.QUOTE_ALL)
            # wr.writerow([f'LEVEL {level}'])
            wr.writerow(columns)
            for row in std_table:
                wr.writerow(list(row))
            wr.writerow('')


def generate_plots(maps, map_labels, columns, recordings_dir, plot_title='Comparing Planning Methods'):
    means_prefix = 'means_'
    std_prefix = 'std_'

    columns.remove('Method')

    renames_index = {
        'lrtdp': 'LRTDP',
        'rtdp': 'RTDP',
        'vi': 'Value Iteration',
        'pi': 'Policy Iteration',
        # 'hvi': 'Hierarchical VI',
        'uct-ave': 'UCT (ave)',
        'uct-max': 'UCT (max)',
    }

    def get_all_levels(prefix, column):
        for idx in range(len(maps)):
            map = maps[idx]
            temp = pd.read_csv(join(recordings_dir, 'results', f'{prefix}{map}.csv'))
            temp.set_index('Method', inplace=True)
            temp = temp[[column]]
            temp = temp.rename({column: map_labels[map][column]}, axis='columns')
            temp = temp.rename(renames_index, axis='index')
            if idx == 0:
                means = temp
            else:
                means = means.join(temp)
        return means.T

    fig, ax = plt.subplots(3, figsize=(7, 8))
    plt.subplots_adjust(hspace=0.4)

    for index in range(len(columns)):
        column = columns[index]
        means = get_all_levels(means_prefix, column)
        ax[index].set_ylabel(column, fontsize='medium')
        #     std = get_all_levels(std_prefix)
        means.plot.bar(ax=ax[index])  ## yerr=std,
        #     ax[index].legend(title='Methods', bbox_to_anchor=(1, 1.05), loc='upper left')
        ax[index].get_legend().remove()
        ax[index].set_xticklabels(ax[index].get_xticklabels(), rotation=0)

    # if 'UCT' in plot_title: ax[0].set_ylim(top=3)
    # if 'VI' in plot_title or 'PI' in plot_title:
    #     ax[0].set_ylim(top=6.2)
    #     ax[1].set_ylim(top=105)
    #     ax[2].set_ylim(top=25)
    # if 'RTDP' in plot_title:
    #     ax[1].set_ylim(top=225)
    #     ax[2].set_ylim(top=22)
    ax[0].set_ylim(top=8.2)
    ax[1].set_ylim(top=210)
    ax[2].set_ylim(top=25)

    ## show one legent
    if len(means.columns) <= 9:
        ax[0].legend(title='Method', bbox_to_anchor=(0.5, 1.6), ncol=3, loc='upper center')
        fig.suptitle(plot_title, y=1.05, fontsize=14)
    else:  ## if len(means.columns) <= 12:
        ax[0].legend(title='Method', bbox_to_anchor=(0.5, 1.8), ncol=3, loc='upper center')
        fig.suptitle(plot_title, y=1.09, fontsize=14)

    # plot_dir = join(recordings_dir, 'plots')
    # if not isdir(plot_dir): mkdir(plot_dir)

    file_name = 'Summary'
    if 'Parameters' in plot_title:
        file_name = plot_title[plot_title.index('of') + 3:]
    plt.savefig(join(recordings_dir, f'{file_name}.pdf'), bbox_inches='tight', pad_inches=1)


def get_params(planner_name, planner_params_spaces):
    planner_names = []
    planner_params_space = planner_params_spaces[planner_name]
    planner_params = []
    for key, values in planner_params_space.items():
        for value in values:
            planner_params.append({key: value})
            planner_names.append(f'{planner_name.upper()} ({key}={value})')
    print(f'... initiated {len(planner_names)} {planner_name} planners')
    return planner_names, planner_params


def test_planning_template(planner_names=None, planner_params=None, test_name='test_template',
                           plot_title='', just_plot=False, seeds=3):
    maps = [
        # '6by6_3_Z.csv',
        # '13by13_3.csv',
        '12by12_6.csv', '24by24_6.csv',
        '46by45_2.csv'
    ]  ##
    player_name = 'tester'
    # player_names = {'lrtdp': 'rtdp'}  ## same as planner name for now
    planner_param = None

    timeout = 10
    columns = ['Method', '# Victims Saved', '# Steps Taken', 'Time taken (s)']
    recordings_dir = join('recordings', test_name)  ## _{utils.get_time()

    all_results = {}
    map_labels = {}
    map_labels_saved = join(recordings_dir, 'results', 'map_labels.json')

    if not just_plot:
        if isdir(recordings_dir): shutil.rmtree(recordings_dir)
        for map in maps:
            print('\n=======================================')
            print('Experimenting on Map:', map)
            all_results[map] = {}
            for planner_name in planner_names:
                print('    Experimenting with Planner:', planner_name)

                ## when testing parameters
                if planner_params != None:
                    planner_param = planner_params[planner_names.index(planner_name)]

                # if planner_name in player_names:
                #     player_name = player_names[planner_name]
                # else:
                #     player_name = planner_name

                all_results[map][planner_name], upper_bounds = \
                    run_single_planning_method(map, player_name, planner_name, recordings_dir,
                                               seeds=seeds, timeout=timeout, planner_param=planner_param)
            print_results(map, all_results, columns, recordings_dir, CSV=True)

            ## record the upper bounds of various metrics for the plot
            n_victims, max_steps, timeout = upper_bounds
            map_short = map[:map.find('_')]  ## get the substring before the first _
            map_labels[map] = {}
            map_labels[map][columns[1]] = f'{map_short} \n≤ {n_victims}'
            map_labels[map][columns[2]] = f'{map_short} \n≤ {max_steps}'
            map_labels[map][columns[3]] = f'{map_short} \n≤ {timeout}'

        ## print again in the end
        print(f'\n* * * * * * * * * * * {test_name} * * * * * * * * * * * * ')
        for map in maps:
            print_results(map, all_results, columns, recordings_dir)
        with open(map_labels_saved, 'w') as outfile:
            json.dump(map_labels, outfile)
    else:
        with open(map_labels_saved, 'r') as infile:
            map_labels = json.load(infile)

    generate_plots(maps, map_labels, columns, recordings_dir, plot_title)


def test_planning_methods(just_plot=False, seeds=3):
    planner_names = [
        'lrtdp',
        # 'rtdp',
        # 'vi',
        # 'pi',
        # 'uct-ave',
        # 'uct-max',

        'hvi',
    ]

    test_planning_template(planner_names=planner_names, test_name=f'test_planning_methods-seeds={seeds}',
                           plot_title='Comparing Planning Methods', just_plot=just_plot, seeds=seeds)


def test_planning_parameters(planner_name='lrtdp', just_plot=False, seeds=3):
    initial_Qs = [0, 2, 5]
    epsilon_cons = [0.0001, 0.001, 0.01]
    epsilon_exps = [0, 0.1, 0.2]
    gammas = [0.8, 0.9, 0.95]  #
    betas = [0, 0.75, 1, 2]
    simulations = [50, 100, 200, 500]
    planner_params_spaces = {
        'lrtdp': {
            'initial_Q': initial_Qs,
            'epsilon_exp': epsilon_exps,
            'gamma': gammas,
            'simulations': simulations
        },
        'rtdp': {
            'initial_Q': initial_Qs,
            'epsilon_exp': epsilon_exps,
            'gamma': gammas,
            'simulations': simulations
        },
        # 'hvi': [],
        'vi': {'epsilon_con': epsilon_cons, 'gamma': gammas},
        'pi': {'epsilon_con': epsilon_cons, 'gamma': gammas},
        'uct-ave': {'beta': betas, 'gamma': gammas},
        'uct-max': {'beta': betas, 'gamma': gammas},
    }
    planner_names, planner_params = get_params(planner_name, planner_params_spaces)
    test_planning_template(planner_names=planner_names, planner_params=planner_params,
                           test_name=f'test_parameters-{planner_name}',
                           plot_title=f'Comparing Parameters of {planner_name.upper()}',
                           just_plot=just_plot, seeds=seeds)


def test_planning(map='6by6_3_Z.csv', player_name='rtdp', planner_name='lrtdp', goal_state_action=None,
                  recordings_dir=join('recordings', 'test_planning_test')):
    # map = '12by12_6.csv'
    # map = '13by13_2.csv'
    # map = '24by24_6.csv'  ## one victim in corridor
    map = 'test3.csv'
    # map = 'test4.csv'
    # map = 'test5.csv'
    # map = '46by45_2.csv'
    # map = '48by89.csv'
    # map = '48by89_easy.csv'
    # map = '48by89_med.csv'
    # map = '48by89_hard.csv'

    # player_name = 'vi'
    player_name = 'lrtdp'
    player_name = 'ra*'
    # player_name = 'uct-24'
    # player_name = 'tester'  ## in order for lrtdp to run
    # player_name = 'uct-13'  ## in order for uct to run
    # player_name = 'uct-24'  ## in order for uct to run
    # player_name = 'ma*-h3'  ## in order for uct to run
    # player_name = 'both'
    # player_name = 'with_dog_yellow'

    # planner_name = 'hvi'
    # planner_name = 'hva*'
    planner_name = 'ha*'

    # planner_name = 'muct'
    # planner_name = 'ma*'
    # planner_name = 'mra*'
    # planner_name = 'ra*'
    # planner_name = 'rdfs'
    # planner_name = 'rbnb'

    # planner_name = 'vi'
    # planner_name = 'lrtdp'
    # planner_name = 'uct'
    # planner_name = 'a*'
    # goal_state_action = 'ALL_TILES'
    # goal_state_action = ((733, 270), 'go_straight')

    ## show the whole animation, close viz automatically, or let interface stay there
    # result = run_single_planning(map, player_name, planner_name, recordings_dir, timeout=100)
    result = run_single_planning(map, player_name, planner_name, recordings_dir, goal_state_action=goal_state_action,
                                 verbose=False, timeout=100, WIPE_VIZ=False, MAGNIFY=True)  #

    # ## show only the last image, or not show use interface at all
    # result = run_single_planning(map, player_name, planner_name, recordings_dir, timeout=100, SHOW_FINAL_ONLY=True, WIPE_VIZ=False)
    # result = run_single_planning(map, player_name, planner_name, recordings_dir, timeout=100, USE_INTERFACE=False)

    # print(result)


def test_player_types(map='24by24_6.csv', planner_name='ma*',
                      recordings_dir=join('recordings', 'test_player_types_ma*')):

    map = 'test3.csv'
    # map = '48by89_easy.csv'

    player_types = [k for k in player.players if 'ma*' in k]

    for player_name in player_types:
        print(f'player {player_name}')
        run_single_planning(map, player_name, planner_name, recordings_dir,
                            verbose=False, timeout=100, WIPE_VIZ=True, MAGNIFY=True, SHOW_FINAL_ONLY=True)  #, MAGNIFY=True


def test_player_params(map='48by89.csv', planner_name='ha*',
                       recordings_dir=join('recordings', 'test_player_types_params')):
    import player

    player_type = 'with_dog_yellow'
    player_types = ASIST_settings.get_player_types(player.players[player_type], player_type, CHECK=False)

    for player_name in player_types:
        if player_name != player_type:
            print(f'player {player_name}')
            run_single_planning(map, player_type, planner_name, recordings_dir, player_types_name=player_name,
                                verbose=False, timeout=100)  # , SHOW_FINAL_ONLY=True)  #, MAGNIFY=True


if __name__ == '__main__':
    # -------------------------------------------------------------------
    #  Planning tests
    # -------------------------------------------------------------------

    test_planning()
    # test_planning(map='6by6_3_Z.csv', player_name='tester', planner_name='uct')
    # test_planning_methods(just_plot=False, seeds=3)
    # test_planning_parameters(planner_name='pi', just_plot=True, seeds=3)
    # test_player_types()
    # test_player_params()
