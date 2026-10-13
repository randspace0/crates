package io.github.andraantariksa.crates.ui.crate

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.andraantariksa.crates.data.CrateDetail
import io.github.andraantariksa.crates.data.CratesApi
import io.github.andraantariksa.crates.data.Dependency
import io.github.andraantariksa.crates.data.Owner
import io.github.andraantariksa.crates.ui.CrateRoute
import io.github.andraantariksa.crates.ui.Load
import io.github.andraantariksa.crates.ui.load
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CrateViewModel(handle: SavedStateHandle, private val api: CratesApi) : ViewModel() {
    val name = handle.toRoute<CrateRoute>().name

    private val _detail = MutableStateFlow<Load<CrateDetail>>(Load.Loading)
    val detail = _detail.asStateFlow()

    /** Rendered README HTML, or null when the crate has none. */
    private val _readme = MutableStateFlow<Load<String?>>(Load.Loading)
    val readme = _readme.asStateFlow()

    private val _dependencies = MutableStateFlow<Load<List<Dependency>>>(Load.Loading)
    val dependencies = _dependencies.asStateFlow()

    private val _owners = MutableStateFlow<List<Owner>>(emptyList())
    val owners = _owners.asStateFlow()

    init {
        reload()
    }

    fun reload() {
        _detail.value = Load.Loading
        viewModelScope.launch {
            val detail = load { api.crate(name) }
            _detail.value = detail
            if (detail !is Load.Ready) return@launch
            val version = detail.data.crate.version
            launch {
                _readme.value = load {
                    val response = api.readme(name, version)
                    if (response.isSuccessful) response.body()?.string() else null
                }
            }
            launch { _dependencies.value = load { api.dependencies(name, version).dependencies } }
            launch { _owners.value = runCatching { api.owners(name).users }.getOrDefault(emptyList()) }
        }
    }
}
