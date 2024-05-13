#!/bin/bash

output=$(git checkout .)

if echo "$output" | grep -q "files that should have been pointers, but weren't"; then
    echo "Error: Encountered files that should have been pointers, but weren't."
    exit 1
else
    echo "No LFS pointer errors detected."
    exit 0
fi
