from collections import defaultdict
import numpy as np
from tqdm import tqdm
import time

import mdp
import visualize

class UCT:
    """Implementation of UCT based on Leslie's lecture notes
    """
    def __init__(self, actions, reward_fn, transition_fn, done_fn=None, num_search_iters=1000, gamma=0.9, seed=0):
        self._actions = actions
        self._reward_fn = reward_fn
        self._transition_fn = transition_fn
        self._done_fn = done_fn or (lambda s,a : False)
        self._num_search_iters = num_search_iters
        self._gamma = gamma
        self._rng = np.random.RandomState(seed)
        self._Q = None
        self._N = None
        self._beta = 1  ## coeff of UCT exploration term

    def update_param(self, num_search_iters, gamma, beta):
        self._num_search_iters = num_search_iters
        self._gamma = gamma
        self._beta = beta

    def run(self, state, horizon=100):
        # Initialize Q[s][a][d] -> float
        self._Q = defaultdict(lambda : defaultdict(lambda : defaultdict(float)))
        # Initialize N[s][a][d] -> int
        self._N = defaultdict(lambda : defaultdict(lambda : defaultdict(int)))
        # Loop search
        for it in range(self._num_search_iters):
            # Update Q
            self._search(state, 0, horizon=horizon)

    def get_action(self, state, t=0):
        # Return best action, break ties randomly
        action = max(self._actions, key=lambda a : (self._Q[state][a][t], self._rng.uniform()))

        to_print = '  |  '
        for a, di in self._Q[state].items():
            to_print += f'{a}: {str(round(di[t],3))}  | '
        print(state, action, t, to_print)

        return action

    def _search(self, s, depth, horizon=100):
        # Base case
        if depth == horizon:
            return 0.

        # Select an action, balancing explore/exploit
        a = self._select_action(s, depth, horizon=horizon)
        # Create a child state
        next_state = self._transition_fn(s, a)

        # Get value estimate
        # if self._done_fn(s, a):
        #     # Some environments terminate problems before the horizon
        #     q = self._reward_fn(s, a)
        # else:
        q = self._reward_fn(s, a) + self._gamma * self._search(next_state, depth+1, horizon=horizon)

        # Update values and counts
        num_visits = self._N[s][a][depth] # before now
        # First visit to (s, a, depth)
        if num_visits == 0:
            self._Q[s][a][depth] = q
        # We've been here before
        else:
            # Running average
            q_ave =  (num_visits / (num_visits + 1.)) * self._Q[s][a][depth] + \
                                   (1 / (num_visits + 1.)) * q
            q_max = max(self._Q[s][a][depth], q)
            # if abs(self._Q[s][a][depth] - q_ave) <= 10 ** -3:
            #     print('ave', num_visits)
            # if abs(self._Q[s][a][depth] - q_max) <= 10 ** -3:
            #     print('max', num_visits)
            # print((s,a), q, self._Q[s][a][depth], q_ave, q_max)
            # self._Q[s][a][depth] = (num_visits / (num_visits + 1.)) * self._Q[s][a][depth] + \
            #                        (1 / (num_visits + 1.)) * q
            self._Q[s][a][depth] = q_ave
        # Update num visits
        self._N[s][a][depth] += 1
        return self._Q[s][a][depth]

    def _select_action(self, s, depth, horizon):
        # If there is any action where N(s, a, depth) == 0, try it first
        untried_actions = [a for a in self._actions if self._N[s][a][depth] == 0]
        if len(untried_actions) > 0:
            return self._rng.choice(untried_actions)
        # Otherwise, take an action to trade off exploration and exploitation
        N_s_d = sum(self._N[s][a][depth] for a in self._actions)
        best_action_score = -np.inf
        best_actions = []
        for a in self._actions:
            explore_bonus = (np.log(N_s_d) / self._N[s][a][depth])**((horizon + depth) / (2*horizon + depth))
            score = self._Q[s][a][depth] + explore_bonus * self._beta
            if score > best_action_score:
                best_action_score = score
                best_actions = [a]
            elif score == best_action_score:
                best_actions.append(a)
        return self._rng.choice(best_actions)

def plan(env,agent,dc, screen, ts, s0, TXT_name):
    """ play the game using Monte Carlo Tree Search """
    # uct = UCT(env.actions, env.R, env.T, done_fn=env.check_done,
    #           num_search_iters=1000, gamma=0.99, seed=0)
    uct = env.initiate_uct()
    s = s0
    ep = []  # collect episode
    unobserved_in_rooms, obs_rewards, tiles_to_color, tiles_to_change = env.observe(None, s[0])
    visualize.update_maze(env, agent, dc, screen, ts, s=s, trace=env.visited_tiles,
                real_pos=env.tilesummary[s[0]]['pos'], tiles_to_color=tiles_to_color, tiles_to_change=tiles_to_change)

    for iter in tqdm(range(visualize.MAX_ITER)):

        if env.replan:
            uct.run(s, horizon=env.uct_max_num_steps)
            steps_since_replanning = 0
            env.replan = False
        action = uct.get_action(s, t=steps_since_replanning)
        steps_since_replanning += 1

        if env.check_turned_too_much(action): break
        ep.append((s, action))
        sa_last = env.tilesummary[s[0]]['pos']
        s = env.T(s, action)
        env._pos_agent = s
        # # visualize.update_graph("plots/mctree.png", screen, -200)
        env.collect_reward()
        unobserved_in_rooms, obs_rewards, tiles_to_color, tiles_to_change = env.observe(None, s[0])
        visualize.update_maze(env, agent, dc, screen, ts, s=s, trace=env.visited_tiles, real_pos=env.tilesummary[s[0]]['pos'],
                              sa_last=sa_last, tiles_to_color=tiles_to_color, tiles_to_change=tiles_to_change)
        if env.check_mission_finish(): break

    return ep