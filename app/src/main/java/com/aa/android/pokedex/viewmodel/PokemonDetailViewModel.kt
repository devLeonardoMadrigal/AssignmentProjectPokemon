package com.aa.android.pokedex.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aa.android.pokedex.api.entity.PokemonDTO
import com.aa.android.pokedex.model.UiState
import com.aa.android.pokedex.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel(){

    private val _pokemonDetail = MutableLiveData<UiState<PokemonDTO>>()
    val pokemonDetail: LiveData<UiState<PokemonDTO>> = _pokemonDetail

    fun fetchPokemon(name: String){

        _pokemonDetail.value = UiState.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = repository.getPokemon(name)
                _pokemonDetail.postValue(UiState.Ready(data))
            } catch (e: Exception){
                _pokemonDetail.postValue(UiState.Error(e))
            }
        }
    }
}