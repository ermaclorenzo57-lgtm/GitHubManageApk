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
    # Approved independent dog-and-folder artwork, supplied by the maintainer.
    # Keep the complete design inside the adaptive launcher icon's safe area.
    import hashlib
    import shutil
    artwork = Path(__file__).parent / "phase3" / "maxrepo_launcher_art.webp"
    if hashlib.sha256(artwork.read_bytes()).hexdigest() != "c7b25955d669abcea039c1aa772f0dcf89c0063710f453e534dd0fe1d3e5e601":
        raise SystemExit("Approved launcher artwork checksum mismatch")
    drawable = app / "res/drawable-nodpi"
    drawable.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(artwork, drawable / "maxrepo_launcher_art.webp")
    (app / "res/drawable/ic_launcher_foreground.xml").write_text(
        '<inset xmlns:android="http://schemas.android.com/apk/res/android" '
        'android:drawable="@drawable/maxrepo_launcher_art" android:inset="16.6667%" />\n',
        encoding="utf-8")
    (app / "res/mipmap-anydpi-v26/ic_launcher.xml").write_text(
        '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
        '    <background android:drawable="@drawable/maxrepo_launcher_background" />\n'
        '    <foreground android:drawable="@drawable/ic_launcher_foreground" />\n'
        '</adaptive-icon>\n', encoding="utf-8")
    (app / "res/drawable/maxrepo_launcher_background.xml").write_text(
        '<shape xmlns:android="http://schemas.android.com/apk/res/android">\n'
        '    <gradient android:startColor="#113D80" android:endColor="#36A2D4" android:angle="90" />\n'
        '</shape>\n', encoding="utf-8")
    print("Public beta candidate overlay applied. Android tests are still required.")

if __name__ == "__main__":
    p = argparse.ArgumentParser()
    p.add_argument("project", type=Path)
    prepare(p.parse_args().project)
