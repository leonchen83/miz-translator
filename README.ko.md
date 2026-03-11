# DCS 미션 번역기

## 소개

### 🌐 Language | 语言 | 言語 | 언어

- 🇨🇳 [简体中文说明](./README.md)
- 🇺🇸 [English README](./README.en.md)
- 🇯🇵 [日本語の説明](./README.ja.md)
- 🇰🇷 [한국어 설명](./README.ko.md)

DCS 미션 번역기는 DCS 미션 파일을 중국어, 일본어 또는 한국어로 번역하는 도구입니다.
미션 파일에서 텍스트를 추출한 후 AI를 사용하여 번역하고, 번역된 내용을 원본 미션 파일에 다시 기록합니다.

### 📝 기타 지원 언어 (별도 README 없음)
- 🇪🇸 스페인어
- 🇫🇷 프랑스어
- 🇩🇪 독일어
- 🇮🇹 이탈리아어
- 🇳🇱 네덜란드어
- 🇵🇱 폴란드어
- 🇸🇪 스웨덴어
- 🇳🇴 노르웨이어
- 🇩🇰 덴마크어
- 🇷🇴 루마니아어
- 🇨🇿 체코어
- 🇭🇺 헝가리어
- 🇧🇬 불가리아어
- 🇺🇦 우크라이나어
- 🇻🇳 베트남어
- 🇲🇾 말레이어
- 🇬🇷 그리스어
- 🇮🇱 히브리어
- 🇸🇦 아랍어

> ⚠️ 참고: 위 추가 언어들은 프로그램에서 번역을 지원하지만, 별도의 README 파일은 없습니다. 사용법은 영어 또는 중국어 README를 참고하세요.

## 사용 방법

### 설치

먼저 Java 17을 설치해야 합니다. 아래 링크에서 Java 17을 다운로드하여 설치하세요.  
[Java 17 다운로드](https://aka.ms/download-jdk/microsoft-jdk-21.0.10-windows-x64.exe)

그런 다음, 최신 버전의 미션 번역기를 다운로드해야 합니다.  
아래 링크에서 최신 버전을 다운로드하고 `/path/to/miz-translator` 경로에 압축을 해제하세요.  
[최신 버전 다운로드](https://github.com/leonchen83/miz-translator/releases/latest/download/miz-translator-release.zip)

### 설정

`/path/to/miz-translator/conf` 폴더에 `trans.conf` 파일이 있습니다. 이 파일에서 번역기의 여러 설정을 구성할 수 있습니다.

```properties
# AI에게 전달되는 프롬프트입니다. F/A 18은 예시이며, 필요에 따라 항공기나 캠페인 종류를 수정할 수 있습니다.
hint="당신은 번역가입니다. 아래는 전투기 F/A 18과 관련된 영어 내용입니다. 한국어로 번역해 주세요. 단, markdown을 사용하지 말고 원문의 줄바꿈 형식을 유지하며, 불필요한 설명은 추가하지 마세요. 모두 대문자인 약어는 원래 형태로 유지해 주세요

language=ko-KR

# 지원되는 AI 번역기: deepseek, doubao, openai
translator=deepseek

# 번역기에 사용할 API 키. deepseek 또는 doubao를 사용하는 경우 여기에 API 키를 입력하세요.
apikey=your-api-key

# Open API의 base URL
baseurl=https://api.deepseek.com/v1

# 사용할 AI 모델
model=deepseek-chat

# AI의 temperature 설정. 음수를 입력하면 기본값이 사용됩니다.
temperature=1.3

# AI의 max_tokens 설정. 기본값은 4096입니다.
maxTokens=4096

# 최소 번역 길이. 12자 미만의 텍스트는 번역하지 않고 원문을 유지합니다.
minimumLength=12

# 번역에서 제외하고 싶은 단어 또는 구문은 filters로 지정하세요. 여러 개 지정 가능하며, 각 항목은 filters로 시작해야 합니다.
filters1=JAMMER COOLING
filters2=INSERT ON COURSE AUDIO

# 번역하지 않을 키를 keyFilters에 지정하세요. 여러 개 지정 가능하며, 각 항목은 keyFilters로 시작해야 합니다.
keyFilters1=DictKey_UnitName_
keyFilters2=DictKey_WptName_
keyFilters3=DictKey_GroupName_
keyFilters4=DictKey_ActionRadioText_
````

### 실행

```shell
cd /path/to/miz-translator/bin
./trans -f /path/to/missions
```

`/path/to/miz-translator/bin`는 압축을 푼 디렉토리 경로이며, `/path/to/missions`는 번역할 미션 파일의 경로입니다.

### 단계별 실행

```shell
# 단계별 실행도 가능합니다
cd /path/to/miz-translator/bin

# 미션 파일에서 텍스트를 추출하여 JSON 파일로 저장
./trans -f /path/to/missions -d

# JSON 파일을 번역
./trans -f /path/to/missions -t

# 번역된 텍스트를 다시 .miz 미션 파일로 압축
./trans -f /path/to/missions -c
```

### 음성 번역

음성 번역을 수행하려면 먼저 `edge-tts`,`faster-whisper`와 `ffmpeg`를 설치해야 합니다.

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

음성 번역에서는 먼저 `trans` 명령을 실행하여 번역된 텍스트를 생성한 후, 아래 명령을 사용하여 음성 파일을 생성하고 miz 미션 파일에 패키징합니다.

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
  -e LANG="ko-KR" \
  -e BASE_URL="https://api.deepseek.com/v1" \
  -e HINT="${hint}" \
  -e PROXY="http://proxy.example.com:8080" \
  miz-translator:latest \
  trans -f /tmp/miz-uploaded
  
docker run --rm \
  -v /path/to/miz:/tmp/miz-uploaded \
  -e API_KEY="${api-key}" \
  -e LANG="ko-KR" \
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
