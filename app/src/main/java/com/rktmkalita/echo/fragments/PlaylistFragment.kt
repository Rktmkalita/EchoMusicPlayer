package com.rktmkalita.echo.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.rktmkalita.echo.Database.EchoDatabase
import com.rktmkalita.echo.R

class PlaylistFragment : Fragment() {
    override fun onCreateView(inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, savedInstanceState: Bundle?): View {
        requireActivity().title = "Playlists"
        val content = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(requireContext().getColor(R.color.surface))
        }
        val add = TextView(requireContext()).apply {
            text = "+  Create playlist"
            textSize = 18f
            setTextColor(requireContext().getColor(R.color.colorAccent))
            setPadding(0, 12, 0, 24)
            setOnClickListener { createPlaylist() }
        }
        content.addView(add)
        renderPlaylists(content)
        val scroll = android.widget.ScrollView(requireContext()).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(-1, -1)
            setFillViewport(true)
            setBackgroundColor(requireContext().getColor(R.color.surface))
            addView(content)
        }
        val root = FrameLayout(requireContext()).apply {
            setBackgroundColor(requireContext().getColor(R.color.surface))
            addView(scroll, FrameLayout.LayoutParams(-1, -1))
        }
        val miniPlayer = inflater.inflate(R.layout.mini_player, root, false)
        root.addView(miniPlayer, FrameLayout.LayoutParams(-1, 72).apply { gravity = android.view.Gravity.BOTTOM })
        bindMiniPlayer(miniPlayer)
        return root
    }

    private fun bindMiniPlayer(view: View) {
        val current = SongPlayingFragment.Statified.currentSongHelper
        val player = SongPlayingFragment.Statified.mediaPlayer
        view.findViewById<TextView>(R.id.songTitleMainScreen).apply {
            text = current?.songTitle ?: ""
            isSelected = true
            setHorizontallyScrolling(true)
        }
        view.visibility = if (current?.songPath != null && player != null) View.VISIBLE else View.INVISIBLE
        view.findViewById<ImageButton>(R.id.playPauseButton).apply {
            setBackgroundResource(if (player?.isPlaying == true) R.drawable.pause_icon else R.drawable.play_icon)
            setOnClickListener { player?.let { if (it.isPlaying) it.pause() else it.start() } }
        }
        view.findViewById<ImageButton>(R.id.miniPreviousButton).setOnClickListener { SongPlayingFragment().playPrevious() }
        view.findViewById<ImageButton>(R.id.miniNextButton).setOnClickListener { SongPlayingFragment.Staticated.playNext("PlayNextNormal") }
    }

    private fun renderPlaylists(root: LinearLayout) {
        val database = EchoDatabase(requireContext())
        database.playlistNames().forEach { name ->
            root.addView(TextView(requireContext()).apply {
                text = "♫  $name"
                textSize = 17f
                setTextColor(requireContext().getColor(R.color.textPrimary))
                setPadding(0, 18, 0, 18)
                setOnClickListener {
                    parentFragmentManager.beginTransaction().replace(R.id.details_fragment, PlaylistSongsFragment().apply {
                        arguments = Bundle().apply { putString("playlistName", name) }
                    }).addToBackStack("PlaylistSongs").commit()
                }
            })
        }
    }

    private fun createPlaylist() {
        val input = EditText(requireContext()).apply { hint = "Playlist name" }
        AlertDialog.Builder(requireContext()).setTitle("New playlist").setView(input)
            .setPositiveButton("Create") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    EchoDatabase(requireContext()).createPlaylist(name)
                    parentFragmentManager.beginTransaction().replace(R.id.details_fragment, PlaylistFragment()).commit()
                }
            }.setNegativeButton("Cancel", null).show()
    }
}
