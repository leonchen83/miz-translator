# DCS Mission Translator

## Introduction

### 🌐 Language | 语言 | 言語 | 언어

- 🇨🇳 [简体中文说明](./README.md)
- 🇺🇸 [English README](./README.en.md)
- 🇯🇵 [日本語の説明](./README.ja.md)
- 🇰🇷 [한국어 설명](./README.ko.md)

DCS Mission Translator is a tool designed to translate DCS mission files into Chinese, Japanese, or Korean.
It extracts text from mission files, uses AI to perform the translation, and then writes the translated content back into the original mission file.

### 📝 Other Supported Languages (no separate README)
- 🇪🇸 Español
- 🇫🇷 Français
- 🇩🇪 Deutsch
- 🇮🇹 Italiano
- 🇳🇱 Nederlands
- 🇵🇱 Polski
- 🇸🇪 Svenska
- 🇳🇴 Norsk
- 🇩🇰 Dansk
- 🇷🇴 Română
- 🇨🇿 Čeština
- 🇭🇺 Magyar
- 🇧🇬 Български
- 🇺🇦 Українська
- 🇻🇳 Tiếng Việt
- 🇲🇾 Bahasa Melayu
- 🇬🇷 Ελληνικά
- 🇮🇱 עברית
- 🇸🇦 العربية

> ⚠️ Note: For these additional languages, the program supports translation,  
> but there is no separate README file. Use the English or Chinese README for instructions.

## Usage

### Installation

First, you need to install Java 17. You can download and install Java 17 from [here](https://www.oracle.com/java/technologies/downloads/#java17-windows).

Next, download the latest version of the mission translator. You can get the latest release from [here](https://github.com/leonchen83/miz-translator/releases/latest/download/miz-translator-release.zip). Extract it to `/path/to/miz-translator`.

### Configuration

In the `/path/to/miz-translator/conf` directory, there is a file named `trans.conf`. You can configure the translator using this file.

```properties
# Prompt for the AI; here "F/A 18" is an example. You can modify the aircraft type or campaign as needed.
hint=You are a translator. The following English texts are related to the F/A 18 fighter jet. Translate them into Simplified Chinese without using markdown formatting. Keep the original line breaks and do not add extra explanations. Preserve acronyms written in uppercase.

# Target language 
# language code + country code, e.g., zh-CN for Simplified Chinese, ja-JP for Japanese, ko-KR for Korean
language=zh-CN

# AI translators supported: deepseek, doubao, and openai
translator=deepseek

# API Key for the translator. If using deepseek or doubao, you must fill in your API Key here.
apikey=your-api-key

# Base URL for the Open API
baseurl=https://api.deepseek.com/v1

# AI model to use
model=deepseek-chat

# Temperature for the AI. Use a negative value for the default.
temperature=1.3

# Max tokens for the AI; default is 4096
maxTokens=4096

# Minimum text length to trigger translation; texts shorter than 12 characters will be kept as-is
minimumLength=12

# Phrases to exclude from translation. Use multiple filters, each starting with 'filters'
filters1=JAMMER COOLING
filters2=INSERT ON COURSE AUDIO

# Keys to exclude from translation. Use multiple keyFilters, each starting with 'keyFilters'
keyFilters1=DictKey_UnitName_
keyFilters2=DictKey_WptName_
keyFilters3=DictKey_GroupName_
keyFilters4=DictKey_ActionRadioText_
````

### Run

```shell
cd /path/to/miz-translator/bin
./trans -f /path/to/missions
```

Here, `/path/to/miz-translator/bin` is the path to the extracted folder, and `/path/to/missions` is the path to the mission file you want to translate.

### Step-by-Step Mode

```shell
# You can also run it step by step
cd /path/to/miz-translator/bin

# Extract text from the mission file into a JSON file
./trans -f /path/to/missions -d

# Translate the JSON file
./trans -f /path/to/missions -t

# Compress the translated text back into a .miz mission file
./trans -f /path/to/missions -c
```

### Voice Translation

Voice translation requires the installation of `edge-tts`,`faster-whisper` and `ffmpeg`.

```shell
# MacOS
brew install python
brew install ffmpeg
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
uvicorn bin.main:app --host 0.0.0.0 --port 8000

# Windows
winget install Python.Python.3.11
winget install -e --id BtbN.FFmpeg.LGPL.8.0
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
uvicorn bin.main:app --host 0.0.0.0 --port 8000

# verify install
$ edge-tts --version
$ ffmpeg -version
```

Voice translation first requires running the `trans` command to generate the translated voice text, then use the following command to generate the voice files and package them into the miz mission file.

```shell
cd /path/to/miz-translator/bin
./trans -f /path/to/missions
./trans-voice -f /path/to/missions
```

### Docker

```shell
# pull docker image
docker pull redisrdbcli/miz-translator:latest

# run text translation
docker run --rm \
  -v /path/to/trans.conf:/app/miz-translator/conf/trans.conf:ro \
  -v /path/to/miz:/tmp/miz-uploaded \
  miz-translator:latest \
  trans -f /tmp/miz-uploaded

# run voice translation
docker run --rm \
  -v /path/to/trans.conf:/app/miz-translator/conf/trans.conf:ro \
  -v /path/to/miz:/tmp/miz-uploaded \
  miz-translator:latest \
  trans-voice -f /tmp/miz-uploaded
  
# use environment variables instead of a config file
docker run --rm \
  -v /path/to/miz:/tmp/miz-uploaded \
  -e API_KEY="${api-key}" \
  -e LANG="{{languageCode-countryCode}}" \
  -e BASE_URL="https://api.deepseek.com/v1" \
  -e HINT="${hint}" \
  -e PROXY="http://proxy.example.com:8080" \
  miz-translator:latest \
  trans -f /tmp/miz-uploaded
  
docker run --rm \
  -v /path/to/miz:/tmp/miz-uploaded \
  -e API_KEY="${api-key}" \
  -e LANG="{{languageCode-countryCode}}" \
  -e BASE_URL="https://api.deepseek.com/v1" \
  -e HINT="${hint}" \
  -e PROXY="http://proxy.example.com:8080" \
  miz-translator:latest \
  trans-voice -f /tmp/miz-uploaded
  
# use local web service
docker run -d \
  --name miz-translator \
  -p 8000:8000 \
  -v /path/to/miz:/tmp/miz-uploaded \
  miz-translator:latest \
  uvicorn bin.main:app --host 0.0.0.0 --port 8000
```
