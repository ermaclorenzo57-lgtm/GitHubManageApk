# Beta 2 security review

Status: targeted static source inspection and changes; not a complete independent security audit.

| Area | Observed / changed | Verification still needed |
|---|---|---|
| Credential storage | AES-GCM Android Keystore and backup-disabled manifest in v3 archive | Hardware/device behavior, corruption, account isolation |
| GitHub auth | HTTPS, origin (scheme/host/port) restriction, no auth on other origins, refuse missing token | Unit and integration tests, redirect edge cases |
| Account switching | Cancel active ViewModel operations on switch/sign-out; clear previous account UI metadata | Slow-network and concurrent action device regression |
| Update check | Fixed HTTPS public endpoint, no auth header, no redirects, bounded response; strict version tags; manual release page only | Network/device checks; change endpoint to dedicated release repo before launch |
| Builds | Manual main-only release signing and apksigner verification | Signing secret environment protection; key provenance; release upgrade |
| Local/exported data | Existing per-account storage and redaction | Review actual exports, encrypted backups, logs, arbitrary API tester, file handling, downloaded APK verification |
| Source and dependencies | Two source ZIPs visible in public repo | Full history and archive secret scanning, dependency and license audit, security regression tests |

No public release should claim a full audit. Rotate any exposed real token or key, regardless of whether it is later removed from Git history.

### Test coverage
New JVM tests for version ordering, stable/beta selection and credential origin port restriction; existing JVM tests remain in CI. New Android UI test checks nav label baseline/order and local Studio access. API 29 and 35 emulator job is separate from physical-device testing. Physical low-memory devices, font scaling, slow connections and upgrade signature compatibility require hands-on verification.
