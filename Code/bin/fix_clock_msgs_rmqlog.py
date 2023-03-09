#!/usr/bin/env python3

import argparse
import rmq_log_utils as utils


# Clock messages in RMQ log must have app-id and routing-key

def main(infile, outfile):
    print('Reading from', infile)
    print('Writing to', outfile)
    parsed = utils.read_log_file(infile)

    for ts, msg in parsed:
        if 'tb_clock' in msg:
            if not 'routing-key' in msg:
                rkey = 'clock'
                if 'received-routing-key' in msg:
                    rkey = msg['received-routing-key']
                msg['routing-key'] = rkey
            if not 'app-id' in msg:
                msg['app-id'] = 'TestbedBusInterface'
        # print(msg)
    utils.write_log_file(outfile, parsed)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Test reading of RMQ Log files')
    parser.add_argument('rmq_file')
    parser.add_argument('out_dir')
    args = parser.parse_args()
    main(args.rmq_file, args.out_dir + '/' + args.rmq_file)
