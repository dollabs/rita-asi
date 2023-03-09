"""
Loosely based on https://github.com/kudkudak/python-ai-samples/blob/master/AI-UCTAlgorithm/UCT.py
Read http://mcts.ai/about/index.html for a good description of MCTS
"""

import datetime
import copy
import math
from collections import defaultdict
from random import choice, shuffle
import networkx as nx
from networkx import DiGraph
import matplotlib.pyplot as plt
import time
import random

import mdp
import visualize
from tqdm import tqdm

class PrintDiGraph(DiGraph):
    """ for printing the MC search tree """

    def __init__(self, data=None, name='', file=None, **attr):
        DiGraph.__init__(self, data=data, name=name, **attr)
        self.write = False
        self.labels = {}
        self.dist = {}
        if file is None:
            import sys
            self.fh = sys.stdout
        else:
            self.fh = open(file, 'w')

    def add_node(self, n, attr_dict=None, **attr):
        DiGraph.add_node(self, n, attr_dict=attr_dict, **attr)
        self.labels[n] = n
        self.dist[n] = {}
        if self.write: self.fh.write("Add node: %s\n" % n)

    def add_nodes_from(self, nodes, **attr):
        for n in nodes:
            self.add_node(n, **attr)

    def remove_node(self, n):
        DiGraph.remove_node(self, n)
        if self.write: self.fh.write("Remove node: %s\n" % n)

    def remove_nodes_from(self, nodes):
        for n in nodes:
            self.remove_node(n)

    def add_edge(self, u, v, attr_dict=None, **attr):
        if v not in self.nodes:
            self.add_node(v, **attr)

        self.dist[u][v] = 1
        for e in self.edges():
            if e[0] == u and e[1] != v:
                self.dist[e[1]][v] = 1

        DiGraph.add_edge(self, u, v, attr_dict=attr_dict, **attr)
        if self.write: self.fh.write("Add edge: %s->%s\n" % (u, v))

    def add_edges_from(self, ebunch, attr_dict=None, **attr):
        for e in ebunch:
            u, v = e[0:2]
            self.add_edge(u, v, attr_dict=attr_dict, **attr)

    def remove_edge(self, u, v):
        DiGraph.remove_edge(self, u, v)
        if self.write: self.fh.write("Remove edge: %s-%s\n" % (u, v))

    def remove_edges_from(self, ebunch):
        for e in ebunch:
            u, v = e[0:2]
            self.remove_edge(u, v)

    def clear(self):
        DiGraph.clear(self)
        if self.write: self.fh.write("Clear graph\n")

    def generate_tree(self, root, bect_child_node, Nodes, edge_labels):
        start_graph = time.time()
        fig, ax1 = plt.subplots()
        plt.box(False)
        plt.sca(ax1)
        ax1.margins(x=0.2, y=0.1)
        # ax1.set_title('Monte Carlo Search Tree',fontsize=12)

        pos = nx.kamada_kawai_layout(self, scale=4) #,dist=self.dist
        for node in self.labels.keys():
            self.labels[node] = str(self.labels[node])+'='+str(round(Nodes[node].getExpectedValue(),2))

        nx.draw_networkx_nodes(self, pos, node_color='#F2F09E', node_size=200, alpha=1)
        nx.draw_networkx_nodes(self, pos, node_color='#1abc9c', node_size=200, nodelist=[root.state],alpha=1)
        nx.draw_networkx_edges(self, pos, edge_color='#F2F09E', arrowstyle='->', arrowsize=18, width=2, alpha=1)
        nx.draw_networkx_edges(self, pos, edge_color='#f09289', edgelist=[(root.state,bect_child_node.state)],arrowstyle='->', arrowsize=18, width=2, alpha=1)
        nx.draw_networkx_edge_labels(self, pos, edge_labels, font_size=8)
        nx.draw_networkx_labels(self, pos, labels=self.labels, font_size=8)
        fig.set_size_inches(4, 2.2)
        plt.savefig("plots/mctree.png", bbox_inches='tight')
        plt.close()
        # print('... finished generating graph tree',str(time.time() - start_graph), 'seconds')
        # ------------------------------


class UCBPolicy(object):
    """ for selecting the best child node """

    C = 0.05 #math.sqrt(2)/2
    """ Class for best child policy based on UCB bound v"""
    def __init__(self, C = math.sqrt(2)):
        self.C = 1 / (2 * math.sqrt(2))  # mozna lepiej rozwiazac
        UCBPolicy.C = self.C

    def setParams(self, C):
        self.C = C

    @staticmethod
    def getScore(n):
        """ Returns UCB1 score for node (not root) """
        return n.getExpectedValue() + UCBPolicy.C*math.sqrt(2*math.log(n.parent.N)/n.N)

    def bestChild(self, node):
        """
            UCT Method: Assumes that all childs are expanded
            Implements given policy
        """
        L = [n.getExpectedValue() + self.C*math.sqrt(2*math.log(node.N)/n.N) for n in node.children]
        return node.children[L.index(max(L))]

class Node:
    """ for building the search tree """
    def __init__(self, env, action=None, parent_agent=None):
        self.env = env
        self.state = env._pos_agent
        self.parent_agent = parent_agent
        self.action = action
        self.actions_remaining = env.available_actions()
        shuffle(self.actions_remaining)
        self.N = 0
        self.Q = 0.0
        self.parent = None
        self.children = []

    def getExpectedValue(self):
        """ returns expected value, if transposition option is on uses dict """
        return self.Q / (float(self.N) + 1)

    def isFullyExpanded(self):
        return len(self.actions_remaining) == 0

def copyenv(env):
    """ each simulation starts with a new copy of the original env """
    copy_env = mdp.POMDP(env.MAP, env.MAP_ROOM, env.player)
    copy_env._pos_agent = env._pos_agent
    copy_env.tilesummary = copy.deepcopy(env.tilesummary)
    return copy_env

class MonteCarloAgent(object):
    """ input: the environment
        function: get the best child after simulations
    """

    def __init__(self, env, best_child_policy=UCBPolicy, **kwargs):

        ## default value
        self._runtime = datetime.timedelta(seconds=kwargs.get('runtime', visualize.MCTS_RUNTIME))
        self._max_depth = kwargs.get('max_depth', visualize.MCTS_DEPTH)
        self._gamma = kwargs.get('gamma', 0.8)

        ## tree set up for UCB algorithm, an variant of MCTS
        self._env = env
        self.Nodes = {}
        self.bestChild = best_child_policy().bestChild

        ## for visualization
        self.G = PrintDiGraph()
        self.edge_labels = {}

    def get_action(self):
        """ Return the best move from the current game state using UCT algorithm """

        start = time.time()
        copy_env = copyenv(self._env)
        root = Node(copy_env, parent_agent=self)
        self.Nodes[root.state] = root
        self.G.add_node(root.state)

        # ------------------------------
        # --- UCB algorithm, an variant of MCTS
        # ------------------------------

        ## start select-expand-simulate-backup process until running out of computational budget
        begin = datetime.datetime.utcnow()
        count = 0
        while datetime.datetime.utcnow() - begin < self._runtime:
            count += 1
            leaf = self.select_leaf(root)
            q = self.simulate(leaf)
            self.backup(leaf, q)
            # print('         ///////', leaf.state, leaf.action, q)

            # print('simulate!!!!!',leaf.action, leaf.env._pos_agent, leaf.actions_remaining, leaf.env.tilesummary[18]['reward']) #,leaf.env.tilesummary[19]['reward'],leaf.env.tilesummary[12]['reward'],leaf.env.tilesummary[13]['reward'],leaf.env.tilesummary[14]['reward'])

        ## after all simulations, find the best children of root node (current state)
        bect_child_node = None
        bect_child_value = -10
        L = [n.getExpectedValue() for n in root.children]
        V = {}
        for n in root.children:
            V[n.action] = n.getExpectedValue()
            if V[n.action] > bect_child_value:
                bect_child_value = V[n.action]
                bect_child_node = n
        # print(V)

        ## generate a gif of the search tree for debugging
        self.G.generate_tree(root, bect_child_node, self.Nodes, self.edge_labels)

        pi = {}
        action = root.children[L.index(max(L))].action
        pi[copy_env._pos_agent] = [(action,V[action])]

        print('finished MCTS in', round(time.time() - start, 3), 'seconds')
        return pi

    def pick_move_policy(self, env):
        # Policy for simulation, currently set at random
        return choice(env.available_actions())

    def simulate(self, node):
        """Simulate from the current node"""
        copy_env = copyenv(node.env) # copy.deepcopy(node.env)
        simulation_depth = 0
        rewards = []
        trace = []
        while simulation_depth <= self._max_depth: #and not copy_env.inTerminalState():
            before = copy_env._pos_agent
            action = self.pick_move_policy(copy_env)
            pos = copy_env._pos_agent
            reward = copy_env.act(action)
            # self.G.add_edge(pos,copy_env._pos_agent)
            trace.append([before,action,copy_env._pos_agent,reward])
            rewards.append(reward)
            simulation_depth += 1

        discounted_rewards = [(self._gamma ** i) * r for i, r in zip(range(len(rewards)), rewards)]
        # print(sum(discounted_rewards), discounted_rewards, trace)
        return sum(discounted_rewards)

    def select_leaf(self, node):
        """
        Find the leaf to expand
        :param node: node to start from
        :return: child leaf or terminal node
        """
        # while not node.isTerminal():
        if not node.isFullyExpanded():
            return self.expand(node)
        else:
            return self.bestChild(node)

    def expand(self, node):
        actions = {'go_straight':'go','turn_left':'left','turn_right':'right'}
        """
        Expands node by one random step.
        :param node: Node to expand
        :return: child node
        """
        action = node.actions_remaining.pop()
        # new_env = copy.deepcopy(node.env)
        new_env = copyenv(node.env)
        new_env.act(action)
        child_node = Node(new_env, action=action, parent_agent=self)
        self.Nodes[child_node.state] = child_node
        self.G.add_edge(node.state,child_node.state)
        if isinstance(action, str):     ## when action is on the tile level
            action = actions[action]
        self.edge_labels[(node.state,child_node.state)] = action
        child_node.parent = node
        node.children.append(child_node)
        return child_node

    def backup(self, node, q):
        """
        Backup and add all the new q values to the nodes, from the leaf upwards.
        :param node:
        :param q:
        :return:
        """
        while node != None:
            node.Q += q
            node.N += 1
            node = node.parent


def plan(env,agent,dc, screen, ts, s0, TXT_name):
    """ play the game using Monte Carlo Tree Search """
    mcagent = MonteCarloAgent(env)
    s = s0
    trace = [s[0]]
    ep = []  # collect episode

    visualize.update_maze(env, agent, dc, screen, ts, s, trace, real_pos=env.tilesummary[s[0]]['pos'])

    for iter in tqdm(range(visualize.MAX_ITER)):

        if env.replan:
            pi = mcagent.get_action()
        a = random.choice(pi[s])[0]
        if env.check_turned_too_much(a): break
        ep.append((s, a))
        sa_last = env.tilesummary[s[0]]['pos']
        s = env.T(s, a)

        visualize.update_maze(env, agent, dc, screen, ts, s, trace, real_pos=env.tilesummary[s[0]]['pos'], sa_last=sa_last)
        visualize.update_graph("plots/mctree.png", screen, -200)

        env.collect_reward()
        if env.check_mission_finish(): break

    return ep