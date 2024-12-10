package com.ashu.photodescriber.repository.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ashu.photodescriber.global.Converters
import com.ashu.photodescriber.repository.db.dao.ImageDao
import com.ashu.photodescriber.repository.db.entity.UserImages

@Database(entities = [UserImages::class], version = 1)
@TypeConverters(Converters::class)
abstract class ImageDatabase: RoomDatabase() {

    abstract fun imageDao(): ImageDao
}