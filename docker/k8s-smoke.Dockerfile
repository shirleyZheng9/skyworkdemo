# 基于运行基座的极小镜像：Python 内置 HTTP，用于 K8s 调度/探针/网络排查。
# 构建（在项目根目录）：
#   docker build --platform linux/amd64 -f docker/k8s-smoke.Dockerfile -t bote-k8s-smoke:local .
# 本地试跑：
#   docker run --rm -p 8080:8080 bote-k8s-smoke:local
#   curl -sSf http://127.0.0.1:8080/ >/dev/null && echo ok

ARG BASE_RUNTIME_IMAGE=bote-runtime-base:1.0.0
FROM ${BASE_RUNTIME_IMAGE}

EXPOSE 8080

CMD ["python3", "-m", "http.server", "8080", "--bind", "0.0.0.0"]
