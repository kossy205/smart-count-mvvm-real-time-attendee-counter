package com.kosiso.smartcount.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

object Common {

    @Composable
    fun ShowSnackBar(
        snackBarHostState: SnackbarHostState,
        message: String,
        duration: SnackbarDuration
    ){
        LaunchedEffect(key1 = message) { // key1 triggers recomposition when message changes
            snackBarHostState.showSnackbar(
                message = message,
                duration = duration
            )
        }
    }

    @Composable
    fun ShowDialog(
        titleText: String,
        dialogText: String,
        positiveButtonText: String,
        negativeButtonText: String,
        confirmButtonClick:() -> Unit,
        dismissButtonClick:() -> Unit){
        AlertDialog(
            onDismissRequest = {  },
            confirmButton = {
                TextButton(onClick = { confirmButtonClick() }) {
                    Text(positiveButtonText)
                }
            },
            dismissButton = {
                TextButton(onClick = { dismissButtonClick() }) {
                    Text(negativeButtonText)
                }
            },
            title = {
                Text(titleText)
            },
            text = {
                Text(dialogText)
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        )
    }


    @Composable
    fun IconButtonDesign(
        iconId: Int,
        iconColor: Color,
        backgroundColor: Color,
        onIconClick: () -> Unit,
        modifier: Modifier = Modifier
    ){
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .clickable(
                    enabled = true,
                    onClick = onIconClick
                ),
            contentAlignment = Alignment.Center
        ){
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .fillMaxSize(),
                painter = painterResource(iconId),
                contentDescription = "",
                tint = iconColor
            )
        }
    }
}