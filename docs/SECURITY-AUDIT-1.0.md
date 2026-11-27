# MaxRepo 1.0 audit record

Initial audit: [run 46035073389](https://github.com/ermaclorenzo57-lgtm/GitHubManageApk/actions/runs/46035073389), commit 95883dc.

- Gitleaks history and historical ZIP scan: passed; this does not prove that no credential was ever exposed.
- Gradle wrapper check and resolved dependency inventory: passed.
- OSV query: 241 resolved Maven coordinates, 64 advisory matches; release blocked pending remediation and review.
- Findings included Bouncy Castle 1.80/1.80.2, older build-classpath dependencies including Netty, Guava, protobuf, Commons IO/Compress, jose4j, JDOM and Kotlin Gradle plugin. The first report did not distinguish runtime vs build-only scopes.
- CodeQL analysis started; review actual alerts, not only workflow exit status.

## Remediation initiated

- Updated the app's Bouncy Castle PKIX dependency to 1.87 (upstream stable release). Verify actual resolved versions and key generation/export on Android; do not assume a version bump resolves every advisory.
- Added scope information to future OSV reports so release runtime and build-only findings can be distinguished.
- Added weekly Dependabot updates for GitHub Actions; Gradle dependencies inside the ZIP are covered by the assembled-project audit rather than claiming Dependabot can read the ZIP.
- Added wrapper integrity check to production signing.
- Candidate internal version code 10003; display version remains 1.0.0. New build, device tests and signed upgrade required.
- Remaining: remediate or formally assess build-only advisory matches, review CodeQL, dependency integrity and license inventory, confirm device behavior, and protect main.

No official release is approved by this report. Never publish debug APKs or skip audit gates to force a release.
