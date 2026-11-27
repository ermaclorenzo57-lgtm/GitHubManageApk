package com.lomipe.devhub.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun AppRoot(vm: AppViewModel, onRequestNotifications: () -> Unit) {
    val top = vm.stack.lastOrNull()
    val labels = listOf("Home", "Repos", "Builds", "Files", "Settings", "Studio")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.List, Icons.Filled.Build,
        Icons.Filled.KeyboardArrowDown, Icons.Filled.Settings, Icons.Filled.Star)
    val snack = remember { SnackbarHostState() }
    val ctx = LocalContext.current
    val updates = remember { UpdateState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { updates.check(ctx, false) }
    LaunchedEffect(vm.message) {
        val message = vm.message ?: return@LaunchedEffect
        snack.showSnackbar(message, withDismissAction = true, duration = SnackbarDuration.Short)
        if (vm.message == message) vm.message = null
    }
    BackHandler(enabled = top != null) { vm.pop() }
    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        bottomBar = {
            if (top == null) NavigationBar(tonalElevation = 2.dp) {
                labels.forEachIndexed { i, t ->
                    NavigationBarItem(
                        selected = vm.tab == i, onClick = { vm.tab = i },
                        icon = { Icon(icons[i], contentDescription = null, modifier = Modifier.size(22.dp)) },
                        label = { Text(t, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.sp)) },
                        alwaysShowLabel = true
                    )
                }
            }
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().imePadding()) {
            if (top == null) {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Lomile Dev Hub", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f),
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (updates.available != null) TextButton(onClick = { vm.tab = 4 }) { Text("Update") }
                    }
                }
                HorizontalDivider()
            }
            if (vm.busy > 0) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (vm.login == null && top == null && vm.tab != 4 && vm.tab != 5) {
                Page {
                    Text("Your mobile development workspace", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Manage repositories, monitor GitHub Actions and work with APK artifacts.")
                    Spacer(Modifier.height(20.dp))
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Connect GitHub", style = MaterialTheme.typography.titleMedium)
                            Note("Use a fine-grained token restricted to the repositories you choose. Start with read-only access; enable write permissions only for actions you need.")
                            Button(onClick = { vm.tab = 4 }, modifier = Modifier.fillMaxWidth()) { Text("Connect account") }
                            TextButton(onClick = { Intents.openGitHubUrl(ctx, "https://github.com/settings/personal-access-tokens/new") }) { Text("Create a GitHub token") }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Explore without an account", style = MaterialTheme.typography.titleMedium)
                            Note("Local Studio tools do not require a GitHub token.")
                            OutlinedButton(onClick = { vm.tab = 5 }, modifier = Modifier.fillMaxWidth()) { Text("Open Studio") }
                        }
                    }
                }
            } else when (top) {
                is Screen.RepoDetail -> vm.repoSession?.let { RepoScreen(vm, it) }
                is Screen.RunDetail -> vm.runSession?.let { RunScreen(vm, it, onRequestNotifications) }
                is Screen.LogView -> vm.logSession?.let { LogScreen(vm, it) }
                null -> when (vm.tab) {
                    0 -> HomeScreen(vm)
                    1 -> ReposScreen(vm)
                    2 -> BuildsScreen(vm)
                    3 -> DownloadsScreen(vm)
                    5 -> AdvancedStudio(vm)
                    else -> SettingsScreen(vm, updates, { scope.launch { updates.check(ctx, true) } })
                }
            }
        }
    }
}

@Composable
fun Page(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp), content = content)
}
