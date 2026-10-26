package com.rktmkalita.echo.fragments


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.*
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.rktmkalita.echo.Database.EchoDatabase
import com.rktmkalita.echo.R
import com.rktmkalita.echo.Songs
import com.rktmkalita.echo.adapters.FavouriteAdapter
import com.rktmkalita.echo.playback.Media3PlayerAdapter


/**
 * A simple [Fragment] subclass.
 */
class FavouriteFragment : Fragment() {

    var myActivity: Activity?=null
    var getSongsList : ArrayList<Songs>?=null
    var noFavourites: TextView?=null
    var nowPlayingBottomBar: RelativeLayout?=null
    var playPauseButton: ImageButton?=null
    var songTitle: TextView?=null
    var recyclerView: RecyclerView?=null
    var trackPosition: Int=0
    var favouriteContent: EchoDatabase?=null

    var refreshList: ArrayList<Songs>?=null
    var getListfromDatabase: ArrayList<Songs>?=null

    object Statified{
        var mediaPlayer: Media3PlayerAdapter?=null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_favourite,container,false)
        requireActivity().title = "Favourites"
        noFavourites = view?.findViewById(R.id.noFavourites)
        nowPlayingBottomBar = view?.findViewById(R.id.hiddenBarFavScreen)
        songTitle = view?.findViewById(R.id.songTitleMainScreen)
        playPauseButton = view?.findViewById(R.id.playPauseButton)
        view?.findViewById<ImageButton>(R.id.miniPreviousButton)?.setOnClickListener { SongPlayingFragment().playPrevious(); refreshMiniArtwork() }
        view?.findViewById<ImageButton>(R.id.miniNextButton)?.setOnClickListener { SongPlayingFragment.Staticated.playNext("PlayNextNormal"); refreshMiniArtwork() }
        recyclerView = view?.findViewById(R.id.favouriteRecycler)
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        favouriteContent = EchoDatabase(myActivity)
        getSongsList = getSongsFromPhone()
        if (getSongsList == null){
            recyclerView?.visibility = View.INVISIBLE
            noFavourites?.visibility = View.VISIBLE
        }else{
            var favouriteAdapter = FavouriteAdapter(getSongsList as ArrayList<Songs>, myActivity as Context)
            val mLayoutManager = LinearLayoutManager(activity)
            recyclerView?.layoutManager = mLayoutManager
            recyclerView?.itemAnimator = DefaultItemAnimator()
            recyclerView?.adapter = favouriteAdapter
            recyclerView?.setHasFixedSize(true)
            recyclerView?.visibility = View.VISIBLE
            noFavourites?.visibility = View.INVISIBLE
        }

        bottomBarSetup()
        display_favourites_by_searching()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item = menu?.findItem(R.id.action_sort)
        item?.isVisible = false
    }

    fun getSongsFromPhone(): ArrayList<Songs>{

        var arrayList = ArrayList<Songs>()
        var contentResolver = myActivity?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songUri, null, null,null,null)
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

    fun bottomBarSetup(){
        try {
            bottomBarClickHandler()
            songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            songTitle?.isSelected = true
            songTitle?.setHorizontallyScrolling(true)
            SongPlayingFragment.Staticated.updateAlbumArt(SongPlayingFragment.Statified.currentSongHelper?.songPath)
            SongPlayingFragment.Staticated.updateMiniArtwork(view?.findViewById(R.id.defaultMusic), SongPlayingFragment.Statified.currentSongHelper?.songPath)
            SongPlayingFragment.Statified.mediaPlayer?.setOnPlayingChangedListener { isPlaying ->
                activity?.runOnUiThread {
                    nowPlayingBottomBar?.visibility = if (SongPlayingFragment.Statified.currentSongHelper?.songPath != null) View.VISIBLE else View.INVISIBLE
                    playPauseButton?.setBackgroundResource(
                        if (isPlaying) R.drawable.pause_icon else R.drawable.play_icon
                    )
                    songTitle?.text = SongPlayingFragment.Statified.currentSongHelper?.songTitle
                    songTitle?.isSelected = true
                    songTitle?.setHorizontallyScrolling(true)
                    SongPlayingFragment.Staticated.updateMiniArtwork(
                        view?.findViewById(R.id.defaultMusic),
                        SongPlayingFragment.Statified.currentSongHelper?.songPath
                    )
                }
            }
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
    fun bottomBarClickHandler(){
        nowPlayingBottomBar?.setOnClickListener({
            Statified.mediaPlayer = SongPlayingFragment.Statified.mediaPlayer
            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist", SongPlayingFragment.Statified.currentSongHelper?.songArtist)
            args.putString("path", SongPlayingFragment.Statified.currentSongHelper?.songPath)
            args.putString("songTitle", SongPlayingFragment.Statified.currentSongHelper?.songTitle)
                    args.putInt("songId", SongPlayingFragment.Statified.currentSongHelper?.songId?.toInt() ?: 0)
                    args.putInt("songPosition", SongPlayingFragment.Statified.currentSongHelper?.currentPosition?.toInt() ?: 0)
            args.putParcelableArrayList("songData", SongPlayingFragment.Statified.fetchSongs)
            args.putString("FavBottomBar","success")
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
                if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying == true){
                    SongPlayingFragment.Statified.mediaPlayer?.pause()
                    trackPosition = SongPlayingFragment.Statified.mediaPlayer?.getCurrentPosition() ?: 0
                    playPauseButton?.setBackgroundResource(R.drawable.play_icon)
                }else{
                    SongPlayingFragment.Statified.mediaPlayer?.start()
                    playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
                }
            })
    }

    private fun refreshMiniArtwork() {
        SongPlayingFragment.Staticated.updateMiniArtwork(view?.findViewById(R.id.defaultMusic), SongPlayingFragment.Statified.currentSongHelper?.songPath)
        songTitle?.text = SongPlayingFragment.Statified.currentSongHelper?.songTitle ?: ""
        songTitle?.isSelected = true
        songTitle?.setHorizontallyScrolling(true)
    }

    fun display_favourites_by_searching(){
        if ((favouriteContent?.chechSize() ?: 0) > 0){
            recyclerView?.visibility = View.VISIBLE
            noFavourites?.visibility = View.INVISIBLE
            refreshList = ArrayList<Songs>()
            getListfromDatabase = favouriteContent?.queryDBList()
            var fetchListfromDevice = getSongsFromPhone()
            if (fetchListfromDevice != null){
                for (i in 0 until fetchListfromDevice.size){
                    for(j in 0 until (getListfromDatabase?.size ?: 0)){
                        if (getListfromDatabase?.get(j)?.songID == fetchListfromDevice?.get(i)?.songID){
                            refreshList?.add((getListfromDatabase as ArrayList<Songs>)[j])
                        }
                    }
                }
            }else{

            }
            if (refreshList.isNullOrEmpty()){
                recyclerView?.visibility = View.INVISIBLE
                noFavourites?.visibility = View.VISIBLE
            }else{
                val favouriteAdapter = FavouriteAdapter(refreshList as ArrayList<Songs>, myActivity as Context)
                val mLayoutManager = LinearLayoutManager(activity)
                recyclerView?.layoutManager = mLayoutManager
                recyclerView?.itemAnimator = DefaultItemAnimator()
                recyclerView?.adapter = favouriteAdapter
                recyclerView?.setHasFixedSize(true)
                recyclerView?.visibility = View.VISIBLE
                noFavourites?.visibility = View.INVISIBLE
            }
        }else{
            recyclerView?.visibility = View.INVISIBLE
            noFavourites?.visibility = View.VISIBLE
        }
    }
}// Required empty public constructor
