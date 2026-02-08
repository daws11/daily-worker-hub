package com.example.dwhubfix.data.repository.integration

import android.content.SharedPreferences

/**
 * Simple wrapper for in-memory SharedPreferences in tests
 * Avoids the complexity of implementing the full Android Context interface
 */
class TestSharedPreferencesProvider {

    private val sharedPrefs = InMemorySharedPreferences("user_session")

    fun getSharedPreferences(): SharedPreferences = sharedPrefs
    fun clearPreferences() { sharedPrefs.clear() }

    /**
     * In-memory SharedPreferences for testing
     */
    class InMemorySharedPreferences(private val name: String) : SharedPreferences {
        private val data = mutableMapOf<String, Any?>()
        private val listeners = mutableSetOf<SharedPreferences.OnSharedPreferenceChangeListener>()

        override fun getAll() = data.toMap()
        override fun getString(key: String?, defaultValue: String?) = data[key] as? String ?: defaultValue
        override fun getStringSet(key: String?, defaultValue: Set<String>?) = @Suppress("UNCHECKED_CAST") (data[key] as? Set<String> ?: defaultValue)
        override fun getInt(key: String?, defaultValue: Int) = data[key] as? Int ?: defaultValue
        override fun getLong(key: String?, defaultValue: Long) = data[key] as? Long ?: defaultValue
        override fun getFloat(key: String?, defaultValue: Float) = data[key] as? Float ?: defaultValue
        override fun getBoolean(key: String?, defaultValue: Boolean) = data[key] as? Boolean ?: defaultValue
        override fun contains(key: String?) = data.containsKey(key)
        override fun edit() = EditorImpl()
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) { listener?.let { listeners.add(it) } }
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) { listeners.remove(listener) }
        fun clear() { data.clear() }

        inner class EditorImpl : SharedPreferences.Editor {
            private val pendingChanges = mutableMapOf<String, Any?>()
            private var clearAll = false

            override fun putString(key: String?, value: String?) = apply { key?.let { pendingChanges[it] = value } }
            override fun putStringSet(key: String?, values: MutableSet<String>?) = apply { key?.let { pendingChanges[it] = values } }
            override fun putInt(key: String?, value: Int) = apply { key?.let { pendingChanges[it] = value } }
            override fun putLong(key: String?, value: Long) = apply { key?.let { pendingChanges[it] = value } }
            override fun putFloat(key: String?, value: Float) = apply { key?.let { pendingChanges[it] = value } }
            override fun putBoolean(key: String?, value: Boolean) = apply { key?.let { pendingChanges[it] = value } }
            override fun remove(key: String?) = apply { key?.let { pendingChanges[it] = null } }
            override fun clear() = apply { clearAll = true }

            override fun commit(): Boolean {
                applyPendingChanges()
                return true
            }

            override fun apply() = applyPendingChanges()

            private fun applyPendingChanges() {
                if (clearAll) { data.clear(); clearAll = false }
                for ((key, value) in pendingChanges) {
                    if (value == null) data.remove(key) else data[key] = value
                }
                pendingChanges.clear()
                for (listener in listeners) { listener.onSharedPreferenceChanged(this@InMemorySharedPreferences, null) }
            }
        }
    }
}

/**
 * Minimal Context stub that only implements getSharedPreferences
 * Uses wrapper pattern to avoid implementing all abstract methods
 */
class TestContextWrapper(private val prefsProvider: TestSharedPreferencesProvider) {

    /**
     * Get SharedPreferences - delegates to the provider
     */
    fun getSharedPreferences(name: String?, mode: Int): SharedPreferences {
        return prefsProvider.getSharedPreferences()
    }

    fun clearPreferences() {
        prefsProvider.clearPreferences()
    }
}
