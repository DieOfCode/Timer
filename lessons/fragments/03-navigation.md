# Fragment — часть 3: Navigation и back stack

Практика Navigation разделена на четыре шага. Выполняйте их последовательно.

| Шаг | Тема | Результат |
|---|---|---|
| 3.1 | [Зависимости и модель](03-navigation/01-dependencies.md) | Navigation подключён к проекту |
| 3.2 | [Три destination](03-navigation/02-destinations.md) | Созданы Timer, Settings и History |
| 3.3 | [Graph и NavHost](03-navigation/03-graph-and-host.md) | `TimerFragment` показывается через graph |
| 3.4 | [Actions, Back и тестирование](03-navigation/04-actions-and-testing.md) | Все переходы работают |

Итог:

```text
MainActivity
└── NavHostFragment
    ├── TimerFragment
    ├── SettingsFragment
    └── HistoryFragment
```

Ориентир на всю часть: 60–90 минут. После каждого шага собирайте проект.

Начните с [шага 3.1](03-navigation/01-dependencies.md).
