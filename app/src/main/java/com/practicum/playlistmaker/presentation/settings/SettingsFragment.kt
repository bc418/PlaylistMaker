package com.practicum.playlistmaker.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Switch
import androidx.fragment.app.Fragment
import com.practicum.playlistmaker.App
import com.practicum.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private val viewModel by viewModel<SettingsViewModel>()
    private lateinit var themeSwitcher: Switch

    private var isThemeChanging = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settingsShareButton = view.findViewById<LinearLayout>(R.id.button_settings_share)
        settingsShareButton.setOnClickListener {
            openShare()
        }

        val supportButton = view.findViewById<LinearLayout>(R.id.button_settings_support)
        supportButton.setOnClickListener {
            openSupport()
        }

        val agreementButton = view.findViewById<LinearLayout>(R.id.button_settings_agreement)
        agreementButton.setOnClickListener {
            openAgreement()
        }

        themeSwitcher = view.findViewById(R.id.settings_switch)

        viewModel.state.observe(viewLifecycleOwner) { state ->
            render(state)
        }
    }

    private fun render(state: SettingsState) {
        themeSwitcher.setOnCheckedChangeListener(null)

        if (themeSwitcher.isChecked != state.isDarkThemeEnabled) {
            themeSwitcher.isChecked = state.isDarkThemeEnabled
        }

        if (!isThemeChanging) {
            themeSwitcher.isEnabled = true
        }

        themeSwitcher.setOnCheckedChangeListener { buttonView, checked ->
            if (isThemeChanging) {
                return@setOnCheckedChangeListener
            }

            isThemeChanging = true
            buttonView.isEnabled = false

            viewModel.onThemeSwitchChanged(checked)

            buttonView.post {
                if (isAdded) {
                    (requireActivity().applicationContext as App).switchTheme(checked)
                }
            }
        }
    }

    private fun openShare() {
        val shareText = getString(R.string.share_link)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        startActivity(Intent.createChooser(intent, "Поделиться приложением"))
    }

    private fun openSupport() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.support_email_text))
        }

        startActivity(intent)
    }

    private fun openAgreement() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(getString(R.string.practicum_offer_link))
        }

        startActivity(intent)
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
}