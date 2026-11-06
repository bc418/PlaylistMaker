package com.practicum.playlistmaker.presentation.playlistcreate

import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import android.graphics.Outline
import android.view.ViewOutlineProvider
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.practicum.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreatePlaylistFragment : Fragment() {
    private val viewModel by viewModel<CreatePlaylistViewModel>()
    private var selectedCoverUri: String? = null
    private lateinit var nameField: TextInputEditText
    private lateinit var descriptionField: TextInputEditText
    private lateinit var createButton: Button
    private lateinit var coverImage: ImageView

    private val photoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedCoverUri = uri.toString()
            renderCover()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_create_playlist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nameField = view.findViewById(R.id.playlistNameInput)
        descriptionField = view.findViewById(R.id.playlistDescriptionInput)
        createButton = view.findViewById(R.id.createPlaylistConfirmButton)
        coverImage = view.findViewById(R.id.createPlaylistCover)

        val cornerRadius = 8f * resources.displayMetrics.density

        coverImage.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(
                    0,
                    0,
                    view.width,
                    view.height,
                    cornerRadius
                )
            }
        }

        coverImage.clipToOutline = true

        selectedCoverUri = savedInstanceState?.getString(KEY_COVER_URI)
        nameField.setText(savedInstanceState?.getString(KEY_NAME).orEmpty())
        descriptionField.setText(savedInstanceState?.getString(KEY_DESCRIPTION).orEmpty())
        renderCover()

        nameField.doAfterTextChanged { createButton.isEnabled = !it.isNullOrBlank() }
        createButton.isEnabled = !nameField.text.isNullOrBlank()
        coverImage.setOnClickListener {
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { confirmExit() }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)
        view.findViewById<View>(R.id.createPlaylistBackButton).setOnClickListener { confirmExit() }
        createButton.setOnClickListener {
            viewModel.createPlaylist(
                nameField.text?.toString().orEmpty(),
                descriptionField.text?.toString().orEmpty(),
                selectedCoverUri
            )
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                CreatePlaylistState.Idle -> createButton.isEnabled = !nameField.text.isNullOrBlank()
                CreatePlaylistState.Saving -> createButton.isEnabled = false
                is CreatePlaylistState.Created -> {
                    Toast.makeText(requireContext(), getString(R.string.playlist_created, state.name), Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                }
                CreatePlaylistState.Error -> {
                    Toast.makeText(requireContext(), R.string.playlist_save_error, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }
    }

    private fun renderCover() {
        val hasCover = selectedCoverUri != null

        val padding = if (hasCover) {
            0
        } else {
            (76 * resources.displayMetrics.density).toInt()
        }

        coverImage.setPadding(padding, padding, padding, padding)

        coverImage.setBackgroundResource(
            if (hasCover) {
                R.drawable.bg_playlist_cover_selected
            } else {
                R.drawable.bg_playlist_cover_placeholder
            }
        )

        coverImage.scaleType = if (hasCover) {
            ImageView.ScaleType.CENTER_CROP
        } else {
            ImageView.ScaleType.CENTER_INSIDE
        }

        if (hasCover) {
            Glide.with(this)
                .load(Uri.parse(selectedCoverUri))
                .centerCrop()
                .into(coverImage)
        } else {
            Glide.with(this).clear(coverImage)
            coverImage.setImageResource(R.drawable.ic_playlist_add_cover)
        }
    }

    private fun confirmExit() {
        val dirty = !nameField.text.isNullOrEmpty() ||
                !descriptionField.text.isNullOrEmpty() ||
                selectedCoverUri != null

        if (!dirty) {
            findNavController().popBackStack()
            return
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.playlist_exit_title)
            .setMessage(R.string.playlist_exit_message)
            .setNegativeButton(R.string.playlist_exit_cancel, null)
            .setPositiveButton(R.string.playlist_exit_confirm) { _, _ ->
                findNavController().popBackStack()
            }
            .create()

        dialog.setOnShowListener {
            val buttonColor = ContextCompat.getColor(
                requireContext(),
                R.color.playlist_dialog_button_color
            )

            dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                .setTextColor(buttonColor)

            dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(buttonColor)
        }

        dialog.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::nameField.isInitialized) {
            outState.putString(KEY_NAME, nameField.text?.toString())
            outState.putString(KEY_DESCRIPTION, descriptionField.text?.toString())
        }
        outState.putString(KEY_COVER_URI, selectedCoverUri)
    }

    companion object {
        private const val KEY_NAME = "playlist_draft_name"
        private const val KEY_DESCRIPTION = "playlist_draft_description"
        private const val KEY_COVER_URI = "playlist_draft_cover_uri"
    }
}
