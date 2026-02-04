package com.example.dwhubfix.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Session Manager for user session data
 *
 * This class manages user session persistence using SharedPreferences.
 * It stores authentication tokens, user ID, selected role, and UI state.
 *
 * Note: Uses commit() (synchronous) for critical auth data to prevent data loss.
 * Uses apply() (asynchronous) for non-critical UI state.
 */
object SessionManager {
    private const val PREF_NAME = "user_session"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_PENDING_ROLE = "pending_role"
    private const val KEY_PHONE_NUMBER = "phone_number"
    private const val KEY_SELECTED_ROLE = "selected_role"
    private const val KEY_CURRENT_STEP = "current_step"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Save user session (access token and user ID)
     * Uses commit() for immediate persistence to prevent data loss
     */
    fun saveSession(context: Context, accessToken: String, userId: String) {
        // Use commit() for critical auth data to ensure it's written immediately
        // This prevents data loss if app is killed
        getPrefs(context).edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_USER_ID, userId)
            .commit()
    }

    fun savePendingRole(context: Context, role: String) {
        getPrefs(context).edit().putString(KEY_PENDING_ROLE, role).apply()
    }

    fun getPendingRole(context: Context): String? {
        return getPrefs(context).getString(KEY_PENDING_ROLE, null)
    }

    fun saveSelectedRole(context: Context, role: String) {
        getPrefs(context).edit().putString(KEY_SELECTED_ROLE, role).apply()
    }

    fun getSelectedRole(context: Context): String? {
        return getPrefs(context).getString(KEY_SELECTED_ROLE, null)
    }

    fun saveCurrentStep(context: Context, step: String) {
        getPrefs(context).edit().putString(KEY_CURRENT_STEP, step).apply()
    }

    fun getCurrentStep(context: Context): String? {
        return getPrefs(context).getString(KEY_CURRENT_STEP, null)
    }

    fun getAccessToken(context: Context): String? {
        return getPrefs(context).getString(KEY_ACCESS_TOKEN, null)
    }

    fun getUserId(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_ID, null)
    }

    fun clearSession(context: Context) {
        // Use commit() for critical auth operations
        getPrefs(context).edit().clear().commit()
    }

    fun savePhoneNumber(context: Context, phoneNumber: String) {
        getPrefs(context).edit().putString(KEY_PHONE_NUMBER, phoneNumber).apply()
    }

    fun getPhoneNumber(context: Context): String? {
        return getPrefs(context).getString(KEY_PHONE_NUMBER, null)
    }
}
