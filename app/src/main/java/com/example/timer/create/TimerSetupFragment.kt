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
import com.example.timer.databinding.FragmentTimerSetupBinding
import kotlinx.coroutines.launch

class TimerSetupFragment : Fragment() {

    private var _binding: FragmentTimerSetupBinding? = null
    private val binding: FragmentTimerSetupBinding
        get() = requireNotNull(_binding)

    private var hasRenderedInitialDuration = false

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
        _binding = FragmentTimerSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nextButton.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.nextButton.isEnabled = state.isInitialized

                    if (
                        state.isInitialized &&
                        !hasRenderedInitialDuration
                    ) {
                        binding.durationInput.setText(
                            state.durationMinutes.toString()
                        )
                        hasRenderedInitialDuration = true
                    }
                }
            }
        }

        binding.nextButton.setOnClickListener {
            val minutes = binding.durationInput.text
                .toString()
                .toIntOrNull()

            if (minutes == null || minutes !in 1..1_440) {
                binding.durationInput.error = getString(
                    R.string.duration_validation_error
                )
                return@setOnClickListener
            }

            viewModel.setDurationMinutes(minutes)
            findNavController().navigate(
                R.id.action_timerSetupFragment_to_soundSelectionFragment
            )
        }
    }

    override fun onDestroyView() {
        hasRenderedInitialDuration = false
        _binding = null
        super.onDestroyView()
    }
}
