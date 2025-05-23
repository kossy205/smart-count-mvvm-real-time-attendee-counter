package com.kosiso.smartcount.ui.uiScreens


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.KeyEvent
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.kosiso.smartcount.ui.menu.BottomNavItem
import com.kosiso.smartcount.ui.ui_utils.Common
import com.kosiso.smartcount.R
import com.kosiso.smartcount.ui.theme.Black
import com.kosiso.smartcount.ui.theme.Pink
import com.kosiso.smartcount.ui.theme.White
import com.kosiso.smartcount.utils.AuthFlowNavigation
import com.kosiso.smartcount.utils.Constants
import com.kosiso.smartcount.utils.MainAppNavigation
import com.kosiso.smartcount.utils.RootNavEnum
import com.kosiso.smartcount.utils.SplashNavEnum
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
    lateinit var mainViewModel: MainViewModel
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
            mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

            Permission()

            val rootNavController = rememberNavController()
            navControllerState = rootNavController

            handleIntent(intent, rootNavController)

            RootNavigation(rootNavController, mainViewModel)
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {

    }


    @Composable
    fun RootNavigation(rootNavController: NavHostController, mainViewModel: MainViewModel){
        NavHost(
            navController = rootNavController,
            startDestination = RootNavEnum.SPLASH_FLOW.route
        ) {
            // Splash Screen
            splashScreenNavGraph(rootNavController)

            // Auth/Intro Flow
            authNavGraph(rootNavController)

            // Main App (with bottom navigation)
            composable(RootNavEnum.MAIN_APP.route) {
                /**
                 * MainApp has a navigation and navController of its own called mainAppNavController.
                 * the "rootNavController" is to navigate from this MainApp to a navgraph under the RootNavigation.
                 */
                MainApp(mainViewModel,rootNavController)
            }
        }
    }


    fun NavGraphBuilder.splashScreenNavGraph(navController: NavHostController){
        navigation(
            startDestination = SplashNavEnum.SPLASH_SCREEN.route,
            route = RootNavEnum.SPLASH_FLOW.route
        ) {
            composable(SplashNavEnum.SPLASH_SCREEN.route) {
                SplashScreen(
                    // After splashscreen, navigate to either mainApp or Auth Flow and clear auth backstack
                    onNavigateToNextScreen = {
                        navController.navigate(
                            if (isLoggedIn()) RootNavEnum.MAIN_APP.route else RootNavEnum.AUTH_FLOW.route
                        ) {
                            popUpTo(RootNavEnum.SPLASH_FLOW.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }

    private fun isLoggedIn(): Boolean {
        return mainViewModel.getCurrentUser() != null
    }

    fun NavGraphBuilder.authNavGraph(navController: NavController) {
        navigation(
            startDestination = AuthFlowNavigation.INTRO.route,
            route = RootNavEnum.AUTH_FLOW.route
        ) {
            composable(AuthFlowNavigation.INTRO.route) {

                IntroScreen(
                    onNavigateToLoginScreen = {
                        Log.i("intro btn 1", "pressed")
                        navController.navigate(AuthFlowNavigation.LOGIN.route)
                    }
                )
            }
            composable(AuthFlowNavigation.SIGN_UP.route) {
                SignUpScreen(mainViewModel){
                    mainViewModel.resetAuthState()
                    navController.navigate(AuthFlowNavigation.LOGIN.route)
                }
            }
            composable(AuthFlowNavigation.LOGIN.route) {
                LoginScreen(
                    // After login, navigate to main app and clear auth backstack
                    mainViewModel = mainViewModel,
                    onNavigateToMainScreen = {
                        mainViewModel.resetAuthState()
                        navController.navigate(RootNavEnum.MAIN_APP.route) {
                            popUpTo(RootNavEnum.AUTH_FLOW.route) { inclusive = true }
                        }
                    },
                    onNavigationToSignUpScreen = {
                        mainViewModel.resetAuthState()
                        navController.navigate(AuthFlowNavigation.SIGN_UP.route)
                    }
                )
            }
        }
    }

    @Composable
    fun MainApp(mainViewModel: MainViewModel, rootNavController: NavHostController){
        // another nav controller, not same with root. Its unique to MainApp
        val mainAppNavController = rememberNavController()

        val bottomNavItems = listOf<BottomNavItem>(
            BottomNavItem(
                id = UUID.randomUUID().toString(),
                name = "CapCount",
                route = MainAppNavigation.CAP_COUNT.route,
                icon = R.drawable.ic_capture1
            ),
            BottomNavItem(
                id = UUID.randomUUID().toString(),
                name = "TapCount",
                route = MainAppNavigation.TAP_COUNT.route,
                icon = R.drawable.ic_step
            ),
            BottomNavItem(
                id = UUID.randomUUID().toString(),
                name = "Counts",
                route = MainAppNavigation.HISTORY.route,
                icon = R.drawable.ic_arrange1
            ),
            BottomNavItem(
                id = UUID.randomUUID().toString(),
                name = "Profile",
                route = MainAppNavigation.PROFILE.route,
                icon = R.drawable.ic_profile0
            )
        )

        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    navItems = bottomNavItems,
                    navController = mainAppNavController,
                    onItemClick = {
                        /**
                         * The if statement below doesn't execute the "mainAppNavController.navigate" if ...
                         * the current route is the route a user is trying to navigate to.
                         * i.e if a user is in profile route and then reselects the profile again. it ...
                         * doesnt execute the block of code inside it (mainAppNavController.navigate).
                         *
                         * Help performance of app as it prevents "mainAppNavController.navigate" from executing entirely if...
                         * the current route is the route a user is trying to navigate to.
                         */
                        if (mainAppNavController.currentDestination?.route != it.route) {
                            mainAppNavController.navigate(it.route) {
                                // Pop all entries above CAP_COUNT, keeping CAP_COUNT
                                popUpTo(MainAppNavigation.TAP_COUNT.route) { inclusive = true }
                                launchSingleTop = true // doesn't add a route to back stack if route is reselected.
                            }
                        }
                    }
                )
            }
        ){
            Navigation(
                mainAppNavController = mainAppNavController,
                rootNavController = rootNavController,
                mainViewModel = mainViewModel
            )
        }
    }

    @Composable
    fun Navigation(mainAppNavController: NavHostController, rootNavController: NavHostController, mainViewModel: MainViewModel){

        /**
         * viewModel() -> If you wanted the view model class to be scoped to only a particular
         * composable e.g this navigation composable, this is where you instantiate it with viewModel().
         *
         * val mainViewModel: MainViewModel by viewModel()
         *
         * As long as this Navigation composable stays active, the view model stays active too.
         * If the composable recomposes, the view model goes to default and resets too
         */

        NavHost(navController = mainAppNavController, startDestination = MainAppNavigation.TAP_COUNT.route){
            composable(MainAppNavigation.CAP_COUNT.route){
                CapCountScreen()
                Log.i("Home screen Clicked", "Home screen Clicked")
            }
            composable(MainAppNavigation.TAP_COUNT.route){
                TapCountScreen(mainViewModel)
                Log.i("Home 1 screen Clicked", "Home 1 screen Clicked")
            }
            composable(MainAppNavigation.HISTORY.route){
                CountHistoryScreen(mainViewModel)
                Log.i("Home 2 screen Clicked", "Home 2 screen Clicked")
            }
            composable(MainAppNavigation.PROFILE.route){
                // Home 3 Screen
                ProfileScreen(
                    mainViewModel,
                    onNavigateToIntroScreen = {
                        rootNavController.navigate(RootNavEnum.AUTH_FLOW.route) {
                            popUpTo(RootNavEnum.MAIN_APP.route) { inclusive = true }
                        }
                    }
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
                navController.navigate(MainAppNavigation.TAP_COUNT.route) {
                    // Optional: Pop up to the start destination to avoid building up a large stack of destinations
                    popUpTo(MainAppNavigation.TAP_COUNT.route) {
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
            if(selected){
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun Permission() {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        var showPermanentlyDeniedDialog by remember { mutableStateOf(false) }
        var showTemporarilyDeniedDialog by remember { mutableStateOf(false) }

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