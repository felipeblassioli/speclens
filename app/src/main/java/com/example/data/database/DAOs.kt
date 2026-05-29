package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewSessionDao {
    @Query("SELECT * FROM review_sessions ORDER BY id DESC")
    fun getAllSessions(): Flow<List<ReviewSession>>

    @Query("SELECT * FROM review_sessions WHERE id = :id")
    fun getSessionById(id: Int): Flow<ReviewSession?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ReviewSession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<ReviewSession>)

    @Update
    suspend fun updateSession(session: ReviewSession)

    @Delete
    suspend fun deleteSession(session: ReviewSession)
}

@Dao
interface PrivateNoteDao {
    @Query("SELECT * FROM private_notes WHERE sessionId = :sessionId AND isDiscarded = 0")
    fun getNotesForSession(sessionId: Int): Flow<List<PrivateNote>>

    @Query("SELECT * FROM private_notes WHERE id = :id")
    suspend fun getNoteById(id: Int): PrivateNote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: PrivateNote): Long

    @Update
    suspend fun updateNote(note: PrivateNote)

    @Delete
    suspend fun deleteNote(note: PrivateNote)
}

@Dao
interface FindingDao {
    @Query("SELECT * FROM findings WHERE sessionId = :sessionId AND isDiscarded = 0")
    fun getFindingsForSession(sessionId: Int): Flow<List<Finding>>

    @Query("SELECT * FROM findings WHERE id = :id")
    suspend fun getFindingById(id: Int): Finding?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinding(finding: Finding): Long

    @Update
    suspend fun updateFinding(finding: Finding)

    @Delete
    suspend fun deleteFinding(finding: Finding)
}
