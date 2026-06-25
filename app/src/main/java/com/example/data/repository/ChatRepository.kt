package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GeminiRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.dao.ChatDao
import com.example.data.model.ChatMessage
import com.example.data.model.ChatThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ChatRepository(private val chatDao: ChatDao) {
    val allThreads: Flow<List<ChatThread>> = chatDao.getAllThreads()

    fun getMessagesForThread(threadId: Long): Flow<List<ChatMessage>> =
        chatDao.getMessagesForThread(threadId)

    suspend fun createNewThread(title: String = "New Chat"): Long {
        return chatDao.insertThread(ChatThread(title = title))
    }

    suspend fun insertMessage(message: ChatMessage): Long {
        return chatDao.insertMessage(message)
    }

    suspend fun updateThreadTitle(threadId: Long, title: String) {
        chatDao.updateThreadTitle(threadId, title)
    }

    suspend fun deleteThread(threadId: Long) {
        chatDao.deleteThread(threadId)
    }

    suspend fun clearAllThreads() {
        chatDao.clearAllThreads()
    }

    /**
     * Sends a message to JARVIS, including conversation history from the DB.
     */
    suspend fun sendMessageToJarvis(
        threadId: Long,
        userPrompt: String,
        temperature: Float = 0.7f,
        customSystemPrompt: String? = null
    ): String {
        // 1. Insert user message in local DB
        val userMessage = ChatMessage(
            threadId = threadId,
            role = "user",
            content = userPrompt
        )
        chatDao.insertMessage(userMessage)

        // 2. Fetch thread messages to check if we should auto-rename
        val existingMessages = chatDao.getMessagesForThread(threadId).firstOrNull() ?: emptyList()
        
        // Auto-rename thread title if it has only 1 message
        val userMsgCount = existingMessages.count { it.role == "user" }
        if (userMsgCount <= 1) {
            val shortTitle = if (userPrompt.length > 25) {
                userPrompt.take(25) + "..."
            } else {
                userPrompt
            }
            chatDao.updateThreadTitle(threadId, shortTitle)
        }

        // 3. Prepare the system instruction
        val jarvisPersonality = """
            You are JARVIS, an advanced AI assistant with expert knowledge across all subjects. 
            Your role is to provide accurate, detailed, and easy-to-understand answers to any question. 
            You solve problems, give step-by-step guidance, help with studies, coding, business, technology, and daily life. 
            You can generate creative ideas, write content, create image prompts, design concepts, and generate video concepts and scripts. 
            Always be intelligent, helpful, professional, and friendly. 
            Analyze every request carefully, ask clarifying questions when needed, and provide the best possible solution. 
            Your goal is to assist users efficiently and make complex tasks simple.
        """.trimIndent()

        val finalSystemPrompt = if (customSystemPrompt.isNullOrBlank()) {
            jarvisPersonality
        } else {
            "$jarvisPersonality\n\nAdditionally, adhere to this personality adjustment: $customSystemPrompt"
        }

        val systemInstructionContent = Content(
            parts = listOf(Part(text = finalSystemPrompt))
        )

        // 4. Map DB history messages to Gemini request content format
        val geminiHistory = existingMessages.map { msg ->
            Content(
                role = if (msg.role == "user") "user" else "model",
                parts = listOf(Part(text = msg.content))
            )
        }.toMutableList()

        // Append the newest user message if it's not already in the retrieved list
        if (geminiHistory.none { it.parts.firstOrNull()?.text == userPrompt }) {
            geminiHistory.add(
                Content(
                    role = "user",
                    parts = listOf(Part(text = userPrompt))
                )
            )
        }

        // 5. Invoke Gemini API
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            val errText = "API Key not configured. Please enter your Gemini API Key in the AI Studio Secrets panel."
            chatDao.insertMessage(ChatMessage(threadId = threadId, role = "model", content = errText))
            return errText
        }

        val request = GeminiRequest(
            contents = geminiHistory,
            generationConfig = GenerationConfig(temperature = temperature),
            systemInstruction = systemInstructionContent
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "JARVIS returned empty response."
            
            // Insert JARVIS response in local DB
            chatDao.insertMessage(
                ChatMessage(
                    threadId = threadId,
                    role = "model",
                    content = replyText
                )
            )
            replyText
        } catch (e: Exception) {
            val errReply = "Connection error: ${e.localizedMessage ?: "Could not reach JARVIS. Please check your internet connection and verify your API Key."}"
            chatDao.insertMessage(
                ChatMessage(
                    threadId = threadId,
                    role = "model",
                    content = errReply
                )
            )
            errReply
        }
    }
}
