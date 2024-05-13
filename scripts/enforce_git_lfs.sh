#!/bin/bash

output=$(git checkout .)

if echo "$output" | grep -q "files that should have been pointers, but weren't"; then
    echo "Error: Encountered files that should have been pointers, but weren't."
    exit 1
else
    exit 0
fi
