package com.sdp13epfl2021.projmag.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.sdp13epfl2021.projmag.R
import com.sdp13epfl2021.projmag.adapter.MessageListAdapter
import com.sdp13epfl2021.projmag.database.interfaces.CommentsDatabase
import com.sdp13epfl2021.projmag.database.interfaces.ProjectId
import com.sdp13epfl2021.projmag.database.interfaces.UserdataDatabase
import com.sdp13epfl2021.projmag.model.*
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import kotlin.concurrent.thread

@AndroidEntryPoint
class CommentsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageListAdapter
    private lateinit var comments : List<Message>



    @Inject
    lateinit var  commentsDB : CommentsDatabase

    @Inject
    lateinit var userDB: UserdataDatabase

    @Inject
    @Named("currentUserId")
    lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        //get the users profile
        var profile : ImmutableProfile? = null
         userDB.getProfile(userId,{
             if (it != null) {
                 profile = it
             }
        },{})
        // send a message
        // get the project id
        val projectId = intent.extras?.getString("projectId")
        setUpSendButton(profile, projectId)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_comments)
        commentsDB.getCommentsOfProject(projectId!!, {
            this.comments = it
            recyclerView.adapter = MessageListAdapter(commentsDB,this, comments)
            recyclerView.setHasFixedSize(false)
        }, {})

        commentsDB.addListener(projectId) { _: ProjectId, _: List<Message> ->
            this.runOnUiThread {
                commentsDB.getCommentsOfProject(projectId, {
                    this.comments = it
                    recyclerView.adapter = MessageListAdapter(commentsDB,this, comments)
                    recyclerView.setHasFixedSize(false)
                }, {})
            }
        }
    }

    private fun setUpSendButton(profile : ImmutableProfile?, projectId : String?) {
        if(profile != null) {
            val sendCommentButton = findViewById<ImageButton>(R.id.comments_send_button)
            sendCommentButton.setOnClickListener {
                val editTextView = findViewById<EditText>(R.id.comments_edit_text)
                val messageText = editTextView.text.toString()
                when (val message = Message.build(messageText, profile, Date().time) ) {
                    is Success ->
                    {
                        commentsDB.addCommentToProjectComments(message.value, (projectId ?: ""),{}, {})
                        editTextView.text.clear()
                    }
                    is Failure -> Log.d("MYTEST","Error occurred when sending message : ${message.reason}")
                }
            }
        }
    }
}
