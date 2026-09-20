# Урок: Intent в приложении «Таймер»

## Результат урока

После урока вы сможете объяснить:

- зачем Android нужен `Intent`;
- чем explicit intent отличается от implicit intent;
- что такое `action`, `data`, `category`, `component`, `extras` и `flags`;
- почему между Fragment нашего приложения не нужно передавать Intent;
- как уведомление открывает `MainActivity`;
- как `AlarmManager` доставляет событие в `BroadcastReceiver`;
- чем `Intent` отличается от `PendingIntent`;
- какие данные можно передавать через extras;
- какие проверки нужны для безопасности.

---

## 1. Ментальная модель

`Intent` — объект-сообщение, описывающий намерение выполнить действие.

Intent сам ничего не запускает. Мы создаём сообщение и передаём его Android:

```text
Наш код
  -> создаёт Intent
  -> вызывает системный метод
  -> Android читает Intent
  -> выбирает или находит компонент
  -> запускает компонент и передаёт ему Intent
```

Пример запуска конкретной Activity:

```kotlin
val intent = Intent(this, MainActivity::class.java)
startActivity(intent)
```

Здесь `Intent` означает: «Android, запусти `MainActivity`».

Intent можно представить как конверт:

```text
Кому:      component
Что сделать: action
С чем:     data
Уточнения: categories
Параметры: extras
Инструкция системе: flags
```

Один Intent не обязан содержать все поля. Состав зависит от сценария.

---

## 2. Какие компоненты получают Intent

Intent используется для взаимодействия с системными компонентами Android:

| Получатель | Как отправляется |
|---|---|
| `Activity` | `startActivity(intent)` |
| `Service` | `startService(intent)` или другой подходящий API |
| `BroadcastReceiver` | `sendBroadcast(intent)` или системный планировщик |

В нашем таймере:

```text
Launcher
  -> Intent
  -> MainActivity

AlarmManager
  -> PendingIntent с explicit Intent
  -> TimerExpiredReceiver

Уведомление
  -> PendingIntent с explicit Intent
  -> MainActivity
```

Fragment не является самостоятельным системным компонентом и не запускается
через `startActivity()`. Переходы между Fragment выполняет `NavController`.

---

## 3. Explicit intent

Explicit intent точно указывает класс получателя:

```kotlin
val intent = Intent(requireContext(), MainActivity::class.java)
startActivity(intent)
```

Ключевая часть:

```kotlin
MainActivity::class.java
```

После этого Android не ищет подходящее приложение: получатель уже известен.

Explicit intent используем:

- для компонентов внутри своего приложения;
- для `TimerExpiredReceiver`;
- для открытия `MainActivity` из уведомления;
- для запуска собственного `Service`, если он когда-нибудь появится.

### Почему передаётся Context

Конструктор:

```kotlin
Intent(context, MainActivity::class.java)
```

использует `context`, чтобы определить пакет приложения и сформировать полное имя
компонента.

В Activity можно передать `this`:

```kotlin
Intent(this, MainActivity::class.java)
```

Во Fragment сам Fragment не является `Context`, поэтому используется:

```kotlin
Intent(requireContext(), MainActivity::class.java)
```

В `BroadcastReceiver` контекст приходит параметром:

```kotlin
override fun onReceive(context: Context, intent: Intent) {
    // context доступен здесь
}
```

---

## 4. Implicit intent

Implicit intent не называет конкретный класс. Он описывает действие, а Android
ищет установленное приложение, способное его выполнить.

Например, открыть страницу:

```kotlin
val intent = Intent(
    Intent.ACTION_VIEW,
    Uri.parse("https://developer.android.com")
)

startActivity(intent)
```

Мы не говорим «открой Chrome». Мы говорим «покажи этот URL». Браузер выбирает
система или пользователь.

### Поделиться результатом таймера

Позже `HistoryFragment` сможет поделиться записью:

```kotlin
val sendIntent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(
        Intent.EXTRA_TEXT,
        "Я завершил таймер на 25 минут"
    )
}

val chooser = Intent.createChooser(
    sendIntent,
    getString(R.string.share_timer_result)
)

startActivity(chooser)
```

`createChooser()` явно показывает системный выбор приложений и не закрепляет
случайный обработчик без ведома пользователя.

### Если обработчика нет

Не каждый implicit intent гарантированно имеет получателя. Обрабатывайте этот
случай:

```kotlin
try {
    startActivity(intent)
} catch (error: ActivityNotFoundException) {
    // Показать понятное сообщение пользователю.
}
```

---

## 5. Из чего состоит Intent

### Component

Точный компонент-получатель:

```kotlin
Intent(context, MainActivity::class.java)
```

Наличие component обычно делает Intent explicit.

### Action

Короткое описание операции:

```kotlin
Intent.ACTION_VIEW
Intent.ACTION_SEND
```

Для внутренних событий можно создать собственное действие:

```kotlin
const val ACTION_TIMER_EXPIRED =
    "com.example.timer.action.TIMER_EXPIRED"
```

Полное имя пакета уменьшает вероятность конфликта с действиями других приложений.

### Data

Основной объект операции в виде `Uri`:

```kotlin
intent.data = Uri.parse("https://developer.android.com")
```

В нашем таймере уникальный внутренний URI также можно использовать для различения
системных alarm:

```kotlin
intent.data = Uri.parse("timer://expiration/$timerId")
```

Это не веб-адрес. Это стабильная идентичность события для Android.

### Type

MIME-тип данных:

```kotlin
intent.type = "text/plain"
```

Часто используется в `ACTION_SEND`, открытии файлов и выборе контента.

### Category

Дополнительное описание контекста запуска. В повседневном коде категории нужны
реже, но две из них уже есть в Manifest проекта:

```xml
<category android:name="android.intent.category.LAUNCHER" />
```

`LAUNCHER` сообщает, что Activity должна отображаться в списке запускаемых
приложений. Для большинства implicit intent, отправляемых через `startActivity()`,
принимающий intent filter содержит `CATEGORY_DEFAULT`.

### Extras

Небольшие дополнительные параметры в формате ключ-значение:

```kotlin
intent.putExtra(EXTRA_TIMER_ID, timerId)
intent.putExtra(EXTRA_DURATION_MILLIS, durationMillis)
intent.putExtra(EXTRA_IS_FINISHED, true)
```

Внутри Intent extras хранятся в `Bundle`.

### Flags

Инструкции Android о запуске и back stack:

```kotlin
intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
```

Flags могут серьёзно изменить навигацию. В MVP не добавляем их «на всякий случай».
Сначала используем стандартное поведение task/back stack.

---

## 6. Передача extras

Отправитель:

```kotlin
const val EXTRA_TIMER_ID = "com.example.timer.extra.TIMER_ID"
const val EXTRA_DURATION_MILLIS =
    "com.example.timer.extra.DURATION_MILLIS"

val intent = Intent(context, MainActivity::class.java).apply {
    putExtra(EXTRA_TIMER_ID, 42L)
    putExtra(EXTRA_DURATION_MILLIS, 300_000L)
}
```

Получатель:

```kotlin
val timerId = intent.getLongExtra(EXTRA_TIMER_ID, -1L)
val durationMillis = intent.getLongExtra(
    EXTRA_DURATION_MILLIS,
    0L
)
```

Всегда задавайте безопасное значение по умолчанию и валидируйте результат:

```kotlin
if (timerId < 0L || durationMillis <= 0L) {
    // Intent не содержит корректных данных.
    return
}
```

### Какие значения подходят

Extras подходят для небольших данных:

- `Boolean`, `Int`, `Long`, `Double`;
- коротких `String`;
- небольших массивов;
- компактных `Parcelable` при необходимости.

Не передавайте через Intent:

- изображения целиком;
- большие списки истории;
- базы данных или repository;
- View, Activity, Fragment или Context;
- важное состояние, единственная копия которого существует только в extra.

У Binder-транзакций есть ограничение размера. Для больших данных передавайте
идентификатор, а сами данные читайте из repository:

```kotlin
intent.putExtra(EXTRA_TIMER_ID, timerId)
```

Получатель загружает запись по `timerId`.

---

## 7. Intent и навигация между Fragment

В нашей архитектуре:

```text
MainActivity
└── NavHostFragment
    ├── TimerFragment
    ├── SettingsFragment
    └── HistoryFragment
```

Между этими экранами Intent не нужен.

Неправильная идея:

```kotlin
// SettingsFragment не является Activity.
val intent = Intent(requireContext(), SettingsFragment::class.java)
startActivity(intent)
```

Правильно:

```kotlin
findNavController().navigate(
    R.id.action_timerFragment_to_settingsFragment
)
```

Аргументы Fragment передаются через Navigation arguments/Safe Args или через
общее состояние подходящего scope, а не через Activity Intent.

Граница простая:

```text
Переход внутри NavHost       -> NavController
Запуск системного компонента -> Intent
```

---

## 8. Intent filters в Manifest

Стартовая Activity проекта содержит фильтр примерно такого вида:

```xml
<activity
    android:name=".MainActivity"
    android:exported="true">

    <intent-filter>
        <action android:name="android.intent.action.MAIN" />

        <category
            android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

Он означает:

- `MAIN` — основная точка входа;
- `LAUNCHER` — Activity можно запускать с иконки;
- `exported="true"` — компонент доступен системному launcher.

Обычным внутренним компонентам intent filter чаще не нужен:

```xml
<receiver
    android:name=".alarm.TimerExpiredReceiver"
    android:exported="false" />
```

Его можно адресовать explicit intent из нашего приложения, но другие приложения
не получают к нему обычный внешний доступ.

Intent filter — это не код запуска. Это объявление: «компонент способен принимать
Intent с такими action/category/data».

---

## 9. Что такое PendingIntent

Обычный Intent выполняется, когда наш код вызывает, например:

```kotlin
startActivity(intent)
```

Но alarm должен сработать позже, возможно когда процесс приложения уже уничтожен.
Уведомление тоже обрабатывает нажатие позднее. Для этого используется
`PendingIntent`.

`PendingIntent` — защищённый системный токен, который разрешает другому компоненту
или Android выполнить заранее описанный Intent от имени нашего приложения.

```text
Сейчас:
приложение создаёт Intent
  -> оборачивает в PendingIntent
  -> передаёт AlarmManager или NotificationManager

Позже:
Android активирует PendingIntent
  -> доставляет сохранённый Intent
  -> запускает Activity/Receiver/Service
```

Это не просто «Intent с задержкой»: PendingIntent имеет собственную идентичность,
права создавшего приложения и правила обновления.

---

## 10. PendingIntent для окончания таймера

Explicit intent для receiver:

```kotlin
val intent = Intent(
    context,
    TimerExpiredReceiver::class.java
).apply {
    action = ACTION_TIMER_EXPIRED
    data = Uri.parse("timer://expiration/$timerId")
    putExtra(EXTRA_TIMER_ID, timerId)
}
```

Оборачиваем его:

```kotlin
val pendingIntent = PendingIntent.getBroadcast(
    context,
    timerId.toInt(),
    intent,
    PendingIntent.FLAG_UPDATE_CURRENT or
        PendingIntent.FLAG_IMMUTABLE
)
```

Почему `getBroadcast()`:

```text
getActivity()  -> запускает Activity
getBroadcast() -> доставляет Intent в BroadcastReceiver
getService()   -> запускает Service
```

Этот `PendingIntent` передаётся `AlarmManager`. Когда наступает время, Android
вызывает `TimerExpiredReceiver.onReceive()`.

### FLAG_IMMUTABLE

На современных версиях Android необходимо явно указать изменяемость
`PendingIntent`. По умолчанию выбираем:

```kotlin
PendingIntent.FLAG_IMMUTABLE
```

Это запрещает стороне, получившей PendingIntent, менять смысл вложенного Intent.
`FLAG_MUTABLE` нужен только специальным сценариям, например некоторым direct reply.

### FLAG_UPDATE_CURRENT

Если подходящий PendingIntent уже существует, создатель обновляет его extras:

```kotlin
PendingIntent.FLAG_UPDATE_CURRENT
```

`FLAG_IMMUTABLE` не мешает самому создателю обновить PendingIntent через
`FLAG_UPDATE_CURRENT`.

---

## 11. Идентичность PendingIntent

Два PendingIntent могут считаться одним и тем же, даже если extras отличаются.
Extras не являются надёжной частью их идентичности.

Поэтому для отдельных таймеров используем:

- различный `requestCode`; и/или
- уникальный `data` URI; и
- одинаково сформированный Intent при отмене.

Пример:

```kotlin
intent.data = Uri.parse("timer://expiration/$timerId")
```

Для отмены alarm нужно восстановить PendingIntent той же идентичности:

```kotlin
val pendingIntent = PendingIntent.getBroadcast(
    context,
    timerId.toInt(),
    createExpirationIntent(context, timerId),
    PendingIntent.FLAG_NO_CREATE or
        PendingIntent.FLAG_IMMUTABLE
)

if (pendingIntent != null) {
    alarmManager.cancel(pendingIntent)
    pendingIntent.cancel()
}
```

Чтобы создание и отмена не разошлись, формирование Intent выносим в одну функцию.

В первой версии приложения одновременно работает только один таймер, поэтому
можно использовать постоянный `requestCode`. Уникальный `timerId` вводим, когда
появятся несколько сохранённых таймеров или история запусков.

---

## 12. PendingIntent для уведомления

После завершения таймера receiver показывает уведомление. Нажатие должно открыть
`MainActivity`:

```kotlin
val openAppIntent = Intent(
    context,
    MainActivity::class.java
).apply {
    action = ACTION_OPEN_FINISHED_TIMER
    putExtra(EXTRA_TIMER_ID, timerId)
}

val openAppPendingIntent = PendingIntent.getActivity(
    context,
    timerId.toInt(),
    openAppIntent,
    PendingIntent.FLAG_UPDATE_CURRENT or
        PendingIntent.FLAG_IMMUTABLE
)
```

Он передаётся уведомлению:

```kotlin
NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
    .setContentTitle(context.getString(R.string.timer_finished))
    .setContentIntent(openAppPendingIntent)
    .setAutoCancel(true)
```

Путь события:

```text
Пользователь нажимает уведомление
  -> Android активирует PendingIntent
  -> explicit Intent запускает MainActivity
  -> MainActivity/NavHost показывает состояние таймера
```

В MVP уведомление открывает стартовый `TimerFragment`. Позже можно добавить deep
link или обработку action, чтобы открыть конкретную запись `HistoryFragment`.

---

## 13. Intent при уже открытой Activity

Обычно новый Intent приводит к созданию новой Activity, и он доступен через:

```kotlin
intent
```

При специальных launch mode или flags система иногда переиспользует существующий
экземпляр Activity. Тогда новый Intent приходит в:

```kotlin
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIntent(intent)
}
```

В MVP не меняем `launchMode` и не добавляем flags ради оптимизации. Этот раздел
нужен, чтобы позже понимать, почему нажатие на уведомление иногда обрабатывается
не только в `onCreate()`.

Обработку удобно централизовать:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleIntent(intent)
}

override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIntent(intent)
}

private fun handleIntent(intent: Intent?) {
    when (intent?.action) {
        ACTION_OPEN_FINISHED_TIMER -> {
            val timerId = intent.getLongExtra(
                EXTRA_TIMER_ID,
                -1L
            )
            // Проверить timerId и передать событие навигации.
        }
    }
}
```

---

## 14. Получение результата

Старый подход `startActivityForResult()` устарел. Современный код регистрирует
контракт результата заранее.

Пример выбора документа во Fragment:

```kotlin
private val openDocument = registerForActivityResult(
    ActivityResultContracts.OpenDocument()
) { uri: Uri? ->
    if (uri != null) {
        // Работаем с выбранным URI.
    }
}
```

Запуск:

```kotlin
openDocument.launch(arrayOf("audio/*"))
```

Это может пригодиться в `SettingsFragment`, если пользователь сможет выбрать звук
таймера. Контракт внутри использует системное взаимодействие на основе Intent, но
даёт типизированные входные и выходные данные.

---

## 15. Безопасность

### Используйте explicit intent внутри приложения

```kotlin
Intent(context, TimerExpiredReceiver::class.java)
```

Так событие не попадёт случайному компоненту другого приложения.

### Не экспортируйте внутренние компоненты

```xml
android:exported="false"
```

Исключение — `MainActivity` с launcher filter, которую должен запускать системный
launcher.

### Проверяйте входные данные

Даже если Intent считается внутренним:

```kotlin
val timerId = intent.getLongExtra(EXTRA_TIMER_ID, -1L)
if (timerId < 0L) return
```

Если компонент экспортирован, считать входные extras доверенными нельзя.

### Используйте immutable PendingIntent

```kotlin
PendingIntent.FLAG_IMMUTABLE
```

Mutable выбираем только при документированной необходимости.

### Не кладите секреты в Intent

Intent — транспорт сообщения, а не защищённое хранилище. Токены, пароли и другие
секреты не должны передаваться без необходимости.

### Не запускайте собственный Service implicit intent

Для внутреннего Service всегда указывайте конкретный класс.

---

## 16. Intent в этапах нашего курса

### Этап 1: Activity

Изучаем launcher intent, который запускает `MainActivity` из иконки приложения.

### Этап 2: Navigation

Фиксируем, что переходы между `TimerFragment`, `SettingsFragment` и
`HistoryFragment` выполняются без Intent.

### Этап 3: Уведомление

Создаём `PendingIntent.getActivity()`, чтобы нажатие открыло `MainActivity`.

### Этап 4: Фоновое завершение

Создаём `PendingIntent.getBroadcast()`, чтобы `AlarmManager` вызвал
`TimerExpiredReceiver`.

### Этап 5: Настройки

Используем Activity Result API, если добавим выбор пользовательского звука.

### Этап 6: История

Используем implicit `ACTION_SEND`, чтобы поделиться результатом таймера.

---

## 17. Практические задания

### Задание 1. Исследовать launcher intent

В `MainActivity.onCreate()` временно выведите:

```kotlin
Log.d("TimerIntent", "action=${intent.action}")
Log.d("TimerIntent", "data=${intent.data}")
Log.d("TimerIntent", "extras=${intent.extras}")
```

Запустите приложение с иконки и найдите action в Logcat.

Ожидаемое значение обычно связано с `android.intent.action.MAIN`.

### Задание 2. Открыть документацию

Добавьте временную кнопку и запустите `ACTION_VIEW` для страницы Android
Developers. Проверьте поведение, когда доступно несколько браузеров.

### Задание 3. Поделиться текстом

Создайте `ACTION_SEND` и откройте его через `Intent.createChooser()`.

### Задание 4. Передать extra

В учебной `AboutActivity` передайте строку с версией приложения и прочитайте её
через `getStringExtra()`.

### Задание 5. Позже в проекте

После реализации таймера самостоятельно объясните два разных PendingIntent:

```text
Alarm PendingIntent        Notification PendingIntent
getBroadcast()             getActivity()
TimerExpiredReceiver       MainActivity
срабатывает по времени     срабатывает по нажатию
```

---

## 18. Частые ошибки

### Пытаться запустить Fragment через Intent

Fragment открывается через `NavController`, не `startActivity()`.

### Передавать только extras и забывать про идентичность PendingIntent

Разные extras не гарантируют разные PendingIntent. Используйте стабильный
`requestCode` и/или уникальный `data` URI.

### Создавать mutable PendingIntent без причины

Для alarm и обычного открытия уведомления используйте `FLAG_IMMUTABLE`.

### Не регистрировать Activity или Receiver

Системные компоненты приложения должны быть корректно объявлены в Manifest.

### Доверять любым данным экспортированного компонента

Проверяйте action, URI, типы, диапазоны чисел и наличие обязательных данных.

### Использовать Intent как хранилище

Intent доставляет команду и небольшой контекст. Источник данных — ViewModel,
repository, DataStore или база данных в зависимости от срока жизни данных.

---

## 19. Контрольные вопросы

1. Почему Intent называется сообщением, а не прямым вызовом функции?
2. Кто создаёт экземпляр Activity после `startActivity()`?
3. Чем explicit intent отличается от implicit intent?
4. Почему между Fragment нашего приложения Intent не используется?
5. Чем `data` отличается от `extras`?
6. Почему большие данные не передают через extras?
7. Что делает intent filter?
8. Почему у launcher Activity `exported="true"`?
9. Чем Intent отличается от PendingIntent?
10. Почему alarm использует `getBroadcast()`, а уведомление — `getActivity()`?
11. Зачем нужен `FLAG_IMMUTABLE`?
12. Почему разные extras могут не создать разные PendingIntent?
13. Где Activity получает первый Intent?
14. Когда может потребоваться `onNewIntent()`?
15. Какой механизм используется для результата от внешнего приложения?

## Краткая формула

```text
Intent
  = описание действия, которое Android должен доставить сейчас

PendingIntent
  = право выполнить заранее описанный Intent позже

NavController
  = переход между Fragment внутри MainActivity
```

Для нашего таймера основное правило:

```text
Внутри интерфейса  -> Navigation Component
На границе с ОС    -> Intent / PendingIntent
```
