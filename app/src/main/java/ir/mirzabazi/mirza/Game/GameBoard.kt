package ir.mirzabazi.mirza.Game

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.YuvImage
import android.widget.FrameLayout
import cn.easyar.Target
import ir.mirzabazi.mirza.Game.Actions.*
import ir.mirzabazi.mirza.Question.Question
import ir.mirzabazi.mirza.R
import org.json.JSONObject
import java.io.File
import java.lang.Exception
import ir.mirzabazi.mirza.Game.LVFile.Companion.FilesTags as TAGS

class GameBoard : WidgetFinder.Companion.WidgetFinderListener,
    Location.Companion.LocationListener, LVFile.Companion.LVFileListener, Monitor {

    //controler value
    private var InMap : Boolean = true

    private val finder : WidgetFinder
    private val question : Question
    private val location : Location
    private val score_storage : ScoreStorage
    private val lv_file : LVFile
    private val score : ScoreStorage.Companion.ScoreSection
    private val level_information : LVFile.Companion.LevelProfile
    private val squares = hashMapOf<String , LocationSquare>()
    private var active_squares = arrayListOf<LocationSquare>()
    private val map_id_question = hashMapOf<Int , Int>()

    private var is_show_information = false
    private var last_widget_information_name = ""
    private var widget_find_count = 0

    private val listener : GameBoardListener
    private val context : Context


    constructor(context : Context, level_path:File, listener: GameBoardListener){
        this.listener = listener
        this.context = context
        this.lv_file = LVFile(level_path, this)
        this.level_information = lv_file.getLevelInformation()
        this.score_storage = ScoreStorage(context)
        this.score = score_storage.OpenSection(level_information.id)
        this.finder = WidgetFinder(context , this)
        this.question = Question(context , lv_file , score , {ChangeScore()})
        this.location = Location(context , this)
    }

    //init method
    fun init(){
        start()
        loadWidgets()
    }
    private fun loadWidgets() {try{
        /*
        * this method load a json file and crate all widgets
        * */

        var widgets : ArrayList<JSONObject> = lv_file.getWidgetsJson()
        var id = 0 // for sale id for each

        for (json in widgets){
            //get public information
            var lat = json.getDouble(TAGS.TAG_LATITUDE)
            var lon = json.getDouble(TAGS.TAG_LONGITUDE)
            var type = json.getString(TAGS.TAG_TYPE)
            var activable : Activable = when(type){
                TAGS.Companion.Widget.TYPE ->{
                    var question_id = json.getInt(TAGS.Companion.Widget.TAG_QUESTION_ID)
                    map_id_question.put(id , question_id)
                    Widget.FromJsonObject(id , finder , json , this)
                }
                TAGS.Companion.ScoreWidget.TYPE -> ScoreWidget.FromJsonObject(id , json , score , finder , this)
                TAGS.Companion.Toast.TYPE -> ir.mirzabazi.mirza.Game.Actions.Toast.FromJsonObject(id , json , this)
                TAGS.Companion.FinishWidget.TYPE -> FinishWidget.FromJsonObject(id , json , finder , this)
                else -> object : Activable{ override fun ACTIVE() {} override fun INACTIVE() {} }

            }

            var name_square = "${(lat * level_information.coefficient.lat).toInt()}:${(lon * level_information.coefficient.Lon).toInt()}"
            var square : LocationSquare
            if (squares.containsKey(name_square)){
                square = squares.get(name_square)!!
            }else{
                square = LocationSquare()
                squares.put(name_square , square)
            }

            square.add(activable)
        }
    }catch (e : Exception){stop() ; listener.FaildLoad()}}


    //Active and Inavtive fustions
    private fun start(){
        var permission = object : IPermissionCheckListener{
            override fun PermissionDenied() {
                this@GameBoard.PermissionDenied()
            }

            override fun PermissionGranted() {
                this@GameBoard.PermissionGranted()
            }
        }
        ir.mirzabazi.mirza.Game.Permissions.CheckAllPermission(context , permission)
        location.StartGet(permission)
        finder.start()
    }
    fun stop(){
        location.StopGet()
        finder.Stop()

    }

    fun tackePickture() : YuvImage{
        /*
        * send last Yuv input image from camera
        * */

        var array = finder.getStreamer().peek().images()
        var index = Math.max(array.lastIndex - 1 , 0)
        var image = array.get(index)

        val bytes = ByteArray(this.finder.getStreamer().peek().images()[index].buffer().size())
        this.finder.getStreamer().peek().images()[index].buffer().copyTo(bytes, 0)

        var yuv = YuvImage(bytes, ImageFormat.NV21, image.width(), image.height(), null)

        return yuv
    }

    //other function
    fun ShowQuestion(id : Int){
        var questin = map_id_question.get(id)
        if (questin != null){
            question.showQuestion(questin)
        }
    }
    fun ChangeScore(){
        /*
        * get new score and call change score in listener
        * */
        var new_score = score.getScore()
        listener.ChangeScore(new_score)
    }
    fun SetShower(layout : FrameLayout){
        finder.AddShower(layout)
    }

    //permission function
    fun PermissionDenied(){
        stop()
        listener.PermisstionDenied()
    }
    fun PermissionGranted(){/*do no things*/}


    //Widget Finder function
    override fun faildLoadImage(target: Target) {
        stop()
        listener.FaildLoad()
    }
    override fun IDNotFound() { /*do not things*/}



    // location function
    override fun LocationChange(lat: Double, lon: Double) {
        /*
        * change location to new time
        * */
        var cur_lat  = (lat * level_information.coefficient.lat).toInt()
        var cur_lon = (lon * level_information.coefficient.Lon).toInt()
        var new_active = arrayListOf<LocationSquare>()
        var InMap = false

        for (lat_sum in -1..1){
            var lat = cur_lat + lat_sum

            for (lon_sum in -1..1){
                var lon = cur_lon + lon_sum
                var name = "$lat:$lon"

                if (squares.contains(name)){
                    var square = squares.get(name)

                    if (! active_squares.contains(square)){
                        square?.ACTIVE()
                        new_active.add(square!!)
                        active_squares.remove(square)
                    }else{
                        new_active.add(square!!)
                        active_squares.remove(square)
                        continue
                    }
                    InMap = true
                }

            }

        }

        if (InMap){ // check out in map
            if (! this.InMap){
                this.InMap = true
                listener.EnterInMap()
            }
        }else{
            if (this.InMap){
                listener.ExitOnMap()
                this.InMap = false
            }
        }

        active_squares.forEach { it.INACTIVE() }
        active_squares = new_active

    }
    override fun providersNotEnable() {
        stop()
        listener.ProviderIsNotEnable()
    }
    override fun providerDisabled() {
        listener.ProviderStatusChange(false)
    }
    override fun providerEnable() {
        listener.ProviderStatusChange(true)
    }

    // LV functions
    override fun InvalidPath() {
        stop()
        listener.FaildLoad()
    }
    override fun InvalidInfoFile() {
        InvalidPath()
    }
    override fun TextLoaded(text: String, id: Int) {
        /*
        * call show in listener to show a information
        * */
        if (is_show_information) return // if information is now showing
        listener.ShowWidgetInformation(last_widget_information_name , text , id)
    }
    override fun TextFileNotFound() {/*No actions*/}
    override fun TextException(e: Exception) {/*No actions*/}
    override fun QuestionLoaded(quastion: LVFile.Companion.QuestionProfile, id: Int) {/*the question do this actions*/}
    override fun QuastionFileNotFound() {/*the question do this actions*/}
    override fun QuestionException(e: Exception) {/*the question do this actions*/}

    //Monitor functions
    override fun Show(id: Int, name: String) {
        last_widget_information_name = name
        lv_file.loadText(id)
    }
    override fun Hide() {
        /*
        * hide information
        * */
        is_show_information = false
        listener.HideWidgetInformaiton()
    }
    override fun Toast(id: Int, text: String) {
        /*
        * call Toast in listener to show it
        * */
        listener.ShowToast(text)
    }
    override fun Score(id: Int, name: String) {
        widget_find_count++
        ChangeScore()
        listener.ShowToast("${context.getString(R.string.activity_game_score_text_game_board)} $name")
    }
    override fun Finish() {
        /*
        * gamer find the finish score and now game is end
        * */
        score.AddFinishScore()
        score.Store(score_storage)
        var new_score = score.getScore()
        var old_score = score.lastScore
        var question_count_answer = question.getAnswerCount()

        var pro = FinishGameProfile(level_information.name , old_score , new_score , question_count_answer , widget_find_count)
        stop()
        listener.FinishGame(pro)
    }


    companion object{
        interface GameBoardListener{
            fun ProviderIsNotEnable()
            fun FaildLoad()
            fun PermisstionDenied()
            fun ProviderStatusChange(enable : Boolean)
            fun ShowWidgetInformation(name : String , text : String , id_question : Int)
            fun HideWidgetInformaiton()
            fun ShowToast(text: String)
            fun FinishGame(pro : FinishGameProfile)
            fun ChangeScore(score : Int)
            fun ExitOnMap()
            fun EnterInMap()
        }

        interface IPermissionCheckListener{
            fun PermissionDenied()
            fun PermissionGranted()
        }
        data class FinishGameProfile(var name : String , var old_score : Int , var new_score : Int , var question_count_answer : Int , var widget_find_count : Int)
    }
}

private class LocationSquare : Activable{
    private val actions = arrayListOf<Activable>()
    private var enable = false
    fun add(action: Activable){
        /*
        * this method add a action
        * */
        actions.add(action)
    }
    fun remove(action: Activable){
        /*
        * this method remove a actions
        * */
        actions.remove(action)
    }


    override fun ACTIVE() {
        /*
        * active all actions
        * */
        if (enable) return

        actions.forEach {
            it.ACTIVE()
        }
        enable = true
    }

    override fun INACTIVE() {
        /*
        * disable all actions
        * */
        if (! enable) return
        actions.forEach {
            it.INACTIVE()
        }
        enable = false
    }
}