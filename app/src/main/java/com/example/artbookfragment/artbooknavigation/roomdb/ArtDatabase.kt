package com.example.artbookfragment.artbooknavigation.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.artbookfragment.artbooknavigation.model.Art

@Database(entities = [Art::class], version = 1)
abstract class ArtDatabase : RoomDatabase() {
    abstract fun ArtDao() : ArtDao
}