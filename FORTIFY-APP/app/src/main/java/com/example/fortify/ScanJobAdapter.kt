package com.example.fortify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

// Data class to hold all info about a scan job
data class ScanJob(
    val fileName: String,
    var jobId: String,
    var status: String,
    var resultColor: Int
)

// --- THIS IS THE MAIN CHANGE ---
// We've added an onItemClick lambda function to the constructor.
// This is how the adapter will communicate clicks back to the activity.
class ScanJobAdapter(
    private val scanJobs: MutableList<ScanJob>,
    private val onItemClick: (ScanJob) -> Unit
) : RecyclerView.Adapter<ScanJobAdapter.ScanJobViewHolder>() {

    class ScanJobViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileNameTextView: TextView = view.findViewById(R.id.fileNameTextView)
        val statusTextView: TextView = view.findViewById(R.id.statusTextView)
        val itemLayout: ConstraintLayout = view.findViewById(R.id.itemLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanJobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scan_job, parent, false)
        return ScanJobViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScanJobViewHolder, position: Int) {
        val job = scanJobs[position]
        holder.fileNameTextView.text = job.fileName
        holder.statusTextView.text = job.status
        holder.itemLayout.setBackgroundColor(
            ContextCompat.getColor(holder.itemView.context, job.resultColor)
        )

        // --- THIS IS THE NEW CLICK LOGIC ---
        // Set a click listener on the entire row view
        holder.itemView.setOnClickListener {
            // Only allow clicks if the scan is done
            if (job.status != "Uploading..." && job.status != "Scanning...") {
                onItemClick(job)
            }
        }
    }

    override fun getItemCount() = scanJobs.size

    fun addJob(job: ScanJob) {
        scanJobs.add(0, job)
        notifyItemInserted(0)
    }

    fun updateJobId(oldJobId: String, newJobId: String) {
        val index = scanJobs.indexOfFirst { it.jobId == oldJobId }
        if (index != -1) {
            scanJobs[index].jobId = newJobId
        }
    }

    fun updateJobStatus(jobId: String, newStatus: String, newColor: Int) {
        val index = scanJobs.indexOfFirst { it.jobId == jobId }
        if (index != -1) {
            scanJobs[index].status = newStatus
            scanJobs[index].resultColor = newColor
            notifyItemChanged(index)
        }
    }
}

