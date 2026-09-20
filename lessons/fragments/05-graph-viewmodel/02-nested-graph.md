# Шаг 5.2: создаём nested graph и три экрана

На этом шаге создадим только переходы. Общие данные подключим в шаге 5.3.

## Результат шага

```text
TimerFragment
    -> TimerSetupFragment
    -> SoundSelectionFragment
    -> TimerConfirmationFragment
    -> TimerFragment
```

Три новых Fragment будут находиться внутри nested graph `createTimerFlow`.

## 1. Добавьте строки

В `res/values/strings.xml`:

```xml
<string name="create_timer">Создать таймер</string>
<string name="timer_setup_title">Длительность таймера</string>
<string name="duration_minutes_hint">Минуты</string>
<string name="next">Далее</string>
<string name="sound_selection_title">Выберите сигнал</string>
<string name="sound_bell">Звонок</string>
<string name="sound_silent">Без звука</string>
<string name="confirmation_title">Проверьте таймер</string>
<string name="finish_creation">Готово</string>
```

## 2. Добавьте кнопку входа в flow

В `fragment_timer.xml` поместите кнопку между `resetButton` и
`openSettingsButton`:

```xml
<Button
    android:id="@+id/createTimerButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginTop="24dp"
    android:text="@string/create_timer"
    app:layout_constraintBottom_toTopOf="@id/openSettingsButton"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toBottomOf="@id/resetButton" />
```

У `resetButton` замените нижнюю привязку:

```xml
app:layout_constraintBottom_toTopOf="@id/createTimerButton"
```

У `openSettingsButton` замените верхнюю привязку:

```xml
app:layout_constraintTop_toBottomOf="@id/createTimerButton"
```

Так цепочка ConstraintLayout останется непрерывной.

## 3. Создайте layouts шагов

Создайте `res/layout/fragment_timer_setup.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:orientation="vertical"
    android:padding="24dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/timer_setup_title"
        android:textSize="24sp" />

    <EditText
        android:id="@+id/durationInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:hint="@string/duration_minutes_hint"
        android:inputType="number" />

    <Button
        android:id="@+id/nextButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="@string/next" />

</LinearLayout>
```

Создайте `res/layout/fragment_sound_selection.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:orientation="vertical"
    android:padding="24dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/sound_selection_title"
        android:textSize="24sp" />

    <RadioGroup
        android:id="@+id/soundGroup"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp">

        <RadioButton
            android:id="@+id/soundBellButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:checked="true"
            android:text="@string/sound_bell" />

        <RadioButton
            android:id="@+id/soundSilentButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/sound_silent" />
    </RadioGroup>

    <Button
        android:id="@+id/nextButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="@string/next" />

</LinearLayout>
```

Создайте `res/layout/fragment_timer_confirmation.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:orientation="vertical"
    android:padding="24dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/confirmation_title"
        android:textSize="24sp" />

    <TextView
        android:id="@+id/summaryText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp" />

    <Button
        android:id="@+id/finishButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="@string/finish_creation" />

</LinearLayout>
```

После сборки View Binding сгенерирует:

```text
FragmentTimerSetupBinding
FragmentSoundSelectionBinding
FragmentTimerConfirmationBinding
```

## 4. Создайте три Fragment

Создайте пакет `com.example.timer.create`.

`TimerSetupFragment.kt`:

```kotlin
package com.example.timer.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.timer.R
import com.example.timer.databinding.FragmentTimerSetupBinding

class TimerSetupFragment : Fragment() {

    private var _binding: FragmentTimerSetupBinding? = null
    private val binding get() = requireNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimerSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nextButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_timerSetupFragment_to_soundSelectionFragment
            )
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
```

`SoundSelectionFragment.kt`:

```kotlin
package com.example.timer.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.timer.R
import com.example.timer.databinding.FragmentSoundSelectionBinding

class SoundSelectionFragment : Fragment() {

    private var _binding: FragmentSoundSelectionBinding? = null
    private val binding get() = requireNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSoundSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nextButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_soundSelectionFragment_to_timerConfirmationFragment
            )
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
```

`TimerConfirmationFragment.kt`:

```kotlin
package com.example.timer.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.timer.R
import com.example.timer.databinding.FragmentTimerConfirmationBinding

class TimerConfirmationFragment : Fragment() {

    private var _binding: FragmentTimerConfirmationBinding? = null
    private val binding get() = requireNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimerConfirmationBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.finishButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_timerConfirmationFragment_to_timerFragment
            )
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
```

Не вызывайте `requireActivity().onBackPressed()` для завершения flow: нам нужно
явно удалить весь nested graph из back stack.

## 5. Добавьте nested graph

Внутрь корневого элемента `<navigation>` файла `nav_graph.xml`, после обычных
destination, добавьте:

```xml
<navigation
    android:id="@+id/createTimerFlow"
    app:startDestination="@id/timerSetupFragment">

    <fragment
        android:id="@+id/timerSetupFragment"
        android:name="com.example.timer.create.TimerSetupFragment"
        android:label="@string/timer_setup_title"
        tools:layout="@layout/fragment_timer_setup">

        <action
            android:id="@+id/action_timerSetupFragment_to_soundSelectionFragment"
            app:destination="@id/soundSelectionFragment" />
    </fragment>

    <fragment
        android:id="@+id/soundSelectionFragment"
        android:name="com.example.timer.create.SoundSelectionFragment"
        android:label="@string/sound_selection_title"
        tools:layout="@layout/fragment_sound_selection">

        <action
            android:id="@+id/action_soundSelectionFragment_to_timerConfirmationFragment"
            app:destination="@id/timerConfirmationFragment" />
    </fragment>

    <fragment
        android:id="@+id/timerConfirmationFragment"
        android:name="com.example.timer.create.TimerConfirmationFragment"
        android:label="@string/confirmation_title"
        tools:layout="@layout/fragment_timer_confirmation">

        <action
            android:id="@+id/action_timerConfirmationFragment_to_timerFragment"
            app:destination="@id/timerFragment"
            app:launchSingleTop="true"
            app:popUpTo="@id/createTimerFlow"
            app:popUpToInclusive="true" />
    </fragment>
</navigation>
```

Внутри `timerFragment` добавьте action, destination которого — ID всего nested
graph, а не его первого Fragment:

```xml
<action
    android:id="@+id/action_timerFragment_to_createTimerFlow"
    app:destination="@id/createTimerFlow" />
```

Navigation сам откроет `startDestination` этого graph — `timerSetupFragment`.
`launchSingleTop` не даст Navigation добавить второй `TimerFragment`, когда
исходный экземпляр снова окажется верхним после очистки flow.

## 6. Подключите кнопку входа

В `TimerFragment.onViewCreated()`:

```kotlin
binding.createTimerButton.setOnClickListener {
    findNavController().navigate(
        R.id.action_timerFragment_to_createTimerFlow
    )
}
```

## Проверка шага

1. Соберите и запустите приложение.
2. Нажмите «Создать таймер» — откроется ввод длительности.
3. Нажмите «Далее» — откроется выбор сигнала.
4. Ещё раз нажмите «Далее» — откроется подтверждение.
5. Нажмите «Готово» — вернётесь на `TimerFragment`.
6. Нажимайте системную кнопку Back на каждом шаге и проверьте обратный порядок.

Пока введённые данные не сохраняются — это ожидаемо. На этом шаге мы проверяем
только структуру flow и переходы.

## Контрольные вопросы

1. Почему action входа указывает на `createTimerFlow`, а не прямо на setup?
2. Какой destination открывается при входе в nested graph?
3. Зачем при завершении нужны `popUpTo` и `popUpToInclusive="true"`?

Далее: [шаг 5.3](03-shared-viewmodel.md).
