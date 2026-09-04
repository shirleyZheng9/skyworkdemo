FROM hub-nj.iwhalecloud.com/lingxi/java:25-python3-nodejs AS tmp

COPY bote-document-boot/target/bote-document-boot.jar /app/app.jar

WORKDIR /app/tmp

RUN jar xf ../app.jar && \
    find-start-class META-INF/MANIFEST.MF > start-class && \
    rm -rf org BOOT-INF/classpath.idx BOOT-INF/layers.idx META-INF/MANIFEST.MF META-INF/maven && \
    mv META-INF BOOT-INF/classes/

FROM hub-nj.iwhalecloud.com/lingxi/java:25-python3-nodejs

LABEL maintainer=bian.jiaping

EXPOSE 9080

 # 设置时区
ENV TZ=Asia/Shanghai

ENV ZSMART_HOME=/opt/ZSMART_HOME \
    # 需要与 management.port 一致，否则 endpoints 不会注册 MVC
    SERVER_PORT=9080 \
    APP_CONTEXT_PATH=/ \
    MANAGEMENT_PORT=9080 \
    MANAGEMENT_CONTEXT_PATH=/management \
    MANAGEMENT_SECURITY_ENABLED=true \
    FTF_MANAGEMENT_SECURITY_ENABLED=true \
    FTF_MANAGEMENT_SECURITY_ALLOW=127.0.0.1 \
    # JVM
    JAVA_OPTS="-XX:MaxRAMPercentage=60.0 -Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true -Dfile.encoding=UTF-8" \
    # 日志
    z_app=lcdp \
    zcm_log_type="project=kp"

LABEL zcm.logs.lcdp="/app/log/*.log" \
      zcm.logs.lcdp.tags="project=kp"

COPY --chown=zpaas:zpaas bote-document-boot/docker/docker-app-def.sh /app/docker-app.def
COPY --chown=zpaas:zpaas --from=tmp /app/tmp /app/app

USER 1810
