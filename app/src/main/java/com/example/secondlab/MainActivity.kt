package com.example.secondlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.pow

// корисні копалини
data class Minerals(
    val coal: Double = 0.0,
    val mazut: Double = 0.0,
    val gas: Double = 0.0,
)

// результати розрахунків
data class CalculationResults(
    val coalEmissionFactor: Double = 0.0,
    val coalEmissionValue: Double = 0.0,
    val mazutEmissionFactor: Double = 0.0,
    val mazutEmissionValue: Double = 0.0,
    val gasEmissionFactor: Double = 0.0,
    val gasEmissionValue: Double = 0.0,
)

@Composable
fun CalculatorScreen(
) {
    var minerals by remember { mutableStateOf(Minerals()) }
    var results by remember { mutableStateOf<CalculationResults?>(null) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Калькулятор валових викидів",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        InputField("Вугілля, т", minerals.coal) { minerals = minerals.copy(coal = it) }
        InputField("Мазут, т", minerals.mazut) { minerals = minerals.copy(mazut = it) }
        InputField("Газ, тис.м^3", minerals.gas) { minerals = minerals.copy(gas = it) }

        Button(
            onClick = { results = calculateResults(minerals) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .size(width = 300.dp, height = 50.dp),

            ) {
            Text("Розрахувати")
        }

        results?.let { DisplayResults(it) }
    }
}

@Composable
fun InputField(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit
) {
    OutlinedTextField(
        value = if (value == 0.0) "" else value.toString(),
        onValueChange = {
            onValueChange(it.toDoubleOrNull() ?: 0.0)
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

@Composable
fun DisplayResults(results: CalculationResults) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            "Результати розрахунків:",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ResultSection("Спалювання вугілля:") {
            ResultItem("Показник емісії твердих частинок", results.coalEmissionFactor, "г/ГДж")
            ResultItem("Валовий викид", results.coalEmissionValue, "т")
        }

        ResultSection("Спалювання мазуту:") {
            ResultItem("Показник емісії твердих частинок", results.mazutEmissionFactor, "г/ГДж")
            ResultItem("Валовий викид", results.mazutEmissionValue, "т")
        }

        ResultSection("Спалювання газу:") {
            ResultItem("Показник емісії твердих частинок", results.gasEmissionFactor, "г/ГДж")
            ResultItem("Валовий викид", results.gasEmissionValue, "т")
        }

    }
}

@Composable
fun ResultSection(title: String, content: @Composable () -> Unit) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
    content()
}

@Composable
fun ResultItem(label: String, value: Double, sign: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(String.format("%.2f", value) + " " + sign)
    }
}

private fun calculateResults(minerals: Minerals): CalculationResults {
    // нижча теплота згоряння робочої маси вугілля
    val coalHeatValue = 20.47
    // нижча теплота згоряння робочої маси мазуту
    val mazutHeatValue = 39.48

    // частка золи, яка виходить з котла у вигляді леткої золи (вугілля)
    val aCoal = 0.8
    // частка золи, яка виходить з котла у вигляді леткої золи (мазут)
    val aMazut = 1

    // масовий вміст горючих речовин у леткій золі (вугілля)
    val flammableSubstancesCoal = 1.5
    // масовий вміст горючих речовин у леткій золі (мазут)
    val flammableSubstancesMazut = 0

    // масовий вміст золи в паливі на робочу масу, % (вугілля)
    val arCoal = 25.2
    // масовий вміст золи в паливі на робочу масу, % (мазут)
    val arMazut = 0.15

    // ефективність очищення димових газів від твердих частинок
    val n = 0.985

    // емісія твердих частинок (вугілля)
    val coalEmissionFactor = (10.0.pow(6) / coalHeatValue) * aCoal * (arCoal / (100 - flammableSubstancesCoal)) * (1 - n)
    // валовий викид твердих частинок (вугілля)
    val coalEmissionValue = 10.0.pow(-6) * coalEmissionFactor * coalHeatValue * minerals.coal

    // емісія твердих частинок (мазут)
    val mazutEmissionFactor = (10.0.pow(6) / mazutHeatValue) * aMazut * (arMazut / (100 - flammableSubstancesMazut)) * (1 - n)
    // валовий викид твердих частинок (мазут)
    val mazutEmissionValue =
        10.0.pow(-6) * mazutEmissionFactor * mazutHeatValue * minerals.mazut

    // при спалюванні природного газу тверді частинки відсутні, тоді
    // емісія твердих частинок (газ)
    val gasEmissionFactor = 0.0
    // валовий викид твердих частинок (газ)
    val gasEmissionValue = 0.0

    return CalculationResults(
        coalEmissionFactor = coalEmissionFactor,
        coalEmissionValue = coalEmissionValue,
        mazutEmissionFactor = mazutEmissionFactor,
        mazutEmissionValue = mazutEmissionValue,
        gasEmissionFactor = gasEmissionFactor,
        gasEmissionValue = gasEmissionValue,
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorScreen()
        }
    }
}



