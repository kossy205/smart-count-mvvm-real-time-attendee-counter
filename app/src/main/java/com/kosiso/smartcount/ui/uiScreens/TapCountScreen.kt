package com.kosiso.smartcount.ui.uiScreens

import android.content.Intent
import android.text.format.DateUtils
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Shape
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
import com.kosiso.smartcount.TapCountForeground
import com.kosiso.smartcount.database.models.Count
import com.kosiso.smartcount.repository.MainRepoImpl
import com.kosiso.smartcount.ui.theme.BackgroundColor
import com.kosiso.smartcount.ui.theme.Black
import com.kosiso.smartcount.ui.theme.Pink
import com.kosiso.smartcount.ui.theme.White
import com.kosiso.smartcount.ui.theme.onest
import com.kosiso.smartcount.ui.ui_utils.Common
import com.kosiso.smartcount.utils.Constants
import com.kosiso.smartcount.viewmodels.MainViewModel
import java.sql.Timestamp
import java.util.Date


@Preview(showBackground = true, backgroundColor = 0xFF00FF00)
@Composable
private fun Preview(){
    SessionCountSection()

}


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
            val countDetailsSection = createRefFor("count_details_section")
            val countButtonsSection = createRefFor("count_buttons_section")

            constrain(topIconSection){
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }

            constrain(countDetailsSection){
                top.linkTo(topIconSection.bottom, margin = 70.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
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

            CountButtonsSection(mainViewModel)
        }
    }
}

@Composable
private fun SendCommandToService(action: String){
    val context = LocalContext.current
    LaunchedEffect(key1 = action) {
        Log.i("send command", "works")
        Intent(context, TapCountForeground::class.java).also{
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
private fun TopIconSection(mainViewModel: MainViewModel){
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

            Row {
//                Common.IconButtonDesign(
//                    iconId = R.drawable.ic_profile0,
//                    iconColor = Black,
//                    backgroundColor = White,
//                    onIconClick = {
//
//                    }
//                )
//                Spacer(modifier = Modifier.width(5.dp))
                Common.IconButtonDesign(
                    iconId = R.drawable.ic_capture1,
                    iconColor = Black,
                    backgroundColor = White,
                    onIconClick = {

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

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ){
            Common.IconButtonDesign(
                iconId = R.drawable.ic_save,
                iconColor = White,
                backgroundColor = Pink,
                onIconClick = {
                    showDialog = true
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){
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
                    .weight(0.7f)
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

@Composable
private fun SessionCountSection(){

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
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
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .fillMaxWidth()
                .background(White)
                .height(200.dp)
        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
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
                    onClick = {

                    },
                    modifier = Modifier
                        .width(100.dp)
                        .height(20.dp)
                        .border(
                            width = 1.dp,
                            color = Color.Black,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    shape = RoundedCornerShape(10.dp),
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