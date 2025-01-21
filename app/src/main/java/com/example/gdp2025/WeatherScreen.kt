package com.example.gdp2025

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.gdp2025.R.*
import com.example.gdp2025.RetrofitInstance.weatherApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.rememberCoroutineScope as rememberCoroutineScope1
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp


@Composable
fun WeatherScreen() {
    var city by remember { mutableStateOf("Navi Mumbai") }
    val apiKey = "324bd9f23ac0d46f5f23582be6b51c5c"
    var weatherResponse by remember { mutableStateOf<WeatherResponse?>(null) }

    LaunchedEffect(city) {

        try {
            val response = weatherApi.getWeather(city, apiKey)
            weatherResponse = response
        } catch (e: Exception) {
            var errorMessage =
                "Failed to fetch weather data. Please check your internet connection."
            Log.e("WeatherScreen", "Error fetching weather data: $e")
        }

    }

    weatherResponse?.let { weather ->
        val temperature = weather.main.temp.toInt()
        val weatherCondition = weather.weather.first().description.capitalize(Locale.getDefault())
        val time = SimpleDateFormat("EEE, HH:mm", Locale.getDefault()).format(Date())

        Box(modifier = Modifier.fillMaxSize()
            )
        {
            Image(painter = getBackgroundForWeather(mapWeatherToType(weatherCondition)), contentDescription = null, modifier = Modifier
                .fillMaxSize()
                .blur(5.dp), contentScale = ContentScale.FillBounds)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SearchBar(onCityChange = {
                newCity-> city = newCity
            })
            Spacer(modifier = Modifier.height(32.dp))
            LocationIndicator(city)
            Spacer(modifier = Modifier.height(32.dp))
            val description = weather.weather.first().description
            val weatherType = mapWeatherToType(description)
            val warningMessage = generateWarningMessage(weatherType)
            LottieWeatherAnimation(weatherType)
            Spacer(modifier = Modifier.height(64.dp))
            WeatherInfo(temperature, weatherCondition, time)
            Spacer(modifier = Modifier.height(64.dp))
            WarningMessage(warningMessage)
        }
    }

}
@Composable
fun SearchBar(onCityChange: (String) -> Unit) {
    val textState = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope1()
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    OutlinedTextField(
        value = textState.value,
        onValueChange = {
            textState.value = it
            debounceJob?.cancel()
            debounceJob = coroutineScope.launch {
                delay(500) // Debounce delay
                onCityChange(it)
            }
        },
        placeholder = { Text("Search for a City/Town") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        shape = RoundedCornerShape(8.dp),
        singleLine = true, // Ensure single-line input
        modifier = Modifier.fillMaxWidth()
                           .background(Color.White)
            .padding(8.dp),
        textStyle = TextStyle(
            color = Color.Black,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp

        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                debounceJob?.cancel()
                debounceJob = coroutineScope.launch {
                    onCityChange(textState.value)
                }
            }
        ),
    )


}

@Composable
fun getBackgroundForWeather(weatherType: WeatherType): Painter {
    return when(weatherType){
        WeatherType.SUNNY -> painterResource(id = drawable.sunny_background)
        WeatherType.CLOUDY -> painterResource(id = drawable.cloud)
        WeatherType.RAINY -> painterResource(id = drawable.rain_background)
        WeatherType.STORMY -> painterResource(id = drawable.stormy_background)
    }
}

fun mapWeatherToType(description: String): WeatherType {
    return when {
        description.contains("clear", ignoreCase = true) -> WeatherType.SUNNY
        description.contains("cloud", ignoreCase = true) -> WeatherType.CLOUDY
        description.contains("rain", ignoreCase = true) -> WeatherType.RAINY
        description.contains("storm", ignoreCase = true) -> WeatherType.STORMY
        else -> WeatherType.SUNNY
    }

}
fun generateWarningMessage(weatherType: WeatherType): String {
    return when (weatherType) {
        WeatherType.SUNNY -> "Sunny Outside. Stay Hydrated!"
        WeatherType.RAINY -> "Don't forget to take your umbrella!"
        WeatherType.CLOUDY -> "Perfect for Skye Pics!"
        WeatherType.STORMY -> "Lightening Outside.Stay Home!"
    }
}



@Composable
fun LottieWeatherAnimation(weatherType: WeatherType, modifier: Modifier = Modifier) {
    val animationRes = when (weatherType) {
        WeatherType.SUNNY -> raw.sunny_animation
        WeatherType.CLOUDY -> raw.cloudy_animation
        WeatherType.RAINY -> raw.rainy_animation
        WeatherType.STORMY -> raw.stormy_animation
    }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
        composition = composition,
        progress = progress,
        modifier = modifier.size(250.dp)
    )
}

@Preview
@Composable
fun WeatherScreenPreview() {
    WeatherScreen()
}