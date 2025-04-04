package com.example.bluetoothiot

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import com.example.bluetoothiot.ui.theme.BluetoothiotTheme

class DeviceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BluetoothiotTheme {
                DeviceScreen()
            }
        }
    }
}

@Composable
fun DeviceScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Select an Action")

        Spacer(modifier = Modifier.height(16.dp))

        val cardSize: Dp = 150.dp // Ensures square shape

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SquareCard(text = "HeartRate", size = cardSize, activityClass = HeartRateActivity::class.java)
            SquareCard(text = "SpO2", size = cardSize, activityClass = SpO2Activity::class.java)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SquareCard(text = "Weight", size = cardSize, activityClass = WeightActivity::class.java)

        }
    }
}

@Composable
fun SquareCard(text: String, size: Dp, activityClass: Class<*>) {
    val context = LocalContext.current // Get context inside Composable
    Card(
        modifier = Modifier
            .size(size)
            .padding(8.dp)
            .clickable {
                // Start the corresponding activity when the card is clicked
                val intent = Intent(context, activityClass)
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text)
        }
    }
}

// Placeholder for activities
class HeartRateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Text("Heart Rate Activity")
        }
    }
}

class SpO2Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Text("SpO2 Activity")
        }
    }
}

class WeightActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Text("Weight Activity")
        }
    }
}

