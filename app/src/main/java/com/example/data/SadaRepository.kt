package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SadaRepository(private val dao: SadaDao) {

    val allVoiceNotes: Flow<List<VoiceNote>> = dao.getAllVoiceNotes()
    val allTasks: Flow<List<TaskItem>> = dao.getAllTasks()
    val helpMessages: Flow<List<HelpMessage>> = dao.getAllHelpMessages()

    suspend fun getVoiceNoteById(id: Long): VoiceNote? = dao.getVoiceNoteById(id)

    fun getTasksForNote(noteId: Long): Flow<List<TaskItem>> = dao.getTasksForNote(noteId)

    fun searchNotes(query: String): Flow<List<VoiceNote>> {
        return dao.searchVoiceNotes("%$query%")
    }

    suspend fun insertVoiceNote(voiceNote: VoiceNote): Long = withContext(Dispatchers.IO) {
        dao.insertVoiceNote(voiceNote)
    }

    suspend fun updateVoiceNote(voiceNote: VoiceNote) = withContext(Dispatchers.IO) {
        dao.updateVoiceNote(voiceNote)
    }

    suspend fun deleteVoiceNote(voiceNote: VoiceNote) = withContext(Dispatchers.IO) {
        dao.deleteTasksForNote(voiceNote.id)
        dao.deleteVoiceNote(voiceNote)
    }

    suspend fun insertTask(task: TaskItem) = withContext(Dispatchers.IO) {
        dao.insertTask(task)
    }

    suspend fun updateTask(task: TaskItem) = withContext(Dispatchers.IO) {
        dao.updateTask(task)
    }

    suspend fun deleteTask(task: TaskItem) = withContext(Dispatchers.IO) {
        dao.deleteTask(task)
    }

    suspend fun insertHelpMessage(message: HelpMessage) = withContext(Dispatchers.IO) {
        dao.insertHelpMessage(message)
    }

    suspend fun clearHelpChat() = withContext(Dispatchers.IO) {
        dao.clearHelpChat()
    }

    /**
     * Sends voice note transcript to Gemini for analysis or falls back to local processing
     */
    suspend fun analyzeVoiceNoteAndSave(rawText: String, categoryOverride: String? = null): VoiceNote = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

        var analyzed: AnalyzedNoteResponse? = null

        if (hasKey) {
            try {
                analyzed = callGeminiForAnalysis(apiKey, rawText)
            } catch (e: Exception) {
                Log.e("SadaRepository", "Gemini analysis failed, falling back to local: ${e.message}")
            }
        }

        // Fallback to local offline analysis in Arabic if Gemini is unavailable or fails
        val result = analyzed ?: localOfflineAnalysis(rawText, categoryOverride)

        val note = VoiceNote(
            title = result.title,
            rawText = rawText,
            summary = result.summary,
            category = categoryOverride ?: result.category,
            tags = result.tags,
            isSynced = hasKey // If we used gemini, mark synced, otherwise syncable later!
        )

        val noteId = dao.insertVoiceNote(note)
        val savedNote = note.copy(id = noteId)

        // Save extracted tasks
        result.tasks.forEach { taskData ->
            val priorityStr = when (taskData.priority.lowercase()) {
                "high", "عالية" -> "High"
                "low", "منخفضة" -> "Low"
                else -> "Medium"
            }
            dao.insertTask(
                TaskItem(
                    noteId = noteId,
                    taskDescription = taskData.text,
                    priority = priorityStr,
                    isCompleted = false
                )
            )
        }

        savedNote
    }

    private suspend fun callGeminiForAnalysis(apiKey: String, rawText: String): AnalyzedNoteResponse {
        val prompt = """
        Analyze the following text input (Arabic voice transcript) and extract structural elements in elegant Arabic:
        1. A concise, catchy title (title).
        2. A summarized bullets list of key points (summary). Use standard formatting with - bullet points.
        3. Simple category classification (category). Select MUST be exactly one of: "عمل", "شخصي", "أفكار", "اجتماع", "عام".
        4. Array of active action-items/tasks requested in text (tasks). Each task should have "text" (in Arabic) and "priority" (MUST be one of: "High", "Medium", "Low").
        5. A comma-separated list of short searchable tags (tags).
        6. An estimated productivity score from 1 to 100 based on the tasks energy (productivityScore).

        CRITICAL: Render the output STRICTLY as raw JSON block with this schema. Do not enclose in markdown blocks:
        {
          "title": "...",
          "summary": "- ...\n- ...",
          "category": "...",
          "tags": "..., ...",
          "productivityScore": 80,
          "tasks": [
            {"text": "...", "priority": "High"}
          ]
        }

        Input text:
        "$rawText"
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiRequestConfig(responseMimeType = "application/json")
        )

        val response = RetrofitClient.service.generateContent(apiKey, request)
        val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Empty response from model")

        // Clean json markup of any kind
        val cleanedJson = jsonText.trim()
            .replace("^```json".toRegex(), "")
            .replace("^```".toRegex(), "")
            .replace("```$".toRegex(), "")
            .trim()

        val adapter = RetrofitClient.moshi.adapter(AnalyzedNoteResponse::class.java)
        return adapter.fromJson(cleanedJson) ?: throw Exception("JSON Parsing returned null")
    }

    private fun localOfflineAnalysis(rawText: String, categoryOverride: String?): AnalyzedNoteResponse {
        val words = rawText.trim().split("\\s+".toRegex())
        val title = if (words.size <= 5) {
            rawText.trim()
        } else {
            words.take(4).joinToString(" ") + "..."
        }

        // Build elegant local Arabic summary offline
        val summaryBuilder = StringBuilder()
        summaryBuilder.append("• ملاحظة مسجلة محلياً بدون خادم سحابي.\n")
        summaryBuilder.append("• تم استخراج النصوص تلقائياً بدقة من كلامك.\n")
        if (words.size > 8) {
            summaryBuilder.append("• تم رصد موضوع رئيسي يتعلق بـ: ${words.getOrNull(words.size / 2)} و ${words.getOrNull(words.size - 2)}.\n")
        }

        val tasks = mutableListOf<AnalyzedTaskResponse>()
        // Scan for Arabic trigger words for task extraction offline
        val triggers = listOf("يجب", "لازم", "اتصل", "شراء", "تذكر", "اتصال", "تجهيز", "إحضار", "موعد", "سأقوم", "غدا")
        val sentenceSplit = rawText.split(" و ", "،", ".")

        for (sentence in sentenceSplit) {
            val cleanSentence = sentence.trim()
            if (cleanSentence.isEmpty()) continue
            
            var foundTrigger = false
            for (trig in triggers) {
                if (cleanSentence.contains(trig, ignoreCase = true)) {
                    foundTrigger = true
                    break
                }
            }

            if (foundTrigger && cleanSentence.length > 5) {
                val prio = if (cleanSentence.contains("عاجل") || cleanSentence.contains("ضروري")) "High" else "Medium"
                tasks.add(AnalyzedTaskResponse(text = cleanSentence, priority = prio))
            }
        }

        // Add a default task if empty to show functionality
        if (tasks.isEmpty()) {
            tasks.add(AnalyzedTaskResponse(text = "مراجعة الملاحظة الصوتية المستخرجة وتدقيقها", priority = "Low"))
        }

        // Guess category offline
        var category = categoryOverride ?: "عام"
        if (categoryOverride == null) {
            val textLower = rawText.lowercase()
            category = when {
                textLower.contains("شغل") || textLower.contains("اجتماع") || textLower.contains("تطوير") || textLower.contains("شفرة") -> "عمل"
                textLower.contains("بيت") || textLower.contains("شراء") || textLower.contains("أهلي") || textLower.contains("طعام") -> "شخصي"
                textLower.contains("فكرة") || textLower.contains("اختراع") || textLower.contains("تصميم") -> "أفكار"
                else -> "عام"
            }
        }

        // Guess tags offline
        val tagsList = mutableListOf<String>()
        tagsList.add(category)
        tagsList.add("محلي")
        if (rawText.contains("عاجل")) tagsList.add("عاجل")

        return AnalyzedNoteResponse(
            title = title,
            summary = summaryBuilder.toString(),
            category = category,
            tags = tagsList.joinToString("، "),
            productivityScore = if (tasks.size > 2) 85 else 60,
            tasks = tasks
        )
    }

    /**
     * Support ticket bot: Gemini response or Offline simulated Help Desk response
     */
    suspend fun getBotResponse(userMsg: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

        if (hasKey) {
            try {
                return@withContext callGeminiForBot(apiKey, userMsg)
            } catch (e: Exception) {
                Log.e("SadaRepository", "Gemini Bot failed: ${e.message}")
            }
        }

        // Elegant Offline support bot responses
        val msgLower = userMsg.lowercase()
        return@withContext when {
            msgLower.contains("مزامنة") || msgLower.contains("تقويم") || msgLower.contains("سحاب") -> {
                "أهلاً بك! تطبيق صدى يدعم المزامنة السحابية المشفرة تلقائياً عند تفعيل مفتاح Gemini API في الإعدادات. لمزامنة المهام مع تقويم الهاتف، يمكنك الضغط على زر 'مزامنة مع التقويم' بجوار أي مهمة في شاشة المهام وسيتم إدراجها بالتقويم فوراً! 📅✨"
            }
            msgLower.contains("تصدير") || msgLower.contains("اصدار") || msgLower.contains("ملف") || msgLower.contains("pdf") || msgLower.contains("excel") -> {
                "مرحباً! يمكنك تصدير ملاحظاتك في صدى بصيغ متعددة مثل Excel (ملف CSV متوافق بالكامل) وتقرير نصي PDF أنيق مباشرة من 'شاشة الإعدادات والنسخ السحابي'. اختر النطاق من هناك وسيتم إنشاء الملف ومشاركته معك فوراً! 📊📥"
            }
            msgLower.contains("تسجيل") || msgLower.contains("صوت") || msgLower.contains("ميكروفون") || msgLower.contains("تحدث") -> {
                "أهلاً! للتسجيل الصوتي بدقة عالية، انتقل إلى شاشة 'التسجيل الصوتي'، واضغط على زر الميكروفون الدائري. تذكر منح التطبيق صلاحيات الميكروفون عند طلبها. التطبيق يدعم اللهجات المحلية والعربية الفصحى بشكل مثالي! 🎙️❤️"
            }
            msgLower.contains("نسخ") || msgLower.contains("نسخة") || msgLower.contains("استرجاع") -> {
                "النسخ الاحتياطي في صدى يضمن خصوصية معلوماتك! يمكنك إنشاء نسخة احتياطية مشفرة محلياً بنقرة واحدة من لوحة الإعدادات، كما يمكنك استيرادها لاحقاً في أي وقت لاسترجاع ملاحظاتك كافة حتى بدون إنترنت. 🛡️🔒"
            }
            else -> {
                "مرحباً بك في دعم صدى الفني المباشر! 🎙️✨ أنا 'كرم' مساعِدك الذكي في الخدمة على مدار الساعة. يمكنني مساعدتك في شرح مزامنة التقويم، تصدير التقارير (PDF/Excel)، إدارة الملاحظات الصوتية بدقة والتشفير التام. كيف يمكنني خدمتك الآن في تطبيق صدى؟"
            }
        }
    }

    private suspend fun callGeminiForBot(apiKey: String, userMsg: String): String {
        val systemInstruction = """
        English Context: You are 'Karam' (كرم), an expert, friendly customer support assistant for Sada (صدى), the premier AI-powered smart voice note & productivity organizer Android app.
        Help the user in warm, elegant, short, informative Arabic. Keep responses sweet, bulleted if needed, and extremely accurate to the application features:
        - Sada listens using device mic or local preset tests, converts speech to arabic text.
        - Analyzes and summarizes text via Gemini, extracting tasks and setting priorities (High, Medium, Low).
        - Exports notes as Excel/CSV sheets and PDF reports.
        - Synced events with Google calendar or device calendar contracts.
        - Supports reliable offline database usage and high local security (encrypted AES backups).
        - If users ask general technical questions, provide clear answers inside the chat.
        """.trimIndent()

        val prompt = "User Message:\n$userMsg"

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = "System Directive:\n$systemInstruction\n\n$prompt")
                    )
                )
            )
        )

        val response = RetrofitClient.service.generateContent(apiKey, request)
        return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: "عذراً، لم أستطع معالجة الرد حالياً. يرجى المحاولة بعد قليل أو مراجعة الأسئلة الشائعة."
    }
}
