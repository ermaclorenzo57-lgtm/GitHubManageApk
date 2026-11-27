# Changelog

## MaxRepo 1.0.0 — official release candidate
- Public version reset; internal version code raised to 10000, preserving package identity.
- Neutral classic and fine-grained GitHub token guidance.
- Protected manual production-signing workflow, certificate fingerprint verification and APK checksum.
- Production key setup, physical-device update testing and publishing remain pending.


## MaxRepo 3.2.0-beta.1 — Phase 3 candidate
- Rebrand display name and launcher art; preserve application ID and storage compatibility.
- GitHub-inspired dark/light palettes, five aligned navigation tabs, settings in header, dashboard, repo cards, filtered Actions cards, grouped Studio, files empty state.
- Independent GitHub mobile workspace positioning. No official affiliation implied.
- Address JVM test compile failure by exposing the JDK HTTP server API only to the JVM test classpath; rerun all checks.
- Actual device screenshots and production distribution still pending.

## 3.1.0-beta.2 — candidate, not released
- Replace letter placeholders with equal navigation tabs and single-line labels; use Files for Downloads.
- Align Studio tool cards, repo rows and screen headings; replace persistent messages with snackbars.
- Add signed-out onboarding and public release checks (manual install, no GitHub credential transmitted).
- Cancel outstanding account operations on switching/signing out; clear old account presentation state and reject missing credentials.
- Add release version/origin unit tests and Android emulator layout smoke tests for API 29 and 35.
- Physical device, signing, historical secret and end-to-end security verification remain pending.

## 3.1.0-beta.1 — proposed, not released
- Prepare release branding and origin-bound GitHub authentication checks.
- Add beta documentation, release checklist and landing page.
- Plan reproducible signed APK distribution and checksum verification.
- Pending: Android build and regression results, device tests, signing, update detection and release-only repository.

## 3.0.0
Existing Android source archive. See its UPGRADE-v3.md and validation reports; prior reports are not evidence that the new beta has passed.
