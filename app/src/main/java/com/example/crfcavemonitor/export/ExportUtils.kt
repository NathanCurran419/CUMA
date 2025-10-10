// export/ExportUtils.kt

package com.example.crfcavemonitor.export

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri

fun ContentResolver.takePersistablePermissionIfPossible(uri: Uri) {
    try {
        takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    } catch (_: SecurityException) {
    } catch (_: IllegalArgumentException) {
    }
}