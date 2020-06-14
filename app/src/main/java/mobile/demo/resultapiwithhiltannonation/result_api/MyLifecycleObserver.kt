package mobile.demo.resultapiwithhiltannonation.result_api

import android.os.Environment
import android.util.Log
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import mobile.demo.resultapiwithhiltannonation.interfaces.ActivitiyResultInterface
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

//https://developer.android.com/training/basics/intents/result
class MyLifecycleObserver(private val activityResultInterface: ActivitiyResultInterface, private val file: File,private val registry : ActivityResultRegistry):DefaultLifecycleObserver {
    lateinit var getContent : ActivityResultLauncher<File>
    var photoPicFile=file
    var resultInterface: ActivitiyResultInterface=activityResultInterface

    override fun onCreate(owner: LifecycleOwner) {
        getContent = registry.register("key",owner,SimpleContract(),
            ActivityResultCallback {

                    result ->
                run {

                    if (result != null) {
                        resultInterface.setData(result)


                    }
                }




            })
    }

    fun openCamera() {
        getContent.launch(photoPicFile)
    }


}