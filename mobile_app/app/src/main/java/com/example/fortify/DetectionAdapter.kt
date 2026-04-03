package com.example.fortify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// A data class to represent a single detection from the JSON report
data class DetectionItem(
    val file: String,
    val line: Int,
    val label: String,
    val match: String
)

class DetectionAdapter(private val detections: List<DetectionItem>) :
    RecyclerView.Adapter<DetectionAdapter.DetectionViewHolder>() {

    // This class holds the UI views for a single row (list_item_detection.xml)
    class DetectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val detectionLabelTextView: TextView = view.findViewById(R.id.detectionLabelTextView)
        val filePathTextView: TextView = view.findViewById(R.id.filePathTextView)
        val matchTextView: TextView = view.findViewById(R.id.matchTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetectionViewHolder {
        // Inflate the XML layout for a single row
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_detection, parent, false)
        return DetectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetectionViewHolder, position: Int) {
        // Get the data for the current row
        val detection = detections[position]

        // Bind the data to the UI views
        holder.detectionLabelTextView.text = detection.label
        // We can format the file path to be a bit cleaner
        holder.filePathTextView.text = "in ${detection.file.substringAfter("sources/")}"
        holder.matchTextView.text = detection.match
    }

    override fun getItemCount() = detections.size
}
