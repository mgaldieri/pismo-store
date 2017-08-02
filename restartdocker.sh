#!/usr/bin/env bash

STORE_CONTAINER_NAME="pismo-store"
WAREHOUSE_CONTAINER_NAME="pismo-warehouse"

docker restart ${STORE_CONTAINER_NAME} 2>&1 1>/dev/null
docker restart ${WAREHOUSE_CONTAINER_NAME} 2>&1 1>/dev/null
