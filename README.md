# Result API with Hilt DI
 This is demo app to test Result API and HIlt DI uses

## Related link ->
 
https://github.com/ParkSangGwon/TedPermission  -- Multiple Run Time Library 
https://medium.com/mindorks/multiple-runtime-permissions-in-android-without-any-third-party-libraries-53ccf7550d0  -- 

## Multiple Run Time Library 
https://developer.android.com/training/basics/intents/result
https://android.jlelse.eu/activity-results-api-69be5a225e86   ***** 
https://adambennett.dev/2020/03/introducing-the-activity-result-apis/   *** Adavance Demo
https://www.signom.com/api/rest/docs/



## For Runtime Permission :

https://medium.com/mindorks/multiple-runtime-permissions-in-android-without-any-third-party-libraries-53ccf7550d0
https://github.com/ParkSangGwon/TedPermission

# Result API Study
Getting a result from an activity
Starting another activity, whether one within your app or from another app, doesn't need to be a one-way operation. You can also start another activity and receive a result back. For example, your app can start a camera app and receive the captured photo as a result. Or, you might start the Contacts app in order for the user to select a contact and you'll receive the contact details as a result.

While the underlying startActivityForResult() and onActivityResult() APIs are available on the Activity class on all API levels, it is strongly recommended to use the Activity Result APIs introduced in AndroidX Activity 1.2.0-alpha02 and Fragment 1.3.0-alpha02.

The Activity Result APIs provide components for registering for a result, launching the result, and handling the result once it is dispatched by the system.

Registering a callback for an Activity Result
When starting an activity for a result, it is possible (and, in cases of memory-intensive operations such as camera usage, almost certain) that your process and your activity will be destroyed due to low memory.

For this reason, the Activity Result APIs decouple the result callback from the place in your code where you launch the other activity. As the result callback needs to be available when your process and activity are recreated, the callback must be unconditionally registered every time your activity is created, even if the logic of launching the other activity only happens based on user input or other business logic.

When in a ComponentActivity or a Fragment, the Activity Result APIs provide a registerForActivityResult() API for registering the result callback. registerForActivityResult() takes an ActivityResultContract and an ActivityResultCallback and returns an ActivityResultLauncher which you’ll use to launch the other activity.

An ActivityResultContract defines the input type needed to produce a result along with the output type of the result. The APIs provide default contracts for basic intent actions like taking a picture, requesting permissions, and so on. You can also create your own custom contracts.

ActivityResultCallback is a single method interface with an onActivityResult() method that takes an object of the output type defined in the ActivityResultContract:

KOTLIN
JAVA
val getContent = registerForActivityResult(GetContent()) 
{ 
 uri: Uri? ->
// Handle the returned Uri
}

If you have multiple activity result calls that either use different contracts or want separate callbacks, you can call registerForActivityResult() multiple times to register multiple ActivityResultLauncher instances. You must always call registerForActivityResult() in the same order for each creation of your fragment or activity to ensure that the inflight results are delivered to the correct callback.

registerForActivityResult() is safe to call before your fragment or activity is created, allowing it to be used directly when declaring member variables for the returned ActivityResultLauncher instances.

Note: While it is safe to call registerForActivityResult() before your fragment or activity is created, you cannot launch the ActivityResultLauncher until the fragment or activity's Lifecycle has reached CREATED.
Launching an activity for result
While registerForActivityResult() registers your callback, it does not launch the other activity and kick off the request for a result. Instead, this is the responsibility of the returned ActivityResultLauncher instance.

If input exists, the launcher takes the input that matches the type of the ActivityResultContract. Calling launch() starts the process of producing the result. When the user is done with the subsequent activity and returns, the onActivityResult() from the ActivityResultCallback is then executed, as shown in the following example:

KOTLIN
JAVA
val getContent = registerForActivityResult(GetContent()) 
{ 
uri: Uri? ->
// Handle the returned Uri
}

override fun onCreate(savedInstanceState: Bundle?) {
// ...

val selectButton = findViewById<Button>(R.id.select_button)

selectButton.setOnClickListener {
// Pass in the mime type you'd like to allow the user to select
// as the input
getContent.launch("image/*")
}
}

An overloaded version of launch() allows you to pass an ActivityOptionsCompat in addition to the input.

Note: Since your process and activity can be destroyed between when you call launch() and when the onActivityResult() callback is triggered, any additional state needed to handle the result must be saved and restored separately from these APIs.
Receiving an activity result in a separate class
While the ComponentActivity and Fragment classes implement the ActivityResultCaller interface to allow you to use the registerForActivityResult() APIs, you can also receive the activity result in a separate class that does not implement ActivityResultCaller by using ActivityResultRegistry directly.

For example, you might want to implement a LifecycleObserver that handles registering a contract along with launching the launcher:

KOTLIN
JAVA
class MyLifecycleObserver(private val registry : ActivityResultRegistry)
: DefaultLifecycleObserver {

lateinit var getContent : ActivityResultLauncher<String>

override fun onCreate(owner: LifecycleOwner) {

getContent = registry.register("key", owner, GetContent()) { uri ->
// Handle the returned Uri
}

}

fun selectImage() {
getContent.launch("image/*")
}
}

class MyFragment : Fragment() {

lateinit var observer : MyLifecycleObserver

override fun onCreate(savedInstanceState: Bundle?) {
// ...

observer = MyLifecycleObserver(requireActivity().activityResultRegistry)
lifecycle.addObserver(observer)
}

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
val selectButton = view.findViewById<Button>(R.id.select_button)

selectButton.setOnClickListener {
// Open the activity to select an image
observer.selectImage()
}
}
}

When using the ActivityResultRegistry APIs, it's strongly recommended to use the APIs that take a LifecycleOwner, as the LifecycleOwner automatically removes your registered launcher when the Lifecycle is destroyed. However, in cases where a LifecycleOwner is not available, each ActivityResultLauncher class allows you to manually call unregister() as an alternative.

Testing
By default, registerForActivityResult() automatically uses the ActivityResultRegistry provided by the activity. It also provides an overload that allows you to pass in your own instance of ActivityResultRegistry that can be used to test your activity result calls without actually launching another activity.

When Testing your app’s fragments, providing a test ActivityResultRegistry can be done by using a FragmentFactory to pass in the ActivityResultRegistry to the fragment’s constructor.

Note: Any mechanism that allows you to inject a separate ActivityResultRegistry in tests is enough to enable testing your activity result calls.
For example, a fragment that uses the TakePicturePreview contract to get a thumbnail of the image might be written similar to the following:

KOTLIN
JAVA
class MyFragment(
private val registry: ActivityResultRegistry) : Fragment() {

val thumbnailLiveData = MutableLiveData<Bitmap?>

val takePicture = registerForActivityResult(TakePicturePreview(), registry) 
{
   bitmap: Bitmap? -> thumbnailLiveData.setValue(bitmap)
}

// ...
}

When creating a test specific ActivityResultRegistry, you must implement the onLaunch() method. Instead of calling startActivityForResult(), your test implementation can instead call dispatchResult() directly, providing the exact results you want to use in your test:

val testRegistry = object : ActivityResultRegistry() 
{

override fun <I, O> onLaunch(
requestCode: Int,
contract: ActivityResultContract<I, O>,
input: I,
options: ActivityOptionsCompat?
) 

{
dispatchResult(requestCode, expectedResult)
}
}

The complete test would create the expected result, construct a test ActivityResultRegistry, pass it to the fragment, trigger the launcher (be it directly or via other test APIs such as Espresso), and then verify the results:

@Test
fun activityResultTest {
// Create an expected result Bitmap
val expectedResult = Bitmap.createBitmap(1, 1, Bitmap.Config.RGBA_F16)

// Create the test ActivityResultRegistry
val testRegistry = object : ActivityResultRegistry() {
override fun <I, O> onLaunch(
requestCode: Int,
contract: ActivityResultContract<I, O>,
input: I,
options: ActivityOptionsCompat?
) 
{
dispatchResult(requestCode, expectedResult)
}
}

// Use the launchFragmentInContainer method that takes a
// lambda to construct the Fragment with the testRegistry
with(launchFragmentInContainer { MyFragment(testRegistry) }) 
{
onFragment { fragment ->
// Trigger the ActivityResultLauncher
fragment.takePicture()
// Verify the result is set
assertThat(fragment.thumbnailLiveData.value)
.isSameInstanceAs(expectedResult)
}
}
}

Creating a custom contract
While ActivityResultContracts contains a number of prebuilt ActivityResultContract classes for use, you can provide your own contracts that provide the precise type safe API you require.

Each ActivityResultContract requires defining the input and output classes, using Void (in Kotlin, use either Void? or Unit) as the input type if you do not require any input.

Each contract must implement the createIntent() method, which takes a Context and the input and constructs the Intent that will be used with startActivityForResult().

Each contract must also implement parseResult(), which produces the output from the given resultCode (e.g., Activity.RESULT_OK or Activity.RESULT_CANCELED) and the Intent.

Contracts can optionally implement getSynchronousResult() if it is possible to determine the result for a given input without needing to call createIntent(), start the other activity, and use parseResult() to build the result.

KOTLIN
JAVA

class PickRingtone : ActivityResultContract<Int, Uri?>() 
{
override fun createIntent(context: Context, ringtoneType: Int) =
Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, ringtoneType)

}

override fun parseResult(resultCode: Int, result: Intent?) : Uri? 
{
if (resultCode != Activity.RESULT_OK) {
return null
}
return result?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
}
}

If you do not need a custom contract, you can use the StartActivityForResult contract. This is a generic contract that takes any Intent as an input and returns an ActivityResult, allowing you to extract the resultCode and Intent as part of your callback, as shown in the following example:

KOTLIN
JAVA
val startForResult = registerForActivityResult(StartActivityForResult()) 
{ 
result: ActivityResult ->
if (result.resultCode == Activity.RESULT_OK) {
val intent = result.intent
// Handle the Intent
}
}

override fun onCreate(savedInstanceState: Bundle) {
// ...

val startButton = findViewById(R.id.start_button)

startButton.setOnClickListener {
// Use the Kotlin extension in activity-ktx
// passing it the Intent you want to start
startForResult.launch(Intent(this, ResultProducingActivity::class.java))
}
}





------------------------------------------------------------------------------------------------------------

# Hilt DI Uses


![Banner](https://github.com/MindorksOpenSource/Dagger-Hilt-Tutorial/raw/master/assets/banner-dagger-hilt.png)

## Dependency Injection on Android with Hilt

Dependency injection (DI) is a technique widely used in programming and well suited to Android development, where dependencies are provided to a class instead of creating them itself. By following DI principles, you lay the groundwork for good app architecture, greater code reusability, and ease of testing. Have you ever tried manual dependency injection in your app? Even with many of the existing dependency injection libraries today, it requires a lot of boilerplate code as your project becomes larger, since you have to construct every class and its dependencies by hand, and create containers to reuse and manage dependencies.

By following DI principles, you lay the groundwork for good app architecture, greater code reusability, and ease of testing.
The new Hilt library defines a standard way to do DI in your application by providing containers for every Android class in your project and managing their lifecycles automatically for you. Hilt is currently in alpha, try it in your app and give us feedback using this link.

Hilt is built on top of the popular DI library Dagger so benefits from the compile time correctness, runtime performance, scalability, and Android Studio support that Dagger provides. Due to this, Dagger’s seen great adoption on 74% of top 10k apps of the Google Play Store. However, because of the compile time code generation, expect a build time increase.
Since many Android framework classes are instantiated by the OS itself, there’s an associated boilerplate when using Dagger in Android apps. Unlike Dagger, Hilt is integrated with Jetpack libraries and Android framework classes and removes most of that boilerplate to let you focus on just the important parts of defining and injecting bindings without worrying about managing all of the Dagger setup and wiring. It automatically generates and provides:
Components for integrating Android framework classes with Dagger that you would otherwise need to create by hand.
Scope annotations for the components that Hilt generates automatically.

Predefined bindings and qualifiers.

Best of all, as Dagger and Hilt can coexist together, apps can be migrated on an as-needed basis.

Hilt in action
Just to show you how easy to use Hilt is, let’s perform some quick DI in a typical Android app. Let’s make Hilt inject an AnalyticsAdapter into our MainActivity.

First, enable Hilt in your app by annotating your application class with the @HiltAndroidApp to trigger Hilt’s code generation:


@HiltAndroidApp
class MyApplication : Application() { ... }

Second, tell Hilt how to provide instances of AnalyticsAdapter by annotating its constructor with 

@Inject:
class AnalyticsAdapter @Inject constructor() { ... }

And third, to inject an instance of AnalyticsAdapter into MainActivity, enable Hilt in the activity with the @AndroidEntryPoint annotation and perform field injection using the @Inject annotation:

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  @Inject lateinit var analytics: AnalyticsAdapter
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // analytics instance has been populated by Hilt
    // and it's ready to be used
  }
  
}

For more information, you can easily check out what the new annotations do in the cheat sheet section below.
Comes with Jetpack support!

You can use your favourite Jetpack libraries with Hilt out of the box. We’re providing direct injection support for ViewModel and WorkManager in this release.

For example, to inject a Architecture Components ViewModel LoginViewModel into a LoginActivity: annotate LoginViewModel 
with @ViewModelInject and use it in the activity or fragment as you’d expect:


class LoginViewModel @ViewModelInject constructor(private val analyticsAdapter: AnalyticsAdapter)
       : ViewModel { ... }
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

  private val loginViewModel: LoginViewModel by viewModels()
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // loginViewModel is ready to be used
  }
}

Start using Hilt
If you’re intrigued by Hilt and want to learn more about it, here’s some resources for you to learn in the way you prefer:
Getting started with Hilt

If you’re new to DI or Dagger, check out our guide to add Hilt to an Android app. Alternatively, if you already know Dagger, we’re also providing documentation on dagger.dev.
If you’re just curious about the new annotations and what you can do with Hilt, check out this cheat sheet in the section below.

For Dagger users
If you’re already using Dagger or dagger.android in your app, check out this migration guide or the codelab mentioned below to help you switch to Hilt. As Dagger and Hilt can coexist together, you can migrate your app incrementally.
Codelabs
To learn Hilt in a step-by-step approach, we just released two codelabs:
Using Hilt in your Android app


# Hilt and Dagger annotations cheat sheet. Download from below link:
https://developer.android.com/images/training/dependency-injection/hilt-annotations.pdf
https://dagger.dev/hilt/components ***
https://www.techyourchance.com/dagger-hilt/
https://dev.to/anandpushkar088/experimenting-with-dagger-hilt-3e80
https://joebirch.co/android/exploring-dagger-hilt-an-introduction/
https://github.com/MindorksOpenSource/Dagger-Hilt-Tutorial
https://blog.mindorks.com/dagger-hilt-tutorial
https://proandroiddev.com/exploring-dagger-hilt-and-whats-main-differences-with-dagger-android-c8c54cd92f18
https://dagger.dev/hilt/ ***
https://codelabs.developers.google.com/codelabs/android-dagger-to-hilt/#0
https://medium.com/androiddevelopers/dependency-injection-on-android-with-hilt-67b6031e62d
https://developer.android.com/training/dependency-injection/hilt-android
https://developer.android.com/training/dependency-injection/hilt-jetpack

# Hilt demo
https://github.com/pushkar-anand/hilt-demo
https://github.com/MindorksOpenSource/Dagger-Hilt-Tutorial
https://github.com/android/architecture-samples





