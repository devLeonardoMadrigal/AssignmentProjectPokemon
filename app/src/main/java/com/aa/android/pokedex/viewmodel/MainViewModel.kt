package com.aa.android.pokedex.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.aa.android.pokedex.model.UiState
import com.aa.android.pokedex.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: PokemonRepository
): ViewModel() {

    val pokemonLiveData: LiveData<UiState<List<String>>> = liveData(Dispatchers.IO) {
        emit(UiState.Loading())
        try {
            val data = repository.getAllPokemon()
            emit(UiState.Ready(data))
        } catch (e: Exception) {
            Log.e(this@MainViewModel::class.simpleName, e.message, e)
            emit(UiState.Error(e))
        }
    }
}