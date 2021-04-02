package com.sdp13epfl2021.projmag.database

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.sdp13epfl2021.projmag.JavaToKotlinHelper
import com.sdp13epfl2021.projmag.model.ImmutableProject
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.net.URI


/**
 *  Simple tests as this is just an abstraction of the
 *  Firebase/Firestore API
 */
@Suppress("UNCHECKED_CAST")
class FirebaseProjectsDatabaseTest {
    val mockFirebaseFirestore = Mockito.mock(FirebaseFirestore::class.java)
    val mockColRef = Mockito.mock(CollectionReference::class.java)
    val mockDocRef = Mockito.mock(DocumentReference::class.java)
    val mockTaskCol: Task<QuerySnapshot> = Mockito.mock(Task::class.java) as Task<QuerySnapshot>
    val mockTaskDoc: Task<DocumentSnapshot> =
        Mockito.mock(Task::class.java) as Task<DocumentSnapshot>
    val mockTaskVoid: Task<Void> =
        Mockito.mock(Task::class.java) as Task<Void>
    val mockQS: QuerySnapshot = Mockito.mock(QuerySnapshot::class.java)
    val mockDS: DocumentSnapshot = Mockito.mock(DocumentSnapshot::class.java)
    val mockQDS: QueryDocumentSnapshot = Mockito.mock(QueryDocumentSnapshot::class.java)
    val mockQuery: Query = Mockito.mock(Query::class.java)

    val mockListener = Mockito.mock(ListenerRegistration::class.java)

    val ID = "some-id"
    val project = ImmutableProject(
        id = ID,
        name = "Some test project",
        description = "some description",
        tags = listOf("t1", "t2"),
        isTaken = false,
        bachelorProject = false,
        masterProject = true,
        assigned = listOf("s1", "s2"),
        nbParticipant = 2,
        TA = "Some TA",
        teacher = "Some Teacher",
        lab = "some lab"
    )

    private fun newQDSIterator() = object : MutableIterator<QueryDocumentSnapshot> {
        private var nb = 1

        override fun hasNext(): Boolean = nb > 0

        override fun next(): QueryDocumentSnapshot {
            nb -= 1
            return mockQDS
        }

        override fun remove() {}
    }

    @Before
    fun setUpMocks() {

        // --- mockFirebaseFirestore ---
        Mockito
            .`when`(mockFirebaseFirestore.collection(FirebaseProjectsDatabase.ROOT))
            .thenReturn(mockColRef)

        // --- mockColRef ---
        Mockito
            .`when`(mockColRef.get())
            .thenReturn(mockTaskCol)

        Mockito
            .`when`(mockColRef.document(ID))
            .thenReturn(mockDocRef)

        Mockito
            .`when`(
                mockColRef.whereArrayContainsAny(
                    Mockito.anyString(),
                    Mockito.anyListOf(String::class.java)
                )
            )
            .thenReturn(mockQuery)

        Mockito
            .`when`(mockColRef.addSnapshotListener(Mockito.any()))
            .thenReturn(mockListener)

        // --- mockDocRef ---
        Mockito
            .`when`(mockDocRef.get())
            .thenReturn(mockTaskDoc)

        Mockito
            .`when`(
                mockDocRef.update(
                    JavaToKotlinHelper.anyObject<String>(),
                    JavaToKotlinHelper.anyObject()
                )
            )
            .thenReturn(mockTaskVoid)

        // --- mockTaskCol ---
        Mockito
            .`when`(mockTaskCol.addOnSuccessListener(JavaToKotlinHelper.anyObject()))
            .then {
                val a = it.arguments[0] as OnSuccessListener<QuerySnapshot>
                a.onSuccess(mockQS)
                mockTaskCol
            }

        Mockito
            .`when`(mockTaskCol.addOnFailureListener { Mockito.any(OnFailureListener::class.java) })
            .thenReturn(mockTaskCol)

        // --- mockTaskDoc ---
        Mockito
            .`when`(mockTaskDoc.addOnSuccessListener(JavaToKotlinHelper.anyObject()))
            .then {
                val a = it.arguments[0] as OnSuccessListener<DocumentSnapshot>
                a.onSuccess(mockDS)
                mockTaskDoc
            }

        Mockito
            .`when`(mockTaskDoc.addOnFailureListener { Mockito.any(OnFailureListener::class.java) })
            .thenReturn(mockTaskDoc)

        // --- mockTaskDoc ---
        Mockito
            .`when`(mockTaskVoid.addOnSuccessListener(JavaToKotlinHelper.anyObject()))
            .then {
                mockTaskVoid
            }

        Mockito
            .`when`(mockTaskVoid.addOnFailureListener { Mockito.any(OnFailureListener::class.java) })
            .thenReturn(mockTaskVoid)

        // --- mockDS ---
        Mockito.`when`(mockDS.id).thenReturn(project.id)
        Mockito.`when`(mockDS["name"]).thenReturn(project.name)
        Mockito.`when`(mockDS["lab"]).thenReturn(project.lab)
        Mockito.`when`(mockDS["teacher"]).thenReturn(project.teacher)
        Mockito.`when`(mockDS["TA"]).thenReturn(project.TA)
        Mockito.`when`(mockDS["nbParticipant"]).thenReturn(project.nbParticipant.toLong())
        Mockito.`when`(mockDS["assigned"]).thenReturn(project.assigned)
        Mockito.`when`(mockDS["masterProject"]).thenReturn(project.masterProject)
        Mockito.`when`(mockDS["bachelorProject"]).thenReturn(project.bachelorProject)
        Mockito.`when`(mockDS["tags"]).thenReturn(project.tags)
        Mockito.`when`(mockDS["isTaken"]).thenReturn(project.isTaken)
        Mockito.`when`(mockDS["description"]).thenReturn(project.description)
        Mockito.`when`(mockDS["videoURI"]).thenReturn(project.videoURI)

        // --- mockQS ---
        Mockito
            .`when`(mockQS.iterator())
            .thenReturn(newQDSIterator())

        // --- mockQDS ---
        Mockito
            .`when`(mockQDS.id)
            .thenReturn(ID)
        Mockito.`when`(mockQDS["name"]).thenReturn(project.name)
        Mockito.`when`(mockQDS["lab"]).thenReturn(project.lab)
        Mockito.`when`(mockQDS["teacher"]).thenReturn(project.teacher)
        Mockito.`when`(mockQDS["TA"]).thenReturn(project.TA)
        Mockito.`when`(mockQDS["nbParticipant"]).thenReturn(project.nbParticipant.toLong())
        Mockito.`when`(mockQDS["assigned"]).thenReturn(project.assigned)
        Mockito.`when`(mockQDS["masterProject"]).thenReturn(project.masterProject)
        Mockito.`when`(mockQDS["bachelorProject"]).thenReturn(project.bachelorProject)
        Mockito.`when`(mockQDS["tags"]).thenReturn(project.tags)
        Mockito.`when`(mockQDS["isTaken"]).thenReturn(project.isTaken)
        Mockito.`when`(mockQDS["description"]).thenReturn(project.description)
        Mockito.`when`(mockQDS["videoURI"]).thenReturn(project.videoURI)

        // ---  mockQuery ---
        Mockito
            .`when`(mockQuery.get())
            .thenReturn(mockTaskCol)
    }

    @Test
    fun getAllIdsIsCorrect() {
        val db: ProjectsDatabase = FirebaseProjectsDatabase(mockFirebaseFirestore)
        db.getAllIds(
            { list -> assertEquals(listOf(ID), list) },
            {}
        )
    }

    @Test
    fun getProjectFromIdIsCorrect() {
        Mockito.`when`(mockDS.exists()).thenReturn(true)
        val db: ProjectsDatabase = FirebaseProjectsDatabase(mockFirebaseFirestore)
        db.getProjectFromId(
            ID,
            { p -> assertEquals(project, p) },
            {}
        )
    }

    @Test
    fun getProjectFromIdWhenDSDoNotExistsDoNotCrash() {
        Mockito.`when`(mockDS.exists()).thenReturn(false)
        val db: ProjectsDatabase = FirebaseProjectsDatabase(mockFirebaseFirestore)
        db.getProjectFromId(
            ID,
            { p -> assertEquals(null, p) },
            {}
        )
    }

    @Test
    fun getAllProjectsIsCorrect() {
        val db: ProjectsDatabase = FirebaseProjectsDatabase(mockFirebaseFirestore)
        db.getAllProjects(
            { lp -> assertEquals(listOf(project), lp) },
            {}
        )
    }

    @Test
    fun getProjectsFromNameIsCorrect() {
        val db: ProjectsDatabase = FirebaseProjectsDatabase(mockFirebaseFirestore)
        db.getProjectsFromName(
            project.name,
            { lp -> assertEquals(listOf(project), lp) },
            {}
        )
    }

    @Test
    fun getProjectsFromTagsIsCorrect() {
        val db: ProjectsDatabase = FirebaseProjectsDatabase(mockFirebaseFirestore)
        db.getProjectsFromTags(
            project.tags,
            { lp -> assertEquals(listOf(project), lp) },
            {}
        )
    }

    @Test
    fun updateVideoWithProjectWorks() {
        val db: ProjectsDatabase = FirebaseProjectsDatabase(mockFirebaseFirestore)
        db.updateVideoWithProject(
            ID,
            "",
            {},
            {}
        )
    }

    /*
     * No test for deleteProjectWithId(...). It's implementation is only relying on Firebase calls,
     * and is very similar to the other methods.
     */

    @Test
    fun listenersShouldNotCrash() {
        val db: ProjectsDatabase = FirebaseProjectsDatabase(mockFirebaseFirestore)
        val listener: ((ProjectChange) -> Unit) = {}
        db.addProjectsChangeListener(listener)
        db.removeProjectsChangeListener(listener)
        db.removeProjectsChangeListener(listener)
    }
}