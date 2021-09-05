package ir.mirzabazi.mirza.Game.Actions

import ir.mirzabazi.mirza.Game.LVFile
import org.json.JSONObject

class Toast : Activable{
    private val id : Int
    private val text : String
    private val listener : Monitor
    constructor(id : Int , text : String , listener : Monitor){
        this.id = id
        this.text = text
        this.listener = listener
    }

    override fun ACTIVE() {
        /*
        * call toast in listener
        * */
        listener.Toast(id , text)
    }

    override fun INACTIVE() {}

    companion object{
        fun FromJsonObject(id : Int , json: JSONObject , listener : Monitor) : Toast{
            var text = json.getString(LVFile.Companion.FilesTags.Companion.Toast.TAG_MESSAGE)
            return Toast(id , text , listener)
        }
    }
}