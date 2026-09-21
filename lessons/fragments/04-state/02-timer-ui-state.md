# Шаг 4.2: моделируем TimerUiState

## Состояния таймера

Создайте пакет `com.example.timer.timer` и файл `TimerStatus.kt`:

```kotlin
package com.example.timer.timer

enum class TimerStatus {
    READY,
    RUNNING,
    PAUSED,
    FINISHED
}
```

`enum class` ограничивает набор допустимых значений. Строка `"runing"` могла бы
содержать опечатку; `TimerStatus.RUNNING` проверяется компилятором.

## Снимок UI

Создайте `TimerUiState.kt`:

```kotlin
package com.example.timer.timer

data class TimerUiState(
    val status: TimerStatus = TimerStatus.READY,
    val remainingMillis: Long = 0L
)
```

`data class` — неизменяемый снимок экрана. Для изменения создаётся копия:

```kotlin
val running = current.copy(
    status = TimerStatus.RUNNING,
    remainingMillis = 300_000L
)
```

Мы не храним форматированную строку как источник истины:

```text
remainingMillis = 300000  — данные
"00:05:00"               — отображение
```

Форматирование можно выполнить при подготовке или отрисовке UI. Пока оставляем
миллисекунды, чтобы состояние не зависело от языка.

## Инварианты

Сформулируйте правила:

- `remainingMillis` не бывает отрицательным;
- `READY` имеет нулевой или выбранный начальный результат по решению продукта;
- `RUNNING` означает активный отсчёт;
- `PAUSED` сохраняет положительный остаток;
- `FINISHED` имеет `remainingMillis == 0L`.

Инвариант — условие, которое должно оставаться истинным при любом переходе.

## Практика

Создайте оба файла и временно проверьте в любом Kotlin-коде:

```kotlin
val ready = TimerUiState()
val running = ready.copy(
    status = TimerStatus.RUNNING,
    remainingMillis = 300_000L
)
```

## Контрольные вопросы

1. Почему статус лучше строки?
2. Почему `TimerUiState` состоит из `val`?
3. Чем данные `300_000L` отличаются от текста `00:05:00`?

Далее: [шаг 4.3](03-timer-viewmodel.md).
