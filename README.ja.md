# DCS ミッション翻訳ツール

## はじめに

### 🌐 Language | 语言 | 言語 | 언어

- 🇨🇳 [简体中文说明](./README.md)
- 🇺🇸 [English README](./README.en.md)
- 🇯🇵 [日本語の説明](./README.ja.md)
- 🇰🇷 [한국어 설명](./README.ko.md)

DCS ミッション翻訳ツールは、DCS のミッションファイルを中国語に翻訳するためのツールです。ミッションファイル内のテキストを抽出し、AI を使って中国語に翻訳し、翻訳後のテキストをミッションファイルに再書き込みします。

## 使い方

### インストール

まず、Java 17 をインストールする必要があります。以下のリンクから Java 17 をダウンロードしてインストールしてください。  
[Java 17 をダウンロード](https://www.oracle.com/java/technologies/downloads/#java17-windows)

次に、ミッション翻訳ツールの最新版をダウンロードします。以下のリンクから最新版を取得し、`/path/to/miz-translator` に解凍してください。  
[最新バージョンをダウンロード](https://github.com/leonchen83/miz-translator/releases/download/v1.0.4/miz-translator-release.zip)

### 設定

`/path/to/miz-translator/conf` フォルダには、`trans.conf` という設定ファイルがあります。このファイルで翻訳ツールのパラメータを設定できます。

```properties
# AI へのプロンプト。ここでは「F/A 18」は一例です。必要に応じて航空機の種類や作戦を変更してください。
hint=あなたは翻訳者です。以下は戦闘機 F/A 18 に関する英語のテキストです。これを簡体字中国語に翻訳してください。Markdown は使用せず、元の改行形式を保ち、不要な解説は加えないでください。すべて大文字の略語はそのまま保持してください。

# 利用可能な AI 翻訳エンジン：deepseek、doubao、openai
translator=deepseek

# 翻訳エンジンの API Key。deepseek または doubao を使用する場合は、ここに API Key を入力してください。
apikey=your-api-key

# Open API のベース URL
baseurl=deepseek-chat

# 使用する AI モデル
model=your-model

# AI の temperature 設定。負の値を設定するとデフォルトが使用されます。
temperature=1.3

# AI の max_tokens 設定。デフォルトは 4096。
maxTokens=4096

# 最小翻訳文字数。12文字未満のテキストは翻訳されず、元のままとなります。
minimumLength=12

# 翻訳対象外にしたい語句は filters に記述。複数指定可能で、各フィルタは "filters" で始める必要があります。
filters1=JAMMER COOLING
filters2=INSERT ON COURSE AUDIO

# 翻訳したくないキーを keyFilters に指定。複数指定可能で、各キーは "keyFilters" で始める必要があります。
keyFilters1=DictKey_UnitName_
keyFilters2=DictKey_WptName_
keyFilters3=DictKey_GroupName_
keyFilters4=DictKey_ActionRadioText_
````

### 実行

```shell
cd /path/to/miz-translator/bin
./trans -f /path/to/missions
```

ここで `/path/to/miz-translator/bin` は解凍したフォルダのパス、`/path/to/missions` は翻訳したいミッションファイルのパスです。

### ステップごとの実行

```shell
# ステップ実行も可能です
cd /path/to/miz-translator/bin

# ミッションファイルからテキストを抽出し、JSON ファイルに出力
./trans -f /path/to/missions -d

# JSON ファイルを翻訳
./trans -f /path/to/missions -t

# 翻訳後のテキストを .miz ミッションファイルに再圧縮
./trans -f /path/to/missions -c
```