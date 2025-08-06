package com.bigbackboom.nfc.myna

import android.util.Log

class VisualAp(private val reader: Reader) {

    data class Attributes(
        val birth: String?,
        val sex: String?,
        val publicKey: ByteArray?,
        val name: ByteArray?,
        val address: ByteArray?,
        val photo: ByteArray?,
        val signature: ByteArray?,
        val expire: String?,
        val code: ByteArray?
    )

    fun lookupPinA(): Int {
        reader.selectEF("0013".hexToByteArray()) // Visual Information EF
        val lookupPin = reader.lookupPin()
        Log.d(LOG_TAG, "lookupPin: $lookupPin")
        return lookupPin
    }


    fun verifyPinA(mynumber: String): Boolean {
        reader.selectEF("0013".hexToByteArray()) // Visual Information EF
        val verifyPin = reader.verify(mynumber)
        Log.d(LOG_TAG, "verifyPin: pin verification $verifyPin")
        return verifyPin
    }

    fun getVisualInfo(): Attributes? {
        reader.selectEF("0002".hexToByteArray()) // Visual Information EF

        val tempData = reader.readBinary(7)
        val sizeToRead = tempData.asn1FrameIterator().next().frameSize
        if (sizeToRead <= 0) return null

        // 全体を読み込む
        val wrappedData = reader.readBinary(sizeToRead)
        val wrappedFrame = wrappedData.asn1FrameIterator().next()

        var birth: String? = null
        var sex: String? = null
        var publicKey: ByteArray? = null
        var name: ByteArray? = null
        var address: ByteArray? = null
        var photo: ByteArray? = null
        var signature: ByteArray? = null
        var expire: String? = null
        var code: ByteArray? = null

        wrappedFrame.value?.asn1FrameIterator()?.forEach { frame ->
            val value = frame.value ?: return@forEach

            when (frame.tag) {
                TAG_BIRTH -> {
                    val valueString = String(value)
                    Log.d(LOG_TAG, "Birth: $valueString")
                    birth = valueString
                }
                TAG_SEX -> {
                    val valueString = String(value)
                    Log.d(LOG_TAG, "Sex: $valueString")
                    sex = valueString
                }
                TAG_PUBLIC_KEY -> {
                    Log.d(LOG_TAG, "Public Key: $value")
                    publicKey = value
                }
                TAG_NAME -> {
                    Log.d(LOG_TAG, "Name: $value")
                    name = value
                }
                TAG_ADDRESS -> {
                    Log.d(LOG_TAG, "Address: $value")
                    address = value
                }
                TAG_PHOTO -> {
                    Log.d(LOG_TAG, "Photo: $value")
                    photo = value
                }
                TAG_SIGNATURE -> {
                    Log.d(LOG_TAG, "Signature: $value")
                    signature = value
                }
                TAG_EXPIRED_DATE -> {
                    val valueString = String(value)
                    Log.d(LOG_TAG, "Expired Date: $valueString")
                    expire = valueString
                }
                TAG_CODE -> {
                    Log.d(LOG_TAG, "Code: $value")
                    code = value
                }
            }
        }

        return Attributes(
            birth = birth,
            sex = sex,
            publicKey = publicKey,
            name = name,
            address = address,
            photo = photo,
            signature = signature,
            expire = expire,
            code = code,
        )
    }

    companion object {
        private const val TAG_BIRTH = 0x22 // 生年月日
        private const val TAG_SEX = 0x23 // 性別
        private const val TAG_PUBLIC_KEY = 0x24 // 公開鍵
        private const val TAG_NAME = 0x25 // 氏名
        private const val TAG_ADDRESS = 0x26 // 住所
        private const val TAG_PHOTO = 0x27 // 写真
        private const val TAG_SIGNATURE = 0x28 // 署名
        private const val TAG_EXPIRED_DATE = 0x29 // 有効期限
        private const val TAG_CODE = 0x2A // PIN

        private const val LOG_TAG = "VisualAP"
    }
}
