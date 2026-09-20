# Шаг 4.4: подключаем TimerFragment

## Разметка управления состоянием

На этом шаге учебный `fragmentTitle` больше не нужен. Замените весь
`fragment_timer.xml`, сохранив кнопки навигации:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/timerFragmentRoot"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".timer.TimerFragment">

<TextView
    android:id="@+id/timerText"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/initial_time"
    android:textSize="48sp"
    app:layout_constraintBottom_toTopOf="@id/startButton"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent"
    app:layout_constraintVertical_chainStyle="packed" />

<Button
    android:id="@+id/startButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginTop="24dp"
    android:text="@string/start"
    app:layout_constraintBottom_toTopOf="@id/pauseButton"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toBottomOf="@id/timerText" />

<Button
    android:id="@+id/pauseButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginTop="12dp"
    android:text="@string/pause"
    app:layout_constraintBottom_toTopOf="@id/resetButton"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toBottomOf="@id/startButton" />

<Button
    android:id="@+id/resetButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginTop="12dp"
    android:text="@string/reset"
    app:layout_constraintBottom_toTopOf="@id/openSettingsButton"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toBottomOf="@id/pauseButton" />

<Button
    android:id="@+id/openSettingsButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginTop="24dp"
    android:text="@string/open_settings"
    app:layout_constraintBottom_toTopOf="@id/openHistoryButton"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toBottomOf="@id/resetButton" />

<Button
    android:id="@+id/openHistoryButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginTop="12dp"
    android:text="@string/open_history"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toBottomOf="@id/openSettingsButton" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

В `strings.xml` добавьте:

```xml
<string name="pause">Пауза</string>
<string name="resume">Продолжить</string>
<string name="reset">Сброс</string>
```

## Получаем ViewModel

В `TimerFragment`:

```kotlin
private val viewModel: TimerViewModel by viewModels()
```

Импорт:

```kotlin
import androidx.fragment.app.viewModels
```

## Передаём события

В `onViewCreated()`:

```kotlin
binding.startButton.setOnClickListener {
    viewModel.start()
}

binding.pauseButton.setOnClickListener {
    viewModel.togglePause()
}

binding.resetButton.setOnClickListener {
    viewModel.reset()
}
```

## Собираем StateFlow по lifecycle View

```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { state ->
            render(state)
        }
    }
}
```

Импорты:

```kotlin
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
```

Сбор начинается при `STARTED`, останавливается при уходе экрана и отменяется при
`onDestroyView()`. Новый View создаст новую lifecycle-aware подписку.

## Render

```kotlin
private fun render(state: TimerUiState) {
    binding.timerText.text = formatTime(state.remainingMillis)

    binding.startButton.isEnabled = state.status == TimerStatus.READY
    binding.resetButton.isEnabled = state.status != TimerStatus.READY

    binding.pauseButton.isEnabled =
        state.status == TimerStatus.RUNNING ||
            state.status == TimerStatus.PAUSED

    binding.pauseButton.setText(
        if (state.status == TimerStatus.PAUSED) {
            R.string.resume
        } else {
            R.string.pause
        }
    )
}
```

Функция форматирования:

```kotlin
private fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1_000L
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}
```

## Проверка

1. Старт показывает `00:05:00` и статус `RUNNING` через состояние.
2. Pause меняет доступность/текст кнопок, время пока не уменьшается.
3. Resume возвращает `RUNNING`.
4. Reset возвращает `00:00:00`.
5. После поворота текущее состояние сохраняется благодаря ViewModel.
6. После перехода и возврата UI заново отрисовывается из `uiState`.

## Контрольные вопросы

1. Почему Fragment не меняет `timerText` внутри start listener?
2. Почему `render()` получает весь `TimerUiState`?
3. Почему сбор привязан к `viewLifecycleOwner`?
4. Что переживает поворот: View, Fragment или ViewModel?

После этого можно переходить к [части 5](../05-graph-viewmodel.md) после MVP или к
[части 6](../06-restoration-and-checkpoint.md), а затем реализовывать настоящий
обратный отсчёт.
