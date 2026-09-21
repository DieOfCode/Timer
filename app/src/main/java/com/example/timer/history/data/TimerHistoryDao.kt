package com.example.timer.history.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerHistoryDao {

    @Query(
        "SELECT * FROM timer_history " +
            "ORDER BY finishedAtEpochMillis DESC"
    )
    fun observeAll(): Flow<List<TimerHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: TimerHistoryEntity)

    @Delete
    suspend fun delete(entry: TimerHistoryEntity)

    @Query("DELETE FROM timer_history")
    suspend fun clear()
}
