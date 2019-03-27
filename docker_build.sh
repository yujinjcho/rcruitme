#!/bin/bash

rm -r svc
sbt dist
unzip -d svc target/universal/*-1.0-SNAPSHOT.zip && mv svc/*/* svc/ && rm svc/bin/*.bat && mv svc/bin/* svc/bin/start
docker build -t rcruitme .
