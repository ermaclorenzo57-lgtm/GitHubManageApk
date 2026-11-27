"""Beta 2 source changes, applied to the v3 archive during CI."""
from pathlib import Path
import shutil

def once(path, old, new):
    content = path.read_text(encoding="utf-8")
    if content.count(old) != 1:
        raise SystemExit("Beta 2 patch mismatch: " + str(path) + " / " + old[:80])
    path.write_text(content.replace(old, new, 1), encoding="utf-8")

def apply(root):
    base = root / "app/src/main/java/com/lomipe/devhub"
    ui = base / "ui"
    payload = Path(__file__).parent / "beta2"
    for src, dest in [
        ("AppRoot.kt", ui / "AppRoot.kt"),
        ("ReleaseVersion.kt", base / "core/ReleaseVersion.kt"),
        ("UpdateState.kt", ui / "UpdateState.kt"),
        ("Beta2Test.kt", root / "app/src/test/java/com/lomipe/devhub/core/Beta2Test.kt"),
        ("NavigationLayoutTest.kt", root / "app/src/androidTest/java/com/lomipe/devhub/NavigationLayoutTest.kt"),
    ]:
        shutil.copyfile(payload / src, dest)

    gradle = root / "app/build.gradle.kts"
    once(gradle, 'implementation("androidx.compose.material3:material3")',
         'implementation("androidx.compose.material3:material3")\n    implementation("androidx.compose.material:material-icons-core")')

    studio = ui / "AdvancedStudio.kt"
    once(studio, "import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier\nimport androidx.compose.ui.text.style.TextAlign\nimport androidx.compose.foundation.shape.RoundedCornerShape\nimport androidx.compose.ui.platform.LocalDensity")
    once(studio, 'H("DevHub Studio · 3.0")', 'Text("DevHub Studio", style = MaterialTheme.typography.headlineSmall)')
    once(studio, 'tools.chunked(2).forEach { pair -> Row(Modifier.fillMaxWidth()) { pair.forEach { name -> OutlinedButton(onClick={tool=name},modifier=Modifier.weight(1f).padding(4.dp).heightIn(min=64.dp)){Text(name)} };if(pair.size==1)Spacer(Modifier.weight(1f)) } }',
        '''Spacer(Modifier.height(12.dp))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = if (maxWidth < 300.dp || LocalDensity.current.fontScale > 1.35f) 1 else 2
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                tools.chunked(columns).forEach { pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { name ->
                            OutlinedButton(onClick = { tool = name },
                                modifier = Modifier.weight(1f).heightIn(min = 78.dp),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 16.dp)) {
                                Text(name, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleSmall)
                            }
                        }
                        if (columns == 2 && pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))''')
    smoke = root / "app/src/androidTest/java/com/lomipe/devhub/StudioSmokeTest.kt"
    once(smoke, '"DevHub Studio · 3.0"', '"DevHub Studio"')

    main = ui / "MainScreens.kt"
    once(main, "import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier\nimport androidx.compose.ui.Alignment\nimport androidx.compose.ui.text.style.TextOverflow")
    once(main, '    Row { BtnO("Reload") { vm.loadRepos(true) } }', '''    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Repositories", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        TextButton(onClick = { vm.loadRepos(true) }) { Text("Refresh") }
    }''')
    once(main, '''        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Column(Modifier.weight(1f).clickable { vm.openRepo(r.fullName) }) {
                Text(r.fullName + (if (r.isPrivate) "  (private)" else "") + (vm.prefs.repoPref(vm.login!!,r.fullName,"group")?.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""), style = MaterialTheme.typography.bodyLarge)
                Text("default: ${r.defaultBranch}" + if (!r.canPush) " · read-only" else "", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = { vm.togglePin(r.fullName) }) { Text(if (r.fullName in vm.pinned) "Unpin" else "Pin") }
        }
        HorizontalDivider()'''.replace(r'\$', '$'), '''        OutlinedCard(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f).clickable { vm.openRepo(r.fullName) }.padding(vertical = 4.dp)) {
                    Text(r.fullName.substringAfterLast('/'), style = MaterialTheme.typography.titleMedium,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(r.fullName.substringBeforeLast('/'), style = MaterialTheme.typography.bodySmall,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Note((if (r.isPrivate) "Private" else "Public") + " · " + r.defaultBranch + if (!r.canPush) " · Read-only" else "")
                    vm.login?.let { account -> vm.prefs.repoPref(account,r.fullName,"group")?.takeIf { it.isNotBlank() }?.let { Note(it) } }
                }
                TextButton(onClick = { vm.togglePin(r.fullName) }) { Text(if (r.fullName in vm.pinned) "Unpin" else "Pin") }
            }
        }''')
    once(main, 'fun SettingsScreen(vm: AppViewModel) = Page {', 'fun SettingsScreen(vm: AppViewModel, updates: UpdateState = remember { UpdateState() }, checkUpdates: () -> Unit = {}) = Page {')
    once(main, '    H("GitHub account")', '''    H("GitHub account")
    Note("Start with read access. For repository changes or workflow actions, grant only the permissions needed.")
    TextButton(onClick = { Intents.openGitHubUrl(ctx, "https://github.com/settings/personal-access-tokens/new") }) { Text("Create a fine-grained token") }''')
    once(main, '    Note("Lomipe DevHub 2.0.0. The log is redacted (tokens and signed URLs removed).")', '''    Note("Review exported logs for private information before sharing.")
    H("App updates")
    Note("Installed version: " + updates.installed(ctx))
    Note(updates.status)
    OutlinedButton(onClick = checkUpdates, enabled = !updates.checking) { Text(if (updates.checking) "Checking…" else "Check for updates") }
    updates.available?.let { tag -> Button(onClick = { updates.open(ctx) }) { Text("View release " + tag) } }
    Note("Checks use public release information without your GitHub token. Installation is always manual.")''')

    vm = ui / "AppViewModel.kt"
    once(vm, "import kotlinx.coroutines.Job", "import kotlinx.coroutines.Job\nimport kotlinx.coroutines.CoroutineStart")
    once(vm, '    init {', '    private val operations = mutableSetOf<Job>()\n\n    init {')
    once(vm, "    fun op(block: suspend () -> Unit): Job = viewModelScope.launch {", '''    private fun cancelAccountOps(except: Job? = null) {
        operations.toList().filter { it != except }.forEach { it.cancel() }
    }

    fun op(block: suspend () -> Unit): Job {
        val job = viewModelScope.launch(start = CoroutineStart.LAZY) {''')
    once(vm, '        finally { busy-- }\n    }', '''        finally { busy--; coroutineContext[Job]?.let { operations.remove(it) } }
        }
        operations.add(job)
        job.start()
        return job
    }''')
    once(vm, '        resetAccountState(); onAccountReady()\n        message = "Signed in as ${u.login}"'.replace(r'\$', '$'), '        cancelAccountOps(kotlin.coroutines.coroutineContext[Job])\n        resetAccountState(); onAccountReady()\n        message = "Signed in as ${u.login}"'.replace(r'\$', '$'))
    once(vm, '    fun switchTo(l: String) { prefs.activeLogin = l;', '    fun switchTo(l: String) { cancelAccountOps(); prefs.activeLogin = l;')
    once(vm, '        if (login.equals(l, true)) { login = accounts.firstOrNull();', '        if (login.equals(l, true)) { cancelAccountOps(); login = accounts.firstOrNull();')
    once(vm, '        pinned = emptySet()\n    }', '        pinned = emptySet(); homeUpdated = null; homeNote = null; repoQuery = ""; message = null\n    }')

    http = base / "core/Http.kt"
    once(http, '            c.setRequestProperty("Authorization", "Bearer " + token())', '''            val credential = token()
            require(credential.isNotBlank()) { "Sign in again; the token is no longer available." }
            c.setRequestProperty("Authorization", "Bearer " + credential)''')
    once(http, '                } catch (e: IOException) {\n                    throw IOException(Redactor.redact(e.message ?: e.javaClass.simpleName), e)\n                }', '''                } catch (e: IOException) {
                    throw IOException(Redactor.redact(e.message ?: e.javaClass.simpleName), e)
                } finally {
                    c.disconnect()
                }''')
    print("Beta 2 UI, release checker and security overlay applied.")
