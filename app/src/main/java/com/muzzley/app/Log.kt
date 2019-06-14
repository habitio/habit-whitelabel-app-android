package com.muzzley.app

import com.muzzley.Constants
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object Log {

    private val sdf = SimpleDateFormat(Constants.DATE_FORMAT, Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

//    private val logFile = File("cache/logfile.txt").apply {
//        Timber.d("file: ${this.absolutePath}")
//        parentFile.mkdirs()
//        if (!exists()) {
//            createNewFile()
//        }
//    }
//
    val logFile = createTempFile("log")

    private val writer = logFile.bufferedWriter()
    private val tree = object: Timber.DebugTree() {


        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {

            writer.write("${sdf.format(Date())} $priority/$tag: $message")
            if (t != null) {
                writer.newLine()
                writer.write("$t")
            }
            writer.newLine()
        }
    }


    fun init() {

        Timber.d("tmpFile: ${logFile.absolutePath}")
        Timber.plant(tree)
    }
}