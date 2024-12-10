package com.ashu.photodescriber.repository.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ashu.photodescriber.repository.db.entity.UserImages
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {

    @Query("Select * from user_images")
    fun getAllImages(): Flow<List<UserImages>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertImage(imgPath: UserImages)

    @Query("Update user_images set canonicalImage = :cannonicalPath where imgPath = :img")
    suspend fun updateImage(img: String, cannonicalPath: String)

    @Query("Select * from user_images where imgPath = :imgLocation")
    suspend fun getImage(imgLocation: String): UserImages

    @Query("Select tags from user_images where imgPath = :imgPath")
    suspend fun getTagsForImage(imgPath: String): MutableList<String>
}