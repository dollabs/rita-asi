## Introduction
This module contains a set of Doll/MIT component to extract a physical model from minecraft world files in the testbed. It relies on mca files `./testbed/Local/CLEANMAPS/`. For each world, __Falcon/Sparky__ etc, it consumes region files and corresponding MIN/MAX co-rodinates to produce mission map in json format.

## Engineering Notes
The contents of JSON file are as follows and self explanatory. 
#### JSON Descrition
```
{
    "doors": [
        [
            [
                -2111,
                53,
                178
            ],
            [
                -2111,
                52,
                178
            ]
        ],
        [
            [
                -2113,
                53,
                194
            ],
            [
                -2113,
                52,
                194
            ]
        ],
        ...
        ],
    "data": [
        [
            [
                -2150,
                52,
                169
            ],
            "stained_hardened_clay"
        ],
        ...
        [
            [
                -2126,
                53,
                164
            ],
            "quartz_stairs"
        ],
        [
            [
                -2142,
                52,
                167
            ],
            "end_portal_frame"
        ],
        ...
      ],
"levers": [
    [
      [
        -2035,
        61,
        184
      ],
      "lever"
    ],
    [
      [
        -2035,
        61,
        177
      ],
      "lever"
    ],
}
```

See [IMCW Readme for Details](https://github.com/paulratdollabs/IMCW/blob/master/README.md)

#### Config Format
The config format used to capture region and range details of the mission is as follows. Currently, it resides in `./base-map/ASIST-MC-toolbox/testbed_to_json.py`. If necessary, it could be extracted to json file.

```
# Falcon data comes from multiple region files.
mc_worlds = {
    'Falcon':  # reference to world in maps_base = 'Local/CLEAN_MAPS/'
        {
            'Falcon5':  # Intermediate folder where we generate artifacts
                {'region': (-5, 0), # Reference to files in region/ . Ex: region/r.-5.0.mca
                 'ranges': (-2112, -2049, 128, 207, 60, 62)}, # Max and Min XYZ co-ordinates
            'Falcon4': {'region': (-4, 0),
                        'ranges': (-2048, -2017, 128, 207, 60, 62)}
        },
    'Sparky': {'Sparky': {'region': (-5, 0),
                          'ranges': (-2176, -2097, 144, 207, 52, 54)}}}
```

## Requirements
  * This module relies on other modules in external repositories. We use `git submodules` to initialize and pull updates. `git submodule init` is a one time operation to initialize the submodules and `git submodule update` could be used to pull changes.
When intialized properly, there should be 3 directories `ASIST-MC-toolbox/`, `IMCW/` and `mcworldlib/`.

 * `asist_testbed` environment value is required for correct operation. 

#### With Docker
When using docker to create JSON files, docker version 18 or higher is recommended. We have tested with `Docker version 18.09.7, build 2d0083d` on Ubuntu and `Docker version 19.03.2, build 6a30dfc` on MacOS. 

#### Natively on Host.

 * Java 8, python 3.7 and pipenv. __pipenv__ is a tool that combines pip and virtualenv to create pristine python environments.
   * `pipenv` can be installed as `pip3 install --user pipenv`
   * Typical commands are `pipenv shell` to create a new shell with pristine python enviroment and `pipenv install` to install all the dependencies in the environment. `pipenv install` is a one time operation only or as and when dependencies change which are tarcked in `Pipenv` file.

## Operation
### With Docker
Docker file has command examples to build and run the Docker image.
  * Use `docker build -t asist_base_map .` to build the image.
  * Use `./make_asist_base_map_with_docker.sh` to create the json file.
    * We have tested this script on both Linux and MacOS.

### Native Host
  * Assuming `pipenv shell` has been started, Use `./make_asist_base_map.sh` to create the json file.
    * We have tested this script on both Linux and MacOS.

## Issues

Occasionally and on MacOS, if the `testbed_to_json` command fails with `OSError: [Errno 24] Too many open files`, increase 
the limit as `ulimit -n 4096`. Note this sets the value for the current shell instance only.

```
./testbed_to_json.py Falcon
Creating JSON file for world Falcon
Current Working Dir /Users/prakash/projects/asist-rita/git/asist_rita/Code/asist-base-map/ASIST-MC-toolbox
Ensuring Dir exists Falcon
Falcon5 {'region': (-5, 0), 'ranges': (-2112, -2049, 128, 207, 60, 62)}
Falcon (-5, 0) (-2112, -2049, 128, 207, 60, 62) Falcon5
Current Working Dir /Users/prakash/projects/asist-rita/git/asist_rita/Code/asist-base-map/ASIST-MC-toolbox
Ensuring Dir exists Falcon5/floors
Loading World 'Falcon': 100%|██████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████| 26/26 [00:03<00:00,  8.32 Region/s]
28 31 8 12 12 14
Traceback (most recent call last):
  File "./testbed_to_json.py", line 102, in <module>
  File "./testbed_to_json.py", line 84, in make_world_wrapper
  File "./testbed_to_json.py", line 64, in make_world
  File "/Users/prakash/projects/asist-rita/git/asist_rita/Code/asist-base-map/ASIST-MC-toolbox/map_generator.py", line 138, in generate_maps
  File "/Users/prakash/projects/asist-rita/git/asist_rita/Code/asist-base-map/ASIST-MC-toolbox/map_generator.py", line 34, in create_collage
  File "/Users/prakash/Library/Python/3.7/lib/python/site-packages/PIL/Image.py", line 2766, in open
OSError: [Errno 24] Too many open files: 'resources/myblocks/brick.png'

```

## Questions, comments and feedback
Email: asist-rita@dollabs.com or find anyone of us on Slack!
