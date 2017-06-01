#!/usr/bin/env bash
# helper script to run iptables integration tests on mac

TAG=$(basename $(pwd))

docker run -it -v $(pwd):/home/${TAG} -w /home/${TAG} maven:3-jdk-8 mvn test
