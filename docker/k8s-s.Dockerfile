# 构建：mvn -s .mvn-nexus-settings.xml -U -pl bote-service-boot -am package -DskipTests
# 应用镜像（强制使用本地基座，不向 Hub 拉取 bote-runtime-base）：
#   docker images | grep bote-runtime-base   # 确认本地已有 1.0.0
#   docker build --pull=false --platform linux/amd64 -f docker/k8s-s.Dockerfile \
#     -t bote-service:local --build-arg VERSION=v2.9.6 .
# 无本地基座时先：docker build --pull=false -f docker/base-runtime.Dockerfile -t bote-runtime-base:1.0.0 --build-arg VERSION=1.0.0 .
# 默认 CMD：与 docker-compose / docker-app-def.sh 一致，直接 exec java（扁平 classpath）。
# K8s/docker-compose 若配置 command 会覆盖 CMD。
#
ARG BASE_RUNTIME_IMAGE=bote-runtime-base:1.0.0

FROM ${BASE_RUNTIME_IMAGE} AS unpack-jar
COPY bote-service-boot/target/bote-service-boot.jar /app/app.jar
WORKDIR /app/tmp
RUN jar xf ../app.jar \
 && find-start-class META-INF/MANIFEST.MF > start-class \
 && rm -rf org BOOT-INF/classpath.idx BOOT-INF/layers.idx \
        META-INF/MANIFEST.MF META-INF/maven \
        META-INF/services/java.nio.file.spi.FileSystemProvider \
 && mv META-INF BOOT-INF/classes/

FROM ${BASE_RUNTIME_IMAGE}
ARG VERSION=local
LABEL org.opencontainers.image.version="${VERSION}"
ENV APP_VERSION=${VERSION}
EXPOSE 8080

ENV ZSMART_HOME=/opt/ZSMART_HOME \
    SERVER_PORT=8080 \
    APP_CONTEXT_PATH=/ \
    MANAGEMENT_PORT=8080 \
    MANAGEMENT_CONTEXT_PATH=/management \
    MANAGEMENT_SECURITY_ENABLED=true \
    FTF_MANAGEMENT_SECURITY_ENABLED=true \
    FTF_MANAGEMENT_SECURITY_ALLOW=127.0.0.1 \
    JAVA_OPTS="-XX:MaxRAMPercentage=60.0 -Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true -Dfile.encoding=UTF-8" \
    ENABLE_RUNTIME_UPTIME_TELEMETRY=0 \
    z_app=bote \
    zcm_log_type="project=kp"

COPY --chown=zpaas:zpaas docker/docker-app-def.sh /app/docker-app.def
COPY --chown=zpaas:zpaas --from=unpack-jar /app/tmp /app/app

RUN mkdir -p /opt/ZSMART_HOME/etc /opt/ZSMART_HOME/log /home/zpaas/log \
 && chown -R zpaas:zpaas /opt/ZSMART_HOME /home/zpaas

# Python 非 PID1，避免 kill 占位进程时把整个容器带走；业务仍用 8080。
#CMD ["bash", "-c", "python3 -m http.server 18080 --bind 0.0.0.0 & exec tail -f /dev/null"]
CMD ["bash", "-c", "exec java $JAVA_OPTS -cp /app/app/BOOT-INF/classes/:/app/app/BOOT-INF/lib/* $(cat /app/app/start-class)"]