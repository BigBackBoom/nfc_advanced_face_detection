package com.bigbackboom.nfc.myna

class ASN1Frame(
    val tag: Int,
    val length: Int,
    val frameSize: Int,
    val value: ByteArray? = null
)