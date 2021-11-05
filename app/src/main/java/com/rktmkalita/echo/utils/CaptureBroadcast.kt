package com.rktmkalita.echo.utils

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.rktmkalita.echo.R
import com.rktmkalita.echo.activities.MainActivity
import com.rktmkalita.echo.fragments.SongPlayingFragment

class CaptureBroadcast: BroadcastReceiver(){
    override fun onReceive(p0: Context?, p1: Intent?) {
        if (p1?.action == Intent.ACTION_NEW_OUTGOING_CALL){
            try {
                try {
                    MainActivity.Statified.notificationManager?.cancel(1870)
                }catch (e: Exception){
                    e.printStackTrace()
                }

                if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){
                    SongPlayingFragment.Statified.mediaPlayer?.pause()
                    SongPlayingFragment.Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }else{
            val tm: TelephonyManager = p0?.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
            when(tm?.callState){
                TelephonyManager.CALL_STATE_RINGING ->{
                    try {
                        MainActivity.Statified.notificationManager?.cancel(1870)
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                    try {
                        if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){
                            SongPlayingFragment.Statified.mediaPlayer?.pause()
                            SongPlayingFragment.Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }
                else->{

                }
            }
        }
    }

}