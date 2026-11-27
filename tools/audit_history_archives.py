"""Copy every unique historical ZIP blob for offline, redacted secret scanning."""
import subprocess
import sys
from pathlib import Path

def git(*args):
    return subprocess.check_output(["git", *args])
out = Path(sys.argv[1])
out.mkdir(parents=True, exist_ok=True)
seen = set()
total = 0
for commit in git("rev-list", "--all").decode().splitlines():
    for item in git("ls-tree", "-r", "-z", commit).split(b"\0"):
        if not item:
            continue
        meta, name = item.split(b"\t", 1)
        mode, kind, sha = meta.decode().split()
        if kind != "blob" or not name.lower().endswith(b".zip") or sha in seen:
            continue
        size = int(git("cat-file", "-s", sha))
        total += size
        if size > 100 * 1024 * 1024 or total > 512 * 1024 * 1024:
            raise SystemExit("Historical ZIP audit exceeded size limit; manual review required")
        (out / (sha + ".zip")).write_bytes(git("cat-file", "blob", sha))
        seen.add(sha)
if not seen:
    raise SystemExit("No historical ZIP blobs found")
print("Historical ZIP blobs queued:", len(seen))
