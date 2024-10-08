package com.ankurkushwaha.chaos.presentation.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.ankurkushwaha.chaos.databinding.SleepTimerBottomSheetBinding
import com.ankurkushwaha.chaos.utils.showToast
import com.ankurkushwaha.chaos.worker.SleepTimerWorker
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class SleepTimerBottomSheet : BottomSheetDialogFragment() {
    private var _binding: SleepTimerBottomSheetBinding? = null
    private val binding: SleepTimerBottomSheetBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SleepTimerBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPicker()
        // Check if there's an existing worker and update the switch
        checkIfWorkerIsRunning()
        binding.sleepTimerSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                // Cancel the worker when the switch is unchecked
                cancelExistingWorker()
                showToast(requireContext(), "Sleep Timer cancelled")
            }
        }
    }

    private fun initPicker() {
        binding.timePicker.apply {
            // Set minute step interval
            setStepSizeMinutes(5)
            // Configure visibility for date/time components
            setDisplayDays(false)
            setDisplayMonthNumbers(false)
            setDisplayHours(false)
            setDisplayMinutes(true)
            setDisplayYears(false)

            binding.btnConfirm.setOnClickListener {
                val selectedMinutes = date.minutes
                if (binding.sleepTimerSwitch.isChecked) {
                    if (selectedMinutes > 0) {
                        // Start the sleep timer
                        cancelExistingWorker() // Cancel the previous worker before starting a new one
                        initSleepTimerWorker(selectedMinutes.toLong())
                        dismiss()
                    } else {
                        showToast(requireContext(), "Please set a valid time!")
                    }
                } else {
                    showToast(requireContext(), "Please enable the Sleep Timer")
                }
            }
        }
    }

    // Check if there's any existing sleep timer worker running and update the switch state
    private fun checkIfWorkerIsRunning() {
        val workManager = WorkManager.getInstance(requireContext())

        // Use the tag to find work requests related to SleepTimerWorker
        workManager.getWorkInfosByTagLiveData("SleepTimerWorkerTag")
            .observe(viewLifecycleOwner) { workInfoList ->
                if (workInfoList != null && workInfoList.isNotEmpty()) {
                    val isRunning = workInfoList.any { workInfo ->
                        workInfo.state == WorkInfo.State.RUNNING || workInfo.state == WorkInfo.State.ENQUEUED
                    }
                    // Update the switch if the worker is running
                    binding.sleepTimerSwitch.isChecked = isRunning
                }
            }
    }

    // Cancel any previously running SleepTimerWorker
    private fun cancelExistingWorker() {
        val workManager = WorkManager.getInstance(requireContext())

        // Cancel any work with the tag "SleepTimerWorkerTag"
        workManager.cancelAllWorkByTag("SleepTimerWorkerTag")
    }

    // Initialize the Sleep Timer Worker
    private fun initSleepTimerWorker(minutes: Long) {
        val stopMusicRequest = OneTimeWorkRequestBuilder<SleepTimerWorker>()
            .setInitialDelay(minutes, TimeUnit.MINUTES) // Sleep timer duration
            .addTag("SleepTimerWorkerTag") // Add a tag to manage this worker
            .build()

        // Enqueue the work
        WorkManager.getInstance(requireContext()).enqueue(stopMusicRequest)

        showToast(requireContext(), "Sleep Timer set for $minutes minutes")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}