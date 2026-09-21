# Fragment — часть 2: lifecycle View и View Binding

## Результат

Вы разберёте два lifecycle и создадите binding, который не удерживает
уничтоженную View.

## Теория

```text
onCreate()             Fragment существует
onCreateView()         View создаётся
onViewCreated()        View готова
onStart()/onResume()   View видна и активна
onPause()/onStop()
onDestroyView()        View уничтожена
onDestroy()            Fragment уничтожен
```

Fragment может остаться в back stack после `onDestroyView()`. Поэтому binding
существует только между `onCreateView()` и `onDestroyView()`.

Для UI-наблюдений используется `viewLifecycleOwner`, а не lifecycle Fragment.

## Практика

1. Замените `Fragment(R.layout.fragment_timer)` на `Fragment()`.
2. Создайте `_binding` в `onCreateView()`.
3. Передайте `container, false`: FragmentManager прикрепит View сам.
4. Проверьте binding на существующем `fragmentTitle` в `onViewCreated()`.
5. Обнулите `_binding` в `onDestroyView()`.

```kotlin
class TimerFragment : Fragment() {
    private var _binding: FragmentTimerBinding? = null
    private val binding get() = requireNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fragmentTitle.text =
            getString(R.string.timer_fragment_binding_ready)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

Добавьте в `strings.xml`:

```xml
<string name="timer_fragment_binding_ready">Binding подключён</string>
```

На этом шаге `FragmentTimerBinding` содержит `fragmentTitle`, потому что только
такой `id` сейчас объявлен в `fragment_timer.xml`. Свойства `startButton` и
`timerText` появятся автоматически после добавления соответствующих View в XML на
следующем этапе.

## Контрольные вопросы

1. Почему `_binding` nullable?
2. Почему listeners создаются в `onViewCreated()`?
3. Почему UI-наблюдения используют `viewLifecycleOwner`?

Далее: [часть 3](03-navigation.md).
