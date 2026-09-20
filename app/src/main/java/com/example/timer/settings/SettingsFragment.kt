package com.example.timer.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.timer.R
import com.example.timer.TimerApplication
import com.example.timer.databinding.FragmentSettingsBinding
import com.example.timer.model.TimerSound
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory(
            (requireActivity().application as TimerApplication)
                .timerSettingsRepository
        )
    }

    private var _binding: FragmentSettingsBinding? = null
    private var hasRenderedInitialSettings = false

    private val binding: FragmentSettingsBinding
        get() = requireNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collectState()

        binding.saveButton.setOnClickListener {
            val durationMinutes = binding.defaultDurationInput.text
                .toString()
                .toIntOrNull()

            if (durationMinutes == null || durationMinutes !in 1..1_440) {
                binding.defaultDurationInput.error = getString(
                    R.string.duration_validation_error
                )
                return@setOnClickListener
            }

            val sound = when (
                binding.defaultSoundGroup.checkedRadioButtonId
            ) {
                R.id.defaultSoundSilentButton -> TimerSound.SILENT
                else -> TimerSound.BELL
            }

            viewModel.saveSettings(
                durationMinutes = durationMinutes,
                sound = sound
            )
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun collectState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect(::render)
                }

                launch {
                    viewModel.events.collect(::handleEvent)
                }
            }
        }
    }

    private fun render(state: SettingsUiState) {
        binding.saveButton.isEnabled =
            !state.isLoading && !state.isSaving

        if (!state.isLoading && !hasRenderedInitialSettings) {
            binding.defaultDurationInput.setText(
                state.defaultDurationMinutes.toString()
            )

            when (state.defaultSound) {
                TimerSound.BELL -> {
                    binding.defaultSoundBellButton.isChecked = true
                }

                TimerSound.SILENT -> {
                    binding.defaultSoundSilentButton.isChecked = true
                }
            }

            hasRenderedInitialSettings = true
        }
    }

    private fun handleEvent(event: SettingsEvent) {
        val message = when (event) {
            SettingsEvent.Saved -> R.string.settings_saved
            SettingsEvent.SaveFailed -> R.string.settings_save_failed
        }

        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        hasRenderedInitialSettings = false
        _binding = null
        super.onDestroyView()
    }
}
