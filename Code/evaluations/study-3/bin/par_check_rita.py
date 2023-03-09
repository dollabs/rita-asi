#!/usr/bin/env python3

# Given a directory containing rmq log files, replay them in parallel in docker environment
# for each instantiation of this script, there will be a base directory, and the base directory
# will contain a directory for each rmq log file. In this directory we will keep stdout and
# rmq log files created by the docker environment for each input RMQ log file.
# Handle control-c. Cleanly stop all parallel threads and their children.

import os
import sys
import argparse
import signal
import psutil
import threading
import subprocess
import time
import datetime

from pprint import pprint
from pathlib import Path
from itertools import islice
from subprocess import PIPE

if 'ritagit' not in os.environ:
    print('environment variable `ritagit` is required.')
    sys.exit(0)

codebase = os.environ['ritagit']
check_script = Path(codebase).joinpath('Code/evaluations/study-3/bin/check_rita.sh')
print('Using ritagit:', codebase)
print('check script:', check_script)
now = datetime.datetime.now()
now_formatted = now.strftime('%B-%d-%H-%Y')
proj_dir = f'run-{now_formatted}'
print('Proj Dir: ', proj_dir)
Path(proj_dir).mkdir(exist_ok=True)
npars = 12
done = False


def bucketize_log_files(top_dir):
    p = Path(top_dir)
    all = [x.absolute().as_posix() for x in p.iterdir() if x.is_file() and x.name.endswith('.log')]
    all = [os.path.abspath(x) for x in all]
    all = sorted(all)
    # pprint(all)
    print(f'RMQ files {len(all)}, max buckets {npars}')
    acc = []
    idx = 0
    for x in range(npars):
        acc.append([])
    for x in all:
        acc[idx].append(x)
        idx = idx + 1
        idx = idx % npars

    return acc


class PartitionThread:
    def __init__(self, partition, proj_name, rmq_port):
        self.procs = []
        self.partition = partition
        self.proj_name = proj_name
        self.rmq_port = rmq_port
        self.th = threading.Thread(name=proj_name, target=self.run)

    def run(self):
        try:
            for f in self.partition:
                if done:
                    print('done is True:', self.proj_name)
                    break
                log_dir = Path(proj_dir).joinpath(Path(f).with_suffix('').name)
                print(f'log dir {log_dir}')
                Path(log_dir).mkdir(exist_ok=True)
                print(
                    f'Starting check script in thread: {self.th.name}\n logfile: {f}\n port: {self.rmq_port}\n Thread id: {self.proj_name}\n Outdir: {log_dir}')
                time.sleep(2)
                part_file = open(self.proj_name + '.out', 'a')
                proc = psutil.Popen([check_script.as_posix(), f, self.rmq_port, self.proj_name, log_dir],
                                    stderr=subprocess.STDOUT,
                                    stdout=part_file)
                self.procs.append(proc)
                print('Waiting for proc', proc)
                proc.wait()



        except KeyboardInterrupt:
            print('Keyboard interrupt')

    def start(self):
        self.th.start()

    def __del__(self):
        print('del Thread', self.proj_name, datetime.datetime.now().isoformat())
        for p in self.procs:
            # outs, errs = p.communicate()
            p.wait()
            # print(outs)
            # print(errs)
            print(p)


class Main:
    def __init__(self, top_dir):
        self.top_dir = top_dir
        self.buckets = bucketize_log_files(top_dir)
        self.exps = []
        self.rmq_port = 6671
        self.partion_id = 'part-'
        self.threads = []
        # print(f'Files in buckets {len(self.buckets)}')
        # pprint(self.buckets)
        print()
        print('Start time', datetime.datetime.now().isoformat())

    def __del__(self):
        # print('Cleaning up all procs recursively.')
        # print('Stop time', datetime.datetime.now().isoformat())
        pass

    def start_runs(self):
        print('Starting rmq logs in parallel. N = ', len(self.buckets))
        partid = 1
        port = self.rmq_port
        for part in self.buckets:
            if len(part) > 0:
                th = PartitionThread(part, self.partion_id + str(partid), str(port))
                self.threads.append(th)
                partid = partid + 1
                port = port + 1
                time.sleep(0.1)
                th.start()
        # for t in self.threads:
        #     print('Waiting for thread', t.partid)
        #     t.th.join()


# def test_one():
#     th = PartitionThread(['exp-0050'], 'part_0', "6670")
#     th.start()
#     # th.th.join()
#     # print('thread state', th.th)


def main(top_dir):
    # test_one()
    print('Starting rmq log runs in parallel. N =', npars)
    m = Main(top_dir)
    m.start_runs()
    time.sleep(0.5)
    print('end main: waiting for threads to finish')
    print('')


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
