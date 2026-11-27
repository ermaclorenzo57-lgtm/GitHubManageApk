from pathlib import Path
import shutil

def apply(root):
    from phase3 import edit, once
    base = root / "app/src/main/java/com/lomipe/devhub"
    ui = base / "ui"
    payload = Path(__file__).parent / "repository_admin"
    shutil.copyfile(payload / "RepoAdmin.kt", base / "core/RepoAdmin.kt")
    shutil.copyfile(payload / "RepoManagementHub.kt", ui / "RepoManagementHub.kt")
    shutil.copyfile(payload / "RepoAdminTest.kt", root / "app/src/test/java/com/lomipe/devhub/core/RepoAdminTest.kt")
    edit(ui / "RepoScreen.kt", lambda s: once(s, "5 -> ManagePanel(vm,s)", "5 -> RepoManagementHub(vm,s)"))
    edit(ui / "MainScreens.kt", lambda s: once(s, '    H("App updates")', '''    H("Support MaxRepo")
    Note("MaxRepo is free. Supporting development is optional. Donation options are not configured yet.")
    TextButton(onClick = { Intents.openGitHubUrl(ctx, "https://github.com/ermaclorenzo57-lgtm/GitHubManageApk#support-maxrepo") }) { Text("Support the project") }
    H("App updates")'''))
