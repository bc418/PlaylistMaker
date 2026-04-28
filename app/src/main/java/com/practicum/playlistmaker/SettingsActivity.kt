package com.practicum.playlistmaker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        val root = findViewById<View>(R.id.root_activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBars.top)
            insets
        }

        val settingsBackButton = findViewById<ImageButton>(R.id.button_settings_back)

        settingsBackButton.setOnClickListener {
            finish()
        }


        val settingsShareButton = findViewById<LinearLayout>(R.id.button_settings_share)

        settingsShareButton.setOnClickListener {
            openShare()
        }

        val supportButton = findViewById<LinearLayout>(R.id.button_settings_support)

        supportButton.setOnClickListener {
            openSupport()
        }

        val agreementButton = findViewById<LinearLayout>(R.id.button_settings_agreement)

        agreementButton.setOnClickListener {
            openAgreement()
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


}