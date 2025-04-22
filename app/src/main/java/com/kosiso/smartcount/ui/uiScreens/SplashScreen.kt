package com.kosiso.smartcount.ui.uiScreens

import android.os.Handler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.kosiso.smartcount.ui.theme.Pink
import com.kosiso.smartcount.ui.theme.White
import com.kosiso.smartcount.ui.theme.onest
import kotlinx.coroutines.delay

@Preview(showBackground = true)
@Composable
private fun Preview(){
    SplashScreen {  }
}


@Composable
fun SplashScreen(onNavigateToNextScreen: ()-> Unit){
    val visible = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible.value = true
        delay(4000L)
        onNavigateToNextScreen()
    }
    LaunchedEffect(Unit) {
        delay(3000L)
        onNavigateToNextScreen()
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Pink
    ) {
        Box(
            contentAlignment = Alignment.Center
        ){
            AnimatedVisibility(
                visible = visible.value,
                enter = fadeIn(
                    initialAlpha = 0f,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000)
                )
            ) {
                Text(
                    text = "Smart Count",
                    style = TextStyle(
                        color = White,
                        fontFamily = onest,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 35.sp
                    )
                )
            }
        }

    }
}