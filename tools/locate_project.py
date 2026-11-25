#!/usr/bin/env python3
"""Find the Android/Gradle project to build, so the workflow never needs editing for new project layouts.

Order of precedence:
  1. --project-dir (workflow input or repo variable LOMIPE_PROJECT_DIR)
  2. exactly one directory containing gradlew + settings.gradle(.kts) (depth <= 3)
  3. the NEWEST project ZIP (by git commit time, else mtime) in the repo root or uploads/ that contains such a project;
     it is extracted safely into .lomipe_src/
Prints `project_dir=<path>` (and writes it to $GITHUB_OUTPUT). Exits non-zero with a clear message when ambiguous.
"""
import argparse, os, subprocess, sys, zipfile

SKIP = {".git", "node_modules", ".lomipe_src", ".lomipe_tools", "build", ".gradle"}
MAX_FILES, MAX_BYTES = 20000, 1024 * 1024 * 1024


def is_project(d):
    return os.path.isfile(os.path.join(d, "gradlew")) and any(
        os.path.isfile(os.path.join(d, n)) for n in ("settings.gradle", "settings.gradle.kts"))


def find_projects(root, depth=3):
    out = []
    for cur, dirs, _ in os.walk(root):
        rel = os.path.relpath(cur, root)
        if rel != "." and rel.count(os.sep) + 1 > depth:
            dirs[:] = []
            continue
        dirs[:] = [d for d in dirs if d not in SKIP]
        if is_project(cur):
            out.append(cur)
            dirs[:] = []
    return sorted(out)


def commit_time(root, path):
    try:
        r = subprocess.run(["git", "log", "-1", "--format=%ct", "--", path], cwd=root, capture_output=True, text=True)
        if r.returncode == 0 and r.stdout.strip():
            return int(r.stdout.strip())
    except Exception:
        pass
    return int(os.path.getmtime(os.path.join(root, path)))


def safe_name(n):
    if not n or n.startswith("/") or "\\" in n or (len(n) > 1 and n[1] == ":"):
        return False
    return all(p not in ("..", ".") for p in n.rstrip("/").split("/"))


def zip_project_prefix(z):
    names = [i.filename for i in z.infolist()]
    sets = {n for n in names}
    for pre in [""] + sorted({n.split("/")[0] + "/" for n in names if "/" in n}):
        if pre + "gradlew" in sets and (pre + "settings.gradle" in sets or pre + "settings.gradle.kts" in sets):
            return pre
    return None


def extract(zip_path, dest):
    with zipfile.ZipFile(zip_path) as z:
        infos = z.infolist()
        if len(infos) > MAX_FILES:
            sys.exit(f"ERROR: {zip_path}: too many entries")
        total = 0
        seen = set()
        for i in infos:
            if not safe_name(i.filename):
                sys.exit(f"ERROR: {zip_path}: unsafe path in archive: {i.filename!r}")
            if i.filename.lower() in seen:
                sys.exit(f"ERROR: {zip_path}: duplicate entry {i.filename!r}")
            seen.add(i.filename.lower())
            total += i.file_size
        if total > MAX_BYTES:
            sys.exit(f"ERROR: {zip_path}: uncompressed size too large")
        prefix = zip_project_prefix(z)
        if prefix is None:
            return None
        base = os.path.realpath(dest)
        for i in infos:
            target = os.path.realpath(os.path.join(base, i.filename))
            if not (target == base or target.startswith(base + os.sep)):
                sys.exit(f"ERROR: path escape blocked: {i.filename!r}")
            if i.is_dir():
                os.makedirs(target, exist_ok=True)
                continue
            os.makedirs(os.path.dirname(target), exist_ok=True)
            with z.open(i) as src, open(target, "wb") as out:
                out.write(src.read())
        return os.path.join(dest, prefix.rstrip("/")) if prefix else dest


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=".")
    ap.add_argument("--project-dir", default="")
    a = ap.parse_args()
    root = os.path.abspath(a.root)

    if a.project_dir:
        d = os.path.join(root, a.project_dir)
        if not is_project(d):
            sys.exit(f"ERROR: '{a.project_dir}' has no gradlew + settings.gradle(.kts)")
        return emit(os.path.relpath(d, root))

    found = find_projects(root)
    if len(found) == 1:
        return emit(os.path.relpath(found[0], root))
    if len(found) > 1:
        sys.exit("ERROR: several Gradle projects found: " + ", ".join(os.path.relpath(f, root) for f in found) +
                 ". Set the repository variable LOMIPE_PROJECT_DIR (or the project_dir input) to choose one.")

    zips = []
    for folder in (".", "uploads"):
        p = os.path.join(root, folder)
        if os.path.isdir(p):
            for n in os.listdir(p):
                if n.lower().endswith(".zip") and os.path.isfile(os.path.join(p, n)):
                    rel = os.path.normpath(os.path.join(folder, n))
                    zips.append((commit_time(root, rel), rel))
    for _, rel in sorted(zips, reverse=True):
        dest = os.path.join(root, ".lomipe_src")
        os.makedirs(dest, exist_ok=True)
        res = extract(os.path.join(root, rel), dest)
        if res and is_project(res):
            print(f"Using newest project ZIP: {rel}", file=sys.stderr)
            return emit(os.path.relpath(res, root))
        print(f"Skipping {rel}: no Gradle project inside", file=sys.stderr)
    sys.exit("ERROR: no Gradle project (gradlew + settings.gradle[.kts]) and no project ZIP found in the repository.")


def emit(path):
    print(f"project_dir={path}")
    out = os.environ.get("GITHUB_OUTPUT")
    if out:
        with open(out, "a") as f:
            f.write(f"project_dir={path}\n")


if __name__ == "__main__":
    main()
