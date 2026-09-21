package com.example.timer.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.timer.TimerApplication
import com.example.timer.databinding.FragmentHistoryBinding
import com.example.timer.history.data.TimerHistoryEntity
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModel.Factory(
            (requireActivity().application as TimerApplication)
                .timerHistoryRepository
        )
    }

    private var _binding: FragmentHistoryBinding? = null
    private lateinit var historyAdapter: TimerHistoryAdapter

    private val binding: FragmentHistoryBinding
        get() = requireNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        historyAdapter = TimerHistoryAdapter(viewModel::delete)

        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }

        collectHistory()

        binding.clearHistoryButton.setOnClickListener {
            viewModel.clearHistory()
        }
    }

    private fun collectHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.history.collect(::render)
            }
        }
    }

    private fun render(entries: List<TimerHistoryEntity>) {
        historyAdapter.submitList(entries)

        val isEmpty = entries.isEmpty()
        binding.historyEmptyText.isVisible = isEmpty
        binding.historyRecyclerView.isVisible = !isEmpty
        binding.clearHistoryButton.isEnabled = !isEmpty
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
