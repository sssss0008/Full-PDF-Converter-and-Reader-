package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val filePath: String,
    val pageCount: Int,
    val fileSize: Long,
    val lastOpenedTimestamp: Long,
    val lastReadPage: Int = 0,
    val lastZoomFactor: Float = 1.0f,
    val isStarred: Boolean = false,
    val isVaultProtected: Boolean = false,
    val isPasswordProtected: Boolean = false,
    val thumbnailPath: String? = null
)

@Entity(tableName = "annotations")
data class AnnotationEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val pageIndex: Int,
    val type: String, // DRAWING, HIGHLIGHT, STAMP, NOTE, SHAPE
    val colorHex: Long,
    val strokeWidth: Float,
    val pathDataJson: String,
    val noteText: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_signatures")
data class SignatureEntity(
    @PrimaryKey val id: String,
    val label: String,
    val imagePath: String,
    val isInitial: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY lastOpenedTimestamp DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isVaultProtected = 0 ORDER BY lastOpenedTimestamp DESC")
    fun getPublicDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isStarred = 1 AND isVaultProtected = 0 ORDER BY lastOpenedTimestamp DESC")
    fun getStarredDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isVaultProtected = 1 ORDER BY lastOpenedTimestamp DESC")
    fun getVaultDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: String): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(document: DocumentEntity)

    @Update
    suspend fun update(document: DocumentEntity)

    @Delete
    suspend fun delete(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE documents SET lastReadPage = :page, lastZoomFactor = :zoom, lastOpenedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateReadingProgress(id: String, page: Int, zoom: Float, timestamp: Long)

    @Query("UPDATE documents SET isStarred = :isStarred WHERE id = :id")
    suspend fun setStarred(id: String, isStarred: Boolean)

    @Query("UPDATE documents SET isVaultProtected = :isVault WHERE id = :id")
    suspend fun setVaultProtected(id: String, isVault: Boolean)

    @Query("UPDATE documents SET title = :title WHERE id = :id")
    suspend fun updateTitle(id: String, title: String)
}

@Dao
interface AnnotationDao {
    @Query("SELECT * FROM annotations WHERE documentId = :documentId AND pageIndex = :pageIndex")
    fun getAnnotationsForPage(documentId: String, pageIndex: Int): Flow<List<AnnotationEntity>>

    @Query("SELECT * FROM annotations WHERE documentId = :documentId")
    fun getAllAnnotationsForDoc(documentId: String): Flow<List<AnnotationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(annotation: AnnotationEntity)

    @Delete
    suspend fun delete(annotation: AnnotationEntity)

    @Query("DELETE FROM annotations WHERE documentId = :docId")
    suspend fun deleteAllForDoc(docId: String)
}

@Dao
interface SignatureDao {
    @Query("SELECT * FROM saved_signatures ORDER BY createdAt DESC")
    fun getAllSignatures(): Flow<List<SignatureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(signature: SignatureEntity)

    @Delete
    suspend fun delete(signature: SignatureEntity)
}

@Database(
    entities = [DocumentEntity::class, AnnotationEntity::class, SignatureEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun annotationDao(): AnnotationDao
    abstract fun signatureDao(): SignatureDao
}
