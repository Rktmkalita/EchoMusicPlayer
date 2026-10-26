package com.rktmkalita.echo

import com.rktmkalita.echo.Database.EchoDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class PlaylistDatabaseTest {
    private lateinit var database: EchoDatabase

    @Before
    fun setUp() {
        database = EchoDatabase(RuntimeEnvironment.getApplication())
        database.writableDatabase
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun newPlaylistStartsEmpty() {
        database.createPlaylist("Empty")

        assertTrue(database.playlistSongs("Empty").isEmpty())
    }

    @Test
    fun playlistContainsOnlySongsAddedToIt() {
        database.createPlaylist("Workout")
        database.createPlaylist("Study")
        val first = Songs(1, "First", "Artist", "/music/first.mp3", 0)
        val second = Songs(2, "Second", "Artist", "/music/second.mp3", 0)

        database.addSongToPlaylist("Workout", first)
        database.addSongToPlaylist("Study", second)

        assertEquals(listOf(1L), database.playlistSongs("Workout").map { it.songID })
        assertEquals(listOf(2L), database.playlistSongs("Study").map { it.songID })
    }

    @Test
    fun sameSongCanBelongToMultiplePlaylistsWithoutDuplicates() {
        database.createPlaylist("A")
        database.createPlaylist("B")
        val song = Songs(7, "Shared", "Artist", "/music/shared.mp3", 0)

        database.addSongToPlaylist("A", song)
        database.addSongToPlaylist("A", song)
        database.addSongToPlaylist("B", song)

        assertEquals(1, database.playlistSongs("A").size)
        assertEquals(1, database.playlistSongs("B").size)
        assertTrue(database.playlistSongs("A").first().songID == 7L)
    }
}
