"""MaxRepo Phase 3 visual system; applied after Beta 2 on the extracted project."""
import os
import shutil
import zipfile
from pathlib import Path

def edit(path, fn):
    content = path.read_text(encoding="utf-8")
    updated = fn(content)
    if updated == content:
        raise SystemExit("Phase 3 patch mismatch: " + str(path))
    path.write_text(updated, encoding="utf-8")

def once(s, old, new):
    if s.count(old) != 1:
        raise SystemExit("Phase 3 patch mismatch: " + old[:100])
    return s.replace(old, new, 1)

def between(s, first, last, replacement):
    a = s.index(first)
    b = s.index(last, a + len(first))
    return s[:a] + replacement + s[b:]

def apply(root):
    app = root / "app"
    ui = app / "src/main/java/com/lomipe/devhub/ui"
    payload = Path(__file__).parent / "phase3"
    for name in ("Theme.kt", "AppRoot.kt", "Dashboard.kt", "StudioGrid.kt"):
        shutil.copyfile(payload / name, ui / name)
    edit(app / "src/main/res/values/strings.xml", lambda s: once(s, ">Lomile Dev Hub<", ">MaxRepo<"))
    edit(app / "src/main/res/drawable/ic_launcher_foreground.xml", lambda s: between(s, "    <path", "</vector>", '''    <path android:fillColor="#79C0FF" android:pathData="M25,76V32h10l19,22l19,-22h10v44H72V49L54,69L36,49v27z" />
'''))
    edit(app / "src/main/res/values/colors.xml", lambda s: once(s, "#1F6FEB", "#0D1117"))
    edit(app / "src/main/java/com/lomipe/devhub/data/Prefs.kt", lambda s: once(s, 'sp.getString("theme", "system")', 'sp.getString("theme", "dark")'))
    edit(ui / "UpdateState.kt", lambda s: once(s, "LomileDevHub-release-check", "MaxRepo-release-check"))
    edit(ui / "MainScreens.kt", lambda s: between(s, "@Composable\nfun RunRow(", "@Composable\nfun DownloadsScreen(", '''@Composable
fun RunRow(repo: String, r: Run, vm: AppViewModel) = MaxRunCard(repo, r, vm)

@Composable
fun HomeScreen(vm: AppViewModel) = MaxRepoDashboard(vm)

@Composable
fun ReposScreen(vm: AppViewModel) = MaxRepositories(vm)

@Composable
fun BuildsScreen(vm: AppViewModel) = MaxBuilds(vm)

'''))
    edit(ui / "MainScreens.kt", lambda s: once(s, '    var toDelete by remember { mutableStateOf<HistoryEntry?>(null) }', '''    var toDelete by remember { mutableStateOf<HistoryEntry?>(null) }
    MaxHeading("Files", "APK artifacts and download history")'''))
    edit(ui / "MainScreens.kt", lambda s: once(s, '    if (vm.history.isEmpty()) Note("Nothing downloaded yet. Open a successful run and download its APK artifact.")', '''    if (vm.history.isEmpty()) MaxPanel {
        Text("No downloads yet", style = MaterialTheme.typography.titleMedium)
        Note("Open a successful workflow run and download an APK artifact.")
        OutlinedButton(onClick = { vm.tab = 2 }) { Text("Browse builds") }
    }'''))
    edit(ui / "MainScreens.kt", lambda s: once(s, '        Column(Modifier.padding(vertical = 8.dp)) {', '        MaxPanel(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {'))
    edit(ui / "MainScreens.kt", lambda s: once(s, '    H("GitHub account")', '    MaxHeading("Settings", "Account, security and application preferences")\n    H("GitHub account")'))
    edit(ui / "MainScreens.kt", lambda s: once(s, '    H("App updates")', '    HorizontalDivider(Modifier.padding(vertical = 12.dp))\n    H("App updates")'))
    edit(ui / "AdvancedStudio.kt", lambda s: once(s, 'Text("DevHub Studio", style = MaterialTheme.typography.headlineSmall)', 'Text("MaxRepo Studio", style = MaterialTheme.typography.headlineSmall)'))
    edit(ui / "AdvancedStudio.kt", lambda s: between(s, "        val tools=listOf(", '        Note("No AI requests', '        MaxStudioGrid { tool = it }\n'))
    # Keep existing tool handlers unchanged; update the UI test to the new entry points.
    tests = app / "src/androidTest/java/com/lomipe/devhub"
    for name in ("StudioSmokeTest.kt", "NavigationLayoutTest.kt"):
        edit(tests / name, lambda s: once(s, '"DevHub Studio"', '"MaxRepo Studio"'))
    edit(tests / "NavigationLayoutTest.kt", lambda s: once(s, '"Files", "Settings", "Studio"', '"Files", "Studio"'))
    edit(tests / "SettingsSmokeTest.kt", lambda s: once(s.replace("import androidx.compose.ui.test.onNodeWithText", "import androidx.compose.ui.test.onNodeWithText\nimport androidx.compose.ui.test.onNodeWithContentDescription"), 'rule.onNodeWithText("Settings").performClick()', 'rule.onNodeWithContentDescription("Settings").performClick()'))

    # AGP's Android boot classpath excludes jdk.httpserver, used by the existing JVM-only MockServer.
    # Supply the JDK module classes only to the JVM test compilation; runtime uses the installed JDK.
    java_home = os.environ.get("JAVA_HOME")
    if not java_home:
        javac = shutil.which("javac")
        if not javac:
            raise SystemExit("JDK required for JVM test compilation")
        java_home = str(Path(javac).resolve().parent.parent)
    module = Path(java_home) / "jmods/jdk.httpserver.jmod"
    if not module.is_file():
        raise SystemExit("JDK httpserver module not found: " + str(module))
    target = app / "build/maxrepo-test-support/jdk-httpserver-api.jar"
    target.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(module) as src, zipfile.ZipFile(target, "w", zipfile.ZIP_DEFLATED) as dest:
        for name in src.namelist():
            if name.startswith("classes/com/sun/net/httpserver/") and name.endswith(".class"):
                dest.writestr(name.removeprefix("classes/"), src.read(name))
    edit(app / "build.gradle.kts", lambda s: once(s, 'testImplementation("junit:junit:4.13.2")', 'testImplementation("junit:junit:4.13.2")\n    testCompileOnly(files("build/maxrepo-test-support/jdk-httpserver-api.jar"))'))
    # Both classic and fine-grained PATs are accepted by the existing GitHub authentication client.
    def token_copy(s):
        s = between(s, '    Note("Start with read access.', '    OutlinedTextField(value = token', '''    Note("Connect using a GitHub personal access token (classic or fine-grained).")
    TextButton(onClick = { Intents.openGitHubUrl(ctx, "https://github.com/settings/tokens") }) { Text("Manage GitHub tokens") }
    Note("Classic: use repo for private repositories, or public_repo for public-only access. Add workflow only when editing workflow files. Fine-grained: select your repositories and grant Contents and Actions access as needed; grant Workflows write only if needed. Some operations or organizations have additional restrictions. Prefer limited access and expiration.")
''')
        return s
    edit(ui / "MainScreens.kt", token_copy)
    # Keep local history and credentials intact; only clear stale cached release metadata after the rebrand.
    edit(ui / "UpdateState.kt", lambda s: s.replace('getSharedPreferences("release_check"', 'getSharedPreferences("maxrepo_release_check_v1"'))
    # Signing tool: portable encrypted PKCS12 generated locally, never automatically uploaded.
    shutil.copyfile(payload / "ReleaseKey.kt", app / "src/main/java/com/lomipe/devhub/core/ReleaseKey.kt")
    shutil.copyfile(payload / "ReleaseSigningPanel.kt", ui / "ReleaseSigningPanel.kt")
    shutil.copyfile(payload / "ReleaseKeyTest.kt", app / "src/test/java/com/lomipe/devhub/core/ReleaseKeyTest.kt")
    shutil.copyfile(payload / "ReleaseKeyDeviceTest.kt", tests / "ReleaseKeyDeviceTest.kt")
    edit(ui / "AdvancedStudio.kt", lambda s: once(s, '"Utilities" -> Utilities(vm)', '"Utilities" -> Utilities(vm)\n            "Release signing" -> ReleaseSigningPanel()'))
    edit(app / "build.gradle.kts", lambda s: once(s, 'dependencies {', 'dependencies {\n    implementation("org.bouncycastle:bcpkix-jdk18on:1.80")'))
    # Bouncy Castle ships duplicate Java 9 OSGi bundle metadata in three JARs.
    # Exclude only desktop OSGi manifests; retain Android classes and license notices.
    edit(app / "build.gradle.kts", lambda s: once(s, '    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }', '''    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
        resources.excludes += setOf("META-INF/versions/**/OSGI-INF/MANIFEST.MF", "OSGI-INF/MANIFEST.MF", "META-INF/OSGI-INF/MANIFEST.MF")
        resources.merges += setOf("META-INF/LICENSE*", "META-INF/NOTICE*")
    }'''))
    print("MaxRepo 1.0 UI, signing tool and JVM test classpath prepared.")
