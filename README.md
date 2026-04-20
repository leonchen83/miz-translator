# DCS 任务翻译器

### 🌐 Language | 语言 | 言語 | 언어

- 🇨🇳 [简体中文说明](./README.md)
- 🇺🇸 [English README](./README.en.md)
- 🇯🇵 [日本語の説明](./README.ja.md)
- 🇰🇷 [한국어 설명](./README.ko.md)

### 📝 其他支持的语言（没有单独 README）
- 🇪🇸 西班牙语
- 🇫🇷 法语
- 🇩🇪 德语
- 🇮🇹 意大利语
- 🇳🇱 荷兰语
- 🇵🇱 波兰语
- 🇸🇪 瑞典语
- 🇳🇴 挪威语
- 🇩🇰 丹麦语
- 🇷🇴 罗马尼亚语
- 🇨🇿 捷克语
- 🇭🇺 匈牙利语
- 🇧🇬 保加利亚语
- 🇺🇦 乌克兰语
- 🇻🇳 越南语
- 🇲🇾 马来语
- 🇬🇷 希腊语
- 🇮🇱 希伯来语
- 🇸🇦 阿拉伯语

> ⚠️ 注意：这些额外语言程序支持翻译，但没有单独的 README 文件。请参考英文或中文 README 获取使用说明。

## 介绍

DCS 任务翻译器是一个用于将 DCS 任务文件翻译为中日韩文的工具。它可以将任务文件中的文本提取出来，然后使用AI将其翻译为中日韩文，最后将翻译后的文本重新写入任务文件中。

## 使用方法

### 安装

下载任务翻译器的最新版本。你可以在[这里](https://github.com/leonchen83/miz-translator/releases/latest/download/miz-translator-release.zip)下载最新版本。并解压到`/path/to/miz-translator`

### 运行

双击打开`/path/to/miz-translator/bin/transgui.cmd`
选择 Miz 文件夹, 输入 `APIkey`, 点击翻译. 对于有中文补丁的战役, 不用输入 `APIkey`, 点击中文补丁即可

### 配置

在`/path/to/miz-translator/conf`文件夹中，有一个`trans.conf`文件。你可以在这个文件中配置翻译器的一些参数。

```properties
# 给AI的提示，这里的F/A 18是一个示例，你可以根据需要修改飞机类型或者战役类型
hint=你是一个翻译，下面是跟战斗机F/A 18相关的英语，翻译成简体中文，但不要使用markdown输出, 保持原文的换行格式，不要添加多余的解释。遇到全大写的缩略词保持缩略词原样

# AI翻译器，目前支持deepseek和doubao以及openai 3种翻译器
translator=deepseek

# 翻译器的API Key，如果你使用的是deepseek或者doubao，你需要在这里填写你的API Key
apikey=your-api-key

# Open API的baseurl
baseurl=https://api.deepseek.com/v1

# 使用的AI模型
model=deepseek-chat

# 使用的AI的temperature, 如果为负数则使用默认值
temperature=1.3

# 使用AI的max_tokens, 默认4096
maxTokens=4096

# 最小翻译长度，小于12个字符不翻译保持原文
minimumLength=12

# 将想保持原文的话放到filters里，可以指定多个filters，但每个必须以filters作为开头
filters1=JAMMER COOLING
filters2=INSERT ON COURSE AUDIO

# 将不想翻译的key放到keyFilters里，可以指定多个keyFilters，但每个必须以keyFilters作为开头
keyFilters1=DictKey_UnitName_
keyFilters2=DictKey_WptName_
keyFilters3=DictKey_GroupName_
keyFilters4=DictKey_ActionRadioText_
```

### 命令行运行

```shell
cd /path/to/miz-translator/bin
./trans -f /path/to/missions
```

`/path/to/miz-translator/bin`是你解压后的文件夹路径，`/path/to/missions`是你要翻译的任务文件路径。

### 分步运行

```shell
# 也可以分步运行
cd /path/to/miz-translator/bin

# 解压出任务文件中的文本为一个json文件
./trans -f /path/to/missions -d

# 翻译json文件
./trans -f /path/to/missions -t

# 将翻译后的文本压缩成miz任务
./trans -f /path/to/missions -c
```
### 语音翻译

语音翻译需要先安装`edge-tts`,`faster-whisper`以及`ffmpeg`. 注意: 语音翻译需要梯子

```shell
# MacOS
brew install python
brew install ffmpeg
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

# Windows
winget install Python.Python.3.11
winget install -e --id BtbN.FFmpeg.LGPL.8.0
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

# verify install
$ edge-tts --version
$ ffmpeg -version
```

语音翻译首先需要执行`trans`命令生成翻译后的语音文本, 然后使用下面的命令生成语音文件, 并打包进miz任务文件中

```shell
cd /path/to/miz-translator/bin
./trans -f /path/to/missions
./trans-voice -f /path/to/missions

# http代理
./trans-voice -f /path/to/missions --proxy http://proxy.example.com:8080
```
