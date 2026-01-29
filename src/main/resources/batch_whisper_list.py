import sys
import json
from pathlib import Path
from faster_whisper import WhisperModel

def main():
    if len(sys.argv) < 4:
        print("usage: python batch_whisper_list.py <audio_dir> <file_list.json> <out.json>")
        sys.exit(1)

    audio_dir = Path(sys.argv[1])
    file_list = Path(sys.argv[2])
    out_file = Path(sys.argv[3])

    files = json.loads(file_list.read_text(encoding="utf-8"))

    model = WhisperModel(
        "large-v3",
        device="cpu",
        compute_type="int8"
    )

    result = {}

    for name in files:
        audio = audio_dir / name
        if not audio.exists():
            continue

        segments, _ = model.transcribe(
            str(audio),
            language="en",
            vad_filter=True
        )

        text = " ".join(s.text.strip() for s in segments)
        result[name] = text

    out_file.write_text(
        json.dumps(result, ensure_ascii=False, indent=2),
        encoding="utf-8"
    )

if __name__ == "__main__":
    main()