#!/bin/bash

COMMAND=$1
NAME=$2
PORT=$3

case $COMMAND in
    start)
        docker run -d --name $NAME -p $PORT:6379 redis
        ;;
    stop)
        docker stop $NAME
        docker rm $NAME
        ;;
    *)
        echo "Usage: $0 {start|stop} {container_name} {port}"
        exit 1
        ;;
esac
