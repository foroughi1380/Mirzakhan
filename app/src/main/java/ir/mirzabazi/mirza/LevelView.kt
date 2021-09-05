package ir.mirzabazi.mirza

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AlertDialog
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.*
import ir.mirzabazi.mirza.Game.LVFile
import ir.mirzabazi.mirza.Game.ScoreStorage
import java.io.BufferedInputStream
import java.io.File
import java.lang.Exception
import java.net.URL

class LevelView : RelativeLayout {
    //controller value
    private var is_downloading : Boolean
    private var is_showing : Boolean
    private var score : ScoreStorage

    //widgets
    private val image : ImageView
    private val name : TextView
    private val description : TextView
    private val start : Button
    private val min_socore : TextView
    private val imag_text : TextView


    constructor(context: Context, attrs: AttributeSet? = null) : super(context , attrs){
        inflate(context , R.layout.level_view , this)

        // init widgets
        is_downloading = false
        is_showing = false
        score = ScoreStorage(context)
        this.image =  findViewById(R.id.activity_menu_show_level_image)
        this.name =  findViewById(R.id.activity_menu_show_level_name)
        this.description =  findViewById(R.id.activity_menu_show_level_description)
        this.start =  findViewById(R.id.activity_menu_show_level_start_btn)
        this.min_socore = findViewById(R.id.activity_menu_show_level_start_min_score)
        this.imag_text = findViewById(R.id.activity_menu_show_level_image_text)

        //init start button
        this.start.setOnClickListener { startClicked(it.tag as Int) }

        //init description
        this.description.movementMethod = ScrollingMovementMethod()
    }


    //utility functions
    fun show(pro : LevelMenuProfile){
        /*
        * this method fill all text and show
        * */

        //control normal state
        if (is_showing) return

        //set widgets
        this.name.text = pro.name
        this.min_socore.text = "${resources.getString(R.string.activity_menu_min_score_text)} : ${pro.min_score}"
        this.description.text = pro.description
        this.start.tag = pro.sid

        tag = pro.min_score <= score.getScore()

        //animation
        var start_x = width / 2
        var start_y = height

        var animation = ViewAnimationUtils.createCircularReveal(this , start_x , start_y , 0f , start_y.toFloat())
        animation.addListener(object : Animator.AnimatorListener{
            override fun onAnimationStart(animation: Animator?) {
                visibility = View.VISIBLE
                is_showing = true
            }

            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
        })
        animation.duration = 1000
        animation.start()


        //set image

            //values
            var bitmap : Bitmap

            //downloading image
            Thread{
                try {
                    var stream = URL("${MenuActivity.sever_address}/img/${pro.sid}").openStream()
                    bitmap = BitmapFactory.decodeStream(stream)
                    stream.close()

                    //set image
                    handler.post {
                        this.image.setImageBitmap(bitmap)
                    }
                }catch (e:Exception){e.printStackTrace()}
            }.start()

    }
    fun hide(){
        if (is_downloading) return // check normal state
        if (! is_showing) return

        var start_x = width / 2
        var start_y = height

        var animation = ViewAnimationUtils.createCircularReveal(this , start_x , start_y , start_y.toFloat() , 0f)
        animation.addListener(object : Animator.AnimatorListener{
            override fun onAnimationEnd(animation: Animator?) {
                image.setImageDrawable(null)
                visibility = View.INVISIBLE
            }

            override fun onAnimationStart(animation: Animator?) {
                is_showing = false
            }

            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
        })
        animation.duration = 1000
        animation.start()
    }
    fun back(){
        is_downloading =false
        is_showing = true
        this.start.text = resources.getString(R.string.activity_menu_start_button)
        this.imag_text.visibility = View.INVISIBLE
        hide()
    }

    //self utility functinos
    private fun changePercent(p : Int){
        /*
        * change the text of image
        * */
        handler.post {
            this.imag_text.text = "%$p"
        }
    }


    private fun startClicked(sid : Int) {try{
        /*
        * this method download level file and call game activity after download
        * */
        if (is_downloading) return // control the now is downloading file or no
        is_downloading = true

        //check score
        if (! (tag as Boolean) ){
            Toast.makeText(context , context.getString(R.string.activity_menu_min_error_score) , Toast.LENGTH_LONG).show()
            return
        }

        //change button text
        this.start.text = resources.getString(R.string.activity_menu_download_button)


        //downlod level file
        this.imag_text.visibility = View.VISIBLE
        val dir = "${context.filesDir.path}/${GameActivity.dir}"

        //thread for download
        var thread = Thread{try {
            var url = URL("${MenuActivity.sever_address}/level/$sid")
            var url_input = url.openStream()
            var url_conection = url.openConnection()
            url_conection.connect()
            val length = url_conection.contentLength
            var downloaded = 0
            var stream = object : BufferedInputStream(url_input , 8 * 1024){
                override fun read(b: ByteArray): Int{
                    var ret = super.read(b)
                    downloaded += ret
                    changePercent(downloaded * 100 / length)
                    return ret
                }
            }
            LVFile.Companion.ExtractFromStream(stream , File(dir))
            callActivity()
        }catch (e:Exception){handler.post { error() }}}
        thread.priority = Thread.MAX_PRIORITY
        thread.start()
    }catch (e:Exception){handler.post { this.error() }}}

    private fun error() {
        var dialog = AlertDialog.Builder(context).setCancelable(false).setPositiveButton(R.string.activity_start_exit_btn_text , { a, b->a.cancel() ; back()}).setMessage(context.getString(R.string.activity_menu_min_error_download_level))
        dialog.show()
    }

    private fun callActivity() {
        handler.post {
            var i = Intent(context , GameActivity::class.java)
            context.startActivity(i)
        }
    }
}
