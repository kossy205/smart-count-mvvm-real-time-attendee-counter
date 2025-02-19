package com.kosiso.smartcount.ui.uiScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosiso.smartcount.ui.theme.Black
import com.kosiso.smartcount.ui.theme.Pink
import com.kosiso.smartcount.ui.theme.onest

@Composable
fun SignUpScreen(){

}

@Composable
private fun SignUpDetailsSection(){

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
            unfocusedBorderColor = Black.copy(alpha = 0.2f),
            focusedBorderColor = Pink,
        ),
        shape = RoundedCornerShape(12.dp)
    )
}