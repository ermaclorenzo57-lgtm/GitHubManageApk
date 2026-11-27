# MaxRepo Phase 3
Positioning: independent GitHub mobile workspace for Android developers.
Tagline: Build. Manage. Ship. Anywhere.
Five primary jobs: manage repositories; monitor Actions; download artifacts; quick developer tools; work from a phone.
Design: GitHub-inspired dark and light colors, thin borders, green/amber/red status tones, compact cards, five tabs, header settings. Original M logo. No GitHub affiliation or imitation of GitHub's logo.
Scope: dashboard, repositories, builds, files, Studio, onboarding, shared theme and settings header. Existing repository detail, logs, workflows and tool editors keep their behavior and inherit the new theme; their individual layouts are not fully redesigned yet. Settings grouping still needs a deeper pass.
Version 3.2.0-beta.1 / code 6; same app ID, storage, repository and release endpoint. Dark by default for new users; existing theme choice remains respected. Re-test updates once dedicated release repository exists.
Previous Beta 2 emulator API 29/35 checks succeeded; APK CI failed at JVM test compilation because AGP omitted jdk.httpserver. Phase 3 adds test-only API classpath preparation. CI and real-device verification are required before release.
