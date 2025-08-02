package com.bigbackboom.nfc.log

import kotlinx.coroutines.flow.StateFlow

interface Logger {
    val logList: StateFlow<List<String>>

    fun putLogMessage(message: String)
}