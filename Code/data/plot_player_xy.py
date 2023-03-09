#!/usr/bin/env python

import sys
import json
from pprint import pprint
import csv

import matplotlib.pyplot as plt
import numpy as np


# name,x,y,z, rmq_recv_time, msg_header_time
# Player396, -2190.5, 28.0, 167.5, 1583444566213, 2020-03-05T21:42:46.212870Z

def read_data(file):
    lines = []
    with open(file, 'r') as csv_file:
        data = csv.reader(csv_file)
        for row in data:
            # print row
            lines.append(row)
    return lines


def plot_xy(data):
    x_idx = 1
    y_idx = 2
    z_idx = 3
    x_vals = []
    y_vals = []
    i = 0
    count = len(data)
    lastx = lasty = None
    for d in data:
        xval = float(d[x_idx])
        yval = float(d[z_idx])
        x_vals.append(xval)
        y_vals.append(yval)

        if lastx != xval or lasty != yval:
            plt.clf()
            plt.plot(x_vals, y_vals)
            plt.draw()
            plt.pause(0.001)

        lastx = xval
        lasty = yval
        i = i + 1
        if i % 250 == 0:
            print 'Plotted so far', i, 'out of', count
    # plt.scatter(x_vals, y_vals)
    print 'Done plotting points'
    plt.show()


def main(file):
    data = read_data(file)
    print data[0]
    print 'Line count', len(data[1:])
    plot_xy(data[1:])


if __name__ == "__main__":
    args = sys.argv
    argc = len(args)
    if argc < 2:
        print(
            'Usage {} \'csv file with player xy required\'\n You can use rmq_reader.py to produce input for this file '.format(
                args[0]))
        sys.exit(1)
    main(args[1])

# plt.ion()
# for i in range(50):
#     y = np.random.random([10,1])
#     plt.plot(y)
#     plt.draw()
#     plt.pause(0.01)
#     plt.clf()
#
# plt.pause(10)
