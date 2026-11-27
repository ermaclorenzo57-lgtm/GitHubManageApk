# MaxRepo privacy notice

Version 1.0.0 · Pre-release notice

MaxRepo is an independent Android developer workspace maintained by Lomile Dev Hub. This notice describes the current release candidate's intended data handling, based on source review. The pre-release security and dependency audit is not yet complete.

## Data and purpose

- GitHub account identifiers, repository selections, settings, local projects and download history are used to provide the app's workspace features.
- Personal access tokens are stored locally using Android Keystore-backed encryption. Use only the permissions necessary. Tokens are used for authorized GitHub API requests.
- GitHub features send requests to GitHub's API and, when needed, artifact hosting destinations. GitHub processes these requests under its [privacy statement](https://docs.github.com/en/site-policy/privacy-policies/github-general-privacy-statement). The app's release checker requests public release metadata without a GitHub personal access token.
- Files selected for upload are sent to the chosen repository when the user requests an upload. Downloaded files may remain in Android shared storage. Local developer tools may also read or write files you select.
- The local API tester can send requests to endpoints you choose. Review destination and request content before sending; do not include secrets in requests to untrusted services.
- Release signing keys are generated locally. The tool exports encrypted keystores to a destination you choose. Explicitly copying the encoded keystore puts sensitive material on the device clipboard; clipboard history or synchronization can retain it. MaxRepo does not automatically upload generated keys.
- Backup tools may export local information when requested. Protect backup files and any passwords used for them.

## Permissions

Internet and network-state permissions support network features. Notification permission supports build alerts. Package-install permission allows opening Android's installer for APKs you select. File access is used for imports, exports and downloads. Android controls permissions and package installation.

## Retention and deletion

Local app data stays on the device until you remove it or clear app storage. Signing out removes the locally stored account token; it does not revoke it on GitHub. Revoke tokens in GitHub settings separately. Shared downloads, exported backups and keystores may remain after uninstalling and must be managed separately. GitHub repository data is managed through GitHub.

## Third parties and advertising

GitHub and any user-selected network destinations receive requests necessary for the features you use. The reviewed application configuration does not declare advertising or analytics SDKs. A full resolved-dependency review is pending; this notice will be revisited if data handling changes.

## Contact and updates

For general privacy questions use [GitHub Issues](https://github.com/ermaclorenzo57-lgtm/GitHubManageApk/issues) without including private data. For security or sensitive issues use the private reporting process in [SECURITY.md](SECURITY.md). Do not share tokens, passwords, private keys, or private repository contents publicly.

This notice describes the release candidate, not a certification of compliance or a guarantee of security. Review the current notice when installing future versions.
