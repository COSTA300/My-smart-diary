package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.crypto.spec.SecretKeySpec

@Entity(tableName = "diary_entries")
data class DiaryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateStr: String, // e.g., "Saturday, May 23, 2026"
    val timestamp: Long,
    val mood: String, // e.g., "Peaceful", "Heavy", "Anxious", "Lonely", "Hopeful", "Secretive"
    val moodColor: Int, // Hex element index/color
    val companionName: String, // Maya, Kiran, Eden
    val encryptedTitle: String,
    val encryptedMainText: String,
    val isDecoy: Boolean
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: Long,
    val sender: String, // "user" or "companion"
    val encryptedText: String,
    val timestamp: Long
)

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries WHERE isDecoy = :isDecoy ORDER BY timestamp DESC")
    fun getEntries(isDecoy: Boolean): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries WHERE id = :entryId")
    suspend fun getEntryById(entryId: Long): DiaryEntryEntity?

    @Query("SELECT * FROM diary_entries WHERE dateStr = :dateStr AND isDecoy = :isDecoy LIMIT 1")
    suspend fun getEntryByDate(dateStr: String, isDecoy: Boolean): DiaryEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DiaryEntryEntity): Long

    @Query("SELECT * FROM chat_messages WHERE entryId = :entryId ORDER BY timestamp ASC")
    fun getMessagesForEntry(entryId: Long): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM diary_entries WHERE id = :entryId")
    suspend fun deleteEntry(entryId: Long)

    @Query("DELETE FROM chat_messages WHERE entryId = :entryId")
    suspend fun deleteMessagesForEntry(entryId: Long)
}

@Database(entities = [DiaryEntryEntity::class, ChatMessageEntity::class], version = 1, exportSchema = false)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
}

// Presentation/Domain Models with decrypted content
data class DecryptedDiaryEntry(
    val id: Long = 0,
    val dateStr: String,
    val timestamp: Long,
    val mood: String,
    val moodColor: Int,
    val companionName: String,
    val title: String,
    val mainText: String,
    val isDecoy: Boolean
)

data class DecryptedChatMessage(
    val id: Long = 0,
    val entryId: Long,
    val sender: String, // "user" or "companion"
    val text: String,
    val timestamp: Long
)

class DiaryRepository(private val dao: DiaryDao) {
    // Unlocked key stored ONLY in memory during the active session (Zero-Knowledge)
    private var activeKey: SecretKeySpec? = null

    fun setKey(key: SecretKeySpec) {
        activeKey = key
    }

    fun clearKey() {
        activeKey = null
    }

    fun isUnlocked(): Boolean = activeKey != null

    private fun encryptText(plaintext: String): String {
        val key = activeKey ?: return plaintext
        if (plaintext.isBlank()) return ""
        return com.example.security.CryptoManager.encrypt(plaintext, key)
    }

    private fun decryptText(ciphertext: String): String {
        val key = activeKey ?: return ciphertext
        if (ciphertext.isBlank()) return ""
        return com.example.security.CryptoManager.decrypt(ciphertext, key)
    }

    fun getDecryptedEntries(isDecoy: Boolean): Flow<List<DecryptedDiaryEntry>> {
        return dao.getEntries(isDecoy).map { entities ->
            entities.map { entity ->
                DecryptedDiaryEntry(
                    id = entity.id,
                    dateStr = entity.dateStr,
                    timestamp = entity.timestamp,
                    mood = entity.mood,
                    moodColor = entity.moodColor,
                    companionName = entity.companionName,
                    title = decryptText(entity.encryptedTitle),
                    mainText = decryptText(entity.encryptedMainText),
                    isDecoy = entity.isDecoy
                )
            }
        }
    }

    suspend fun insertDecryptedEntry(entry: DecryptedDiaryEntry): Long {
        val entity = DiaryEntryEntity(
            id = entry.id,
            dateStr = entry.dateStr,
            timestamp = entry.timestamp,
            mood = entry.mood,
            moodColor = entry.moodColor,
            companionName = entry.companionName,
            encryptedTitle = encryptText(entry.title),
            encryptedMainText = encryptText(entry.mainText),
            isDecoy = entry.isDecoy
        )
        return dao.insertEntry(entity)
    }

    fun getDecryptedMessagesForEntry(entryId: Long): Flow<List<DecryptedChatMessage>> {
        return dao.getMessagesForEntry(entryId).map { entities ->
            entities.map { entity ->
                DecryptedChatMessage(
                    id = entity.id,
                    entryId = entity.entryId,
                    sender = entity.sender,
                    text = decryptText(entity.encryptedText),
                    timestamp = entity.timestamp
                )
            }
        }
    }

    suspend fun insertDecryptedMessage(msg: DecryptedChatMessage): Long {
        val entity = ChatMessageEntity(
            id = msg.id,
            entryId = msg.entryId,
            sender = msg.sender,
            encryptedText = encryptText(msg.text),
            timestamp = msg.timestamp
        )
        return dao.insertMessage(entity)
    }

    suspend fun deleteEntry(entryId: Long) {
        dao.deleteEntry(entryId)
        dao.deleteMessagesForEntry(entryId)
    }
}
