from mcts import MonteCarloAgent
from game import GridWorld
import copy

if __name__ == "__main__":
    env = GridWorld()

    print('from',env._pos_agent, 'to',env._pos_goal)
    _map = copy.deepcopy(env._map)
    _map[env._pos_agent[0],env._pos_agent[1]] = '5'
    print(_map)
    print()

    agent = MonteCarloAgent(env)
    reward = 0
    count = 0
    actions = {0:'up', 1:'down', 2:'left', 3:'right'}
    while reward == 0:
        count += 1
        action = agent.get_action()
        reward = env.act(action)
        _map = copy.deepcopy(env._map)
        _map[env._pos_agent[0],env._pos_agent[1]] = '5'
        print('step',count,actions[action],'to',env._pos_agent)
        print(_map)
        print()
