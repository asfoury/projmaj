package com.sdp13epfl2021.projmag.activities


import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import android.widget.MediaController
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import com.sdp13epfl2021.projmag.R
import com.sdp13epfl2021.projmag.database.Utils
import com.sdp13epfl2021.projmag.model.ImmutableProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xml.sax.XMLReader
import java.io.File

class ProjectInformationActivity : AppCompatActivity() {

    private var videosUris: List<Uri> = emptyList()
    private var current: Int = -1

    @Synchronized
    private fun addVideo(videoUri: Uri, videoView: VideoView) {
        videosUris = videosUris + videoUri
        if (current < 0) {
            readVideo(0, videoView)
            videoView.pause()
        }
    }

    private fun readVideo(newCurrent: Int, videoView: VideoView) {
        if (newCurrent >= 0 && newCurrent < videosUris.size) {
            current = newCurrent
            videoView.setVideoURI(videosUris[current])
            videoView.start()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_information)

        // get all the text views that will be set
        val title = findViewById<TextView>(R.id.info_project_title)
        val lab = findViewById<TextView>(R.id.info_lab_name)
        val description = findViewById<TextView>(R.id.info_description)
        val nbOfStudents = findViewById<TextView>(R.id.info_nb_students)
        val type = findViewById<TextView>(R.id.info_available_for)
        val responsible = findViewById<TextView>(R.id.info_responsible_name)
        val video = findViewById<VideoView>(R.id.info_video)

        // get the project
        val project: ImmutableProject? = intent.getParcelableExtra("project")
        if (project != null) {
            // set the text views
            title.text = project.name
            lab.text = project.lab

            val projectDir = File(File(filesDir, "projects"), project.id)
            val (cleanDescription, videosLinks) = extractVideos(project.description)
            val imageGetter = ImageGetter2(this, projectDir, description)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                description.text = Html.fromHtml(
                    cleanDescription,
                    Html.FROM_HTML_MODE_LEGACY,
                    imageGetter,
                    TagHandler2()
                )
            } else {
                description.text = Html.fromHtml(cleanDescription, imageGetter, TagHandler2())
            }

            // Allow user to click on link
            description.movementMethod = LinkMovementMethod.getInstance()

            nbOfStudents.text = getString(R.string.display_number_student, project.nbParticipant)
            type.text =
                if (project.bachelorProject && project.masterProject) getString(R.string.display_bachelor_and_master)
                else if (project.bachelorProject) getString(R.string.display_bachelor_only)
                else if (project.masterProject) getString(R.string.display_master_only)
                else getString(R.string.display_not_found)

            responsible.text = project.teacher

            if (videosLinks.isEmpty()) {
                video.isInvisible = true
            } else {
                val controller = AlwaysShownMediaController(this)
                video.setMediaController(controller)
                controller.show()

                // pause/start when we touch the video
                video.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            if (video.isPlaying) {
                                video.pause()
                            } else {
                                video.start()
                            }
                            controller.show(0) //update controller state
                        }
                    }
                    true
                }

                // set the controller buttons to play next/prev video
                controller.setPrevNextListeners({ next ->
                    readVideo(current + 1, video)
                }, { prev ->
                    readVideo(current - 1, video)
                })

                // play the next video when the video is finished
                video.setOnCompletionListener {
                    readVideo(current + 1, video)
                }

                // resize the video view to use full width (with original ratio)
                video.setOnPreparedListener { mp ->
                    val width = resources.displayMetrics.widthPixels
                    val height = (mp.videoHeight / (mp.videoWidth / (width).toFloat())).toInt()
                    video.layoutParams.height = height
                }

                // download all videos
                videosLinks.forEach { link ->
                    Utils.fileDatabase.getFile(link, projectDir, { file ->
                        addVideo(Uri.fromFile(file), video)
                    }, {
                        Toast.makeText(
                            this,
                            "An error occurred while downloading a video.",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                }

            }

        } else {
            Toast.makeText(this, "An error occurred while loading project.", Toast.LENGTH_LONG).show()
        }

        // make the back button in the title bar work
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }


     // return a pair of (text without video tag, and a list of theses videos links)
    private fun extractVideos(text: CharSequence): Pair<String, List<String>> {
        var newText = text
        var links = emptyList<String>()

        val startString = "<video>"
        val endString = "</video>"

        while (newText.isNotEmpty()) {
            val start = newText.indexOf(startString)
            val end = newText.indexOf(endString)
            if (start < 0 || end < 0) {
                break
            }
            links = links + newText.substring(start + startString.length, end)
            newText = newText.removeRange(start, end + endString.length)
        }

        return Pair(newText.toString(), links)
    }
}

private class ImageGetter2(
    private val context: Context,
    private val projectDir: File,
    private val htmlTextView: TextView
) : Html.ImageGetter {

    override fun getDrawable(source: String): Drawable {
        val res = context.resources
        val holder = BitmapDrawablePlaceHolder(res, null)

        Utils.fileDatabase.getFile(source, projectDir, { file ->
            GlobalScope.launch(Dispatchers.IO) {
                val draw = Drawable.createFromPath(file.path)
                if (draw != null) {
                    var width = draw.intrinsicWidth
                    var height = draw.intrinsicHeight

                    val maxWidth = res.displayMetrics.widthPixels
                    if (width > maxWidth) {
                        height = (height / (width / maxWidth.toFloat())).toInt()
                        width = maxWidth
                    }

                    draw.setBounds(0, 0, width, height)
                    holder.setDrawable(draw)
                    holder.setBounds(0, 0, width, height)

                    // force view update
                    withContext(Dispatchers.Main) {
                        htmlTextView.text = htmlTextView.text
                    }
                } else {
                    Toast.makeText(
                        context,
                        "An error occurred while loading image.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }, {
            Toast.makeText(
                context,
                "An error occurred while downloading image.",
                Toast.LENGTH_SHORT
            ).show()
        })
        return holder
    }
}

private class BitmapDrawablePlaceHolder(res: Resources, bitmap: Bitmap?) : BitmapDrawable(res, bitmap) {
    private var drawable: Drawable? = null

    override fun draw(canvas: Canvas) {
        drawable?.run { draw(canvas) }
    }

    fun setDrawable(drawable: Drawable) {
        this.drawable = drawable
    }
}

// This is a normal MediaController but it is never hidden
private class AlwaysShownMediaController(context: Context) : MediaController(context, false) {

    override fun hide() {
        show(0)
    }
}

// This is used to handle some tags unsupported in version 23 (Android 6.0), such as list ul/ol
private class TagHandler2 : Html.TagHandler {

    var parent: String? = null
    var index: Int = 1

    override fun handleTag(
        opening: Boolean,
        tag: String,
        output: Editable,
        xmlReader: XMLReader
    ) {
        when (tag) {
            "ul", "ol" -> parent = if (opening) tag else null
            "li" -> {
                if (opening) {
                    if (parent == "ul")
                        output.append("\n\t•")
                    if (parent == "ol") {
                        output.append("\n\t$index. ")
                        index += 1
                    }
                }
            }
        }
    }
}