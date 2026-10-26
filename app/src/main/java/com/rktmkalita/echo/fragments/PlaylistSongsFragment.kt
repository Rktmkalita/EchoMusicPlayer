package com.rktmkalita.echo.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rktmkalita.echo.Database.EchoDatabase
import com.rktmkalita.echo.R
import com.rktmkalita.echo.adapters.PlaylistSongsAdapter

class PlaylistSongsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val name = requireArguments().getString("playlistName") ?: "Playlist"
        val root = FrameLayout(requireContext()).apply {
            setBackgroundColor(requireContext().getColor(R.color.surface))
        }
        val songs = EchoDatabase(requireContext()).playlistSongs(name)
        Log.d("EchoPlaylist", "opened '$name' with ${songs.size} playlist rows")
        val recycler = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
            setBackgroundColor(requireContext().getColor(R.color.surface))
            adapter = PlaylistSongsAdapter(songs, requireContext())
            setPadding(0, 0, 0, 72)
        }
        root.addView(recycler, FrameLayout.LayoutParams(-1, -1))
        if (songs.isEmpty()) {
            root.addView(TextView(requireContext()).apply {
                text = "No songs in this playlist yet"
                textSize = 16f
                setTextColor(requireContext().getColor(R.color.textSecondary))
                setPadding(32, 48, 32, 32)
            }, FrameLayout.LayoutParams(-1, -2))
        }
        val miniPlayer = inflater.inflate(R.layout.mini_player, root, false)
        val miniParams = FrameLayout.LayoutParams(-1, 72).apply { gravity = android.view.Gravity.BOTTOM }
        root.addView(miniPlayer, miniParams)
        bindMiniPlayer(miniPlayer)
        requireActivity().title = name
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
        view.findViewById<ImageButton>(R.id.miniPreviousButton).setOnClickListener {
            SongPlayingFragment().playPrevious()
        }
        view.findViewById<ImageButton>(R.id.miniNextButton).setOnClickListener {
            SongPlayingFragment.Staticated.playNext("PlayNextNormal")
        }
    }
}
