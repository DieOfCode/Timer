# Шаг 3.3: navigation graph и NavHostFragment

## Создаём graph

Создайте `app/src/main/res/navigation/nav_graph.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<navigation
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/navGraph"
    app:startDestination="@id/timerFragment">

    <fragment
        android:id="@+id/timerFragment"
        android:name="com.example.timer.timer.TimerFragment"
        android:label="@string/timer_title"
        tools:layout="@layout/fragment_timer">

        <action
            android:id="@+id/action_timerFragment_to_settingsFragment"
            app:destination="@id/settingsFragment" />

        <action
            android:id="@+id/action_timerFragment_to_historyFragment"
            app:destination="@id/historyFragment" />
    </fragment>

    <fragment
        android:id="@+id/settingsFragment"
        android:name="com.example.timer.settings.SettingsFragment"
        android:label="@string/settings_title"
        tools:layout="@layout/fragment_settings" />

    <fragment
        android:id="@+id/historyFragment"
        android:name="com.example.timer.history.HistoryFragment"
        android:label="@string/history_title"
        tools:layout="@layout/fragment_history" />
</navigation>
```

`timerFragment` — стартовая destination. `action_...` — маршруты, а не экраны.
`tools:layout` используется только редактором preview.

## Подключаем NavHost

Замените `activity_main.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.fragment.app.FragmentContainerView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/navHostFragment"
    android:name="androidx.navigation.fragment.NavHostFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:defaultNavHost="true"
    app:navGraph="@navigation/nav_graph"
    tools:context=".MainActivity" />
```

`defaultNavHost=true` передаёт NavHost обработку системного Back.
`MainActivity.kt` менять не требуется.

## Готово, если

- приложение запускается;
- стартовым экраном остаётся `TimerFragment`;
- `MainActivity` создаёт NavHost, а не `TimerFragment` напрямую;
- проект собирается.

Далее: [шаг 3.4](04-actions-and-testing.md).
