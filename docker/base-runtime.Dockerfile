# 自建替代 hub-nj.iwhalecloud.com/lingxi/java:21-python3-nodejs-chromium
# 构建上下文必须是 bote_service 根目录（含 docker/find-start-class）。勿在 docker/ 目录下使用「-f docker/xxx」会报 lstat docker。
# 最简：在 bote_service 执行 make docker-base TAG=1.0.0
# 或：./docker/build-base.sh bote-runtime-base:1.0.0 1.0.0
# 若当前已在 bote_service/docker 目录（常见），用：
#   docker build -f base-runtime.Dockerfile -t bote-runtime-base:1.0.0 --build-arg VERSION=1.0.0 ..
# 若在 bote_service 根目录，才用：
#   docker build -f docker/base-runtime.Dockerfile -t bote-runtime-base:1.0.0 --build-arg VERSION=1.0.0 .
# 应用镜像：./docker/build-app.sh bote-service:1.0.0 1.0.0（或见 Dockerfile 头注释）
#
# 说明：与官方 lingxi 镜像在软件版本、健康检查脚本等细节上可能仍有差异；healthcheck.sh 等若由平台注入，需自行补齐。

FROM fedora:41

RUN dnf -y upgrade --refresh \
  && dnf -y install \
    java-21-openjdk-devel \
    python3 \
    python3-pip \
    nodejs \
    npm \
    chromium \
    curl \
    findutils \
    tar \
    net-tools \
    glibc-langpack-en \
    glibc-langpack-zh \
  && dnf clean all \
  && rm -rf /var/cache/dnf/* /var/log/*

COPY docker/find-start-class /usr/local/bin/find-start-class
RUN chmod 755 /usr/local/bin/find-start-class

RUN groupadd -g 1810 zpaas \
  && useradd -u 1810 -g zpaas -M -s /sbin/nologin zpaas

WORKDIR /app

ARG VERSION=local
LABEL org.opencontainers.image.version="${VERSION}"
