# Security

## Reporting
Use the repository's private vulnerability reporting feature if enabled. Otherwise contact the maintainer privately before sharing technical details. Do not file public issues containing credentials, private logs, or exploit details. If a token might have leaked, revoke it in GitHub immediately.

## Credential design (v3.0 source inspection)
SecureTokenStore uses Android Keystore AES-256-GCM, app-private ciphertext, and account-bound associated data. Backup is disabled and cleartext network traffic is disabled in the manifest. These are code observations, not independent certification.

Use a classic or fine-grained personal access token with the minimum necessary access. Prefer fine-grained tokens limited to selected repositories where supported. Grant read permissions for viewing; grant write permissions only when an intentional feature requires them. Keep a short expiry and revoke unused credentials. Do not reuse a broad administrative token.

## Pending release checks
Verify account isolation, logout, cache invalidation, redaction, redirect handling, APK signature verification, dependency integrity and historical secret exposure. Review the public Git history, including both archives. If any real secret was committed, revoke/rotate it; deleting a file alone is insufficient.

No release until the checklist is signed off. No claim of a completed security audit is made.
