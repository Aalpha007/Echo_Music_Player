package com.project.echo.adapters

import android.content.Context
import android.support.constraint.R.id.parent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.project.echo.R
import com.project.echo.activities.MainActivity
import com.project.echo.fragments.AboutUsFragment
import com.project.echo.fragments.FavouriteFragment
import com.project.echo.fragments.MainScreenFragment
import com.project.echo.fragments.SettingsFragment

class NavigationDrawerAdapter(_contentList: ArrayList<String>, _getImages: IntArray, _conext: Context): RecyclerView.Adapter<NavigationDrawerAdapter.NavViewHolder>(){
    var contentList: ArrayList<String>?=null
    var getImages: IntArray?=null
    var mContext: Context?=null
    init {
        this.contentList = _contentList
        this.getImages = _getImages
        this.mContext = _conext
    }
    override fun onCreateViewHolder(p0: ViewGroup, viewType: Int): NavViewHolder {
        var itemView = LayoutInflater.from(p0.context)
            .inflate(R.layout.row_custom_navigationdrawer, p0, false)

        val returnThis = NavViewHolder(itemView)
        return returnThis
    }

    override fun getItemCount(): Int {
        return (contentList as ArrayList).size
    }

    override fun onBindViewHolder(holder: NavViewHolder, position: Int) {
        holder.icon_GET?.setBackgroundResource(getImages?.get(position) as Int)
        holder.text_GET?.text = contentList?.get(position)
        holder.contentHolder?.setOnClickListener{
            when (position) {
                0 -> {
                    val mainScreenFragment = MainScreenFragment()
                    (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.details_fragment, mainScreenFragment)
                        .commit()
                }
                1 -> {
                    val favouriteFragment = FavouriteFragment()
                    (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.details_fragment, favouriteFragment)
                        .commit()
                }
                2 -> {
                    val settingsFragment = SettingsFragment()
                    (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.details_fragment, settingsFragment)
                        .commit()
                }
                3 -> {
                    val aboutUsFragment = AboutUsFragment()
                    (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.details_fragment, aboutUsFragment)
                        .commit()
                }
            }
            MainActivity.Statified.drawerLayout?.closeDrawers()
        }
    }

    class NavViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var icon_GET: ImageView?=null
        var text_GET: TextView?=null
        var contentHolder: RelativeLayout?=null
        init {
            icon_GET = itemView.findViewById(R.id.icon_navdrawer)
            text_GET = itemView.findViewById(R.id.text_navdrawer)
            contentHolder = itemView.findViewById(R.id.navdrawer_item_content_holder)
        }
    }

}