# Шаг 5.1: scope и owner ViewModel

## Что означает scope

`ViewModel` живёт не сама по себе. Она хранится внутри объекта, реализующего
`ViewModelStoreOwner`. Такой объект называют owner — владелец ViewModel.

```text
ViewModelStoreOwner
└── ViewModelStore
    └── экземпляры ViewModel
```

Когда owner окончательно уничтожается, его `ViewModelStore` очищается, а у
ViewModel вызывается `onCleared()`.

Scope — это граница, внутри которой один экземпляр ViewModel доступен и
сохраняется.

## Три распространённых scope

### Scope одного Fragment

```kotlin
private val viewModel: TimerViewModel by viewModels()
```

Owner — текущий Fragment. Другой Fragment с таким же объявлением получит другой
экземпляр.

Подходит для состояния одного экрана:

```text
TimerFragment -> TimerViewModel
```

### Scope Activity

```kotlin
private val viewModel: SharedViewModel by activityViewModels()
```

Owner — `MainActivity`. Все её Fragment могут получить один экземпляр.

Это широкий scope: ViewModel будет жить, пока живёт Activity, даже если flow,
ради которого она была создана, уже закрыт. Для короткого мастера настройки это
часто слишком долго.

### Scope navigation graph

```kotlin
private val viewModel: CreateTimerFlowViewModel
    by navGraphViewModels(R.id.createTimerFlow)
```

Owner — `NavBackStackEntry` graph с ID `createTimerFlow`. Один экземпляр доступен
всем destination, которые находятся внутри этого graph.

```text
Вошли в createTimerFlow  -> создан NavBackStackEntry и ViewModel
Переходим между шагами   -> используется та же ViewModel
Поворачиваем экран       -> используется та же ViewModel
Удалили graph из stack   -> onCleared()
Вошли снова              -> создан новый экземпляр
```

## Navigation graph не является экраном

Nested graph — логическая группа destination. У него нет собственного XML
layout и собственного Fragment. Однако в back stack у него есть запись, которая
может владеть ViewModel.

```text
NavController back stack
├── timerFragment
├── createTimerFlow       <- owner общей ViewModel
├── timerSetupFragment
└── soundSelectionFragment
```

Когда открыт следующий шаг, предыдущие Fragment могут быть остановлены, а их
View — уничтожены. Общая ViewModel при этом остаётся, потому что запись
`createTimerFlow` всё ещё находится в back stack.

## Почему не использовать корневой navGraph

Технически ViewModel можно привязать к корневому `navGraph`, но тогда её scope
почти совпадёт со сроком жизни всего `NavHostFragment`:

```text
navGraph
├── TimerFragment
├── SettingsFragment
├── HistoryFragment
└── createTimerFlow
```

Черновик создания таймера продолжит жить после завершения flow. Узкий nested
graph точнее выражает требование: «храним данные только пока пользователь
создаёт таймер».

## Мини-практика без кода

Определите подходящий owner:

1. Текущее состояние экрана таймера — `TimerFragment`.
2. Черновик, который нужен трём шагам создания таймера — `createTimerFlow`.
3. Настройка темы, общая абсолютно всем экранам Activity — возможно,
   `MainActivity`, хотя для постоянной настройки позже лучше Repository/DataStore.

Нарисуйте ожидаемый срок жизни черновика:

```text
TimerFragment
    |
    | открыть flow
    v
[ createTimerFlow: ViewModel создана ]
    Setup -> Sound -> Confirmation
[ завершить flow: ViewModel очищена ]
    |
    v
TimerFragment
```

На этом шаге код проекта не меняем.

## Контрольные вопросы

1. Кто хранит экземпляр ViewModel?
2. Получат ли два Fragment с `by viewModels()` один экземпляр?
3. Почему Activity scope слишком широк для временного черновика?
4. Что должно произойти с ViewModel после удаления nested graph из back stack?

Далее: [шаг 5.2](02-nested-graph.md).
