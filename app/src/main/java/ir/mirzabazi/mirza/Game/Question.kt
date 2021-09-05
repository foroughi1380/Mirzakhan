package ir.mirzabazi.mirza.Question

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.support.design.widget.BottomNavigationView
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import ir.mirzabazi.mirza.Game.LVFile
import ir.mirzabazi.mirza.Game.ScoreStorage
import ir.mirzabazi.mirza.R
import java.lang.Exception

class Question : LVFile.Companion.LVFileListener , PagerAdapter, ViewPager.OnPageChangeListener {

    //text shower
    private var text : TextShower?
    private var question : QuestionShower?

    private val answered = arrayListOf<Int>()
    private var answerd_listener : (()->Unit)?
    private val score : ScoreStorage.Companion.ScoreSection
    private val dialog : Dialog
    private val menu : BottomNavigationView
    private val shower : ViewPager
    private val lv_file : LVFile

    constructor(context: Context , levelPath : LVFile , storeage : ScoreStorage.Companion.ScoreSection , answerd : (()->Unit)? = null){
        this.answerd_listener = answerd
        this.score = storeage
        this.dialog = Dialog(context)
        this.dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.dialog.setContentView(R.layout.question_dialog)
        this.menu = dialog.findViewById(R.id.questin_dialog_menu)
        this.shower = dialog.findViewById(R.id.questin_dialog_viewer)
        this.text = null
        this.question = null
        this.lv_file = levelPath

        //init shower
        this.shower.adapter = this
        this.shower.addOnPageChangeListener(this)

        //set listener to menu
        for (i in 0 until this.menu.menu.size()){
            var item = this.menu.menu.getItem(i)
            item.setOnMenuItemClickListener {
                this.shower.setCurrentItem(i)
                false
            }
        }
    }

    //lv file listener
    override fun InvalidPath() {/*for init*/}
    override fun InvalidInfoFile() {/*for init*/}
    override fun TextLoaded(text: String, id: Int) {
        /*
        * change text load in text view
        * */
        var text = "\n\n\n\n\n$text\n\n\n\n\n\n\n"
        this.text?.setText(text)
    }
    override fun TextFileNotFound() {
        this.text?.error()
    }
    override fun TextException(e: Exception) {
        this.text?.error()
    }


    override fun QuestionLoaded(question: LVFile.Companion.QuestionProfile, id: Int) {
        this.question?.setQuestion(question)
    }
    override fun QuastionFileNotFound() {
        this.question?.error()
    }
    override fun QuestionException(e: Exception) {
        this.question?.error()
    }

    private fun Answered(id : Int){
        /*
        * answer to a question
        * */
        answered.add(id)
        score.AddAnswerScore()
        answerd_listener?.let { it() }
    }


    fun showQuestion(id : Int){
        this.text = TextShower(dialog.context)
        this.question = QuestionShower(dialog.context ,id , this::Answered)
        lv_file.loadText(id , this)
        if (answered.contains(id)){ // check before answered to this question or no
            this.question?.answerd()
        } else{
            lv_file.loadQuestion(id , this)
        }
        dialog.show()
    }

    fun getAnswerCount() : Int{
        return answered.size
    }


    //pager adapter listener
    @SuppressLint("ResourceType")
    override fun instantiateItem(container: ViewGroup, position: Int): View {
        var ret = View(dialog.context)

        if (position == 0){
            container.addView(text)
            ret = this.text as View
        }else if (position == 1){
            container.addView(question)
            ret = this.question as View
        }

        return ret
    }
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view  === `object`
    }
    override fun getCount(): Int {
        return 2
    }


    //view page listener
    override fun onPageScrollStateChanged(state: Int) {}
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageSelected(position: Int) {
        menu.menu.getItem(position).setChecked(true)
    }


    companion object{
        private class TextShower(context: Context) : RelativeLayout(context) {
            private val text :TextView
            private val error :TextView
            private val progress : ProgressBar

            init {
                var view = inflate(context , R.layout.quesion_dialog_text_shower , null)
                this.addView(view)
                text = view.findViewById(R.id.questin_dialog_text_shower_text)
                text.movementMethod = ScrollingMovementMethod()
                error = view.findViewById(R.id.question_dialog_question_shower_wrong_load)
                progress = view.findViewById(R.id.questin_dialog_text_shower_progress)
            }

            fun error(){
                text.visibility = View.INVISIBLE
                progress.visibility = View.INVISIBLE
                error.visibility = View.VISIBLE
            }

            fun setText(text : String){
                this.text.visibility = View.VISIBLE
                progress.visibility = View.INVISIBLE
                this.error.visibility = View.INVISIBLE

                this.text.text = text
            }

            fun Progress(){
                this.text.visibility = View.INVISIBLE
                progress.visibility = View.VISIBLE
                this.error.visibility = View.INVISIBLE
            }
        }

        private class QuestionShower(context: Context , id : Int , listener : (id : Int)->Unit ) : RelativeLayout(context){
            private var btn_submit : Button
            private var question : TextView
            private var answer : EditText
            private var error : TextView
            private var progress : ProgressBar
            private lateinit var questionProfile: LVFile.Companion.QuestionProfile
            private val question_id : Int
            private val listener : (id : Int)->Unit
            init {
                this.question_id = id
                this.listener = listener
                var view = View.inflate(context , R.layout.quesion_dialog_question_shower,null)
                addView(view)
                this.btn_submit = view.findViewById(R.id.question_dialog_question_shower_submit)
                this.btn_submit.setOnClickListener(this::CheckAnswer)
                this.btn_submit.visibility = View.INVISIBLE
                this.answer = view.findViewById(R.id.question_dialog_question_shower_text)
                this.answer.visibility = View.INVISIBLE
                this.question = view.findViewById(R.id.questin_dialog_question_shower_question)
                this.question.visibility = View.INVISIBLE
                this.error = view.findViewById(R.id.questin_dialog_question_shower_error)
                this.progress = view.findViewById(R.id.question_dialog_question_shower_progress)
            }


            fun error(){
                error.setText(R.string.dialog_question_question_error)
                progress.visibility = View.GONE
                error.visibility = View.VISIBLE
                btn_submit.visibility = View.GONE
                answer.visibility = View.GONE
                question.visibility = View.GONE

            }

            fun answerd(in_this : Boolean = false){
                if (in_this){
                    error.setText(R.string.dialog_question_question_score)
                }else {
                    error.setText(R.string.dialog_question_question_answered)
                }
                progress.visibility = View.GONE
                error.visibility = View.VISIBLE
                btn_submit.visibility = View.GONE
                answer.visibility = View.GONE
                question.visibility = View.GONE
            }

            fun setQuestion(quastion: LVFile.Companion.QuestionProfile){
                progress.visibility = View.GONE
                questionProfile = quastion
                this.question.isEnabled = true
                this.answer.isEnabled = true
                this.btn_submit.isEnabled = true
                this.question.text = quastion.quastion
                this.btn_submit.visibility = View.VISIBLE
                this.answer.visibility = View.VISIBLE
                this.question.visibility = View.VISIBLE
            }

            private fun CheckAnswer(view : View){
                var answer = this.answer.text.toString().trim()
                if (answer.contains(questionProfile.answer)){
                    answerd(true)
                    listener(question_id)
                }else{
                    this.answer.error = resources.getString(R.string.dialog_question_question_wrong_answerd)
                    this.answer.requestFocus()
                    YoYo.with(Techniques.Shake).playOn(this.answer)
                }
            }

        }
    }
}
