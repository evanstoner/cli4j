#!/usr/bin/env bash
# helper script to run iptables integration tests on mac

MAVEN=~/.m2:/root/.m2
TAG=$(basename $(pwd))

docker build -t ${TAG} .
docker run --privileged -it -v $(pwd):/home/${TAG} -v ${MAVEN} -w /home/${TAG} ${TAG} mvn test
