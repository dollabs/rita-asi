# For the new maze with spinning

import sys
import torch
import numpy as np
import torch.nn as nn 
import torch.optim as optim
import torch.nn.functional as F
from torch.autograd import Variable 
import matplotlib.pyplot as plt 
from torch.distributions import Categorical

import mdp
import visualize
import player
import os
import argparse
import json

DISCOUNT_RATE = 0.99
LEARNING_RATE = 0.5
POLICY_NODES = [2, 128, 64, 4]
VALUE_NODES = [2, 128, 64, 1]
DROPOUT_RATE = 0.1
WEIGHT_DECAY = 0.0005
MAX_STEPS = 5000

parser = argparse.ArgumentParser(description='Parse arguments for the code.')

parser.add_argument('-n', '--player_name', type=str,
    default='systematic', help='Player type')
parser.add_argument('-m', '--map', type=str,
    default='6by6_6_T.csv', help='Filname of map')
   
args = parser.parse_args()

# device = torch.device('cuda:0' if torch.cuda.is_available() else "cpu")
device = torch.device("cpu")

def available_actions(env, s):
    actions = [] # 'up', 'down', 'left', 'right'

    ## delete actions that go into walls
    for a in env.actions:
        if not env.T(s, a) == 'wall':
            actions.append(1) 
        else:
            actions.append(0)
    return np.array(actions)

class ActorCriticNetwork(nn.Module):

    def __init__(self, 
                policy_nodes=POLICY_NODES, 
                value_nodes=VALUE_NODES, 
                lr=LEARNING_RATE):
        super(ActorCriticNetwork,self).__init__()

        self.policy_nodes = policy_nodes
        self.value_nodes = value_nodes
        self.n_acts = self.policy_nodes[-1]

        # Define policy network here       
        self.policy_linears = [] 
        for i in range(len(self.policy_nodes))[:-1]: 
            self.policy_linears.append(nn.Linear(self.policy_nodes[i], self.policy_nodes[i+1]))
        self.policy_linears = nn.ModuleList(self.policy_linears)
        # Add dropout to introduce stochasticity
        # TODO: Currently 0.3 is a sweet spot but might want to make this a parameter
        self.dropout = nn.Dropout(p=DROPOUT_RATE)

        # Define value network here
        self.value_linears = [] 
        for i in range(len(self.value_nodes))[:-1]: 
            self.value_linears.append(nn.Linear(self.value_nodes[i], self.value_nodes[i+1]))
        self.value_linears = nn.ModuleList(self.value_linears)

        # Define the optimizer
        self.optimizer = optim.Adam(self.parameters(), 
                                    lr=lr, 
                                    weight_decay=WEIGHT_DECAY)

    def forward(self, state):
        out = state

        # Forward prop through policy network
        for i in range(len(self.policy_nodes) - 2):
            out = F.relu(self.policy_linears[i](out)) 
        out = self.dropout(out)
        policy_prob = F.softmax(self.policy_linears[-1](out),dim=1) 
        
        value = state
        # Forward prop through value network
        for i in range(len(self.policy_nodes) - 2):
            value = F.relu(self.policy_linears[i](value)) 
        value = self.value_linears[-1](value)

        return (policy_prob, value)

    def get_action(self, state, env, epsilon=0.1):

        # Flag to denote if a valid action was chosen
        flag = 'valid'

        # Convert the original state to a tensor
        state_orig = state
        state = torch.from_numpy(np.array([env.tilesummary[state]['row'],
                                env.tilesummary[state]['col']])).float().unsqueeze(0).to(device) # numpy list to torch.tensor
        
        # Get the policy probability vector and value function
        (p, value) = self.forward(Variable(state)) # multinomial probability over actions
        prob_vector = np.squeeze(p.detach().cpu().numpy())

        # option 1
        # # epsilon-greedy policy here
        # if np.random.rand() <= epsilon:
        #     action = np.random.choice(len(env.actions))
        # else:
        #     action = np.random.choice(len(env.actions), p=prob_vector)

        # next_state = env.T(state_orig, env.actions[action])
        # # If the next state is a wall, action is invalid
        # if next_state == 'wall':
        #     flag = 'invalid'
        #     next_state = state_orig
        # # Convert the probability to log probability

        # option 2
        # masked_prob = prob_vector * available_actions(env, state_orig)
        # masked_prob /= np.sum(masked_prob)
        # action = np.random.choice(len(env.actions), p=masked_prob)
        # log_prob = torch.log(p.squeeze(0)[action]).to(device)
        # next_state = env.T(state_orig, env.actions[action])

        return action, log_prob, value, next_state, flag

def update_policy(network, rewards, log_probs, values):
    dscnt_rwds = [] 

    # Calculate the doscounted rewards
    for t in range(len(rewards)):
        Gt = 0 
        pw = 0
        for r in rewards[t:]:
            Gt = Gt + DISCOUNT_RATE**pw * r
            pw = pw + 1
        dscnt_rwds.append(Gt)

    dscnt_rwds = torch.Tensor(dscnt_rwds).to(device)
    dscnt_rwds = (dscnt_rwds - dscnt_rwds.mean()) / (dscnt_rwds.std() + 1e-9) # normalize discounted rewards

    # Calculate the policy loss, i.e., policy gradient
    policy_loss = [-log_prob * (Gt - value) for log_prob, value, Gt in zip(log_probs, values, dscnt_rwds) ]
    # Calculate the value loss which is the derivate of the L1 or L2 loss
    value_loss = [F.smooth_l1_loss(value, Gt) for value, Gt in zip(values, dscnt_rwds)]

    # Perform the optimization here
    network.optimizer.zero_grad()
    policy_gradient = torch.stack(policy_loss).sum() + torch.stack(value_loss).sum()
    policy_gradient.backward()
    network.optimizer.step()

def train(continue_train=False, 
        continue_episodes=300,
        lr=LEARNING_RATE,
        max_steps=MAX_STEPS):

    DEBUG = False
    env = mdp.POMDP(visualize.MAP, visualize.MAP_ROOM, player.players[visualize.PLAYER_NAME]) 
    # env.print_tile_summary()
    
    if continue_train:
        net = ActorCriticNetwork(policy_nodes=POLICY_NODES, lr=lr)
        net.load_state_dict(torch.load('actor_critic_net.pt'))
        net.train()
        net.to(device)
        episodes = continue_episodes
    else:
        net = ActorCriticNetwork(policy_nodes=POLICY_NODES, lr=lr)
        net.train()
        net.to(device)
        episodes = 100

    max_steps = max_steps # time steps per episode
    accum_rewards = [] # collect reward collected at each episode

    for ep in range(episodes):
        # Get a new environment
        env = mdp.POMDP(visualize.MAP, 
                        visualize.MAP_ROOM, 
                        player.players[visualize.PLAYER_NAME]) 
        # Initialize the start state
        state = (env.start_tile, env.start_heading)

        log_probs = []
        rewards = []
        values = []
        trace = [state]

        for step in range(max_steps):
            action, log_prob, value, new_state = net.get_action(state, env) 

            # Calculate the associated reward for this state and action
            action_string = env.actions[action]
            reward = env.R(state, action_string)
            rewards.append(reward)
            
            if new_state not in trace:
                if DEBUG: print(new_state, action, log_prob, reward)
                trace.append(new_state)

            # Collect log probabilites and values
            log_probs.append(log_prob) 
            values.append(value.detach())

            # Update the weights after collecting a complete trajectory
            if step == max_steps:
                update_policy(net, rewards, log_probs, values)
                accum_rewards.append(sum(rewards))
                break

            state = new_state

        print("episode: {}, total reward: {}, length: {}".format(
            ep, 
            np.round(sum(rewards), 3),  
            step))

    torch.save(net.state_dict(), 'actor_critic_net.pt')

def generate_trajectory(env, checkpoint='actor_critic_net.pt', 
                        max_steps=MAX_STEPS, 
                        epsilon=0.1):

    policy_net = ActorCriticNetwork(policy_nodes=POLICY_NODES, lr=LEARNING_RATE)
    if os.path.exists('actor_critic_net.pt'):
        policy_net.load_state_dict(torch.load(checkpoint))
    policy_net.train()
    policy_net.to(device)

    max_steps = max_steps # time steps per episode

    # Get the start state
    state = env.start_tile

    rewards = []
    trace = []
    visited = []
    log_probs = []
    values = []

    for step in range(max_steps):
        # Get action, corresponding probability and next state from network
        action, log_prob, value, new_state, flag = policy_net.get_action(state, env, epsilon=epsilon) 
        visited.append(state)

        # Calculate the associated reward
        action_string = env.actions[action]
        
        if flag == 'invalid':
            reward = -0.1
        else:
            if new_state in visited:
                reward = -0.02
                # reward = env.R(state, action_string)
            else:
                reward = env.R(state, action_string)
            log_probs.append(log_prob)
            values.append(value)
            # Collect rewards
            rewards.append(reward)
        # Update trace
        trace.append((state, action_string))

        if step == max_steps:
            update_policy(policy_net, rewards, log_probs, values)
            break

        state = new_state

    torch.save(policy_net.state_dict(), 'actor_critic_net.pt')

    # print("total reward: {}, length: {}".format(np.round(sum(rewards), 3),  step))   
    return (sum(rewards), trace) 

if __name__ == '__main__':
    visualize.PLAYER_NAME = args.player_name
    visualize.MAP = args.map
    visualize.READ_ROOMS = False
    visualize.PLANNING = False
    visualize.HIERARCHICAL_PLANNING = False
    visualize.LEARNING = True
    visualize.INVERSE_PLANNING = False
    train(continue_train=False, max_steps=500)

# Parameters
# 6by6_3_Z.csv
# lr = 1.25, dropout = 0.3
# 24