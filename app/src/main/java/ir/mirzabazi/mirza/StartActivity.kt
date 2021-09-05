package ir.mirzabazi.mirza

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import ir.mirzabazi.mirza.Game.ScoreStorage

class StartActivity : AppCompatActivity() {

    //widgets
    private lateinit var score_txt : TextView
    private lateinit var start_btn : LottieAnimationView
    private lateinit var help_btn : Button
    private lateinit var exit_btn : Button
    private lateinit var start_view : StartView

    //controller
    private lateinit var score : ScoreStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        //init widgets
        this.score_txt = findViewById(R.id.activity_start_score_txt)
        this.start_btn = findViewById(R.id.activity_start_start_button)
        this.help_btn = findViewById(R.id.activity_start_help_button)
        this.exit_btn = findViewById(R.id.activity_start_exit_button)
        this.start_view = findViewById(R.id.activity_start_start_view)

        //init enable
        this.help_btn.isEnabled = false
        this.exit_btn.isEnabled = false
        this.start_view.isEnabled = false
        this.start_btn.isEnabled = false

        // init controller
        this.score = ScoreStorage(this)

        //set listeners
        this.start_btn.setOnClickListener { this.show_menu() }
        this.help_btn.setOnClickListener { this.show_help() }
        this.exit_btn.setOnClickListener { this.exit() }


        //actions
        this.score_txt.text = score.getScore().toString()

        //init start view
        this.start_view.setListener {
            showAnimations()
        }

        //check permission
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            ActivityCompat.requestPermissions(this , ir.mirzabazi.mirza.Game.Permissions.allPermission , 0)
        }else{
            this.start_view.start()
        }
    }

    //controller functions
    private fun showAnimations(){
        /*
        * start start button animation and help exit animation
        * */
        this.start_btn.playAnimation()
        this.start_view.isEnabled = true
        this.start_btn.isEnabled = true

        YoYo.with(Techniques.Bounce).onStart {
            this.exit_btn.visibility = View.VISIBLE
            this.exit_btn.isEnabled = true
        }.playOn(this.exit_btn)

        YoYo.with(Techniques.BounceInDown).onStart {
            this.help_btn.visibility = View.VISIBLE
            this.help_btn.isEnabled = true
        }.playOn(this.help_btn)
    }

    //listener functions
    private fun show_menu(){
        /*
        * show the menu
        * */
        var i = Intent(this , MenuActivity::class.java)
        i.putExtra(MenuActivity.json_name , start_view.json)
        startActivity(i)
    }
    private fun show_help(){
        var dialog = AlertDialog.Builder(this).setCancelable(false).setPositiveButton(R.string.activity_start_exit_btn_text , {a,b->a.cancel()}).setMessage(getString(R.string.activity_start_help_message))
        dialog.show()
    }
    private fun exit(){
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        var ok = true
        grantResults.forEach {
            ok = ok && (it == PackageManager.PERMISSION_GRANTED)
        }

        if (ok){
            this.start_view.start()
        }else{
            Runtime.getRuntime().exit(0)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}
