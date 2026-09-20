# Шаг 4.3: создаём TimerViewModel

## Зависимости

В `libs.versions.toml`:

```toml
lifecycle = "2.11.0"
```

В `[libraries]`:

```toml
androidx-lifecycle-viewmodel = { module = "androidx.lifecycle:lifecycle-viewmodel", version.ref = "lifecycle" }
androidx-lifecycle-runtime = { module = "androidx.lifecycle:lifecycle-runtime", version.ref = "lifecycle" }
```

В `app/build.gradle.kts`:

```kotlin
implementation(libs.androidx.lifecycle.viewmodel)
implementation(libs.androidx.lifecycle.runtime)
```

## MutableStateFlow и StateFlow

ViewModel изменяет mutable-поток внутри, но наружу отдаёт только read-only поток:

```text
_uiState: MutableStateFlow  — private, можно менять
uiState: StateFlow          — public, можно только наблюдать
```

## Реализация

Создайте `TimerViewModel.kt`:

```kotlin
package com.example.timer.timer

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TimerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    fun start() {
        _uiState.update {
            TimerUiState(
                status = TimerStatus.RUNNING,
                remainingMillis = FIVE_MINUTES_MILLIS
            )
        }
    }

    fun pause() {
        _uiState.update { current ->
            if (current.status == TimerStatus.RUNNING) {
                current.copy(status = TimerStatus.PAUSED)
            } else {
                current
            }
        }
    }

    fun resume() {
        _uiState.update { current ->
            if (current.status == TimerStatus.PAUSED) {
                current.copy(status = TimerStatus.RUNNING)
            } else {
                current
            }
        }
    }

    fun togglePause() {
        when (_uiState.value.status) {
            TimerStatus.RUNNING -> pause()
            TimerStatus.PAUSED -> resume()
            else -> Unit
        }
    }

    fun reset() {
        _uiState.value = TimerUiState()
    }

    private companion object {
        const val FIVE_MINUTES_MILLIS = 5 * 60 * 1_000L
    }
}
```

Пока `start()` устанавливает тестовые пять минут, но не уменьшает время. Это
осознанно: сначала проверяем переходы и сохранение state при повороте.

## Почему проверки внутри ViewModel

`pause()` допустим только из `RUNNING`, `resume()` — только из `PAUSED`.
`togglePause()` выбирает допустимый переход внутри ViewModel. Fragment не должен
дублировать эти бизнес-правила.

## Практика

Создайте ViewModel и убедитесь, что проект собирается. UI пока к нему не
подключайте.

## Контрольные вопросы

1. Почему `_uiState` private?
2. Зачем наружу отдаётся `asStateFlow()`?
3. Почему переходы проверяются в ViewModel?

Далее: [шаг 4.4](04-fragment-rendering.md).
