# Шаг 3.1: модель и зависимости Navigation

## Модель

`NavHostFragment` — контейнер текущего экрана:

```text
MainActivity
└── NavHostFragment
    └── текущая destination
```

`nav_graph.xml` описывает destinations и разрешённые переходы. `NavController`
читает graph и командует переходом, а внутри использует FragmentManager.

```text
findNavController().navigate(action)
  -> NavController
  -> FragmentManager
  -> новый Fragment
```

## Подключение

В `[versions]` файла `gradle/libs.versions.toml`:

```toml
navigation = "2.10.1"
```

В `[libraries]`:

```toml
androidx-navigation-fragment = { module = "androidx.navigation:navigation-fragment", version.ref = "navigation" }
androidx-navigation-ui = { module = "androidx.navigation:navigation-ui", version.ref = "navigation" }
```

В `dependencies` файла `app/build.gradle.kts`:

```kotlin
implementation(libs.androidx.navigation.fragment)
implementation(libs.androidx.navigation.ui)
```

`navigation-fragment` предоставляет `NavHostFragment`, `NavController` и
`findNavController()`. `navigation-ui` позднее свяжет навигацию с Toolbar или
`BottomNavigationView`.

Выполните Gradle Sync и Build → Make Project.

## Готово, если

- Gradle Sync успешен;
- accessors `libs.androidx.navigation.fragment` и `.ui` распознаются;
- проект собирается.

Далее: [шаг 3.2](02-destinations.md).
