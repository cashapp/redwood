#!/usr/bin/env bash

output=$(git checkout .)

if [[ output == *"files that should have been pointers, but weren't"* ]]; then
    echo "Error: Encountered files that should have been checked in using Git LFS, but weren't."
    exit 1
else
    exit 0
fi
