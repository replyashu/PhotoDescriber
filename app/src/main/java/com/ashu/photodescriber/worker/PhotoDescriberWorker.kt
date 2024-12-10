//package com.ashu.photodescriber.worker
//
//import android.content.Context
//import androidx.work.Worker
//import androidx.work.WorkerParameters
//import dagger.assisted.Assisted
//import dagger.assisted.AssistedInject
//
//class PhotoDescriberWorker @AssistedInject constructor(
//    @Assisted appContext: Context,
//    @Assisted params: WorkerParameters):
//    Worker(appContext, params) {
//
//    override fun doWork(): Result {
//
//        return Result.success()
//    }
//}