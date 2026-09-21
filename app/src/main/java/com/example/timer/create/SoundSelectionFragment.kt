package com.example.timer.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.timer.R
import com.example.timer.TimerApplication
import com.example.timer.databinding.FragmentSoundSelectionBinding
import com.example.timer.model.TimerSound

class SoundSelectionFragment : Fragment() {

    private var _binding: FragmentSoundSelectionBinding? = null
    private val binding: FragmentSoundSelectionBinding
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
        _binding = FragmentSoundSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nextButton.setOnClickListener {
            val selectedSound = when (binding.soundGroup.checkedRadioButtonId) {
                R.id.soundSilentButton -> TimerSound.SILENT
                else -> TimerSound.BELL
            }

            viewModel.selectSound(selectedSound)
            findNavController().navigate(
                R.id.action_soundSelectionFragment_to_timerConfirmationFragment
            )
        }
        when (viewModel.uiState.value.sound) {
            TimerSound.BELL -> binding.soundBellButton.isChecked = true
            TimerSound.SILENT -> binding.soundSilentButton.isChecked = true
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
