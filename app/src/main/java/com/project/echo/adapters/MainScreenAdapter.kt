package com.project.echo.adapters

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.project.echo.R
import com.project.echo.Songs
import com.project.echo.activities.MainActivity
import com.project.echo.fragments.MainScreenFragment
import com.project.echo.fragments.SongPlayingFragment
import kotlinx.android.synthetic.main.row_custom_mainscreen_adapter.view.*

class MainScreenAdapter(_songDetails: ArrayList<Songs>, _context: Context): RecyclerView.Adapter<MainScreenAdapter.MyViewHolder>(){

    var songDetails: ArrayList<Songs>? = null
    var mContext: Context? = null

    init {
        songDetails = _songDetails
        mContext = _context
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val songObject = songDetails?.get(position)

        holder.trackTitle?.text = songObject?.songTitle
        holder.trackArtist?.text = songObject?.artists

        holder.contentHolder?.setOnClickListener {
                SongPlayingFragment.Statified.mediaplayer?.stop()
                val songPlayingFragment = SongPlayingFragment()
                var args = Bundle()
                args.putString("songArtist", songObject?.artists)
                args.putString("path", songObject?.songData)
                args.putString("songTitle", songObject?.songTitle)
                args.putInt("SongId", songObject?.songID?.toInt() as Int)
                args.putInt("songPosition", position)
                args.putParcelableArrayList("songData", songDetails)
                songPlayingFragment.arguments = args
                (mContext as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, songPlayingFragment)
                    .addToBackStack("SongPlayingFragment")
                    .commit()
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
        val itemView = LayoutInflater.from(p0.context)
            .inflate(R.layout.row_custom_mainscreen_adapter, p0, false)

        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        if (songDetails == null){
            return 0
        }
        else {
            return (songDetails as ArrayList<Songs>).size
        }
    }

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
        var trackTitle: TextView? = null
        var trackArtist: TextView? = null
        var contentHolder: RelativeLayout? = null

        init {
            trackTitle = view.findViewById(R.id.trackTitle) as TextView
            trackArtist = view.findViewById(R.id.trackArtist) as TextView
            contentHolder = view.findViewById(R.id.contentRow) as RelativeLayout
        }
    }
}