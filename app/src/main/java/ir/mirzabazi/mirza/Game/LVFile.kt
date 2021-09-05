 package ir.mirzabazi.mirza.Game

 import org.json.JSONObject
 import java.io.File
 import java.io.InputStream
 import java.util.zip.ZipEntry
 import java.util.zip.ZipInputStream
 import javax.crypto.Cipher
 import javax.crypto.CipherInputStream
 import javax.crypto.spec.SecretKeySpec

 class LVFile{
     private val listener : LVFileListener
     private lateinit var question_dir : File
     private lateinit var coefficient : Coefficient
     private val widgets_file : ArrayList<File>
     private val root_dir : File
     private var id = -1
     private var name = ""
     val path : String

     private var last_text_load = ""
     private var last_text_id : Int? = null

     constructor(dir : File , listener: LVFileListener){
         this.listener = listener
         this.root_dir = dir
         this.widgets_file = arrayListOf()
         this.path = dir.path
         //check valid file
         if (dir.exists() && dir.isDirectory){
             var info = File(dir.path + "/$INFO_FILE")
             var question_dir = File(dir.path + "/$QUESTION_DIR")

             //check valid info file
             if (info.exists()){ // if info file not found
                 try {
                     fillInfo(info)
                 }catch (e:Exception){
                     listener.InvalidInfoFile()
                     return
                 }
             }else{
                 listener.InvalidPath()
                 return
             }

             //check question dir
             if (question_dir.exists()){
                 this.question_dir = question_dir
             }else{
                 listener.InvalidPath()
                 return
             }
         }else{
             listener.InvalidPath()
             return
         }
         loadFiles()
     }

     private fun loadFiles(){
         // store all widget files
         var files = root_dir.listFiles({
                 it-> var split = it.name.split(".")
             if (split.size != 2){
                 false
             }else{
                 split[1] == WIDGET_FILE_FORMAT
             }

         })
         widgets_file.addAll(files)

     }
     fun getWidgetsJson() : ArrayList<JSONObject>{
         var ret = arrayListOf<JSONObject>()
         for (file in widgets_file){
             var json_text = file.inputStream().bufferedReader().readText().trim()
             var json_object = JSONObject(json_text)
             ret.add(json_object)
         }

         return ret
     }

     fun fillInfo(info_file : File){
         var json_text = info_file.inputStream().bufferedReader().readText().trim()
         var json = JSONObject(json_text)
         coefficient = Coefficient.FromJson(json)
         id = json.getInt(FilesTags.INFO_TAG_LEVEL_ID_SERVER)
         name = json.getString(FilesTags.INFO_TAG_LEVEL_NAME)
     }

     //load text file
     fun loadText(id : Int , listener : LVFileListener = this.listener){
         /*
         * this method read file text and call textLoaded function
         * */

         var file = getFile(id , FileType.text)

         if(! CheckValidFile(file)){ // check valid file
             listener.TextFileNotFound()
             return
         }

         if (last_text_id!=null && last_text_id == id){
             listener.TextLoaded(last_text_load , id)
             return
         }else{
             last_text_id = null
         }


         try { // load the text file
             var text = file.inputStream().bufferedReader().readText()
             listener.TextLoaded(text , id)
             return
         }catch (e:Exception){
             listener.TextException(e)
             return
         }
     }

     //load question
     fun loadQuestion(id : Int , listener : LVFileListener = this.listener){
         /*
         * this method read a question file
         * */
         var file = getFile(id , FileType.question)

         //check valid file
         if (! CheckValidFile(file)){
             listener.QuastionFileNotFound()
             return
         }


         try { // load question
             var text = file.inputStream().bufferedReader().readText().trim() // load text
             var json = JSONObject(text)

             var question = json.getString(QuestionProfile.TAG_QUESTION)
             var answer = json.getString(QuestionProfile.TAG_ANSWER)

             var question_pro = QuestionProfile(question , answer , id)
             listener.QuestionLoaded(question_pro , id)
             return
         }catch (e:Exception){
             listener.QuestionException(e)
             return
         }
     }

     fun getLevelInformation() : LevelProfile{
         return LevelProfile(name , id , coefficient)
     }
     //utility functions
     private fun CheckValidFile(file : File) : Boolean{
         // this method check a file is valid or no
         return file.exists() && file.isFile
     }
     private fun getFile(id : Int , type : FileType) : File {
         /*
         * this function return a text or type file
         * */
         var typeText = when(type){
             FileType.text-> "t"
             FileType.question -> "q"
         }
         return File("${question_dir.path}/$typeText$id")
     }


     companion object{
         const val QUESTION_DIR = "question"
         const val IMAGE_DIR = "image"
         const val INFO_FILE = "info"
         const val WIDGET_FILE_FORMAT = "widget"
         class FilesTags {companion object{

             const val INFO_TAG_LATITUDE_COEFFICIENT = "lat_co"
             const val INFO_TAG_LONGITUDE_COEFFICIENT = "lon_co"
             const val INFO_TAG_LEVEL_ID_SERVER = "sid"
             const val INFO_TAG_LEVEL_NAME = "name"

             const val TAG_TYPE = "type"
             const val TAG_LATITUDE = "lat"
             const val TAG_LONGITUDE = "lon"

             class Widget{companion object{
                 const val TYPE = "widget"
                 const val TAG_NAME = "name"
                 const val TAG_QUESTION_ID = "question_id"
                 const val TAG_IMAGES = "images"
             }}

             class Toast{companion object{
                 const val TYPE = "info"
                 const val TAG_MESSAGE = "message"
             }}

             class ScoreWidget{companion object{
                 const val TYPE = "score"
                 const val TAG_IMAGES = "images"
                 const val TAG_NAME = "name"
             }}

             class FinishWidget{companion object{
                 const val TYPE = "finish"
                 const val TAG_IMAGES = "images"
             }}


         }}
         private enum class FileType{
             text,question
         }
         data class Coefficient(var lat : Double , var Lon : Double){
             companion object{
                 fun FromJson(json : JSONObject) : Coefficient{
                     var lat = json.getDouble(FilesTags.INFO_TAG_LATITUDE_COEFFICIENT)
                     var lon = json.getDouble(FilesTags.INFO_TAG_LONGITUDE_COEFFICIENT)
                     return Coefficient(lat , lon)
                 }
             }
         }
         data class QuestionProfile(var quastion: String , var answer : String , var id : Int){
             companion object{
                 var TAG_QUESTION = "q"
                 var TAG_ANSWER = "a"
             }
         }

         interface LVFileListener{
             //init functions
             fun InvalidPath()
             fun InvalidInfoFile()

             //text functions
             fun TextLoaded(text : String , id : Int)
             fun TextFileNotFound()
             fun TextException(e : Exception)

             //question functions
             fun QuestionLoaded(quastion : QuestionProfile, id : Int)
             fun QuastionFileNotFound()
             fun QuestionException(e : Exception)
         }
         data class LevelProfile(val name : String , val id : Int , val coefficient : Coefficient)
         fun ExtractFromStream(inputStream: InputStream , dir : File){
             /*
             * extract all lv file in to a dir
             * */
             val code = "auimgltjauimgltjauimgltjauimgltj"

             // init cipher
             var key = SecretKeySpec(code.toByteArray() , "AES")
             var cipher = Cipher.getInstance("AES")
             cipher.init(Cipher.DECRYPT_MODE , key)

             //init reader
             var reader = CipherInputStream(inputStream , cipher)
             var zip = ZipInputStream(reader)
             var entry : ZipEntry? = zip.nextEntry
             while (entry != null){
                 //write all file to dir
                 var writer = File("${dir.absolutePath}/${entry.name}")
                 var parent = writer.parentFile
                 if (! parent.exists()){
                     parent.mkdirs()
                 }
                 writer.outputStream().write(zip.readBytes())
                 entry = zip.nextEntry
             }
             zip.close()
         }
     }
 }