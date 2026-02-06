#!/usr/bin/env bash
# generate-config.sh
# Generates trans.conf if not mounted as read-only.
# Requires API_KEY to be set, LANG optional (default zh-CN).

set -e

CONF_FILE="/app/miz-translator/conf/trans.conf"

# -----------------------------
# 统一 LANG 默认值
# -----------------------------
LANG="${LANG:-zh-CN}"

# -----------------------------
# 如果 conf 已存在且不可写，跳过生成
# -----------------------------
if [ -f "$CONF_FILE" ] && ! touch "$CONF_FILE" 2>/dev/null; then
  echo "conf file is mounted externally as read-only, skipping generation."
  exec "$@"
fi

mkdir -p "$(dirname "$CONF_FILE")"
echo "Generating conf at $CONF_FILE"

# -----------------------------
# LANG_NAME 映射（人类可读）
# -----------------------------
case "$LANG" in
    zh-CN) LANG_NAME="简体中文" ;;
    en-US) LANG_NAME="English" ;;
    ja-JP) LANG_NAME="日本語" ;;
    ko-KR) LANG_NAME="한국어" ;;
    es-ES) LANG_NAME="Español" ;;
    fr-FR) LANG_NAME="Français" ;;
    de-DE) LANG_NAME="Deutsch" ;;
    it-IT) LANG_NAME="Italiano" ;;
    nl-NL) LANG_NAME="Nederlands" ;;
    pl-PL) LANG_NAME="Polski" ;;
    sv-SE) LANG_NAME="Svenska" ;;
    no-NO) LANG_NAME="Norsk" ;;
    da-DK) LANG_NAME="Dansk" ;;
    ro-RO) LANG_NAME="Română" ;;
    cs-CZ) LANG_NAME="Čeština" ;;
    hu-HU) LANG_NAME="Magyar" ;;
    bg-BG) LANG_NAME="Български" ;;
    uk-UA) LANG_NAME="Українська" ;;
    vi-VN) LANG_NAME="Tiếng Việt" ;;
    ms-MY) LANG_NAME="Bahasa Melayu" ;;
    el-GR) LANG_NAME="Ελληνικά" ;;
    he-IL) LANG_NAME="עברית" ;;
    ar-SA) LANG_NAME="العربية" ;;
    *) LANG_NAME="$LANG" ;;
esac

# -----------------------------
# 默认提示词，使用 {{lang}} 占位
# -----------------------------
DEFAULT_HINT="你是一个军事方面的翻译，下面是 DCS World 中战役或任务的英语文本，这个文本包含无线电对话、战役或任务的简介等等。请翻译成 {{lang}}，不要添加多余的解释以及补充出多余的对话，不要使用 markdown 输出，保持原文的换行格式。"

HINT_VALUE="${HINT:-$DEFAULT_HINT}"

# 替换 {{lang}} → LANG_NAME
if [[ "$HINT_VALUE" == *"{{lang}}"* ]]; then
  HINT_VALUE="${HINT_VALUE//\{\{lang\}\}/$LANG_NAME}"
fi

# -----------------------------
# 写入 trans.conf
# -----------------------------
> "$CONF_FILE"

cat >> "$CONF_FILE" <<EOF
#
# translate hint
#
hint=$HINT_VALUE

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
language=${LANG}
EOF

# -----------------------------
# 固定 keyFilters
# -----------------------------
cat >> "$CONF_FILE" <<EOF
keyFilters1=DictKey_UnitName_
keyFilters2=DictKey_WptName_
keyFilters3=DictKey_GroupName_
EOF

# -----------------------------
# 额外 keyFilters 文件
# -----------------------------
if [ -n "$KEY_FILTERS_EXTRA_FILE" ] && [ -f "$KEY_FILTERS_EXTRA_FILE" ]; then
  i=4
  while IFS= read -r line || [ -n "$line" ]; do
    [ -n "$line" ] && echo "keyFilters$i=$line" >> "$CONF_FILE"
    ((i++))
  done < "$KEY_FILTERS_EXTRA_FILE"
fi

# -----------------------------
# filters 文件
# -----------------------------
if [ -n "$FILTERS_FILE" ] && [ -f "$FILTERS_FILE" ]; then
  i=1
  while IFS= read -r line || [ -n "$line" ]; do
    [ -n "$line" ] && echo "filters$i=$line" >> "$CONF_FILE"
    ((i++))
  done < "$FILTERS_FILE"
fi

# -----------------------------
# source 文件
# -----------------------------
if [ -n "$SOURCE_FILE" ] && [ -f "$SOURCE_FILE" ]; then
  i=1
  while IFS= read -r line || [ -n "$line" ]; do
    [ -n "$line" ] && echo "source$i=$line" >> "$CONF_FILE"
    ((i++))
  done < "$SOURCE_FILE"
fi

# -----------------------------
# target 文件
# -----------------------------
if [ -n "$TARGET_FILE" ] && [ -f "$TARGET_FILE" ]; then
  i=1
  while IFS= read -r line || [ -n "$line" ]; do
    [ -n "$line" ] && echo "target$i=$line" >> "$CONF_FILE"
    ((i++))
  done < "$TARGET_FILE"
fi

# -----------------------------
# Docker 强制指定语言
# -----------------------------
echo "# language: must be set in Docker via LANG env variable" >> "$CONF_FILE"
echo "language=${LANG}" >> "$CONF_FILE"

echo "Config generated at $CONF_FILE"

# -----------------------------
# 执行命令
# -----------------------------
exec "$@"
