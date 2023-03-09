#!/usr/bin/env python3

# install docker api as
# pip install --user docker
import docker
import argparse
from pprint import pprint
import sys
import os
from pathlib import Path
import utils

proj_home = None
config_j = None
debug = False
client = docker.from_env()

URL = "gitlab.asist.aptima.com:5050/asist/testbed/ta1_doll_"
images = ['rita_rabbitmq', 'rita_tailer', 'rita_mqt2rmq', 'rita_clojure']
# images = ['mongo:4.2']
tagged_images = [[x, URL + x] for x in images]
version = None


def setup_proj_home():
    global proj_home
    pth = os.path.dirname(os.path.realpath(__file__))
    # print(pth, type(pth))
    proj_home = Path(pth).parents[1]
    print('RITA HOME: ', proj_home)


def init_globals():
    global version
    setup_proj_home()
    config_j = proj_home.joinpath('Code/resources/public/config.json')
    print('Config file:', config_j)
    conf = utils.read_json(config_j)
    version = conf['rita-version']


def get_image(name):
    img = None
    try:
        img = client.images.get(name)
    except docker.errors.ImageNotFound:
        # print('Image', name, ' not found')
        pass
    return img


def find_images_with_tags():
    timages = []  # Images tagged with aptima gitlab url
    for img in client.images.list():
        # print(img)
        for tag in img.tags:
            if URL in tag:
                timages.append(tag)
    return timages


def remove_old_tags():
    timages = find_images_with_tags()
    for tag_name in timages:
        tag_img = get_image(tag_name)
        if tag_img is not None:
            print('Removing tagged image: ', tag_name)
            client.images.remove(tag_name)


def print_images():
    # pprint(all)
    for img_name in images:
        full_name = img_name
        print('Looking for:', full_name)
        img = get_image(full_name)
        print('Got:', full_name)
        # print('attrs')
        # pprint(img.attrs)
        # print('id', img.id)
        # print('labels', img.labels)
        # print('short_id', img.short_id)
        print('tags', img.tags)
        print()


def tag_images():
    remove_old_tags()
    print()
    for name, tag_name in tagged_images:
        img = get_image(name)
        if img is not None:
            tag_name = '{}:{}'.format(tag_name, version)
            print('Tagging:', name, '=>', tag_name)
            img.tag(tag_name)
        else:
            print('Warn: Image not found:', name)
        print()


def upload_images():
    timages = find_images_with_tags()
    for tag_name in timages:
        print('Pushing:', tag_name, '=>', tag_name)
        print()
        status = client.images.push(tag_name, stream=True, decode=True)
        # print('status type', type(status))
        # print(status)
        for x in status:
            if 'status' in x and debug:
                print(tag_name, ': ', x['status'], end='')
                if 'id' in x:
                    print(':', x['id'], end='')
                print()

            if 'status' not in x:
                pprint(x)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Functions for releasing Rita Containers to Asist Testbed')

    parser.add_argument('--tag_images', default=True, action="store_true",
                        help='Tag latest version of Rita images with testbed URL')
    parser.add_argument('--no_tag_images', dest='tag_images', action='store_false',
                        help='Do not Tag latest version of Rita images with testbed URL')

    parser.add_argument('--upload_images', action="store_true", help='Upload Tagged Rita images to testbed registry')

    args = parser.parse_args()

    init_globals()
    if args.tag_images:
        print('Tagging images')
        tag_images()

    if args.upload_images:
        print('Uploading images')
        upload_images()

    # print_images()

# TODO
# There should not be any need to push mongodb