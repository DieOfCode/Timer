package com.example.timer.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.timer.R
import com.example.timer.databinding.ItemTimerHistoryBinding
import com.example.timer.history.data.TimerHistoryEntity
import com.example.timer.model.TimerSound
import java.text.DateFormat
import java.util.Date

class TimerHistoryAdapter(
    private val onDeleteClick: (TimerHistoryEntity) -> Unit
) : ListAdapter<TimerHistoryEntity, TimerHistoryAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemTimerHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemTimerHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: TimerHistoryEntity) {
            val context = binding.root.context

            binding.historyItemTitle.text = context.getString(
                R.string.history_item_summary,
                formatDuration(entry.durationMillis),
                soundLabel(entry.soundName)
            )

            binding.historyItemDate.text = DateFormat
                .getDateTimeInstance(
                    DateFormat.MEDIUM,
                    DateFormat.SHORT
                )
                .format(Date(entry.finishedAtEpochMillis))

            binding.deleteHistoryItemButton.setOnClickListener {
                onDeleteClick(entry)
            }
        }

        private fun formatDuration(durationMillis: Long): String {
            val minutes = (durationMillis / 60_000L)
                .coerceAtLeast(1L)

            return binding.root.context.getString(
                R.string.history_duration_minutes,
                minutes
            )
        }

        private fun soundLabel(soundName: String): String {
            val sound = runCatching {
                TimerSound.valueOf(soundName)
            }.getOrDefault(TimerSound.BELL)

            val label = when (sound) {
                TimerSound.BELL -> R.string.sound_bell
                TimerSound.SILENT -> R.string.sound_silent
            }

            return binding.root.context.getString(label)
        }
    }

    private companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TimerHistoryEntity>() {
            override fun areItemsTheSame(
                oldItem: TimerHistoryEntity,
                newItem: TimerHistoryEntity
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: TimerHistoryEntity,
                newItem: TimerHistoryEntity
            ): Boolean = oldItem == newItem
        }
    }
}
