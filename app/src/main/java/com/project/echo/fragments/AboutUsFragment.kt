package com.project.echo.fragments
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.project.echo.R

class AboutUsFragment : Fragment() {

    var myActivity: Activity? = null
    var tlt: TextView? = null
    var img: ImageView? = null
    var nam: TextView? = null
    var email: TextView? = null
    var clg: TextView? = null
    var ver: TextView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_about_us, container, false)
        tlt = view?.findViewById(R.id.dev)
        img = view?.findViewById(R.id.ivProfile)
        nam = view?.findViewById(R.id.tvName)
        email = view?.findViewById(R.id.tvEmail)
        clg = view?.findViewById(R.id.clg)
        ver = view?.findViewById(R.id.tvVersion)
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

    override fun onResume() {
        super.onResume()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        var item = menu?.findItem(R.id.action_sort)
        item?.isVisible = false
    }


}
