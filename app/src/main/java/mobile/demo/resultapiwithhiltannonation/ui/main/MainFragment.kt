package mobile.demo.resultapiwithhiltannonation.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.main_fragment.*
import mobile.demo.resultapiwithhiltannonation.R
import mobile.demo.resultapiwithhiltannonation.interfaces.ActivitiyResultInterface
import mobile.demo.resultapiwithhiltannonation.result_api.MyLifecycleObserver
import mobile.demo.resultapiwithhiltannonation.result_api.SimpleContract
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment(), ActivitiyResultInterface {

    companion object {
        fun newInstance() = MainFragment()

    }

    var mCurrentPhotoPath: String? = null;


    lateinit var observer : MyLifecycleObserver


    private lateinit var viewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observer = MyLifecycleObserver(this,createImageFile(),requireActivity().activityResultRegistry)
        lifecycle.addObserver(observer)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        take_pic?.setOnClickListener(object:View.OnClickListener
        {
            override fun onClick(v: View?) {

                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                    simplePhotoClickContractRegistration.launch(createImageFile())
                else
                    if(isExternalStoragePermissionGranted())
                        simplePhotoClickContractRegistration.launch(createImageFile())
            }
        })


        take_pic_2?.setOnClickListener (object :View.OnClickListener
        {
            override fun onClick(v: View?) {
                // Open the activity to select an image
                observer.openCamera()
            }

        })

        take_pic_3?.setOnClickListener (object :View.OnClickListener
        {
            override fun onClick(v: View?) {

            }

        })


    }

    private fun isExternalStoragePermissionGranted(): Boolean {
        return if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
                askPermissionsForPhotoPic(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.CAMERA))
            false
        }
    }

    private fun askPermissionsForPhotoPic(arrayOf: Array<String>) {

        val permissionlistener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                Toast.makeText(requireContext(),"Any one of permission is cancel by you . you can't proceed without all all sucessful permission",Toast.LENGTH_LONG).show()
            }


            override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {
                simplePhotoClickContractRegistration.launch(createImageFile())
            }

        }
        TedPermission(requireContext())
            .setPermissionListener(permissionlistener)
            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(*arrayOf)
            .check();

    }

    @Throws(IOException::class)
    private fun createImageFile(): File { // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        var image: File = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
        mCurrentPhotoPath = image.absolutePath
        return image;
    }



    private val simplePhotoClickContractRegistration=registerForActivityResult(SimpleContract())
    {
        result: Bitmap? ->
        run {

            if (result == null)
                Toast.makeText(context, "No photo result Or User cancel it", Toast.LENGTH_LONG)
                    .show()
            else {

                set_pic.setImageBitmap(result)
            }
        }
    }

    override fun setData(bitmap: Bitmap) {
        if (bitmap == null)
            Toast.makeText(context, "No photo result Or User cancel it", Toast.LENGTH_LONG)
                .show()
        else {

            set_pic.setImageBitmap(bitmap)
        }
    }


}