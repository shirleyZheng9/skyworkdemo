# 博特 —— 智能对话机器人

## 架构

* Java 21
* Maven 3.6+
* Spring Boot 3.5
* 荔枝包
* MyBatis

## 本地运行

启动类: `com.iwhalecloud.bote.Application`

需配置环境变量或系统参数: `ZSMART_HOME=/path/to/ZSMART_HOME`

Nginx 反向代理前端:

```nginx
map $http_upgrade $connection_upgrade {
    default upgrade;
    '' close;
}

proxy_set_header X-Original-URI         $request_uri;
proxy_set_header Host $http_host;
proxy_set_header X-Real-IP $remote_addr;
proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
proxy_set_header X-Forwarded-Proto $scheme;
proxy_read_timeout 600s;
proxy_buffering off;
proxy_set_header Upgrade $http_upgrade;
proxy_set_header Connection $connection_upgrade;
proxy_http_version 1.1;

server {
  listen 9001;
  server_name _;

  location / {
    proxy_pass http://10.10.202.25:9001/;
  }

  location /api/ {
    proxy_pass http://127.0.0.1:8080/;
  }
}
```

## 检查依赖更新

```shell
# 检查 Maven 插件更新
mvn versions:display-property-updates -N -P'!nexus-116'
# 检查依赖更新
mvn versions:display-dependency-updates -N -P'!nexus-116'
```
