package com.june.onetapsms.accessibility

import com.june.onetapsms.SharedPrefManager

object Codes {
    private val smart = arrayOf("5", "4", "1", "1")
    private val tnt = arrayOf("5", "4", "1", "1")
    private val globe = arrayOf("8", "2", "1")
    private val tm = arrayOf("11", "1", "9", "1")

    fun codes() = when (SharedPrefManager.network) {
        0 -> smart
        1 -> tnt
        2 -> globe
        else -> tm
    }

    var index = 0
}