package com.rktmkalita.echo.adapters

import android.content.Context
import com.rktmkalita.echo.Songs

class PlaylistSongsAdapter(
    songs: List<Songs>,
    context: Context
) : MainScreenAdapter(ArrayList(songs), context)
