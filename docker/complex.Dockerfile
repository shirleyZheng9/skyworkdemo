FROM hub-nj.iwhalecloud.com/lingxi/java:25-python3-nodejs-chromium AS tmp

COPY bote-service/bote-service-boot/target/bote-service-boot.jar /app/app.jar

WORKDIR /app/tmp

# 目标架构，值为 amd64, arm64
# https://github.com/docker-library/bashbrew/blob/master/architecture/oci-platform.go#L14
ARG TARGETARCH

RUN jar xf ../app.jar && \
    find-start-class META-INF/MANIFEST.MF > start-class && \
    rm -rf org BOOT-INF/classpath.idx BOOT-INF/layers.idx META-INF/MANIFEST.MF META-INF/maven META-INF/services/java.nio.file.spi.FileSystemProvider && \
    # 删除 playwright driver 中不需要的文件(node.js 二进制文件, 平台驱动), 只保留 linux 或 linux-arm64 驱动  \
    if ls BOOT-INF/lib/driver-bundle-1.*.jar &> /dev/null; then \
      if [[ $TARGETARCH == "arm64" ]]; then \
        zip -d BOOT-INF/lib/driver-bundle-1.*.jar "driver/*/node" "driver/mac/*" "driver/mac-arm64/*" "driver/win32_x64/*" "driver/linux/*"; \
      else \
        zip -d BOOT-INF/lib/driver-bundle-1.*.jar "driver/*/node" "driver/mac/*" "driver/mac-arm64/*" "driver/win32_x64/*" "driver/linux-arm64/*"; \
      fi; \
    fi && \
    mv META-INF BOOT-INF/classes/

FROM hub-nj.iwhalecloud.com/lingxi/java:25-python3-nodejs-chromium

LABEL maintainer=bian.jiaping

EXPOSE 8080
EXPOSE 9300
EXPOSE 9888

# SERVER_PORT 需要与 MANAGEMENT_PORT 一致，否则 endpoints 不会注册 MVC
# ENABLE_RUNTIME_UPTIME_TELEMETRY=0 用于禁用 TensorFlow 的遥测
# PLAYWRIGHT_NODEJS_PATH: 让 playwright 使用系统自带的 node. driver-bundle 中的 node 会被删除以降低镜像体积
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
    PLAYWRIGHT_NODEJS_PATH=/usr/bin/node \
    NODE_ENV=production \
    COLLAB_SERVER_PORT=9300 \
    TZ=Asia/Shanghai \
    z_app=bote \
    zcm_log_type="project=kp"

LABEL zcm.logs.bote="/app/log/*.log" \
      zcm.logs.bote.tags="project=kp"

COPY --chown=zpaas:zpaas bote-service/docker/docker-app-multi-def.sh /app/docker-app.def
COPY --chown=zpaas:zpaas --from=tmp /app/tmp /app/app
COPY --chown=zpaas:zpaas bote-service/docker/start_node.sh /app/start_node.sh
COPY --chown=zpaas:zpaas docs-web/dist-server /app/collab-server

# 需要安装的 Python 包，多个用空格分隔，可指定版本，比如 "requests pandas==2.2.3"
# 用于支持在构建流程中动态指定需要安装的包，以满足部分现场的需要（只需创建单独的构建流程，不需要创建定制包）
ARG PYTHON_PACKAGES
# 需要安装的 RPM 包，多个用空格分隔
ARG RPM_PACKAGES
# TensorFlow/Keras 模型下载地址
ARG KERAS_MODELS
RUN if [ -n "$PYTHON_PACKAGES" ]; then pip3 install --no-cache-dir $PYTHON_PACKAGES; fi
RUN if [ -n "$RPM_PACKAGES" ]; then dnf install -y --nodocs $RPM_PACKAGES && dnf clean all && rm -rf /var/cache/dnf/.gpg* /var/cache/dnf/* /var/log/*; fi
# 下载模型并放到 keras 的缓存目录
RUN if [ -n "$KERAS_MODELS" ]; then \
      curl -Lfo keras-models.tar.gz $KERAS_MODELS; \
      mkdir -p /app/.keras/models/; \
      tar -xf keras-models.tar.gz -C /app/.keras/models/ --strip-components=1; \
      chown -R zpaas:zpaas /app/.keras; \
      rm -f keras-models.tar.gz; \
    fi

USER 1810
