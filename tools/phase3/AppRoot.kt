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
    val labels = listOf("Home", "Repos", "Builds", "Files", "Studio")
    val tabs = listOf(0, 1, 2, 3, 5)
    var previousTab by remember { mutableIntStateOf(0) }
    fun settings() { if (vm.tab != 4) previousTab = vm.tab; vm.tab = 4 }
    val icons = listOf(Icons.Filled.Home, Icons.Filled.List, Icons.Filled.Build,
        Icons.Filled.KeyboardArrowDown, Icons.Filled.Star)
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
    BackHandler(enabled = top != null || vm.tab == 4) { if (top != null) vm.pop() else vm.tab = previousTab }
    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        bottomBar = {
            if (top == null) NavigationBar(tonalElevation = 0.dp, containerColor = MaterialTheme.colorScheme.surface) {
                labels.forEachIndexed { i, t ->
                    NavigationBarItem(
                        selected = vm.tab == tabs[i], onClick = { vm.tab = tabs[i] },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer, selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer, selectedTextColor = MaterialTheme.colorScheme.primary),
                        icon = { Icon(icons[i], contentDescription = null, modifier = Modifier.size(22.dp)) },
                        label = { Text(t, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, letterSpacing = 0.sp)) },
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
                        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
                            Text("M", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("MaxRepo", style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Developer workspace", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                        if (updates.available != null) TextButton(onClick = { settings() }) { Text("Update") }
                        IconButton(onClick = { settings() }) { Icon(Icons.Filled.Settings, contentDescription = "Settings") }
                    }
                }
                HorizontalDivider()
            }
            if (vm.busy > 0) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (vm.login == null && top == null && vm.tab != 4 && vm.tab != 5) {
                Page {
                    Text("Build. Manage. Ship. Anywhere.", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Your GitHub mobile workspace for Android. Manage repositories, monitor Actions, download artifacts, and use developer tools on your phone.")
                    Spacer(Modifier.height(20.dp))
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Connect GitHub", style = MaterialTheme.typography.titleMedium)
                            Note("Use a fine-grained token restricted to the repositories you choose. Start with read-only access; enable write permissions only for actions you need.")
                            Button(onClick = { settings() }, modifier = Modifier.fillMaxWidth()) { Text("Connect account") }
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
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 14.dp), content = content)
}
