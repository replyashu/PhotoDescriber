package com.ashu.photodescriber.global

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.ashu.photodescriber.repository.db.ImageDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideContext(application: Application): Context = application.applicationContext

    @Singleton
    @Provides
    fun provideImageDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, ImageDatabase::class.java, "img_db").build()

    @Provides
    fun provideImageDao(database: ImageDatabase) = database.imageDao()
}