# MaxRepo 1.0 signing and migration

First public versionName: 1.0.0. Internal versionCode: 10000. Package identity remains com.lomipe.devhub. Every later public update must use a greater versionCode and the same signing certificate. If old test APKs use a different key, a one-time fresh install is required; the old 3.x beta update checker also may not detect the marketing-version reset, so migrate manually once.

1. Create a release keystore on a trusted device with Java keytool (or Android Studio). Do not generate it in ephemeral CI. Use a strong password, keep encrypted offline backups and retain the alias. If an existing permanent key signed the installed APK, reuse that exact key rather than creating another.
2. Create GitHub environment maxrepo-production, restrict it to main, protect main and require review where available.
3. Add environment secrets LOMIPE_KEYSTORE_BASE64 (base64 of keystore bytes, not encryption), LOMIPE_KEYSTORE_PASSWORD, LOMIPE_KEY_ALIAS, LOMIPE_KEY_PASSWORD. Never commit or paste keys/passwords in chat.
4. Add environment variable MAXREPO_CERT_SHA256 (SHA-256 of the signing certificate from keytool -list -v or apksigner). Confirm it against the actual installed production APK.
5. After CI passes and the PR is reviewed/merged, manually run MaxRepo production signing on main. Verify APK signature, package, version and checksums. Download artifact; perform a fresh install and a signed update smoke test before publishing. No automatic public release.
6. Create GitHub release v1.0.0 with verified APK and checksum; later use v1.0.1 etc. Update checks use current repository until a dedicated release repository is configured.
7. Only then remove old Actions runs, keeping any evidence or artifacts you need. Deleting runs removes logs and associated artifacts, not Git history. Use repository Actions UI; old runs may not be recoverable. Do not clear user account tokens, local files or other repositories as part of this cleanup.
