# Шаг 3.4: actions, Back и проверка

## Переходы из TimerFragment

Добавьте импорт:

```kotlin
import androidx.navigation.fragment.findNavController
```

В `onViewCreated()`:

```kotlin
binding.openSettingsButton.setOnClickListener {
    findNavController().navigate(
        R.id.action_timerFragment_to_settingsFragment
    )
}

binding.openHistoryButton.setOnClickListener {
    findNavController().navigate(
        R.id.action_timerFragment_to_historyFragment
    )
}
```

В `SettingsFragment` и `HistoryFragment` кнопка вызывает:

```kotlin
findNavController().popBackStack()
```

## Ожидаемый back stack

```text
Запуск:           [Timer]
Открыли Settings: [Timer, Settings]
Back:             [Timer]
Открыли History:  [Timer, History]
Back:             [Timer]
```

## Проверка

1. Запустите приложение.
2. Откройте Settings и вернитесь кнопкой интерфейса.
3. Откройте History и вернитесь системным Back.
4. Поверните устройство на каждом экране.
5. Убедитесь, что Activity не создаётся для каждого перехода.
6. Проверьте `onDestroyView()` в Logcat.

## Частые ошибки

- `NavHostFragment` не найден: проверьте dependency и Gradle Sync.
- `nav_graph` не найден: файл должен лежать в `res/navigation`.
- action не распознаётся: проверьте его `android:id` и выполните Rebuild.
- `ClassNotFoundException`: `android:name` не совпадает с package класса.
- `View already has a parent`: binding Fragment должен использовать
  `inflate(inflater, container, false)`.
- Back закрывает Activity: проверьте `app:defaultNavHost="true"` и переход через
  `NavController`.
- Fragment добавлен в Manifest: удалите его; там остаётся только Activity.

## Контрольные вопросы

1. Чем NavHostFragment отличается от destination?
2. Что означает `startDestination`?
3. Чем action отличается от destination?
4. Что делает `popBackStack()`?
5. Что остаётся неизменным при переходе: Activity, NavHost или destination?

## Готово, если

- работают оба перехода;
- работают UI-кнопка и системный Back;
- `MainActivity` не знает о View экранов;
- все Fragment очищают binding;
- вы можете нарисовать текущий back stack.

Далее: [часть 4](../04-state-and-communication.md).
