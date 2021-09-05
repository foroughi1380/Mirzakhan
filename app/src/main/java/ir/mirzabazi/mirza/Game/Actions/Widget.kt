package ir.mirzabazi.mirza.Game.Actions

import ir.mirzabazi.mirza.Game.WidgetFinder
import org.json.JSONObject
import ir.mirzabazi.mirza.Game.LVFile.Companion.FilesTags.Companion.Widget as TAGS
open class Widget : WidgetFinder.Companion.Findable , Activable{
    protected val id : Int
    protected val name : String
    private val finder : WidgetFinder
    private val listener : Monitor
    constructor(id : Int , name : String , images : ArrayList<String> , finder : WidgetFinder , listener : Monitor){
        this.id = id
        this.name = name
        this.finder = finder
        this.listener = listener
        finder.addWidget(id , images , this) // create widget in Widget finder
    }

    override fun Find() {
        /*
        * call show in listener
        * */
        listener.Show(id , name)
    }

    override fun Loss() {
        /*
        * call hide in listener
        * */
        listener.Hide()
    }

    override fun ACTIVE() {
        /*
        * start finding
        * */
        finder.StartTrackWidget(id)
    }

    override fun INACTIVE() {
        /*
        * stop finding
        * */
        finder.StopTrackWidget(id)
    }
    companion object{
        fun FromJsonObject(id : Int , finder : WidgetFinder , json : JSONObject , listener : Monitor) : Widget{
            var name = json.getString(TAGS.TAG_NAME)
            var array_images = json.getJSONArray(TAGS.TAG_IMAGES)
            var images = arrayListOf<String>()
            var i = -1
            while (++i < array_images.length()){
                images.add(array_images.getString(i))
            }

            return Widget(id , name , images , finder , listener)
        }
    }
}