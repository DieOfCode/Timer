# Курс по Fragment: оглавление

Большой урок разделён на короткие части. Каждая часть добавляет одну идею и
заканчивается практикой в приложении «Таймер».

| Часть | Тема | Практический результат |
|---|---|---|
| 1 | [Модель Fragment](fragments/01-foundations.md) | Создан и показан минимальный `TimerFragment` |
| 2 | [Lifecycle View и View Binding](fragments/02-view-lifecycle-binding.md) | UI перенесён во Fragment без утечки binding |
| 3 | [Navigation и back stack](fragments/03-navigation.md) | Подключены `NavHostFragment` и три destination |
| 4 | [Состояние и общение](fragments/04-state-and-communication.md) | Fragment работает через свой ViewModel |
| 5 | [Graph-scoped ViewModel](fragments/05-graph-viewmodel.md) | В четыре шага создаётся общий ViewModel внутри flow |
| 6 | [Восстановление и ошибки](fragments/06-restoration-and-checkpoint.md) | Пройдены поворот и итоговый чек-лист |

## Как проходить

1. Прочитайте теорию одной части.
2. Ответьте на контрольные вопросы.
3. Выполните только её практику.
4. Запустите приложение.
5. Сравните результат с критерием готовности.

Не переходите дальше при ошибке сборки. Части 1–4 и 6 обязательны для MVP.
Часть 5 выполняется после MVP.

```text
MainActivity
└── NavHostFragment
    ├── TimerFragment    -> TimerViewModel
    ├── SettingsFragment -> SettingsViewModel
    └── HistoryFragment  -> HistoryViewModel
```

Начинаем с [части 1](/Users/dieofcode/StudioProjects/Timer/lessons/fragments/01-foundations.md).
