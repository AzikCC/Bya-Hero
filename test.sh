#!/bin/sh
# Builds, then runs all tests. Exit code is 1 if any test fails.
set -e
cd "$(dirname "$0")"
./build.sh
java -cp out com.byahero.TestRunner
