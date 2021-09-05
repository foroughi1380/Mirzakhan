package ir.mirzabazi.mirza.Game

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ScoreStorage : SQLiteOpenHelper {


    private val sections : ArrayList<ScoreSection>
    constructor(context: Context) : super(context , DB_NAME , null , DB_NOW_VERSHON){
        sections = ArrayList()
    }

    fun OpenSection(id : Int) : ScoreSection{
        var last_Score = 0
        var is_new = false
        var result =  readableDatabase.rawQuery(ReadScoreSQL(id) , null)

        if (result != null){
            last_Score = result.count
            is_new = result.count == 0
        }

        var ret = ScoreSection(id , is_new , last_Score)
        sections.add(ret)
        return ret
    }
    private fun StoreSection(section : ScoreSection, if_biger : Boolean = true){
        var new_score = section.getScore()
        var old_score = section.lastScore

        if (! sections.contains(section)) return

        if (if_biger && new_score > old_score) return

        var sql : String =
        if (section.isNew){
            InsertScoreSQL(section.id ,  new_score)
        }else{
            UpdateScoreSQL(section.id , new_score)
        }

        writableDatabase.execSQL(sql)
    }

    fun getScore():Int{
        var sum = 0
        var res = readableDatabase.rawQuery("select score from $DB_SCORE_TABLE_NAME" , null)
        if (!res.moveToFirst()) return sum
        do {
            sum += res.getInt(0)
        }while (res.moveToNext())

        return sum
    }
    //init table
    override fun onCreate(db: SQLiteDatabase?) {
        var sql = "create table $DB_SCORE_TABLE_NAME(id INTEGER primary key , score INTEGER)"
        db?.execSQL(sql)
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        var del_sql = "drop table $DB_SCORE_TABLE_NAME"
        db?.execSQL(del_sql)

        var sql = "create table $DB_SCORE_TABLE_NAME(id INTEGER primary key , score INTEGER)"
        db?.execSQL(sql)
    }
    companion object{
        val DB_NAME = "DMS"
        val DB_NOW_VERSHON = 1
        val DB_SCORE_TABLE_NAME = "ss"
        // create sql function
        fun ReadScoreSQL(id : Int) : String{
            var sql = "select $DB_SCORE_TABLE_NAME.score from $DB_SCORE_TABLE_NAME where $DB_SCORE_TABLE_NAME.id = $id"
            return sql
        }
        fun InsertScoreSQL(id : Int , score : Int) : String{
            var sql = "insert into $DB_SCORE_TABLE_NAME values ($id ,$score )"
            return sql
        }
        fun UpdateScoreSQL(id : Int , score : Int) : String {
            var sql = "update $DB_SCORE_TABLE_NAME set score = $score where id = $id"
            return sql
        }

        class ScoreSection{
            private val scores : ArrayList<Int>
            val id : Int
            val isNew : Boolean
            val lastScore : Int
            constructor(id : Int , isNew : Boolean = false , last_score : Int = 0){
                scores = ArrayList()
                this.id = id
                this.isNew = isNew
                if (isNew){
                    this.lastScore = 0
                }else{
                    this.lastScore = last_score
                }
            }

            // adding score
            fun AddCustomScore(score : Int){
                /*
                * adding its int to scores
                * */
                scores.add(score)
            }
            fun AddWidgetScore(score : Int = Find_Widget_SCORE){
                AddCustomScore(score)
            }
            fun AddAnswerScore(score : Int = ANSWER_WIDGT_QUESTION_SCORE){
                AddCustomScore(score)
            }
            fun AddFinishScore(score : Int = FINISH_GAME_SCORE){
                AddCustomScore(score)
            }

            //calculating score
            fun getScore() : Int{
                var sum = 0
                scores.forEach {
                    sum += it
                }
                return sum
            }

            //store
            fun Store(storage : ScoreStorage , is_biger : Boolean = true){
                storage.StoreSection(this , is_biger)
            }
            companion object{
                val Find_Widget_SCORE = 30
                val ANSWER_WIDGT_QUESTION_SCORE = 60
                val FINISH_GAME_SCORE = 100
            }
        }
    }
}