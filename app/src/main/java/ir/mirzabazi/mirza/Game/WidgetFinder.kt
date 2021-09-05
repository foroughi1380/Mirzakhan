package ir.mirzabazi.mirza.Game

import android.content.Context
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.widget.FrameLayout
import cn.easyar.*
import cn.easyar.Target


class WidgetFinder : Thread{
    private val camera : CameraDevice
    private val streamer : CameraFrameStreamer
    private var Loop = true
    private val handler : Handler
    private val listener : WidgetFinderListener
    private var last_find : Int? = null
    private val widgets =  hashMapOf<Int,ImageTracker>()
    private val enable_widget = arrayListOf<Int>()
    private val context : Context
    private var curent_widget_listener : Findable? = null

    constructor(context : Context , listener: WidgetFinderListener) : super(){
        //init EasyAR
        var state = Engine.initialize(context as AppCompatActivity , ARKEY)

        // init all target
        this.camera = CameraDevice()
        this.streamer = CameraFrameStreamer()
        this.streamer.attachCamera(camera)
        this.handler = Handler()
        this.listener = listener
        this.context = context
        super.setDaemon(true)
        super.setPriority(Thread.MAX_PRIORITY)
    }

    override fun run() {
        /*
         * this method search for check find widget
         */
        while (Loop){
            if (streamer == null) return
            var frame = streamer.peek().targetInstances()
            var find = false
            for (instance in frame){
                if (instance.status() == TargetStatus.Tracked){
                    val id = instance.target().name().toInt()
                    find = true
                    if (last_find == id) break

                    if (curent_widget_listener != null){
                        curent_widget_listener!!.Loss()
                        curent_widget_listener = null
                    }

                    last_find = id
                    curent_widget_listener = widgets[id]!!.listener
                    handler.post {
                        curent_widget_listener!!.Find()
                    }
                }
            }
            if (! find){
                handler.post {
                    curent_widget_listener?.Loss()
                }
                last_find = null
                curent_widget_listener = null
            }

            // sleep
            try {
                Thread.sleep(300)
            }catch (e:Exception){}
        }
    }
    override fun start(){

        camera.open(CameraDeviceType.Default)
        camera.setSize(Vec2I(1280 , 720))
        camera.start()
        streamer.start()
        if (super.getState() != Thread.State.RUNNABLE){
            super.start()
        }
    }
    fun Stop(){
        Loop = false
        camera.stop()
        streamer.stop()
    }
    fun addWidget(id : Int , images : ArrayList<String> , lis : Findable){
        /*
        * this method add a target to stream
        * */
        var tracker = Companion.ImageTracker(lis)
        for (path in images){
            var json = "{\"images\":[{\"image\" : \"$path\" , \"name\" : \"$id\"}]}"
            var target = ImageTarget()
            target.setup(json , StorageType.Json , "")
            tracker.loadTarget(target , {target , status->
                handler.post{
                    if (! status){ // if image not load
                        listener.faildLoadImage(target)
                    }
                }
            })
        }
        tracker.attachStreamer(streamer)
        widgets.put(id , tracker)

    }
    //fun StartTracking and Stop Tracking
    fun StartTrackWidget(id : Int){
        /*
        * call start function
        * */
        if (! widgets.containsKey(id)){
            listener.IDNotFound()
            return
        }

        if (enable_widget.contains(id)){
            return
        }

        var tracker = widgets.get(id)
        try {
            tracker?.start()
            enable_widget.add(id)
        }catch (e:Exception){}
    }
    fun StopTrackWidget(id : Int){
        /*
        * call start function
        * */
        if (! widgets.containsKey(id)){
            listener.IDNotFound()
            return
        }
        if (! enable_widget.contains(id)) {
            return
        }
        var tracker = widgets.get(id)
        try {
            tracker?.stop()
            enable_widget.remove(id)
        }catch (e:Exception){}
    }

    fun AddShower(layout : FrameLayout){
        /*
        * this function add a WidgetFinderViewPecker to layout
        * */
        var p = WidgetFinderViewPecker(context ,  this)
        layout.addView(p , FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT , FrameLayout.LayoutParams.MATCH_PARENT))
    }

    fun getCamera() : CameraDevice{
        return this.camera
    }
    fun getStreamer() : CameraFrameStreamer{
        return streamer
    }

    companion object{
        private val ARKEY = "NiQw7HCIRoNriczz6QPtUPeYeWM0g2QmdnlMYLdAOvBh7cPARrNTvNQ2s3p0r6T0l8NHv0HCArE82eT408wfiUU44Hepq9z2VwCNeKVriuJrH0q4sN2RCOCm8w80UwRXCo9HYPEmr6sXqKnRTFGekIiT2aYSOyJagiuihiN2ZrC0GbjAqTQJCn3wt0RXOARrqxjEZhfS";

        interface WidgetFinderListener{
            fun faildLoadImage(target: Target)
            fun IDNotFound()
        }
        interface Findable{
            fun Find()
            fun Loss()
        }
        data class ImageTracker(var listener : Findable) : cn.easyar.ImageTracker()
    }
}