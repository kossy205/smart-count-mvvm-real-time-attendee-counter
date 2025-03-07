package com.kosiso.smartcount.ui.uiScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosiso.smartcount.ui.theme.BackgroundColor
import com.kosiso.smartcount.ui.theme.Black
import com.kosiso.smartcount.ui.theme.Pink
import com.kosiso.smartcount.ui.theme.White
import com.kosiso.smartcount.ui.theme.onest

@Preview(showBackground = true)
@Composable
private fun Preview(){
    IntroScreen(onNavigateToLoginScreen = {})
}

@Composable
fun IntroScreen(onNavigateToLoginScreen: ()-> Unit){
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 15.dp)
            .padding(bottom = 65.dp)
    ){
        Text(
            text = "Intro Screen",
            style = TextStyle(
                color = Black,
                fontFamily = onest,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 35.sp
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                onNavigateToLoginScreen
            },
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                bottomStart = 12.dp,
                topEnd = 12.dp,
                bottomEnd = 12.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Pink
            )
        ) {
            Text(
                text = "Go To Login",
                style = TextStyle(
                    color = White,
                    fontFamily = onest,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )
        }

    }

}
