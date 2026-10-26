package com.rktmkalita.echo.fragments


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.*
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.rktmkalita.echo.R
import com.rktmkalita.echo.Songs
import com.rktmkalita.echo.adapters.MainScreenAdapter
import com.rktmkalita.echo.playback.Media3PlayerAdapter
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class MainScreenFragment : Fragment() {

    var getSongsList: ArrayList<Songs>? = null
    var nowPlayingBottomBar: RelativeLayout? = null
    var playPauseButton: ImageButton? = null
    var songTitle: TextView? = null
    var visibleLayout: RelativeLayout? = null
    var noSongs: RelativeLayout? = null
    var recyclerView: RecyclerView? = null
    var myActivity: Activity? = null
    var _mainScreenAdapter: MainScreenAdapter?=null
    var trackPosition: Int=0

    object Statified{
        var mediaplayer: Media3PlayerAdapter? = null
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_main_screen, container, false)
        setHasOptionsMenu(true)
        requireActivity().title = "All songs"
        visibleLayout = view?.findViewById<RelativeLayout>(R.id.visibleLayout)
        noSongs = view?.findViewById<RelativeLayout>(R.id.noSongs)
        nowPlayingBottomBar = view?.findViewById<RelativeLayout>(R.id.hiddenBarMainScreen)
        songTitle = view?.findViewById<TextView>(R.id.songTitleMainScreen)
        playPauseButton = view?.findViewById<ImageButton>(R.id.playPauseButton)
        view?.findViewById<ImageButton>(R.id.miniPreviousButton)?.setOnClickListener { SongPlayingFragment().playPrevious(); refreshMiniArtwork() }
        view?.findViewById<ImageButton>(R.id.miniNextButton)?.setOnClickListener { SongPlayingFragment.Staticated.playNext("PlayNextNormal"); refreshMiniArtwork() }
        recyclerView = view?.findViewById<RecyclerView>(R.id.contentMain)
        return view

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getSongsList = getSongsFromPhone()
        val prefs = requireActivity().getSharedPreferences("action_sort", Context.MODE_PRIVATE)
        val action_sort_ascending = prefs.getString("action_sort_ascending", "true")
        val action_sort_recent = prefs.getString("action_sort_recent", "false")

        if (getSongsList == null){
            visibleLayout?.visibility = View.INVISIBLE
            noSongs?.visibility = View.VISIBLE
        }else{
            _mainScreenAdapter = MainScreenAdapter(getSongsList as ArrayList<Songs>, myActivity as Context)
            val mLayoutManager = LinearLayoutManager(requireActivity())
            recyclerView?.layoutManager = mLayoutManager
            recyclerView?.itemAnimator = DefaultItemAnimator()
            recyclerView?.adapter = _mainScreenAdapter
        }

        if (getSongsList != null){
            if (action_sort_ascending!!.equals("true",true)){
                Collections.sort(getSongsList, Songs.Statified.nameComparator)
                _mainScreenAdapter?.notifyDataSetChanged()
            }else if (action_sort_recent!!.equals("true", true)){
                Collections.sort(getSongsList, Songs.Statified.dateComparator)
                _mainScreenAdapter?.notifyDataSetChanged()
            }
        }

        bottomBar_setup()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu?.clear()
        inflater?.inflate(R.menu.main, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search songs or artists"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean = true

            override fun onQueryTextChange(newText: String): Boolean {
                _mainScreenAdapter?.filter(newText)
                return true
            }
        })
        return
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val switcher = item?.itemId
        if (switcher == R.id.action_sort_ascending){
            val editor = myActivity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)?.edit()
            editor?.putString("action_sort_ascending","true")
            editor?.putString("action_sort_recent","false")
            editor?.apply()
            if (getSongsList!=null){
                Collections.sort(getSongsList, Songs.Statified.nameComparator)
            }
            _mainScreenAdapter?.notifyDataSetChanged()
            return false
        }else if (switcher == R.id.action_sort_recent){
            val editor = myActivity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)?.edit()
            editor?.putString("action_sort_ascending","false")
            editor?.putString("action_sort_recent","true")
            editor?.apply()
            if (getSongsList!=null){
                Collections.sort(getSongsList, Songs.Statified.dateComparator)
            }
            _mainScreenAdapter?.notifyDataSetChanged()
            return false
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        myActivity = activity
    }

    fun getSongsFromPhone(): ArrayList<Songs>{

        var arrayList = ArrayList<Songs>()
        var contentResolver = myActivity?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DATE_ADDED)
        var songCursor = contentResolver?.query(songUri, projection,
                MediaStore.Audio.Media.IS_MUSIC + "=1", null, MediaStore.Audio.Media.TITLE + " COLLATE NOCASE ASC")
        if(songCursor!=null && songCursor.moveToFirst()){
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dataIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            do {
                var currentId = songCursor.getLong(songId)
                var currentTitle = songCursor.getString(songTitle)
                var currentArtist = songCursor.getString(songArtist)
                var currentData = songCursor.getString(songData)
                var currentDate = songCursor.getLong(dataIndex)
                arrayList.add(Songs(currentId, currentTitle, currentArtist, currentData, currentDate))
            } while (songCursor.moveToNext())
            songCursor.close()
        }
        return arrayList
    }

    fun bottomBar_setup(){
        try {
            bottomBar_ClickHandler()
            songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            songTitle?.isSelected = true
            songTitle?.setHorizontallyScrolling(true)
            SongPlayingFragment.Staticated.updateAlbumArt(SongPlayingFragment.Statified.currentSongHelper?.songPath)
            SongPlayingFragment.Staticated.updateMiniArtwork(view?.findViewById(R.id.defaultMusic), SongPlayingFragment.Statified.currentSongHelper?.songPath)
            SongPlayingFragment.Statified.mediaPlayer?.setOnCompletionListener({
                songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
                SongPlayingFragment.Staticated.onSongComplete()
            })
            val hasCurrentSong = SongPlayingFragment.Statified.currentSongHelper?.songPath != null &&
                    SongPlayingFragment.Statified.mediaPlayer != null
            nowPlayingBottomBar?.visibility = if (hasCurrentSong) View.VISIBLE else View.INVISIBLE
            playPauseButton?.setBackgroundResource(
                if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying == true) R.drawable.pause_icon
                else R.drawable.play_icon
            )
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
    fun bottomBar_ClickHandler(){
        nowPlayingBottomBar?.setOnClickListener({
            Statified.mediaplayer = SongPlayingFragment.Statified.mediaPlayer
            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist", SongPlayingFragment.Statified.currentSongHelper?.songArtist)
            args.putString("path", SongPlayingFragment.Statified.currentSongHelper?.songPath)
            args.putString("songTitle", SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            args.putInt("songId", SongPlayingFragment.Statified.currentSongHelper?.songId?.toInt() as Int)
            args.putInt("songPosition", SongPlayingFragment.Statified.currentSongHelper?.currentPosition?.toInt() as Int)
            args.putParcelableArrayList("songData", SongPlayingFragment.Statified.fetchSongs)
            args.putString("MainBottomBar","success")
            songPlayingFragment.arguments = args
            parentFragmentManager.beginTransaction()
                    .replace(R.id.details_fragment, songPlayingFragment)
                    .addToBackStack("SongPlayingFragment")
                    .commit()
        })
        listOf(R.id.defaultMusic, R.id.nowPlaying, R.id.songTitleMainScreen).forEach { id ->
            nowPlayingBottomBar?.findViewById<View>(id)?.setOnClickListener { nowPlayingBottomBar?.performClick() }
        }

        playPauseButton?.setOnClickListener({
            if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){
                SongPlayingFragment.Statified.mediaPlayer?.pause()
                trackPosition = SongPlayingFragment.Statified.mediaPlayer?.getCurrentPosition() as Int
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            }else{
                SongPlayingFragment.Statified.mediaPlayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
    }

    private fun refreshMiniArtwork() {
        SongPlayingFragment.Staticated.updateMiniArtwork(view?.findViewById(R.id.defaultMusic), SongPlayingFragment.Statified.currentSongHelper?.songPath)
                    songTitle?.text = SongPlayingFragment.Statified.currentSongHelper?.songTitle
                    songTitle?.isSelected = true
                    songTitle?.setHorizontallyScrolling(true)
    }

}// Required empty public constructor
