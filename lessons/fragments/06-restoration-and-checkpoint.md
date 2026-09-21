# Fragment — часть 6: восстановление и итоговая проверка

## Результат

Приложение переживает поворот и возврат без дубликатов Fragment и без обращения к
уничтоженному binding.

## Теория

FragmentManager и NavController восстанавливают навигационное состояние. Не
создавайте стартовый Fragment вручную при каждом `onCreate()`.

ViewModel переживает configuration change, но не заменяет постоянное хранилище.
Небольшой UI state восстанавливается через `SavedStateHandle`, а история и
настройки — через соответствующий data layer.

```text
onCreateView() -> новая View
onViewCreated() -> подписка на uiState
render(state) -> экран восстановлен
```

## Практика

Проверьте:

- первый запуск;
- поворот на каждом экране;
- `Timer -> Settings -> Back`;
- `Timer -> History -> Back`;
- сворачивание и возвращение;
- увеличенный системный шрифт;
- `Don't keep activities` как учебный стресс-тест.

## Частые ошибки

- `lateinit` binding без очистки;
- binding в `onCreate()` или после `onDestroyView()`;
- UI-наблюдение без `viewLifecycleOwner`;
- обязательные параметры конструктора Fragment;
- глобальные ссылки на Fragment;
- ручные транзакции внутри NavHost;
- состояние таймера в `TextView`;
- повторное добавление Fragment после поворота.

## Финальная проверка

Объясните разницу Activity, Fragment и View; роль FragmentManager и NavHost;
два lifecycle; срок жизни binding; три ViewModel scopes; back stack и
восстановление после поворота.

После этого можно переходить к логике обратного отсчёта.
