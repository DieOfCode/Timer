package com.example.timer.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.timer.history.data.TimerHistoryEntity
import com.example.timer.history.data.TimerHistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: TimerHistoryRepository
) : ViewModel() {

    val history: StateFlow<List<TimerHistoryEntity>> = repository.history
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    fun delete(entry: TimerHistoryEntity) {
        viewModelScope.launch {
            repository.delete(entry)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clear()
        }
    }

    class Factory(
        private val repository: TimerHistoryRepository
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras
        ): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel(repository) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
}
