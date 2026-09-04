#!/bin/bash

echo "等待服务启动..."
# 此处调用的是Java的健康检测接口
while true; do
    if curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:9080/management/health/liveness | grep -q "200"; then
        echo "服务已就绪，开始启动node服务"
        break
    else
        echo "服务未就绪，等待5秒后重试..."
        sleep 5
    fi
done

export SIDECAR_BASE_URL="${SIDECAR_BASE_URL:-http://127.0.0.1:9080}"
export SIDECAR_WS_BASE_URL="${SIDECAR_WS_BASE_URL:-ws://127.0.0.1:9888}"
export COLLAB_SERVER_PORT="${COLLAB_SERVER_PORT:-9300}"

node /app/collab-server/dist/main.js
