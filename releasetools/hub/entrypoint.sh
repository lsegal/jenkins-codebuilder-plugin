#!/bin/sh

export GITHUB_TOKEN=${RELEASE_TOKEN:-$GITHUB_TOKEN}
hub "$@"
