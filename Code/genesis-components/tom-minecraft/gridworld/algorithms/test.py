import visualize


def test_planning(MAP='6by6_3_Z.csv', PLAYER='6by6_rooms.csv'):
	visualize.PLANNING = True
	visualize.INVERSE_PLANNING = False
	visualize.LEARNING = False

	visualize.WORLD_WIDTH = 6
	visualize.WORLD_HEIGHT = 6
	visualize.MAP = MAP
	visualize.MAP_ROOM = MAP_ROOM
	visualize.TILE_SIZE = 60
	visualize.READ_ROOMS = True
	visualize.MAX_ITER = 100

def experiment_replay():

	READ_ROOMS = False
	MAX_ITER = 1000

	WORLD_WIDTH = 24
	WORLD_HEIGHT = 24
	MAP = '6by6_3_Z.csv'
	MAP_ROOM = '6by6_3_rooms.csv'
	TILE_SIZE = 16

## ----------------------------------------
#    Main function for different experiments
## ----------------------------------------
if __name__ == '__main__':

	test_planning(MAP='6by6_3_Z.csv', PLAYER='6by6_rooms.csv')