package com.kosiso.smartcount.ui.uiScreens


import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.kosiso.smartcount.ui.menu.BottomNavItem
import com.kosiso.smartcount.ui.utils.Common
import com.kosiso.smartcount.R
import com.kosiso.smartcount.ui.theme.BackgroundColor
import com.kosiso.smartcount.ui.theme.Black
import com.kosiso.smartcount.ui.theme.Pink
import com.kosiso.smartcount.ui.theme.White
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            Permission()

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
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {

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
                // Home 2 Screen

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


    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun Permission(){
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        var showPermanentlyDeniedDialog by remember { mutableStateOf(false) }
        var showTemporarilyDeniedDialog by remember { mutableStateOf(false) }

        val permissionState = rememberPermissionState(
            Manifest.permission.CAMERA
        )

        // Permission launcher for manual permission request
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                when {
                    isGranted -> {
                        // Permission was granted
                        Log.i("permission granted", "granted ${permissionState.status} and $isGranted")
                    }
                    permissionState.status.shouldShowRationale -> {
                        // Permission was denied, but we can ask again
                        showTemporarilyDeniedDialog = true
                        Log.i("permission denied", "temporal ${permissionState.status.shouldShowRationale}")
                    }
                    !permissionState.status.shouldShowRationale && !isGranted-> {
                        // Permission permanently denied
                        showPermanentlyDeniedDialog = true
                        Log.i("permission denied", "permanent ${!permissionState.status.shouldShowRationale} and ${!permissionState.status.isGranted}")
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
                        // Only launch request if the permission is not granted
                        if (!permissionState.status.isGranted) {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)

                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }
        )


        if(showTemporarilyDeniedDialog){
            Common.ShowDialog(
                titleText = "Camera Permission",
                dialogText = "Pls grant Camera permission to use this app.",
                positiveButtonText = "Grant",
                negativeButtonText = "Dismiss",
                confirmButtonClick = {
                    showTemporarilyDeniedDialog = false
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                dismissButtonClick = {
                    showTemporarilyDeniedDialog = false
                }
            )
        }
        if(showPermanentlyDeniedDialog){
            Common.ShowDialog(
                titleText = "Camera Permission",
                dialogText = "Camara permission is required for this app to work. You can go to settings and manually grant permission.",
                positiveButtonText = "Go to settings",
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
        }
        return super.onKeyDown(keyCode, event)
    }


}