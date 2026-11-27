# Development notes

The active source is assembled from the root `LomipeDevHub_v3.0_source.zip` and the overlays in `tools/`. The baseline ZIP is not a current APK.

| Path | Purpose |
| --- | --- |
| `.github/workflows/` | CI builds, emulator tests and manual production signing |
| `tools/` | Source preparation and verification |
| `docs/` | Product site, release and security documentation |
| `archive/legacy/` | Historical development files; not used in current builds |

Verified production candidate: [build 10002](https://github.com/ermaclorenzo57-lgtm/GitHubManageApk/actions/runs/46029050038), app version 1.0.0. Physical-device upgrade validation and the security review remain pending. Do not use debug APKs as production updates. Do not publish or change the permanent signing identity without owner authorization.

See [release checklist](RELEASE-CHECKLIST.md), [signing guide](MAXREPO-1.0-SIGNING.md) and [beta testing guide](BETA-TESTING.md).
