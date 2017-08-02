#!/usr/bin/env bash

./gradlew run > log_app_out.txt 2> log_app_err.txt &
echo $! > .saved_pid
