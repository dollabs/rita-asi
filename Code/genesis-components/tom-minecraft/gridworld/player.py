players = {

	###############################################
	##     for 24by24_6
	###############################################

	## the basic player type rewarded by victims and doors
	"systematic": {
		"rewards": {
			"wall": -1000,
			"fire": -0.2,
			"air": 0,
			"gravel": 0.2,
			"door": 0.2,
			"victim": 1,
			"victim-yellow": 1
		},
		"exploration_reward": 0.025,
		"epsilon": 1e-2,
		"gamma": 0.99,
		"prior_belief": "BELIEF_ONE_EACH_ROOM",
		"planning_algo": "value_iteration"
	},

	"lrtdp": {
		"costs": {
			"go_straight": 0.05,
			"turn_left": 0.05,
			"turn_right": 0.05,
			"triage": 0.2
		},
		'gamma': 0.95,
		"prior_belief": "BELIEF_UNIFORM_IN_ROOM"
	},

	"uct": {
		"costs": {
			"go_straight": 0.05,
			"turn_left": 0.05,
			"turn_right": 0.05,
			"triage": 0.2
		},
		'gamma': 0.95,
		"prior_belief": "BELIEF_UNIFORM_IN_ROOM"
	},

	"ra*": {
		"costs": {
			"go_straight": 0.04,
			"turn_left": 0.05,
			"turn_right": 0.05,
			"triage": 0.2
		},
		'gamma': 0.9,
		"prior_belief": "BELIEF_ONE_EACH_ROOM"
	},

	"uct-13": {
		"rewards": {
			"wall": -1000,
			"fire": -0.2,
			"air": 0,
			"door": 0,
			"victim": 10,
			"victim-yellow": 30
		},
		"costs": {
			"go_straight": 0.05,
			"turn_left": 0.1,
			"turn_right": 0.1,
			"triage": 0.2
		},
		'beta': 1.5,
		'temperature': 0.01,
		'tilelevel_gamma': 0.8,
		'certainty_boost_factor': 1,
		'exploration_reward': 0,
		'information_reward': 0, ##0.3,
		'observation_reward': 0.01,
		"prior_belief": "BELIEF_ONE_EACH_ROOM",
		# 'dog': True
	},

	"uct-24": {
		"rewards": {
			"wall": -1000,
			"fire": -0.2,
			"air": 0,
			"door": 0,
			"victim": 10,
			"victim-yellow": 30
		},
		"costs": {
			"go_straight": 0.2,
			"turn_left": 0.5,
			"turn_right": 0.5,
			"triage": 0.2,
			"gravel": 5
		},
		# 'countdown': 60,
		'timeout': 2,
		'beta': 3,
		'horizon': 9,
		'temperature': 0.01,
		'gamma': 0.7,
		"prior_belief": "BELIEF_ONE_EACH_ROOM",
		# 'dog': True
	},

	"vi": {
		"rewards": {
			"door": 0,
			"victim": 1,
			"victim-yellow": 1
		},
		"costs": {
			"go_straight": 0.05,
			"turn_left": 0.05,
			"turn_right": 0.05,
			"triage": 0.2
		},
		'gamma': 0.95,
		"prior_belief": "BELIEF_UNIFORM_IN_ROOM"
	},

	"hvi": {
		"rewards": {
			"wall": -1000,
			"fire": -0.2,
			"air": 0,
			"door": 0.2,
			"victim": 1,
			"victim-yellow": 1
		},
		"costs": {
			"go_straight": 0,
			"turn_left": 0.005,
			"turn_right": 0.005,
			"triage": 0.2
		},
		'temperature': 0.01,
		'tilelevel_gamma': 0.95,
		'certainty_boost_factor': 20,
		'exploration_reward': 0,
		'information_reward': 0.05,
		'observation_reward': 0.1,
		"prior_belief": "BELIEF_UNIFORM_IN_ROOM"
	},

	"tester": {
		"rewards": {
			"door": 0,
			"victim": 1,
			"victim-yellow": 1
		},
		"costs": {
			"go_straight": 0.01,
			"turn_left": 0.05,
			"turn_right": 0.05,
			"triage": 0.2
		},
		'temperature': 0.01,
		'gamma': 0.9,
		'information_reward': 0.05,
		"prior_belief": "BELIEF_UNIFORM_IN_ROOM"
	},

	# "tester": {
	# 	"rewards": {
	# 		"wall": -1000,
	# 		"fire": -0.2,
	# 		"air": 0,
	# 		"door": 0.2,
	# 		"victim": 1,
	# 		"victim-yellow": 1
	# 	},
	# 	"costs": {
	# 		"go_straight": 0.05,
	# 		"turn_left": 0.05,
	# 		"turn_right": 0.05,
	# 		"triage": 0.2
	# 	},
	# 	'temperature': 0.01,
	# 	'tilelevel_gamma': 0.95,
	# 	'certainty_boost_factor': 20,
	# 	'exploration_reward': 0,
	# 	'information_reward': 0.05,
	# 	'observation_reward': 0.1,
	# 	"prior_belief": "BELIEF_UNIFORM_IN_ROOM"
	# },

	###############################################
	##     for DARPA dry run - 46by45_2.csv
	###############################################

	"both": {
		"rewards": {
			"victim": 10,
			"victim-yellow": 30
		},
	},
	"green": {
		"rewards": {
			"victim": 10,
			"victim-yellow": 0
		},
	},
	"yellow": {
		"rewards": {
			"victim": 0,
			"victim-yellow": 30
		},
	},

	## DARPA condition 1
	"with_dog_both": {
		"rewards": {
			"victim": 10,
			"victim-yellow": 30
		},
		"dog": True,
	},

	## DARPA strategy 1
	"with_dog_green": {
		"rewards": {
			"victim": 10,
			"victim-yellow": 0
		},
		"dog": True,
	},

	## DARPA strategy 3
	"with_dog_yellow": {
		"rewards": {
			"victim": 0,
			"victim-yellow": 30
		},
		"dog": True,
		# 'prior_belief': 'BELIEF_ONE_EACH_ROOM'
	},

	# ###############################################
	# ##     for DARPA fitted to the big map
	# ###############################################
	#
	# "both": {
	# 	"rewards": {
	# 		"wall": -1000,
	# 		"air": 0,
	# 		"door": 0.2,
	# 		"victim": 1,
	# 		"victim-yellow": 1
	# 	},
	# 	'temperature': 0.01, #0.0166828,
	# 	'tilelevel_gamma': 0.95,
	# 	'certainty_boost_factor': 20,
	# 	'exploration_reward': 0,
	# 	'information_reward': 0.05,
	# 	'observation_reward': 0.003,
	# 	"prior_belief": "BELIEF_UNIFORM_IN_ROOM"
	# },
	# "green": {
	# 	"rewards": {
	# 		"wall": -1000,
	# 		"air": 0,
	# 		"door": 0.2,
	# 		"victim": 1,
	# 		"victim-yellow": -0.2
	# 	},
	# 	'temperature': 0.01,
	# 	'tilelevel_gamma': 0.97,
	# 	'certainty_boost_factor': 40,
	# 	'exploration_reward': 0.01,
	# 	'information_reward': 0.05,
	# 	'observation_reward': 0.005,
	# 	"prior_belief": "BELIEF_UNIFORM_IN_ROOM"
	# },
	# "yellow": {
	# 	"rewards": {
	# 		"wall": -1000,
	# 		"air": 0,
	# 		"door": 0.2,
	# 		"victim": -0.2,
	# 		"victim-yellow": 1
	# 	},
	# 	'temperature': 0.01,
	# 	'tilelevel_gamma': 0.97,
	# 	'certainty_boost_factor': 40,
	# 	'exploration_reward': 0.01,
	# 	'information_reward': 0.05,
	# 	'observation_reward': 0.008,
	# 	"prior_belief": "BELIEF_UNIFORM_IN_ROOM"
	# },
	#
	# ## DARPA condition 1
	# "with_dog_both": {
	# 	"rewards": {
	# 		"wall": -1000,
	# 		"air": 0,
	# 		"door": 0.02,
	# 		"victim": 1,
	# 		"victim-yellow": 1
	# 	},
	# 	"dog": True,
	# 	'temperature': 0.01,
	# 	'tilelevel_gamma': 0.97,
	# 	'certainty_boost_factor': 40,
	# 	'exploration_reward': 0,
	# 	'information_reward': 0.05,
	# 	'observation_reward': 0.006,
	# 	"prior_belief": "BELIEF_UNIFORM_IN_ROOM"
	# },
	#
	# ## DARPA strategy 1
	# "with_dog_green": {
	# 	"rewards": {
	# 		"wall": -1000,
	# 		"air": 0,
	# 		"door": 0.02,
	# 		"victim": 1,
	# 		"victim-yellow": -0.2
	# 	},
	# 	"dog": True,
	# 	'temperature': 0.01,
	# 	'tilelevel_gamma': 0.97,
	# 	'certainty_boost_factor': 40,
	# 	'exploration_reward': 0.01,
	# 	'information_reward': 0.05,
	# 	'observation_reward': 0.005,
	# 	"prior_belief": "BELIEF_UNIFORM_IN_ROOM"
	# },
	#
	# ## DARPA strategy 3
	# "with_dog_yellow": {
	# 	"rewards": {
	# 		"wall": -1000,
	# 		"air": 0,
	# 		"door": 0.02,
	# 		"victim": -0.2,
	# 		"victim-yellow": 1
	# 	},
	# 	"dog": True,
	# 	'temperature': 0.01,
	# 	'tilelevel_gamma': 0.97,
	# 	'certainty_boost_factor': 40,
	# 	'exploration_reward': 0.02,
	# 	'information_reward': 0.05,
	# 	'observation_reward': 0.0005,
	# 	"prior_belief": "BELIEF_UNIFORM_IN_ROOM"
	# },

	#############################################################

	## the player can only see 2 squares around
	"difficult": {
		"rewards": {
			"wall": -1000,
			"fire": -0.2,
			"air": 0,
			"gravel": -0.2,
			"door": 0.7,
			"victim": 1,
			"victim-yellow": 1
		},
		"torch": True,
		"exploration_reward": 0.01,
		"epsilon": 1e-2,
		"gamma": 0.8,
		"prior_belief": "BELIEF_UNIFORM",
		"planning_algo": "value_iteration"
	},

	## rewarded also by putting off fire
	"fire_fighter": {
		"rewards": {
			"wall": -1000,
			"fire": 0.5,
			"air": 0,
			"gravel": -0.2,
			"door": 0.7,
			"victim": 1,
			"victim-yellow": 1
		},
		"exploration_reward": 0.1,
		"epsilon": 1e-2,
		"gamma": 0.8,
		"prior_belief": "BELIEF_UNIFORM",
		"planning_algo": "value_iteration"
	},

	###############################################
	##      22 April RL project
	###############################################

	## the most basic
	"human": {
		"rewards": {
		    "wall": -1000,
		    "fire": 0,
		    "air": 0,
		    "gravel": 0,
		    "door": 0,
		    "victim": 1,
		    "victim-yellow": 1
		},
		"exploration_reward": 0.01,
		"epsilon": None,
		"gamma": None,
		"planning_algo": None
	},
	"RL": {
		"rewards": {
		    "wall": -1000,
		    "entrance": -1000,
		    "grass": -1000,
		    "fire": -0.2,
		    "air": 0,
		    "gravel": -0.2,
		    "door": 0.2,
		    "hand-sanitizer": 1,
		    "victim": 1,
		    "victim-yellow": 1
		},
		"exploration_reward": 0.01,
		"epsilon": 1e-2,
		"gamma": 0.8,
		"planning_algo": "value_iteration"
	},

	"Q-Learner": {
		"rewards": {
		    "wall": -1000,
		    "entrance": -1000,
		    "grass": -1000,
		    "fire": 0,
		    "air": 0,
		    "gravel": 0,
		    "door": 0.5,
		    "hand-sanitizer": 1,
		    "victim": 1,
		    "victim-yellow": 1
		},
		"exploration_reward": 0,
		"epsilon": 1e-2,
		"gamma": 0.99,
		"planning_algo": "value_iteration"
	},

	## ------------ 0131
	"ma*-h12": { 'horizon': 12},
	"ma*-h9": { 'horizon': 9 },
	"ma*-h7": { 'horizon': 7},
	"ma*-h5": { 'horizon': 5},
	"ma*-h4": { 'horizon': 4},
	"ma*-h3": { 'horizon': 3},
	"ma*-h2": { 'horizon': 2},

}
