package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalEditorColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    documentId: Long,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalEditorColors.current

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var showPreview by remember { mutableStateOf(false) }

    LaunchedEffect(documentId) {
        viewModel.loadDocument(documentId)
    }

    LaunchedEffect(uiState.document) {
        uiState.document?.let { doc ->
            title = doc.title
            content = doc.content
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
    ) {
        // Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    viewModel.saveContent(documentId, title, content)
                    onBack()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }

                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    ),
                    cursorBrush = SolidColor(colors.accent),
                    modifier = Modifier.weight(1f)
                )

                if (uiState.document?.syntax == "markdown" || uiState.document?.extension == "md") {
                    IconButton(onClick = { showPreview = !showPreview }) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Preview",
                            tint = if (showPreview) colors.accent else colors.textSecondary
                        )
                    }
                }

                IconButton(onClick = { viewModel.saveContent(documentId, title, content) }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        tint = colors.accent
                    )
                }
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.accent)
            }
        } else if (showPreview) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                MarkdownPreview(
                    markdownText = content,
                    theme = EditorThemes.OneDarkPro,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            BasicTextField(
                value = content,
                onValueChange = { content = it },
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = colors.textPrimary,
                    lineHeight = 22.sp
                ),
                cursorBrush = SolidColor(colors.accent),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                decorationBox = { innerTextField ->
                    if (content.isEmpty()) {
                        Text(
                            text = "Start writing...",
                            color = colors.textTertiary,
                            fontSize = 15.sp
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}
