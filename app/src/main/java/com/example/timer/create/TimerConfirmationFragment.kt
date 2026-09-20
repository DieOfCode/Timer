package com.example.timer.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.timer.R
import com.example.timer.TimerApplication
import com.example.timer.databinding.FragmentTimerConfirmationBinding
import com.example.timer.model.TimerSound
import kotlinx.coroutines.launch

class TimerConfirmationFragment : Fragment() {

    private var _binding: FragmentTimerConfirmationBinding? = null
    private val binding: FragmentTimerConfirmationBinding
        get() = requireNotNull(_binding)
    private val viewModel: CreateTimerFlowViewModel
            by navGraphViewModels(R.id.createTimerFlow) {
                CreateTimerFlowViewModel.Factory(
                    (requireActivity().application as TimerApplication)
                        .timerSettingsRepository
                )
            }

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
            val state = viewModel.uiState.value

            val resultBundle = Bundle().apply {
                putLong(
                    TimerCreateResultContract.DURATION_MILLIS_KEY,
                    state.durationMinutes * 60_000L
                )

                putString(
                    TimerCreateResultContract.SOUND_KEY,
                    state.sound.name
                )
            }

            val navController = findNavController()

            val timerEntry = navController.getBackStackEntry(
                R.id.timerFragment
            )

            timerEntry.savedStateHandle[
                TimerCreateResultContract.RESULT_KEY
            ] = resultBundle

            navController.navigate(
                R.id.action_timerConfirmationFragment_to_timerFragment
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.summaryText.text = buildString {
                        append("Длительность: ")
                        append(state.durationMinutes)
                        append(" мин.\nСигнал: ")
                        append(
                            when (state.sound) {
                                TimerSound.BELL -> "Звонок"
                                TimerSound.SILENT -> "Без звука"
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
