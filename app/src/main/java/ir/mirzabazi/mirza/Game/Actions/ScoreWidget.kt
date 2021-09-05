package ir.mirzabazi.mirza.Game.Actions

import ir.mirzabazi.mirza.Game.LVFile
import ir.mirzabazi.mirza.Game.ScoreStorage
import ir.mirzabazi.mirza.Game.WidgetFinder
import org.json.JSONObject

class ScoreWidget : Widget{
    private var listener : Monitor
    private var find = false
    private val score : ScoreStorage.Companion.ScoreSection
    constructor(id : Int , name : String , images : ArrayList<String> , score : ScoreStorage.Companion.ScoreSection ,finder : WidgetFinder,lis : Monitor) : super(id , name , images , finder , lis){
        this.listener = lis
        this.score = score
    }

    override fun Find() {
        /*
        * call score in listener
        * */
        find = true
        INACTIVE()
        score.AddWidgetScore()
        listener.Score(super.id , super.name)
    }

    override fun Loss(){}
    override fun ACTIVE() {
        /*
        * this class most only one find in finder
        * */
        if (! find){
            super.ACTIVE()
        }
    }

    companion object{
        fun FromJsonObject(id : Int , json: JSONObject ,score : ScoreStorage.Companion.ScoreSection ,finder: WidgetFinder , listener: Monitor) : ScoreWidget{
            var name = json.getString(LVFile.Companion.FilesTags.Companion.ScoreWidget.TAG_NAME)
            var images = arrayListOf<String>()
            var array_images = json.getJSONArray(LVFile.Companion.FilesTags.Companion.ScoreWidget.TAG_IMAGES)
            var i = -1
            while (++i < array_images.length()){
                images.add(array_images.getString(i))
            }
            return ScoreWidget(id , name , images , score ,finder , listener)
        }
    }
}