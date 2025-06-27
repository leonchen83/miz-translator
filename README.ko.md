# DCS 미션 번역기

## 소개

### 🌐 Language | 语言 | 言語 | 언어

- 🇨🇳 [简体中文说明](./README.md)
- 🇺🇸 [English README](./README.en.md)
- 🇯🇵 [日本語の説明](./README.ja.md)
- 🇰🇷 [한국어 설명](./README.ko.md)

DCS 미션 번역기는 DCS 미션 파일을 중국어로 번역하기 위한 도구입니다. 이 도구는 미션 파일에서 텍스트를 추출한 후, AI를 사용하여 번역하고, 번역된 내용을 다시 미션 파일에 작성합니다.

## 사용 방법

### 설치

먼저 Java 17을 설치해야 합니다. 아래 링크에서 Java 17을 다운로드하여 설치하세요.  
[Java 17 다운로드](https://www.oracle.com/java/technologies/downloads/#java17-windows)

그런 다음, 최신 버전의 미션 번역기를 다운로드해야 합니다.  
아래 링크에서 최신 버전을 다운로드하고 `/path/to/miz-translator` 경로에 압축을 해제하세요.  
[최신 버전 다운로드](https://github.com/leonchen83/miz-translator/releases/download/v1.0.4/miz-translator-release.zip)

### 설정

`/path/to/miz-translator/conf` 폴더에 `trans.conf` 파일이 있습니다. 이 파일에서 번역기의 여러 설정을 구성할 수 있습니다.

```properties
# AI에게 전달되는 프롬프트입니다. F/A 18은 예시이며, 필요에 따라 항공기나 캠페인 종류를 수정할 수 있습니다.
hint=당신은 번역가입니다. 아래는 F/A 18 전투기에 관련된 영어 문장입니다. 이를 간체 중국어로 번역하세요. Markdown 형식을 사용하지 말고, 원래의 줄바꿈을 유지하며 불필요한 설명은 추가하지 마세요. 모든 대문자 약어는 그대로 유지하세요.

# 지원되는 AI 번역기: deepseek, doubao, openai
translator=deepseek

# 번역기에 사용할 API 키. deepseek 또는 doubao를 사용하는 경우 여기에 API 키를 입력하세요.
apikey=your-api-key

# Open API의 base URL
baseurl=deepseek-chat

# 사용할 AI 모델
model=your-model

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