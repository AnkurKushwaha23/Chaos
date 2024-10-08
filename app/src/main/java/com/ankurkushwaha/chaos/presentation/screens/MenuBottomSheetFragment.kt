package com.ankurkushwaha.chaos.presentation.screens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ankurkushwaha.chaos.databinding.MenuBottomsheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MenuBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: MenuBottomsheetBinding? = null
    private val binding: MenuBottomsheetBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MenuBottomsheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)

        binding.apply {

            reportBug.setOnClickListener {
                getMail("Chaos : Bug Report")
            }

            suggestions.setOnClickListener {
                getMail("Chaos : Suggestions")
            }

            timer.setOnClickListener {
                val bottomSheet = SleepTimerBottomSheet()
                bottomSheet.show(childFragmentManager, "CustomBottomSheet")
            }

            aboutDeveloper.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_LINK))
                startActivity(intent)
                Toast.makeText(requireContext(), "Thanks for Contacting Us !!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun getMail(subject: String) {
        val uriBuilder = StringBuilder("mailto:" + Uri.encode(EMAIL))
        uriBuilder.append("?subject=" + Uri.encode(subject))
        val uriString = uriBuilder.toString()

        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(uriString))
        startActivity(Intent.createChooser(intent, "Send Suggestions"))
        Toast.makeText(requireContext(), "Thanks for Contacting Us !!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private const val EMAIL = "ankursenpai@gmail.com"
        private const val GITHUB_LINK = "https://github.com/AnkurKushwaha23/"
    }
}