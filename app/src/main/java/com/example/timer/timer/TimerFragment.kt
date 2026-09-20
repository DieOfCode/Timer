package com.example.timer.timer

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.timer.R
import com.example.timer.alarm.TimerAlarmScheduler
import com.example.timer.databinding.FragmentTimerBinding
import androidx.navigation.fragment.findNavController
import com.example.timer.create.TimerCreateResultContract
import com.example.timer.model.TimerSound
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TimerFragment : Fragment() {
    private val viewModel: TimerViewModel by viewModels()

    private var _binding: FragmentTimerBinding? = null

    private val binding: FragmentTimerBinding
        get() = requireNotNull(_binding)

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                context?.let { currentContext ->
                    Toast.makeText(
                        currentContext,
                        R.string.notification_permission_denied,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            requestExactAlarmAccessIfNeeded()
        }

    private val exactAlarmAccessLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            scheduleAlarmForCurrentState()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TimerFragment", "onCreate")
    }
    private fun formatTime(milliseconds: Long): String {
        val safeMilliseconds = milliseconds.coerceAtLeast(0L)
        val totalSeconds = (safeMilliseconds + 999L) / 1_000L

        val hours = totalSeconds / 3_600L
        val minutes = (totalSeconds % 3_600L) / 60L
        val seconds = totalSeconds % 60L

        return "%02d:%02d:%02d".format(
            hours,
            minutes,
            seconds
        )
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)

        return binding.root
    }
    private fun render(state: TimerUiState) {
        binding.timerText.text = formatTime(state.remainingMillis)

        binding.startButton.isEnabled = state.status == TimerStatus.READY
        binding.resetButton.isEnabled = state.status != TimerStatus.READY

        binding.pauseButton.isEnabled =
            state.status == TimerStatus.RUNNING ||
                    state.status == TimerStatus.PAUSED

        binding.pauseButton.setText(
            if (state.status == TimerStatus.PAUSED) {
                R.string.resume
            } else {
                R.string.pause
            }
        )
    }

    private suspend fun collectTimerState() {
        viewModel.uiState.collect { state ->
            render(state)
        }
    }

    private suspend fun collectAlarmState() {
        viewModel.uiState
            .map { state ->
                AlarmState(
                    status = state.status,
                    finishTimeElapsedRealtime =
                        state.finishTimeElapsedRealtime,
                    sound = state.sound
                )
            }
            .distinctUntilChanged()
            .collect { alarmState ->
                syncAlarm(alarmState)
            }
    }

    private suspend fun collectTimerCreationResult() {
        val savedStateHandle = findNavController()
            .getBackStackEntry(R.id.timerFragment)
            .savedStateHandle

        savedStateHandle
            .getStateFlow<Bundle?>(
                TimerCreateResultContract.RESULT_KEY,
                null
            )
            .filterNotNull()
            .collect { resultBundle ->
                val durationMillis = resultBundle.getLong(
                    TimerCreateResultContract.DURATION_MILLIS_KEY
                )

                val soundName = resultBundle.getString(
                    TimerCreateResultContract.SOUND_KEY
                )

                val sound = soundName
                    ?.let { savedName ->
                        runCatching {
                            TimerSound.valueOf(savedName)
                        }.getOrNull()
                    }
                    ?: TimerSound.BELL

                viewModel.configureTimer(
                    durationMillis = durationMillis,
                    sound = sound
                )

                savedStateHandle.remove<Bundle>(
                    TimerCreateResultContract.RESULT_KEY
                )
            }
    }

    private fun collectState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                launch { collectTimerState() }
                launch { collectAlarmState() }
                launch { collectTimerCreationResult() }
            }

        }
    }




    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        collectState()

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

        binding.createTimerButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_timerFragment_to_createTimerFlow
            )
        }

        binding.startButton.setOnClickListener {
            viewModel.start()
            requestTimerPermissionsIfNeeded()
        }

        binding.pauseButton.setOnClickListener {
            viewModel.togglePause()
        }

        binding.resetButton.setOnClickListener {
            viewModel.reset()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun requestTimerPermissionsIfNeeded() {
        val needsNotificationPermission =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED

        if (needsNotificationPermission) {
            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            requestExactAlarmAccessIfNeeded()
        }
    }

    private fun requestExactAlarmAccessIfNeeded() {
        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            TimerAlarmScheduler.canScheduleExact(requireContext())
        ) {
            scheduleAlarmForCurrentState()
            return
        }

        Toast.makeText(
            requireContext(),
            R.string.exact_alarm_permission_explanation,
            Toast.LENGTH_LONG
        ).show()

        val intent = Intent(
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
            Uri.parse("package:${requireContext().packageName}")
        )

        try {
            exactAlarmAccessLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                R.string.exact_alarm_unavailable,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun syncAlarm(state: AlarmState) {
        val finishTime = state.finishTimeElapsedRealtime

        when (state.status) {
            TimerStatus.RUNNING -> {
                if (finishTime != null) {
                    TimerAlarmScheduler.schedule(
                        context = requireContext(),
                        triggerAtElapsedRealtime = finishTime,
                        sound = state.sound
                    )
                }
            }

            TimerStatus.READY,
            TimerStatus.PAUSED -> {
                TimerAlarmScheduler.cancel(requireContext())
            }

            TimerStatus.FINISHED -> Unit
        }
    }

    private fun scheduleAlarmForCurrentState() {
        val state = viewModel.uiState.value

        syncAlarm(
            AlarmState(
                status = state.status,
                finishTimeElapsedRealtime =
                    state.finishTimeElapsedRealtime,
                sound = state.sound
            )
        )
    }

    private data class AlarmState(
        val status: TimerStatus,
        val finishTimeElapsedRealtime: Long?,
        val sound: TimerSound
    )
}
