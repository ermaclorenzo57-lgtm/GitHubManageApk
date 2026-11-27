# Public beta release gate

Version: proposed 3.1.0-beta.1; version code 4 (existing archive reports 3.0.0/code 3). Confirm no installed test build has a higher code before release.

## 1. Security
- [ ] Review all current source and Git history for secrets, including older ZIPs; rotate anything exposed.
- [ ] Verify token encryption, account switching, logout, redacted logs and cache isolation.
- [ ] Verify host/scheme/port credential restrictions and redirect tests.
- [ ] Review dependencies and Gradle wrapper integrity.
- [ ] Verify permissions, backup rules and user confirmation before uploading, deleting or installing.
- [ ] Review third-party licenses and redistribution rights.

## 2. Branding / usability
- [ ] Review updated name/icon, onboarding, permission prompts and all screens on a small phone.
- [ ] Verify error and offline states; ensure destructive actions require deliberate confirmation.
- [ ] Capture real screenshots; do not substitute mock screenshots for the shipped app.

## 3. Release infrastructure
- [ ] Create a private source repository and separate public releases-only repository via GitHub settings; verify permissions. Do not assume removing ZIPs erases public history.
- [ ] Set a protected release environment, stable signing credentials and offline backup of the signing key. Never commit the key or send it in chat.
- [ ] Run release unit tests, lint and build; verify APK signature and signing certificate fingerprint; record SHA-256.
- [ ] Confirm version code, app ID, upgrade compatibility and device install.
- [ ] Implement and test update detection against the releases-only repo before advertising it.
- [ ] Finalize privacy notice, proprietary distribution terms (or explicit open-source license if chosen), and support channel.

## 4. Beta test
- [ ] Run the device matrix and cases in BETA-TESTING.md.
- [ ] Resolve all credential leaks, crashes, data loss and other release blockers.
- [ ] Obtain tester feedback and record verified results.

## 5. Launch
- [ ] Owner approves exact signed APK and checksum.
- [ ] Publish as a pre-release on the releases-only repo; add install instructions and actual screenshots.
- [ ] Enable landing page hosting and link to the exact release.
- [ ] Gather feedback; consider Play distribution only after reviewing store policies.

No box may be marked complete solely because a script or document exists.
