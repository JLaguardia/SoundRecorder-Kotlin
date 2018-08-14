package com.prismsoftworks.genericsoundboard

import `object`.Sound
import adapter.SoundAdapter
import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {
    val TAG: String = MainActivity::class.java.simpleName
    val internalDir = "/sounds"
    val KEY_APPNAME = "spappname"
    private var appName = "Generic Soundboard"
    private var soundList = ArrayList<Sound>()
    private lateinit var mRecorder: MediaRecorder
    private var isRecording = false
    private var userFileName = ""
    private lateinit var mPrefs: SharedPreferences
    private var mSavedRootFile: File? = null
    private var fileExtension = "3GP"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        mPrefs = getPreferences(Context.MODE_PRIVATE)
        init()
    }

    private fun init(){
        populateSoundList()
        appName = mPrefs.getString(KEY_APPNAME, "Generic Soundboard")
        lblAppTitle.text = appName
        rvMainList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvMainList.adapter = SoundAdapter(soundList, this)
        btnMainOpts.setOnClickListener{
            var poppy = PopupMenu(this, it)
            poppy.inflate(R.menu.popup_menu)
            poppy.setOnMenuItemClickListener {
                var builder = AlertDialog.Builder(this)
                when(it.itemId) {
                    R.id.miRename -> {
                        builder.setTitle("Rename Application")
                        val input = EditText(this)
                        input.inputType = InputType.TYPE_CLASS_TEXT
                        builder.setView(input)
                        builder.setPositiveButton("Confirm") { _: DialogInterface, _: Int ->
                            appName = input.text.toString()
                            mPrefs.edit().putString(KEY_APPNAME, appName).apply()
                            lblAppTitle.text = appName
                        }

                        builder.setNegativeButton("Cancel") { dlg: DialogInterface, _: Int ->
                            dlg.cancel()
                        }

                        builder.show()

//                        layoutInflater.inflate(R.layout.rename_view, btnMainOpts.parent as ViewGroup, false)
                        true
                    }
                    R.id.miDelete -> {
                        if(soundList.count() > 0){
                            builder.setTitle("Delete all sound files?")
                            builder.setPositiveButton("Yes"){ _: DialogInterface, _: Int ->
                                deleteAllFiles()
                            }

                            builder.setNegativeButton("No"){dlg: DialogInterface, _: Int ->
                                dlg.cancel()
                            }
                            builder.show()
                        }

                        true
                    }
                    R.id.miProperties -> {
                        true
                    } else -> {
                    false
                    }
                }
            }

            poppy.show()

        }
    }

    private fun populateSoundList(){
        mSavedRootFile = File(Environment.getExternalStorageDirectory().absolutePath + internalDir)
        if(!mSavedRootFile!!.exists()){
            mSavedRootFile!!.mkdirs()
            soundList = arrayListOf()
        } else {
            val files = mSavedRootFile!!.listFiles()
                soundList.clear()

            if(files.isNotEmpty()){
                for(file: File in files){
                    soundList.add(Sound(file.name, file))
                }
            }
        }

        soundList.add(Sound(getString(R.string.new_sound), null))
    }

    fun stopRecording(){
        if(isRecording){
            mRecorder.stop()
            populateSoundList()
            rvMainList.adapter.notifyDataSetChanged()
        }
    }

    fun deleteAllFiles(){
        soundList.forEach { sound: Sound ->
            if(sound.file != null)
                sound.file!!.delete()
        }

        soundList.clear()
        populateSoundList()
        rvMainList.adapter.notifyDataSetChanged()
    }

    fun startRecording(img: ImageView){
        val permissions = intArrayOf(
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                , ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                , ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE))

        if (permissions[0] != PackageManager.PERMISSION_GRANTED
                || permissions[1] != PackageManager.PERMISSION_GRANTED
                || permissions[2] != PackageManager.PERMISSION_GRANTED) {
            val neededPerms = arrayOf(Manifest.permission.RECORD_AUDIO
                                    , Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    , Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, neededPerms, 0)
            if (permissions[0] != PackageManager.PERMISSION_GRANTED
                    || permissions[1] != PackageManager.PERMISSION_GRANTED
                    || permissions[2] != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        mRecorder = MediaRecorder()
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
//        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mRecorder.setOutputFile(getFilePath(true))
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)

        try {
            mRecorder.prepare()
            mRecorder.start()
            isRecording = true
            img.setImageResource(R.drawable.ic_stop_black_48dp)
            img.setOnClickListener { l: View ->
                stopRecording()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun getFilePath(newFile: Boolean): String {
        if (newFile) {
            userFileName = ""
        }

        userFileName = if (userFileName == "") getDefaultName() else userFileName + fileExtension
        var copyCount = 0
        var tempFileName = ""
        for (file in mSavedRootFile!!.listFiles()) {
            if (file.name == userFileName) {
                copyCount++
                tempFileName = userFileName + copyCount
            }
        }

        if (tempFileName != "")
            userFileName = tempFileName
        return mSavedRootFile!!.absolutePath + "/" + userFileName
    }

    private fun getDefaultName(): String {
        val c = Calendar.getInstance()
        return StringBuilder()
                .append(c.get(java.util.Calendar.YEAR)).append("-")
                .append(c.get(java.util.Calendar.MONTH) + 1).append("-")
                .append(c.get(java.util.Calendar.DAY_OF_MONTH)).append("_")
                .append(c.get(java.util.Calendar.HOUR)).append(".")
                .append(c.get(java.util.Calendar.MINUTE)).append(".")
                .append(c.get(java.util.Calendar.SECOND)).append(".$fileExtension")
                .toString()
    }
}