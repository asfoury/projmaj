package com.sdp13epfl2021.projmag.database

import android.content.Context
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File

class Utils(
    context: Context,
    val userDataDatabase: UserDataDatabase,
    val candidatureDatabase: CandidatureDatabase,
    val fileDatabase: FileDatabase,
    val metadataDatabase: MetadataDatabase,
    val projectsDatabase: CachedProjectsDatabase,
) {

    companion object {
        private var instance: Utils? = null

        @Synchronized
        fun getInstance(
            context: Context,
            reset: Boolean = false,
            userDataDB: UserDataDatabase = UserDataFirebase(Firebase.firestore, Firebase.auth),
            candidatureDB: CandidatureDatabase = FirebaseCandidatureDatabase(Firebase.firestore, Firebase.auth, userDataDB),
            fileDB: FileDatabase = FirebaseFileDatabase(Firebase.storage, Firebase.auth),
            metadataDB: MetadataDatabase = MetadataFirebase(Firebase.firestore),
            projectsDB: CachedProjectsDatabase = CachedProjectsDatabase(
                OfflineProjectDatabase(
                    FirebaseProjectsDatabase(
                        Firebase.firestore
                    ),
                    File(context.filesDir, "projects")
                )
            )
        ): Utils {
            if (instance == null || reset) {
                instance = Utils(context.applicationContext, userDataDB, candidatureDB, fileDB, metadataDB, projectsDB)
            }
            return instance!!
        }
    }
}