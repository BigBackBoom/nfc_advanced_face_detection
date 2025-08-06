package com.bigbackboom.nfcad.mynainfo

import android.graphics.Bitmap
import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.bigbackboom.nfc.myna.Reader
import com.bigbackboom.nfc.myna.TextAP
import com.gemalto.jp2.JP2Decoder
import com.pay.nfc.util.LogAssistance
import java.io.IOException
import androidx.core.graphics.createBitmap

@Composable
fun MynaInfoScreen(onBackClick: () -> Unit) {
    MynaInfoContent(onBackClick)
}

fun getNfcCallback(pin: String, logAssistance: LogAssistance, update: (Bitmap) -> Unit) = object : NfcAdapter.ReaderCallback {
    override fun onTagDiscovered(tag: Tag?) {
        logAssistance.putLogMessage("onTagDiscovered() $tag")
        if (tag == null) return
        val reader = Reader(tag, logAssistance)
        try {
            logAssistance.putLogMessage("connect...")
            reader.connect()
        } catch (e: Exception) {
            logAssistance.putLogMessage("Failed to connect $e")
            return
        }
        onConnect(pin, reader, logAssistance, update)
        try {
            logAssistance.putLogMessage("reader close...")
            reader.close()
        } catch (e: IOException) {
            logAssistance.putLogMessage("Failed to close reader ${e.message}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MynaInfoContent(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
    val logAssistance = LogAssistance()
    var pin by remember { mutableStateOf<String>("") }
    var bmp by remember { mutableStateOf<Bitmap?>(null) }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        try {
            nfcAdapter.disableReaderMode(context as android.app.Activity)
        } catch (e: Exception) {
            logAssistance.putLogMessage("disableReaderMode exception $e")
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Read MyNumber")
                },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "description"
                        )
                    }
                })
        }

    ) { paddingValues ->
        val logList = logAssistance.logList.collectAsState().value
        val callback = getNfcCallback(pin, logAssistance) {
            bmp = it
            logAssistance.putLogMessage("Image updated")
        }
        val state = rememberScrollState()
        var isListening by remember { mutableStateOf(false) }
        val focusManager = LocalFocusManager.current
        Column(modifier = Modifier.padding(paddingValues)) {
            Column(
                Modifier
                    .padding(16.dp)
                    .verticalScroll(state)
            ) {
                Row {
                    TextField(
                        value = pin,
                        onValueChange = { pin = it },
                        label = { Text("Give your Pin") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    Button(
                        {
                            focusManager.clearFocus()
                            logAssistance.putLogMessage("start listening..")
                            nfcAdapter.enableReaderMode(
                                context as android.app.Activity,
                                callback,
                                NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                                null
                            )
                            isListening = true
                        },
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                        enabled = isListening.not() && pin.length == 4
                    ) {
                        Text("Start")
                    }
                }

                logList.forEach {
                    Text(it)
                }

                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    bitmap = bmp?.asImageBitmap() ?: createBitmap(1, 1).asImageBitmap(),
                    contentDescription = "MyNumber Card Image"
                )
            }
        }
    }
}

private fun onConnect(pin: String, reader: Reader, logAssistance: LogAssistance, update: (Bitmap) -> Unit) {
    try {
        procedure(pin, reader, logAssistance, update)
    } catch (e: Exception) {
        logAssistance.putLogMessage("error connection $e")
    }
}

private fun procedure(pin: String, reader: Reader, logAssistance: LogAssistance, update: (Bitmap) -> Unit): Result {
    if (pin.length != 4) {
        return Result(ResultStatus.ERROR_INSUFFICIENT_PIN)
    }

    // select ap
    val textAP = reader.selectTextAp()

    // remaining pin count
    val count = textAP.lookupPin()
    logAssistance.putLogMessage("remaining count $count")
    if (count == 0) {
        return Result(ResultStatus.ERROR_TRY_COUNT_IS_NOT_LEFT)
    }

    // pin verify
    if (!textAP.verifyPin(pin)) {
        logAssistance.putLogMessage("verifyPin failed")
        return Result(ResultStatus.ERROR_INCORRECT_PIN, count - 1)
    }
    logAssistance.putLogMessage("verifyPin success")
    // my_number check
    val myNumber = textAP.readMyNumber()
    logAssistance.putLogMessage("readMyNumber success")
    // read other details
    val attributes = textAP.readAttributes()
    val name = attributes?.name
    val dob = attributes?.birth
    logAssistance.putLogMessage("$name, $dob")

    // select visual ap
    val visualAp = reader.selectVisualAp()

    // remaining pin count
    val vCount = visualAp.lookupPinA()
    logAssistance.putLogMessage("VisualAP remaining count $vCount")

    if(!visualAp.verifyPinA(myNumber)) {
        logAssistance.putLogMessage("verifyPin failed")
        return Result(ResultStatus.ERROR_INCORRECT_PIN, count - 1)
    }

    val visualAttr = visualAp.getVisualInfo()
    val bmp = JP2Decoder(visualAttr?.photo).decode()
    update(bmp)

    return Result(ResultStatus.SUCCESS, count, myNumber, attributes)
}

data class Result(
    val status: ResultStatus,
    val tryCountRemain: Int? = null,
    val myNumber: String? = null,
    val attributes: TextAP.Attributes? = null,
)

enum class ResultStatus {
    SUCCESS,
    ERROR_TRY_COUNT_IS_NOT_LEFT,
    ERROR_INSUFFICIENT_PIN,
    ERROR_INCORRECT_PIN
}