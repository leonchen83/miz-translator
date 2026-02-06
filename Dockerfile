FROM eclipse-temurin:21-jdk-jammy

ENV DEBIAN_FRONTEND=noninteractive

WORKDIR /app

# --------------------------
# 设置 apt 源（根据架构选择）
# --------------------------
RUN ARCH=$(dpkg --print-architecture) && \
    if [ "$ARCH" = "arm64" ]; then \
        echo "deb https://mirrors.aliyun.com/ubuntu-ports/ jammy main restricted universe multiverse" > /etc/apt/sources.list && \
        echo "deb https://mirrors.aliyun.com/ubuntu-ports/ jammy-updates main restricted universe multiverse" >> /etc/apt/sources.list && \
        echo "deb https://mirrors.aliyun.com/ubuntu-ports/ jammy-backports main restricted universe multiverse" >> /etc/apt/sources.list && \
        echo "deb https://mirrors.aliyun.com/ubuntu-ports/ jammy-security main restricted universe multiverse" >> /etc/apt/sources.list; \
    else \
        echo "deb https://mirrors.aliyun.com/ubuntu/ jammy main restricted universe multiverse" > /etc/apt/sources.list && \
        echo "deb https://mirrors.aliyun.com/ubuntu/ jammy-updates main restricted universe multiverse" >> /etc/apt/sources.list && \
        echo "deb https://mirrors.aliyun.com/ubuntu/ jammy-backports main restricted universe multiverse" >> /etc/apt/sources.list && \
        echo "deb https://mirrors.aliyun.com/ubuntu/ jammy-security main restricted universe multiverse" >> /etc/apt/sources.list; \
    fi

RUN apt-get update && apt-get install -y --no-install-recommends \
    bash \
    ffmpeg \
    unzip \
    curl \
    wget \
    git \
    jq \
    ca-certificates \
    maven \
    python3-full \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

# ========================
# Python venv
# ========================
RUN python3 -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

RUN pip install --upgrade pip \
    && pip install -i https://pypi.tuna.tsinghua.edu.cn/simple \
        fastapi \
        uvicorn[standard] \
        edge-tts \
        faster-whisper \
        python-multipart

COPY . /app

RUN mkdir -p /root/.m2

# 配置阿里云 Maven 仓库
RUN tee /root/.m2/settings.xml > /dev/null <<EOF
<settings>
  <mirrors>
    <mirror>
      <id>aliyunmaven</id>
      <mirrorOf>*</mirrorOf>
      <name>阿里云公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
EOF

RUN mvn clean install -Dmaven.test.skip=true

RUN unzip -o /app/target/miz-translator-release.zip -d /app \
 && ln -sf /app/miz-translator/bin/trans /usr/local/bin/trans \
 && ln -sf /app/miz-translator/bin/trans-voice /usr/local/bin/trans-voice
 
COPY generate-config.sh /app/miz-translator/generate-config.sh
RUN chmod +x /app/miz-translator/generate-config.sh
ENTRYPOINT ["/app/miz-translator/generate-config.sh"]

WORKDIR /app/miz-translator
CMD ["uvicorn", "bin.main:app", "--host", "0.0.0.0", "--port", "8000"]