package com.lomipe.devhub.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun MaxStudioGrid(onTool: (String) -> Unit) {
    val groups = listOf(
        "Create & edit" to listOf(Triple("Code workbench", "Edit local source", Icons.Filled.Edit),
            Triple("UI designer", "Design and preview", Icons.Filled.Star),
            Triple("Asset library", "Inspect project files", Icons.Filled.List),
            Triple("Task board", "Organize development", Icons.Filled.CheckCircle)),
        "Test & ship" to listOf(Triple("Test lab", "Inspect project structure", Icons.Filled.Build),
            Triple("API tester", "Test requests", Icons.Filled.PlayArrow),
            Triple("APK analyzer", "Inspect packages", Icons.Filled.Info),
            Triple("Build templates", "Create workflow files", Icons.Filled.Build)),
        "Workspace tools" to listOf(Triple("Encrypted backup", "Protect local work", Icons.Filled.Lock),
            Triple("AI handoff", "Prepare context", Icons.Filled.Share),
            Triple("Utilities", "Developer helpers", Icons.Filled.Settings),
            Triple("Release signing", "Create permanent signing key", Icons.Filled.Lock))
    )
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = if (maxWidth < 300.dp || LocalDensity.current.fontScale > 1.35f) 1 else 2
        Column {
            groups.forEach { (section, tools) ->
                MaxHeading(section)
                tools.chunked(columns).forEach { pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { (name, description, icon) ->
                            MaxPanel(Modifier.weight(1f).heightIn(min = 118.dp).clickable { onTool(name) }) {
                                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                Text(name, style = MaterialTheme.typography.titleSmall)
                                Note(description)
                            }
                        }
                        if (columns == 2 && pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}
