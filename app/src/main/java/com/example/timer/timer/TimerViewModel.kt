package com.example.timer.timer

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timer.model.TimerSound
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class TimerViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(restoreState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var finishTimeMillis: Long? = null

    init {
        val restoredState = _uiState.value

        when (restoredState.status) {
            TimerStatus.RUNNING -> {
                startCountdown(restoredState.remainingMillis)
            }

            TimerStatus.FINISHED -> {
                saveFinishedState()
            }

            TimerStatus.READY,
            TimerStatus.PAUSED -> Unit
        }
    }

    private fun restoreState(): TimerUiState {
        val durationMillis = restoreDurationMillis()
        val sound = restoreSound()

        val status = savedStateHandle
            .get<String>(KEY_STATUS)
            ?.let { savedStatus ->
                runCatching {
                    TimerStatus.valueOf(savedStatus)
                }.getOrNull()
            }
            ?: TimerStatus.READY

        return when (status) {
            TimerStatus.READY -> TimerUiState(
                status = TimerStatus.READY,
                durationMillis = durationMillis,
                remainingMillis = durationMillis,
                sound = sound
            )

            TimerStatus.RUNNING -> restoreRunningState(
                durationMillis = durationMillis,
                sound = sound
            )

            TimerStatus.PAUSED -> restorePausedState(
                durationMillis = durationMillis,
                sound = sound
            )

            TimerStatus.FINISHED -> TimerUiState(
                status = TimerStatus.FINISHED,
                durationMillis = durationMillis,
                remainingMillis = 0L,
                sound = sound,
                finishTimeElapsedRealtime = null
            )
        }
    }

    private fun restoreDurationMillis(): Long {
        return savedStateHandle
            .get<Long>(KEY_DURATION_MILLIS)
            ?.coerceIn(
                MIN_DURATION_MILLIS,
                MAX_DURATION_MILLIS
            )
            ?: DEFAULT_TIMER_DURATION_MILLIS
    }

    private fun restoreSound(): TimerSound {
        val savedSound = savedStateHandle.get<String>(KEY_SOUND)

        return savedSound
            ?.let { soundName ->
                runCatching {
                    TimerSound.valueOf(soundName)
                }.getOrNull()
            }
            ?: TimerSound.BELL
    }

    private fun restoreRunningState(
        durationMillis: Long,
        sound: TimerSound
    ): TimerUiState {
        val savedFinishTime =
            savedStateHandle.get<Long>(KEY_FINISH_TIME_MILLIS)

        val remainingMillis = savedFinishTime
            ?.let { finishTime ->
                finishTime - SystemClock.elapsedRealtime()
            }
            ?.coerceAtLeast(0L)
            ?: 0L

        return if (remainingMillis > 0L) {
            TimerUiState(
                status = TimerStatus.RUNNING,
                durationMillis = durationMillis,
                remainingMillis = remainingMillis,
                sound = sound,
                finishTimeElapsedRealtime = savedFinishTime
            )
        } else {
            TimerUiState(
                status = TimerStatus.FINISHED,
                durationMillis = durationMillis,
                remainingMillis = 0L,
                sound = sound,
                finishTimeElapsedRealtime = null
            )
        }
    }

    private fun restorePausedState(
        durationMillis: Long,
        sound: TimerSound
    ): TimerUiState {
        val remainingMillis = savedStateHandle
            .get<Long>(KEY_REMAINING_MILLIS)
            ?.coerceAtLeast(0L)
            ?: 0L

        return if (remainingMillis > 0L) {
            TimerUiState(
                status = TimerStatus.PAUSED,
                durationMillis = durationMillis,
                remainingMillis = remainingMillis,
                sound = sound,
                finishTimeElapsedRealtime = null
            )
        } else {
            TimerUiState(
                status = TimerStatus.FINISHED,
                durationMillis = durationMillis,
                remainingMillis = 0L,
                sound = sound,
                finishTimeElapsedRealtime = null
            )
        }
    }

    fun start() {
        val current = _uiState.value

        if (current.status != TimerStatus.READY) {
            return
        }

        startCountdown(current.durationMillis)
    }

    fun configureTimer(
        durationMillis: Long,
        sound: TimerSound
    ) {
        val safeDuration = durationMillis.coerceIn(
            MIN_DURATION_MILLIS,
            MAX_DURATION_MILLIS
        )

        stopCountdown()

        _uiState.value = TimerUiState(
            status = TimerStatus.READY,
            durationMillis = safeDuration,
            remainingMillis = safeDuration,
            sound = sound,
            finishTimeElapsedRealtime = null
        )

        saveReadyState()
    }

    fun pause() {
        val current = _uiState.value

        if (current.status != TimerStatus.RUNNING) {
            return
        }

        val remainingMillis = finishTimeMillis
            ?.let { finishTime ->
                finishTime - SystemClock.elapsedRealtime()
            }
            ?.coerceAtLeast(0L)
            ?: current.remainingMillis

        stopCountdown()

        _uiState.value = current.copy(
            status = if (remainingMillis > 0L) {
                TimerStatus.PAUSED
            } else {
                TimerStatus.FINISHED
            },
            remainingMillis = remainingMillis,
            finishTimeElapsedRealtime = null
        )

        if (remainingMillis > 0L) {
            savePausedState(remainingMillis)
        } else {
            saveFinishedState()
        }
    }

    fun resume() {
        val current = _uiState.value

        if (
            current.status != TimerStatus.PAUSED ||
            current.remainingMillis <= 0L
        ) {
            return
        }

        startCountdown(current.remainingMillis)
    }

    fun togglePause() {
        when (_uiState.value.status) {
            TimerStatus.RUNNING -> pause()
            TimerStatus.PAUSED -> resume()
            else -> Unit
        }
    }

    fun reset() {
        stopCountdown()

        _uiState.update { current ->
            current.copy(
                status = TimerStatus.READY,
                remainingMillis = current.durationMillis,
                finishTimeElapsedRealtime = null
            )
        }

        saveReadyState()
    }

    private fun startCountdown(durationMillis: Long) {
        stopCountdown()

        val safeDuration = durationMillis.coerceAtLeast(0L)

        if (safeDuration == 0L) {
            _uiState.update { current ->
                current.copy(
                    status = TimerStatus.FINISHED,
                    remainingMillis = 0L,
                    finishTimeElapsedRealtime = null
                )
            }
            saveFinishedState()
            return
        }

        val finishTime =
            SystemClock.elapsedRealtime() + safeDuration

        finishTimeMillis = finishTime
        saveRunningState(finishTime)

        _uiState.update { current ->
            current.copy(
                status = TimerStatus.RUNNING,
                remainingMillis = safeDuration,
                finishTimeElapsedRealtime = finishTime
            )
        }

        timerJob = viewModelScope.launch {
            while (isActive) {
                val remainingMillis = (
                        finishTime - SystemClock.elapsedRealtime()
                        ).coerceAtLeast(0L)

                if (remainingMillis == 0L) {
                    finishTimeMillis = null
                    timerJob = null

                    _uiState.update { current ->
                        current.copy(
                            status = TimerStatus.FINISHED,
                            remainingMillis = 0L,
                            finishTimeElapsedRealtime = null
                        )
                    }

                    saveFinishedState()
                    break
                }

                _uiState.update { current ->
                    current.copy(
                        status = TimerStatus.RUNNING,
                        remainingMillis = remainingMillis,
                        finishTimeElapsedRealtime = finishTime
                    )
                }

                delay(TICK_INTERVAL_MILLIS)
            }
        }
    }

    private fun stopCountdown() {
        timerJob?.cancel()
        timerJob = null
        finishTimeMillis = null
    }

    private fun saveRunningState(finishTimeMillis: Long) {
        savedStateHandle[KEY_STATUS] = TimerStatus.RUNNING.name
        savedStateHandle[KEY_FINISH_TIME_MILLIS] = finishTimeMillis

        savedStateHandle.remove<Long>(KEY_REMAINING_MILLIS)

        saveConfiguration()
    }

    private fun savePausedState(remainingMillis: Long) {
        savedStateHandle[KEY_STATUS] = TimerStatus.PAUSED.name
        savedStateHandle[KEY_REMAINING_MILLIS] = remainingMillis

        savedStateHandle.remove<Long>(KEY_FINISH_TIME_MILLIS)

        saveConfiguration()
    }

    private fun saveFinishedState() {
        savedStateHandle[KEY_STATUS] = TimerStatus.FINISHED.name
        savedStateHandle[KEY_REMAINING_MILLIS] = 0L

        savedStateHandle.remove<Long>(KEY_FINISH_TIME_MILLIS)

        saveConfiguration()
    }

    private fun saveReadyState() {
        savedStateHandle[KEY_STATUS] = TimerStatus.READY.name

        savedStateHandle.remove<Long>(KEY_REMAINING_MILLIS)
        savedStateHandle.remove<Long>(KEY_FINISH_TIME_MILLIS)

        saveConfiguration()
    }

    private fun saveConfiguration() {
        val current = _uiState.value

        savedStateHandle[KEY_DURATION_MILLIS] =
            current.durationMillis

        savedStateHandle[KEY_SOUND] = current.sound.name
    }

    private companion object {
        const val MIN_DURATION_MILLIS = 60_000L
        const val MAX_DURATION_MILLIS = 1_440 * 60_000L
        const val TICK_INTERVAL_MILLIS = 200L

        const val KEY_STATUS = "timer_status"
        const val KEY_REMAINING_MILLIS = "remaining_millis"
        const val KEY_FINISH_TIME_MILLIS = "finish_time_millis"
        const val KEY_DURATION_MILLIS = "duration_millis"
        const val KEY_SOUND = "timer_sound"
    }
}
