# bin/main.py
from fastapi import FastAPI, Form, UploadFile, File
from fastapi.responses import HTMLResponse, StreamingResponse
from fastapi.responses import FileResponse
from pathlib import Path
from fastapi import HTTPException
from datetime import datetime
import shutil
import asyncio
import zipfile

app = FastAPI(title="MIZ Translator Web")

UPLOAD_ROOT = Path("/tmp/miz-uploaded")
UPLOAD_ROOT.mkdir(exist_ok=True)

DEFAULT_HINT = (
    "你是一个军事方面的翻译，下面是 DCS World 中战役或任务的英语文本，"
    "这个文本包含无线电对话、战役或任务的简介等等。"
    "请翻译成 {{lang}}，不要添加多余的解释以及补充出多余的对话，"
    "不要使用 markdown 输出，保持原文的换行格式。"
)

LANG_DISPLAY = {
    "zh-CN": "简体中文",
    "ja-JP": "日本語",
    "ko-KR": "한국어",
    "es-ES": "Español",
    "fr-FR": "Français",
    "de-DE": "Deutsch",
    "it-IT": "Italiano",
    "nl-NL": "Nederlands",
    "pl-PL": "Polski",
    "sv-SE": "Svenska",
    "no-NO": "Norsk",
    "da-DK": "Dansk",
    "ro-RO": "Română",
    "cs-CZ": "Čeština",
    "hu-HU": "Magyar",
    "bg-BG": "Български",
    "uk-UA": "Українська",
    "vi-VN": "Tiếng Việt",
    "ms-MY": "Bahasa Melayu",
    "el-GR": "Ελληνικά",
    "he-IL": "עברית",
    "ar-SA": "العربية",
}

# 首页表单
@app.get("/", response_class=HTMLResponse)
async def index():
    return f"""
    <html>
    <head>
        <title>MIZ Translator</title>
        <style>
            body {{
                font-family: Arial, sans-serif;
                padding: 20px;
                background: #f7f8fa;
            }}
            h2 {{
                text-align: center;
            }}
            form {{
                max-width: 900px;
                margin: auto;
                background: #fff;
                padding: 20px;
                border-radius: 10px;
                box-shadow: 0 2px 12px rgba(0,0,0,0.1);
            }}
            .form-row {{
                display: flex;
                align-items: center;
                margin-bottom: 12px;
                flex-wrap: wrap;
            }}
            .form-row label {{
                width: 140px;
                margin-right: 12px;
                font-weight: bold;
            }}
            .form-row input[type="text"],
            .form-row select,
            .form-row textarea {{
                flex: 1;
                padding: 6px 8px;
                border-radius: 4px;
                border: 1px solid #ccc;
                font-size: 14px;
            }}
            .form-row textarea {{
                resize: vertical;
            }}
            .form-row-inline {{
                display: flex;
                align-items: center;
                gap: 20px;
            }}
            #log {{
                width: 100%;
                min-height: 400px;
                overflow: auto;
                border: 1px solid #ccc;
                padding: 8px;
                box-sizing: border-box;
                background: #1e1e1e;
                color: #dcdcdc;
                font-family: Consolas, monospace;
            }}
            button {{
                padding: 8px 16px;
                margin-right: 12px;
                border: none;
                border-radius: 4px;
                background-color: #007bff;
                color: white;
                cursor: pointer;
                font-size: 14px;
            }}
            button:hover {{
                background-color: #0056b3;
            }}
        </style>
        <script>
        async function translateForm(event, formId, preId) {{
            event.preventDefault();
            const form = document.getElementById(formId);
            const pre = document.getElementById(preId);
            const btn = document.getElementById("downloadBtn");

            pre.textContent = "";
            btn.style.display = "none";
            btn.dataset.zip = "";

            const formData = new FormData(form);
            const response = await fetch(form.action, {{
                method: "POST",
                body: formData
            }});

            const reader = response.body.getReader();
            const decoder = new TextDecoder();

            let zipName = null;

            while (true) {{
                const {{ done, value }} = await reader.read();
                if (done) break;

                const text = decoder.decode(value);
                pre.textContent += text;
                pre.scrollTop = pre.scrollHeight;

                const m = text.match(/\\[DOWNLOAD\\] \\/download\\/([a-zA-Z0-9._-]+)/);
                if (m) {{
                    zipName = m[1];
                    btn.dataset.zip = zipName;
                    btn.style.display = "inline";
                }}
            }}
        }}

        function downloadZip() {{
            const btn = document.getElementById("downloadBtn");
            const zipName = btn.dataset.zip;
            window.location.href = `/download/${{zipName}}`;
        }}
        </script>
    </head>
    <body>
        <h2>MIZ Translator</h2>
        <form id="translateForm" action="/translate" enctype="multipart/form-data" method="post" onsubmit="translateForm(event,'translateForm','log')">
            <div class="form-row">
                <label>API_KEY:</label>
                <input type="text" name="api_key" required />
            </div>
            <div class="form-row">
                <label>BASE URL:</label>
                <input type="text" name="base_url" value="https://api.deepseek.com/v1" />
            </div>
            <div class="form-row">
                <label>语言 (LANG):</label>
                <select name="lang">
                {''.join([
                    f'<option value="{code}" {"selected" if code=="zh-CN" else ""}>{name}</option>'
                    for code, name in LANG_DISPLAY.items()
                ])}
                </select>
            </div>
            <div class="form-row">
                <label>模型 (MODEL):</label>
                <input type="text" name="model" value="deepseek-reasoner" />
            </div>
            <div class="form-row">
                <label>Proxy (可选):</label>
                <input type="text" name="proxy" />
            </div>
            <div class="form-row">
                <label>保留原文:</label>
                <input type="checkbox" name="original" />
            </div>
            <div class="form-row">
                <label>生成语音:</label>
                <input type="checkbox" name="voice" />
            </div>
            <div class="form-row">
                <label>提示词:</label>
                <textarea name="hint" rows="4">{DEFAULT_HINT}</textarea>
            </div>
            <div class="form-row">
                <label>上传 MIZ 文件:</label>
                <input type="file" name="miz_files" webkitdirectory directory multiple required />
            </div>
            <div class="form-row">
                <label>日志输出:</label>
                <pre id="log"></pre>
            </div>
            <div class="form-row">
                <button type="submit">翻译</button>
                <button id="downloadBtn" style="display:none;" type="button" onclick="downloadZip()">下载结果 ZIP</button>
            </div>
        </form>
    </body>
    </html>
    """
    
def zip_directory(src_dir: Path) -> Path:
    timestamp = datetime.now().strftime("%Y%m%d%H%M%S")
    zip_path = UPLOAD_ROOT / f"{src_dir.name}_{timestamp}.zip"

    with zipfile.ZipFile(zip_path, "w", zipfile.ZIP_DEFLATED) as z:
        for p in src_dir.rglob("*"):
            if p.is_file():
                # 保留目录结构
                z.write(p, arcname=p.relative_to(src_dir))

    return zip_path

def save_upload_files(upload_files: list[UploadFile]) -> Path:
    if not upload_files:
        raise ValueError("没有上传文件")

    first = Path(upload_files[0].filename)
    if len(first.parts) < 2:
        raise ValueError("请使用目录方式上传")

    root_dir_name = first.parts[0]
    target_dir = UPLOAD_ROOT / root_dir_name
    target_dir.mkdir(parents=True, exist_ok=True)

    saved_miz = 0
    saved_json = 0

    for f in upload_files:
        rel = Path(f.filename)

        # 只接受 .miz 或 .json
        if not (rel.name.lower().endswith(".miz") or rel.name.lower().endswith(".json")):
            continue

        dest = target_dir / rel.name
        dest.parent.mkdir(parents=True, exist_ok=True)  # 确保子目录存在
        with dest.open("wb") as out:
            shutil.copyfileobj(f.file, out)

        if rel.name.lower().endswith(".miz"):
            saved_miz += 1
        else:
            saved_json += 1

    if saved_miz == 0:
        raise ValueError("目录中未找到任何 .miz 文件")

    print(f"[DEBUG] Uploaded {saved_miz} .miz files and {saved_json} .json files to {target_dir}")
    return target_dir

# 生成 trans.conf
def generate_conf(tmp_dir: Path, api_key, base_url, lang, model, hint, proxy):
    conf_path = tmp_dir / "trans.conf"
    with conf_path.open("w", encoding="utf-8") as f:
        f.write(f"apiKey={api_key}\n")
        f.write(f"baseURL={base_url}\n")
        f.write(f"language={lang}\n")
        f.write(f"model={model}\n")
        f.write(f"hint={hint}\n")
        f.write(f"temperature=1.3\n")
        f.write(f"ttsProxy={proxy or ''}\n")
        f.write(f"ttsService=edge-tts\n")
    return conf_path

# 异步执行命令，实时输出
async def run_command(cmd: list[str]):
    proc = await asyncio.create_subprocess_exec(
        *cmd,
        stdout=asyncio.subprocess.PIPE,
        stderr=asyncio.subprocess.STDOUT
    )
    while True:
        line = await proc.stdout.readline()
        if not line:
            break
        # 注意这里line是bytes，需要解码
        yield line.decode("utf-8", errors="ignore")
    await proc.wait()

# 翻译接口
@app.post("/translate")
async def translate(
    api_key: str = Form(...),
    base_url: str = Form("https://api.deepseek.com/v1"),
    lang: str = Form("zh-CN"),
    model: str = Form("deepseek-reasoner"),
    proxy: str = Form(None),
    original: bool = Form(False),
    hint: str = Form(DEFAULT_HINT),
    voice: bool = Form(False),
    miz_files: list[UploadFile] = File(...)
):
    lang_name = LANG_DISPLAY.get(lang, lang)
    if "{{lang}}" in hint:
        hint = hint.replace("{{lang}}", lang_name)
    tmp_dir = save_upload_files(miz_files)
    conf_path = generate_conf(tmp_dir, api_key, base_url, lang, model, hint, proxy)

    async def event_stream():
        cmd_trans = ["trans", "-f", str(tmp_dir), "-s", str(conf_path)]
        if original:
            cmd_trans.append("-o")
        yield f"$ {' '.join(cmd_trans)}\n"
        async for line in run_command(cmd_trans):
            yield line

        if voice:
            cmd_voice = ["trans-voice", "-f", str(tmp_dir), "-s", str(conf_path)]
            if proxy:
                cmd_voice.extend(["-p", proxy])
            yield f"$ {' '.join(cmd_voice)}\n"
            async for line in run_command(cmd_voice):
                yield line
        yield "\n[INFO] Packaging result...\n"
        zip_path = zip_directory(tmp_dir)
        yield f"[DONE] Package ready: {zip_path.name}\n"
        yield f"[DOWNLOAD] /download/{zip_path.stem}\n"
    return StreamingResponse(event_stream(), media_type="text/plain")
    
@app.get("/download/{zip_name}")
async def download(zip_name: str):
    zip_path = UPLOAD_ROOT / f"{zip_name}.zip"

    if not zip_path.exists():
        raise HTTPException(status_code=404, detail="ZIP not found")

    return FileResponse(
        zip_path,
        media_type="application/zip",
        filename=f"{zip_name}.zip"
    )
