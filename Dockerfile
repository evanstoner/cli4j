FROM maven:3-jdk-8

RUN apt-get update && apt-get install -yq \
    sudo \
    && rm -rf /var/lib/apt/lists/*
