package com.kosiso.smartcount.ui.uiScreens


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import java.util.UUID


import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.kosiso.smartcount.ui.menu.BottomNavItem
import com.kosiso.smartcount.ui.ui_utils.Common
import com.kosiso.smartcount.R
import com.kosiso.smartcount.ui.theme.Black
import com.kosiso.smartcount.ui.theme.Pink
import com.kosiso.smartcount.ui.theme.White
import com.kosiso.smartcount.utils.Constants
import com.kosiso.smartcount.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
class MainActivity : ComponentActivity() {

    /**
     * viewModels() -> Used to access the view model class and scope the lifecycle to the activity or fragment.
     * viewModel() > used to access the view model class and scope the lifecycle to a composable.
     * We used viewModels() below, which means several composables can use this viewmodel without any issues happening interms of an recomposition.
     * It is used to make the lifecycle of the view model tied to main activity. When main activity dies, only then does the viewmodel class dies.
     * But for viewModel(), its tied to a composable only. It can be a TapScreen() or what ever. Meaning the lifecycle is tied to the TapScreen(),
     * this should be done when you need a view Model class only for TapScreen()
     */
    val mainViewModel: MainViewModel by viewModels()
    private var navControllerState: NavHostController? = null


    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: PowerManager.WakeLock


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Get the PowerManager system service
            powerManager = getSystemService(POWER_SERVICE) as PowerManager
            // Create a WakeLock with the screen flag
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MyApp::PreventScreenOff")

            Permission()

            val navController = rememberNavController()
            navControllerState = navController

            handleIntent(intent, navController)

            RootNavigation(navController, mainViewModel)
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {

    }


    @Composable
    fun RootNavigation(navController: NavHostController, mainViewModel: MainViewModel){
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn()) "main_app" else "auth_flow"
        ) {
            // Auth/Intro Flow
            authNavGraph(navController)

            // Main App (with bottom navigation)
            composable("main_app") {
                MainApp(mainViewModel)
            }
        }
    }

    private fun isLoggedIn(): Boolean {
        return mainViewModel.getCurrentUser() != null
    }

    fun NavGraphBuilder.authNavGraph(navController: NavController) {
        navigation(
            startDestination = "intro_screen",
            route = "auth_flow"
        ) {
            composable("intro_screen") {

                IntroScreen(
                    onNavigateToLoginScreen = {
                        Log.i("intro btn 1", "pressed")
                        navController.navigate("login_screen")
                    }
                )
            }
            composable("signUp_screen") {
                SignUpScreen(mainViewModel){
                    mainViewModel.resetAuthState()
                    navController.navigate("login_screen")
                }
            }
            composable("login_screen") {
                LoginScreen(
                    // After login, navigate to main app and clear auth backstack
                    mainViewModel = mainViewModel,
                    onNavigateToMainScreen = {
                        mainViewModel.resetAuthState()
                        navController.navigate("main_app") {
                            popUpTo("auth_flow") { inclusive = true }
                        }
                    },
                    onNavigationToSignUpScreen = {
                        mainViewModel.resetAuthState()
                        navController.navigate("signUp_screen")
                    }
                )
            }
        }
    }

    @Composable
    fun MainApp(mainViewModel: MainViewModel){
        // another nav controller, not same with root. Its unique to MainApp
        val navController = rememberNavController()

        val bottomNavItems = listOf<BottomNavItem>(
            BottomNavItem(
                id = UUID.randomUUID().toString(),
                name = "CapCount",
                route = "cap_count",
                icon = R.drawable.ic_capture1
            ),
            BottomNavItem(
                id = UUID.randomUUID().toString(),
                name = "TapCount",
                route = "tap_count",
                icon = R.drawable.ic_step
            ),
            BottomNavItem(
                id = UUID.randomUUID().toString(),
                name = "Counts",
                route = "counts",
                icon = R.drawable.ic_arrange1
            ),
            BottomNavItem(
                id = UUID.randomUUID().toString(),
                name = "Profile",
                route = "profile",
                icon = R.drawable.ic_profile0
            )
        )

        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    navItems = bottomNavItems,
                    navController = navController,
                    onItemClick = {
                        navController.navigate(it.route)
                    }
                )
            }
        ){
            Navigation(navController = navController, mainViewModel = mainViewModel)
        }
    }

    @Composable
    fun Navigation(navController: NavHostController, mainViewModel: MainViewModel){

        /**
         * viewModel() -> If you wanted the view model class to be scoped to only a particular
         * composable e.g this navigation composable, this is where you instantiate it with viewModel().
         *
         * val mainViewModel: MainViewModel by viewModel()
         *
         * As long as this Navigation composable stays active, the view model stays active too.
         * If the composable recomposes, the view model goes to default and resets too
         */

        val snackBarHostState = remember { SnackbarHostState() }
        NavHost(navController = navController, startDestination = "cap_count"){
            composable("cap_count"){
                CapCountScreen()
                Common.ShowSnackBar(
                    snackBarHostState,
                    "Home screen Clicked",
                    SnackbarDuration.Long
                )
                Log.i("Home screen Clicked", "Home screen Clicked")
            }
            composable("tap_count"){
                TapCountScreen(mainViewModel)
                Common.ShowSnackBar(
                    snackBarHostState,
                    "Home 1 screen Clicked",
                    SnackbarDuration.Long
                )
                Log.i("Home 1 screen Clicked", "Home 1 screen Clicked")
            }
            composable("counts"){
                CountHistoryScreen(mainViewModel)
                Common.ShowSnackBar(
                    snackBarHostState,
                    "Home 2 screen Clicked",
                    SnackbarDuration.Long
                )
                Log.i("Home 2 screen Clicked", "Home 2 screen Clicked")
            }
            composable("profile"){
                // Home 3 Screen

                Common.ShowSnackBar(
                    snackBarHostState,
                    "Home 3 screen Clicked",
                    SnackbarDuration.Long
                )
                Log.i("Home 3 screen Clicked", "Home 3 screen Clicked")
            }
        }
    }

    @Composable
    fun BottomNavigationBar(
        navItems: List<BottomNavItem>,
        navController: NavController,
        modifier: Modifier = Modifier,
        onItemClick: (BottomNavItem) -> Unit
    ){
        val backStackEntry = navController.currentBackStackEntryAsState()
        NavigationBar(
            modifier = modifier.height(60.dp),
            containerColor = White,
            tonalElevation = 5.dp
        ) {
            navItems.forEach{navItem ->

                val selected = navItem.route == backStackEntry.value?.destination?.route
                NavigationBarItem(
                    modifier = Modifier.padding(top = 5.dp),
                    selected = selected,
                    onClick = { onItemClick(navItem) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = White,
                        unselectedIconColor = Black.copy(alpha = 0.8f),
                        indicatorColor = Pink
                    ),
                    icon = {
                        BottomNavIconStyle(
                            navItem,
                            selected
                        )
                    },
                    alwaysShowLabel = true,
                    label = {
                        Text(
                            text = navItem.name,
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp
                        )
                    }
                )
            }
        }
    }

    @Override
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle the intent when activity is already running
        navControllerState?.let { navController ->
            handleIntent(intent, navController)
        }
    }

    private fun handleIntent(intent: Intent?, navController: NavHostController) {
        when (intent?.action) {
            Constants.ACTION_SHOW_TAP_SCREEN -> {
                navController.navigate("tap_count") {
                    // Optional: Pop up to the start destination to avoid building up a large stack of destinations
                    popUpTo("cap_count") {
                        saveState = true
                    }
                    // Avoid multiple copies of the same destination
                    launchSingleTop = true
                    // Restore state when reselecting a previously selected item
                    restoreState = true
                }
            }
        }
    }


    @Composable
    fun BottomNavIconStyle(
        navItem: BottomNavItem,
        selected: Boolean
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Box{
                Icon(
                    painter = painterResource(navItem.icon),
                    contentDescription = navItem.name,
                    modifier = Modifier.size(24.dp)
                )
            }
//        Text(
//            text = navItem.name,
//            textAlign = TextAlign.Center,
//            fontSize = 10.sp,
//            modifier = Modifier.padding(top = 4.dp)
//        )
            if(selected){
            }
        }
    }


//    @OptIn(ExperimentalPermissionsApi::class)
//    @Composable
//    fun Permission1(){
//        val context = LocalContext.current
//        val lifecycleOwner = LocalLifecycleOwner.current
//        var showPermanentlyDeniedDialog by remember { mutableStateOf(false) }
//        var showTemporarilyDeniedDialog by remember { mutableStateOf(false) }
//
//        val permissionState = rememberPermissionState(
//            Manifest.permission.CAMERA
//        )
//
//        // Permission launcher for manual permission request
//        val permissionLauncher = rememberLauncherForActivityResult(
//            contract = ActivityResultContracts.RequestPermission(),
//            onResult = { isGranted ->
//                when {
//                    isGranted -> {
//                        // Permission was granted
//                        Log.i("permission granted", "granted ${permissionState.status} and $isGranted")
//                    }
//                    permissionState.status.shouldShowRationale -> {
//                        // Permission was denied, but we can ask again
//                        showTemporarilyDeniedDialog = true
//                        Log.i("permission denied", "temporal ${permissionState.status.shouldShowRationale}")
//                    }
//                    !permissionState.status.shouldShowRationale && !isGranted-> {
//                        // Permission permanently denied
//                        showPermanentlyDeniedDialog = true
//                        Log.i("permission denied", "permanent ${!permissionState.status.shouldShowRationale} and ${!permissionState.status.isGranted}")
//                    }
//                }
//            }
//        )
//
//        // Launch permission request on start
//        DisposableEffect(
//            key1 = lifecycleOwner,
//            effect = {
//                val observer = LifecycleEventObserver { _, event ->
//                    if (event == Lifecycle.Event.ON_START) {
//                        // Only launch request if the permission is not granted
//                        if (!permissionState.status.isGranted) {
//                            permissionLauncher.launch(Manifest.permission.CAMERA)
//                        }
//                    }
//                }
//                lifecycleOwner.lifecycle.addObserver(observer)
//
//                onDispose {
//                    lifecycleOwner.lifecycle.removeObserver(observer)
//                }
//            }
//        )
//
//
//        if(showTemporarilyDeniedDialog){
//            Common.ShowDialog(
//                titleText = "Camera Permission",
//                dialogText = "Pls grant Camera permission to use this app.",
//                positiveButtonText = "Grant",
//                negativeButtonText = "Dismiss",
//                confirmButtonClick = {
//                    showTemporarilyDeniedDialog = false
//                    permissionLauncher.launch(Manifest.permission.CAMERA)
//                },
//                dismissButtonClick = {
//                    showTemporarilyDeniedDialog = false
//                }
//            )
//        }
//        if(showPermanentlyDeniedDialog){
//            Common.ShowDialog(
//                titleText = "Camera Permission",
//                dialogText = "Camara permission is required for this app to work. You can go to settings and manually grant permission.",
//                positiveButtonText = "Go to settings",
//                negativeButtonText = "Dismiss",
//                confirmButtonClick = {
//                    showPermanentlyDeniedDialog = false
//                },
//                dismissButtonClick = {
//                    showPermanentlyDeniedDialog = false
//                }
//            )
//        }
//    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun Permission() {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        var showPermanentlyDeniedDialog by remember { mutableStateOf(false) }
        var showTemporarilyDeniedDialog by remember { mutableStateOf(false) }

        // Define multiple permissions
//        val permissions = listOf(
//            Manifest.permission.CAMERA,
//            Manifest.permission.POST_NOTIFICATIONS
//        )
        val permissions = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.POST_NOTIFICATIONS
            )
            else -> arrayOf(
                Manifest.permission.CAMERA
            )
        }

        // Create a list of permission states for each permission
        val permissionStates = permissions.map { permission ->
            rememberPermissionState(permission)
        }

        // Permission launcher for manual permission request
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { permissionsResult ->
                permissionsResult.forEach { (permission, isGranted) ->
                    when {
                        isGranted -> {
                            // Permission was granted
                            Log.i("permission granted", "granted $permission")
                        }
                        permissionStates.find { it.permission == permission }?.status?.shouldShowRationale == true -> {
                            // Permission was denied, but we can ask again
                            showTemporarilyDeniedDialog = true
                            Log.i("permission denied", "temporal $permission")
                        }
                        else -> {
                            // Permission permanently denied
                            showPermanentlyDeniedDialog = true
                            Log.i("permission denied", "permanent $permission")
                        }
                    }
                }
            }
        )

        // Launch permission request on start
        DisposableEffect(
            key1 = lifecycleOwner,
            effect = {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_START) {
                        // Only launch request if the permissions are not granted
                        if (permissionStates.any { !it.status.isGranted }) {
                            permissionLauncher.launch(permissions)
                        }
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)

                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }
        )

        // Show dialog for temporarily denied permissions
        if (showTemporarilyDeniedDialog) {
            Common.ShowDialog(
                titleText = "Permission Required",
                dialogText = "Please grant the required permissions to use this app.",
                positiveButtonText = "Grant",
                negativeButtonText = "Dismiss",
                confirmButtonClick = {
                    showTemporarilyDeniedDialog = false
                    permissionLauncher.launch(permissions) // Relaunch permission request
                },
                dismissButtonClick = {
                    showTemporarilyDeniedDialog = false
                }
            )
        }

        // Show dialog for permanently denied permissions
        if (showPermanentlyDeniedDialog) {
            Common.ShowDialog(
                titleText = "Permission Required",
                dialogText = "Permissions are required for this app to work. You can go to settings and manually grant them.",
                positiveButtonText = "Go to Settings",
                negativeButtonText = "Dismiss",
                confirmButtonClick = {
                    showPermanentlyDeniedDialog = false
                },
                dismissButtonClick = {
                    showPermanentlyDeniedDialog = false
                }
            )
        }
    }



    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if (event?.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    mainViewModel.increment()
                    return true
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    mainViewModel.decrement()
                    return true
                }
            }
            if (!wakeLock.isHeld) {
                wakeLock.acquire()
                Log.i("Volume button pressed, keeping screen on", "Volume button pressed, keeping screen on")
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

}