package com.kosiso.smartcount.ui.uiScreens


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
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
import com.kosiso.smartcount.R
import com.kosiso.smartcount.database.models.User
import com.kosiso.smartcount.ui.screen_states.MainOperationState
import com.kosiso.smartcount.ui.screen_states.MainOperationState.Success
import com.kosiso.smartcount.ui.theme.BackgroundColor
import com.kosiso.smartcount.ui.theme.Black
import com.kosiso.smartcount.ui.theme.Pink
import com.kosiso.smartcount.ui.theme.Red
import com.kosiso.smartcount.ui.theme.White
import com.kosiso.smartcount.ui.theme.onest
import com.kosiso.smartcount.viewmodels.MainViewModel


@Preview()
@Composable
private fun Preview(){

//    ProfileFunctions()
//    ProfileDetails()
//    ProfileScreen()
}

@Composable
fun ProfileScreen(mainViewModel: MainViewModel, onNavigateToIntroScreen: ()-> Unit){

    mainViewModel.getUserDetailsFromRoomDB()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 15.dp)
    ){
        val constraints = ConstraintSet {
            val profileDetails = createRefFor("profile_details")
            val profileFunctions = createRefFor("profile_functions")

            constrain(profileDetails) {
                top.linkTo(parent.top, margin = 30.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
            constrain(profileFunctions) {
                top.linkTo(profileDetails.bottom, margin = 70.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        }
        ConstraintLayout(
            constraintSet = constraints,
            modifier = Modifier.fillMaxSize()
        ){
            ProfileDetails(mainViewModel)

            ProfileFunctions(mainViewModel, onNavigateToIntroScreen)
        }
    }
}


@Composable
private fun ProfileDetails(mainViewModel: MainViewModel){

    var showDialog by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    CheckGetUserFromRoomDBResult(
        mainViewModel = mainViewModel,
        onUserGotten = {user->
            name = user.name
            email = user.email
            phone = user.phone
        }
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .layoutId("profile_details")
    ){
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(100.dp))
                
        ){
            Image(
                painter = painterResource(id = R.drawable.dummy),
                contentDescription = "Profile Image",
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = name,
            style = TextStyle(
                color = Black,
                fontFamily = onest,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp
            )
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = email,
            style = TextStyle(
                color = Black.copy(alpha = 0.6f),
                fontFamily = onest,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = phone,
            style = TextStyle(
                color = Black.copy(alpha = 0.6f),
                fontFamily = onest,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                showDialog = true
            },
            modifier = Modifier
                .height(35.dp),
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Black
            )
        ){
            Text(
                text = "Edit Profile",
                style = TextStyle(
                    color = White,
                    fontFamily = onest,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            )
        }
    }

    if(showDialog){
        ShowCustomDialog(
            onDismiss = { showDialog = false },
            cancelButton = { showDialog = false },
            confirmButton = {
                mainViewModel.updateUserDetails(textInput)
                showDialog = false
            },
            textInput = textInput,
            onTextInputChange = { textInput = it }
        )
    }
}

@Composable
private fun ProfileFunctions(mainViewModel: MainViewModel, onNavigateToIntroScreen: () -> Unit){

    var showFeatureDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .layoutId("profile_functions")

    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                    .fillMaxWidth()
        ){
            FunctionBox(
                functionName = "Feature Request",
                iconId = R.drawable.ic_idea,
                contentColor = Black.copy(alpha = 0.8f),
                onClick = {
                    showFeatureDialog = true
                },
            )

            Divider(
                color = Color.Black.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            FunctionBox(
                functionName = "Support",
                iconId = R.drawable.ic_online,
                contentColor = Black.copy(alpha = 0.8f),
                onClick = {
                    showSupportDialog = true
                },
            )

            Divider(
                color = Color.Black.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            FunctionBox(
                functionName = "Request Account Deletion",
                iconId = R.drawable.ic_delete,
                contentColor = Black.copy(alpha = 0.8f),
                onClick = {
                    showDeleteDialog = true
                },
            )

            Divider(
                color = Color.Black.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            FunctionBox(
                functionName = "Logout",
                iconId = R.drawable.ic_logout,
                contentColor = Red.copy(alpha = 0.8f),
                onClick = {
                    showLogoutDialog = true
                },
            )

        }
    }

    if(showFeatureDialog){
        ShowFeatureDialog(
            showDialog = {
                showFeatureDialog = it
            }
        )
    }
    if(showSupportDialog){
        ShowSupportDialog(
            showDialog = {
                showSupportDialog = it
            }
        )
    }
    if(showDeleteDialog){
        ShowDeleteDialog(
            showDialog = {
                showDeleteDialog = it
            },
            mainViewModel = mainViewModel
            ,
            onNavigateToIntroScreen = {
                onNavigateToIntroScreen()
            }
        )
    }
    if(showLogoutDialog){
        ShowLogoutDialog(
            showDialog = {
                showLogoutDialog = it
            },
            mainViewModel = mainViewModel,
            onNavigateToIntroScreen = {
                onNavigateToIntroScreen()
            }
        )
    }
}

@Composable
fun ShowFeatureDialog(showDialog: (Boolean)-> Unit){
    val featureLink = "https://forms.gle/yY6ujpv5qSJTiaAY9"
    val context = LocalContext.current

    ShowDialog(
        dialogTitle = "Request A Feature",
        dialogContent = {
            Text(
                text = "Tell us what feature you'd love to see in Smart Counter and we would implement it ASAP! The goal is to make life convenient for you.",
                style = TextStyle(
                    color = Black,
                    fontFamily = onest,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp
                ),
                modifier = Modifier
                    .padding(15.dp)
            )
        },
        onDismiss = {showDialog(false)},
        cancelButton = {showDialog(false)},
        confirmButton = {
            openLink(context, featureLink)
            showDialog(false)
        },
        confirmButtonText = "Request"
    )
}

@Composable
fun ShowSupportDialog(showDialog: (Boolean)-> Unit){
    val supportLink = "https://forms.gle/d8H71nyCzVYKeRdp8"
    val context = LocalContext.current

    ShowDialog(
        dialogTitle = "Support Email",
        dialogContent = {
            Text(
                text = "Should include a detailed explanation of your problem or what you want to enquire.",
                style = TextStyle(
                    color = Black,
                    fontFamily = onest,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp
                ),
                modifier = Modifier
                    .padding(15.dp)
            )
        },
        onDismiss = {showDialog(false)},
        cancelButton = {showDialog(false)},
        confirmButton = {
            openLink(context, supportLink)
            showDialog(false)
        },
        confirmButtonText = "Got it"
    )
}

@Composable
fun ShowDeleteDialog(showDialog: (Boolean)-> Unit, mainViewModel: MainViewModel,onNavigateToIntroScreen: () -> Unit){
    val deleteLink = "https://forms.gle/D8UbHj1P3bUiUJYPA"
    val context = LocalContext.current

    ShowDialog(
        dialogTitle = "Delete account?",
        dialogContent = {
            Text(
                text = "Sad to see you go, if you have any issues or feature you would want, you can contact support." +
                        "\nStill want to delete account?",

                style = TextStyle(
                    color = Black,
                    fontFamily = onest,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp
                ),
                modifier = Modifier
                    .padding(15.dp)
            )
        },
        onDismiss = {showDialog(false)},
        cancelButton = {showDialog(false)},
        confirmButton = {
            mainViewModel.signOut()
            openLink(context, deleteLink)
            showDialog(false)
            onNavigateToIntroScreen()
        },
        confirmButtonText = "Yes"
    )
}

@Composable
fun ShowLogoutDialog(showDialog: (Boolean)-> Unit, mainViewModel: MainViewModel,onNavigateToIntroScreen: () -> Unit){

    ShowDialog(
        dialogTitle = "Logout",
        dialogContent = {
            Text(
                text = "Are you sure you want to logout? This would take you back to the login screen.",
                style = TextStyle(
                    color = Black,
                    fontFamily = onest,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp
                ),
                modifier = Modifier
                    .padding(15.dp)
            )
        },
        onDismiss = {showDialog(false)},
        cancelButton = {showDialog(false)},
        confirmButton = {
            mainViewModel.signOut()
            showDialog(false)
            onNavigateToIntroScreen()
        },
        confirmButtonText = "Yes"
    )
}

@Composable
private fun CheckGetUserFromRoomDBResult(
    mainViewModel: MainViewModel,
    onUserGotten:(User)->Unit){

    val userResult = mainViewModel.getUserDetailsFromRoomDBResult.collectAsState()

    when(val result = userResult.value){
        is Success ->{
            Log.i("get user from local db","success")
            val user = result.data
            onUserGotten(user)
        }
        is MainOperationState.Error ->{
            Log.i("get user from local db","failed: ${result.message}")
        }
        else ->{
            // loading and idle - do nothing
        }
    }
}

@Composable
private fun FunctionBox(
    functionName: String,
    iconId: Int,
    contentColor: Color,
    onClick: () -> Unit,
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(White)
            .clickable {
                onClick()
            }
    ){
        Box(
            modifier = Modifier
                .weight(0.15f),
            contentAlignment = Alignment.Center
        ){
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .fillMaxSize(),
                painter = painterResource(iconId),
                contentDescription = "",
                tint = contentColor
            )
        }

        Box(
            modifier = Modifier
                .weight(0.65f)
        ){
            Text(
                text = functionName,
                style = TextStyle(
                    color = contentColor,
                    fontFamily = onest,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            )
        }

        Box(
            modifier = Modifier
                .weight(0.15f),
            contentAlignment = Alignment.Center
        ){
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .fillMaxSize(),
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = "",
                tint = contentColor
            )
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
                    text = "Edit Name",
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
                            text = "new name",
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

@Composable
private fun ShowDialog(
    dialogTitle: String,
    dialogContent: @Composable () -> Unit,
    onDismiss: () -> Unit,
    cancelButton: () -> Unit,
    confirmButton: () -> Unit,
    confirmButtonText: String
){

    Dialog(onDismissRequest = {onDismiss()}){
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(1.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ){

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                Text(
                    text = dialogTitle,
                    style = TextStyle(
                        color = Black,
                        fontFamily = onest,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    ),
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(15.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                dialogContent()

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
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
                        onClick = {
                            confirmButton()
                        }
                    ) {
                        Text(
                            text = confirmButtonText,
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

private fun openLink(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}