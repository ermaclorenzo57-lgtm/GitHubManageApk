# Beta testing guide

Status: candidate not distributed. Use disposable test repositories and narrowly scoped short-lived tokens, not your main credentials or important projects.

| Device | Required scenarios |
|---|---|
| Android 10/11 | Install, launch, token storage, file picker, downloads |
| Android 13 | Notifications, background monitoring, permission denial |
| Android 15/16 | Install and upgrade, layout, permissions, background work |
| Low-memory Android phone | Large repository list, workflow logs, canceled download, slow network |

Test authentication rejection and expiration; switch accounts and verify no data crosses accounts; sign out; import a repo; view a private and public repo; read Actions logs; download artifacts; cancel a transfer; deny permissions; recover from airplane mode; confirm uploads and destructive operations; verify APK signer before installing. Repeat after upgrading from the previous build.

Report device model, Android version, app version, steps, expected/actual outcome and redacted screenshots. Never include tokens, private repository contents or logs containing secrets.

Release blockers: credential exposure, unauthorized operation, data loss, crashes in core paths, unsigned/mismatched APK, broken upgrade, or misleading privacy claims.
