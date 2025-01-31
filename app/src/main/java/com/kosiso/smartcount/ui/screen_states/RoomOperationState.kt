package com.kosiso.smartcount.ui.screen_states

sealed class RoomOperationState<out T> {
    data object Loading : RoomOperationState<Nothing>()
    data class Success<T>(val data: T) : RoomOperationState<T>()
    data class Error(val message: String) : RoomOperationState<Nothing>()
}