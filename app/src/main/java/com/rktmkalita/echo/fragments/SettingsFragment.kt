package com.rktmkalita.echo.fragments


import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.app.AlertDialog
import com.rktmkalita.echo.playback.EqualizerController
import com.rktmkalita.echo.R


/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : Fragment() {

    var myActivity: Activity?=null
    var shakeSwitch: Switch?=null
    object Statified{
        var MY_PREFS_NAME = "ShakeFeature"
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_settings,container,false)
        requireActivity().title = "Settings"
        shakeSwitch = view?.findViewById(R.id.switchShake)
        view?.findViewById<Button>(R.id.equalizerButton)?.setOnClickListener { showEqualizerDialog() }
        return view
    }

    private fun showEqualizerDialog() {
        val presets = arrayOf("Normal", "Bass Boost", "Rock", "Pop", "Jazz", "Classical", "Vocal", "Custom")
        AlertDialog.Builder(requireContext())
            .setTitle("Equalizer preset")
            .setItems(presets) { _, which ->
                if (presets[which] == "Custom") showCustomEqualizer() else EqualizerController.preset(presets[which])
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showCustomEqualizer() {
        val layout = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 8, 24, 8) }
        val bars = Array(5) { SeekBar(requireContext()).apply { max = 30; progress = 15; minimumHeight = 48 } }
        bars.forEach { layout.addView(it) }
        AlertDialog.Builder(requireContext())
            .setTitle("Custom equalizer")
            .setView(layout)
            .setPositiveButton("Apply") { _, _ ->
                EqualizerController.applyLevels(ShortArray(5) { ((bars[it].progress - 15) * 100).toShort() })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myActivity = context as? Activity
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val prefs = myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)
        shakeSwitch?.isChecked = prefs?.getBoolean("feature", false) ?: false
        shakeSwitch?.setOnCheckedChangeListener({compoundButton, b ->
            if (b){
                val editor = myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME,Context.MODE_PRIVATE)?.edit()
                editor?.putBoolean("feature",true)
                editor?.apply()
            }else{
                val editor = myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME,Context.MODE_PRIVATE)?.edit()
                editor?.putBoolean("feature",false)
                editor?.apply()
            }

        })
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item = menu?.findItem(R.id.action_sort)
        item?.isVisible = false
    }

}// Required empty public constructor
