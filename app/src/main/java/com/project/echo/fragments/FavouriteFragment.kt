package com.project.echo.fragments

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.project.echo.R
import com.project.echo.Songs
import com.project.echo.adapters.FavouriteAdapter
import com.project.echo.databases.EchoDatabase
import com.project.echo.fragments.FavouriteFragment.Statified.favoriteContent
import com.project.echo.fragments.FavouriteFragment.Statified.getListfromDatabase
import com.project.echo.fragments.FavouriteFragment.Statified.mediaPlayer
import com.project.echo.fragments.FavouriteFragment.Statified.myActivity
import com.project.echo.fragments.FavouriteFragment.Statified.noFavourites
import com.project.echo.fragments.FavouriteFragment.Statified.nowPlayingBottomBar
import com.project.echo.fragments.FavouriteFragment.Statified.playPauseButton
import com.project.echo.fragments.FavouriteFragment.Statified.recyclerView
import com.project.echo.fragments.FavouriteFragment.Statified.refreshList
import com.project.echo.fragments.FavouriteFragment.Statified.songTitle
import com.project.echo.fragments.FavouriteFragment.Statified.trackPosition

class FavouriteFragment : Fragment() {

    object Statified {
        var myActivity: Activity? = null
        //var getSongList: ArrayList<Songs>? = null
        var noFavourites: TextView? = null
        var nowPlayingBottomBar: RelativeLayout? = null
        var playPauseButton: ImageButton? = null
        var songTitle: TextView? = null
        var recyclerView: RecyclerView? = null
        var trackPosition: Int = 0
        var favoriteContent: EchoDatabase? = null
        var refreshList: ArrayList<Songs>? = null
        var getListfromDatabase: ArrayList<Songs>? = null
        var mediaPlayer: MediaPlayer? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_favourite, container, false)
        activity?.title = "Favourites"
        noFavourites = view?.findViewById(R.id.noFavorites)
        nowPlayingBottomBar = view?.findViewById(R.id.hiddenBarFavScreen)
        songTitle = view?.findViewById(R.id.songTitleFavScreen)
        playPauseButton = view?.findViewById(R.id.playPauseButton)
        recyclerView = view?.findViewById(R.id.favouriteRecycler)
        favoriteContent = EchoDatabase(myActivity)

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = context as Activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        display_favourites_by_searching()
        bottomBarSetup()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        var item = menu?.findItem(R.id.action_sort)
        item?.isVisible = false
    }

    fun getSongsFromPhone(): ArrayList<Songs> {
        var arrayList = ArrayList<Songs>()
        var contentResolver = myActivity?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songUri, null, null, null, null)
        if (songCursor != null && songCursor.moveToFirst()) {
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            while (songCursor.moveToNext()) {
                var currentId = songCursor.getLong(songId)
                var currentTitle = songCursor.getString(songTitle)
                var currentArtist = songCursor.getString(songArtist)
                var currentData = songCursor.getString(songData)
                var currentDate = songCursor.getLong(dateIndex)
                arrayList.add(Songs(currentId, currentTitle, currentArtist, currentData, currentDate))
            }
        }
        return arrayList
    }

    fun bottomBarSetup() {
        try {
            bottomBarClickHandler()
            songTitle?.text = SongPlayingFragment.Statified.currentSongHelper?.songTitle
            SongPlayingFragment.Statified.mediaplayer?.setOnCompletionListener {
                songTitle?.text = SongPlayingFragment.Statified.currentSongHelper?.songTitle
                SongPlayingFragment.Staticated.onSongComplete()
            }
            if (SongPlayingFragment.Statified.mediaplayer?.isPlaying as Boolean) {
                nowPlayingBottomBar?.visibility = View.VISIBLE
            } else {
                nowPlayingBottomBar?.visibility = View.INVISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun bottomBarClickHandler() {
        nowPlayingBottomBar?.setOnClickListener {
            //mediaPlayer = SongPlayingFragment.Statified.mediaplayer
            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist", SongPlayingFragment.Statified.currentSongHelper?.songArtist)
            args.putString("songTitle", SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            args.putString("path", SongPlayingFragment.Statified.currentSongHelper?.songPath)
            args.putInt("songId", SongPlayingFragment.Statified.currentSongHelper?.songId?.toInt() as Int)
            args.putInt(
                "songPosition",
                SongPlayingFragment.Statified.currentSongHelper?.currentPosition?.toInt() as Int
            )
            args.putParcelableArrayList("songData", SongPlayingFragment.Statified.fetchSongs)
            args.putString("FavBottomBar", "success")
            songPlayingFragment.arguments = args
            fragmentManager?.beginTransaction()
                ?.replace(R.id.details_fragment, songPlayingFragment)
                ?.addToBackStack("SongPlayingFragment")
                ?.commit()
        }

        playPauseButton?.setOnClickListener {
            if (SongPlayingFragment.Statified.mediaplayer?.isPlaying as Boolean) {
                SongPlayingFragment.Statified.mediaplayer?.pause()
                trackPosition = SongPlayingFragment.Statified.mediaplayer?.currentPosition as Int
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            } else {
                SongPlayingFragment.Statified.mediaplayer?.seekTo(trackPosition)
                SongPlayingFragment.Statified.mediaplayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        }
    }

    fun display_favourites_by_searching() {
        if (favoriteContent?.checkSize() as Int > 0) {
            refreshList = ArrayList<Songs>()
            getListfromDatabase = favoriteContent?.queryDBList()
            val fetchListfromDevice = getSongsFromPhone()
            if (fetchListfromDevice != null) {
                for (i in 0..fetchListfromDevice.size - 1) {
                    for (j in 0..getListfromDatabase?.size as Int - 1) {
                        if (getListfromDatabase?.get(j)?.songID == fetchListfromDevice.get(i).songID) {
                            refreshList?.add((getListfromDatabase as ArrayList<Songs>)[j])
                        }
                    }
                }
            } else {
            }
            if (refreshList == null) {
                recyclerView?.visibility = View.INVISIBLE
                noFavourites?.visibility = View.VISIBLE
            } else {
                val favouriteAdapter = FavouriteAdapter(refreshList as ArrayList<Songs>, myActivity as Context)
                val mLayoutManager = LinearLayoutManager(activity)
                recyclerView?.layoutManager = mLayoutManager
                recyclerView?.itemAnimator = DefaultItemAnimator()
                recyclerView?.adapter = favouriteAdapter
                recyclerView?.setHasFixedSize(true)
            }
        } else {
            recyclerView?.visibility = View.INVISIBLE
            noFavourites?.visibility = View.VISIBLE
        }

    }
}
