"""
## default mode is REPLAY_WITH_TOM, which replays a file and not receive/send RMQ messages
python rita_tom.py -m REPLAY_WITH_TOM -f ../../../test/data/ASIST_data_study_id_000001_condition_id_000005_trial_id_000015_messages.log
python rita_tom.py -f ../../../test/data/ASIST_data_study_id_000001_condition_id_000005_trial_id_000015_messages.log

## mode REPLAY_IN_RITA requires rabbitmq to be running, 'brew services run rabbitmq'
python rita_tom.py -m REPLAY_IN_RITA -i 1

## hide interface for debugging
python rita_tom.py -m REPLAY_IN_RITA -i 0
"""

import argparse
import sys
import replayer

## python visualize.py -m REPLAY_IN_RITA
parser = argparse.ArgumentParser(description='Parse arguments for the code.')
parser.add_argument('-m', '--mode', type=str, default='REPLAY_WITH_TOM', help='Mode of code you want to test')
parser.add_argument('-i', '--interface', type=str, default='1', help='flag if interface is used')
## replay files
parser.add_argument('-f', '--file', type=str, default='', help='Relative path of log file you want to test')
parser.add_argument('-d', '--directory', type=str, default='', help='Relative path of directory of log files that you want to test')
## listen to RMQ messages
parser.add_argument('-t', '--host', type=str, default='localhost', help='RabbitMQ host, if not localhost')
parser.add_argument('-p', '--port', type=str, default='5672', help='RabbitMQ port, if not 5672')

args = parser.parse_args()

if __name__ == '__main__':

    USE_INTERFACE = True
    if int(args.interface) == 0:
        USE_INTERFACE = False

    if args.mode == 'REPLAY_IN_RITA':
        print('Choosing the mode of listening to rabbitmq messages')
        replayer = replayer.ReplayerRMQ(host=args.host, port=args.port, USE_INTERFACE=USE_INTERFACE)

     ## old Hackathon data through RMQ
    elif args.mode == 'REPLAY_WITH_TOM':
        if args.directory != '':
            print('Choosing the mode of replaying all files in a directory: ', args.directory)
            replayer = replayer.ReplayerRITA(dir=args.directory, USE_INTERFACE=USE_INTERFACE)

        elif args.file != '':
            print('Choosing the mode of replaying one file: ')
            replayer = replayer.ReplayerRITA(file=args.file, USE_INTERFACE=USE_INTERFACE)

        else:
            print('WARNING: please specify a file (-f) or a directory (-d) to replay. Quiting now ...')
            sys.exit()
    else:
        f'WARNING: unspecified mode'
        sys.exit()

    replayer.RITA_ANALYZE_MACROS = False
    replayer.run()
