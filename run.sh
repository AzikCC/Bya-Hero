#!/bin/sh
# Builds, then starts the website at http://localhost:8080/board
set -e
cd "$(dirname "$0")"
./build.sh
java -cp out com.byahero.Main
