package com.kosiso.smartcount.ui.screen_states

sealed class MainOperationState<out T> {
    data object Idle : MainOperationState<Nothing>()
    data object Loading : MainOperationState<Nothing>()
    data class Success<T>(val data: T) : MainOperationState<T>()
    data class Error(val message: String) : MainOperationState<Nothing>()
}