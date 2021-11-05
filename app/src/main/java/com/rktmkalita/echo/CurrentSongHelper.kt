package com.rktmkalita.echo

/**
 * Created by R kalita on 10/07/2018.
 */
class CurrentSongHelper{
    var songArtist: String?=null
    var songTitle: String?=null
    var songId: Long=0
    var songPath: String?=null
    var currentPosition: Int=0
    var isPlaying: Boolean = false
    var isLoop: Boolean = false
    var isShuffle: Boolean = false
    var trackPosition: Int = 0
}