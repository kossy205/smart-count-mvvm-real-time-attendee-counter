package com.kosiso.smartcount.ui.uiScreens

import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosiso.smartcount.ui.screen_states.MainOperationState
import com.kosiso.smartcount.ui.screen_states.MainOperationState.Idle
import com.kosiso.smartcount.ui.screen_states.MainOperationState.Loading
import com.kosiso.smartcount.ui.screen_states.MainOperationState.Success
import com.kosiso.smartcount.ui.theme.BackgroundColor
import com.kosiso.smartcount.ui.theme.Black
import com.kosiso.smartcount.ui.theme.Pink
import com.kosiso.smartcount.ui.theme.White
import com.kosiso.smartcount.ui.theme.onest
import com.kosiso.smartcount.viewmodels.MainViewModel


@Preview(showBackground = true)
@Composable
private fun Preview(){
//    LoginScreen(mainViewModel = MainViewModel, onNavigateToMainScreen = {}, onNavigationToSignUpScreen = {})
}

@Composable
fun LoginScreen(
    mainViewModel: MainViewModel,
    onNavigateToMainScreen: ()-> Unit,
    onNavigationToSignUpScreen: ()-> Unit
){
    LoginFieldsSection(
        mainViewModel,
        onNavigateToMainScreen,
        onNavigationToSignUpScreen
    )
}


@Composable
private fun LoginFieldsSection(
    mainViewModel: MainViewModel,
    onNavigateToMainScreen: ()-> Unit,
    onNavigateToSignUpScreen: ()-> Unit
){

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }


    CheckAuthOperationResult(mainViewModel, onNavigateToMainScreen)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 15.dp)
            .padding(bottom = 65.dp)
    ){
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .layoutId("signUp_Fields_section")
        ) {

            Text(
                buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = Pink
                        )
                    ) {
                        append("Log in ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = Black
                        )
                    ) {
                        append("to ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = Black
                        )
                    ) {
                        append(" \nyour account")
                    }
                },
                style = TextStyle(
                    color = Black,
                    fontFamily = onest,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp
                ),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier  = Modifier.height(30.dp))

            EmailTextArea(
                emailInput = emailInput,
                onEmailInputChange = { emailInput = it }
            )

            Spacer(modifier  = Modifier.height(10.dp))

            PasswordTextArea(
                passwordInput = passwordInput,
                onPasswordInputChange = { passwordInput = it }
            )

            Spacer(modifier  = Modifier.height(30.dp))

            ButtonProgressBarSection(
                loginUser = {
                    mainViewModel.signInUser(
                        emailInput,
                        passwordInput
                    )
                },
                goToSignUpScreen = onNavigateToSignUpScreen,
                mainViewModel = mainViewModel
            )
        }
    }
}

@Composable
private fun CheckAuthOperationResult(
    mainViewModel: MainViewModel,
    onNavigateToMainScreen: () -> Unit
){
    val context = LocalContext.current
    val authResult = mainViewModel.authOperationResult.collectAsState()

    LaunchedEffect(authResult.value) {
        when(val result = authResult.value){
            Idle -> { Log.i("logging in user 1", "idle") }

            Loading -> { Log.i("logging in user 1", "loading") }

            is Success -> {
                Log.i("logging in user 1", "success: ${result.data}")
                onNavigateToMainScreen()
            }

            is MainOperationState.Error -> {
                val errorMessage = result.message
                Toast.makeText(context, "Error: ${errorMessage}", Toast.LENGTH_LONG).show()
                Log.i("logging in user 1", errorMessage.toString())
            }
        }
    }

}

@Composable
private fun EmailTextArea(
    emailInput: String,
    onEmailInputChange: (String) -> Unit
){
    OutlinedTextField(
        value = emailInput,
        onValueChange = onEmailInputChange,
        placeholder = {
            Text(
                text = "example@gmail.com",
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
            unfocusedBorderColor = Black.copy(alpha = 0.5f),
            focusedBorderColor = Pink,
        ),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        singleLine = true
    )
}

@Composable
private fun PasswordTextArea(
    passwordInput: String,
    onPasswordInputChange: (String) -> Unit
){
    OutlinedTextField(
        value = passwordInput,
        onValueChange = onPasswordInputChange,
        placeholder = {
            Text(
                text = "password",
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
            unfocusedBorderColor = Black.copy(alpha = 0.5f),
            focusedBorderColor = Pink,
        ),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        ),
        singleLine = true
    )
}

@Composable
private fun ButtonProgressBarSection(
    loginUser: () -> Unit,
    goToSignUpScreen: () -> Unit,
    mainViewModel: MainViewModel
){

    Column(
            verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
        .fillMaxWidth()
    ){

        val authResult by mainViewModel.authOperationResult.collectAsState()

        Button(
            onClick = {
                loginUser()
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
            if(authResult == Loading){
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(30.dp),
                    color = White,
                    strokeCap = StrokeCap.Round
                )
            }else{
                Text(
                    text = "Login",
                    style = TextStyle(
                        color = White,
                        fontFamily = onest,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }

        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ){
            Text(
                text = "Don't have an account?",
                style = TextStyle(
                    color = Black,
                    fontFamily = onest,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.width(3.dp))

            Text(
                text = "SignUp here",
                style = TextStyle(
                    color = Pink,
                    fontFamily = onest,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textDecoration = TextDecoration.Underline
                ),
                modifier = Modifier
                    .clickable{
                        goToSignUpScreen()
                    }
            )
        }
    }

}


private fun resetAuthState(mainViewModel: MainViewModel){
    mainViewModel.resetAuthState()
}
