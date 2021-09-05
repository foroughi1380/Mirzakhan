package ir.mirzabazi.mirza

import android.animation.Animator
import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.widget.RelativeLayout
import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import ir.mirzabazi.mirza.Game.GameBoard
import java.io.BufferedReader
import java.lang.Exception
import java.net.URL
import java.security.Permission

class StartView(context: Context, attrs: AttributeSet? = null) : RelativeLayout(context, attrs),
    GameBoard.Companion.IPermissionCheckListener , Runnable {

    //widgets
    private val progress : LottieAnimationView
    private var thread : Thread? = null

    //values
    public var json = "[]"
    private var listener : (() -> Unit)? = null
    init {
        inflate(context , R.layout.start_view , this)
        progress = findViewById(R.id.start_view_progress)


    }

    //start function
    fun start(){
        thread = Thread(this@StartView)
        thread?.start()


        //set listener
        /*progress.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {

            }

            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
        })*/
    }

    override fun run() {
        try {
            Thread.sleep(5000)
            ir.mirzabazi.mirza.Game.Permissions.CheckAllPermission(context , this)
        }catch (e:Exception){

        }
    }


    //listeners functions
    override fun PermissionDenied() {
        try {
            handler.post { NO() }
            thread?.stop()
        }catch (e:Exception){}
    }

    override fun PermissionGranted() {
        try {
            var path = "${MenuActivity.sever_address}/$INFO_FILE_NAME"
            var text  = URL(path).openStream().bufferedReader().readText()
            json = text
            handler.post {
                this.OK()
            }
        }catch (e:Exception){
            handler.post {
                Toast.makeText(context , context.getText(R.string.start_error) , Toast.LENGTH_LONG).show()
                this.NO()
            }
        }
    }



    //utility functions
    private fun OK(){
        this.animate().scaleXBy(2f).scaleYBy(2f).setDuration(3000).start()
        YoYo.with(Techniques.FadeOut).onEnd{
            this.visibility = View.GONE
            listener?.let { it() }
        }.playOn(this)

    }
    private fun NO(){
        /*
        * show the dialog error for exit
        * */
        var dialog = AlertDialog.Builder(context).setMessage(R.string.activity_start_get_error).setPositiveButton(R.string.activity_start_exit_btn_text , {a ,b->
            a.cancel()
            Runtime.getRuntime().exit(0)
        })
        dialog.setCancelable(false)
        dialog.show()
    }

    //other function
    fun setListener(listener : () -> Unit){
        this.listener = listener
    }

    companion object{
        const val INFO_FILE_NAME = "info"
    }
}