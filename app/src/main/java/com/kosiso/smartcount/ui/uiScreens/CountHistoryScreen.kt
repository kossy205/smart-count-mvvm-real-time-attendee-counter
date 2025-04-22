package com.kosiso.smartcount.ui.uiScreens

import android.text.format.DateUtils
import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosiso.smartcount.ui.ui_utils.Common
import com.kosiso.smartcount.R
import com.kosiso.smartcount.database.models.Count
import com.kosiso.smartcount.ui.screen_states.MainOperationState
import com.kosiso.smartcount.ui.screen_states.MainOperationState.Loading
import com.kosiso.smartcount.ui.screen_states.MainOperationState.Success
import com.kosiso.smartcount.ui.screen_states.MainOperationState.Error
import com.kosiso.smartcount.ui.screen_states.MainOperationState.Idle
import com.kosiso.smartcount.ui.theme.BackgroundColor
import com.kosiso.smartcount.ui.theme.Black
import com.kosiso.smartcount.ui.theme.Pink
import com.kosiso.smartcount.ui.theme.White
import com.kosiso.smartcount.ui.theme.onest
import com.kosiso.smartcount.utils.CountType
import com.kosiso.smartcount.viewmodels.MainViewModel
import java.sql.Timestamp
import java.util.Date


@Preview()
@Composable
private fun Preview(){
 CountItem(Count(
     countName = "Combined Service",
     countType = "Individual",
     count = 101
 ))
}

@Composable
fun CountHistoryScreen(mainViewModel: MainViewModel){

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 15.dp)
            .padding(bottom = 65.dp)

    ){
        Column{

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Count History",
                style = TextStyle(
                    color = Black,
                    fontFamily = onest,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 30.sp
                )
            )

            Spacer(modifier = Modifier.height(15.dp))

            CountHistoryList(mainViewModel)
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountHistoryList(mainViewModel: MainViewModel){
    // use remember incase
    val roomResult = mainViewModel.roomOperationResult.collectAsState()


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(White)

    ){
        Log.i("show count history", "show")
        when(val result = roomResult.value){
            Idle -> { Log.i("getting counts", "idle") }

            Loading -> { Log.i("getting counts", "loading") }

            is Success-> {
                val count = result.data
                if (count.isEmpty()) {
                    Log.i("show count history 1", "$count")
                    Text("No counts available",
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.Center))
                } else {
                    Log.i("show count history 2", "$count")
                    LazyColumn {
                        items(
                            items = count,
                            key = { it.id }
                        ) { count ->

                            SwipeToDelete(
                                onDelete = {
                                    mainViewModel.deleteCount(count.id)
                                },
                                countItem = {
                                    CountItem(count)
                                }
                            )

                        }
                    }

                }
            }

            is Error -> {
                val errorMessage = result.message
                Log.i("getting counts", errorMessage)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDelete(
    onDelete:() -> Unit,
    countItem: @Composable () -> Unit
){
    val dismissState = rememberSwipeToDismissBoxState()

    // Handle state changes
    if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
        LaunchedEffect(Unit) {
            onDelete()
            dismissState.reset()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red)
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        countItem()
    }
}


@Composable
private fun CountItem(count: Count){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(White)
    ){
        Box(
            modifier = Modifier
                .weight(0.15f),
            contentAlignment = Alignment.Center
        ){
            if(count.countType == CountType.INDIVIDUAL.type){
                Common.IconButtonDesign(
                    iconId = R.drawable.ic_profile0,
                    iconColor = Black,
                    backgroundColor = White,
                    onIconClick = {}
                )
            }
            if(count.countType == CountType.SESSION_COUNT.type){
                Common.IconButtonDesign(
                    iconId = R.drawable.ic_multiple_profiles,
                    iconColor = Black,
                    backgroundColor = White,
                    onIconClick = {}
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(0.75f)
                .padding(end = 16.dp)
        ){
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceBetween
            ){
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ){
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ){
                        Text(
                            text = count.countName,
                            style = TextStyle(
                                color = Black,
                                fontFamily = onest,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Text(
                                text = "${count.count}",
                                style = TextStyle(
                                    color = Black,
                                    fontFamily = onest,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 19.sp
                                )
                            )
                            Text(
                                text = "ppl",
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

                Spacer(modifier = Modifier.height(7.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ){
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ){
                        Text(
                            text = formatTimeAgo(count.date!!),
                            style = TextStyle(
                                color = Black.copy(alpha = 0.4f),
                                fontFamily = onest,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        )

                        Text(
                            text = "${count.countType}",
                            style = TextStyle(
                                color = Pink,
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

fun formatTimeAgo(timestamp: Timestamp): String {
    val now = Date()
    val createdAt = timestamp.time

    // Use DateUtils to format the duration
    return DateUtils.getRelativeTimeSpanString(
        createdAt,
        now.time,
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
}
