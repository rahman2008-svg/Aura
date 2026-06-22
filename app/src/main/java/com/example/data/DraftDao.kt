package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DraftDao {
    @Query("SELECT * FROM drafts ORDER BY timestamp DESC")
    fun getAllDrafts(): Flow<List<Draft>>

    @Query("SELECT * FROM drafts WHERE id = :id LIMIT 1")
    suspend fun getDraftById(id: Int): Draft?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: Draft): Long

    @Delete
    suspend fun deleteDraft(draft: Draft)

    @Query("DELETE FROM drafts WHERE id = :id")
    suspend fun deleteDraftById(id: Int)
}
