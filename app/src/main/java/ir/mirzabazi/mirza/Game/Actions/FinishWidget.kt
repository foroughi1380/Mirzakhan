package ir.mirzabazi.mirza.Game.Actions

import ir.mirzabazi.mirza.Game.LVFile
import ir.mirzabazi.mirza.Game.WidgetFinder
import org.json.JSONObject

class FinishWidget : Widget{
    private val listener: Monitor
    constructor(id : Int , images: ArrayList<String> , finder : WidgetFinder , listener : Monitor) : super(id , "" , images , finder , listener){
        this.listener = listener
    }

    override fun Find() {
        /*
        * call Finish in listener
        * */
        listener.Finish()
    }

    override fun Loss() {}

    companion object{
        fun FromJsonObject(id : Int , json : JSONObject , finder : WidgetFinder , listener : Monitor): FinishWidget {

            var images = arrayListOf<String>()
            var array_images = json.getJSONArray(LVFile.Companion.FilesTags.Companion.FinishWidget.TAG_IMAGES)
            var i = -1
            while (++i < array_images.length()){
                images.add(array_images.getString(i))
            }
            return FinishWidget(id , images , finder , listener)
        }
    }
}