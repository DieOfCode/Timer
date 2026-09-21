# Архитектура приложения «Таймер»

Роль `Intent` и `PendingIntent` в этой архитектуре подробно разобрана в
[`lessons/intent.md`](lessons/intent.md).

Роль Fragment, `NavHostFragment`, lifecycle View и правила работы с binding
разделены на короткие занятия в [`lessons/fragments.md`](lessons/fragments.md).

## Зафиксированное решение

Приложение строим по принципу **Single Activity** с интерфейсом на XML:

```text
Timer
└── MainActivity
    └── NavHostFragment
        ├── TimerFragment
        ├── SettingsFragment
        └── HistoryFragment
```

Новые Activity для обычных экранов приложения не создаём. Переходами между
экранами управляет Navigation Component.

## Ответственность компонентов

### MainActivity

`MainActivity` — системная точка входа и контейнер навигации.

Она отвечает за:

- создание корневого layout;
- размещение `NavHostFragment`;
- общие элементы интерфейса, если они появятся;
- обработку системных переходов, которые относятся ко всему приложению.

В `MainActivity` не размещаем:

- расчёт оставшегося времени;
- логику старта, паузы и сброса;
- чтение истории;
- настройки пользователя;
- прямое управление View отдельных Fragment.

### NavHostFragment

`NavHostFragment` занимает основную область `MainActivity` и показывает одну
destination из navigation graph. Он заменяет содержимое при переходах и ведёт
back stack экранов.

### TimerFragment

Главный экран приложения:

- ввод часов, минут и секунд;
- отображение оставшегося времени;
- старт, пауза, продолжение и сброс;
- наблюдение за `TimerViewModel`;
- запрос необходимых разрешений в пользовательском контексте.

XML: `fragment_timer.xml`.

### SettingsFragment

Экран настроек:

- звук окончания;
- вибрация;
- поведение уведомления;
- другие пользовательские параметры.

XML: `fragment_settings.xml`.

Настройки позднее можно хранить через DataStore. Экран не должен напрямую
управлять работающим таймером.

### HistoryFragment

Экран завершённых таймеров:

- список предыдущих запусков;
- длительность;
- время запуска и завершения;
- результат: завершён или отменён.

XML: `fragment_history.xml`.

История не входит в первый MVP. Сначала используем временные данные, затем при
необходимости добавляем Room.

## ViewModel

Для каждого самостоятельного экрана используем собственный ViewModel:

```text
TimerFragment    -> TimerViewModel
SettingsFragment -> SettingsViewModel
HistoryFragment  -> HistoryViewModel
```

`TimerViewModel` хранит UI-состояние и управляет переходами таймера:

```text
READY -> RUNNING -> PAUSED -> RUNNING -> FINISHED
```

Fragment отображает состояние, но не является его источником.

Если нескольким Fragment понадобится общее состояние, сначала проверяем, нельзя
ли получить его из repository. Activity-scoped ViewModel добавляем только при
реальной необходимости.

Для нескольких шагов одного ограниченного flow используем ViewModel, привязанный
к nested navigation graph. Например, будущие экраны выбора длительности, звука и
названия пресета смогут разделять `CreateTimerFlowViewModel`. Он существует, пока
`createTimerFlow` находится в back stack, и очищается при выходе из всего flow.
Для `TimerFragment`, `SettingsFragment` и `HistoryFragment` общий graph-scoped
ViewModel не используем: это самостоятельные экраны, а их долгоживущие данные
связываются через repository.

## Навигация

Navigation graph хранится в XML:

```text
res/navigation/nav_graph.xml
```

Стартовая destination:

```text
TimerFragment
```

Планируемые переходы:

```text
TimerFragment -> SettingsFragment
TimerFragment -> HistoryFragment
SettingsFragment --Back--> TimerFragment
HistoryFragment  --Back--> TimerFragment
```

Для переходов используем `NavController`, а не создаём Fragment вручную через
конструктор и не выполняем ручные `FragmentTransaction` без необходимости.

## Планируемая структура файлов

```text
app/src/main/
├── AndroidManifest.xml
├── java/com/example/timer/
│   ├── MainActivity.kt
│   ├── timer/
│   │   ├── TimerFragment.kt
│   │   ├── TimerViewModel.kt
│   │   ├── TimerUiState.kt
│   │   └── TimerState.kt
│   ├── settings/
│   │   ├── SettingsFragment.kt
│   │   └── SettingsViewModel.kt
│   ├── history/
│   │   ├── HistoryFragment.kt
│   │   └── HistoryViewModel.kt
│   ├── alarm/
│   │   ├── AlarmScheduler.kt
│   │   └── TimerExpiredReceiver.kt
│   └── notification/
│       └── NotificationHelper.kt
└── res/
    ├── layout/
    │   ├── activity_main.xml
    │   ├── fragment_timer.xml
    │   ├── fragment_settings.xml
    │   └── fragment_history.xml
    ├── navigation/
    │   └── nav_graph.xml
    └── values/
        ├── colors.xml
        ├── dimens.xml
        ├── strings.xml
        └── themes.xml
```

Это целевая структура, а не список файлов, которые нужно создать немедленно.
Компоненты добавляем постепенно по мере прохождения курса.

## Порядок реализации

1. Разобраться с текущей `MainActivity`, XML, `inflate()` и View Binding.
2. Подключить Navigation Component.
3. Превратить `activity_main.xml` в контейнер с `NavHostFragment`.
4. Перенести интерфейс таймера в `TimerFragment`.
5. Реализовать `TimerViewModel` и состояния таймера.
6. Добавить пустые `SettingsFragment` и `HistoryFragment` для практики навигации.
7. Реализовать настройки.
8. Реализовать историю только после готового таймера и фонового уведомления.

## Решения для MVP

- Одна Activity: `MainActivity`.
- Первый и главный экран: `TimerFragment`.
- XML + View Binding.
- Navigation graph в XML.
- `TimerViewModel` — источник состояния таймера.
- `CountDownTimer` — обновление открытого экрана.
- `AlarmManager` и `BroadcastReceiver` — завершение в фоне.
- `SettingsFragment` и `HistoryFragment` сначала могут быть заглушками.

Эти решения считаются зафиксированными до появления конкретной причины изменить
архитектуру.
