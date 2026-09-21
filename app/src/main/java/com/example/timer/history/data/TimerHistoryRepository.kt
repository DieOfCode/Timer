package com.example.timer.history.data

import com.example.timer.model.TimerSound
import kotlinx.coroutines.flow.Flow

class TimerHistoryRepository(
    private val dao: TimerHistoryDao
) {

    val history: Flow<List<TimerHistoryEntity>> = dao.observeAll()

    suspend fun recordTimerFinished(
        durationMillis: Long,
        finishedAtEpochMillis: Long,
        sound: TimerSound
    ) {
        dao.insert(
            TimerHistoryEntity(
                durationMillis = durationMillis,
                finishedAtEpochMillis = finishedAtEpochMillis,
                soundName = sound.name
            )
        )
    }

    suspend fun delete(entry: TimerHistoryEntity) {
        dao.delete(entry)
    }

    suspend fun clear() {
        dao.clear()
    }
}
