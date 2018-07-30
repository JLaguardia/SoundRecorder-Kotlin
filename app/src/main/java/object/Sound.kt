package `object`

import java.io.File

/**
 * Created by jameslaguardia on 3/13/18.
 */
class Sound(title: String, file: File?){
    private var mFile: File? = file
    private var mTitle: String = title
    private var editMode: Boolean = false

    fun getSoundFile(): File?{
        return mFile
    }

    fun setTitle(title: String){
        this.mTitle = title
    }

    fun getTitle(): String{
        return mTitle
    }

    fun getEditMode(): Boolean{
        return editMode
    }

    companion object {
        interface IEdit{
           fun editClick
        }
    }
}