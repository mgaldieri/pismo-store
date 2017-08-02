#!/usr/bin/env bash

STORE_CONTAINER_NAME="pismo-store"
WAREHOUSE_CONTAINER_NAME="pismo-warehouse"

DOCKER_SUBNET="172.25.0.0/16"
DOCKER_NET_NAME="pismo-net"

if docker network disconnect ${DOCKER_NET_NAME} ${STORE_CONTAINER_NAME}; then
    docker stop ${STORE_CONTAINER_NAME} 2>&1 1>/dev/null
    docker rm ${STORE_CONTAINER_NAME} 2>&1 1>/dev/null

    echo "Servidor PISMO-STORE encerrado"
fi

if docker network disconnect ${DOCKER_NET_NAME} ${WAREHOUSE_CONTAINER_NAME}; then
    docker stop ${WAREHOUSE_CONTAINER_NAME} 2>&1 1>/dev/null
    docker rm ${WAREHOUSE_CONTAINER_NAME} 2>&1 1>/dev/null

    echo "Servidor PISMO-WAREHOUSE encerrado"
fi

if docker network rm ${DOCKER_NET_NAME}; then
    echo "Rede encerrada"
fi

unset DOCKER_IP
unset WAREHOUSE_SERVER_PORT