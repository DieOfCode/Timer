# Шаг 5.3: разделяем ViewModel внутри graph

Теперь длительность, выбранная на первом экране, и звук со второго экрана должны
попасть на экран подтверждения.

## 1. Опишите состояние черновика

В пакете `com.example.timer.create` создайте `TimerSound.kt`:

```kotlin
package com.example.timer.create

enum class TimerSound {
    BELL,
    SILENT
}
```

Создайте `CreateTimerUiState.kt`:

```kotlin
package com.example.timer.create

data class CreateTimerUiState(
    val durationMinutes: Int = 5,
    val sound: TimerSound = TimerSound.BELL
)
```

Это один immutable-снимок всего черновика:

```text
CreateTimerUiState
├── durationMinutes
└── sound
```

## 2. Создайте ViewModel

Создайте `CreateTimerFlowViewModel.kt`:

```kotlin
package com.example.timer.create

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CreateTimerFlowViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTimerUiState())
    val uiState: StateFlow<CreateTimerUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "created: ${System.identityHashCode(this)}")
    }

    fun setDurationMinutes(minutes: Int) {
        _uiState.update { current ->
            current.copy(durationMinutes = minutes.coerceIn(1, 1_440))
        }
    }

    fun selectSound(sound: TimerSound) {
        _uiState.update { current ->
            current.copy(sound = sound)
        }
    }

    override fun onCleared() {
        Log.d(TAG, "cleared: ${System.identityHashCode(this)}")
        super.onCleared()
    }

    private companion object {
        const val TAG = "CreateTimerFlowVM"
    }
}
```

Ограничение `1..1_440` не даёт сохранить ноль, отрицательное значение или более
суток. `EditText` отвечает за ввод, ViewModel — за допустимое состояние.

## 3. Получите ViewModel на первом экране

В `TimerSetupFragment` добавьте свойство:

```kotlin
private val viewModel: CreateTimerFlowViewModel
    by navGraphViewModels(R.id.createTimerFlow)
```

Импорт:

```kotlin
import androidx.navigation.navGraphViewModels
```

`R.id.createTimerFlow` — это ID owner. Если вместо него вызвать обычный
`by viewModels()`, каждый экран получит отдельную ViewModel.

В `onViewCreated()` сначала восстановите текущее значение в поле, затем замените
listener кнопки:

```kotlin
binding.durationInput.setText(
    viewModel.uiState.value.durationMinutes.toString()
)

binding.nextButton.setOnClickListener {
    val minutes = binding.durationInput.text
        .toString()
        .toIntOrNull()

    if (minutes == null || minutes !in 1..1_440) {
        binding.durationInput.error = "Введите число от 1 до 1440"
        return@setOnClickListener
    }

    viewModel.setDurationMinutes(minutes)
    findNavController().navigate(
        R.id.action_timerSetupFragment_to_soundSelectionFragment
    )
}
```

Поле заполняется из `uiState`, поэтому после перехода назад пользователь увидит
свой сохранённый выбор.

Позже строку ошибки следует вынести в `strings.xml`; здесь оставляем её рядом с
валидацией, чтобы не отвлекаться от graph scope.

## 4. Получите тот же экземпляр на втором экране

В `SoundSelectionFragment` добавьте то же объявление:

```kotlin
private val viewModel: CreateTimerFlowViewModel
    by navGraphViewModels(R.id.createTimerFlow)
```

В `onViewCreated()` отобразите сохранённый выбор:

```kotlin
when (viewModel.uiState.value.sound) {
    TimerSound.BELL -> binding.soundBellButton.isChecked = true
    TimerSound.SILENT -> binding.soundSilentButton.isChecked = true
}
```

Затем замените listener:

```kotlin
binding.nextButton.setOnClickListener {
    val selectedSound = when (binding.soundGroup.checkedRadioButtonId) {
        R.id.soundSilentButton -> TimerSound.SILENT
        else -> TimerSound.BELL
    }

    viewModel.selectSound(selectedSound)
    findNavController().navigate(
        R.id.action_soundSelectionFragment_to_timerConfirmationFragment
    )
}
```

## 5. Покажите общий state на подтверждении

В `TimerConfirmationFragment` добавьте ту же graph-scoped ViewModel:

```kotlin
private val viewModel: CreateTimerFlowViewModel
    by navGraphViewModels(R.id.createTimerFlow)
```

В `onViewCreated()` оставьте listener завершения и добавьте lifecycle-aware
подписку:

```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { state ->
            binding.summaryText.text = buildString {
                append("Длительность: ")
                append(state.durationMinutes)
                append(" мин.\nСигнал: ")
                append(
                    when (state.sound) {
                        TimerSound.BELL -> "Звонок"
                        TimerSound.SILENT -> "Без звука"
                    }
                )
            }
        }
    }
}
```

Импорты:

```kotlin
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.navGraphViewModels
import kotlinx.coroutines.launch
```

В production-коде пользовательские строки и шаблон summary нужно вынести в
resources. Здесь важен сам путь данных.

## Что произошло

Каждый Fragment выполнил одинаковый запрос:

```kotlin
navGraphViewModels(R.id.createTimerFlow)
```

Navigation нашёл один `NavBackStackEntry` для этого graph. Поэтому все три
делегата обратились к одному `ViewModelStore` и получили один экземпляр:

```text
TimerSetupFragment -----------\
SoundSelectionFragment --------> createTimerFlow ViewModelStore
TimerConfirmationFragment ----/          └── CreateTimerFlowViewModel
```

## Проверка шага

1. Откройте flow.
2. Введите `12` минут.
3. Выберите «Без звука».
4. На подтверждении должно появиться `12 мин.` и `Без звука`.
5. Нажмите Back, смените звук и снова перейдите вперёд.
6. Нажмите Back до ввода времени: в поле должно оставаться `12`.
7. Поверните устройство на любом шаге: черновик не должен сброситься.

Если подтверждение показывает значения по умолчанию, проверьте, что во всех
трёх Fragment используется именно `navGraphViewModels` с одинаковым
`R.id.createTimerFlow`, а не `viewModels()`.

## Контрольные вопросы

1. Почему одинаковый класс ViewModel ещё не гарантирует одинаковый экземпляр?
2. Как `R.id.createTimerFlow` влияет на поиск ViewModel?
3. Почему экран setup восстанавливает поле из `uiState`?
4. Где проверяется допустимый диапазон длительности?

Далее: [шаг 5.4](04-back-stack-and-testing.md).
