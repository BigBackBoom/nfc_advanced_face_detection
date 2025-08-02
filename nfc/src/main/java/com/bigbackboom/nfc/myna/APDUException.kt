package com.bigbackboom.nfc.myna

class APDUException(val sw1: Byte, val sw2: Byte): Exception()