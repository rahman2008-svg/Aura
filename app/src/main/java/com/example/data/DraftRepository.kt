package com.example.data

import kotlinx.coroutines.flow.Flow

class DraftRepository(private val draftDao: DraftDao) {
    val allDrafts: Flow<List<Draft>> = draftDao.getAllDrafts()

    suspend fun getDraftById(id: Int): Draft? {
        return draftDao.getDraftById(id)
    }

    suspend fun insertDraft(draft: Draft): Long {
        return draftDao.insertDraft(draft)
    }

    suspend fun deleteDraft(draft: Draft) {
        draftDao.deleteDraft(draft)
    }

    suspend fun deleteDraftById(id: Int) {
        draftDao.deleteDraftById(id)
    }
}
