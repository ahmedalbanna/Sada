package com.example.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TaskItem
import com.example.data.VoiceNote
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SadaMainView(viewModel: SadaViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0A0A0A), // Obsidian Black
        bottomBar = {
            if (currentScreen != AppScreen.RECORDER && currentScreen != AppScreen.NOTE_DETAIL) {
                SadaBottomBar(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            if (currentScreen != AppScreen.RECORDER && currentScreen != AppScreen.NOTE_DETAIL) {
                SadaTopBar(viewModel = viewModel)
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                    AppScreen.RECORDER -> RecorderScreen(viewModel = viewModel)
                    AppScreen.NOTES_LIST -> NotesListScreen(viewModel = viewModel)
                    AppScreen.TASKS_CALENDAR -> TasksScreen(viewModel = viewModel)
                    AppScreen.SETTINGS_SUPPORT -> SettingsSupportScreen(viewModel = viewModel)
                    AppScreen.NOTE_DETAIL -> NoteDetailScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SadaTopBar(viewModel: SadaViewModel) {
    val isAnalyzing by viewModel.isAnalyzingSection.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A0A))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .drawBehind {
                drawLine(
                    color = EmeraldGreen.copy(alpha = 0.2f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isAnalyzing) RealNeonTeal else EmeraldGreen)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "صـدى",
                color = DimWhite,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(onClick = { viewModel.navigateTo(AppScreen.SETTINGS_SUPPORT) }) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = EmeraldGreen)
        }
    }
}

@Composable
fun SadaBottomBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF0A0A0A),
        tonalElevation = 0.dp,
        modifier = Modifier.drawBehind {
             drawLine(
                color = EmeraldGreen.copy(alpha = 0.1f),
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx()
            )
        }
    ) {
        val items = listOf(
            NavigationTabItem(AppScreen.DASHBOARD, "الرئيسية", Icons.Default.Dashboard),
            NavigationTabItem(AppScreen.RECORDER, "تسجيل", Icons.Default.Mic),
            NavigationTabItem(AppScreen.NOTES_LIST, "ملاحظاتي", Icons.Default.Notes),
            NavigationTabItem(AppScreen.TASKS_CALENDAR, "المهام", Icons.Default.DateRange)
        )

        items.forEach { tab ->
            val isSelected = currentScreen == tab.screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(tab.screen) },
                icon = {
                    Icon(
                        tab.icon,
                        contentDescription = tab.label,
                        tint = if (isSelected) EmeraldGreen else SoftCyberGray.copy(alpha = 0.6f)
                    )
                },
                label = {
                    Text(
                        tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) EmeraldGreen else SoftCyberGray.copy(alpha = 0.6f)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = EmeraldGreen.copy(alpha = 0.1f)
                )
            )
        }
    }
}

data class NavigationTabItem(val screen: AppScreen, val label: String, val icon: ImageVector)

// ==========================================
// 1. DASHBOARD SCREEN
// ==========================================
@Composable
fun DashboardScreen(viewModel: SadaViewModel) {
    val notes by viewModel.voiceNotes.collectAsStateWithLifecycle()
    val tasks by viewModel.taskItems.collectAsStateWithLifecycle()

    val totalNotes = notes.size
    val totalTasks = tasks.size
    val completedTasks = tasks.filter { it.isCompleted }.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Text(
                    text = "مرحباً بك في صدى 👋",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DimWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "نظم أفكارك ومهامك بصوتك بكل سهولة.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftCyberGray
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardStatCard(
                    modifier = Modifier.weight(1f),
                    label = "الملاحظات",
                    value = totalNotes.toString(),
                    icon = Icons.Default.Mic,
                    color = EmeraldGreen
                )
                DashboardStatCard(
                    modifier = Modifier.weight(1f),
                    label = "المهام",
                    value = totalTasks.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = RealNeonTeal
                )
                DashboardStatCard(
                    modifier = Modifier.weight(1f),
                    label = "المنجزة",
                    value = completedTasks.toString(),
                    icon = Icons.Default.DoneAll,
                    color = EmeraldGreen
                )
            }
        }

        item {
            Text(
                text = "آخر الملاحظات",
                style = MaterialTheme.typography.titleMedium,
                color = DimWhite,
                fontWeight = FontWeight.Bold
            )
        }

        items(notes.take(3)) { note ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateToNoteDetail(note.id) },
                colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
                border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = note.title, style = MaterialTheme.typography.titleSmall, color = DimWhite)
                    Text(
                        text = note.rawText,
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftCyberGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardStatCard(
    modifier: Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, color = DimWhite, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = SoftCyberGray)
        }
    }
}

// ==========================================
// 2. RECORDER SCREEN (Recording Mode)
// ==========================================
@Composable
fun RecorderScreen(viewModel: SadaViewModel) {
    val context = LocalContext.current
    val recordState by viewModel.recordState.collectAsStateWithLifecycle()
    val recordedText by viewModel.recordedText.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzingSection.collectAsStateWithLifecycle()

    var recordingTime by remember { mutableLongStateOf(0L) }
    
    LaunchedEffect(recordState) {
        if (recordState == RecordState.LISTENING) {
            val startTime = System.currentTimeMillis()
            while (recordState == RecordState.LISTENING) {
                recordingTime = System.currentTimeMillis() - startTime
                kotlinx.coroutines.delay(100)
            }
        } else if (recordState == RecordState.IDLE) {
            recordingTime = 0L
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = DimWhite)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (recordState == RecordState.LISTENING) "محلي (Offline)" else "جاهز",
                        style = MaterialTheme.typography.labelMedium,
                        color = SoftCyberGray
                    )
                }
            }
        },
        containerColor = Color(0xFF0A0A0A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Waveform and Timer
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LiveWaveform(isRecording = recordState == RecordState.LISTENING)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                val minutes = (recordingTime / 1000) / 60
                val seconds = (recordingTime / 1000) % 60
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.displayMedium,
                    color = DimWhite,
                    fontWeight = FontWeight.Light
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = recordedText.ifEmpty { "تحدث الآن لتسجيل ملاحظتك..." },
                    style = MaterialTheme.typography.bodyLarge,
                    color = SoftCyberGray.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 64.dp, start = 32.dp, end = 32.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.clearRecorder() }) {
                    Icon(Icons.Default.Delete, contentDescription = "Cancel", tint = MutedSlate)
                }

                RecordButton(
                    isRecording = recordState == RecordState.LISTENING,
                    isProcessing = recordState == RecordState.PROCESSING,
                    onClick = {
                        if (recordState == RecordState.LISTENING) {
                            viewModel.stopListening()
                            viewModel.submitTextForAnalysis(recordedText) { id ->
                                viewModel.navigateToNoteDetail(id)
                            }
                        } else {
                            viewModel.startListening(context)
                        }
                    }
                )

                IconButton(onClick = { /* Lock logic */ }) {
                    Icon(Icons.Default.LockOpen, contentDescription = "Lock", tint = MutedSlate)
                }
            }
        }
    }
}

@Composable
fun LiveWaveform(isRecording: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
        if (isRecording) {
            repeat(3) { i ->
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.5f + (i * 0.2f),
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200 + (i * 200), easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "ring_scale_$i"
                )
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .border(1.dp, EmeraldGreen.copy(alpha = 0.3f / (i + 1)), CircleShape)
                )
            }
        }

        Canvas(modifier = Modifier.size(100.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val barWidth = 4.dp.toPx()
            val barSpacing = 6.dp.toPx()
            val barCount = 8
            
            for (i in 0 until barCount) {
                val heightMultiplier = if (isRecording) (0.4f + (Math.random() * 0.6f).toFloat()) * waveScale else 0.2f
                val height = (size.height * heightMultiplier)
                val x = (size.width / 2) + (i - barCount / 2) * (barWidth + barSpacing)
                
                drawLine(
                    color = EmeraldGreen,
                    start = Offset(x, size.height / 2 - height / 2),
                    end = Offset(x, size.height / 2 + height / 2),
                    strokeWidth = barWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun RecordButton(isRecording: Boolean, isProcessing: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(EmeraldGreen, EmeraldGreen.copy(alpha = 0.8f), Color.Transparent)
                )
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .drawBehind {
                if (isRecording) {
                   drawCircle(
                       color = EmeraldGreen.copy(alpha = 0.2f),
                       radius = size.minDimension / 1.5f,
                       style = Stroke(width = 2.dp.toPx())
                   )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = if (isRecording) Color.White else EmeraldGreen,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color(0xFF0A0A0A), modifier = Modifier.size(24.dp))
                } else {
                    Icon(
                        if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color(0xFF0A0A0A),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. NOTE DETAIL SCREEN (Results & Summary Mode)
// ==========================================
@Composable
fun NoteDetailScreen(viewModel: SadaViewModel) {
    val note by viewModel.selectedNote.collectAsStateWithLifecycle()
    val tasks by viewModel.tasksForSelectedNote.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (note == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = EmeraldGreen)
        }
        return
    }

    val n = note!!

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(AppScreen.NOTES_LIST) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DimWhite)
                }
                Text("تفاصيل الملاحظة", style = MaterialTheme.typography.titleMedium, color = DimWhite, fontWeight = FontWeight.Bold)
                IconButton(onClick = { /* Share Logic */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Export", tint = EmeraldGreen)
                }
            }
        },
        containerColor = Color(0xFF0A0A0A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Meta Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = SimpleDateFormat("EEEE، hh:mm ص", Locale("ar")).format(Date(n.createdAt)),
                    style = MaterialTheme.typography.labelMedium,
                    color = SoftCyberGray
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmeraldGreen.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(n.category, style = MaterialTheme.typography.labelSmall, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                }
            }

            // Summary Card
            SummaryCard(n.summary)

            // Key Points (Derived from tags for now or extracted if summary is long)
            KeyPointsCard(n.tags)

            // Action Items
            ActionItemsCard(tasks, onTaskChecked = { task, checked -> 
                viewModel.updateTaskStatus(task, checked)
            })
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SummaryCard(summary: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("✨ الخلاصة التنفيذية", style = MaterialTheme.typography.titleMedium, color = DimWhite, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SoftCyberGray.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = summary.ifEmpty { "لم يتم استخراج خلاصة محددة." },
                style = MaterialTheme.typography.bodyLarge,
                color = DimWhite.copy(alpha = 0.9f),
                lineHeight = 26.sp
            )
        }
    }
}

@Composable
fun KeyPointsCard(tags: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PushPin, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("📌 النقاط الرئيسية", style = MaterialTheme.typography.titleMedium, color = DimWhite, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            tags.split("،", ",").forEach { tag ->
                if (tag.isNotBlank()) {
                    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(EmeraldGreen))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(tag.trim(), style = MaterialTheme.typography.bodyMedium, color = SoftCyberGray)
                    }
                }
            }
        }
    }
}

@Composable
fun ActionItemsCard(tasks: List<TaskItem>, onTaskChecked: (TaskItem, Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("⚡ المهام المستخرجة", style = MaterialTheme.typography.titleMedium, color = DimWhite, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (tasks.isEmpty()) {
                Text("لا توجد مهام محددة في هذه الملاحظة.", style = MaterialTheme.typography.bodySmall, color = MutedSlate)
            } else {
                tasks.forEach { task ->
                    ActionItemRow(task, onChecked = { onTaskChecked(task, it) })
                }
            }
        }
    }
}

@Composable
fun ActionItemRow(task: TaskItem, onChecked: (Boolean) -> Unit) {
    val priorityColor = when (task.priority.lowercase()) {
        "high" -> CyberPriorityHigh
        "medium" -> CyberPriorityMed
        else -> EmeraldGreen
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ObsidianSurface)
            .drawBehind {
                drawLine(
                    color = priorityColor,
                    start = Offset(size.width, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = 4.dp.toPx()
                )
            }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = onChecked,
            colors = CheckboxDefaults.colors(
                checkedColor = EmeraldGreen,
                uncheckedColor = SoftCyberGray.copy(alpha = 0.5f),
                checkmarkColor = Color(0xFF0A0A0A)
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.taskDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = if (task.isCompleted) SoftCyberGray else DimWhite,
                textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
            )
            Text(
                text = if (task.priority.lowercase() == "high") "عالية" else if (task.priority.lowercase() == "medium") "متوسطة" else "منخفضة",
                style = MaterialTheme.typography.labelSmall,
                color = priorityColor.copy(alpha = 0.8f)
            )
        }
    }
}

// ==========================================
// 4. NOTES LIST SCREEN
// ==========================================
@Composable
fun NotesListScreen(viewModel: SadaViewModel) {
    val notes by viewModel.voiceNotes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("بحث في ملاحظاتك...") },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldGreen,
                unfocusedBorderColor = SoftCyberGray.copy(alpha = 0.2f),
                focusedTextColor = DimWhite,
                unfocusedTextColor = DimWhite
            )
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(notes) { note ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateToNoteDetail(note.id) },
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
                    border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = EmeraldGreen)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = note.title, style = MaterialTheme.typography.titleMedium, color = DimWhite, fontWeight = FontWeight.Bold)
                            Text(text = note.rawText, style = MaterialTheme.typography.bodySmall, color = SoftCyberGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. TASKS SCREEN
// ==========================================
@Composable
fun TasksScreen(viewModel: SadaViewModel) {
    val tasks by viewModel.taskItems.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("مهامي المستخرجة", style = MaterialTheme.typography.headlineSmall, color = DimWhite, fontWeight = FontWeight.Bold)
        }

        items(tasks) { task ->
            ActionItemRow(task, onChecked = { viewModel.updateTaskStatus(task, it) })
        }
    }
}


// ==========================================
// 6. SETTINGS & SUPPORT SCREEN
// ==========================================
@Composable
fun SettingsSupportScreen(viewModel: SadaViewModel) {
    val logs by viewModel.syncStatusLogs.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("الإعدادات والمزامنة", style = MaterialTheme.typography.headlineSmall, color = DimWhite, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("المزامنة والنسخ الاحتياطي", style = MaterialTheme.typography.titleMedium, color = EmeraldGreen)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.triggerCloudSyncSimulation() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("مزامنة سحابية الآن ☁️", color = Color(0xFF0A0A0A))
                }
                Spacer(modifier = Modifier.height(8.dp))
                val context = LocalContext.current
                Button(
                    onClick = { viewModel.performEncryptedExport(context) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianSurface)
                ) {
                    Text("تصدير نسخة مشفرة 🔒", color = EmeraldGreen)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("سجل النظام", style = MaterialTheme.typography.titleSmall, color = SoftCyberGray)
                Spacer(modifier = Modifier.height(8.dp))
                logs.take(5).forEach { log ->
                    Text(text = log, style = MaterialTheme.typography.labelSmall, color = MutedSlate)
                }
            }
        }
    }
}
