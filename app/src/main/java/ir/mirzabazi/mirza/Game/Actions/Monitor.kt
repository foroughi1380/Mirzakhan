package ir.mirzabazi.mirza.Game.Actions

interface Monitor{
    fun Show(id : Int , name : String)
    fun Hide()
    fun Toast(id : Int , text : String)
    fun Score(id : Int , name : String)
    fun Finish()
}