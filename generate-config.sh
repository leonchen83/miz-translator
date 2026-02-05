#!/usr/bin/env bash
# generate-config.sh
# Generates trans.conf if not mounted as read-only.
# Requires API_KEY and LANG to be set as environment variables.

set -e

CONF_FILE="/app/miz-translator/conf/trans.conf"

# 如果 conf 已存在且不可写，说明挂载了只读文件，跳过生成
if [ -f "$CONF_FILE" ] && ! touch "$CONF_FILE" 2>/dev/null; then
  echo "conf file is mounted externally as read-only, skipping generation."
  exec "$@"
fi

mkdir -p "$(dirname "$CONF_FILE")"

echo "Generating conf at $CONF_FILE"

# 清空原文件
> "$CONF_FILE"

# 写入基础配置
cat >> "$CONF_FILE" <<EOF
#
# translate hint
#
hint=${HINT:-你是一个军事方面的翻译，下面是DCS World 中战役的英语文本，这个文本包含无线电对话,战役简介等等. 请翻译成简体中文，不要添加多余的解释以及补充出多余的对话，不要使用markdown输出, 保持原文的换行格式}

#
# the number of texts to use for translation one time
#
batchSize=${BATCH_SIZE:-36}

#
# For any API compatible with OpenAI (including official OpenAI API, Azure OpenAI, or other OpenAI-compatible endpoints),
# just set translator=openai
#
translator=${TRANSLATOR:-openai}

#
# available service: edge-tts
#
ttsService=${TTS_SERVICE:-edge-tts}
ttsProxy=${PROXY:-}

#
# API configuration
#
baseURL=${BASE_URL:-https://api.deepseek.com/v1}
apiKey=${API_KEY}
model=${MODEL:-deepseek-reasoner}
rateLimitPerMinute=${RATE_LIMIT_PER_MINUTE:-}
temperature=${TEMPERATURE:-1.3}
maxTokens=${MAX_TOKENS:-4096}
minimumLength=${MINIMUM_LENGTH:-12}
original=${ORIGINAL:-false}
original=${LANG:-zh-CN}
EOF

# 固定 keyFilters
cat >> "$CONF_FILE" <<EOF
keyFilters1=DictKey_UnitName_
keyFilters2=DictKey_WptName_
keyFilters3=DictKey_GroupName_
EOF

# 额外 keyFilters 文件，每行一个
if [ -n "$KEY_FILTERS_EXTRA_FILE" ] && [ -f "$KEY_FILTERS_EXTRA_FILE" ]; then
  i=4
  while IFS= read -r line || [ -n "$line" ]; do
    [ -n "$line" ] && echo "keyFilters$i=$line" >> "$CONF_FILE"
    ((i++))
  done < "$KEY_FILTERS_EXTRA_FILE"
fi

# filters 文件，每行一个
if [ -n "$FILTERS_FILE" ] && [ -f "$FILTERS_FILE" ]; then
  i=1
  while IFS= read -r line || [ -n "$line" ]; do
    [ -n "$line" ] && echo "filters$i=$line" >> "$CONF_FILE"
    ((i++))
  done < "$FILTERS_FILE"
fi

# source 文件，每行一个
if [ -n "$SOURCE_FILE" ] && [ -f "$SOURCE_FILE" ]; then
  i=1
  while IFS= read -r line || [ -n "$line" ]; do
    [ -n "$line" ] && echo "source$i=$line" >> "$CONF_FILE"
    ((i++))
  done < "$SOURCE_FILE"
fi

# target 文件，每行一个
if [ -n "$TARGET_FILE" ] && [ -f "$TARGET_FILE" ]; then
  i=1
  while IFS= read -r line || [ -n "$line" ]; do
    [ -n "$line" ] && echo "target$i=$line" >> "$CONF_FILE"
    ((i++))
  done < "$TARGET_FILE"
fi

# Docker 强制指定语言
echo "# language: must be set in Docker via LANG env variable" >> "$CONF_FILE"
echo "language=${LANG}" >> "$CONF_FILE"

echo "Config generated at $CONF_FILE"

# 执行命令
exec "$@"