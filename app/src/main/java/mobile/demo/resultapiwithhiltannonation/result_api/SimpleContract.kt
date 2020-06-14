package mobile.demo.resultapiwithhiltannonation.result_api

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.FileProvider
import mobile.demo.resultapiwithhiltannonation.BuildConfig
import java.io.File

class SimpleContract : ActivityResultContract<File,Bitmap>() {


    private var currentFile: File? = null



    override fun createIntent(context: Context, input: File?): Intent {

                currentFile=input
                val photoURI = currentFile?.let { FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", it) }
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                        takePictureIntent.setClipData(ClipData.newRawUri("", photoURI));
                        takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }

        return takePictureIntent



    }


    override fun parseResult(resultCode: Int, intent: Intent?): Bitmap? = when{
        resultCode != Activity.RESULT_OK -> null      // Return null, if action is cancelled
        else -> getBitmap(currentFile?.absolutePath!!)      // Return the data
    }


    private fun getBitmap(path:String): Bitmap? {
        var options: BitmapFactory.Options;
        try {
            val bitmap = BitmapFactory.decodeFile(path)
            return bitmap;
        } catch (e: OutOfMemoryError) {
            try {
                options = BitmapFactory.Options()
                options?.inSampleSize = 2

                val bitmap = BitmapFactory.decodeFile(path, options)
                return bitmap
            } catch (e: java.lang.Exception) {
                return null;
            }
        }
    }



}