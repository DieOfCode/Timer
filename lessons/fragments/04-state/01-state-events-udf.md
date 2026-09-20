# Шаг 4.1: State, event и однонаправленный поток

## Что уже есть и чего ещё нет

На входе в этот шаг в проекте уже существуют `MainActivity`, `NavHostFragment` и
три экранных Fragment. Класса `TimerViewModel` пока нет — мы создадим его только в
шаге 4.3.

В этом шаге слово `ViewModel` используется для описания **целевой схемы**. Код с
`viewModel.start()` ниже пока не нужно добавлять в `TimerFragment`.

```text
Шаг 4.1 — понимаем проблему и поток данных
Шаг 4.2 — описываем TimerUiState
Шаг 4.3 — создаём TimerViewModel
Шаг 4.4 — подключаем его к TimerFragment
```

## Что такое ViewModel — первое знакомство

`ViewModel` — отдельный объект, который хранит состояние экрана и обрабатывает
относящиеся к нему правила. Он не является `View`, `Fragment` или `Activity` и не
имеет собственного XML.

```text
TimerFragment
  - показывает View
  - получает клики

TimerViewModel
  - хранит TimerUiState
  - принимает start/pause/reset
  - создаёт новое состояние
```

Fragment получает ViewModel через AndroidX, а не создаёт его обычным
конструктором. Позже это будет выглядеть так:

```kotlin
private val viewModel: TimerViewModel by viewModels()
```

Эта строка здесь только для знакомства. До шага 4.3 класса `TimerViewModel` нет,
поэтому копировать её в проект ещё не нужно.

## Зачем отделять состояние от View

Плохой источник состояния:

```kotlin
val isRunning = binding.timerText.text != "00:00:00"
```

`TextView` предназначен для отображения. Он может быть уничтожен при навигации
или повороте, а формат текста может измениться из-за локализации.

Состояние — данные, достаточные для полной отрисовки экрана:

```text
status
remainingMillis
выбранная длительность
доступность кнопок
```

## State и event

```text
Event: пользователь нажал «Старт»
State: таймер работает, осталось 300000 мс
```

Event кратковременен. State существует между событиями и описывает текущий
результат.

| Event | Новый state |
|---|---|
| Start | `RUNNING` |
| Pause | `PAUSED` |
| Resume | `RUNNING` |
| Reset | `READY` |

## Unidirectional Data Flow

```text
События идут вверх:
View -> Fragment -> ViewModel

Состояние идёт вниз:
ViewModel -> Fragment -> View
```

В будущей реализации Fragment не будет менять данные ViewModel напрямую. Он
будет вызывать методы:

```kotlin
viewModel.start()
viewModel.pause()
viewModel.reset()
```

ViewModel будет публиковать read-only state. Fragment получит state и вызовет:

```kotlin
render(state)
```

## Ответственность

```text
TimerFragment
- listeners
- lifecycle-aware collection
- render
- Navigation

TimerViewModel
- проверка допустимых переходов
- изменение состояния
- подготовка UI state

View
- только отображение
```

## Мини-практика

Код в проект на шаге 4.1 не добавляем. Это упражнение на проектирование.

Для каждого действия напишите event и ожидаемый state:

1. Первый запуск приложения.
2. Нажатие Start.
3. Нажатие Pause.
4. Нажатие Reset.
5. Поворот экрана во время `PAUSED`.

## Контрольные вопросы

1. Почему `TextView.text` не должен быть источником времени?
2. В каком направлении идут события?
3. В каком направлении идёт состояние?

Далее: [шаг 4.2](02-timer-ui-state.md).
