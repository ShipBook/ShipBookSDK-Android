package io.shipbook.shipbooksdk.Util

import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// Plain writeText truncates in place; a kill between truncate and flush leaves a partially written file behind.
internal fun File.writeTextAtomically(text: String) {
    val temp = File(parentFile, "$name.tmp")
    FileOutputStream(temp).use {
        it.write(text.toByteArray())
        it.fd.sync()
    }
    if (!temp.renameTo(this)) throw IOException("failed to rename ${temp.name} to $name")
}
