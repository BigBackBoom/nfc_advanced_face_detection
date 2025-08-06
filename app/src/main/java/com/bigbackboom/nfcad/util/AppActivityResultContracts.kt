package com.bigbackboom.nfcad.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract

object AppActivityResultContracts {
    class PickImage : ActivityResultContract<Unit, Uri?>() {
        override fun createIntent(context: Context, input: Unit): Intent = Intent(
            Intent.ACTION_PICK
        ).setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            if (intent == null || resultCode != Activity.RESULT_OK) return null
            return intent.data
        }
    }
}
