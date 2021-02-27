package com.june.onetapsms

import com.pixplicity.easyprefs.library.Prefs

object SharedPrefManager {
    private const val TAG = "SharedPrefManager"
    private const val FLAG_INITIAL_SETUP = "FLAG_INITIAL_SETUP"
    private const val NAME = "NAME"
    private const val CONFIRMATION_NUMBER = "CONFIRMATION_NUMBER"
    private const val RA_NUMBER = "RA_NUMBER"
    private const val NETWORK = "NETWORK"

    var isInitialSetup: Boolean
        get() = Prefs.getBoolean(FLAG_INITIAL_SETUP, false)
        set(value) = Prefs.putBoolean(FLAG_INITIAL_SETUP, value)

    var name: String
        get() = Prefs.getString(NAME, "")
        set(value) = Prefs.putString(NAME, value)

    var confirmationNumber: String
        get() = Prefs.getString(CONFIRMATION_NUMBER, "")
        set(value) = Prefs.putString(CONFIRMATION_NUMBER, value)

    var raNumber: String
        get() = Prefs.getString(RA_NUMBER, "")
        set(value) = Prefs.putString(RA_NUMBER, value)

    var network: Int
        get() = Prefs.getInt(NETWORK, 0)
        set(value) = Prefs.putInt(NETWORK, value)
}