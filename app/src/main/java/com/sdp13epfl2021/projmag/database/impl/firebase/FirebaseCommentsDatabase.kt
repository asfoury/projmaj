package com.sdp13epfl2021.projmag.database.impl.firebase

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.sdp13epfl2021.projmag.database.interfaces.CommentsDatabase
import com.sdp13epfl2021.projmag.database.interfaces.ProjectId
import com.sdp13epfl2021.projmag.database.interfaces.UserdataDatabase
import com.sdp13epfl2021.projmag.model.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class FirebaseCommentsDatabase @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val userdataDatabase: UserdataDatabase
) : CommentsDatabase {

    companion object {
        /**
         *  The field containing all the comments to all projects
         */
        const val PROJECT_COMMENTS = "project-comments"
    }

    override fun addCommentToProjectComments(
        message: Message,
        projectId: ProjectId,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        getCommentsOfProject(projectId, { oldMessages ->
            val newMessages: List<Message> = oldMessages + message
            val messagesStrings = newMessages.map(Message::toMapString)
            val docData = hashMapOf(
                "comments" to messagesStrings
            )
            firestore
                .collection(PROJECT_COMMENTS)
                .document(projectId)
                .set(docData)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener(onFailure)

        }, {})
    }

    @Suppress("UNCHECKED_CAST")
    private fun dataListToMessages(dataList: List<*>?): List<Message> {
        return dataList?.mapNotNull { commentData ->
            val commentMap = commentData as? Map<String, Any>
            commentMap?.let {
                val messageContent = it["message"] as? String
                val userId = it["sender"] as? String
                val createdAt = it["creationDate"] as? Long

                if (messageContent != null && userId != null && createdAt != null) {
                    Message(messageContent, userId, createdAt)
                } else {
                    null
                }
            }
        } ?: emptyList()
    }

    override fun getCommentsOfProject(
        projectId: ProjectId,
        onSuccess: (List<Message>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore
            .collection(PROJECT_COMMENTS)
            .document(projectId)
            .get()
            .addOnSuccessListener { doc ->
                val messages = dataListToMessages(doc["comments"] as? List<*>)
                onSuccess(messages)
            }
            .addOnFailureListener(onFailure)
    }

    override fun addListener(
        projectID: ProjectId,
        onChange: (ProjectId, List<Message>) -> Unit
    ) {
        getDoc(projectID)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.data?.let {
                    GlobalScope.launch {
                        val messages = dataListToMessages(it["comments"] as? List<*>)
                        onChange(projectID, messages)
                    }
                }
            }
    }



    private fun getDoc(projectID: ProjectId): DocumentReference {
        return firestore
            .collection(PROJECT_COMMENTS)
            .document(projectID)
    }


}