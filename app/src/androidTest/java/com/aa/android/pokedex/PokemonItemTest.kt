package com.aa.android.pokedex

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test


class PokemonItemTest{
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun pokemonItemDisplaysCapitalizedPokemonName(){
        composeTestRule.setContent {
            PokemonItem(
                pokemon = "charizard",
                isLoading = false,
                onPokemonClick = {}
            )
        }
        composeTestRule.onNodeWithText("Charizard").assertIsDisplayed()
    }
}