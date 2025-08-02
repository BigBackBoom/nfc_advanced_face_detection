package com.pay.nfc.util

import com.bigbackboom.nfc.log.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class LogAssistance(): Logger {
    private val _logList = MutableStateFlow<List<String>>(emptyList())
    override val logList: StateFlow<List<String>> = _logList

    override fun putLogMessage(message: String) {
        _logList.update { currentList -> currentList + message }
    }
}