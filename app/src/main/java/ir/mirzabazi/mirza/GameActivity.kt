package ir.mirzabazi.mirza

import android.animation.Animator
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Bundle
import android.os.Environment
import android.os.Vibrator
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewAnimationUtils
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.github.florent37.shapeofview.shapes.BubbleView
import ir.mirzabazi.mirza.Game.GameBoard
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*


class GameActivity : AppCompatActivity(), GameBoard.Companion.GameBoardListener {

    //controller value
    private lateinit var queue_toast : ArrayList<String>
    private var toast_showed : Boolean = false
    private lateinit var  error_dialog : AlertDialog.Builder
    private lateinit var wait_dilaog : Dialog

    //widgets
    private lateinit var score_txt : TextView
    private lateinit var title_txt : TextView
    private lateinit var title_line : View
    private lateinit var help_btn : TextView
    private lateinit var message_box : BubbleView
    private lateinit var message_txt : TextView
    private lateinit var more_btn : TextView
    private lateinit var camera_btn : ImageView
    private lateinit var exit_on_map_txt : TextView
    private lateinit var toast_text : TextView
    private lateinit var camera_animation : View

    //finish widgets
    private lateinit var finish_layout : LinearLayout
    private lateinit var finish_animation : LottieAnimationView
    private lateinit var finish_information : TextView
    private lateinit var finish_message : TextView
    private lateinit var finish_submit : Button

    //workers
    private lateinit var board : GameBoard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // init widgets
        this.score_txt = findViewById(R.id.game_activity_score_txt)
        this.title_txt = findViewById(R.id.game_activity_title_txt)
        this.title_line = findViewById(R.id.game_activity_title_line)
        this.help_btn = findViewById(R.id.game_activity_help_btn)
        this.message_box = findViewById(R.id.game_activity_message_box)
        this.message_txt = findViewById(R.id.game_activity_message_txt)
        this.more_btn = findViewById(R.id.game_activity_more_btn)
        this.camera_btn = findViewById(R.id.game_activity_icon_camera_btn)
        this.exit_on_map_txt = findViewById(R.id.game_activity_txt_exit_on_map)
        this.toast_text = findViewById(R.id.game_activity_toast)
        this.camera_animation = findViewById(R.id.game_activity_camera_animation)

        //init finish widget
        this.finish_layout = findViewById(R.id.game_activity_finish_game_layout)
        this.finish_animation = findViewById(R.id.game_activity_finish_game_animation)
        this.finish_information = findViewById(R.id.game_activity_finish_game_infomation)
        this.finish_message = findViewById(R.id.game_activity_finish_game_message)
        this.finish_submit = findViewById(R.id.game_activity_finish_game_submit)

        //set listeners
        this.help_btn.setOnClickListener(this::help_click)
        this.camera_btn.setOnClickListener(this::camera_click)
        this.more_btn.setOnClickListener(this::more_click)
        this.finish_submit.setOnClickListener { finish() }

        //set other value
        queue_toast = arrayListOf()


        //set error dialog
        this.error_dialog = AlertDialog.Builder(this)
        this.error_dialog.setCancelable(false)
        this.error_dialog.setTitle(R.string.activity_game_dialog_error_title)

        //var dir = "${filesDir.path}/$dir"
        val dir = "/storage/emulated/0/mirza"
        var path = File(dir)
        this.board = GameBoard(this , path , this)
        board.SetShower(findViewById(R.id.game_activity_camera_shower))
        board.init()
    }

    //utility functions
    private fun showFinish(name : String , old_score : Int , new_score : Int , widget_count : Int , answer_cont : Int){
        /*
        * show layout finish game
        * */

        //crate information text
        var text = """
            ${getString(R.string.activity_game_dialog_finish_game_information_name)} : $name
            ${getString(R.string.activity_game_dialog_finish_game_information_old_score)} : $old_score
            ${getString(R.string.activity_game_dialog_finish_game_information_new_score)} : $new_score
            ${getString(R.string.activity_game_dialog_finish_game_information_count_answered)} : $answer_cont
            ${getString(R.string.activity_game_dialog_finish_game_information_count_widgets)} : $widget_count

        """.trimIndent()
        this.finish_information.text = text
        this.finish_information.visibility = View.INVISIBLE
        this.finish_information.visibility = View.INVISIBLE
        this.camera_btn.isEnabled = false
        this.title_txt.setText(R.string.activity_game_dialog_finish_game_title)
        this.title_line.visibility = View.VISIBLE

        // start showing layout
        var animation = ViewAnimationUtils.createCircularReveal(this.finish_layout , 0 , 0 , 0.0f , this.window.decorView.height.toFloat())
        animation.addListener(object : Animator.AnimatorListener{
            override fun onAnimationEnd(animation: Animator?) {
                finish_animation.playAnimation()
                YoYo.with(Techniques.BounceIn).onStart{
                    finish_information.visibility = View.VISIBLE
                }.playOn(finish_information)

                YoYo.with(Techniques.BounceIn).onStart{
                    finish_message.visibility = View.VISIBLE
                }.playOn(finish_message)

                YoYo.with(Techniques.BounceIn).onStart{
                    finish_submit.visibility = View.VISIBLE
                }.playOn(finish_submit)
            }

            override fun onAnimationStart(animation: Animator?) {
                finish_layout.visibility = View.VISIBLE
            }

            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
        })
        animation.start()
    }

    //click listeners
    private fun help_click(view : View){
        YoYo.with(Techniques.Wave).playOn(view)
    }
    private fun camera_click(view : View){
        //tacke aniamtion
        this.camera_animation.visibility = View.VISIBLE
        YoYo.with(Techniques.FadeOut).duration(500).playOn(this.camera_animation)
        YoYo.with(Techniques.FadeIn).duration(500).onEnd{
            this.camera_animation.visibility = View.INVISIBLE
        }.delay(550).playOn(this.camera_animation)

        Thread {
            try {

                var pic_name = Date().toString() + ".jpg"
                var dir =Environment.getExternalStorageDirectory().absolutePath + "/MirzaKhan Pic"

                var dir_file = File(dir) // check exitst dierectory
                if (! dir_file.exists()) dir_file.mkdir()

                val path = dir + "/" + pic_name
                val image = board.tackePickture()
                val stream = FileOutputStream(File(path))

                //rotate image
                val store_buf_bytes = ByteArrayOutputStream()
                image.compressToJpeg(Rect(0, 0, image.width, image.height), 90, store_buf_bytes)
                var bitmap = BitmapFactory.decodeByteArray(store_buf_bytes.toByteArray() , 0 , store_buf_bytes.toByteArray().size)
                val mat = Matrix()
                mat.postRotate(90.0f)
                var right_bitmap =  Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true)

                //store image
                right_bitmap.compress(Bitmap.CompressFormat.JPEG , 90 , stream)
                MediaStore.Images.Media.insertImage(this.contentResolver, path, pic_name, "mirza khan image")


                stream.flush()
                stream.close()

                this.toast_text.handler.post {
                    ShowToast(getString(R.string.activity_game_text_txt_to_tacke_pickture))
                }

            }catch (e: Exception){
                e.printStackTrace()
                this.toast_text.handler.post {
                    ShowToast(getString(R.string.activity_game_text_txt_faild_to_tacke_pickture))
                }
            }

        }.start()
    }
    private fun more_click(view : View){
        //show question
        var id = view.getTag() as Int
        this.board.ShowQuestion(id)
        HideWidgetInformaiton()
    }
    override fun ProviderIsNotEnable() {
        /*
        * show error dialog and close the this activity
        * */
        this.error_dialog.setPositiveButton(R.string.activity_game_dialog_error_button , {dialog, which ->
            dialog.cancel()
            finish()
        })
        this.error_dialog.setMessage(R.string.activity_game_dialog_error_provider_not_enable)
        this.error_dialog.show()
    }
    override fun FaildLoad() {
        /*
        * show error dialog and close the this activity
        * */
        this.error_dialog.setPositiveButton(R.string.activity_game_dialog_error_button , {dialog, which ->
            dialog.cancel()
            finish()
        })
        this.error_dialog.setMessage(R.string.activity_game_dialog_error_faid_load)
        this.error_dialog.show()
    }
    override fun PermisstionDenied() {
        /*
        * show error dialog and close the this activity
        * */
        this.error_dialog.setPositiveButton(R.string.activity_game_dialog_error_button , {dialog, which ->
            dialog.cancel()
            finish()
        })
        this.error_dialog.setMessage(R.string.activity_game_dialog_error_permission_denide)
        this.error_dialog.show()
    }
    override fun ProviderStatusChange(enable: Boolean) {
        this.error_dialog.setPositiveButton(null , null)
        this.error_dialog.setMessage(R.string.activity_game_dialog_error_provider_status_change)
        if (!enable){
            this.wait_dilaog = this.error_dialog.create()
            wait_dilaog.show()
        }else if (this.wait_dilaog?.isShowing){
            this.wait_dilaog?.hide()
        }
    }

    override fun ShowWidgetInformation(name: String, text: String, id_question: Int) {
        // set texts
        this.title_txt.text = name
        this.message_txt.text = text

        //show widgets
        this.title_line.visibility = View.VISIBLE
        this.message_box.visibility = View.VISIBLE
        this.more_btn.visibility = View.VISIBLE

        // set id to button tag
        this.more_btn.tag = id_question

        //start animation
        YoYo.with(Techniques.BounceIn).playOn(this.message_box)
    }
    override fun HideWidgetInformaiton() {
        // set texts
        this.title_txt.text = getString(R.string.activity_game_text_txt_no_title)



        //start animation
        YoYo.with(Techniques.Wobble).onEnd{

            //hide widgets
            this.title_line.visibility = View.INVISIBLE
            this.message_box.visibility = View.INVISIBLE

        }.playOn(this.message_box)
        YoYo.with(Techniques.FadeOut).playOn(this.message_box)
    }
    override fun ShowToast(text: String) {

        //adding to oast_showd
        this.queue_toast.add(text)

        if (toast_showed) return // adding to queue
        toast_showed = true

        // vibrate
        var vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(300)


        Thread{
            var text = queue_toast.get(0)
            do{
                toast_text.handler.post {
                    toast_text.text = text
                    toast_text.visibility = View.VISIBLE
                    YoYo.with(Techniques.FadeIn).playOn(toast_text)
                }
                Thread.sleep(3000)
                toast_text.handler.post {

                    YoYo.with(Techniques.FadeOut).onEnd{
                        toast_text.visibility = View.INVISIBLE
                    }.playOn(toast_text)
                }

                queue_toast.remove(text)
                if (queue_toast.size > 0){
                    text = queue_toast.get(0)
                }
                Thread.sleep(1500)
            }while (queue_toast.size > 0)
            toast_showed = false
        }.start()
    }
    override fun ChangeScore(score: Int) {
        this.score_txt.text = "$score"
        YoYo.with(Techniques.Wave).playOn(this.score_txt)
    }
    override fun ExitOnMap() {
        /*
        * show exit on map text
        * */
        this.exit_on_map_txt.visibility = View.VISIBLE
    }
    override fun EnterInMap() {
        /*
        * hide exit on map text
        * */
        this.exit_on_map_txt.visibility = View.INVISIBLE
    }
    override fun FinishGame(pro: GameBoard.Companion.FinishGameProfile) {
        showFinish(pro.name , pro.old_score , pro.new_score , pro.widget_find_count , pro.question_count_answer)
    }

    override fun onDestroy() {
        this.board.stop()
        super.onDestroy()
    }

    override fun onBackPressed() {
        var dialog = AlertDialog.Builder(this).setTitle(R.string.activity_game_dialog_title_exit)
        dialog.setMessage(R.string.activity_game_dialog_message_exit)
        dialog.setPositiveButton(R.string.activity_game_dialog_button_yes_exit , { dialog, which ->
            super.onBackPressed()
        })
        dialog.setNegativeButton(R.string.activity_game_dialog_button_no_exit , null)
        dialog.show()
    }

    companion object{
        const val dir = "level"
    }
}
