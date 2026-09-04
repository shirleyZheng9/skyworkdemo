#!/bin/bash
# https://docs.iwhalecloud.com/doi/YNy/%E5%AE%B9%E5%99%A8%E9%95%9C%E5%83%8F%E7%9B%B8%E5%85%B3/%E5%A6%82%E4%BD%95%E4%BD%BF%E7%94%A8%E6%A0%87%E5%87%86%E7%9A%84Entrypoint%E8%84%9A%E6%9C%AC

# shellcheck disable=SC2034
APP=("app")
app=(
  # start command. Java 基础镜像不会自动处理 JAVA_OPTS 环境变量
  "java $JAVA_OPTS -cp /app/app/BOOT-INF/classes/:/app/app/BOOT-INF/lib/* $(cat /app/app/start-class)"
  # process key (启动命令中的关键字，用来查询进程 ID)
  "$(cat /app/app/start-class)"
  # liveness check command
  "echo $(netstat -lpnt | grep -c -w 8080)"
  # liveness successful code
  "1"
  # readiness check command
  "healthcheck.sh"
  # readiness successful code
  "UP"
)
