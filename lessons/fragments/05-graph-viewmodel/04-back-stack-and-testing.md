# Шаг 5.4: back stack, очистка и итоговая проверка

Общие данные между экранами — только половина задачи. Важно доказать, что
черновик очищается именно после завершения flow.

## Что делает action завершения

В шаге 5.2 мы добавили:

```xml
<action
    android:id="@+id/action_timerConfirmationFragment_to_timerFragment"
    app:destination="@id/timerFragment"
    app:launchSingleTop="true"
    app:popUpTo="@id/createTimerFlow"
    app:popUpToInclusive="true" />
```

Параметры означают:

- `destination` — куда перейти;
- `launchSingleTop` — не создавать второй `TimerFragment`, если после очистки он
  уже находится сверху;
- `popUpTo` — до какой записи очистить back stack;
- `popUpToInclusive="true"` — удалить и сам `createTimerFlow`.

Упрощённо:

```text
До «Готово»:                    После «Готово»:

timerFragment                   timerFragment
createTimerFlow       ->
timerSetupFragment
soundSelectionFragment
timerConfirmationFragment
```

Когда запись `createTimerFlow` удалена, её `ViewModelStore` очищается и вызывается
`CreateTimerFlowViewModel.onCleared()`.

Если убрать `popUpToInclusive`, запись graph может остаться в back stack, а
черновик — продолжить жить дольше ожидаемого.

## Проверяем идентичность экземпляра

В каждом из трёх Fragment временно добавьте в `onViewCreated()`:

```kotlin
Log.d(
    "CreateTimerFlowScreen",
    "${javaClass.simpleName}: ${System.identityHashCode(viewModel)}"
)
```

Импорт:

```kotlin
import android.util.Log
```

При переходах число после двоеточия должно быть одинаковым:

```text
TimerSetupFragment: 12345678
SoundSelectionFragment: 12345678
TimerConfirmationFragment: 12345678
```

`identityHashCode` используется только как учебная метка экземпляра. Логику
приложения на нём строить нельзя.

## Проверяем срок жизни

Откройте Logcat и установите фильтр:

```text
CreateTimerFlowVM
```

### Сценарий 1: переходы вперёд

1. Откройте flow.
2. Перейдите Setup → Sound → Confirmation.
3. Найдите сообщение `created`.
4. Убедитесь, что оно появилось один раз.
5. Сообщения `cleared` пока быть не должно.

Причина: меняются destination, но `createTimerFlow` остаётся в back stack.

### Сценарий 2: поворот

1. Оставаясь внутри flow, поверните устройство.
2. Убедитесь, что введённые данные сохранились.
3. Сравните метку экземпляра до и после поворота.
4. `onCleared()` вызываться не должен.

View и Fragment пересоздаются, но owner graph-scoped ViewModel переживает
configuration change вместе с `NavHostFragment`.

### Сценарий 3: Back внутри flow

1. На подтверждении нажмите Back.
2. Вернитесь к Setup.
3. Убедитесь, что введённые данные остались.
4. `onCleared()` пока не должен вызываться.

Запись graph всё ещё есть в back stack.

### Сценарий 4: завершение flow

1. Снова дойдите до подтверждения.
2. Нажмите «Готово».
3. В Logcat должно появиться `cleared` с прежней меткой экземпляра.
4. Откройте flow заново.
5. Должно появиться новое сообщение `created` с другой меткой.
6. Значения должны вернуться к `5` минутам и `BELL`.

Это главный критерий правильного graph scope.

### Сценарий 5: выход Back с первого шага

На `TimerSetupFragment` нажмите системный Back. Весь nested graph должен исчезнуть
из back stack, поэтому ожидается `onCleared()`.

Если этого не происходит, снимите состояние back stack в отладчике и проверьте,
что вход выполнялся через action к `R.id.createTimerFlow`, а не прямой переход к
`timerSetupFragment`.

## Что не гарантирует ViewModel

Graph-scoped ViewModel переживает переходы между Fragment и обычный поворот, но
не является постоянным хранилищем:

```text
Переход внутри graph       -> данные остаются
Поворот                    -> данные остаются
Удаление graph из stack    -> данные очищаются
Уничтожение процесса       -> данные могут быть потеряны
```

Для восстановления после уничтожения процесса позже используют
`SavedStateHandle`, а для постоянных пользовательских данных — Repository,
DataStore или базу данных.

## Итоговый чек-лист

- [ ] `createTimerFlow` — отдельный nested graph.
- [ ] Его `startDestination` — `timerSetupFragment`.
- [ ] Все три Fragment используют `navGraphViewModels(R.id.createTimerFlow)`.
- [ ] Setup записывает длительность в ViewModel.
- [ ] Sound записывает выбранный сигнал.
- [ ] Confirmation отображает общий `uiState`.
- [ ] Back и поворот не сбрасывают черновик.
- [ ] Завершение удаляет graph через `popUpToInclusive`.
- [ ] После удаления graph вызывается `onCleared()`.
- [ ] Новый вход создаёт чистый черновик.

## Контрольные вопросы

1. Кто является owner `CreateTimerFlowViewModel`?
2. Почему переход между destination не вызывает `onCleared()`?
3. Что именно делает `popUpToInclusive="true"`?
4. Почему ViewModel нельзя считать постоянным хранилищем?
5. Чем срок жизни `CreateTimerFlowViewModel` отличается от Activity-scoped
   ViewModel?

Далее: [часть 6](../06-restoration-and-checkpoint.md).
