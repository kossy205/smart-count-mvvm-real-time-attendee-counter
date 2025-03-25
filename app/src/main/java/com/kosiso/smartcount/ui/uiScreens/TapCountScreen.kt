package com.kosiso.smartcount.ui.uiScreens

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.layoutId
import com.kosiso.smartcount.R
import com.kosiso.smartcount.Foreground
import com.kosiso.smartcount.database.models.Count
import com.kosiso.smartcount.database.models.User
import com.kosiso.smartcount.ui.screen_states.MainOperationState
import com.kosiso.smartcount.ui.screen_states.MainOperationState.Idle
import com.kosiso.smartcount.ui.screen_states.MainOperationState.Loading
import com.kosiso.smartcount.ui.screen_states.MainOperationState.Success
import com.kosiso.smartcount.ui.theme.BackgroundColor
import com.kosiso.smartcount.ui.theme.Black
import com.kosiso.smartcount.ui.theme.Green
import com.kosiso.smartcount.ui.theme.Pink
import com.kosiso.smartcount.ui.theme.White
import com.kosiso.smartcount.ui.theme.onest
import com.kosiso.smartcount.ui.ui_utils.Common
import com.kosiso.smartcount.utils.Constants
import com.kosiso.smartcount.viewmodels.MainViewModel

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale


@Preview(showBackground = true, backgroundColor = 0xFF00FF00)
@Composable
private fun Preview(){
    SessionCountSection()
}


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun TapCountScreen(mainViewModel: MainViewModel){

    SendCommandToService(Constants.ACTION_START)
    Log.i("tap count screen", "visible")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 15.dp)
            .padding(bottom = 65.dp)
    ){
        val constraints = ConstraintSet {
            val topIconSection = createRefFor("top_icon_section")
            val sessionCountSection = createRefFor("session_count_section")
            val countDetailsSection = createRefFor("count_details_section")
            val countButtonsSection = createRefFor("count_buttons_section")

            constrain(topIconSection){
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }

            constrain(countDetailsSection){
                top.linkTo(topIconSection.bottom, margin = 25.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }

            constrain(sessionCountSection){
                top.linkTo(countDetailsSection.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(countButtonsSection.top)
                verticalBias = 0.8f
            }

            constrain(countButtonsSection){
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom, margin = 10.dp)
            }
        }

        Log.i("tap count screen 1", "visible")

        ConstraintLayout(
            constraintSet = constraints,
            modifier = Modifier.fillMaxSize()
        ){
            TopIconSection(mainViewModel)

            CountDetailsSection(mainViewModel)

            SessionCountSection()

            CountButtonsSection(mainViewModel)
        }
    }
}

@Composable
private fun SendCommandToService(action: String){
    val context = LocalContext.current
    LaunchedEffect(key1 = action) {
        Log.i("send command", "works")
        Intent(context, Foreground::class.java).also{
            Log.i("send command 1", "works")
            it.action = action
            // this starts the service
            // while the "startForeground(id.notification)" is what makes or promote it to a foreground
            context.startService(it)
            Log.i("send command 2", "works")
        }
        Log.i("send command 3", "works")
    }

}

@Composable
private fun SendLocationUpdateCommand(action: String){
    val context = LocalContext.current
    LaunchedEffect(key1 = action) {
        Log.i("send location command", "works")
        Intent(context, Foreground::class.java).also{
            Log.i("send location command 1", "works")
            it.action = action
            // this starts the service
            // while the "startForeground(id.notification)" is what makes or promote it to a foreground
            context.startService(it)
            Log.i("send location command 2", "works")
        }
        Log.i("send location command 3", "works")
    }

}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
private fun CheckUploadToAvailableUsersDBResult(mainViewModel: MainViewModel){

    val uploadResult = mainViewModel.uploadToAvailableUsersDBResult.collectAsState()
    when (val result = uploadResult.value) {
        Idle -> {
            Log.i("upload available user", "idle")
        }
        Loading -> {
            Log.i("upload available user", "loading")
        }
        is Success -> {
            SendLocationUpdateCommand(Constants.ACTION_START_LOCATION_UPDATE)
            Log.i("upload available user", "success")
        }
        is MainOperationState.Error -> {
            val errorMessage = result.message
            Log.i("upload available user", errorMessage)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
private fun TopIconSection(mainViewModel: MainViewModel){

    val context = LocalContext.current
    val onlineStatusData = mainViewModel.onlineStatus.collectAsState().value
    var isOnline by remember { mutableStateOf(false) }

    var shouldRequestPermission by remember { mutableStateOf(false) }
    val permissions = when {
        // android 10 and above
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
        // android 9 and below
        else -> arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
    // Update permission state reactively
    var arePermissionsGranted by remember {
        mutableStateOf(
            permissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }
    Log.i("permission in icon section granted?", "$arePermissionsGranted")

    // get user details success/error
    LaunchedEffect(Unit) {
        mainViewModel.getUserDetails()
    }
    val getUserDetails = mainViewModel.getUserDetailsResult.collectAsState()
    val userDetail: User? = when (val result = getUserDetails.value) {
        Idle -> {
            Log.i("getting user details", "idle")
            null
        }
        Loading -> {
            Log.i("getting user details", "loading")
            null
        }
        is Success<User> -> {
            val user = result.data
            Log.i("getting user details", "success: $user")
            user
        }
        is MainOperationState.Error -> {
            val errorMessage = result.message
            Log.i("getting user details", errorMessage)
            isOnline = false
            Toast.makeText(context, "Error coming online, make sure you have a good internet connection and try again", Toast.LENGTH_LONG).show()
            null
        }
    }

    val user = User(
        id = mainViewModel.getCurrentUser()?.uid.toString(),
        name = userDetail?.name.toString(),
        phone = userDetail?.phone.toString(),
        email = userDetail?.email.toString(),
        password = userDetail?.password.toString(),
        image = userDetail?.image.toString(),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .height(74.dp)
            .layoutId("top_icon_section")
    ){
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Common.IconButtonDesign(
                iconId = R.drawable.ic_reset_bold,
                iconColor = White,
                backgroundColor = Pink,
                onIconClick = {
                    mainViewModel.reset()
                }
            )


            if(onlineStatusData == true){
                isOnline = true
                Common.IconButtonDesign(
                    iconId = R.drawable.ic_online,
                    iconColor = White,
                    backgroundColor = Green,
                    onIconClick = {
                        mainViewModel.onlineStatus(!isOnline)
                    }
                )
                // location action is sent to foreground only if the "addToAvailableUsersDB" is successful
                // so to get location updates, "addToAvailableUsersDB" need to be successful first
                mainViewModel.addToAvailableUsersDB(user)
                CheckUploadToAvailableUsersDBResult(mainViewModel)
            }else{
                isOnline = false
                Common.IconButtonDesign(
                    iconId = R.drawable.ic_offline,
                    iconColor = Black,
                    backgroundColor = White,
                    onIconClick = {
                        if(arePermissionsGranted == true){
                            // permissions granted
                            mainViewModel.onlineStatus(!isOnline)
                        }else{
                            shouldRequestPermission = true
                        }
                    }
                )
                mainViewModel.removeFromAvailableUserDB()
                SendLocationUpdateCommand(Constants.ACTION_STOP_LOCATION_UPDATE)
                // location action is sent to foreground once the online status is false.
                // doesnt wait for any success
            }

            if(shouldRequestPermission){
                LocationPermission(
                    onGranted = {
                        // Update permission status when granted
                        // Makes it known that the permissions has been granted
                        arePermissionsGranted = permissions.all {
                            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                        }
                        mainViewModel.onlineStatus(!isOnline)
                        shouldRequestPermission = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CountDetailsSection(mainViewModel: MainViewModel){

    val displayedCount = mainViewModel.count.collectAsState(initial = 0).value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .layoutId("count_details_section"),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ){

        Text(
            text = "Attendees",
            style = TextStyle(
                color = Black,
                fontFamily = onest,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        )

        Spacer(modifier = Modifier.height(1.dp))

        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "$displayedCount",
                style = TextStyle(
                    color = Black,
                    fontFamily = onest,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 40.sp
                )
            )

            Spacer(modifier = Modifier.width(3.dp))

            Text(
                text = "ppl",
                style = TextStyle(
                    color = Black.copy(alpha = 0.3f),
                    fontFamily = onest,
                    fontWeight = FontWeight.Medium,
                    fontSize = 25.sp
                ),
                modifier = Modifier
                    .padding(top = 1.dp)
            )

        }

        Spacer(modifier = Modifier.height(1.dp))

        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = "Max in a Section:",
                style = TextStyle(
                    color = Black.copy(alpha = 0.3f),
                    fontFamily = onest,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.width(2.dp))

            Text(
                text = "50",
                style = TextStyle(
                    color = Black,
                    fontFamily = onest,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            )
        }
    }
}

@Composable
private fun SessionCountSection(){
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .layoutId("session_count_section")
    ){
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
        ){
//            Text(
//                text = "Session Count",
//                style = TextStyle(
//                    color = Black,
//                    fontFamily = onest,
//                    fontWeight = FontWeight.Medium,
//                    fontSize = 16.sp
//                ),
//                modifier = Modifier.align(Alignment.Start)
//            )
//            Spacer(modifier = Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .fillMaxWidth()
                    .background(Black)
            ){
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ){
                    Column(
                        verticalArrangement = Arrangement.Center,
                    ){
                        Text(
                            text = "Session Count",
                            style = TextStyle(
                                color = White.copy(alpha = 0.8f),
                                fontFamily = onest,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier.padding(bottom = 7.dp)
                        )
                        Text(
                            text = "Count with other users who are \nonline and 200 meters around you.",
                            style = TextStyle(
                                color = White.copy(alpha = 0.6f),
                                fontFamily = onest,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        )
                    }

                    Common.IconButtonDesign(
                        iconId = R.drawable.ic_play,
                        iconColor = White,
                        backgroundColor = White.copy(alpha = 0.2f),
                        onIconClick = {

                        },
                        modifier = Modifier
                            .size(25.dp)
                    )
                }

            }
        }
    }
}

@Composable
private fun CountButtonsSection(mainViewModel: MainViewModel){

    var showDialog by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    val displayedCount = mainViewModel.count.collectAsState(initial = 0).value
    val context = LocalContext.current

    val countHistory = Count(
        count = displayedCount,
        countName = "$textInput",
        countType = "Individual"
    )


    if (showDialog) {
        if (displayedCount > 0) {
            ShowCustomDialog(
                onDismiss = { showDialog = false },
                cancelButton = { showDialog = false },
                confirmButton = {
                    mainViewModel.insertCount(countHistory)
                    showDialog = false
                },
                textInput = textInput,
                onTextInputChange = { textInput = it }
            )
        } else {
            LaunchedEffect(Unit) {
                showDialog = false
                Toast.makeText(context, "Count must be greater than 0", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .layoutId("count_buttons_section"),
    ){

//        Row(
//            modifier = Modifier
//                .fillMaxWidth(),
//            horizontalArrangement = Arrangement.End,
//            verticalAlignment = Alignment.CenterVertically
//        ){
//            Common.IconButtonDesign(
//                iconId = R.drawable.ic_save,
//                iconColor = White,
//                backgroundColor = Pink,
//                onIconClick = {
//                    showDialog = true
//                }
//            )
//        }
//
//        Spacer(modifier = Modifier.height(50.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){
            Common.IconButtonDesign(
                iconId = R.drawable.ic_save,
                iconColor = White,
                backgroundColor = Pink,
                onIconClick = {
                    showDialog = true
                },
                modifier = Modifier.weight(0.1f)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = {
                    mainViewModel.decrement()
                },
                modifier = Modifier
                    .weight(0.3f)
                    .height(50.dp),
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    bottomStart = 12.dp,
                    topEnd = 0.dp,
                    bottomEnd = 0.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Black
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_subtract),
                    contentDescription = "subtract",
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = {
                    mainViewModel.increment()
                },
                modifier = Modifier
                    .weight(0.6f)
                    .height(50.dp),
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    bottomStart = 0.dp,
                    topEnd = 12.dp,
                    bottomEnd = 12.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Pink
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = "add",
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShowCustomDialog(
    onDismiss: () -> Unit,
    cancelButton: () -> Unit,
    confirmButton: () -> Unit,
    textInput: String,
    onTextInputChange: (String) -> Unit
){
    Dialog(onDismissRequest = onDismiss){
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ){

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Text(
                    text = "Count Name",
                    style = TextStyle(
                        color = Black,
                        fontFamily = onest,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = textInput,
                    onValueChange = onTextInputChange,
                    placeholder = {
                        Text(
                            text = "Sunday Combined Service",
                            style = TextStyle(
                                color = Black.copy(alpha = 0.4f),
                                fontFamily = onest,
                                fontWeight = FontWeight.Normal,
                                fontSize = 15.sp
                            )
                        )
                    },
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontFamily = onest,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Black.copy(alpha = 0.2f),
                        focusedBorderColor = Pink,
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Bottom
                ){

                    TextButton(
                        onClick = cancelButton
                    ) {
                        Text(
                            text = "Cancel",
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = confirmButton
                    ) {
                        Text(
                            text = "Save",
                            style = TextStyle(
                                color = Pink,
                                fontFamily = onest,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        )
                    }

                }

            }

        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermission(onGranted: @Composable () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showPermanentlyDeniedDialog by remember { mutableStateOf(false) }
    var showTemporarilyDeniedDialog by remember { mutableStateOf(false) }
    var isAllGranted by remember { mutableStateOf(false) }
    var shouldOpenAppSettingToAcceptPermission by remember { mutableStateOf(false) }

    val permissions = when {
        // android 10 and above
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
        // android 9 and below
        else -> arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
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
                        isAllGranted = true
                        Log.i("location permission granted", "granted $permission")
                    }
                    permissionStates.find { it.permission == permission }?.status?.shouldShowRationale == true -> {
                        // Permission was denied, but we can ask again
                        showTemporarilyDeniedDialog = true
                        Log.i("location permission denied", "temporal $permission")
                    }
                    else -> {
                        // Permission permanently denied
                        showPermanentlyDeniedDialog = true
                        Log.i("location permission denied", "permanent $permission")
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

    // Invoke onGranted when permissions are granted
    if (isAllGranted) {
        onGranted() // This is now in a composable context
    }

    // Show dialog for temporarily denied permissions
    if (showTemporarilyDeniedDialog) {
        Common.ShowDialog(
            titleText = "Permission Required",
            dialogText = "Please grant location permissions to count with other users.",
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
            dialogText = "Locations permissions are required to count with other users. You can go to settings and manually grant them.",
            positiveButtonText = "Go to Settings",
            negativeButtonText = "Dismiss",
            confirmButtonClick = {
                showPermanentlyDeniedDialog = false
                // Launcher to open app settings
                shouldOpenAppSettingToAcceptPermission = true
            },
            dismissButtonClick = {
                showPermanentlyDeniedDialog = false
            }
        )
    }

    if(shouldOpenAppSettingToAcceptPermission){
        LaunchAppSetting()
    }
}

@Composable
private fun LaunchAppSetting(){
    val context = LocalContext.current
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        onResult = {  }
    )
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    settingsLauncher.launch(intent)
}





























@Composable
private fun SessionCountSection1(){
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(250.dp)
            .layoutId("session_count_section")
    ){
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ){
            Text(
                text = "Session Count",
                style = TextStyle(
                    color = Black,
                    fontFamily = onest,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(White)
            ){
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.Top,
                ){
                    Text(
                        text = "Want to start a session count?",
                        style = TextStyle(
                            color = Black.copy(alpha = 0.5f),
                            fontFamily = onest,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = "You will be able to count with other users who are online and 200 meters around you.",
                        style = TextStyle(
                            color = Black.copy(alpha = 0.4f),
                            fontFamily = onest,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier =  Modifier.height(10.dp))
                    Button(
                        onClick = { /* Add your click action here */ },
                        modifier = Modifier
                            .width(90.dp)
                            .height(40.dp)
                            .border(
                                width = 1.dp,
                                color = Black.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(30.dp)
                            )
                            .clip(RoundedCornerShape(30.dp)), // clips the border to match the shape and height of the button
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Text(
                            text = "Start",
                            style = TextStyle(
                                color = Black.copy(alpha = 0.5f),
                                fontFamily = onest,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun RequestLocationPermission(permissions: Array<String>){
    requestMultiplePermissions(
        permissions = permissions,
        onPermissionsGranted = {
            // permissions granted
        },
        onPermissionsDenied = {

        }
    )
}

// check if all specified permissions are granted
@Composable
fun ArePermissionsGranted(vararg permissions: String): Boolean {
    val context = LocalContext.current
    var allGranted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        allGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    return allGranted
}

// Function to request multiple permissions
@Composable
fun requestMultiplePermissions(
    permissions: Array<String>,
    onPermissionsGranted: () -> Unit, // all permissions are granted
    onPermissionsDenied: (Map<String, Boolean>) -> Unit = {} // Called with permission results if any denied
): () -> Unit { // Returns a function to trigger the request
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val allGranted = result.values.all { it } //    all permissions were granted
        if (allGranted) {
            onPermissionsGranted()
        } else {
            onPermissionsDenied(result)
            // Show rationale if any permission needs explanation
            showRationale = permissions.any {
                ActivityCompat.shouldShowRequestPermissionRationale(context.findActivity(), it)
            }
        }
    }

    // Rationale dialog
    if (showRationale) {
        PermissionRationaleDialog(
            onDismiss = { showRationale = false },
            onConfirm = {
                permissionLauncher.launch(permissions)
                showRationale = false
            }
        )
    }

    // Return the function to trigger the request
    return {
        val shouldShowRationale = permissions.any {
            ActivityCompat.shouldShowRequestPermissionRationale(context.findActivity(), it)
        }
        if (shouldShowRationale) {
            showRationale = true
        } else {
            permissionLauncher.launch(permissions)
        }
    }
}


// Rationale dialog composable
@Composable
fun PermissionRationaleDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permissions Needed") },
        text = { Text("This app needs location permissions to be able to count with other users. Please grant them.") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Helper to get Activity from Context
fun Context.findActivity(): ComponentActivity {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is ComponentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    throw IllegalStateException("No ComponentActivity found")
}

//@OptIn(ExperimentalPermissionsApi::class)
//@Composable
//fun PermissionPartialOrPermanentDenial(){
//    when {
//        permissionState.allGranted -> {
//            // all permissions granted
//
//        }
//        permissionState.grantedPermissions.isNotEmpty() && permissionState.deniedPermissions.isNotEmpty() -> {
//            // Partial grant case
//
//        }
//        permissionState.permanentlyDeniedPermissions.isNotEmpty() -> {
//            // Permanent denial case
//
//        }
//    }
//}

//@Composable
//private fun ShowAddCountersDialog(
//    onDismiss: () -> Unit,
//    cancelButton: () -> Unit,
//    confirmButton: () -> Unit
//){
//    Dialog(onDismissRequest = onDismiss){
//        Surface(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            shape = MaterialTheme.shapes.medium,
//            color = MaterialTheme.colorScheme.surface
//        ){
//
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp)
//            ) {
//
//                Text(
//                    text = "Available Users",
//                    style = TextStyle(
//                        color = Black,
//                        fontFamily = onest,
//                        fontWeight = FontWeight.Medium,
//                        fontSize = 16.sp
//                    )
//                )
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                LazyColumn {
//                    items(
//                        items = count,
//                        key = { it.id }
//                    ) { count ->
//
//                        SwipeToDelete(
//                            onDelete = {
//                                mainViewModel.deleteCount(count.id)
//                            },
//                            countItem = {
//                                CountItem(count)
//                            }
//                        )
//
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth(),
//                    horizontalArrangement = Arrangement.End,
//                    verticalAlignment = Alignment.Bottom
//                ){
//
//                    TextButton(
//                        onClick = cancelButton
//                    ) {
//                        Text(
//                            text = "Cancel",
//                            style = TextStyle(
//                                color = Black,
//                                fontFamily = onest,
//                                fontWeight = FontWeight.SemiBold,
//                                fontSize = 14.sp
//                            )
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.width(8.dp))
//
//                    TextButton(
//                        onClick = confirmButton
//                    ) {
//                        Text(
//                            text = "Save",
//                            style = TextStyle(
//                                color = Pink,
//                                fontFamily = onest,
//                                fontWeight = FontWeight.SemiBold,
//                                fontSize = 14.sp
//                            )
//                        )
//                    }
//
//                }
//
//            }
//
//        }
//    }
//}