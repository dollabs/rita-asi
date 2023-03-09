#!/usr/bin/env python3

import edn_format
import argparse
from pathlib import Path
import os.path
from pprint import pprint
import sys
import shutil


# Iterate over all directories
# for each dir containing experiment_definition.edn
#  Copy LOG files to the directory.

def copy_log_files(logdir, todir):
    print('Copying log files from', logdir)


def debug_exp_definition(dirs):
    for dir in dirs:
        exp_def_file = os.path.abspath(dir) + '/' + 'experiment-definition.edn'
        test_files = []
        train_files = []
        if os.path.exists(exp_def_file):
            print('Got exp_def:', exp_def_file)
            dats = open(exp_def_file).read()
            # print(dats)
            dat = edn_format.loads(dats)
            # print('dat type', type(dat))
            # edn_format.immutable_dict.ImmutableDict
            # print('blash', edn_format.edn_lex.Keyword('xyz').name)
            # pprint(dat)
            # for k, v in dat.items():
            #     print(k, type(k), k.name)

            test_files = dat[edn_format.edn_lex.Keyword('test-files')]
            train_files = dat[edn_format.edn_lex.Keyword('training-log-files')]
        else:
            print('Exp Def not found', exp_def_file)
        train = set(train_files)
        test = set(test_files)
        print('')
        print('-------- Exp:', dir)
        print('Train Files:', len(train_files))
        print('Test Files:', len(test_files))
        print('Total Files', len(train_files) + len(test_files))
        print('Train Files set:', len(train))
        print('Test Files set:', len(test))
        print('Train Files has duplicates:', len(train_files) != len(train))
        print('Test Files has duplicates:', len(test_files) != len(test))


def get_log_files(dir):
    exp_def_file = os.path.abspath(dir) + '/' + 'experiment-definition.edn'
    files = []
    if os.path.exists(exp_def_file):
        print('Got exp_def:', exp_def_file)
        dats = open(exp_def_file).read()
        # print(dats)
        dat = edn_format.loads(dats)
        # print('dat type', type(dat))
        # edn_format.immutable_dict.ImmutableDict
        # print('blash', edn_format.edn_lex.Keyword('xyz').name)
        # pprint(dat)
        # for k, v in dat.items():
        #     print(k, type(k), k.name)

        files = dat[edn_format.edn_lex.Keyword('test-files')]
    else:
        print('Exp Def not found', exp_def_file)

    return files


def copy_log_files_main(dirs, log_dir):
    for d in dirs:  # ['exp-0001']:#
        print('Working on dir', os.path.abspath(d))
        files = get_log_files(d)
        print('Got files:', len(files))
        for f in files:
            ff = os.path.basename(f)
            ff = os.path.abspath(log_dir) + '/' + ff
            print('Copying: ', ff)
            print('To: ', os.path.abspath(d))
            shutil.copy2(ff, d)
        print()


def main(log_dir):
    here = Path('.')
    print('Current Dir', os.path.abspath(here), '\n')
    dirs = [x for x in here.iterdir() if x.is_dir()]
    dirs.sort()
    copy_log_files_main(dirs, log_dir)
    # debug_exp_definition()


# get_log_files('exp-0001')
# sys.exit(0)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Setup experiments')
    parser.add_argument('log_dir')
    args = parser.parse_args()
    main(args.log_dir)
