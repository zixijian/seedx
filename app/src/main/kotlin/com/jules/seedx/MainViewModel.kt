package com.jules.seedx

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    var seed by mutableStateOf("")
    var centerX by mutableStateOf("0")
    var centerZ by mutableStateOf("0")
    var range by mutableStateOf("10000")
    var maxDist by mutableStateOf("64")
    var selectedType by mutableStateOf(0) // 0: Witch Hut, 1: Fortress

    var results by mutableStateOf<List<String>>(emptyList())
    var isSearching by mutableStateOf(false)

    private val native = SeedXNative()

    fun search() {
        val s = seed.toLongOrNull() ?: 0L
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
