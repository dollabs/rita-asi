#!/usr/bin/env python3

# Run do_experiment.sh in parallel
# Given a list of directories, split them into n partitions.
# For each partition, create 5 threads / process.
# In each thread, call do_experiment for each exp in the partition.

# Handle control-c. Cleanly stop all parallel threads and their children.

import os
import sys
import argparse
import signal
import psutil
import threading
import subprocess
import datetime

from pprint import pprint
from pathlib import Path
from itertools import islice
from subprocess import PIPE

if 'ritagit' not in os.environ:
    print('environment variable `ritagit` is required.')
    sys.exit(0)

codebase = os.environ['ritagit']
exp_script = Path(codebase).joinpath('Code/evaluations/nov-2020/bin/do_experiment.sh')
print('Using ritagit:', codebase)
print('experiment script:', exp_script)

npars = 20
done = False


def make_chunks(data, SIZE):
    it = iter(data)
    for i in range(0, len(data), SIZE):
        yield [k for k in islice(it, SIZE)]


def get_exp_dirs(top_dir):
    p = Path(top_dir)
    all = [x.absolute().as_posix() for x in p.iterdir() if x.is_dir() and x.name.startswith('exp-')]
    # pprint(all)
    # all = [os.path.abspath(x) for x in all]
    all = sorted(all)
    acc = []
    for sample in make_chunks(all, int(len(all) / npars)):
        # pprint(sample)
        acc.append(sample)
    return acc


class PartitionThread:
    def __init__(self, partition, partid, rmq_port):
        self.procs = []
        self.partition = partition
        self.partid = partid
        self.rmq_port = rmq_port
        self.th = threading.Thread(name=partid, target=self.run)

    def run(self):
        try:
            for exp in self.partition:
                if done:
                    print('done is True:',self.partid)
                    break
                print('Starting ', exp)
                print(exp_script.as_posix())
                part_file = open(self.partid + '.out', 'w')
                proc = psutil.Popen([exp_script.as_posix(), exp, self.rmq_port, self.partid], stderr=subprocess.STDOUT,
                                    stdout=part_file)
                self.procs.append(proc)
                print('Waiting for proc', proc)
                proc.wait()
                # print(proc)
                # TODO Wait until all child procs are done?
        except KeyboardInterrupt:
            print('Keyboard interrupt')


    def start(self):
        self.th.start()

    def __del__(self):
        print('del Thread', self.partid, datetime.datetime.now().isoformat()) 
        for p in self.procs:
            # outs, errs = p.communicate()
            p.wait()
            # print(outs)
            # print(errs)
            print(p)
        


class Main:
    def __init__(self, top_dir):
        self.top_dir = top_dir
        self.partitions = get_exp_dirs(top_dir)
        self.exps = []
        self.rmq_port = 6671
        self.partion_id = 'part-'
        self.threads = []
        pprint(self.partitions)
        print('Start time', datetime.datetime.now().isoformat())

    def __del__(self):
        print('Cleaning up all procs recursively.')
        print('Stop time', datetime.datetime.now().isoformat())

    def start_experiments(self):
        print('Starting experiments in parallel. N = ', len(self.partitions))
        partid = 1
        port = self.rmq_port
        for part in self.partitions:
            th = PartitionThread(part, self.partion_id + str(partid), str(port))
            self.threads.append(th)
            partid = partid + 1
            port = port + 1
            th.start()
        # for t in self.threads:
        #     print('Waiting for thread', t.partid)
        #     t.th.join()


def test_one():
    th = PartitionThread(['exp-0050'], 'part_0', "6670")
    th.start()
    # th.th.join()
    # print('thread state', th.th)


def main(top_dir):
    # test_one()
    print('Starting experiments in parallel. N =', npars)
    
    m = Main(top_dir)
    m.start_experiments()
    print('Done main')


def signal_handler(sig, frame):
    global done
    done = True
    print('You pressed Ctrl+C!')
    # sys.exit(0)


signal.signal(signal.SIGINT, signal_handler)
# print('Press Ctrl+C')
# signal.pause()

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Executes experiments (do_experiment.sh) in parallel')
    parser.add_argument('top_dir')
    args = parser.parse_args()
    main(args.top_dir)
