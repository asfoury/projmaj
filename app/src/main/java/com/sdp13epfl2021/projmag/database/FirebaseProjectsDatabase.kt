package com.sdp13epfl2021.projmag.database

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A Firebase Firestore Database of Projects
 */
object FirebaseProjectsDatabase : ProjectsDatabase {
    /**
     * the Root collection in Firebase
     */
    private const val ROOT = "projects"

    /**
     * return a new FireStore instance
     */
    private fun getDB() = FirebaseFirestore.getInstance()

    /**
     * Take a `DocumentSnapshot` from Firebase and return a `Project`
     *
     * @param doc the Firebase document
     * @return a Project built from the given document
     */
    private fun documentToProject(doc: DocumentSnapshot): Project =
        DummyProject(
            name = doc["name"] as String,
            lab = doc["lab"] as String,
            teacher = doc["teacher"] as String,
            TA = doc["TA"] as String,
            nbParticipant = (doc["nbParticipant"] as Long).toInt(),
            assigned = doc["assigned"] as List<String>,
            masterProject = doc["masterProject"] as Boolean,
            bachelorProject = doc["bachelorProject"] as Boolean,
            tags = doc["tags"] as List<String>,
            isTaken = doc["isTaken"] as Boolean,
            description = doc["description"] as String
        )

    override fun getAllIds(onSuccess: (List<ProjectId>) -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = getDB().collection(ROOT)
        docRef
            .get()
            .addOnSuccessListener { query ->
                val project = query
                    .map { doc -> doc.id }
                onSuccess(project)
            }.addOnFailureListener {
                onFailure(it)
            }
    }

    override fun getProjectFromId(
        id: ProjectId,
        onSuccess: (Project) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val docRef = getDB().collection(ROOT).document(id)
        docRef
            .get()
            .addOnSuccessListener { query ->
                val project = if (query.exists()) {
                    documentToProject(query)
                } else {
                    null
                }
                onSuccess(project)
            }.addOnFailureListener {
                onFailure(it)
            }
    }

    override fun getProjectFromIds(
        ids: List<ProjectId>,
        onSuccess: (List<Project>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val docRef = getDB().collection(ROOT)
        docRef
            .get()
            .addOnSuccessListener { query ->
                val project = query
                    .filter { ids.contains(it.id) }
                    .map { doc ->
                        documentToProject(doc)
                    }
                onSuccess(project)
            }.addOnFailureListener {
                onFailure(it)
            }
    }

    override fun getProjectsFromName(
        name: String,
        onSuccess: (List<Project>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val docRef = getDB().collection(ROOT)
            .whereArrayContainsAny("name-search", name.toLowerCase().split(" "))
        docRef
            .get()
            .addOnSuccessListener { query ->
                val project = query.map { documentToProject(it) }
                onSuccess(project)
            }.addOnFailureListener {
                onFailure(it)
            }
    }

    override fun getProjectsFromTags(
        tags: List<String>,
        onSuccess: (List<Project>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        tags.flatMap { tag -> tag.toLowerCase().split(" ") }
        val docRef = getDB().collection(ROOT).whereArrayContains("tags-search", tags)
        docRef
            .get()
            .addOnSuccessListener { query ->
                val project = query.map { documentToProject(it) }
                onSuccess(project)
            }.addOnFailureListener {
                onFailure(it)
            }
    }

    override fun pushProjectWithId(
        project: Project,
        onSuccess: (ProjectId) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        project?.run {
            getDB().collection(ROOT).add(
                hashMapOf(
                    "name" to name,
                    "name-search" to name.toLowerCase().split(" "),
                    "lab" to lab,
                    "lab-search" to lab.toLowerCase(),
                    "teacher" to teacher,
                    "teacher-search" to teacher.toLowerCase().split(" "),
                    "TA" to TA,
                    "TA-search" to TA.toLowerCase(),
                    "nbParticipant" to nbParticipant,
                    "assigned" to assigned,
                    "masterProject" to masterProject,
                    "bachelorProject" to bachelorProject,
                    "tags" to tags,
                    "tags-search" to tags.map { it.toLowerCase() },
                    "isTaken" to isTaken,
                    "description" to description
                )
            )
                .addOnSuccessListener { onSuccess(it.id) }
                .addOnFailureListener { onFailure(it) }
        }
    }

    override fun deleteProjectWithId(
        id: ProjectId,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        getDB()
            .collection(ROOT)
            .document(id)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}