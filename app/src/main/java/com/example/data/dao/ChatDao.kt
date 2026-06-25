package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ChatMessage
import com.example.data.model.ChatThread
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_threads ORDER BY timestamp DESC")
    fun getAllThreads(): Flow<List<ChatThread>>

    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun getMessagesForThread(threadId: Long): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThread(thread: ChatThread): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("UPDATE chat_threads SET title = :title WHERE id = :threadId")
    suspend fun updateThreadTitle(threadId: Long, title: String)

    @Query("DELETE FROM chat_threads WHERE id = :threadId")
    suspend fun deleteThread(threadId: Long)

    @Query("DELETE FROM chat_threads")
    suspend fun clearAllThreads()
}
