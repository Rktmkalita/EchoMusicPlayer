package com.rktmkalita.echo.adapters

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.ImageView
import android.media.MediaMetadataRetriever
import android.widget.Toast
import com.rktmkalita.echo.R
import com.rktmkalita.echo.Songs
import com.rktmkalita.echo.activities.MainActivity
import com.rktmkalita.echo.fragments.MainScreenFragment
import com.rktmkalita.echo.fragments.SongPlayingFragment

/**
 * Created by R kalita on 10/07/2018.
 */
class MainScreenAdapter(_songDetails: ArrayList<Songs>, _context: Context): RecyclerView.Adapter<MainScreenAdapter.MyViewHolder>(){
    private val allSongDetails = ArrayList<Songs>(_songDetails)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_custom_mainscreen_adapter, parent, false)

        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        if(songDetails==null){
            return 0
        }else{
            return (songDetails as ArrayList<Songs>).size
        }
    }

    fun filter(query: String) {
        val normalizedQuery = query.trim().lowercase()
        songDetails = if (normalizedQuery.isEmpty()) {
            ArrayList(allSongDetails)
        } else {
            ArrayList(allSongDetails.filter {
                it.songTitle.lowercase().contains(normalizedQuery) ||
                        it.artist.lowercase().contains(normalizedQuery)
            })
        }
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val songObject = songDetails?.get(position)
        holder?.trackTitle?.text = songObject?.songTitle
        holder?.trackArtist?.text = songObject?.artist
        holder.songArtwork?.setImageResource(R.drawable.ic_song_note)
        songObject?.songData?.let { path ->
            runCatching {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(path)
                retriever.embeddedPicture?.let { bytes ->
                    holder.songArtwork?.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                }
                retriever.release()
            }
        }
        holder?.contentHolder?.setOnClickListener({
            val songPlayingFragment = SongPlayingFragment()
            val args = Bundle()
            args.putString("songArtist", songObject?.artist)
            args.putString("path", songObject?.songData)
            args.putString("songTitle", songObject?.songTitle)
            args.putInt("songId", songObject?.songID?.toInt() as Int)
            args.putInt("songPosition", position)
            args.putParcelableArrayList("songData", songDetails)
            songPlayingFragment.arguments = args
            (mContext as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, songPlayingFragment)
                    .addToBackStack("SongPlayingFragment")
                    .commit()
        })
    }

    var songDetails: ArrayList<Songs>?=null
    var mContext: Context?=null
    init {
        this.songDetails=_songDetails
        this.mContext=_context
    }


    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var trackTitle: TextView?=null
        var trackArtist: TextView?=null
        var contentHolder: RelativeLayout?=null
        var songArtwork: ImageView?=null

        init {
            trackTitle=view.findViewById<TextView>(R.id.trackTitle)
            trackArtist=view.findViewById<TextView>(R.id.trackArtist)
            contentHolder=view.findViewById<RelativeLayout>(R.id.contentRow)
            songArtwork=view.findViewById(R.id.songArtwork)
        }

    }

}
