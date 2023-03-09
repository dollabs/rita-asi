#!/usr/bin/env bash
pamela --input SARbuilding.pamela --json-ir --output SARbuilding.pamela.json-ir build
pamela --input Participant.pamela --json-ir --output Participant.json-ir build
pamela --input rabs.pamela --json-ir --output rabs.json-ir build
