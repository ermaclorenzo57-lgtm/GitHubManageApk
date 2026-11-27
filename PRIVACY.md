# Privacy notice — draft for beta review

Lomile Dev Hub is a proposed Android application for interacting with GitHub. This draft must be checked against the actual release build before publication.

- GitHub account identifiers, settings, repository selections and history may be stored locally. Source inspection shows GitHub tokens encrypted using Android Keystore with app-private storage and backups disabled.
- Network requests for GitHub features are sent to GitHub. Artifact downloads may follow redirects to hosting endpoints; the HTTP client is designed not to forward GitHub credentials outside its trusted destinations.
- Files chosen for upload are sent to GitHub when the user requests an upload; downloaded APKs or artifacts may remain in shared storage.
- Internet and network-state access support GitHub features; notification permission supports build alerts; package installation permission supports launching Android's installer for user-selected APKs.
- To remove local app data, sign out and clear app storage or uninstall. Revoke GitHub tokens separately. Delete shared downloads separately. Remote GitHub content is governed by GitHub's policies and must be managed on GitHub.
- No advertising or paid registration is planned for the beta. Telemetry, SDK behavior, retention and support contact must be verified against the actual build before this notice is finalized.

Do not send secrets in feedback. Use the security reporting channel described in SECURITY.md.
