#!/bin/bash

VERSION=$(cat ./VERSION)
DOCKER_IMAGE=rcruitme
ECR_REPO=692977587379.dkr.ecr.us-east-1.amazonaws.com
ECS_CLUSTER=Nodejs-Monolith-ECSCluster-14Q5JZDDGF5JE
SERVICE_NAME=rcruitme

DEPLOY_TAG=$ECR_REPO/$DOCKER_IMAGE:$VERSION

docker tag $DOCKER_IMAGE:latest $DEPLOY_TAG

$(aws ecr get-login --no-include-email --region us-east-1)
docker push $DEPLOY_TAG

ecs deploy $ECS_CLUSTER $SERVICE_NAME --tag $VERSION --timeout -1
