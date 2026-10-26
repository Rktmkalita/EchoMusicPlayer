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
import android.widget.RadioGroup
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
    var equalizerSwitch: Switch?=null
    object Statified{
        var MY_PREFS_NAME = "ShakeFeature"
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_settings,container,false)
        requireActivity().title = "Settings"
        equalizerSwitch = view?.findViewById(R.id.switchEqualizer)
        val equalizerPresets = view?.findViewById<RadioGroup>(R.id.equalizerPresets)
        val prefs = requireContext().getSharedPreferences("AudioSettings", Context.MODE_PRIVATE)
        equalizerSwitch?.isChecked = prefs.getBoolean("enabled", false)
        EqualizerController.setEnabled(equalizerSwitch?.isChecked == true)
        val savedPreset = prefs.getString("preset", "Normal")
        if (equalizerSwitch?.isChecked == true && savedPreset != "Custom") {
            EqualizerController.preset(savedPreset ?: "Normal")
        }
        equalizerPresets?.visibility = if (equalizerSwitch?.isChecked == true) View.VISIBLE else View.GONE
        val presetIds = mapOf("Normal" to R.id.presetNormal, "Bass Boost" to R.id.presetBass, "Rock" to R.id.presetRock, "Pop" to R.id.presetPop, "Jazz" to R.id.presetJazz, "Classical" to R.id.presetClassical, "Vocal" to R.id.presetVocal, "Custom" to R.id.presetCustom)
        presetIds[savedPreset]?.let { equalizerPresets?.check(it) }
        equalizerSwitch?.setOnCheckedChangeListener { _, enabled ->
            prefs.edit().putBoolean("enabled", enabled).apply()
            EqualizerController.setEnabled(enabled)
            equalizerPresets?.visibility = if (enabled) View.VISIBLE else View.GONE
        }
        equalizerPresets?.setOnCheckedChangeListener { _, checkedId ->
            val selected = presetIds.entries.firstOrNull { it.value == checkedId }?.key ?: return@setOnCheckedChangeListener
            if (selected == "Custom") showCustomEqualizer() else {
                EqualizerController.preset(selected)
                prefs.edit().putString("preset", selected).apply()
            }
        }
        return view
    }

    private fun showEqualizerDialog() {
        val presets = arrayOf("Normal", "Bass Boost", "Rock", "Pop", "Jazz", "Classical", "Vocal", "Custom")
        AlertDialog.Builder(requireContext())
            .setTitle("Equalizer preset")
            .setItems(presets) { _, which ->
                if (presets[which] == "Custom") showCustomEqualizer() else {
                    EqualizerController.preset(presets[which])
                    requireContext().getSharedPreferences("AudioSettings", Context.MODE_PRIVATE).edit()
                        .putString("preset", presets[which]).apply()
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showCustomEqualizer() {
        val layout = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 8, 24, 8) }
        val prefs = requireContext().getSharedPreferences("AudioSettings", Context.MODE_PRIVATE)
        val saved = prefs.getString("custom", "0,0,0,0,0")!!.split(",").map { it.toIntOrNull() ?: 0 }
        val bars = Array(5) { index -> SeekBar(requireContext()).apply { max = 30; progress = (saved[index] / 100 + 15).coerceIn(0, 30); minimumHeight = 48 } }
        bars.forEach { layout.addView(it) }
        AlertDialog.Builder(requireContext())
            .setTitle("Custom equalizer")
            .setView(layout)
            .setPositiveButton("Apply") { _, _ ->
                val levels = ShortArray(5) { ((bars[it].progress - 15) * 100).toShort() }
                EqualizerController.applyLevels(levels)
                prefs.edit().putString("preset", "Custom").putString("custom", levels.joinToString(",")).apply()
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
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item = menu?.findItem(R.id.action_sort)
        item?.isVisible = false
    }

}// Required empty public constructor
