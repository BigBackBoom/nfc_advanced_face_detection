package com.bigbackboom.nfc.myna

class VisualAp(private val reader: Reader) {

    fun getVisualInfo(pin: String) {
        reader.selectEF("0012".hexToByteArray()) // Visual Information EF
        reader.verify(pin)
    }
}
