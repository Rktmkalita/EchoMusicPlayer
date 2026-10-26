package com.rktmkalita.echo.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.rktmkalita.echo.R


/**
 * A simple [Fragment] subclass.
 */
class AboutUsFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_about_us, container, false)
        requireActivity().title = "About Us"
        return view
    }

}// Required empty public constructor
