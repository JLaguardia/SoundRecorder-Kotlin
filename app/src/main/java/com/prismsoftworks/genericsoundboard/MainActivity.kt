package com.prismsoftworks.genericsoundboard

import `object`.Sound
import adapter.SoundAdapter
import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.Window
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {
    val TAG: String = MainActivity::class.java.simpleName
    val internalDir = "/sounds"
    private var soundList = ArrayList<Sound>()
    private lateinit var mRecorder: MediaRecorder
    private var isRecording = false
    private var userFileName = ""
    private var mPrefs: SharedPreferences? = null
    private var mSavedRootFile: File? = null
    private var fileExtension = "3GP"

//    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
//        when (item.itemId) {
//            R.id.navigation_home -> {
//                return@OnNavigationItemSelectedListener true
//            }
//            R.id.navigation_files -> {
//                return@OnNavigationItemSelectedListener true
//            }
//            R.id.navigation_settings -> {
//                return@OnNavigationItemSelectedListener true
//            }
//        }
//        false
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.activity_main)
//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        mPrefs = getPreferences(Context.MODE_PRIVATE)
        init()

    }

    private fun init(){
        populateSoundList()
        rvMainList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvMainList.adapter = SoundAdapter(soundList, this)
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