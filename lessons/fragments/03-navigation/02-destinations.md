# Шаг 3.2: создаём три destination

## Строки

Добавьте в `strings.xml`:

```xml
<string name="timer_title">Таймер</string>
<string name="settings_title">Настройки</string>
<string name="history_title">История</string>
<string name="open_settings">Открыть настройки</string>
<string name="open_history">Открыть историю</string>
<string name="go_back">Назад</string>
```

## TimerFragment

В `fragment_timer.xml` оставьте `fragmentTitle` и добавьте две кнопки под ним:

```xml
<Button
    android:id="@+id/openSettingsButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginTop="24dp"
    android:text="@string/open_settings"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toBottomOf="@id/fragmentTitle" />

<Button
    android:id="@+id/openHistoryButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginTop="12dp"
    android:text="@string/open_history"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toBottomOf="@id/openSettingsButton" />
```

## SettingsFragment

Создайте `fragment_settings.xml`: `ConstraintLayout`, центрированный `TextView`
`settingsTitle` и кнопку `backButton` под ним. Используйте строки
`settings_title` и `go_back`.

Создайте `com.example.timer.settings.SettingsFragment` по шаблону binding из
части 2. В `onViewCreated()`:

```kotlin
binding.backButton.setOnClickListener {
    findNavController().popBackStack()
}
```

Добавьте импорт:

```kotlin
import androidx.navigation.fragment.findNavController
```

## HistoryFragment

Аналогично создайте `fragment_history.xml` с `historyTitle` и `backButton`, затем
`com.example.timer.history.HistoryFragment` с таким же `popBackStack()`.

Одинаковый `backButton` допустим в разных layout: каждый binding работает только
со своим деревом View.

## Готово, если

- созданы три Fragment и три layout;
- каждый Fragment очищает `_binding` в `onDestroyView()`;
- сгенерированы `FragmentSettingsBinding` и `FragmentHistoryBinding`;
- проект собирается.

Далее: [шаг 3.3](03-graph-and-host.md).
