#!/usr/bin/env bash

STORE_IMAGE_NAME="mgaldieri/pismo-store:latest"
WAREHOUSE_IMAGE_NAME="mgaldieri/pismo-warehouse:latest"

STORE_CONTAINER_NAME="pismo-store"
WAREHOUSE_CONTAINER_NAME="pismo-warehouse"

DOCKER_IP=localhost
STORE_SERVER_PORT=8001
WAREHOUSE_SERVER_PORT=8000

DOCKER_SUBNET="172.25.0.0/16"
DOCKER_NET_NAME="pismo-net"

if hash docker-machine 2>/dev/null; then
    docker-machine start default 2>&1 1>/dev/null
    docker-machine env default 2>&1 1>/dev/null
    DOCKER_IP=`docker-machine ip default`
fi

export DOCKER_IP=${DOCKER_IP}
export WAREHOUSE_SERVER_PORT=${WAREHOUSE_SERVER_PORT}

if docker run -d --name ${WAREHOUSE_CONTAINER_NAME} -p ${WAREHOUSE_SERVER_PORT}:${WAREHOUSE_SERVER_PORT} ${WAREHOUSE_IMAGE_NAME} 2>&1 1>/dev/null; then
    echo "Servidor PISMO-WAREHOUSE rodando em ${DOCKER_IP}:${WAREHOUSE_SERVER_PORT}"
fi

if docker run -d --name ${STORE_CONTAINER_NAME} --env WAREHOUSE_SERVER_IP=${WAREHOUSE_CONTAINER_NAME} -p ${STORE_SERVER_PORT}:${STORE_SERVER_PORT} ${STORE_IMAGE_NAME} 2>&1 1>/dev/null; then
    echo "Servidor PISMO-STORE rodando em ${DOCKER_IP}:${STORE_SERVER_PORT}"
fi

if docker network create -d bridge --subnet ${DOCKER_SUBNET} ${DOCKER_NET_NAME} 2>&1 1>/dev/null; then
    docker network connect ${DOCKER_NET_NAME} ${STORE_CONTAINER_NAME}
    docker network connect ${DOCKER_NET_NAME} ${WAREHOUSE_CONTAINER_NAME}
fi

echo "Servidores conectados"
