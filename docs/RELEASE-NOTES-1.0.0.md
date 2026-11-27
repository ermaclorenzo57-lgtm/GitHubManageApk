# MaxRepo v1.0.0 — release notes (prepared, not published)

Build. Manage. Ship. Anywhere.

My first public version brings GitHub repositories, build monitoring, APK downloads and a local developer workspace to Android.

- Repository management and GitHub Actions monitoring.
- APK package, checksum and signing compatibility inspection.
- Local Studio tools for source editing, project inspection and release signing.
- Classic or fine-grained GitHub token support and Android Keystore-backed credential storage.
- New original dog-and-folder launcher artwork, light and dark themes.

Requires Android 10+. GitHub operations require internet and appropriate permissions. This is an independent project, not affiliated with GitHub.

## Release assets

Last verified signed build: run 46029050038, code 10002. Security remediation candidate: code 10005, pending build, signing and release checks. Package com.lomipe.devhub. Publish the final verified candidate only, not the earlier build. Publish only the verified production APK and matching SHA256SUMS after passing release gates. Never distribute app-debug.apk as the official release.

## Before publication

Complete the security scans, device update test, main branch protection, license and privacy review. Verify the exact artifact and checksum and retain the permanent signing key. Do not publish a candidate that has unresolved security findings.

- Repository management expansion and optional support section (pending final validation).
