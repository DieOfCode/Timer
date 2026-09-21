# Fragment — часть 1: фундаментальная модель

## Результат

Вы поймёте различия между Activity, Fragment и View, узнаете роль
`FragmentManager` и покажете минимальный `TimerFragment` внутри `MainActivity`.

Ориентир: 30–45 минут.

## 1. Зачем нужен Fragment

Activity предоставляет системное окно. Внутренние экраны не обязательно делать
отдельными Activity.

```text
Activity = системное окно и host
Fragment = экран или часть экрана внутри host
View     = визуальные объекты этого экрана
```

Fragment позволяет:

- разделить приложение на самостоятельные экраны;
- менять содержимое одной Activity;
- вести внутренний back stack;
- дать каждому экрану отдельный ViewModel;
- адаптировать UI под разные размеры устройства.

## 2. Fragment — не Activity

| Activity | Fragment |
|---|---|
| Объявляется в Manifest | Обычно не объявляется в Manifest |
| Имеет системное окно | Рисует внутри окна Activity |
| Запускается через Intent | Показывается FragmentManager/NavController |
| Имеет собственный Context | Получает Context после прикрепления |
| Завершается через `finish()` | Удаляется или заменяется навигацией |

Fragment нельзя открыть так:

```kotlin
startActivity(Intent(this, TimerFragment::class.java))
```

Он не является системной Activity.

## 3. Fragment — не View

Это разные сущности:

```text
TimerFragment      — Kotlin-объект, управляющий экраном
fragment_timer.xml — описание интерфейса
View               — объекты, созданные из XML в памяти
```

Правильная связь:

```text
TimerFragment
    |
    | временно создаёт
    v
View из fragment_timer.xml
```

Fragment может существовать без View. Это станет главной темой части 2.

## 4. Кто управляет Fragment

`FragmentManager`:

- создаёт и прикрепляет Fragment;
- вызывает lifecycle callbacks;
- помещает его View в контейнер;
- сохраняет и восстанавливает состояние;
- удаляет и заменяет Fragment;
- ведёт back stack транзакций.

У `AppCompatActivity` он доступен как:

```kotlin
supportFragmentManager
```

Позднее Navigation Component будет работать поверх него:

```text
NavController.navigate()
  -> Navigation Component
  -> FragmentManager
  -> нужный Fragment
```

FragmentManager может восстановить Fragment самостоятельно. Поэтому нельзя
хранить Fragment в singleton или рассчитывать, что его создаёт только наш код.

## 5. Host и контейнер

Fragment нужен участок окна, куда будет помещена его View:

```text
MainActivity
└── FragmentContainerView
    └── TimerFragment
        └── fragment_timer.xml
```

Сейчас контейнер временно покажет `TimerFragment` напрямую. В части 3 заменим его
на `NavHostFragment`.

## Практика

### Шаг 1. Разметка Fragment

Создайте `app/src/main/res/layout/fragment_timer.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/timerFragmentRoot"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".timer.TimerFragment">

    <TextView
        android:id="@+id/fragmentTitle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/timer_fragment_title"
        android:textSize="28sp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

В `strings.xml` добавьте:

```xml
<string name="timer_fragment_title">Экран TimerFragment</string>
```

### Шаг 2. Минимальный Fragment

Создайте пакет `com.example.timer.timer` и `TimerFragment.kt`:

```kotlin
package com.example.timer.timer

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.timer.R

class TimerFragment : Fragment(R.layout.fragment_timer) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TimerFragment", "onCreate")
    }

    override fun onDestroy() {
        Log.d("TimerFragment", "onDestroy")
        super.onDestroy()
    }
}
```

`Fragment(R.layout.fragment_timer)` сообщает FragmentManager, какой layout нужно
создать. Binding добавим в части 2.

### Шаг 3. Activity как host

Временно замените `activity_main.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.fragment.app.FragmentContainerView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/fragmentContainer"
    android:name="com.example.timer.timer.TimerFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity" />
```

### Шаг 4. Упростите MainActivity

В новом `ActivityMainBinding` нет `startButton`. Удалите прежний listener:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
}
```

### Шаг 5. Эксперимент

1. Запустите приложение.
2. Убедитесь, что виден текст «Экран TimerFragment».
3. Отфильтруйте Logcat по `TimerFragment`.
4. Поверните устройство.
5. Посмотрите вызовы `onCreate()` и `onDestroy()`.

### Шаг 6. Расширенный lifecycle-лог

Чтобы увидеть работу FragmentManager, временно залогируйте основные callbacks:

```kotlin
package com.example.timer.timer

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.timer.R

class TimerFragment : Fragment(R.layout.fragment_timer) {

    private fun log(event: String) {
        Log.d(
            "TimerFragment",
            "$event fragment=${System.identityHashCode(this)} " +
                "view=${view?.let(System::identityHashCode)}"
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        log("onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("onCreate")
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        log("onViewCreated")
    }

    override fun onStart() {
        super.onStart()
        log("onStart")
    }

    override fun onResume() {
        super.onResume()
        log("onResume")
    }

    override fun onPause() {
        log("onPause")
        super.onPause()
    }

    override fun onStop() {
        log("onStop")
        super.onStop()
    }

    override fun onDestroyView() {
        log("onDestroyView")
        super.onDestroyView()
    }

    override fun onDestroy() {
        log("onDestroy")
        super.onDestroy()
    }

    override fun onDetach() {
        log("onDetach")
        super.onDetach()
    }
}
```

Числа `fragment=...` и `view=...` — идентичности объектов в памяти. Они помогают
отличить новый экземпляр от повторного callback старого экземпляра.

Проверьте три сценария:

```text
Первый запуск:
onAttach -> onCreate -> onViewCreated -> onStart -> onResume

Кнопка Home:
onPause -> onStop
Возврат:
onStart -> onResume

Поворот:
старые Fragment/View проходят уничтожение,
затем FragmentManager создаёт/восстанавливает новые объекты
```

Точная последовательность отдельных системных событий может различаться, поэтому
смотрите на смысл состояний, а не заучивайте лог как неизменную строку.

`onDestroy()` не гарантирован при принудительном завершении процесса. Не сохраняйте
важные данные только в этом callback.

```text
MainActivity создаёт FragmentContainerView
  -> FragmentManager видит android:name
  -> создаёт TimerFragment
  -> Fragment создаёт fragment_timer.xml
  -> View появляется в контейнере
```

## Контрольные вопросы

1. Почему `TimerFragment` не объявлен в Manifest?
2. Кто управляет созданием Fragment?
3. Чем `TimerFragment` отличается от его View?

## Готово, если

- проект собирается;
- на экране виден `TimerFragment`;
- Activity не обращается к его View;
- lifecycle-сообщения видны в Logcat;
- вы можете объяснить Activity → container → Fragment → View.

Далее: [часть 2](02-view-lifecycle-binding.md).
