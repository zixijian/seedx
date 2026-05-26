package com.jules.seedx

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("seedx_prefs", Context.MODE_PRIVATE)

    var seed by mutableStateOf(prefs.getString("last_seed", "") ?: "")
    var centerX by mutableStateOf("0")
    var centerZ by mutableStateOf("0")
    var range by mutableStateOf("10000")
    var maxDist by mutableStateOf("64")
    var selectedType by mutableStateOf(0)

    var results by mutableStateOf<List<String>>(emptyList())
    var isSearching by mutableStateOf(false)

    private val native = SeedXNative()

    fun onSeedChange(newSeed: String) {
        seed = newSeed
        prefs.edit().putString("last_seed", newSeed).apply()
    }

    fun search() {
        val sStr = seed.trim()
        // Save again on search to be sure
        prefs.edit().putString("last_seed", sStr).commit()

        val s = sStr.toLongOrNull() ?: 0L
        val cx = centerX.toIntOrNull() ?: 0
        val cz = centerZ.toIntOrNull() ?: 0
        val r = range.toIntOrNull() ?: 10000
        val d = maxDist.toIntOrNull() ?: 64
        val type = selectedType

        isSearching = true
        viewModelScope.launch(Dispatchers.Default) {
            val res = native.findQuadStructures(s, cx, cz, r, d, type)
            results = res.toList()
            isSearching = false
        }
    }
}
