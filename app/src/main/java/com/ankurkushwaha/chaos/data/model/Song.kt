package com.ankurkushwaha.chaos.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = false) val id: Long,
    val title: String,
    val artist: String?,
    val album: String,
    val duration: Long,
    val path: String,
    val imageUri: String?,
    var isFavorite: Boolean = false
) : Parcelable
