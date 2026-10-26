package com.rktmkalita.echo.fragments


import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.view.*
import android.graphics.Bitmap
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.app.AlertDialog
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.rktmkalita.echo.CurrentSongHelper
import com.rktmkalita.echo.Database.EchoDatabase
import com.rktmkalita.echo.R
import com.rktmkalita.echo.Songs
import com.rktmkalita.echo.playback.Media3PlayerAdapter
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass.
 */
class SongPlayingFragment : Fragment() {

    object Statified{

        lateinit var myActivity: Activity
        var mediaPlayer: Media3PlayerAdapter?=null
        var startTimeText: TextView?=null
        var endTimeText: TextView?=null
        var playPauseImageButton: ImageButton?=null
        var previousImageButton: ImageButton?=null
        var nextImageButton: ImageButton?=null
        var loopImageButton: ImageButton?=null
        var rewind10ImageButton: View?=null
        var forward10ImageButton: View?=null
        var seekbar: SeekBar?=null
        var songArtistView: TextView?=null
        var songTitleView: TextView?=null
        var shuffleImageButton: ImageButton?=nextImageButton

        var currentPosition: Int = 0
        var fetchSongs: ArrayList<Songs>?=null
        var currentSongHelper: CurrentSongHelper?=null
        var audioVisualitation: AudioVisualization?=null
        var glView: GLAudioVisualizationView?=null
        var albumArt: ImageView?=null

        var fab: ImageButton? = null

        var favouriteContent: EchoDatabase?=null
        var mSensorManager: SensorManager?=null
        var mSensorListener: SensorEventListener?=null
        var MY_PREFS_NAME = "ShakeFeature"

        var updateSongTime = object: Runnable{
            override fun run() {
                val getcurrent = Statified.mediaPlayer?.getCurrentPosition()
                if (getcurrent != null) Statified.startTimeText?.text = formatTime(getcurrent)

                if (getcurrent != null) Statified.seekbar?.setProgress(getcurrent)
                Handler().postDelayed(this, 1000)
            }
        }

        fun formatTime(milliseconds: Int): String {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
            val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) % 60
            return String.format("%d:%02d", minutes, seconds)
        }
    }

    object Staticated{
        var MY_PREFS_SHUFFLE = "Shuffle feature"
        var MY_PREFS_LOOP = "Loop feature"

        fun onSongComplete(){
            if (Statified.currentSongHelper?.isShuffle as Boolean){
                playNext("PlayNextLikeNormalShuffle")
                Statified.currentSongHelper?.isPlaying=true
            }else{
                if (Statified.currentSongHelper?.isLoop as Boolean){
                    Statified.currentSongHelper?.isPlaying=true
                    var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
                    Statified.currentSongHelper?.songTitle = nextSong?.songTitle
                    Statified.currentSongHelper?.songPath = nextSong?.songData
                    Statified.currentSongHelper?.currentPosition = Statified.currentPosition
                    Statified.currentSongHelper?.songId = nextSong?.songID as Long

                    updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)

                    Statified.mediaPlayer?.reset()
                    try {
                        Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(Statified.currentSongHelper?.songPath))
                        Statified.mediaPlayer?.prepare()
                        Statified.mediaPlayer?.start()
                        processInformation(Statified.mediaPlayer as Media3PlayerAdapter)
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }else{
                    playNext("PlayNextNormal")
                    Statified.currentSongHelper?.isPlaying=true
                }
            }
            if (Statified.favouriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean){
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity, R.drawable.favorite_on))
            }else{
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity, R.drawable.favorite_off))
            }
        }

        fun updateTextViews(songTitle: String, songArtist: String){
            var songTitleUpdated = songTitle
            if (songTitle.equals("<unknown>", true)){
                songTitleUpdated = "unknown"
            }

            var songArtistUpdated = songArtist
            if (songArtist.equals("<unknown>", true)){
                songArtistUpdated = "unknown"
            }
            Statified.songTitleView?.setText(songTitleUpdated)
            Statified.songArtistView?.setText(songArtistUpdated)
        }

        fun processInformation(mediaPlayer: Media3PlayerAdapter){

            val finalTime = mediaPlayer.duration
            val startTime = mediaPlayer.getCurrentPosition()
            Statified.seekbar?.max = finalTime
            Statified.startTimeText?.text = Statified.formatTime(startTime)
            Statified.endTimeText?.text = Statified.formatTime(finalTime)
            Statified.seekbar?.setProgress(startTime)
            Handler().postDelayed(Statified.updateSongTime, 1000)
        }

        fun updateAlbumArt(path: String?) {
            try {
                if (path.isNullOrBlank()) return
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(path)
                val artwork = retriever.embeddedPicture?.let {
                    android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size)
                }
                retriever.release()
                if (artwork != null) Statified.albumArt?.setImageBitmap(artwork)
                else Statified.albumArt?.setImageResource(R.drawable.now_playing_bar_eq_image)
            } catch (_: RuntimeException) {
                Statified.albumArt?.setImageResource(R.drawable.now_playing_bar_eq_image)
            }
        }

        fun updateMiniArtwork(imageView: ImageView?, path: String?) {
            try {
                if (path.isNullOrBlank()) return
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(path)
                retriever.embeddedPicture?.let {
                    imageView?.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size))
                }
                retriever.release()
            } catch (_: RuntimeException) {
                imageView?.setImageResource(R.drawable.now_playing_bar_eq_image)
            }
        }

        fun playNext(check: String){
            val songCount = Statified.fetchSongs?.size ?: return
            if (songCount == 0) return
            if (check.equals("PlayNextNormal", true)){
                Statified.currentPosition = (Statified.currentPosition + 1) % songCount
            }else if (check.equals("PlayNextLikeNormalShuffle", true)){
                var randomObject = Random()
                var randomPosition = randomObject.nextInt(songCount)
                Statified.currentPosition = randomPosition

            }
            Statified.currentSongHelper?.isLoop = false
            var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
            Statified.currentSongHelper?.songTitle = nextSong?.songTitle
            Statified.currentSongHelper?.songPath = nextSong?.songData
            Statified.currentSongHelper?.songArtist = nextSong?.artist
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition
            val selectedSong = nextSong ?: return
            Statified.currentSongHelper?.songId = selectedSong.songID
            updateAlbumArt(selectedSong.songData)

            updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)

            Statified.mediaPlayer?.stop()
            Statified.mediaPlayer?.reset()
            try {
                Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(Statified.currentSongHelper?.songPath))
                Statified.mediaPlayer?.prepare()
                Statified.mediaPlayer?.start()
                Statified.mediaPlayer?.let { processInformation(it) }
            }catch (e: Exception){
                e.printStackTrace()
            }
            if (Statified.favouriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean){
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity, R.drawable.favorite_on))
            }else{
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity, R.drawable.favorite_off))
            }
        }
    }



    var mAccelaration: Float = 0f
    var mAccelarationCurrent: Float = 0f
    var mAccelarationLast: Float = 0f
    var lastShakeAt: Long = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var view = inflater!!.inflate(R.layout.fragment_song_playing, container,false)
        view.translationY = 0f
        view.alpha = 1f
        setHasOptionsMenu(true)
        requireActivity().title = "Now Playing"
        Statified.seekbar = view?.findViewById(R.id.seekBar)
        Statified.seekbar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) Statified.startTimeText?.text = Statified.formatTime(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Statified.mediaPlayer?.seekTo(seekBar.progress)
            }
        })
        Statified.startTimeText = view?.findViewById(R.id.startTime)
        Statified.endTimeText = view?.findViewById(R.id.endTime)
        Statified.playPauseImageButton = view?.findViewById(R.id.playPauseButton)
        Statified.nextImageButton = view?.findViewById(R.id.nextButton)
        Statified.previousImageButton = view?.findViewById(R.id.previousButton)
        Statified.loopImageButton = view?.findViewById(R.id.loopButton)
        Statified.rewind10ImageButton = view?.findViewById(R.id.rewind10Button)
        Statified.forward10ImageButton = view?.findViewById(R.id.forward10Button)
        Statified.shuffleImageButton = view?.findViewById(R.id.shuffleButton)
        Statified.songArtistView = view?.findViewById(R.id.songArtist)
        Statified.songTitleView = view?.findViewById(R.id.songTitle)
        Statified.glView = view?.findViewById(R.id.visualizer_view)
        Statified.fab = view?.findViewById(R.id.favouriteIcon)
        Statified.albumArt = view?.findViewById(R.id.album_art)
        Statified.fab?.alpha = 0.8f

        var downY = 0f
        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downY = event.rawY
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (event.rawY - downY > 140f) {
                        parentFragmentManager.popBackStack()
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> true
                else -> true
            }
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Statified.myActivity = context as Activity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Statified.audioVisualitation = Statified.glView as AudioVisualization
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        Statified.myActivity = activity
    }

    override fun onResume() {
        super.onResume()
        Statified.audioVisualitation?.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Statified.audioVisualitation?.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Statified.mSensorManager = Statified.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelaration = 0.0f
        mAccelarationCurrent = SensorManager.GRAVITY_EARTH
        mAccelarationLast = SensorManager.GRAVITY_EARTH
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem?=menu?.findItem(R.id.action_redirect)
        item?.isVisible=true
        val item2: MenuItem?=menu?.findItem(R.id.action_sort)
        item2?.isVisible=false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId){
            R.id.action_redirect ->{
                Statified.myActivity?.onBackPressed()
                return false
            }
        }
        return false
    }

    override fun onPause() {
        super.onPause()
        Statified.audioVisualitation?.onPause()
        Statified.mSensorManager?.unregisterListener(Statified.mSensorListener)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Statified.favouriteContent = EchoDatabase(Statified.myActivity)
        Statified.currentSongHelper = CurrentSongHelper()
        Statified.currentSongHelper?.isPlaying = true
        Statified.currentSongHelper?.isLoop = false
        Statified.currentSongHelper?.isShuffle = false

        var path: String?=null
        var _songTitles: String?=null
        var _songArtist: String?=null
        var songId: Long = 0
        try {
            val args = requireArguments()
            Statified.currentPosition = args.getInt("songPosition")
            Statified.fetchSongs = args.getParcelableArrayList("songData")

            path=args.getString("path")
            _songTitles=args.getString("songTitle")
            _songArtist=args.getString("songArtist")
            songId=args.getInt("songId").toLong()

            Statified.currentSongHelper?.songPath = path
            Statified.currentSongHelper?.songArtist = _songArtist
            Statified.currentSongHelper?.songTitle = _songTitles
            Statified.currentSongHelper?.songId = songId
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition

            loadAlbumArt(path)

            Staticated.updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)

        }catch (e: Exception){
            e.printStackTrace()
        }

        val fromFavBottomBar = requireArguments().get("FavBottomBar") as? String
        val fromMainBottomBar = requireArguments().get("MainBottomBar") as? String
        if (fromFavBottomBar != null) {
            Statified.mediaPlayer = FavouriteFragment.Statified.mediaPlayer
        } else if (fromMainBottomBar != null) {
            Statified.mediaPlayer = MainScreenFragment.Statified.mediaplayer
        } else {

            Statified.mediaPlayer = Media3PlayerAdapter.get(Statified.myActivity)
            Statified.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                val queue = Statified.fetchSongs
                if (!queue.isNullOrEmpty()) {
                    Statified.mediaPlayer?.setQueue(queue, Statified.currentPosition)
                } else {
                    Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(path))
                }
                Statified.mediaPlayer?.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Statified.mediaPlayer?.start()
        }
        val player = Statified.mediaPlayer ?: return
        Staticated.processInformation(player)

        Statified.mediaPlayer?.setOnPlayingChangedListener { isPlaying ->
            view?.post {
                Statified.playPauseImageButton?.setBackgroundResource(
                    if (isPlaying) R.drawable.pause_icon else R.drawable.play_icon
                )
                Statified.currentSongHelper?.isPlaying = isPlaying
            }
        }

        if (player.isPlaying){
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        }else{
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        Statified.mediaPlayer?.setOnCompletionListener {
            Staticated.onSongComplete()
        }
        Statified.mediaPlayer?.setOnReadyListener {
            Statified.mediaPlayer?.let { Staticated.processInformation(it) }
            Staticated.updateAlbumArt(Statified.currentSongHelper?.songPath)
            Staticated.updateTextViews(
                Statified.currentSongHelper?.songTitle ?: "Unknown",
                Statified.currentSongHelper?.songArtist ?: "Unknown"
            )
            Statified.playPauseImageButton?.setBackgroundResource(
                if (Statified.mediaPlayer?.isPlaying == true) R.drawable.pause_icon else R.drawable.play_icon
            )
        }
        clickHandler()
        try {
            val visualisationHandler = DbmHandler.Factory.newVisualizerHandler(Statified.myActivity, 0)
            Statified.audioVisualitation?.linkTo(visualisationHandler)
        } catch (e: RuntimeException) {
            Statified.glView?.visibility = View.GONE
        }

        var prefsForShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature", false)
        if (isShuffleAllowed == true){
            Statified.currentSongHelper?.isShuffle = true
            Statified.currentSongHelper?.isLoop = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }else{
            Statified.currentSongHelper?.isShuffle = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }

        var prefsForLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForLoop?.getBoolean("feature", false)
        if (isLoopAllowed == true){
            Statified.currentSongHelper?.isShuffle = false
            Statified.currentSongHelper?.isLoop = true
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
        }else{
            Statified.currentSongHelper?.isLoop = false
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }
        if (Statified.favouriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() ?: 0) == true){
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity, R.drawable.favorite_on))
        }else{
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity, R.drawable.favorite_off))
        }
    }

    private fun loadAlbumArt(path: String?) {
        try {
            if (path.isNullOrBlank()) return
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            val artwork: Bitmap? = retriever.embeddedPicture?.let {
                android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size)
            }
            retriever.release()
            if (artwork != null) Statified.albumArt?.setImageBitmap(artwork)
        } catch (_: RuntimeException) {
            Statified.albumArt?.setImageResource(R.drawable.now_playing_bar_eq_image)
        }
    }


    fun clickHandler(){

        Statified.fab?.setOnClickListener({
            if (Statified.favouriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean){
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity, R.drawable.favorite_off))
                Statified.favouriteContent?.deleteFavourite(Statified.currentSongHelper?.songId?.toInt() as Int)
                Toast.makeText(Statified.myActivity, "Removed from favourites", Toast.LENGTH_SHORT).show()
            }else{
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity, R.drawable.favorite_on))
                Statified.favouriteContent?.storeAsFavourite(Statified.currentSongHelper?.songId?.toInt() as Int, Statified.currentSongHelper?.songArtist,
                        Statified.currentSongHelper?.songTitle, Statified.currentSongHelper?.songPath)
                Toast.makeText(Statified.myActivity, "Added to favourites", Toast.LENGTH_SHORT).show()
            }
        })
        Statified.shuffleImageButton?.setOnClickListener({
            var editorShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()
            if (Statified.currentSongHelper?.isShuffle as Boolean){
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                Statified.currentSongHelper?.isShuffle = false
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            }else{
                Statified.currentSongHelper?.isShuffle=true
                Statified.currentSongHelper?.isLoop=false
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("feature", true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            }
        })
        Statified.nextImageButton?.setOnClickListener({
                Statified.currentSongHelper?.isPlaying = true
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
                if (Statified.currentSongHelper?.isShuffle as Boolean) {
                    Staticated.playNext("PlayNextLikeNormalShuffle")
                } else {
                    Staticated.playNext("PlayNextNormal")
                }
        })
        Statified.previousImageButton?.setOnClickListener({
            Statified.currentSongHelper?.isPlaying = true
                if (Statified.currentSongHelper?.isLoop as Boolean) {
                    Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                }
                playPrevious()
                Statified.mediaPlayer?.let { player ->
                    Statified.playPauseImageButton?.setBackgroundResource(
                        if (player.isPlaying) R.drawable.pause_icon else R.drawable.play_icon
                    )
                }
        })
        Statified.loopImageButton?.setOnClickListener({
            var editorShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()
            if (Statified.currentSongHelper?.isLoop as Boolean){
                Statified.currentSongHelper?.isLoop = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            }else{
                Statified.currentSongHelper?.isLoop = true
                Statified.currentSongHelper?.isShuffle = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", true)
                editorLoop?.apply()
            }
        })
        Statified.playPauseImageButton?.setOnClickListener({

            if (Statified.mediaPlayer?.isPlaying as Boolean){
                Statified.mediaPlayer?.pause()
                Statified.currentSongHelper?.isPlaying = false
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            }else{

                Statified.mediaPlayer?.start()
                Statified.currentSongHelper?.isPlaying = true
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
        view?.findViewById<ImageButton>(R.id.playlistAddButton)?.setOnClickListener {
            val database = EchoDatabase(Statified.myActivity)
            val playlists = database.playlistNames()
            if (playlists.isEmpty()) {
                Toast.makeText(Statified.myActivity, "Create a playlist in Playlists first", Toast.LENGTH_SHORT).show()
            } else {
                AlertDialog.Builder(Statified.myActivity).setTitle("Add to playlist")
                    .setItems(playlists.toTypedArray()) { _, index ->
                        val song = Statified.currentSongHelper
                        if (song != null) database.addSongToPlaylist(playlists[index], Songs(song.songId ?: 0L, song.songTitle ?: "", song.songArtist ?: "", song.songPath ?: "", 0))
                    }.show()
            }
        }
        Statified.rewind10ImageButton?.setOnClickListener {
            val player = Statified.mediaPlayer ?: return@setOnClickListener
            player.seekTo((player.getCurrentPosition() - 10_000).coerceAtLeast(0))
        }
        Statified.forward10ImageButton?.setOnClickListener {
            val player = Statified.mediaPlayer ?: return@setOnClickListener
            val target = player.getCurrentPosition() + 10_000
            player.seekTo(if (player.duration > 0) target.coerceAtMost(player.duration) else target)
        }
    }

    fun playPrevious(){
        Statified.currentPosition = Statified.currentPosition - 1
        if (Statified.currentPosition == -1){
            Statified.currentPosition = 0
        }
        if (Statified.currentSongHelper?.isPlaying as Boolean){
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        }else{
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        Statified.currentSongHelper?.isLoop = false
        var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
        Statified.currentSongHelper?.songTitle = nextSong?.songTitle
        Statified.currentSongHelper?.songPath = nextSong?.songData
        Statified.currentSongHelper?.songArtist = nextSong?.artist
        Statified.currentSongHelper?.currentPosition = Statified.currentPosition
        Statified.currentSongHelper?.songId = nextSong?.songID as Long
        Staticated.updateAlbumArt(nextSong?.songData)

        Staticated.updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)

        Statified.mediaPlayer?.stop()
        Statified.mediaPlayer?.reset()
        try {
            Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(Statified.currentSongHelper?.songPath))
            Statified.mediaPlayer?.prepare()
            Statified.mediaPlayer?.start()
            Staticated.processInformation(Statified.mediaPlayer as Media3PlayerAdapter)
        }catch (e: Exception){
            e.printStackTrace()
        }
        if (Statified.favouriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean){
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity, R.drawable.favorite_on))
        }else{
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity, R.drawable.favorite_off))
        }
    }

    fun bindShakeListener(){
        Statified.mSensorListener = object: SensorEventListener{
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

            }

            override fun onSensorChanged(p0: SensorEvent) {
                val x = p0.values[0]
                val y = p0.values[1]
                val z = p0.values[2]

                mAccelarationLast = mAccelarationCurrent
                mAccelarationCurrent = Math.sqrt(((x*x + y*y + z*z).toDouble())).toFloat()
                val delta = mAccelarationCurrent - mAccelarationLast
                mAccelaration = mAccelaration * 0.9f + delta

                if (mAccelaration > 8f) {
                    val prefs = Statified.myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("feature", false)
                    val now = System.currentTimeMillis()
                    if (isAllowed == true && now - lastShakeAt > 1200L){
                        lastShakeAt = now
                        Staticated.playNext("PlayNextNormal")
                        mAccelaration = 0f
                    }
                }
            }
        }
    }
}// Required empty public constructor
