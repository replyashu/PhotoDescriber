package com.ashu.photodescriber.repository.db

import com.ashu.photodescriber.repository.db.dao.ImageDao
import com.ashu.photodescriber.repository.db.entity.UserImages
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ImageRepository @Inject constructor(private val imgDao: ImageDao) {

    suspend fun getImages(): Flow<List<UserImages>> = imgDao.getAllImages()

    suspend fun getImage(path: String): UserImages = imgDao.getImage(path)

    suspend fun insert(path: String, canonical: String) = imgDao.insertImage(UserImages(0, path, canonical, listOf("sample")))

    suspend fun updateImage(imgPath: String, canonical: String) = imgDao.updateImage(imgPath, canonical)

    suspend fun getTag(path: String) = imgDao.getTagsForImage(path)
}