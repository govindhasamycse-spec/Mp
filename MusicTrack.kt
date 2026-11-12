package com.example.universalmusicplayer

import android.net.Uri

data class MusicTrack(
    val id: Long,
    val title: String,
    val artist: String,
    val uri: Uri
)
