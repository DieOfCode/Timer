# Fragment — часть 4: состояние и ViewModel

Часть разделена на четыре последовательных шага:

| Шаг | Тема | Результат |
|---|---|---|
| 4.1 | [State, event и UDF](04-state/01-state-events-udf.md) | Понятен поток данных |
| 4.2 | [TimerUiState](04-state/02-timer-ui-state.md) | Состояние экрана описано типами |
| 4.3 | [TimerViewModel](04-state/03-timer-viewmodel.md) | Реализованы переходы состояния |
| 4.4 | [Подключение Fragment](04-state/04-fragment-rendering.md) | UI наблюдает и отображает state |

Итоговая модель:

```text
Пользователь
  -> событие
TimerFragment
  -> метод
TimerViewModel
  -> новый TimerUiState
TimerFragment.render(state)
  -> View
```

Реальный секундный отсчёт появится в следующем модуле. Здесь строим и тестируем
архитектуру состояния без асинхронного таймера.

Начните с [шага 4.1](04-state/01-state-events-udf.md).
