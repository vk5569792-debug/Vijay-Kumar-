package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChatMessage
import com.example.data.model.ChatThread
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val allThreads by viewModel.allThreads.collectAsStateWithLifecycle()
    val activeThreadId by viewModel.activeThreadId.collectAsStateWithLifecycle()
    val currentMessages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val temperature by viewModel.temperature.collectAsStateWithLifecycle()
    val customSystemPrompt by viewModel.customSystemPrompt.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()

    var showSettingsSheet by remember { mutableStateOf(false) }

    val activeThread = allThreads.find { it.id == activeThreadId }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = CyberBg,
                drawerContentColor = CyberTextPrimary,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Drawer Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "AI Assis",
                            tint = CyberPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "SYSTEM ARCHIVE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyberPrimary,
                            letterSpacing = 1.5.sp
                        )
                    }
                    
                    Divider(color = CyberBorder, modifier = Modifier.padding(bottom = 16.dp))

                    // Start New Chat Button
                    Button(
                        onClick = {
                            viewModel.startNewThread()
                            scope.launch { drawerState.close() }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberSecondary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .testTag("new_chat_drawer_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New chat")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("INITIALIZE PROTOCOL")
                    }

                    Text(
                        text = "PAST LOGS",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberTextSecondary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    // Logs List
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allThreads) { thread ->
                            val isSelected = thread.id == activeThreadId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) CyberUserBubble else Color.Transparent)
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (isSelected) CyberPrimary else Color.Transparent
                                        ),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        viewModel.selectThread(thread.id)
                                        scope.launch { drawerState.close() }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                                        contentDescription = "Thread Icon",
                                        tint = if (isSelected) CyberPrimary else CyberTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = thread.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) CyberPrimary else CyberTextPrimary,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }
                                
                                IconButton(
                                    onClick = { viewModel.deleteThread(thread.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete log",
                                        tint = Color.Red.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Divider(color = CyberBorder, modifier = Modifier.padding(vertical = 12.dp))

                    // Clear All Chats Button
                    OutlinedButton(
                        onClick = { viewModel.clearAllChats() },
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Wipe database")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PURGE DATABASE", fontSize = 12.sp)
                    }
                }
            }
        },
        modifier = modifier
    ) {
        Scaffold(
            topBar = {
                Surface(
                    color = CyberBg,
                    modifier = Modifier.statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Hamburger menu toggle
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "History Logs",
                                    tint = CyberPrimary
                                )
                            }
                            
                            // Glowing "J" Avatar
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(CyberPrimary.copy(alpha = 0.1f))
                                    .border(
                                        BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.3f)),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "J",
                                    color = CyberPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            // Title and Online Status
                            Column {
                                Text(
                                    text = "JARVIS",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val statusColor = if (isGenerating) CyberSecondary else CyberTertiary
                                    val statusText = if (isGenerating) "PROCESSING..." else "SYSTEM ONLINE"
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(statusColor)
                                    )
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = statusColor.copy(alpha = 0.8f),
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }

                        // Right action button: Personality config trigger
                        IconButton(
                            onClick = { showSettingsSheet = true },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CyberSurface.copy(alpha = 0.4f))
                                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Personality settings",
                                tint = CyberPrimary
                            )
                        }
                    }
                }
            },
            containerColor = CyberBg
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .drawBehind {
                        // Top-left sky blue glow (rgba(56, 189, 248, 0.15))
                        val topLeftGlow = Brush.radialGradient(
                            colors = listOf(
                                CyberPrimary.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = Offset(0f, 0f),
                            radius = size.minDimension * 1.2f
                        )
                        drawRect(brush = topLeftGlow)

                        // Bottom-right purple glow (rgba(124, 58, 237, 0.12))
                        val bottomRightGlow = Brush.radialGradient(
                            colors = listOf(
                                CyberSecondary.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            center = Offset(size.width, size.height),
                            radius = size.minDimension * 1.0f
                        )
                        drawRect(brush = bottomRightGlow)
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (currentMessages.isEmpty()) {
                        // Empty Chat Dashboard
                        EmptyDashboard(
                            isGenerating = isGenerating,
                            onSelectPreset = { prompt ->
                                viewModel.updateInputText(prompt)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // Active Message Feed
                        val listState = rememberLazyListState()
                        
                        // Auto-scroll on new message
                        LaunchedEffect(currentMessages.size) {
                            if (currentMessages.isNotEmpty()) {
                                listState.animateScrollToItem(currentMessages.size - 1)
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                        ) {
                            items(currentMessages) { message ->
                                ChatBubbleItem(message = message)
                            }
                            if (isGenerating) {
                                item {
                                    JarvisLoadingBubble()
                                }
                            }
                        }
                    }

                    // Bottom Pill Input Area
                    MessageInputArea(
                        text = inputText,
                        onTextChange = { viewModel.updateInputText(it) },
                        onSend = { viewModel.sendMessage() },
                        isGenerating = isGenerating,
                        modifier = Modifier
                    )

                    // Immersive Bottom Navigation Bar
                    ImmersiveNavigationBar(
                        onCoreClick = {
                            viewModel.startNewThread()
                        },
                        onArchiveClick = {
                            scope.launch { drawerState.open() }
                        },
                        onExpertClick = {
                            showSettingsSheet = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    )
                }
            }
        }
    }

    // Settings / Personality Customization Dialog
    if (showSettingsSheet) {
        AlertDialog(
            onDismissRequest = { showSettingsSheet = false },
            confirmButton = {
                TextButton(
                    onClick = { showSettingsSheet = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = CyberPrimary)
                ) {
                    Text("ENGAGE SYSTEM")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = "Core customizer", tint = CyberPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "PERSONALITY CORE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyberPrimary
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Modify JARVIS’s cognitive filters and core parameters to align response structures to your preferences.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberTextSecondary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Temperature Slider
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "COGNITIVE ENTROPY (TEMP)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                        Text(
                            String.format("%.1f", temperature),
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = temperature,
                        onValueChange = { viewModel.setTemperature(it) },
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberPrimary,
                            activeTrackColor = CyberSecondary,
                            inactiveTrackColor = CyberBorder
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Deterministic / Concise", fontSize = 9.sp, color = CyberTextSecondary)
                        Text("Creative / Theoretical", fontSize = 9.sp, color = CyberTextSecondary)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Directive Text Field
                    Text(
                        "ASSISTANT PRIMARY DIRECTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = customSystemPrompt,
                        onValueChange = { viewModel.setCustomSystemPrompt(it) },
                        placeholder = {
                            Text(
                                "e.g., Speak sarcastically like Tony Stark, explain things briefly, prioritize code examples.",
                                fontSize = 11.sp,
                                color = CyberTextSecondary.copy(alpha = 0.5f)
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = CyberTextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberPrimary,
                            unfocusedBorderColor = CyberBorder,
                            focusedContainerColor = CyberSurface,
                            unfocusedContainerColor = CyberSurface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // API Key Security warning requested by direct gemini-api prototype mandate
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberUserBubble.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, CyberBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = CyberPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "SECURITY STATEMENT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "This prototype utilizes your local Gemini API Key injected via BuildConfig. Android APKs can be decompiled, meaning keys could be extracted. Do not distribute the generated APK file publicly.",
                                fontSize = 10.sp,
                                color = CyberTextSecondary,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            },
            containerColor = CyberBg,
            textContentColor = CyberTextPrimary,
            titleContentColor = CyberPrimary,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun EmptyDashboard(
    isGenerating: Boolean,
    onSelectPreset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing animated ARC reactor
        ArcReactorCore(isGenerating = isGenerating)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "AWAITING INSTRUCTION",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = CyberPrimary.copy(alpha = 0.8f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Text(
            text = buildAnnotatedString {
                append("How can I assist you, ")
                withStyle(style = SpanStyle(color = CyberPrimary, fontWeight = FontWeight.Bold)) {
                    append("Tony")
                }
                append("?")
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Light,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Preset cards heading
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.Launch,
                contentDescription = "Presets",
                tint = CyberPrimary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "COGNITIVE MODULES",
                style = MaterialTheme.typography.labelSmall,
                color = CyberPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Preset Horizontal Slider
        val presets = listOf(
            PresetPrompt(
                "Code Analysis",
                Icons.Default.Code,
                "Write a clean Kotlin extension function to format timestamps to readable local time.",
                "Debug complex logic or optimize scripts"
            ),
            PresetPrompt(
                "Creative Ideas",
                Icons.Default.Lightbulb,
                "Suggest 5 unique startup ideas merging AI with community gardening.",
                "Generate concepts, prompts, or scripts"
            ),
            PresetPrompt(
                "Study Guide",
                Icons.Default.School,
                "Explain quantum cryptography in simple terms with analogies.",
                "Conceptual training"
            ),
            PresetPrompt(
                "Copywriter",
                Icons.Default.Edit,
                "Draft a professional response to a recruiter accepting a technical interview invite.",
                "Content composition"
            ),
            PresetPrompt(
                "Prompt Generator",
                Icons.Default.Palette,
                "Generate a highly detailed prompt for Midjourney to create a cyberpunk neon city alleyway, 8k.",
                "Image instructions"
            ),
            PresetPrompt(
                "Video Script",
                Icons.Default.Movie,
                "Write a 1-minute YouTube Shorts script introducing the concept of holographic memory storage.",
                "Video scripts"
            )
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(presets) { preset ->
                Card(
                    modifier = Modifier
                        .width(165.dp)
                        .height(145.dp)
                        .clickable { onSelectPreset(preset.text) },
                    colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CyberPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = preset.icon,
                                    contentDescription = preset.title,
                                    tint = CyberPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Use",
                                tint = CyberTextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = preset.description,
                                fontSize = 10.sp,
                                color = CyberTextSecondary,
                                maxLines = 2,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

data class PresetPrompt(
    val title: String,
    val icon: ImageVector,
    val text: String,
    val description: String
)

@Composable
fun ChatBubbleItem(message: ChatMessage) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val containerColor = if (isUser) CyberUserBubble else CyberSurface
    val borderStroke = if (isUser) {
        BorderStroke(1.dp, CyberSecondary.copy(alpha = 0.6f))
    } else {
        BorderStroke(1.dp, CyberBorder)
    }
    
    val timestampFormatted = remember(message.timestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isUser) 48.dp else 0.dp,
                end = if (isUser) 0.dp else 48.dp
            ),
        horizontalAlignment = alignment
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = borderStroke,
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isUser) 12.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 12.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Sender label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isUser) Icons.Default.Person else Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = if (isUser) CyberSecondary else CyberPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isUser) "OPERATOR" else "JARVIS AI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) CyberSecondary else CyberPrimary,
                        letterSpacing = 1.sp
                    )
                }

                // Code block detection or clean body rendering
                val isCodeSnippet = message.content.trim().startsWith("```") && message.content.contains("```")
                
                if (isCodeSnippet) {
                    val rawLines = message.content.split("\n")
                    val codeContent = rawLines
                        .filter { !it.trim().startsWith("```") }
                        .joinToString("\n")
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = codeContent,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CyberPrimary,
                            lineHeight = 16.sp
                        )
                    }
                } else {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyberTextPrimary,
                        lineHeight = 22.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = timestampFormatted,
                        fontSize = 8.sp,
                        color = CyberTextSecondary.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun JarvisLoadingBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val animOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val animOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 130, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val animOffset3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 260, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 48.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = BorderStroke(1.dp, CyberBorder),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = 2.dp,
                bottomEnd = 12.dp
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "JARVIS ANALYZING",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .offset(y = animOffset1.dp)
                            .clip(CircleShape)
                            .background(CyberPrimary)
                    )
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .offset(y = animOffset2.dp)
                            .clip(CircleShape)
                            .background(CyberPrimary)
                    )
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .offset(y = animOffset3.dp)
                            .clip(CircleShape)
                            .background(CyberPrimary)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageInputArea(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isGenerating: Boolean,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CyberBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rounded Text Field
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = {
                Text(
                    text = "Transmit request to JARVIS...",
                    fontSize = 13.sp,
                    color = CyberTextSecondary.copy(alpha = 0.6f)
                )
            },
            modifier = Modifier
                .weight(1f)
                .testTag("chat_input_text_field"),
            shape = RoundedCornerShape(24.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = CyberTextPrimary),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberPrimary,
                unfocusedBorderColor = CyberBorder,
                focusedContainerColor = CyberSurface,
                unfocusedContainerColor = CyberSurface
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (text.isNotEmpty() && !isGenerating) {
                        onSend()
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                }
            ),
            trailingIcon = {
                // Dummy high-tech attachment visual decoration
                Row {
                    Icon(
                        imageVector = Icons.Outlined.Mic,
                        contentDescription = "Voice prompt",
                        tint = CyberTextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 4.dp)
                    )
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Cyber Glowing Send Button
        IconButton(
            onClick = {
                if (text.isNotEmpty() && !isGenerating) {
                    onSend()
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            },
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    if (text.isNotEmpty() && !isGenerating) {
                        CyberPrimary
                    } else {
                        CyberBorder
                    }
                )
                .testTag("send_message_button"),
            enabled = text.isNotEmpty() && !isGenerating
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Transmit request",
                tint = if (text.isNotEmpty() && !isGenerating) CyberBg else CyberTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun ArcReactorCore(isGenerating: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "arc_reactor")
    
    // Pulse animation (breathing)
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Rotate animation (spinning)
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isGenerating) 1500 else 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )

    val coreColor = if (isGenerating) CyberTertiary else CyberPrimary
    val glowColor = if (isGenerating) CyberTertiary.copy(alpha = 0.4f) else CyberAccentGlow.copy(alpha = 0.4f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(140.dp)
            .scale(scale)
            .padding(16.dp)
    ) {
        // Outer Glow Circle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        color = glowColor,
                        radius = size.minDimension / 2f,
                        style = Stroke(width = 8.dp.toPx())
                    )
                }
        )

        // Rotating Tech Ring
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .drawBehind {
                    val radius = size.minDimension / 2f
                    drawCircle(
                        color = coreColor,
                        radius = radius,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(15f, 15f),
                                0f
                            )
                        )
                    )
                }
        )

        // Inner Tech Ring
        Box(
            modifier = Modifier
                .fillMaxSize(0.6f)
                .drawBehind {
                    val radius = size.minDimension / 2f
                    drawCircle(
                        color = coreColor.copy(alpha = 0.7f),
                        radius = radius,
                        style = Stroke(
                            width = 1.5f.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(8f, 12f),
                                0f
                            )
                        )
                    )
                }
        )

        // Center Solid Core
        Box(
            modifier = Modifier
                .size(24.dp)
                .drawBehind {
                    drawCircle(color = coreColor)
                }
        )
    }
}

@Composable
fun ImmersiveNavigationBar(
    onCoreClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onExpertClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CyberBg,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Core Tab (active indicator style)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onCoreClick() }
                    .padding(vertical = 4.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CyberPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Core",
                        tint = CyberPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "CORE",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontSize = 10.sp
                )
            }

            // Archive Tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onArchiveClick() }
                    .padding(vertical = 4.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Archive",
                        tint = CyberTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "ARCHIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberTextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontSize = 10.sp
                )
            }

            // Expert Tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onExpertClick() }
                    .padding(vertical = 4.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Expert",
                        tint = CyberTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "EXPERT",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberTextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontSize = 10.sp
                )
            }
        }
    }
}
