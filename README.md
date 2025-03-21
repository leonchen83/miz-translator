# DCS 任务翻译器

## 介绍

DCS 任务翻译器是一个用于将 DCS 任务文件翻译为中文的工具。它可以将任务文件中的文本提取出来，然后将其翻译为中文，最后将翻译后的文本重新写入任务文件中。

## 使用方法

### 安装

首先，你需要安装Java 17。你可以在[这里](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)下载Java 17并安装。

然后，你需要下载任务翻译器的最新版本。你可以在[这里](https://github.com/leonchen83/miz-translator/releases/download/v1.0.1/miz-translator-release.zip)下载最新版本。并解压到`/path/to/miz-translator`

### 配置

在`/path/to/miz-translator/conf`文件夹中，有一个`trans.conf`文件。你可以在这个文件中配置翻译器的一些参数。

```properties
# 给AI的提示，这里的F/A 18是一个示例，你可以根据需要修改飞机类型或者战役类型
hint=你是一个翻译，下面是跟战斗机F/A 18相关的英语，翻译成简体中文，但不要使用markdown输出, 保持原文的换行格式，不要添加多余的解释。遇到全大写的缩略词保持缩略词原样。

# AI翻译器，目前支持deepseek和doubao以及openai 3种翻译器
translator=deepseek

# 翻译器的API Key，如果你使用的是deepseek或者doubao，你需要在这里填写你的API Key
apikey=your-api-key

# Open API的baseurl
baseurl=https://ark.cn-beijing.volces.com/api/v3

# 使用的AI模型
model=your-model

# 使用的AI的temperature, 如果为负数则使用默认值
temperature=0.5

# 使用AI的max_tokens, 默认4096
maxTokens=4096

# 最小翻译长度，小于12个字符不翻译保持原文
minimumLength=12

# 将想保持原文的话放到filters里，可以指定多个filters，但每个必须以filters作为开头
filters1=JAMMER COOLING
filters2=INSERT ON COURSE AUDIO

# 将不想翻译的key放到keyFilters里，可以指定多个keyFilters，但每个必须以keyFilters作为开头
keyFilters=DictKey_ActionRadioText_
```

### 运行

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

