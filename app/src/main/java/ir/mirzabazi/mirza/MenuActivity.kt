package ir.mirzabazi.mirza

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import ir.mirzabazi.mirza.Game.ScoreStorage
import org.json.JSONArray
import org.json.JSONObject

class MenuActivity : AppCompatActivity() , MenuHoderListener{


    //widgets
    private lateinit var score : TextView
    private lateinit var menu : RecyclerView
    private lateinit var animation : LottieAnimationView
    private lateinit var level_view : LevelView

    //values
    private lateinit var score_sotorage : ScoreStorage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        //init widgets
        score = findViewById(R.id.activity_menu_score_txt)
        menu = findViewById(R.id.activity_menu_menu)
        animation = findViewById(R.id.activity_menu_animation)
        level_view = findViewById(R.id.activity_menu_level_view)
        level_view.visibility = View.INVISIBLE

        // init animation
        animation.playAnimation()

        //init menu
        var json = JSONArray(intent.getStringExtra(json_name))
        var adapt = MenuAdapter( this ,  json , this)
        menu.setAdapter(adapt)
        var screen_with = resources.displayMetrics.widthPixels / resources.displayMetrics.density.toInt()
       // menu.layoutManager = GridLayoutManager(this , Math.min((screen_with / (100 * resources.displayMetrics.density.toInt()) ) , 8))
        menu.layoutManager = LinearLayoutManager(this)


        //init other
        this.score_sotorage = ScoreStorage(this)
        this.score.text = score_sotorage.getScore().toString()

    }

    //listeners function
    override fun clicked(levelMenuProfile: LevelMenuProfile) {
        level_view.show(levelMenuProfile)
    }

    override fun onBackPressed() {
        if (level_view.visibility == View.VISIBLE){
            level_view.hide()
        }else{
            super.onBackPressed()
        }
    }

    //other functions


    companion object{
        const val json_name = "json"
        const val sever_address = "http://mirzabazi.ir/mml"

        private class MenuHoder : RecyclerView.ViewHolder {
            private val text : TextView
            private var profile : LevelMenuProfile
            constructor(text : String , view : View , profile: LevelMenuProfile ,listener: MenuHoderListener) : super(view){
                this.text = view.findViewById(R.id.menu_item_view_text)
                this.text.text = text
                this.profile = profile

                //set listener
                view.setOnClickListener {
                    listener.clicked(profile)
                }
            }
            fun change(text : String , profile: LevelMenuProfile){
                this.text.text = text
                this.profile = profile
            }
        }

        private class MenuAdapter(val context : Context, val jsonArray: JSONArray , val listener: MenuHoderListener) : RecyclerView.Adapter<MenuHoder>(){
            private var count = 0
            private val profiles = arrayListOf<LevelMenuProfile>()
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuHoder {
                var view = LayoutInflater.from(context).inflate(R.layout.menu_item_view , parent , false)
                var profile = LevelMenuProfile.fromJson(jsonArray.getJSONObject(count))
                profiles.add(profile)
                return MenuHoder((++count).toString() , view , profile , listener)
            }

            override fun getItemCount(): Int {
                return jsonArray.length() - 1
            }

            override fun onBindViewHolder(holder: MenuHoder?, position: Int) {
                var pro = profiles.getOrNull(position)

                if (pro == null){ // check profile is exitst
                    pro = LevelMenuProfile.fromJson(jsonArray.getJSONObject(position))
                    profiles.add(pro)
                }


                holder?.change((position + 1) .toString() , pro!!)
            }
        }
    }
}

private interface MenuHoderListener{
    fun clicked(levelMenuProfile: LevelMenuProfile)
}

//for storage level data
data class LevelMenuProfile(var sid : Int, var name : String, var description  : String, var min_score : Int){

    companion object{
        const val TAG_SID = "sid"
        const val TAG_NAME = "name"
        const val TAG_DESCRIPRION = "description"
        const val TAG_MIN_SCORE = "score"

        fun fromJson(json: JSONObject) : LevelMenuProfile{
            /*
            * this method create a LevelMenuProfile and fill it's field
            * */

            var sid = json.getInt(TAG_SID)
            var name = json.getString(TAG_NAME)
            var description = json.getString(TAG_DESCRIPRION)
            var min_score = json.getInt(TAG_MIN_SCORE)
            return LevelMenuProfile(sid , name , description , min_score)
        }
    }
}

