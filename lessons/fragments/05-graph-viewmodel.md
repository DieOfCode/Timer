# Fragment — часть 5: ViewModel navigation graph

В части 4 у `TimerFragment` появилась собственная `TimerViewModel`. Теперь
разберём другой случай: несколько последовательных экранов редактируют один
общий черновик.

```text
createTimerFlow
├── TimerSetupFragment
├── SoundSelectionFragment
└── TimerConfirmationFragment
         └── один CreateTimerFlowViewModel
```

Часть разделена на четыре шага:

| Шаг | Тема | Практический результат |
|---|---|---|
| 5.1 | [Scope и owner ViewModel](05-graph-viewmodel/01-scope-and-owner.md) | Понятно, зачем нужен graph scope |
| 5.2 | [Nested graph и три экрана](05-graph-viewmodel/02-nested-graph.md) | Flow открывается и проходит до конца |
| 5.3 | [Общий ViewModel](05-graph-viewmodel/03-shared-viewmodel.md) | Длительность и звук сохраняются между экранами |
| 5.4 | [Back stack и `onCleared()`](05-graph-viewmodel/04-back-stack-and-testing.md) | Проверены срок жизни и очистка ViewModel |

## Что важно до начала

Это дополнительная практика после MVP. Она не заменяет `TimerViewModel`:

```text
TimerViewModel
└── состояние уже запущенного таймера

CreateTimerFlowViewModel
└── временный черновик во время создания таймера
```

Не привязывайте одну общую ViewModel к `TimerFragment`, `SettingsFragment` и
`HistoryFragment` только потому, что они находятся в одном корневом graph. Это
самостоятельные экраны с разными задачами.

Начните с [шага 5.1](05-graph-viewmodel/01-scope-and-owner.md).
