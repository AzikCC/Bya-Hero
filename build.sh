#!/bin/sh
# Compiles the app and the tests into the "out" folder.
set -e
cd "$(dirname "$0")"
mkdir -p out
javac -d out $(find src test -name '*.java')
echo "Build OK -> out/"
