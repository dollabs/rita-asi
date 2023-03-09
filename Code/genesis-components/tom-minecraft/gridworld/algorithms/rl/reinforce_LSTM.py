import sys
import torch
# import gym
import numpy as np
import torch.nn as nn 
import torch.optim as optim
import torch.nn.functional as F
from torch.autograd import Variable 
import matplotlib.pyplot as plt 
from torch.distributions import Categorical
from os.path import join

import mdp
import visualize
import player
import os
import argparse

GAMMA = 0.99

parser = argparse.ArgumentParser(description='Parse arguments for the code.')

parser.add_argument('-n', '--player_name', type=str,
    default='systematic', help='Player type')
parser.add_argument('-m', '--map', type=str,
    default='6by6_6_T.csv', help='Filname of map')
   
args = parser.parse_args()

# device = torch.device('cuda:0' if torch.cuda.is_available() else "cpu")
device = torch.device("cpu")

class PolicyNetwork(nn.Module):

    def __init__(self, nodes, lr=1.25):
        super(PolicyNetwork,self).__init__()

        self.nodes = nodes
        self.n_acts = nodes[-1]
        
        # Define the policy network layers here
        self.linears = [] 
        for i in range(len(nodes))[:-1]: 
            self.linears.append(nn.Linear(nodes[i],nodes[i+1]))
        self.linears = nn.ModuleList(self.linears)
        # Add dropout as a form of stochasticity here
        self.dropout = nn.Dropout(p=0.45)

        # Add weight decay to ADAM
        self.optimizer = optim.Adam(self.parameters(), 
                                    lr=lr, 
                                    weight_decay=0.0000)

    def forward(self,state):
        out = state
        for i in range(len(self.nodes) - 2):
            out = F.relu(self.linears[i](out)) # pass through layer 1
        out = self.dropout(out)
        out = F.softmax(self.linears[-1](out),dim=1) # pass through layer 2
        return out

    def get_action(self, state, env):

        DEBUG = False

        # Get the original state and convert to tensor
        state_orig = state
        state = np.array(state)
        state = torch.from_numpy(state).float().unsqueeze(0).to(device) 

        # Get the probability vector over actions
        out = self.forward(Variable(state)) # multinomial probability over actions
        prob_vector = np.squeeze(out.detach().cpu().numpy())

        # Sample an action from this distribution
        action = np.random.choice(self.n_acts, p=prob_vector) 
        # action = np.argmax(prob_vector)
        # Get the next state using this action
        next_state = env.T(state_orig, env.actions[action])
        # If next state is a wall resample
        if DEBUG: print(prob_vector)
        while next_state[0] == 'wall':
            if DEBUG: print(prob_vector)
            prob_vector[action] = 0 #10**(-10)
            prob_vector /= np.sum(prob_vector)
            action = np.random.choice(self.n_acts, p=prob_vector)
            # action = np.argmax(prob_vector)
            next_state = env.T(state_orig, env.actions[action])
        # Return log probability of action
        log_prob = torch.log(out.squeeze(0)[action]).to(device)
        if DEBUG: print(prob_vector)

        # m = Categorical(out)
        # action = m.sample()
        # log_prob = m.log_prob(action)

        return action, log_prob, next_state

def update_policy(policy_network,rewards,log_probs):
    dscnt_rwds = [] 

    # Calculate the discounted rewards
    for t in range(len(rewards)):
        Gt = 0 
        pw = 0
        for r in rewards[t:]:
            Gt = Gt + GAMMA**pw * r
            pw = pw + 1
        dscnt_rwds.append(Gt)

    dscnt_rwds = torch.Tensor(dscnt_rwds).to(device)
    dscnt_rwds = (dscnt_rwds - dscnt_rwds.mean()) / (dscnt_rwds.std() + 1e-9)

    # Calculate the loss here
    policy_loss = [-log_prob*Gt for log_prob, Gt in zip(log_probs,dscnt_rwds) ]

    # Perform optimization here
    policy_network.optimizer.zero_grad()
    policy_loss = torch.stack(policy_loss).sum().to(device)
    policy_loss.backward()
    policy_network.optimizer.step()

def train(continue_train=False,
        continue_episodes=300,
        lr=1.25,
        max_steps=5000):

    DEBUG = False
    env = mdp.POMDP(visualize.MAP, visualize.MAP_ROOM, player.players[visualize.PLAYER_NAME]) 
    
    if continue_train:
        policy_net = PolicyNetwork(nodes=[2, 128, 64, len(env.actions)], lr=lr)
        policy_net.load_state_dict(torch.load('policy_net.pt'))
        policy_net.train()
        episodes = continue_episodes
    else:
        policy_net = PolicyNetwork(nodes=[2, 128, 64, len(env.actions)], lr=lr)
        policy_net.train()
        policy_net.to(device)
        episodes = 100

    max_steps = max_steps # time steps per episode
    accum_rewards = [] # collect reward collected at each episode

    for ep in range(episodes):
        # Get a new environment
        env = mdp.POMDP(visualize.MAP, 
                        visualize.MAP_ROOM, 
                        player.players[visualize.PLAYER_NAME]) 
        # Get the start state
        state = (env.start_tile, env.start_heading)

        log_probs = []
        rewards = []
        trace = [state]
        remaining = 6

        for step in range(max_steps):
            # Get action, corresponding probability and next state from network
            action, log_prob, new_state = policy_net.get_action(state, env) 

            # Calculate the associated reward
            action_string = env.actions[action]
            reward = env.R(state, action_string)
            # Collect rewards
            rewards.append(reward)
            
            if new_state not in trace:
                if DEBUG: print(new_state, action, log_prob, reward)
                trace.append(new_state)

            # Collect log probabilities
            log_probs.append(log_prob) 
            
            # After collecting a whole trahectory update the policy 
            if step == max_steps or remaining == 0: # terminate when time steps run out
                update_policy(policy_net,rewards,log_probs)
                accum_rewards.append(sum(rewards))
                break

            state = new_state

        print("episode: {}, total reward: {}, length: {}".format(
            ep, 
            np.round(sum(rewards), 3),  
            step))

    torch.save(policy_net.state_dict(), 'policy_net.pt')

def generate_trajectory(checkpoint='policy_net.pt', 
                        max_steps=5000):

    
    policy_net = PolicyNetwork(nodes=[2, 128, 64, 3], lr=1.25)
    if os.path.exists('policy_net.pt'):
        policy_net.load_state_dict(torch.load(checkpoint))
    policy_net.train()
    policy_net.to(device)

    max_steps = max_steps # time steps per episode

    # Get a new environment
    env = mdp.POMDP(visualize.MAP, 
                    visualize.MAP_ROOM, 
                    player.players[visualize.PLAYER_NAME]) 
    # Get the start state
    state = (env.start_tile, env.start_heading)

    rewards = []
    trace = []
    log_probs = []
    remaining = 6

    for step in range(max_steps):
        # Get action, corresponding probability and next state from network
        action, log_prob, new_state = policy_net.get_action(state, env) 
        log_probs.append(log_prob)

        # Calculate the associated reward
        action_string = env.actions[action]
        reward = env.R(state, action_string)
        # if reward > 1:
            # remaining -= 1
        
        # Collect rewards
        rewards.append(reward)
        # Update trace
        trace.append((state, action_string))

        # After collecting a whole trahectory update the policy 
        if step == max_steps or remaining == 0: # terminate when time steps run out
            update_policy(policy_net, rewards, log_probs)
            break

        state = new_state

    total_reward = sum(rewards)
    print("total reward: {}, length: {}".format(np.round(total_reward, 3),  
                                                step))

    torch.save(policy_net.state_dict(), 'policy_net.pt')
    
    return (total_reward, trace) 

if __name__ == '__main__':
    visualize.PLAYER_NAME = args.player_name
    visualize.MAP = args.map
    visualize.READ_ROOMS = False
    visualize.PLANNING = False
    visualize.HIERARCHICAL_PLANNING = False
    visualize.LEARNING = True
    train(continue_train=False)

# Parameters
# 6by6_3_Z.csv
# lr = 1.25, dropout = 0.3
# 24