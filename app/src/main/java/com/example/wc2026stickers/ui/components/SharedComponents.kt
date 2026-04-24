package com.example.wc2026stickers.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wc2026stickers.data.db.dao.StickerWithQuantity
import com.example.wc2026stickers.data.db.entities.StickerType

@Composable
fun StickerCard(
    sticker: StickerWithQuantity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOwned = sticker.quantityOwned > 0
    val isDuplicate = sticker.quantityOwned > 1
    val cardColor = when {
        isDuplicate -> MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
        isOwned -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (isOwned) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .aspectRatio(0.75f)
            .clip(RoundedCornerShape(8.dp))
            .background(cardColor)
            .border(
                width = if (sticker.isShiny) 2.dp else 0.5.dp,
                color = if (sticker.isShiny) MaterialTheme.colorScheme.secondary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            // Sticker type icon
            Text(
                text = when (sticker.stickerType) {
                    StickerType.BADGE -> "🛡️"
                    StickerType.TEAM_PHOTO -> "📸"
                    StickerType.PLAYER -> "⚽"
                    StickerType.SPECIAL -> "⭐"
                },
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${sticker.teamCode}-${sticker.number}",
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = sticker.label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Duplicate count badge
        if (isDuplicate) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${sticker.quantityOwned}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Shiny sparkle indicator
        if (sticker.isShiny && !isOwned) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(3.dp)
            ) {
                Text("✨", fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TeamProgressRow(
    flag: String,
    name: String,
    confederation: String,
    collected: Int,
    total: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (total > 0) collected.toFloat() / total else 0f

    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        leadingContent = {
            Text(text = flag, fontSize = 28.sp)
        },
        headlineContent = {
            Text(text = name, fontWeight = FontWeight.SemiBold)
        },
        supportingContent = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = confederation,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "$collected / $total",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (collected == total) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        trailingContent = {
            if (collected == total) {
                Text("✅", fontSize = 20.sp)
            }
        }
    )
}
