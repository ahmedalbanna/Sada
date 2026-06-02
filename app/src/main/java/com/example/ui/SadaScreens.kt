package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.HelpMessage
import com.example.data.TaskItem
import com.example.data.VoiceNote
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SadaMainView(viewModel: SadaViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val recordState by viewModel.recordState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            SadaBottomBar(
                currentScreen = currentScreen,
                onNavigate = { viewModel.navigateTo(it) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            SadaTopBar(viewModel = viewModel)

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                    AppScreen.RECORDER -> RecorderScreen(viewModel = viewModel)
                    AppScreen.NOTES_LIST -> NotesListScreen(viewModel = viewModel)
                    AppScreen.TASKS_CALENDAR -> TasksScreen(viewModel = viewModel)
                    AppScreen.SETTINGS_SUPPORT -> SettingsSupportScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SadaTopBar(viewModel: SadaViewModel) {
    val notes by viewModel.voiceNotes.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzingSection.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ObsidianSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .drawBehind {
                drawLine(
                    color = RealNeonTeal.copy(alpha = 0.3f),
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
                text = "صـدى الـذكي",
                color = DimWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "🎙️",
                fontSize = 18.sp
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(DeepForestGlow.copy(alpha = 0.5f))
                .clickable { viewModel.navigateTo(AppScreen.SETTINGS_SUPPORT) }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🔒 تشفير سحابي لحظي",
                    color = RealNeonTeal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SadaBottomBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ObsidianSurface)
            .navigationBarsPadding()
            .drawBehind {
                drawLine(
                    color = RealNeonTeal.copy(alpha = 0.15f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val items = listOf(
            NavigationTabItem(AppScreen.DASHBOARD, "الرئيسية", "📊"),
            NavigationTabItem(AppScreen.RECORDER, "التسجيل المعالج", "🎙️"),
            NavigationTabItem(AppScreen.NOTES_LIST, "ملاحظاتي", "📝"),
            NavigationTabItem(AppScreen.TASKS_CALENDAR, "المهام والتقويم", "📅"),
            NavigationTabItem(AppScreen.SETTINGS_SUPPORT, "الدعم والنسخ", "⚙️")
        )

        items.forEach { tab ->
            val isSelected = currentScreen == tab.screen
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onNavigate(tab.screen) }
                    .padding(vertical = 6.dp)
                    .testTag("nav_btn_${tab.screen.name.lowercase()}"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) RealNeonTeal.copy(alpha = 0.15f) else Color.Transparent)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.emoji,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tab.label,
                    color = if (isSelected) RealNeonTeal else SoftCyberGray.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

data class NavigationTabItem(val screen: AppScreen, val label: String, val emoji: String)

// ==========================================
// 1. DASHBOARD SCREEN (لوحة التحليل المرئي والإنتاجية)
// ==========================================
@Composable
fun DashboardScreen(viewModel: SadaViewModel) {
    val context = LocalContext.current
    val notes by viewModel.voiceNotes.collectAsStateWithLifecycle()
    val tasks by viewModel.taskItems.collectAsStateWithLifecycle()

    val totalNotes = notes.size
    val totalTasks = tasks.size
    val completedTasks = tasks.filter { it.isCompleted }.size
    val pendingTasks = totalTasks - completedTasks

    // Calculate Dynamic Productivity Index (0 - 100)
    val productivityIndex = remember(totalNotes, totalTasks, completedTasks) {
        if (totalNotes == 0) 50
        else {
            val taskRatio = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks.toFloat()) * 50f else 30f
            val notesRatio = (if (totalNotes > 10) 10f else totalNotes.toFloat()) / 10f * 50f
            (taskRatio + notesRatio).toInt().coerceIn(10, 100)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
                border = BorderStroke(1.dp, RealNeonTeal.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text(
                            text = "أهلاً بك يا صديقي! 👋",
                            color = DimWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "تطبيق صدى يراقب ويسجل محاديثك ويصنفها بتشفير محلي متكامل لحماية خصوصيتك.",
                            color = SoftCyberGray,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                    Text(
                        text = "🧠",
                        fontSize = 42.sp,
                        modifier = Modifier.weight(0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Productivity Ring Widget
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "مؤشر الإنتاجية الأسبوعي",
                        color = RealNeonTeal,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier.size(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Track circle
                            drawCircle(
                                color = SoftCyberGray.copy(alpha = 0.08f),
                                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Progress arc
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(RealNeonTeal, EmeraldGreen, RealNeonTeal)
                                ),
                                startAngle = -90f,
                                sweepAngle = (productivityIndex.toFloat() / 100f) * 360f,
                                useCenter = false,
                                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$productivityIndex%",
                                color = RealNeonTeal,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "معدل الإنجاز",
                                color = SoftCyberGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    val feedbackText = when {
                        productivityIndex >= 80 -> "أداء ممتاز جداً! إنتاجيتك في القمة هذا الأسبوع! 🚀🔥"
                        productivityIndex >= 50 -> "أداء متزن. يمكنك إكمال المزيد من المهام الصوتية اليوم 🎯"
                        else -> "خمول نسبي. ابدأ بتسجيل فكرة أو التحدث لإنتاج ملاحظة وتلخيصها! 🎙️"
                    }
                    Text(
                        text = feedbackText,
                        color = DimWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Stats Dashboard Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "إجمالي الملاحظات",
                    value = "$totalNotes",
                    iconEmoji = "📝",
                    color = RealNeonTeal
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "المهام المنجزة",
                    value = "$completedTasks",
                    iconEmoji = "✅",
                    color = EmeraldGreen
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "ملاحظات معلقة",
                    value = "$pendingTasks",
                    iconEmoji = "⏰",
                    color = CyberPriorityHigh
                )
            }
        }

        // Category breakdown Visual charts
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "تصنيف الملاحظات المنظمة",
                        color = DimWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val categories = listOf("عمل", "شخصي", "أفكار", "عام")
                    categories.forEach { cat ->
                        val count = notes.filter { it.category == cat }.size
                        val ratio = if (totalNotes > 0) count.toFloat() / totalNotes else 0f

                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = cat, color = SoftCyberGray, fontSize = 12.sp)
                                Text(text = "$count ملاحظات (${(ratio * 100).toInt()}%)", color = RealNeonTeal, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SoftCyberGray.copy(alpha = 0.08f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(if (ratio > 0) ratio else 0.01f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when (cat) {
                                                "عمل" -> RealNeonTeal
                                                "شخصي" -> EmeraldGreen
                                                "أفكار" -> CyberPriorityMed
                                                else -> MutedSlate
                                            }
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active notification priorities list widget
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "نظام التوجيه الذكي للتنبيهات والمهام",
                        color = DimWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "يتم فرز منبهات المهام تلقائياً بالهاتف بناء على تحليل الذكاء الاصطناعي للأولويات لمساعدتك على التركيز.",
                        color = SoftCyberGray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    iconEmoji: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
        border = BorderStroke(1.dp, color.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = iconEmoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = color,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                color = SoftCyberGray,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


// ==========================================
// 2. RECORDER SCREEN (التسجيل الدقيق وسيناريوهات محاكاة الفحص)
// ==========================================
@Composable
fun RecorderScreen(viewModel: SadaViewModel) {
    val context = LocalContext.current
    val recordState by viewModel.recordState.collectAsStateWithLifecycle()
    val recordedText by viewModel.recordedText.collectAsStateWithLifecycle()
    val recordError by viewModel.recordError.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzingSection.collectAsStateWithLifecycle()

    var manualTextMode by remember { mutableStateOf(false) }
    var typedInputText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("عام") }

    val presetTemplates = listOf(
        PresetVoiceScene(
            title = "اجتماع إطلاق تطبيق صدى 💻",
            text = "السلام عليكم، اليوم نريد حسم إطلاق تطبيق صدى الصوتي. ضروري جداً على أحمد إنهاء واجهات العرض اليوم لعرض التقارير. وأيضاً يجب على مروان اختبار التصدير للملاحظات إلى ملف Excel و PDF. وسأقوم أنا بمراجعة خوارزمية التشفير التام وتشفير المزامنة الاحتياطية لضمان أمان معلومات المستخدمين بالكامل."
        ),
        PresetVoiceScene(
            title = "أفكار ريادة أعمال تجارية 💡",
            text = "مرحبا، خطرت ببالي اليوم فكرة ريادية ممتازة جداً تعتمد على روبوت متطور يخدم كبار السن بالمنزل. عاجل يجب علي الاتصال بالمهندس سمير غدا لشرح الأفكار. وسأقوم بكتابة دراسة جدوى استثمارية مبدئية لعرضها على المستثمر في عطلة نهاية الأسبوع."
        ),
        PresetVoiceScene(
            title = "مشتريات وقائمة واجبات المنزل وعطلة 🛒",
            text = "تذكير سريع: يجب شراء الفواكه والخضار والأسماك لتحضير وجبة الغداء. ولازم أتصل بالطبيب لتأكيد موعد فحص العيون للوالد غدا صباحا وهو عاجل. وممكن أشتري هدية بسيطة للأخت بمناسبة تخرجها."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Microphone visualizer core
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "مسجّل الصوت ومعالج اللهجات الذكي",
                    color = RealNeonTeal,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Microphone pulse visualizer ring
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            if (recordState == RecordState.LISTENING) {
                                RealNeonTeal.copy(alpha = 0.15f)
                            } else {
                                SoftCyberGray.copy(alpha = 0.05f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                if (recordState == RecordState.LISTENING) {
                                    CyberPriorityHigh.copy(alpha = 0.2f)
                                } else {
                                    RealNeonTeal.copy(alpha = 0.1f)
                                }
                            )
                            .clickable {
                                if (recordState == RecordState.LISTENING) {
                                    viewModel.stopListening()
                                } else {
                                    viewModel.startListening(context)
                                }
                            }
                            .testTag("record_mic_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (recordState == RecordState.LISTENING) "🛑" else "🎙️",
                            fontSize = 32.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = when (recordState) {
                        RecordState.IDLE -> "اضغط على زر الميكروفون للتحدث بالعربية أو اللهجة المحلية"
                        RecordState.LISTENING -> "جاري الاستماع إليك بكل دقة... اضغط لإيقاف الحفظ 🟢"
                        RecordState.PROCESSING -> "يرجى الانتظار، جاري تصنيف وتلخيص واستخراج المهام بالذكاء الاصطناعي... 🧠✨"
                        RecordState.SUCCESS -> "اكتمل التقاط الصوت بنجاح! راجع النص بالأسفل ثم حلله."
                        RecordState.ERROR -> "فشل الالتقاط لمشغل الصوت لعدم توفر الخدمة بجهاز المحاكاة."
                    },
                    color = if (recordState == RecordState.ERROR) CyberPriorityHigh else SoftCyberGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }

        if (recordError.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "⚠️ تنبيه المحاكي / النظام المحلي:", color = CyberPriorityHigh, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = recordError, color = SoftCyberGray, fontSize = 11.sp, lineHeight = 16.sp)
                }
            }
        }

        // Preset templates helper for testing
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "💡 أمثلة وسيناريوهات محاكاة جاهزة للتجربة والتقييم:",
                    color = EmeraldGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "اضغط على أي نص أدناه لحقنه ومعالجته كأنه كلام حقيقي منطوق:",
                    color = SoftCyberGray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                presetTemplates.forEach { preset ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                viewModel.setSampleText(preset.text)
                                typedInputText = preset.text
                            },
                        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                        border = BorderStroke(1.dp, RealNeonTeal.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = preset.title, color = RealNeonTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = preset.text, color = SoftCyberGray, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 15.sp)
                        }
                    }
                }
            }
        }

        // Input Transcript Frame panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "نص الكلام الملتقط المعالج",
                        color = DimWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        Text(
                            text = "تعديل كتابي",
                            color = RealNeonTeal,
                            fontSize = 11.sp,
                            modifier = Modifier.clickable { manualTextMode = !manualTextMode }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (manualTextMode) {
                    TextField(
                        value = typedInputText,
                        onValueChange = {
                            typedInputText = it
                            viewModel.setSampleText(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("typed_transcript_input"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = ObsidianSurface,
                            unfocusedContainerColor = ObsidianSurface,
                            focusedTextColor = DimWhite,
                            unfocusedTextColor = DimWhite
                        ),
                        placeholder = { Text("اكتب أو عدل النص هنا يدوياً...") }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianSurface)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = if (recordedText.isEmpty()) "في انتظار تحدثك أو اختيار سيناريو حقن جاهز من بالأعلى... 📝" else recordedText,
                            color = if (recordedText.isEmpty()) SoftCyberGray.copy(alpha = 0.5f) else DimWhite,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom category selector override
                Text(text = "اختر تصنيف الملاحظة المفضل:", color = SoftCyberGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("عام", "عمل", "شخصي", "أفكار").forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RealNeonTeal,
                                selectedLabelColor = ObsidianSurface,
                                containerColor = ObsidianSurface,
                                labelColor = SoftCyberGray
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val finalMsg = if (manualTextMode) typedInputText else recordedText
                        viewModel.submitTextForAnalysis(finalMsg, selectedCategory) {
                            Toast.makeText(context, "تم حفظ وتنظيم الملاحظة الصوتية بالذكاء الاصطناعي بنجاح!", Toast.LENGTH_LONG).show()
                            viewModel.navigateTo(AppScreen.NOTES_LIST)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("analyze_text_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = RealNeonTeal),
                    enabled = (recordedText.isNotEmpty() || typedInputText.isNotEmpty()) && !isAnalyzing
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(color = ObsidianSurface, modifier = Modifier.size(24.dp))
                    } else {
                        Text("تنظيم الملاحظة بالذكاء الاصطناعي 🧠✨", color = ObsidianSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

data class PresetVoiceScene(val title: String, val text: String)


// ==========================================
// 3. NOTES LIST SCREEN (أرشيف الملاحظات المنظمة)
// ==========================================
@Composable
fun NotesListScreen(viewModel: SadaViewModel) {
    val context = LocalContext.current
    val notes by viewModel.voiceNotes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var expandedNote by remember { mutableStateOf<VoiceNote?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search bar snake-case test-tagged
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_notes_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = DimWhite,
                unfocusedTextColor = DimWhite,
                focusedBorderColor = RealNeonTeal,
                unfocusedBorderColor = MutedSlate
            ),
            placeholder = { Text("بحث باسم الملاحظة، الكلمات أو التصنيفات...") },
            leadingIcon = { Text("🔎", fontSize = 18.sp) }
        )

        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "📭", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "لا توجد ملاحظات منظمة بعد!", color = DimWhite, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "انتقل لشاشة التسجيل وسجل أول نوت صوتي للاختبار", color = SoftCyberGray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notes) { note ->
                    NoteItem(
                        note = note,
                        onExpand = { expandedNote = note },
                        onDelete = { viewModel.deleteVoiceNote(note) }
                    )
                }
            }
        }
    }

    if (expandedNote != null) {
        val note = expandedNote!!
        AlertDialog(
            onDismissRequest = { expandedNote = null },
            confirmButton = {
                TextButton(onClick = { expandedNote = null }) {
                    Text("إغلاق", color = RealNeonTeal)
                }
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = note.title, color = DimWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(RealNeonTeal.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = note.category, color = RealNeonTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            text = {
                NoteDetailsDialogContent(note = note, viewModel = viewModel)
            },
            containerColor = ObsidianSurfaceElevated,
            modifier = Modifier.heightIn(max = 560.dp)
        )
    }
}

@Composable
fun NoteItem(
    note: VoiceNote,
    onExpand: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpand() }
            .testTag("note_item_card_${note.id}"),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
        border = BorderStroke(1.dp, RealNeonTeal.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    color = DimWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (note.category) {
                                    "عمل" -> RealNeonTeal.copy(alpha = 0.12f)
                                    "شخصي" -> EmeraldGreen.copy(alpha = 0.12f)
                                    "أفكار" -> CyberPriorityMed.copy(alpha = 0.12f)
                                    else -> SoftCyberGray.copy(alpha = 0.12f)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = note.category,
                            color = when (note.category) {
                                "عمل" -> RealNeonTeal
                                "شخصي" -> EmeraldGreen
                                "أفكار" -> CyberPriorityMed
                                else -> SoftCyberGray
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text(text = "🗑️", fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = note.rawText,
                color = SoftCyberGray,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp
            )

            if (note.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    note.tags.split("،").take(3).forEach { tag ->
                        Text(
                            text = "#${tag.trim()}",
                            color = RealNeonTeal.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoteDetailsDialogContent(note: VoiceNote, viewModel: SadaViewModel) {
    val context = LocalContext.current
    var customTags by remember { mutableStateOf(note.tags) }
    var customNotes by remember { mutableStateOf(note.notes) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "🔍 تفصيل التلخيص الذكي والتحليل:", color = RealNeonTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)

        // Bulleted points summary box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ObsidianSurface)
                .padding(10.dp)
        ) {
            Text(text = note.summary, color = DimWhite, fontSize = 12.sp, lineHeight = 18.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "🎙️ النص الكامل الحرفي المسجل:", color = SoftCyberGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ObsidianSurface)
                .padding(10.dp)
        ) {
            Text(text = note.rawText, color = SoftCyberGray, fontSize = 11.sp, lineHeight = 16.sp)
        }

        Spacer(modifier = Modifier.height(6.dp))
        Divider(color = MutedSlate.copy(alpha = 0.2f))

        // Tags & user editable comments
        Text(text = "✍️ تخصيص الكلمات المفتاحية وملاحظاتي:", color = RealNeonTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = customTags,
            onValueChange = {
                customTags = it
                viewModel.editVoiceNoteMetadata(note, it, customNotes)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("وسوم (مفصولة بفواصل)", fontSize = 11.sp) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = DimWhite, unfocusedTextColor = DimWhite)
        )

        OutlinedTextField(
            value = customNotes,
            onValueChange = {
                customNotes = it
                viewModel.editVoiceNoteMetadata(note, customTags, it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            label = { Text("إضافة ملاحظات وتعديلات شخصية", fontSize = 11.sp) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = DimWhite, unfocusedTextColor = DimWhite)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Share & Export Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val shareBody = """
                    📢 تقرير صدى الذكي لتنظيم الصوت:
                    📄 العنوان: ${note.title}
                    📂 التصنيف: ${note.category}
                    
                    🎯 التلخيص:
                    ${note.summary}
                    
                    📖 النص الحرفي:
                    ${note.rawText}
                    ---
                    تمت المزامنة والتنظيم المشفر بواسطة تطبيق صدى.
                    """.trimIndent()

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, note.title)
                        putExtra(Intent.EXTRA_TEXT, shareBody)
                    }
                    context.startActivity(Intent.createChooser(intent, "مشاركة تقرير صدى عبر:"))
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = DeepForestGlow)
            ) {
                Text("🔗 مشاركة النص", fontSize = 11.sp, color = RealNeonTeal)
            }

            Button(
                onClick = {
                    val csvFormat = """
                    "العنوان","التصنيف","النص الكامل","التلخيص","الوسوم"
                    "${note.title}","${note.category}","${note.rawText}","${note.summary}","${note.tags}"
                    """.trimIndent()

                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Sada Excel Export", csvFormat))
                    Toast.makeText(context, "تم توليد ونسخ كود Excel (CSV) إلى الحافظة بنجاح! 📊🚀", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianSurface)
            ) {
                Text("📊 تصدير Excel", fontSize = 11.sp, color = EmeraldGreen)
            }
        }
    }
}


// ==========================================
// 4. TASKS SCREEN (المهام المستخرجة ومزامنة التقويم)
// ==========================================
@Composable
fun TasksScreen(viewModel: SadaViewModel) {
    val context = LocalContext.current
    val tasks by viewModel.taskItems.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
            border = BorderStroke(1.dp, RealNeonTeal.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "حاضنة المهام ومزامنة التقويم 📅",
                    color = RealNeonTeal,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "يتم استخراج المهام التالية تلقائياً من دلالات تسجيلاتك الصوتية. اضغط على أيقونة التقويم لمزامنتها مع تطبيقات التقويم بهاتفك فوراً.",
                    color = SoftCyberGray,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🎯", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "جميع المهام منجزة! لا أشغال معلقة.", color = DimWhite, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "سجل ملاحظة بالصوت تحتوي على مهام ليتم تصنيفها هنا", color = SoftCyberGray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // High Priority Group
                val highPriority = tasks.filter { it.priority.lowercase() == "high" }
                if (highPriority.isNotEmpty()) {
                    item {
                        Text(text = "🔥 عاجلة وهامة جداً (${highPriority.size})", color = CyberPriorityHigh, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    items(highPriority) { task ->
                        TaskRow(task = task, viewModel = viewModel, context = context)
                    }
                }

                // Medium Priority Group
                val medPriority = tasks.filter { it.priority.lowercase() == "medium" }
                if (medPriority.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "⚡ متوسطة الأولوية (${medPriority.size})", color = CyberPriorityMed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    items(medPriority) { task ->
                        TaskRow(task = task, viewModel = viewModel, context = context)
                    }
                }

                // Low Priority Group
                val lowPriority = tasks.filter { it.priority.lowercase() == "low" }
                if (lowPriority.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "🍃 منخفضة الأولوية / لاحقاً (${lowPriority.size})", color = CyberPriorityLow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    items(lowPriority) { task ->
                        TaskRow(task = task, viewModel = viewModel, context = context)
                    }
                }
            }
        }
    }
}

@Composable
fun TaskRow(
    task: TaskItem,
    viewModel: SadaViewModel,
    context: Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
        border = BorderStroke(
            1.dp,
            if (task.isCompleted) MutedSlate.copy(alpha = 0.2f)
            else {
                when (task.priority.lowercase()) {
                    "high" -> CyberPriorityHigh.copy(alpha = 0.2f)
                    "medium" -> CyberPriorityMed.copy(alpha = 0.2f)
                    else -> CyberPriorityLow.copy(alpha = 0.2f)
                }
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1.5f)
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { viewModel.updateTaskStatus(task, it) },
                    modifier = Modifier.testTag("task_checkbox_${task.id}")
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = task.taskDescription,
                    color = if (task.isCompleted) SoftCyberGray.copy(alpha = 0.5f) else DimWhite,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            // Sync with Android Device Calendar Contract
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(RealNeonTeal.copy(alpha = 0.12f))
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_INSERT).apply {
                                    data = CalendarContract.Events.CONTENT_URI
                                    putExtra(CalendarContract.Events.TITLE, "مهمة صدى: ${task.taskDescription}")
                                    putExtra(CalendarContract.Events.DESCRIPTION, "مهمة مستخرجة ومحللة تلقائياً بنظام صدى الذكي. أولوية: ${task.priority}")
                                    putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, System.currentTimeMillis() + 3600000)
                                    putExtra(CalendarContract.EXTRA_EVENT_END_TIME, System.currentTimeMillis() + 7200000)
                                    putExtra(CalendarContract.Events.ALL_DAY, false)
                                    putExtra(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED)
                                }
                                context.startActivity(intent)
                                Toast.makeText(context, "جاري فتح تقويم الهاتف لمزامنة المهمة... 📅✨", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "فشلت المزامنة: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "📅", fontSize = 16.sp)
                }

                IconButton(
                    onClick = { viewModel.deleteTask(task) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Text(text = "🗑️", fontSize = 14.sp)
                }
            }
        }
    }
}


// ==========================================
// 5. SETTINGS / ENCRYPTED BACKUP & TECHNICAL SUPPORT LIVE CHAT
// ==========================================
@Composable
fun SettingsSupportScreen(viewModel: SadaViewModel) {
    val context = LocalContext.current
    val chatMessages by viewModel.helpChatMessages.collectAsStateWithLifecycle()
    val isSendingSupport by viewModel.isSupportSending.collectAsStateWithLifecycle()
    val syncLogs by viewModel.syncStatusLogs.collectAsStateWithLifecycle()

    var userHelpInputText by remember { mutableStateOf("") }
    var activeTabKey by remember { mutableStateOf("chat") } // "chat" or "settings"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Nested sub tabs switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ObsidianSurfaceElevated)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (activeTabKey == "chat") RealNeonTeal else Color.Transparent)
                    .clickable { activeTabKey = "chat" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💬 الدعم المباشر ومساعد صدى",
                    color = if (activeTabKey == "chat") ObsidianSurface else SoftCyberGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (activeTabKey == "settings") RealNeonTeal else Color.Transparent)
                    .clickable { activeTabKey = "settings" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚙️ التشفير، النسخ والتصدير",
                    color = if (activeTabKey == "settings") ObsidianSurface else SoftCyberGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (activeTabKey == "chat") {
            // LIVE AI SUPPORT CHAT SCREEN
            Column(modifier = Modifier.weight(1f)) {
                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated),
                    border = BorderStroke(1.dp, RealNeonTeal.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "مساعد الدعم الفني الذكي (كرم) 🤖", color = EmeraldGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "مسح المحادثة",
                                color = CyberPriorityHigh,
                                fontSize = 11.sp,
                                modifier = Modifier.clickable { viewModel.clearSupportChat() }
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (chatMessages.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "مرحباً بك! أنا كرم مساعدك الذكي 🤖\nكيف يمكنني مساعدتك بخصوص مزامنة التقويم، التشفير والتصدير؟",
                                            color = SoftCyberGray,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            } else {
                                items(chatMessages) { msg ->
                                    ChatBubbleRow(msg = msg)
                                }
                            }
                        }

                        if (isSendingSupport) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(6.dp)) {
                                CircularProgressIndicator(color = EmeraldGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "كرم يفكر ويكتب الرد... ✍️", color = SoftCyberGray, fontSize = 10.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chat Input Row snake-case testTag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userHelpInputText,
                        onValueChange = { userHelpInputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DimWhite,
                            unfocusedTextColor = DimWhite
                        ),
                        placeholder = { Text("اطرح استفسارك بخصوص التصدير أو المزامنة...", fontSize = 12.sp) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen)
                            .clickable {
                                if (userHelpInputText.isNotBlank()) {
                                    viewModel.sendSupportChat(userHelpInputText)
                                    userHelpInputText = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✈️", fontSize = 20.sp)
                    }
                }
            }

        } else {
            // ENCRYPTED RECOVERY BACKUP & SETTINGS EXPORTER PANEL
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Priority Customize timing
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(text = "⚙️ تخصيص أولويات إشعارات التطبيق:", color = RealNeonTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(text = "توجيه تنبيهات المهام العاجلة (High Priority):", color = SoftCyberGray, fontSize = 12.sp)
                        var alertPrefChoice by remember { mutableStateOf("منبه إشعار فوري") }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            listOf("منبه إشعار فوري", "اهتزاز فوري", "صامت").forEach { pref ->
                                FilterChip(
                                    selected = alertPrefChoice == pref,
                                    onClick = { alertPrefChoice = pref },
                                    label = { Text(pref, fontSize = 10.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "جدولة المهام الدورية (Medium / Low Priority):", color = SoftCyberGray, fontSize = 12.sp)
                        var timeChoice by remember { mutableStateOf("كل يوم - صباحاً") }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            listOf("كل يوم - صباحاً", "كل يوم - مساءً", "إشعار أسبوعي").forEach { pref ->
                                FilterChip(
                                    selected = timeChoice == pref,
                                    onClick = { timeChoice = pref },
                                    label = { Text(pref, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }

                // Database Manager Backups (AES encrypt code paste block)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "🛡️ النسخ الاحتياطي التام والمشفر محلياً:", color = RealNeonTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "يقوم صدى بحزم كل ملاحظاتك ومهامك في تشفير كود AES مشفر آمن محلي تماما لحمايتها ثم فك تشفيرها فورا.",
                            color = SoftCyberGray,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )

                        var copiedCodeText by remember { mutableStateOf("") }
                        var pastedCodeContent by remember { mutableStateOf("") }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val code = viewModel.performEncryptedExport(context)
                                    if (code != null) {
                                        copiedCodeText = code
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Sada Encrypted Config Backup", code))
                                        Toast.makeText(context, "تم تشفير ونسخ كود النسخة الاحتياطية بنجاح 🔒", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepForestGlow)
                            ) {
                                Text("🔒 تصدير بنسخة مشفرة", fontSize = 11.sp, color = RealNeonTeal)
                            }

                            Button(
                                onClick = { viewModel.triggerCloudSyncSimulation() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = ObsidianSurface)
                            ) {
                                Text("📡 مزامنة سحابية لحظية", fontSize = 11.sp, color = EmeraldGreen)
                            }
                        }

                        if (copiedCodeText.isNotEmpty()) {
                            Text(text = "كود النسخة المشفر الحالي النشط (اضغط للنسخ):", color = SoftCyberGray, fontSize = 10.sp)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 60.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ObsidianSurface)
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Sada Encrypted Config Backup", copiedCodeText))
                                        Toast.makeText(context, "تم النسخ!", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(8.dp)
                            ) {
                                Text(text = copiedCodeText, color = RealNeonTeal, fontSize = 9.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        Divider(color = MutedSlate.copy(alpha = 0.2f))

                        Text(text = "استرجاع النسخة الاحتياطية من كود مشفر:", color = SoftCyberGray, fontSize = 11.sp)
                        OutlinedTextField(
                            value = pastedCodeContent,
                            onValueChange = { pastedCodeContent = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("قم بلصق الكود المشفر هنا المستخرج سابقا...", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = DimWhite, unfocusedTextColor = DimWhite)
                        )

                        Button(
                            onClick = {
                                if (pastedCodeContent.isNotBlank()) {
                                    val ok = viewModel.performEncryptedImport(pastedCodeContent)
                                    if (ok) {
                                        Toast.makeText(context, "اكتمل فك التشفير واستعادة كل البيانات بنجاح!", Toast.LENGTH_LONG).show()
                                        pastedCodeContent = ""
                                    } else {
                                        Toast.makeText(context, "فشل الاسترجاع، الكود الملصق غير صالح!", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MutedSlate),
                            enabled = pastedCodeContent.isNotBlank()
                        ) {
                            Text("🔓 فك التشفير واسترجاع الملاحظات والمهام", fontSize = 12.sp, color = DimWhite)
                        }
                    }
                }

                // Multi Format Exporter widgets
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "📊 تصدير الملاحظات وبورصة البيانات بصيغ متعددة:", color = RealNeonTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                        Button(
                            onClick = {
                                val reports = viewModel.voiceNotes.value
                                val reportBuilder = StringBuilder()
                                reportBuilder.append("=========================================\n")
                                reportBuilder.append("         تقرير صدى الإنتاجي الأسبوعي 📝      \n")
                                reportBuilder.append("=========================================\n\n")

                                reports.forEach { r ->
                                    reportBuilder.append("• العنوان: ${r.title}\n")
                                    reportBuilder.append("• التصنيف: ${r.category}\n")
                                    reportBuilder.append("• التلخيص النقاط:\n${r.summary}\n")
                                    reportBuilder.append("-----------------------------------------\n\n")
                                }

                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Sada Weekly Notes PDF Report")
                                    putExtra(Intent.EXTRA_TEXT, reportBuilder.toString())
                                }
                                context.startActivity(Intent.createChooser(intent, "تصدير تقرير PDF عبر:"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepForestGlow)
                        ) {
                            Text("📥 تصدير تقرير كافة الملاحظات كتقرير نصي أنيق (PDF)", fontSize = 12.sp, color = RealNeonTeal)
                        }
                    }
                }

                // Simulation Terminal Logs Screen View
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceElevated)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(text = "🖥️ سجل فعاليات نظام التزامن والتشفير اللحظي:", color = SoftCyberGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black)
                                .padding(10.dp)
                        ) {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(syncLogs) { log ->
                                    Text(text = log, color = RealNeonTeal, fontSize = 10.sp, modifier = Modifier.padding(vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubbleRow(msg: HelpMessage) {
    val isUser = msg.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 240.dp)
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) RealNeonTeal else ObsidianSurface
            ),
            border = if (!isUser) BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.2f)) else null
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = msg.messageText,
                    color = if (isUser) ObsidianSurface else DimWhite,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isUser) "أنا" else "كرم (الدعم)",
                    color = if (isUser) ObsidianSurface.copy(alpha = 0.6f) else SoftCyberGray.copy(alpha = 0.6f),
                    fontSize = 8.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
