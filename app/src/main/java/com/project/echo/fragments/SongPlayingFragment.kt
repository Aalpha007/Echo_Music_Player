package com.project.echo.fragments


import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.project.echo.CurrentSongHelper
import com.project.echo.R
import com.project.echo.Songs
import com.project.echo.databases.EchoDatabase
import com.project.echo.fragments.SongPlayingFragment.Staticated.onSongComplete
import com.project.echo.fragments.SongPlayingFragment.Staticated.playNext
import com.project.echo.fragments.SongPlayingFragment.Staticated.processInformation
import com.project.echo.fragments.SongPlayingFragment.Statified.MY_PREFS_NAME
import com.project.echo.fragments.SongPlayingFragment.Statified.audioVisualization
import com.project.echo.fragments.SongPlayingFragment.Statified.currentPosition
import com.project.echo.fragments.SongPlayingFragment.Statified.currentSongHelper
import com.project.echo.fragments.SongPlayingFragment.Statified.endTimeText
import com.project.echo.fragments.SongPlayingFragment.Statified.fab
import com.project.echo.fragments.SongPlayingFragment.Statified.favouriteContent
import com.project.echo.fragments.SongPlayingFragment.Statified.fetchSongs
import com.project.echo.fragments.SongPlayingFragment.Statified.glView
import com.project.echo.fragments.SongPlayingFragment.Statified.loopImageButton
import com.project.echo.fragments.SongPlayingFragment.Statified.mAcceleration
import com.project.echo.fragments.SongPlayingFragment.Statified.mAccelerationCurrent
import com.project.echo.fragments.SongPlayingFragment.Statified.mAccelerationLast
import com.project.echo.fragments.SongPlayingFragment.Statified.mediaplayer
import com.project.echo.fragments.SongPlayingFragment.Statified.myActivity
import com.project.echo.fragments.SongPlayingFragment.Statified.nextImageButton
import com.project.echo.fragments.SongPlayingFragment.Statified.playpauseImageButton
import com.project.echo.fragments.SongPlayingFragment.Statified.previousImageButton
import com.project.echo.fragments.SongPlayingFragment.Statified.seekbar
import com.project.echo.fragments.SongPlayingFragment.Statified.shuffleImageButton
import com.project.echo.fragments.SongPlayingFragment.Statified.songArtistView
import com.project.echo.fragments.SongPlayingFragment.Statified.songTitleView
import com.project.echo.fragments.SongPlayingFragment.Statified.startTimeText
import com.project.echo.fragments.SongPlayingFragment.Statified.updateSongTime
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit

class SongPlayingFragment : Fragment() {

    object Statified {
        var myActivity: Activity? = null
        var mediaplayer: MediaPlayer? = null
        var startTimeText: TextView? = null
        var endTimeText: TextView? = null
        var playpauseImageButton: ImageButton? = null
        var previousImageButton: ImageButton? = null
        var nextImageButton: ImageButton? = null
        var loopImageButton: ImageButton? = null
        var seekbar: SeekBar? = null
        var songArtistView: TextView? = null
        var songTitleView: TextView? = null
        var shuffleImageButton: ImageButton? = null
        var currentPosition: Int? = null
        var fetchSongs: ArrayList<Songs>? = null
        var currentSongHelper: CurrentSongHelper? = null
        var audioVisualization: AudioVisualization? = null
        var glView: GLAudioVisualizationView? = null
        var fab: ImageButton? = null
        var favouriteContent: EchoDatabase? = null
        var mSensorManager: SensorManager? = null
        var mSensorListener: SensorEventListener? = null
        var MY_PREFS_NAME = "ShakeFeature"
        var mAcceleration: Float = 0f
        var mAccelerationCurrent: Float = 0f
        var mAccelerationLast: Float = 0f
        var updateSongTime = object : Runnable {
            override fun run() {
                val getcurrent = mediaplayer?.currentPosition
                startTimeText?.text = String.format(
                    "%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(getcurrent?.toLong() as Long),
                    TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getcurrent?.toLong() as Long))
                )
                seekbar?.setProgress(getcurrent?.toInt() as Int)
                Handler().postDelayed(this, 10)
            }
        }
    }

    object Staticated {
        var MY_PREFS_SHUFFLE = "Shuffle feature"
        var MY_PREFS_LOOP = "Loop feature"

        fun onSongComplete() {
            if (currentSongHelper?.isShuffle as Boolean) {
                playNext("PlayNextLikeNormalShuffle")
                currentSongHelper?.isPlaying = true
            } else {
                if (currentSongHelper?.isLoop as Boolean) {
                    currentSongHelper?.isPlaying = true
                    var nextSong = fetchSongs?.get(currentPosition as Int)

                    currentSongHelper?.songTitle = nextSong?.songTitle
                    currentSongHelper?.songPath = nextSong?.songData
                    currentSongHelper?.currentPosition = currentPosition
                    currentSongHelper?.songId = nextSong?.songID as Long

                    updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)

                    mediaplayer?.reset()
                    try {
                        mediaplayer?.setDataSource(myActivity, Uri.parse(currentSongHelper?.songPath))
                        mediaplayer?.prepare()
                        mediaplayer?.start()
                        processInformation(mediaplayer as MediaPlayer)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    playNext("PlayNextNormal")
                    currentSongHelper?.isPlaying = true
                }
            }
            if (favouriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_on))
            } else {
                fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_off))
            }
        }

        fun updateTextViews(songtitle: String, songArtist: String) {
            var songTitleUpdated = songtitle
            var songArtistUpdated = songArtist
            if (songtitle.equals("<unknown>", true)) {
                songTitleUpdated = "unknown"
            }
            if (songArtist.equals("<unknown>", true)) {
                songArtistUpdated = "unknown"
            }
            songTitleView?.text = songTitleUpdated
            songArtistView?.text = songArtistUpdated
        }

        fun processInformation(mediaPlayer: MediaPlayer?) {
            val finalTime = mediaPlayer?.duration
            val startTime = mediaPlayer?.currentPosition
            seekbar?.max = finalTime as Int
            startTimeText?.text = String.format(
                "%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(startTime?.toLong() as Long),
                TimeUnit.MILLISECONDS.toSeconds(startTime.toLong()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()))
            )
            endTimeText?.text = String.format(
                "%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) -
                        TimeUnit.MINUTES.toSeconds((TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong())))
            )

            seekbar?.progress = mediaPlayer.currentPosition

            Handler().postDelayed(updateSongTime, 10)

        }

        fun playNext(check: String) {
            if (check.equals("PlayNextNormal", true)) {
                currentPosition = currentPosition?.plus(1)
            } else if (check.equals("PlayNextLikeNormalShuffle", true)) {
                var randomObject = Random()
                var randomPosition = randomObject.nextInt(fetchSongs?.size?.plus(1) as Int)
                currentPosition = randomPosition
            }
            if (currentPosition == fetchSongs?.size) {
                currentPosition = 0
            }
            currentSongHelper?.isLoop = false
            var nextSong = fetchSongs?.get(currentPosition as Int)
            currentSongHelper?.songTitle = nextSong?.songTitle
            currentSongHelper?.songPath = nextSong?.songData
            currentSongHelper?.currentPosition = currentPosition
            currentSongHelper?.songId = nextSong?.songID as Long

            updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)

            mediaplayer?.reset()
            try {
                mediaplayer?.setDataSource(myActivity, Uri.parse(currentSongHelper?.songPath))
                mediaplayer?.prepare()
                mediaplayer?.start()
                processInformation(mediaplayer as MediaPlayer)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (favouriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_on))
            } else {
                fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_off))
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_song_playing, container, false)
        setHasOptionsMenu(true)
        activity?.title = "Now Playing"
        seekbar = view?.findViewById(R.id.seekBar)
        startTimeText = view?.findViewById(R.id.startTime)
        endTimeText = view?.findViewById(R.id.endTime)
        playpauseImageButton = view?.findViewById(R.id.playPauseButton)
        nextImageButton = view?.findViewById(R.id.nextButton)
        previousImageButton = view?.findViewById(R.id.previousButton)
        loopImageButton = view?.findViewById(R.id.loopButton)
        shuffleImageButton = view?.findViewById(R.id.suffleButton)
        songArtistView = view?.findViewById(R.id.songArtist)
        songTitleView = view?.findViewById(R.id.songTitle)
        glView = view?.findViewById(R.id.visualizer_view)
        fab = view?.findViewById(R.id.favouriteIcon)
        fab?.alpha = 0.8f
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioVisualization = glView as AudioVisualization
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onResume() {
        super.onResume()
        audioVisualization?.onResume()
        Statified.mSensorManager?.registerListener(
            Statified.mSensorListener,
            Statified.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        audioVisualization?.onPause()
        Statified.mSensorManager?.unregisterListener(Statified.mSensorListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        audioVisualization?.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statified.mSensorManager = Statified.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAcceleration = 0.0f
        mAccelerationCurrent = SensorManager.GRAVITY_EARTH
        mAccelerationLast = SensorManager.GRAVITY_EARTH
        bindShakeListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = true
        val item2: MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_redirect -> {
                Statified.myActivity?.onBackPressed()
                return false
            }
        }
        return true

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        favouriteContent = EchoDatabase(myActivity)
        currentSongHelper = CurrentSongHelper()
        currentSongHelper?.isPlaying = true
        currentSongHelper?.isLoop = false
        currentSongHelper?.isShuffle = false

        var path: String? = null
        var _songTitle: String? = null
        var _songArtist: String? = null
        var songId: Long? = null
        try {
            path = arguments?.getString("path")
            _songTitle = arguments?.getString("songTitle")
            _songArtist = arguments?.getString("songArtist")
            songId = (arguments?.getInt("SongId"))?.toLong()
            currentPosition = arguments?.getInt("position")
            fetchSongs = arguments?.getParcelableArrayList("songData")

            currentSongHelper?.songPath = path
            currentSongHelper?.songTitle = _songTitle
            currentSongHelper?.songArtist = _songArtist
            currentSongHelper?.songId = songId
            currentSongHelper?.currentPosition = currentPosition

            Staticated.updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        var fromFavBottomBar = arguments?.get("FavBottomBar") as? String
        if (fromFavBottomBar != null) {
            mediaplayer = FavouriteFragment.Statified.mediaPlayer
        } else {
            mediaplayer = MediaPlayer()
            mediaplayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                mediaplayer?.setDataSource(myActivity, Uri.parse(path))
                mediaplayer?.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaplayer?.start()
        processInformation(mediaplayer as MediaPlayer)

        if (currentSongHelper?.isPlaying as Boolean) {
            playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        mediaplayer?.setOnCompletionListener {
            onSongComplete()
        }
        clickHandler()

        audioVisualization?.linkTo(0)

        var prefsForShuffle = myActivity?.getSharedPreferences(
            Staticated.MY_PREFS_SHUFFLE,
            Context.MODE_PRIVATE
        )
        var isSuffleAllowed = prefsForShuffle?.getBoolean("feature", false)
        if (isSuffleAllowed as Boolean) {
            currentSongHelper?.isShuffle = true
            currentSongHelper?.isLoop = false
            shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        } else {
            currentSongHelper?.isShuffle = false
            shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }

        var prefsForLoop = myActivity?.getSharedPreferences(
            Staticated.MY_PREFS_LOOP,
            Context.MODE_PRIVATE
        )
        var isLoopAllowed = prefsForLoop?.getBoolean("feature", false)
        if (isLoopAllowed as Boolean) {
            currentSongHelper?.isShuffle = false
            currentSongHelper?.isLoop = true
            shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
        } else {
            loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            currentSongHelper?.isLoop = false
        }

        if (favouriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_on))
        } else {
            fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_off))
        }
    }

    fun clickHandler() {

        fab?.setOnClickListener {
            if (favouriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_off))
                favouriteContent?.deleteFavourite(currentSongHelper?.songId?.toInt() as Int)
                Toast.makeText(myActivity, "Removed from favourites", Toast.LENGTH_SHORT).show()
            } else {
                fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_on))
                favouriteContent?.storeAsFavorite(
                    currentSongHelper?.songId?.toInt(), currentSongHelper?.songArtist,
                    currentSongHelper?.songTitle, currentSongHelper?.songPath
                )
                Toast.makeText(myActivity, "Added to favourites", Toast.LENGTH_SHORT).show()
            }
        }

        shuffleImageButton?.setOnClickListener {
            var editorShuffle = myActivity?.getSharedPreferences(
                Staticated.MY_PREFS_SHUFFLE,
                Context.MODE_PRIVATE
            )?.edit()
            var editorLoop = myActivity?.getSharedPreferences(
                Staticated.MY_PREFS_LOOP,
                Context.MODE_PRIVATE
            )?.edit()

            if (currentSongHelper?.isShuffle as Boolean) {
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                currentSongHelper?.isShuffle = false
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            } else {
                currentSongHelper?.isShuffle = true
                currentSongHelper?.isLoop = false
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()

                editorLoop?.putBoolean("feature", true)
                editorLoop?.apply()
            }
        }
        nextImageButton?.setOnClickListener {
            currentSongHelper?.isPlaying = true
            playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            if (currentSongHelper?.isShuffle as Boolean) {
                playNext("PlayNextLikeNormalShuffle")
            } else {
                playNext("PlayNextNormal")
            }
        }
        previousImageButton?.setOnClickListener {
            currentSongHelper?.isPlaying = true
            if (currentSongHelper?.isLoop as Boolean) {
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrevious()
        }
        loopImageButton?.setOnClickListener {

            var editorShuffle = myActivity?.getSharedPreferences(
                Staticated.MY_PREFS_SHUFFLE,
                Context.MODE_PRIVATE
            )?.edit()
            var editorLoop = myActivity?.getSharedPreferences(
                Staticated.MY_PREFS_LOOP,
                Context.MODE_PRIVATE
            )?.edit()

            if (currentSongHelper?.isLoop as Boolean) {
                currentSongHelper?.isLoop = false
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            } else {
                currentSongHelper?.isLoop = true
                currentSongHelper?.isShuffle = false
                loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                shuffleImageButton?.setBackgroundResource((R.drawable.shuffle_white_icon))
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", true)
                editorLoop?.apply()
            }
        }
        playpauseImageButton?.setOnClickListener {
            if (mediaplayer?.isPlaying as Boolean) {
                mediaplayer?.pause()
                currentSongHelper?.isPlaying = false
                playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            } else {
                mediaplayer?.start()
                currentSongHelper?.isPlaying = true
                playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        }
    }

    fun playPrevious() {
        currentPosition = currentPosition?.minus(1)
        if (currentPosition == -1) {
            currentPosition = 0
        }
        if (currentSongHelper?.isPlaying as Boolean) {
            playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        currentSongHelper?.isLoop = false

        var nextSong = fetchSongs?.get(currentPosition as Int)
        currentSongHelper?.songPath = nextSong?.songData
        currentSongHelper?.songTitle = nextSong?.songTitle
        currentSongHelper?.songArtist = nextSong?.artists
        currentSongHelper?.songId = nextSong?.songID as Long

        Staticated.updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)

        mediaplayer?.reset()
        try {
            mediaplayer?.setDataSource(myActivity, Uri.parse(currentSongHelper?.songPath))
            mediaplayer?.prepare()
            mediaplayer?.start()
            processInformation(mediaplayer as MediaPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (favouriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_on))
        } else {
            fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context, R.drawable.favorite_off))
        }
    }

    fun bindShakeListener() {
        Statified.mSensorListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }

            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                mAccelerationLast = mAccelerationCurrent
                mAccelerationCurrent = Math.sqrt(((x * x + y * y + z * z).toDouble())).toFloat()
                val delta = mAccelerationCurrent - mAccelerationLast
                mAcceleration = mAcceleration * 0.9f + delta
                if (mAcceleration > 12) {
                    val prefs = Statified.myActivity?.getSharedPreferences(
                        MY_PREFS_NAME,
                        Context.MODE_PRIVATE
                    )
                    val isAllowed = prefs?.getBoolean("feature", false)
                    if (isAllowed as Boolean) {
                        Staticated.playNext("PlayNextNormal")
                    }
                }
            }

        }
    }

}