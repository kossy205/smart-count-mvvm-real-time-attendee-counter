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
import com.kosiso.smartcount.database.models.User
import com.kosiso.smartcount.ui.screen_states.MainOperationState.Loading
import com.kosiso.smartcount.ui.screen_states.MainOperationState.Success
import com.kosiso.smartcount.ui.screen_states.MainOperationState.Error
import com.kosiso.smartcount.ui.screen_states.MainOperationState.Idle
import com.kosiso.smartcount.ui.theme.BackgroundColor
import com.kosiso.smartcount.ui.theme.Black
import com.kosiso.smartcount.ui.theme.Pink
import com.kosiso.smartcount.ui.theme.White
import com.kosiso.smartcount.ui.theme.onest
import com.kosiso.smartcount.viewmodels.MainViewModel

@Preview(showBackground = true)
@Composable
private fun Preview(){
//    SignUpScreen(mainViewModel = MainViewModel, onNavigateToLoginScreen = {})
}

@Composable
fun SignUpScreen(
    mainViewModel: MainViewModel,
    onNavigateToLoginScreen: ()-> Unit
){
    SignUpFieldsSection(mainViewModel, onNavigateToLoginScreen)
}

@Composable
private fun SignUpFieldsSection(
    mainViewModel: MainViewModel,
    onNavigateToLoginScreen: ()-> Unit
){

    var textInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    val user: User = User(
        mainViewModel.getCurrentUser()?.uid.toString(),
        textInput,
        phoneInput,
        emailInput,
        passwordInput,
        "",
        0,
        emptyList()
    )

    CheckAuthOperationResult(mainViewModel, user)
    CheckRegisterOperationResult(onNavigateToLoginScreen, mainViewModel)

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
                            color = Black
                        )
                    ) {
                        append("Quickly ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = Pink
                        )
                    ) {
                        append("Create")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = Black
                        )
                    ) {
                        append(" \nA New Account")
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

            TextArea(
                textInput = textInput,
                onTextInputChange = {textInput = it}
            )

            Spacer(modifier  = Modifier.height(10.dp))

            EmailTextArea(
                emailInput = emailInput,
                onEmailInputChange = { emailInput = it }
            )

            Spacer(modifier  = Modifier.height(10.dp))

            PhoneNumberTextArea(
                phoneInput = phoneInput,
                onPhoneInputChange = { phoneInput = it }
            )

            Spacer(modifier  = Modifier.height(10.dp))

            PasswordTextArea(
                passwordInput = passwordInput,
                onPasswordInputChange = { passwordInput = it }
            )

            Spacer(modifier  = Modifier.height(30.dp))

            ButtonProgressBarSection(
                signUpUser = {
                    mainViewModel.signUpNewUser(
                        emailInput,
                        passwordInput
                    )
                },
                goToLoginScreen = {
                    onNavigateToLoginScreen()
                },
                mainViewModel = mainViewModel
            )
        }
    }
}

@Composable
private fun CheckAuthOperationResult(mainViewModel: MainViewModel, user: User){
    val context = LocalContext.current
    val authResult = mainViewModel.authOperationResult.collectAsState()

    LaunchedEffect(authResult.value) {
        when(val result = authResult.value){
            Idle -> { Log.i("signing up user 1", "idle") }

            Loading -> { Log.i("signing up user 1", "loading") }

            is Success -> {
                mainViewModel.registerNewUserInDB(user)
                Log.i("signing up user 1", "success: ${result.data}")
            }

            is Error -> {
                val errorMessage = result.message
                Toast.makeText(context, "Error: ${errorMessage}", Toast.LENGTH_LONG).show()
                Log.i("signing up user 1", errorMessage.toString())
            }
        }
    }

}

@Composable
private fun CheckRegisterOperationResult(
    onNavigateToLoginScreen: () -> Unit,
    mainViewModel: MainViewModel
){
    val context = LocalContext.current
    val registerResult = mainViewModel.registerOperationResult.collectAsState()

    LaunchedEffect(registerResult.value) {
        when(val result = registerResult.value){

            Idle -> { Log.i("register user 1", "idle") }

            Loading -> { Log.i("register user 1", "loading") }

            is Success -> {
                onNavigateToLoginScreen()
                Log.i("register user 1", "success: ${result.data}")
            }

            is Error -> {
                val errorMessage = result.message
                Toast.makeText(context, "Error: ${errorMessage}", Toast.LENGTH_LONG).show()
                Log.i("register user 1", errorMessage.toString())
            }
        }
    }

}


@Composable
private fun TextArea(
    textInput: String,
    onTextInputChange: (String) -> Unit
){
    OutlinedTextField(
        value = textInput,
        onValueChange = onTextInputChange,
        placeholder = {
            Text(
                text = "name",
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
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
        singleLine = true
    )
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
private fun PhoneNumberTextArea(
    phoneInput: String,
    onPhoneInputChange: (String) -> Unit
){
    OutlinedTextField(
        value = phoneInput,
        onValueChange = onPhoneInputChange,
        placeholder = {
            Text(
                text = "+234 123 456 7890",
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
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        ),
        singleLine = true,
//        visualTransformation = PhoneNumberVisualTransformation()
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
    signUpUser: () -> Unit,
    goToLoginScreen: () -> Unit,
    mainViewModel: MainViewModel
){

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .layoutId("signUp_button_section")
    ){

        val authResult by mainViewModel.authOperationResult.collectAsState()

        Button(
            onClick = {
                signUpUser()
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
                    text = "Sign Up",
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
                text = "Have an account?",
                style = TextStyle(
                    color = Black,
                    fontFamily = onest,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.width(3.dp))

            Text(
                text = "Login here",
                style = TextStyle(
                    color = Pink,
                    fontFamily = onest,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textDecoration = TextDecoration.Underline
                ),
                modifier = Modifier
                    .clickable{
                        goToLoginScreen()
                    }
            )
        }
    }

}