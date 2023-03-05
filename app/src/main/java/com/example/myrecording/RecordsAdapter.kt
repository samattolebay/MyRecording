package com.example.myrecording

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class RecordsAdapter(private val onPlayClick: (Record) -> Unit) :
    ListAdapter<Record, RecordsAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.nameText)
        private val date: TextView = view.findViewById(R.id.dateText)
        private val time: TextView = view.findViewById(R.id.timeText)
        private val play: ImageButton = view.findViewById(R.id.playButton)
        private val progress: SeekBar = view.findViewById(R.id.seekBar)


        fun bind(record: Record, onPlayClick: (Record) -> Unit) {
            name.text = record.name
            date.text = record.date
            var seconds = (record.duration / 1000).toInt()
            val minutes = seconds / 60
            seconds %= 60
            time.text = String.format("%d:%d", minutes, seconds)
            play.setOnClickListener {
                onPlayClick(record)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onPlayClick)
    }
}

object DiffCallback : ItemCallback<Record>() {
    override fun areItemsTheSame(oldItem: Record, newItem: Record): Boolean =
        oldItem.name == newItem.name

    override fun areContentsTheSame(oldItem: Record, newItem: Record): Boolean = oldItem == newItem

}