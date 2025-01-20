package com.kosiso.smartcount.ui.uiScreens

import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.layoutId
import androidx.core.content.ContentProviderCompat.requireContext
import com.kosiso.smartcount.R
import com.kosiso.smartcount.TapCountForeground
import com.kosiso.smartcount.repository.MainRepository
import com.kosiso.smartcount.ui.theme.BackgroundColor
import com.kosiso.smartcount.ui.theme.Black
import com.kosiso.smartcount.ui.theme.Pink
import com.kosiso.smartcount.ui.theme.White
import com.kosiso.smartcount.ui.theme.onest
import com.kosiso.smartcount.ui.utils.Common
import com.kosiso.smartcount.utils.Constants
import com.kosiso.smartcount.viewmodels.MainViewModel


@Preview(showBackground = true)
@Composable
private fun Preview(){
//    TapCountScreen(mainViewModel = MainViewModel())
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
            TopIconSection()

            CountDetailsSection(mainViewModel)

            CountButtonsSection(mainViewModel)
        }
    }
}

@Composable
private fun SendCommandToService(action: String){
    val context = LocalContext.current
    Intent(context, TapCountForeground::class.java).also{
        it.action = action
        // this starts the service
        // while the "startForeground(id.notification)" is what makes or promote it to a foreground
        context.startService(it)
    }
}


@Composable
private fun TopIconSection(){
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
                iconId = R.drawable.ic_arrange1,
                iconColor = White,
                backgroundColor = Pink,
                onIconClick = {

                }
            )

            Row {
                Common.IconButtonDesign(
                    iconId = R.drawable.ic_profile0,
                    iconColor = Black,
                    backgroundColor = White,
                    onIconClick = {

                    }
                )
                Spacer(modifier = Modifier.width(5.dp))
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
                iconId = R.drawable.ic_arrange1,
                iconColor = White,
                backgroundColor = Pink,
                onIconClick = {

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
                    topStart = 10.dp,
                    bottomStart = 10.dp,
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
                    topEnd = 10.dp,
                    bottomEnd = 10.dp
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