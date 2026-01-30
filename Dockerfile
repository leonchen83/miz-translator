FROM bellsoft/liberica-openjdk-debian:21

WORKDIR /app

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
    && pip install \
        edge-tts \
        faster-whisper

COPY . /app

RUN mvn clean install -Dmaven.test.skip=true

RUN unzip -o /app/target/miz-translator-release.zip -d /app \
 && ln -sf /app/miz-translator/bin/trans /usr/local/bin/trans \
 && ln -sf /app/miz-translator/bin/trans-voice /usr/local/bin/trans-voice
 
COPY generate-config.sh /app/miz-translator/generate-config.sh
RUN chmod +x /app/miz-translator/generate-config.sh
ENTRYPOINT ["/app/miz-translator/generate-config.sh"]

WORKDIR /app/miz-translator
CMD ["/bin/bash"]