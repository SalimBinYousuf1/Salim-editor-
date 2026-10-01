package com.example.ui.editor.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FindReplace
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EditorColors
import com.example.ui.theme.SalimRed

@Composable
fun FindAndReplaceBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    replaceQuery: String,
    onReplaceQueryChange: (String) -> Unit,
    matchCount: Int,
    currentMatchIndex: Int,
    onNextMatch: () -> Unit,
    onPrevMatch: () -> Unit,
    onReplace: () -> Unit,
    onReplaceAll: () -> Unit,
    matchCase: Boolean,
    onToggleMatchCase: () -> Unit,
    useRegex: Boolean,
    onToggleRegex: () -> Unit,
    showReplace: Boolean,
    onToggleShowReplace: () -> Unit,
    onClose: () -> Unit,
    colors: EditorColors,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Find row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceVariant)
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    cursorBrush = SolidColor(colors.accent),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Find in document…",
                                color = colors.textTertiary,
                                fontSize = 13.sp
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (searchQuery.isNotEmpty()) {
                val matchDisplay = if (matchCount > 0) "${currentMatchIndex + 1}/$matchCount" else "0/0"
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = matchDisplay,
                    fontSize = 12.sp,
                    color = if (matchCount > 0) colors.accent else SalimRed,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(
                onClick = onPrevMatch,
                enabled = matchCount > 0,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Outlined.KeyboardArrowUp,
                    contentDescription = "Previous Match",
                    tint = if (matchCount > 0) colors.textPrimary else colors.textTertiary
                )
            }

            IconButton(
                onClick = onNextMatch,
                enabled = matchCount > 0,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Next Match",
                    tint = if (matchCount > 0) colors.textPrimary else colors.textTertiary
                )
            }

            // Case sensitive [Aa]
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (matchCase) colors.accent else colors.surfaceVariant)
                    .clickable(onClick = onToggleMatchCase),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aa",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (matchCase) Color.White else colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Regex [.*]
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (useRegex) colors.accent else colors.surfaceVariant)
                    .clickable(onClick = onToggleRegex),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ".*",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (useRegex) Color.White else colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Toggle replace row
            IconButton(
                onClick = onToggleShowReplace,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Outlined.FindReplace,
                    contentDescription = "Toggle Replace",
                    tint = if (showReplace) colors.accent else colors.textPrimary
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Close Search",
                    tint = colors.textSecondary
                )
            }
        }

        // Replace row
        if (showReplace) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceVariant)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = replaceQuery,
                        onValueChange = onReplaceQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        cursorBrush = SolidColor(colors.accent),
                        decorationBox = { innerTextField ->
                            if (replaceQuery.isEmpty()) {
                                Text(
                                    text = "Replace with…",
                                    color = colors.textTertiary,
                                    fontSize = 13.sp
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(
                    onClick = onReplace,
                    enabled = matchCount > 0,
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Replace", fontSize = 12.sp, color = colors.accent)
                }

                TextButton(
                    onClick = onReplaceAll,
                    enabled = matchCount > 0,
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("All", fontSize = 12.sp, color = colors.accent)
                }
            }
        }
    }
}
