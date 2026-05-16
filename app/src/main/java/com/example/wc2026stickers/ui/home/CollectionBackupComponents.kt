package com.wc2026stickers.app.ui.home

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import com.wc2026stickers.app.data.backup.CollectionRestorePreview
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun CollectionBackupMenu(
    enabled: Boolean,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    IconButton(enabled = enabled, onClick = { expanded = true }) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Backup and restore options"
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text("Export backup") },
            onClick = {
                expanded = false
                onExportClick()
            },
            enabled = enabled
        )
        DropdownMenuItem(
            text = { Text("Import backup") },
            onClick = {
                expanded = false
                onImportClick()
            },
            enabled = enabled
        )
    }
}

@Composable
fun CollectionRestoreDialog(
    preview: CollectionRestorePreview,
    enabled: Boolean,
    onDismiss: () -> Unit,
    onMerge: () -> Unit,
    onReplace: () -> Unit
) {
    val importedCount = preview.validEntries.size
    val skippedCount = preview.skippedStickerIds.size
    val skippedPreview = preview.skippedStickerIds.take(3).joinToString(", ")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Restore collection backup?", fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                buildString {
                    append("Backup exported ${formatExportedAt(preview.backup.exportedAt)} contains ")
                    append("$importedCount collected ${pluralize(importedCount, "sticker", "stickers")}.")
                    append("\n\nMerge keeps the higher quantity for each sticker and never lowers your local counts.")
                    append("\nReplace clears your current local collection and restores only this backup.")
                    if (skippedCount > 0) {
                        append("\n\nSkipped $skippedCount unknown ${pluralize(skippedCount, "entry", "entries")}")
                        if (skippedPreview.isNotBlank()) {
                            append(": $skippedPreview")
                            if (preview.skippedStickerIds.size > 3) append("…")
                        }
                        append(".")
                    }
                }
            )
        },
        confirmButton = {
            TextButton(enabled = enabled, onClick = onMerge) {
                Text("Merge")
            }
        },
        dismissButton = {
            Row {
                TextButton(enabled = enabled, onClick = onDismiss) {
                    Text("Cancel")
                }
                Button(enabled = enabled, onClick = onReplace) {
                    Text("Replace")
                }
            }
        }
    )
}

private fun formatExportedAt(exportedAt: String): String = try {
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(Instant.parse(exportedAt))
} catch (_: Exception) {
    exportedAt
}

private fun pluralize(count: Int, singular: String, plural: String): String =
    if (count == 1) singular else plural
