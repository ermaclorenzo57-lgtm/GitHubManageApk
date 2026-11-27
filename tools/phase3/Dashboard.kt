package com.lomipe.devhub.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lomipe.devhub.core.Run

@Composable
fun MaxPanel(modifier: Modifier = Modifier.fillMaxWidth(), content: @Composable ColumnScope.() -> Unit) {
    OutlinedCard(modifier, shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp), content = content)
    }
}
@Composable
fun MaxHeading(title: String, detail: String? = null) {
    Spacer(Modifier.height(8.dp))
    Text(title, style = MaterialTheme.typography.titleLarge)
    detail?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    Spacer(Modifier.height(10.dp))
}
@Composable
fun MaxLabel(label: String, tone: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Surface(shape = MaterialTheme.shapes.small, color = tone.copy(alpha = .09f), border = BorderStroke(1.dp, tone.copy(alpha = .35f))) {
        Text(label, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = tone, style = MaterialTheme.typography.labelSmall)
    }
}
@Composable
fun RunStatus(r: Run) {
    val tone = when {
        r.isSuccess -> MaterialTheme.colorScheme.secondary
        r.isFailure -> MaterialTheme.colorScheme.error
        r.status == "in_progress" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    MaxLabel(when {
        r.isSuccess -> "Success"
        r.isFailure -> "Failed"
        r.status == "in_progress" -> "Running"
        else -> (r.conclusion ?: r.status).replace('_', ' ').replaceFirstChar { it.uppercase() }
    }, tone)
}
@Composable
fun MaxEmpty(icon: ImageVector, title: String, detail: String, action: String? = null, onAction: () -> Unit = {}) {
    MaxPanel {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Note(detail)
        action?.let { OutlinedButton(onClick = onAction) { Text(it) } }
    }
}
@Composable
fun MaxRunCard(repo: String, r: Run, vm: AppViewModel) {
    MaxPanel(Modifier.fillMaxWidth().clickable { vm.openRun(repo, r.id) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Build, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(repo.substringAfterLast('/'), Modifier.weight(1f), color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            RunStatus(r)
        }
        Text(r.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Note("#" + r.runNumber + " · " + (r.branch ?: "?"))
        Text(r.event.replace('_', ' ') + " · " + r.headSha.take(7), style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        r.commitMessage?.lines()?.firstOrNull()?.let { Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis) }
        if (r.status == "in_progress") LinearProgressIndicator(Modifier.fillMaxWidth())
    }
    Spacer(Modifier.height(8.dp))
}
@Composable
fun MaxRepoDashboard(vm: AppViewModel) = Page {
    MaxPanel {
        Text("YOUR WORKSPACE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text("Welcome back", style = MaterialTheme.typography.headlineSmall)
        Text(vm.login ?: "Local workspace", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Note("Build. Manage. Ship. Anywhere.")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f)) { Text(vm.filteredRepos().size.toString(), style = MaterialTheme.typography.headlineMedium); Note("Repos loaded") }
            Column(Modifier.weight(1f)) { Text(vm.homeRuns.count { it.second.isActive }.toString(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.tertiary); Note("Active") }
            Column(Modifier.weight(1f)) { Text(vm.homeRuns.count { it.second.isFailure }.toString(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.error); Note("Failed") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.tab = 1 }, modifier = Modifier.weight(1f)) { Text("Repositories", maxLines = 1, overflow = TextOverflow.Ellipsis) }
            OutlinedButton(onClick = { vm.refreshHome() }, modifier = Modifier.weight(1f)) { Text("Refresh") }
        }
        vm.homeUpdated?.let { Note("Updated $it") }
        vm.homeNote?.let { Note(it) }
    }
    MaxHeading("Quick actions")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { vm.tab = 2 }, modifier = Modifier.weight(1f)) { Text("Actions") }
        OutlinedButton(onClick = { vm.tab = 5 }, modifier = Modifier.weight(1f)) { Text("Studio") }
        OutlinedButton(onClick = { vm.tab = 3 }, modifier = Modifier.weight(1f)) { Text("Files") }
    }
    MaxHeading("Pinned repositories", "Your frequently used projects")
    if (vm.pinned.isEmpty()) MaxEmpty(Icons.Filled.Star, "No pinned repositories", "Pin a repo to follow its latest workflow runs.", "Browse repos") { vm.tab = 1 }
    vm.pinned.sorted().forEach { repo ->
        MaxPanel(Modifier.fillMaxWidth().clickable { vm.openRepo(repo) }) {
            Text(repo.substringAfterLast('/'), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Note(repo.substringBeforeLast('/'))
            vm.homeRuns.firstOrNull { it.first == repo }?.second?.let { RunStatus(it); Note(it.name) } ?: Note("No runs found")
        }
        Spacer(Modifier.height(8.dp))
    }
    MaxHeading("Active workflows")
    val active = vm.homeRuns.filter { it.second.isActive }
    if (active.isEmpty()) MaxEmpty(Icons.Filled.Build, "All quiet", "No active runs in pinned repositories.")
    active.forEach { (repo, r) -> MaxRunCard(repo, r, vm) }
    MaxHeading("Recent failures")
    val failed = vm.homeRuns.filter { it.second.isFailure }.take(5)
    if (failed.isEmpty()) MaxEmpty(Icons.Filled.CheckCircle, "No recent failures", "Based on the latest loaded runs.")
    failed.forEach { (repo, r) -> MaxRunCard(repo, r, vm) }
    MaxHeading("Recent downloads")
    if (vm.history.isEmpty()) MaxEmpty(Icons.Filled.KeyboardArrowDown, "No APKs yet", "Artifacts from successful builds appear here.")
    vm.history.take(3).forEach { e ->
        MaxPanel(Modifier.fillMaxWidth().clickable { vm.tab = 3 }) { Text(e.apkName, style = MaterialTheme.typography.titleSmall); Note(e.repo) }
        Spacer(Modifier.height(8.dp))
    }
}
@Composable
fun MaxRepositories(vm: AppViewModel) = Page {
    MaxHeading("Repositories", "Manage your GitHub projects")
    RepositoryAddPanel(vm)
    Field("Search repositories", vm.repoQuery, { vm.repoQuery = it })
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Note("Showing " + vm.filteredRepos().size + " loaded repositories")
        Spacer(Modifier.weight(1f))
        IconButton(onClick = { vm.loadRepos(true) }) { Icon(Icons.Filled.Refresh, "Refresh repositories") }
    }
    if (vm.reposLoaded && vm.repos.isEmpty()) MaxEmpty(Icons.Filled.List, "No repositories found", "Check the repository access granted to your token.")
    vm.filteredRepos().forEach { r ->
        MaxPanel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f).clickable { vm.openRepo(r.fullName) }) {
                    Text(r.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(r.owner, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = { vm.togglePin(r.fullName) }) {
                    Icon(Icons.Filled.Star, contentDescription = if (r.fullName in vm.pinned) "Unpin " + r.name else "Pin " + r.name,
                        tint = if (r.fullName in vm.pinned) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            r.description?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis) }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MaxLabel(if (r.isPrivate) "Private" else "Public")
                MaxLabel(if (r.canPush) "Read/write" else "Read-only")
            }
            Note("Default branch: " + r.defaultBranch)
            vm.login?.let { a -> vm.prefs.repoPref(a,r.fullName,"group")?.takeIf { it.isNotBlank() }?.let { Note(it) } }
            vm.homeRuns.firstOrNull { it.first == r.fullName }?.second?.let { RunStatus(it) }
            TextButton(onClick = { vm.openRepo(r.fullName) }) { Text("Open repository") }
        }
        Spacer(Modifier.height(8.dp))
    }
    if (vm.reposHasNext) OutlinedButton(onClick = { vm.loadRepos(false) }, modifier = Modifier.fillMaxWidth()) { Text("Load more") }
    if (vm.repoQuery.isNotBlank()) Note("Search covers loaded repositories.")
}
@Composable
fun MaxBuilds(vm: AppViewModel) = Page {
    MaxHeading("Actions", "Monitor builds and open workflow logs")
    var filter by remember { mutableStateOf(0) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Workflow runs", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = { vm.refreshHome() }) { Icon(Icons.Filled.Refresh, "Refresh builds") }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf("All", "Active", "Failed").forEachIndexed { i, label ->
            FilterChip(selected = filter == i, onClick = { filter = i }, label = { Text(label) })
        }
    }
    vm.homeNote?.let { Note(it) }
    if (vm.pinned.isEmpty()) MaxEmpty(Icons.Filled.Star, "Follow your builds", "Pin repositories to see their runs.", "Repositories") { vm.tab = 1 }
    val rows = vm.homeRuns.filter { when (filter) { 1 -> it.second.isActive; 2 -> it.second.isFailure; else -> true } }
    if (rows.isEmpty() && vm.pinned.isNotEmpty()) MaxEmpty(Icons.Filled.Build, "No matching runs", "Refresh or change the filter.")
    rows.forEach { (repo, run) -> MaxRunCard(repo, run, vm) }
}
