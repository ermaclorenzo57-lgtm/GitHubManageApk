#!/usr/bin/env python3
"""Apply the candidate overlay to the known v3 Android project before building.
Fail closed when the expected source changes; update this patch after source review.
"""
import argparse
from pathlib import Path

def replace_once(path, old, new):
    text = path.read_text(encoding="utf-8")
    if text.count(old) != 1:
        raise SystemExit("Unexpected source; review overlay: " + str(path))
    path.write_text(text.replace(old, new, 1), encoding="utf-8")

def prepare(root):
    app = root / "app/src/main"
    http = app / "java/com/lomipe/devhub/core/Http.kt"
    replace_once(http,
        'private val trustedHosts = setOf(URL(policy.apiBase).host, URL(policy.uploadsBase).host)',
        'private fun origin(u: URL) = Triple(u.protocol.lowercase(), u.host.lowercase(), if (u.port == -1) u.defaultPort else u.port)\n    private val trustedOrigins = setOf(origin(URL(policy.apiBase)), origin(URL(policy.uploadsBase)))')
    replace_once(http, 'private fun trusted(u: URL) = u.host in trustedHosts',
        'private fun trusted(u: URL) = origin(u) in trustedOrigins')
    replace_once(app / "res/values/strings.xml", ">Lomipe DevHub<", ">Lomile Dev Hub<")
    replace_once(app / "res/drawable/ic_launcher_foreground.xml",
        '<path android:fillColor="#FFFFFF" android:pathData="M34,30h12v38h22v10H34z" />',
        '<path android:fillColor="#75E6C2" android:pathData="M28,29h12v39h35v11H28z" />\n    <path android:strokeColor="#FFFFFF" android:strokeWidth="6" android:strokeLineJoin="round" android:pathData="M54,27l14,13l-14,13" />')
    manifest = (app / "AndroidManifest.xml").read_text(encoding="utf-8")
    if 'android:allowBackup="false"' not in manifest or 'android:usesCleartextTraffic="false"' not in manifest:
        raise SystemExit("Manifest security guard failed")
    from beta2 import apply
    apply(root)
    from phase3 import apply as apply_phase3
    apply_phase3(root)
    print("Public beta candidate overlay applied. Android tests are still required.")

if __name__ == "__main__":
    p = argparse.ArgumentParser()
    p.add_argument("project", type=Path)
    prepare(p.parse_args().project)
