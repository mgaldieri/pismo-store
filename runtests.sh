#!/usr/bin/env bash

TEST_DIR=test
VENV_DIR="${TEST_DIR}/venv"
VENV="${VENV_DIR}/pismo-test"

STORE_IMAGE_NAME="mgaldieri/pismo-store:latest"
WAREHOUSE_IMAGE_NAME="mgaldieri/pismo-warehouse:latest"

STORE_CONTAINER_NAME="pismo-store"
WAREHOUSE_CONTAINER_NAME="pismo-warehouse"

DOCKER_IP=localhost
STORE_SERVER_PORT=8001
WAREHOUSE_SERVER_PORT=8000

DOCKER_SUBNET="172.25.0.0/16"
DOCKER_NET_NAME="pismo-net"

echo "Iniciando testes..."

mkdir ${VENV_DIR}
pyvenv "${VENV}" 2>&1 1>/dev/null
source "${VENV}/bin/activate"
pip3 install wheel && pip3 install -r ${TEST_DIR}/requirements.txt 2>&1 1>/dev/null

# Start docker
if hash docker-machine 2>/dev/null; then
    docker-machine start default 2>&1 1>/dev/null
    docker-machine env default 2>&1 1>/dev/null
    DOCKER_IP=`docker-machine ip default`
else
    docker start 2>/dev/null
fi

export DOCKER_IP=${DOCKER_IP}
export WAREHOUSE_SERVER_PORT=${WAREHOUSE_SERVER_PORT}

if docker network create -d bridge --subnet ${DOCKER_SUBNET} ${DOCKER_NET_NAME} 2>&1 1>/dev/null; then
    docker run -d --name ${WAREHOUSE_CONTAINER_NAME} -p ${WAREHOUSE_SERVER_PORT}:${WAREHOUSE_SERVER_PORT} --network=${DOCKER_NET_NAME} ${WAREHOUSE_IMAGE_NAME} 2>&1 1>/dev/null
    docker run -d --name ${STORE_CONTAINER_NAME} --env WAREHOUSE_SERVER_IP=${WAREHOUSE_CONTAINER_NAME} -p ${STORE_SERVER_PORT}:${STORE_SERVER_PORT} --network=${DOCKER_NET_NAME} ${STORE_IMAGE_NAME} 2>&1 1>/dev/null
fi

aloe test/features

${VENV}/bin/deactivate
rm -rf ${VENV_DIR}

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
