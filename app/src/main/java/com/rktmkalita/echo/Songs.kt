package com.rktmkalita.echo

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by R kalita on 10/07/2018.
 */
class Songs(var songID: Long, var songTitle: String, var artist: String, var songData: String, var dateAdded: Long): Parcelable {
    private constructor(parcel: Parcel) : this(
            parcel.readLong(), parcel.readString() ?: "", parcel.readString() ?: "",
            parcel.readString() ?: "", parcel.readLong())

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeLong(songID)
        p0.writeString(songTitle)
        p0.writeString(artist)
        p0.writeString(songData)
        p0.writeLong(dateAdded)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<Songs> {
            override fun createFromParcel(parcel: Parcel) = Songs(parcel)
            override fun newArray(size: Int) = arrayOfNulls<Songs>(size)
        }
    }

    object Statified {
        var nameComparator: Comparator<Songs> = Comparator<Songs> { song1, song2 ->
            val songOne = song1.songTitle.uppercase()
            val songTwo = song2.songTitle.uppercase()
            songOne.compareTo(songTwo)
        }

        var dateComparator: Comparator<Songs> = Comparator<Songs> { song1, song2 ->
            val songOne = song1.dateAdded.toDouble()
            val songTwo = song2.dateAdded.toDouble()
            songTwo.compareTo(songOne)
        }
    }

}
