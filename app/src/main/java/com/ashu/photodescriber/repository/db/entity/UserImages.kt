package com.ashu.photodescriber.repository.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "user_images", indices = arrayOf(Index(value = ["imgPath"], unique = true)))
data class UserImages (

    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val imgPath: String,
    val canonicalImage: String?,
    val tags: List<String>
)