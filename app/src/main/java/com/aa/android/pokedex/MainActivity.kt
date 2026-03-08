package com.aa.android.pokedex

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.aa.android.pokedex.api.entity.PokemonDTO
import com.aa.android.pokedex.model.Type
import com.aa.android.pokedex.model.UiState
import com.aa.android.pokedex.ui.theme.PokedexTheme
import com.aa.android.pokedex.viewmodel.MainViewModel
import com.aa.android.pokedex.viewmodel.PokemonDetailViewModel
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val detailViewModel: PokemonDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokedexTheme {

                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "HomeScreen"){

                    composable("HomeScreen") {
                        Screen(
                            pokemon = mainViewModel.pokemonLiveData,
                            onPokemonClick = { pokemonName ->
                                detailViewModel.fetchPokemon(pokemonName)
                                navController.navigate("PokemonDetails")
                            }
                        )
                    }
                    composable("PokemonDetails") {
                        PokemonDetailScreen(
                            pokemonDetailsLiveData = detailViewModel.pokemonDetail,
                            onBackClick = { navController.navigateUp() }
                        )
                    }

                }
            }
        }
    }
}

@Composable
fun Screen(pokemon: LiveData<UiState<List<String>>>, onPokemonClick: (String) -> Unit = {}) {
    Scaffold(topBar = {
        TopAppBar(backgroundColor = MaterialTheme.colors.primary, title = {
            Image(painter = painterResource(id = R.drawable.pokemon_logo), null)
        })
    }) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            color = MaterialTheme.colors.background
        ) {
            //Text(pokemon) //TODO
            PokemonList(pokemon = pokemon, onPokemonClick = onPokemonClick)
        }
    }
}

@Composable
fun PokemonList(pokemon: LiveData<UiState<List<String>>>, onPokemonClick: (String) -> Unit) {
    val uiState: UiState<List<String>>? by pokemon.observeAsState()
    LazyColumn(modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        uiState?.let {
            when (it) {
                is UiState.Loading -> {
                    items(20) {
                        PokemonItem(pokemon = "", isLoading = true)
                    }
                }
                is UiState.Ready -> {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Total Pokemon fetched: ${it.data.size}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                    }

                    items(it.data) { pkmn ->
                        PokemonItem(pokemon = pkmn, isLoading = false, onPokemonClick = { onPokemonClick(pkmn) })
                    }
                }
                is UiState.Error -> {
                    item {
                        Text(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            textAlign = TextAlign.Center,
                            text = "Error loading list. Please try again later.",
                            color = MaterialTheme.colors.onBackground
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PokemonItem(pokemon: String, isLoading: Boolean, onPokemonClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
            .placeholder(
                visible = isLoading,
                highlight = PlaceholderHighlight.shimmer(),
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        onClick = onPokemonClick
    ) {
        Text(text = pokemon.capitalize(Locale.current), modifier = Modifier.padding(12.dp), textAlign = TextAlign.Center)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PokedexTheme {
        Screen(MutableLiveData(UiState.Ready(listOf("one", "two", "three"))))
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun PokemonDetailScreen(
    pokemonDetailsLiveData : LiveData<UiState<PokemonDTO>>,
    onBackClick: () -> Unit
) {
    val uiState by pokemonDetailsLiveData.observeAsState(UiState.Loading())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("Details screen")},
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary,
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
        uiState.let {
            when(it) {
                is UiState.Error ->  {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        textAlign = TextAlign.Center,
                        text = "Error loading Pokemon. Please try again later.",
                        color = MaterialTheme.colors.onBackground
                    )
                }
                is UiState.Loading -> CircularProgressIndicator()
                is UiState.Ready -> {


                        Text(
                            it.data.name.capitalize(Locale.current),
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            AsyncImage(
                                model = it.data.sprites.defaultFront,
                                contentDescription = "Front sprite for ${it.data.name}",
                                modifier = Modifier.size(200.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Height: ", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "${String.format(format = "%.2f", it.data.height * 10 / 2.54)} ft",
                                fontSize = 24.sp
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Weight: ", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "${String.format(format = "%.2f", it.data.weight / 10 * 2.20462)} lbs",
                                fontSize = 24.sp
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Types: ")
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {

                                items(it.data.types) { currentType ->
                                    val backgroundColor = try {
                                        Type.getColor(currentType.type.name.uppercase())
                                    } catch (e: IllegalArgumentException) {
                                        Type.UNKNOWN.color
                                    }
                                    Text(
                                        currentType.type.name,
                                        modifier = Modifier.background(backgroundColor)
                                    )
                                }
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Stats:")
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(it.data.stats) { currentStat ->
                                    Row(Modifier.fillMaxWidth()) {
                                        Text("${currentStat.stat.name}:")
                                        Spacer(Modifier.size(12.dp))
                                        Text(currentStat.baseStat.toString())
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}