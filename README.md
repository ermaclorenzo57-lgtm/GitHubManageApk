# MaxRepo

**Build. Manage. Ship. Anywhere.**

An independent GitHub mobile workspace for Android developers. Manage repositories, monitor Actions, download APK artifacts, access local developer tools, and work from your phone. Not affiliated with GitHub.

## MaxRepo 1.0.0

First public release candidate, Android 10+ (API 29). Internet is needed for GitHub operations; local Studio tools can work without an account.

The production APK was built and verified in [signing run 46023087566](https://github.com/ermaclorenzo57-lgtm/GitHubManageApk/actions/runs/46023087566) (commit `917105a`, internal version code 10001). This is a test artifact, not an official published release. Public distribution remains pending owner approval, physical-device upgrade testing, security review and release documentation.

- Repositories, branches, files and workflows.
- Actions monitoring, build logs and artifact downloads.
- Local developer Studio, including APK analysis and signing-key generation.
- GitHub personal access tokens: classic or fine-grained, with only the access needed.

Do not post tokens, private signing keys, passwords, or private project information in issues.

## Repository structure

| Path | Purpose |
| --- | --- |
| `LomipeDevHub_v3.0_source.zip` | Required baseline Android source; not a downloadable current APK. |
| `tools/` | Source overlays, preparation and verification scripts. |
| `.github/workflows/` | Development builds, emulator tests and manual production signing. |
| `docs/` | Release readiness, privacy/security guidance, signing and testing. |
| `archive/legacy/` | Historical development material; not used for current builds. |

The current source is assembled from the baseline archive and overlays. The repository is public; a separate releases-only repository is planned but has not been created.

[Changelog](CHANGELOG.md) · [Security](SECURITY.md) · [Privacy draft](PRIVACY.md) · [Release checklist](docs/RELEASE-CHECKLIST.md) · [Signing and migration](docs/MAXREPO-1.0-SIGNING.md)
