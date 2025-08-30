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
    var jobId: String, // Changed to var to allow updating
    var status: String, // "Pending", "Scanning", "Clean", "Malicious(...)"
    var resultColor: Int
)

class ScanJobAdapter(private val scanJobs: MutableList<ScanJob>) :
    RecyclerView.Adapter<ScanJobAdapter.ScanJobViewHolder>() {

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
    }

    override fun getItemCount() = scanJobs.size

    fun addJob(job: ScanJob) {
        scanJobs.add(0, job) // Add new jobs to the top of the list
        notifyItemInserted(0)
    }

    // --- THIS IS THE MISSING FUNCTION THAT HAS BEEN ADDED ---
    fun updateJobId(oldJobId: String, newJobId: String) {
        val index = scanJobs.indexOfFirst { it.jobId == oldJobId }
        if (index != -1) {
            scanJobs[index].jobId = newJobId
            // We don't need to call notifyItemChanged here, as the ID is not visible in the UI
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

