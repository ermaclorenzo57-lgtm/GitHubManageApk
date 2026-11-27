# MaxRepo 1.0 release gate

Display version 1.0.0. Verified candidate version code 10002, package com.lomipe.devhub.

## Verified
- [x] Production key configured and matching certificate verified.
- [x] Unit tests, release lint, APK compilation and signature verification passed in [production run](https://github.com/ermaclorenzo57-lgtm/GitHubManageApk/actions/runs/46023087566).
- [x] Version, package identity and checksum verified; production artifact uploaded.
- [x] Android API 29 and 35 emulator tests passed on the signing-tool source at commit e037371, [test run](https://github.com/ermaclorenzo57-lgtm/GitHubManageApk/actions/runs/46016317640).

- [x] Build 10002 passed production signing and verification in [run 46029050038](https://github.com/ermaclorenzo57-lgtm/GitHubManageApk/actions/runs/46029050038).

## Security and privacy
- [x] Added automated audit workflow and CodeQL configuration; execution and findings review pending.
- [ ] Scan current source, both legacy ZIPs and public Git history for secrets; revoke and rotate any exposed credentials.
- [ ] Verify account isolation, logout, redacted logs, cache, redirect and exported file behavior.
- [ ] Review dependencies, actions and third-party licenses; check permissions and update installation handling.
- [ ] Protect main with required checks/review where available. Restrict production environment to main; retain encrypted offline signing-key backup.
- [ ] Finalize privacy notice, supported distribution terms and security contact.

## Testing and distribution
- [ ] Fresh install the signed APK and verify a later signed build with a higher version code updates without uninstalling or losing data.
- [ ] Complete physical-device, slow-network and accessibility testing; capture real screenshots.
- [ ] Decide source visibility and create a dedicated public download repository if keeping source private. Public history cannot be made retroactively secret.
- [ ] Configure and test the update endpoint against the chosen release repository before launch.
- [ ] Owner approves exact APK and checksum; create GitHub Release v1.0.0 only when authorized.
- [ ] Preserve the signed artifact and test evidence before removing old Actions runs.

No official release has been published. Passing CI does not establish a complete security audit.
