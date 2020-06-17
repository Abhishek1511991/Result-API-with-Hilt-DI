package mobile.demo.resultapiwithhiltannonation.hilt_DI.data

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton


@Module
@InstallIn(FragmentComponent::class)
class CameraCapture @Inject constructor()
{

        @FragmentScoped
        @Singleton
        fun doCameraClick(fragmentContract: ActivityResultLauncher<File>, file:File) = fragmentContract.launch(file)


}