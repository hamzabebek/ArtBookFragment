package com.example.artbookfragment.artbooknavigation.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Art(
    @ColumnInfo(name = "name")
    val artName : String,

    @ColumnInfo(name = "artistname")
    val artistName : String?,

    @ColumnInfo(name = "year")
    val year : String?,

    @ColumnInfo(name = "image")
    val image : ByteArray?
) {
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
}